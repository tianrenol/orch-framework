package com.tianrenservice.orch_framework.core.spi;

import com.tianrenservice.orch_framework.core.testcase.model.TestCaseVO;

/**
 * 测试用例持久化契约 - 替代原 BusinessTestCaseServiceRpc (Dubbo)
 * 业务方可实现为数据库存储、文件存储、远程 RPC 等
 */
public interface TestCasePersistenceService {

    /**
     * 保存或更新测试用例
     */
    void saveOrUpdate(TestCaseVO testCase);

    /**
     * 根据 ID 加载测试用例
     */
    TestCaseVO load(Integer id);
}
