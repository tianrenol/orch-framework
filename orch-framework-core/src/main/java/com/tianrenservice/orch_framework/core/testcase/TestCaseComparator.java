package com.tianrenservice.orch_framework.core.testcase;

import com.tianrenservice.orch_framework.core.spi.JsonSerializer;
import com.tianrenservice.orch_framework.core.testcase.model.TestCaseVO;
import com.tianrenservice.orch_framework.core.util.ObjectCompareUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 测试用例比较器 - 从 AdvertTestCaseService.equalsCase() 提取
 * 比较两个 TestCaseVO 的三组数据 (VO/Env/DealVO) 是否一致
 */
@Slf4j
public class TestCaseComparator {

    private final JsonSerializer jsonSerializer;
    private final ObjectCompareUtil objectCompareUtil;

    public TestCaseComparator(JsonSerializer jsonSerializer) {
        this.jsonSerializer = jsonSerializer;
        this.objectCompareUtil = new ObjectCompareUtil(jsonSerializer);
    }

    /**
     * 比较两个测试用例是否一致
     * @return true 表示一致
     */
    @SuppressWarnings("unchecked")
    public boolean equalsCase(TestCaseVO checkCase, TestCaseVO resultCase, Map<String, String> inconsistentMap) {
        Map<String, String[]> fieldPairs = new LinkedHashMap<>();
        fieldPairs.put("multiBusinessVO", new String[]{checkCase.getMultiBusinessVO(), resultCase.getMultiBusinessVO()});
        fieldPairs.put("multiBusinessEnv", new String[]{checkCase.getMultiBusinessEnv(), resultCase.getMultiBusinessEnv()});
        fieldPairs.put("multiBusinessDealVO", new String[]{checkCase.getMultiBusinessDealVO(), resultCase.getMultiBusinessDealVO()});

        boolean isPass = true;
        for (Map.Entry<String, String[]> entry : fieldPairs.entrySet()) {
            String key = entry.getKey();
            String[] pair = entry.getValue();
            isPass = equalsJsonField(pair[0], pair[1], key, inconsistentMap) && isPass;
        }
        return isPass;
    }

    /**
     * 比较两段 JSON 是否一致（移除 identity 字段后比较）
     */
    @SuppressWarnings("unchecked")
    public boolean equalsJsonField(String checkJson, String resultJson, String fieldString, Map<String, String> inconsistentMap) {
        Map<String, Map<String, Object>> checkMap = jsonSerializer.fromJson(checkJson, Map.class);
        Map<String, Map<String, Object>> resultMap = jsonSerializer.fromJson(resultJson, Map.class);

        if (checkMap == null) checkMap = new HashMap<>();
        if (resultMap == null) resultMap = new HashMap<>();

        // 移除 identity 字段，避免因链路标识不同导致误判
        checkMap.values().stream().filter(Objects::nonNull).forEach(map -> map.remove("identity"));
        resultMap.values().stream().filter(Objects::nonNull).forEach(map -> map.remove("identity"));

        return objectCompareUtil.objectEquals(checkMap, resultMap, fieldString, inconsistentMap);
    }
}
