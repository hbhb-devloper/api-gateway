package com.hbhb.cw.gateway.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author xiaokang
 * @since 2020-09-12
 */
public class JsonUtil {

    /**
     * 对象转json字符串
     */
    public static String convert2Str(Object object) {
        if (object == null) {
            return null;
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 按key获取value
     */
    public static Object findByKey(String json, String key) {
        if (json == null) {
            return null;
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode destNode = objectMapper.readTree(json).get(key);
            if (destNode != null) {
                return destNode.asText();
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
