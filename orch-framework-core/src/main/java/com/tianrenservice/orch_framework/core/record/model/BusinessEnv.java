package com.tianrenservice.orch_framework.core.record.model;

import com.tianrenservice.orch_framework.core.constant.BusinessMode;
import com.tianrenservice.orch_framework.core.exception.InterruptException;
import com.tianrenservice.orch_framework.core.record.annotation.RecordAndReplay;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 环境交互管理器 - 管理录制/回放状态和交互记录
 * Prototype 作用域，每个业务流程独立实例
 */
@Slf4j
@Getter
public class BusinessEnv {

    private final long nowTime = System.currentTimeMillis();

    @RecordAndReplay
    public long getNowTime() {
        return getRealNowTime();
    }

    public long getRealNowTime() {
        return nowTime;
    }

    private BusinessMode mode = BusinessMode.LIVE;
    private Map<String, List<InteractionRecord>> records = new HashMap<>();
    private Map<String, List<InteractionRecord>> covers = new HashMap<>();
    private String testCaseName;

    public void setTestMode(BusinessMode mode, String testCaseName, Map<String, List<InteractionRecord>> records) {
        this.mode = mode;
        this.testCaseName = testCaseName;
        if (mode == BusinessMode.RECORD || mode == BusinessMode.LIVE) {
            this.records.clear();
            return;
        }
        loadRecords(testCaseName, records);
    }

    public void loadRecords(String testCaseName, Map<String, List<InteractionRecord>> records) {
        log.info("环境加载测试记录: {}", testCaseName);
        this.testCaseName = testCaseName;
        this.covers = records;
    }

    public void extendTestMode(BusinessMode mode, String testCaseName,
                               Map<String, List<InteractionRecord>> records,
                               Map<String, List<InteractionRecord>> covers) {
        this.mode = mode;
        this.testCaseName = testCaseName;
        this.records = records;
        this.covers = covers;
    }

    public InteractionRecord findMatchingRecord(List<InteractionRecord> records, List<Object> args) {
        InteractionRecord record = findMatchingRecordIgnoreNoFind(records, args);
        if (record != null) {
            return record;
        }
        throw new InterruptException("方法" + records.stream().findFirst()
                .map(InteractionRecord::getMethodName).orElse("未知")
                + "未找到匹配的参数记录");
    }

    public InteractionRecord findMatchingRecordIgnoreNoFind(List<InteractionRecord> records, List<Object> args) {
        if (records == null || records.isEmpty()) {
            return null;
        }
        for (InteractionRecord record : records) {
            if (argsMatch(record.getArguments(), args)) {
                return record;
            }
        }
        return null;
    }

    private boolean argsMatch(List<Object> recordArgs, List<Object> currentArgs) {
        if (recordArgs.size() != currentArgs.size()) {
            return false;
        }
        for (int i = 0; i < recordArgs.size(); i++) {
            if (!Objects.equals(recordArgs.get(i), currentArgs.get(i))) {
                return false;
            }
        }
        return true;
    }
}