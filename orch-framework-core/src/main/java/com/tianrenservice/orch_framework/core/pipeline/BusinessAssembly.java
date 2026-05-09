package com.tianrenservice.orch_framework.core.pipeline;

import com.tianrenservice.orch_framework.core.constant.BusinessMode;
import com.tianrenservice.orch_framework.core.entity.BusinessEntity;
import com.tianrenservice.orch_framework.core.exception.InterruptException;
import com.tianrenservice.orch_framework.core.record.model.InteractionRecord;
import com.tianrenservice.orch_framework.core.spi.*;
import com.tianrenservice.orch_framework.core.testcase.model.TestCaseVO;
import com.tianrenservice.orch_framework.core.util.CacheInvoke;
import com.tianrenservice.orch_framework.core.vo.BusinessVO;
import com.tianrenservice.orch_framework.core.vo.UserBusinessDealVO;
import com.tianrenservice.orch_framework.core.vo.UserBusinessVO;
import com.tianrenservice.orch_framework.core.vo.BusinessDealVO;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 组合线基类 - Pipeline 编排器
 */
@Setter
@Getter
@Slf4j
public abstract class BusinessAssembly implements AutoCloseable {

    private final String identity = "assembly-" + UUID.randomUUID();

    @Delegate
    private final CacheInvoke cacheInvoke = new CacheInvoke(false);

    public List<BusinessAssemblyUnit<?, ?, ?>> businessAssemblyUnits = new ArrayList<>();
    private BusinessAssemblyUnit<?, ?, ?> currentUnit = null;

    private BusinessMode mode = BusinessMode.LIVE;
    private String testCaseName;
    private TestCaseVO testCaseVO;
    private Map<String, BusinessVO> multiBusinessVO = new HashMap<>();
    private Map<String, Map<String, List<InteractionRecord>>> multiBusinessEnv = new HashMap<>();
    private Map<String, BusinessDealVO<?>> multiBusinessDealVO = new HashMap<>();

    // 步骤输出存储 — 用于步骤间类型安全的数据传递
    private final StepOutputStore outputStore = new StepOutputStore();

    // addStep 注册列表 — run() 时统一执行
    private final List<StepRegistration<?, ?, ?>> pendingSteps = new ArrayList<>();

    // SPI 依赖 - 通过静态方法配置
    private static TypeRegistry typeRegistry;
    private static JsonSerializer jsonSerializer;
    private TestCasePersistenceService testCasePersistenceService;

    public static void configure(TypeRegistry registry, JsonSerializer serializer) {
        typeRegistry = registry;
        jsonSerializer = serializer;
    }

    public static TypeRegistry getTypeRegistry() {
        return typeRegistry;
    }

    public static JsonSerializer getJsonSerializer() {
        return jsonSerializer;
    }

    public abstract String getAssemblyTypeCode();

    private int unitCount = 0;

    public void build(Class<? extends BusinessEntity> tClass, Class<? extends UserBusinessDealVO> vClass, UserBusinessVO businessVO) {
        String assemblyTypeCode = getAssemblyTypeCode();
        BusinessTypeIdentifier businessType = businessVO.getBusinessType();
        unitCount = unitCount + 1;

        // 尝试特化 doBuild（消费方 Assembly 子类重写的方法）
        Object unit = invokeBuild(assemblyTypeCode, businessType, tClass, vClass, businessVO);
        if (unit == null) {
            // 回退到通用 doBuild（DynamicAssembly 等无特化 doBuild 的场景）
            unit = doBuild(null, null, businessVO);
        }

        currentUnit = (BusinessAssemblyUnit<?, ?, ?>) unit;
        businessAssemblyUnits.add(currentUnit);
    }

    public Object invokeBuild(String assemblyTypeCode, BusinessTypeIdentifier businessType, Class<?> tClass, Class<?> vClass, UserBusinessVO businessVO) {
        AssemblyTypeIdentifier assemblyType = typeRegistry.resolveAssemblyType(assemblyTypeCode);
        if (assemblyType == null) {
            return null;
        }
        return cacheInvoke(assemblyType.getAssemblyClass().cast(this), "doBuild",
                Arrays.asList(vClass, tClass, businessType.getVoClass()),
                null, null, businessType.getVoClass().cast(businessVO));
    }

    public BusinessAssemblyUnit doBuild(UserBusinessDealVO v, BusinessEntity t, UserBusinessVO r) {
        return BusinessAssemblyUnit.doBuild(v, t, r, this);
    }

    public static <T extends BusinessAssembly> T createAssembly(Class<T> c) {
        try {
            return c.getConstructor().newInstance();
        } catch (InterruptException | NoSuchMethodException | InstantiationException
                 | IllegalAccessException | InvocationTargetException e) {
            throw new InterruptException("构建组装线失败", e);
        }
    }

    /**
     * 根据类型编码创建 Assembly 实例
     * 优先从 TypeRegistry 查找已注册的 Assembly 类，未找到则创建 DynamicAssembly
     */
    public static BusinessAssembly createForType(String typeCode) {
        AssemblyTypeIdentifier assemblyType = typeRegistry.resolveAssemblyType(typeCode);
        if (assemblyType != null) {
            return createAssembly(assemblyType.getAssemblyClass());
        }
        return new DynamicAssembly(typeCode);
    }

    public void ready(BusinessEntity<?> advertEntity, UserBusinessVO businessVO) {
        if (Objects.isNull(currentUnit)) {
            build(null, null, businessVO);
        }
        currentUnit.ready(advertEntity);
    }

    public void complete(UserBusinessDealVO<?> advertVO, BusinessEntity<?> advertEntity, UserBusinessVO businessVO) {
        if (Objects.isNull(currentUnit)) {
            build(null, null, businessVO);
            currentUnit.ready(advertEntity);
        }
        before(advertVO, advertEntity, businessVO);
        currentUnit.complete(advertVO);
        finishCurrentUnit();
    }

    public void before(UserBusinessDealVO<?> advertVO, BusinessEntity<?> advertEntity, UserBusinessVO businessVO) {
        if (BusinessMode.LIVE.equals(getMode())) {
            log.debug("正常模式，无需额外处理");
            return;
        }
        fillTestCase(advertVO, advertEntity, businessVO);
    }

    /**
     * Unit 级别 finish — 仅完成当前 unit 的 Entity.finish() 和异步任务
     */
    private void finishCurrentUnit() {
        if (currentUnit == null) {
            return;
        }
        currentUnit.getBusinessEntity().finish();
        if (mode != BusinessMode.LIVE && currentUnit.getBusinessDealVO() != null) {
            currentUnit.getBusinessDealVO().executeAllFutureTask();
        }
    }

    /**
     * Assembly 级别 finish — 整条流程线结束时调用一次
     * 处理 Assembly 维度的收尾工作（如保存测试用例）
     */
    public void finish() {
        if (BusinessMode.RECORD.equals(mode) && !multiBusinessVO.isEmpty()) {
            BusinessVO firstVO = multiBusinessVO.values().iterator().next();
            saveTestCase(firstVO);
        }
    }

    @Override
    public void close() {
        finish();
    }

    /**
     * 高层编排接口 — 通过 Assembly 编排 Facade 执行
     * 每次调用驱动一个 Facade 的完整 Pipeline，Assembly 串联所有执行节点
     */
    public <V extends UserBusinessDealVO<T>, T extends BusinessEntity<?>, R extends UserBusinessVO>
        V execute(BusinessFacade<V, T, R> facade, R input) {
        for (StepOutputKey<?> req : facade.requires()) {
            if (!outputStore.contains(req)) {
                throw new InterruptException(
                        "步骤 '" + facade.getAssemblyTypeCode()
                                + "' 依赖的输出键 '" + req.name()
                                + "' 尚未发布，请检查执行顺序。已发布的键: " + outputStore.publishedKeys());
            }
        }
        return facade.process(input, this);
    }

    /**
     * 注册步骤（不立即执行）
     * 注册顺序无关，run() 时根据 requires/provides 自动排序执行
     *
     * @param facade 业务门面
     * @param input  业务输入
     * @return this（fluent API）
     */
    public <V extends UserBusinessDealVO<T>, T extends BusinessEntity<?>, R extends UserBusinessVO>
        BusinessAssembly addStep(BusinessFacade<V, T, R> facade, R input) {
        pendingSteps.add(new StepRegistration<>(
                facade, input, facade.requires(), facade.provides()));
        return this;
    }

    /**
     * 执行所有已注册的步骤
     *
     * 流程：验证依赖声明 → 拓扑排序 → 循环依赖检测 → 按序执行 → finish()
     *
     * @return this
     * @throws InterruptException 存在循环依赖或依赖声明不完整时
     */
    public BusinessAssembly run() {
        if (pendingSteps.isEmpty()) {
            finish();
            return this;
        }

        List<StepRegistration<?, ?, ?>> sorted = topologicalSort(pendingSteps);
        for (StepRegistration<?, ?, ?> step : sorted) {
            executeStep(step);
        }
        finish();
        pendingSteps.clear();
        return this;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void executeStep(StepRegistration<?, ?, ?> step) {
        ((BusinessFacade) step.facade()).process(step.input(), this);
    }

    /**
     * 基于 requires/provides 的拓扑排序（Kahn 算法）
     *
     * @param steps 已注册的步骤列表
     * @return 按依赖顺序排列的步骤列表
     * @throws InterruptException 如果存在循环依赖或依赖声明不完整
     */
    private List<StepRegistration<?, ?, ?>> topologicalSort(List<StepRegistration<?, ?, ?>> steps) {
        // 1. 构建 provides → StepRegistration 映射
        Map<StepOutputKey<?>, StepRegistration<?, ?, ?>> providerMap = new HashMap<>();
        for (StepRegistration<?, ?, ?> step : steps) {
            if (step.provides() != null) {
                StepRegistration<?, ?, ?> existing = providerMap.put(step.provides(), step);
                if (existing != null) {
                    throw new InterruptException(
                            "步骤输出键 '" + step.provides().name() + "' 被多个步骤声明: "
                                    + existing.facade().getAssemblyTypeCode() + ", "
                                    + step.facade().getAssemblyTypeCode());
                }
            }
        }

        // 2. 验证所有 requires 都有对应的 provides
        for (StepRegistration<?, ?, ?> step : steps) {
            for (StepOutputKey<?> req : step.requires()) {
                if (!providerMap.containsKey(req)) {
                    throw new InterruptException(
                            "步骤 '" + step.facade().getAssemblyTypeCode()
                                    + "' 依赖的输出键 '" + req.name() + "' 没有任何步骤声明提供");
                }
            }
        }

        // 3. 构建入度表和邻接表
        Map<StepRegistration<?, ?, ?>, Integer> inDegree = new LinkedHashMap<>();
        Map<StepRegistration<?, ?, ?>, List<StepRegistration<?, ?, ?>>> adjacency = new LinkedHashMap<>();
        for (StepRegistration<?, ?, ?> step : steps) {
            inDegree.put(step, 0);
            adjacency.put(step, new ArrayList<>());
        }

        for (StepRegistration<?, ?, ?> step : steps) {
            for (StepOutputKey<?> req : step.requires()) {
                StepRegistration<?, ?, ?> provider = providerMap.get(req);
                if (provider != null) {
                    adjacency.get(provider).add(step);
                    inDegree.merge(step, 1, Integer::sum);
                }
            }
        }

        // 4. Kahn 算法 — BFS
        Deque<StepRegistration<?, ?, ?>> queue = new ArrayDeque<>();
        for (Map.Entry<StepRegistration<?, ?, ?>, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.add(entry.getKey());
            }
        }

        List<StepRegistration<?, ?, ?>> sorted = new ArrayList<>();
        while (!queue.isEmpty()) {
            StepRegistration<?, ?, ?> current = queue.poll();
            sorted.add(current);
            for (StepRegistration<?, ?, ?> neighbor : adjacency.get(current)) {
                int newDegree = inDegree.get(neighbor) - 1;
                inDegree.put(neighbor, newDegree);
                if (newDegree == 0) {
                    queue.add(neighbor);
                }
            }
        }

        // 5. 循环依赖检测
        if (sorted.size() != steps.size()) {
            List<String> cycleNodes = steps.stream()
                    .filter(s -> !sorted.contains(s))
                    .map(s -> s.facade().getAssemblyTypeCode())
                    .toList();
            throw new InterruptException("检测到循环依赖，涉及步骤: " + cycleNodes);
        }

        return sorted;
    }

    public void setCheckMode(TestCaseVO testCaseVO) {
        this.mode = BusinessMode.CHECK;
        if (loadTestCase(testCaseVO)) {
            log.info("进入检查模式: {}", testCaseVO.getName());
        } else {
            throw new InterruptException("加载测试用例失败，无法进入检查模式: " + testCaseVO.getName());
        }
    }

    public void setReviewMode(TestCaseVO testCaseVO) {
        this.mode = BusinessMode.REVIEW;
        if (loadTestCase(testCaseVO)) {
            log.info("进入复盘模式: {}", testCaseVO.getName());
        } else {
            throw new InterruptException("加载测试用例失败，无法进入复盘模式: " + testCaseVO.getName());
        }
    }

    public void setReplayMode(TestCaseVO testCaseVO) {
        this.mode = BusinessMode.REPLAY;
        if (loadTestCase(testCaseVO)) {
            log.info("进入重播模式: {}", testCaseVO.getName());
        } else {
            throw new InterruptException("加载测试用例失败，无法进入重播模式: " + testCaseVO.getName());
        }
    }

    public void setRegenerateMode(TestCaseVO testCaseVO, TestCasePersistenceService persistenceService) {
        this.mode = BusinessMode.REGENERATE;
        this.testCasePersistenceService = persistenceService;
        if (loadTestCase(testCaseVO)) {
            log.info("进入重生成模式: {}", testCaseVO.getName());
        } else {
            throw new InterruptException("加载测试用例失败，无法进入重生成模式: " + testCaseVO.getName());
        }
    }

    public void setRecordMode(String testCaseName, TestCasePersistenceService persistenceService) {
        this.mode = BusinessMode.RECORD;
        this.testCaseName = testCaseName;
        this.testCasePersistenceService = persistenceService;
        multiBusinessVO.clear();
        multiBusinessEnv.clear();
        multiBusinessDealVO.clear();
        log.info("进入记录模式: {}", testCaseName);
    }

    public void clearTestCase() {
        this.mode = BusinessMode.LIVE;
        this.testCaseName = null;
        this.testCaseVO = null;
        this.multiBusinessVO.clear();
        this.multiBusinessEnv.clear();
        this.multiBusinessDealVO.clear();
        log.info("清除测试用例，恢复到正常模式");
    }

    public void fillTestCase(UserBusinessDealVO<?> advertVO, BusinessEntity<?> advertEntity, UserBusinessVO businessVO) {
        multiBusinessVO.put(currentUnit.getMarkName(), businessVO);
        multiBusinessEnv.put(currentUnit.getMarkName(), advertEntity.getBusinessHelper().getBusinessEnv().getRecords());
        multiBusinessDealVO.put(currentUnit.getMarkName(), advertVO);
    }

    public TestCaseVO generateTestCase(BusinessVO businessVO) {
        if (testCaseName == null) {
            log.warn("无法生成测试用例，未设置测试用例名称");
            return null;
        }
        try {
            TestCaseVO tc = new TestCaseVO();
            tc.setName(testCaseName);
            tc.setDescription("自动生成的测试用例");
            String businessType = this.getAssemblyTypeCode();
            String businessName = this.getAssemblyTypeCode();
            if (businessVO != null && businessVO.getBusinessType() != null) {
                businessType = businessVO.getBusinessType().getCode();
                businessName = businessVO.getBusinessType().getDescription();
            }
            tc.setBusinessType(businessType);
            tc.setBusinessName(businessName);
            tc.setMultiBusinessVO(jsonSerializer.toJson(multiBusinessVO));
            tc.setMultiBusinessEnv(jsonSerializer.toJson(multiBusinessEnv));
            tc.setMultiBusinessDealVO(jsonSerializer.toJson(multiBusinessDealVO));
            log.info("生成测试用例成功: {}", testCaseName);
            return tc;
        } catch (Exception e) {
            throw new InterruptException("生成测试用例失败", e);
        }
    }

    public void saveTestCase(BusinessVO businessVO) {
        if (mode != BusinessMode.RECORD) {
            log.warn("无法保存测试用例，当前模式不是记录模式");
            return;
        }
        TestCaseVO tc = this.generateTestCase(businessVO);
        if (tc == null) {
            log.warn("无法保存测试用例，传入的测试用例对象为空");
            return;
        }
        try {
            testCasePersistenceService.saveOrUpdate(tc);
            log.info("保存测试用例成功: {}", tc.getName());
        } catch (Exception e) {
            throw new InterruptException("保存测试用例失败", e);
        }
    }

    public void updateTestCase(BusinessVO businessVO) {
        if (mode != BusinessMode.REGENERATE) {
            log.error("无法更新测试用例，当前模式不是重生成模式");
            return;
        }
        TestCaseVO tc = this.generateTestCase(businessVO);
        if (tc == null) {
            log.error("无法更新测试用例，传入的测试用例对象为空");
            return;
        }
        try {
            if (this.testCaseVO == null || this.testCaseVO.getId() == null) {
                throw new InterruptException("当前测试用例对象未加载或ID不存在");
            }
            tc.setId(this.testCaseVO.getId());
            testCasePersistenceService.saveOrUpdate(tc);
            log.info("更新测试用例成功: {}", tc.getName());
        } catch (Exception e) {
            throw new InterruptException("更新测试用例失败", e);
        }
    }

    /**
     * 从 TestCaseVO 加载业务输入数据（multiBusinessVO）
     * RECORD 模式和其他模式复用此逻辑
     */
    @SuppressWarnings("unchecked")
    private boolean loadMultiBusinessVO(TestCaseVO testCaseVO) {
        if (testCaseVO == null || testCaseVO.getMultiBusinessVO() == null) {
            return false;
        }
        Map<String, Map<String, Object>> multiBusinessMap =
                jsonSerializer.fromJson(testCaseVO.getMultiBusinessVO(), Map.class);
        if (multiBusinessMap == null || multiBusinessMap.isEmpty()) {
            return false;
        }
        multiBusinessVO = new HashMap<>();
        for (Map.Entry<String, Map<String, Object>> entry : multiBusinessMap.entrySet()) {
            String scope = entry.getKey();
            Map<String, Object> businessData = entry.getValue();
            if (businessData == null || !businessData.containsKey("businessType")) {
                log.error("加载的测试用例数据异常: testCaseName={}, scope={}", testCaseName, scope);
                throw new InterruptException("测试用例数据异常，缺少 businessType 字段");
            }
            Map<String, Object> typeMap = (Map<String, Object>) businessData.get("businessType");
            String typeCode = typeMap != null ? (String) typeMap.get("code") : null;
            if (typeCode == null) {
                throw new InterruptException("测试用例数据异常，businessType.code 为空");
            }
            Class<? extends UserBusinessVO> voClass = typeRegistry.resolveVoClass(typeCode);
            UserBusinessVO businessVO = jsonSerializer.fromJson(jsonSerializer.toJson(businessData), voClass);
            multiBusinessVO.put(scope, businessVO);
        }
        return !multiBusinessVO.isEmpty();
    }

    @SuppressWarnings("unchecked")
    private boolean loadTestCase(TestCaseVO testCaseVO) {
        try {
            this.testCaseVO = testCaseVO;
            if (testCaseVO != null) {
                testCaseName = testCaseVO.getName();
                if (!loadMultiBusinessVO(testCaseVO)) {
                    log.warn("加载的测试用例业务数据为空: testCaseName={}", testCaseName);
                    return false;
                }
                multiBusinessEnv = testCaseVO.getMultiBusinessEnv() != null
                        ? jsonSerializer.fromJson(testCaseVO.getMultiBusinessEnv(), Map.class)
                        : new HashMap<>();
                if (multiBusinessEnv == null || multiBusinessEnv.isEmpty()) {
                    log.warn("加载的测试用例交互记录为空: testCaseName={}", testCaseName);
                    return false;
                }
                log.info("加载测试用例成功: {}", testCaseName);
                return true;
            } else {
                log.warn("未找到测试用例: {}", testCaseName);
            }
        } catch (Exception e) {
            throw new InterruptException("加载测试用例失败", e);
        }
        return false;
    }

    /**
     * 通过 Facade 执行测试用例 — 替代原 doRunTestCase 中的反射调用
     */
    @SuppressWarnings("unchecked")
    public <R extends UserBusinessVO> void doRunTestCaseViaFacade(
            BusinessMode mode, BusinessFacade<?, ?, R> facade,
            TestCaseVO testCaseVO, TestCasePersistenceService persistenceService) {
        if (mode == null) {
            throw new InterruptException("执行测试用例时，业务模式不能为空");
        }
        switch (mode) {
            case LIVE: break;
            case CHECK: setCheckMode(testCaseVO); break;
            case REVIEW: setReviewMode(testCaseVO); break;
            case REPLAY: setReplayMode(testCaseVO); break;
            case REGENERATE: setRegenerateMode(testCaseVO, persistenceService); break;
            case RECORD:
                setRecordMode(testCaseVO.getName(), persistenceService);
                loadMultiBusinessVO(testCaseVO);
                break;
            default: throw new InterruptException("执行测试用例不支持此模式: " + mode);
        }

        List<Map.Entry<String, BusinessVO>> entryList = getSortedBusinessEntries();
        for (int i = 0; i < entryList.size(); i++) {
            int index = Objects.isNull(currentUnit) ? -1
                    : entryList.stream().map(Map.Entry::getKey).collect(Collectors.toList()).indexOf(currentUnit.getMarkName());
            if (i <= index) {
                i = index;
                continue;
            }
            Map.Entry<String, BusinessVO> entry = entryList.get(i);
            BusinessVO businessVO = entry.getValue();
            if (businessVO == null) {
                log.warn("测试用例中缺少业务数据: scope={}", entry.getKey());
                continue;
            }
            R vo = (R) businessVO;
            facade.process(vo, this);
        }

        this.finish();
    }

    /**
     * 获取排序后的业务数据条目列表
     */
    private List<Map.Entry<String, BusinessVO>> getSortedBusinessEntries() {
        List<Map.Entry<String, BusinessVO>> entryList = new ArrayList<>(multiBusinessVO.entrySet());
        return entryList.stream().sorted(Comparator.comparingInt(entry -> {
            String key = entry.getKey();
            int index = key.lastIndexOf('-');
            if (index != -1 && index < key.length() - 1) {
                try {
                    return Integer.parseInt(key.substring(index + 1));
                } catch (NumberFormatException e) {
                    return Integer.MAX_VALUE;
                }
            }
            return Integer.MAX_VALUE;
        })).collect(Collectors.toList());
    }
}
