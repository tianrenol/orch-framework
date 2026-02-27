package com.tianrenservice.orch_framework.core.util;

import com.tianrenservice.orch_framework.core.spi.JsonSerializer;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 对象深度比较工具 - 从原项目 Tools.objectEquals 提取
 * 支持递归比较 Map、Collection、基本类型、字符串，并记录不一致字段
 */
@Slf4j
public class ObjectCompareUtil {

    private final JsonSerializer jsonSerializer;

    public ObjectCompareUtil(JsonSerializer jsonSerializer) {
        this.jsonSerializer = jsonSerializer;
    }

    public boolean objectEquals(Object recordArg, Object currentArg) {
        return objectEquals(recordArg, currentArg, "", null);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public boolean objectEquals(Object recordArg, Object currentArg, String fieldString, Map<String, String> inconsistentMap) {
        if (inconsistentMap == null) {
            inconsistentMap = new HashMap<>();
        }
        if (recordArg == null && currentArg == null) {
            return true;
        }
        if (recordArg == null || currentArg == null) {
            String recordDesc = recordArg == null ? "null " : "not null ";
            String currentDesc = currentArg == null ? "null" : "not null";
            inconsistentMap.put(fieldString, "recordArg is " + recordDesc + "but currentArg is " + currentDesc);
            return false;
        }
        if (Objects.equals(recordArg, currentArg)) {
            return true;
        }
        if (BeanUtil.isPrimitive(recordArg.getClass()) && BeanUtil.isPrimitive(currentArg.getClass())) {
            if (Objects.equals(recordArg, currentArg)) {
                return true;
            }
        }
        if (recordArg instanceof String && currentArg instanceof String) {
            if (recordArg.toString().equals(currentArg.toString())) {
                return true;
            }
        }
        if (recordArg instanceof Collection && currentArg instanceof Collection) {
            if (((Collection<?>) recordArg).size() != ((Collection<?>) currentArg).size()) {
                inconsistentMap.put(fieldString, "recordArg size: " + ((Collection<?>) recordArg).size()
                        + " is not equal to currentArg size: " + ((Collection<?>) currentArg).size());
                return false;
            }
            Iterator<?> recIt = ((Collection<?>) recordArg).iterator();
            Iterator<?> curIt = ((Collection<?>) currentArg).iterator();
            boolean collEqual = true;
            int idx = 0;
            while (recIt.hasNext() && curIt.hasNext()) {
                if (!objectEquals(recIt.next(), curIt.next(), fieldString + "[" + idx + "]", inconsistentMap)) {
                    collEqual = false;
                }
                idx++;
            }
            return collEqual;
        }
        if (recordArg instanceof Map && currentArg instanceof Map) {
            Map<?, ?> recordMap = (Map<?, ?>) recordArg;
            Map<?, ?> currentMap = (Map<?, ?>) currentArg;
            if (recordMap.isEmpty() && currentMap.isEmpty()) {
                return true;
            }
            if (recordMap.isEmpty() || currentMap.isEmpty()) {
                String recordDesc = recordMap.isEmpty() ? "empty " : "not empty ";
                String currentDesc = currentMap.isEmpty() ? "empty" : "not empty";
                inconsistentMap.put(fieldString, "recordMap is " + recordDesc + "but currentMap is " + currentDesc);
                return false;
            }
            for (Object key : recordMap.keySet()) {
                if (!currentMap.containsKey(key)) {
                    inconsistentMap.put(fieldString + "." + key, "recordMap contains key but currentMap does not");
                }
                objectEquals(recordMap.get(key), currentMap.get(key), fieldString + "." + key, inconsistentMap);
            }
            for (Object key : currentMap.keySet()) {
                if (!recordMap.containsKey(key)) {
                    inconsistentMap.put(fieldString + "." + key, "recordMap not contains key but currentMap does");
                }
            }
            return inconsistentMap.isEmpty();
        }
        if (jsonSerializer.toJson(recordArg).equals(jsonSerializer.toJson(currentArg))) {
            return true;
        }
        inconsistentMap.put(fieldString, "recordArg: " + jsonSerializer.toJson(recordArg)
                + " is not equal to currentArg: " + jsonSerializer.toJson(currentArg));
        return false;
    }
}