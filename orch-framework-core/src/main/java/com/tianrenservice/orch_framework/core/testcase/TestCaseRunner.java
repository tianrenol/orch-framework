package com.tianrenservice.orch_framework.core.testcase;

import com.tianrenservice.orch_framework.core.constant.BusinessMode;
import com.tianrenservice.orch_framework.core.exception.InterruptException;
import com.tianrenservice.orch_framework.core.pipeline.BusinessAssembly;
import com.tianrenservice.orch_framework.core.pipeline.BusinessFacade;
import com.tianrenservice.orch_framework.core.spi.TestCasePersistenceService;
import com.tianrenservice.orch_framework.core.testcase.model.TestCaseVO;
import com.tianrenservice.orch_framework.core.vo.BusinessVO;
import lombok.extern.slf4j.Slf4j;

/**
 * 测试用例执行器 - 通过 BusinessFacade 执行测试用例
 */
@Slf4j
public class TestCaseRunner {

    private final BusinessFacade<?, ?, ?> facade;
    private final TestCasePersistenceService persistenceService;

    public TestCaseRunner(BusinessFacade<?, ?, ?> facade,
                          TestCasePersistenceService persistenceService) {
        this.facade = facade;
        this.persistenceService = persistenceService;
    }

    /**
     * 以指定模式执行测试用例
     * @return 执行后的 BusinessAssembly（包含执行结果数据）
     */
    public BusinessAssembly run(BusinessMode mode, TestCaseVO testCaseVO) {
        BusinessAssembly assembly = BusinessAssembly.createForType(facade.getAssemblyTypeCode());
        assembly.doRunTestCaseViaFacade(mode, facade, testCaseVO, persistenceService);
        return assembly;
    }

    /**
     * 从执行后的 Assembly 中提取第一个 BusinessVO
     */
    public BusinessVO extractFirstBusinessVO(BusinessAssembly assembly) {
        return assembly.getMultiBusinessVO().values().stream()
                .findFirst()
                .orElseThrow(() -> new InterruptException("测试用例中未找到业务数据"));
    }

    /**
     * 从执行后的 Assembly 中生成测试用例快照
     */
    public TestCaseVO generateSnapshot(BusinessAssembly assembly) {
        BusinessVO businessVO = extractFirstBusinessVO(assembly);
        return assembly.generateTestCase(businessVO);
    }
}
