package com.tianrenservice.orch_framework.core.testcase;

import com.tianrenservice.orch_framework.core.constant.BusinessMode;
import com.tianrenservice.orch_framework.core.exception.InterruptException;
import com.tianrenservice.orch_framework.core.pipeline.BusinessAssembly;
import com.tianrenservice.orch_framework.core.pipeline.BusinessFacade;
import com.tianrenservice.orch_framework.core.spi.JsonSerializer;
import com.tianrenservice.orch_framework.core.spi.TestCasePersistenceService;
import com.tianrenservice.orch_framework.core.testcase.model.TestCaseVO;
import com.tianrenservice.orch_framework.core.vo.BusinessVO;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试用例引擎 - 提供录制、回放、检查、复盘、重生成等测试能力
 */
@Slf4j
public class TestCaseEngine {

    private final TestCaseRunner runner;
    private final TestCaseComparator comparator;
    private final TestCasePersistenceService persistenceService;
    private final JsonSerializer jsonSerializer;

    public TestCaseEngine(BusinessFacade<?, ?, ?> facade,
                          TestCasePersistenceService persistenceService,
                          JsonSerializer jsonSerializer) {
        this.persistenceService = persistenceService;
        this.jsonSerializer = jsonSerializer;
        this.runner = new TestCaseRunner(facade, persistenceService);
        this.comparator = new TestCaseComparator(jsonSerializer);
    }

    /**
     * 录制测试用例 - 执行业务流程并记录所有交互
     */
    public TestCaseVO record(TestCaseVO testCase) {
        if (testCase == null) {
            throw new IllegalArgumentException("Test case cannot be null");
        }
        BusinessAssembly assembly = runner.run(BusinessMode.RECORD, testCase);
        return runner.generateSnapshot(assembly);
    }

    /**
     * 检查测试用例 - 根据 ID 加载并校验
     */
    public boolean check(Integer testCaseId) {
        TestCaseVO checkCase = persistenceService.load(testCaseId);
        if (checkCase == null) {
            throw new InterruptException("Failed to retrieve test case: " + testCaseId);
        }
        BusinessAssembly assembly = runner.run(BusinessMode.CHECK, checkCase);
        TestCaseVO resultCase = runner.generateSnapshot(assembly);
        Map<String, String> inconsistentMap = new HashMap<>();
        if (comparator.equalsCase(checkCase, resultCase, inconsistentMap)) {
            return true;
        }
        throw new InterruptException("Test case check failed: " + testCaseId
                + ", inconsistencies: " + jsonSerializer.toJson(inconsistentMap));
    }

    /**
     * 复盘测试用例 - 使用提供的用例数据进行验证
     */
    public boolean review(TestCaseVO testCase) {
        if (testCase == null) {
            throw new IllegalArgumentException("Test case cannot be null");
        }
        BusinessAssembly assembly = runner.run(BusinessMode.REVIEW, testCase);
        TestCaseVO resultCase = runner.generateSnapshot(assembly);
        Map<String, String> inconsistentMap = new HashMap<>();
        if (comparator.equalsCase(testCase, resultCase, inconsistentMap)) {
            return true;
        }
        throw new InterruptException("Test case review failed, inconsistencies: "
                + jsonSerializer.toJson(inconsistentMap));
    }

    /**
     * 重播测试用例 - 使用录制数据回放，未匹配的调用直接执行
     */
    public TestCaseVO replay(TestCaseVO testCase) {
        if (testCase == null) {
            throw new IllegalArgumentException("Test case cannot be null");
        }
        BusinessAssembly assembly = runner.run(BusinessMode.REPLAY, testCase);
        return runner.generateSnapshot(assembly);
    }

    /**
     * 重生成测试用例 - 重新执行并更新持久化数据
     */
    public TestCaseVO regenerate(Integer testCaseId, TestCaseVO testCase) {
        if (testCaseId == null || testCase == null) {
            throw new IllegalArgumentException("Test case id and test case cannot be null");
        }
        testCase.setId(testCaseId);
        BusinessAssembly assembly = runner.run(BusinessMode.REGENERATE, testCase);
        BusinessVO businessVO = runner.extractFirstBusinessVO(assembly);
        TestCaseVO resultCase = assembly.generateTestCase(businessVO);
        resultCase.setId(testCaseId);
        Map<String, String> inconsistentMap = new HashMap<>();
        if (comparator.equalsCase(testCase, resultCase, inconsistentMap)) {
            assembly.updateTestCase(businessVO);
            return resultCase;
        }
        throw new InterruptException("Test case regenerate failed, inconsistencies: "
                + jsonSerializer.toJson(inconsistentMap));
    }

    /**
     * 获取比较器（供外部自定义比较逻辑使用）
     */
    public TestCaseComparator getComparator() {
        return comparator;
    }
}
