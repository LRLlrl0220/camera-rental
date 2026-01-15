package com.shiguang.camera.utils;

import com.alibaba.fastjson.JSON;
import java.util.List;

public class JsonUtils {

    /**
     * 将List<Integer>转换为JSON字符串
     */
    public static String listToJson(List<Integer> list) {
        return JSON.toJSONString(list);
    }

    /**
     * 将JSON字符串转换为List<Integer>
     */
    public static List<Integer> jsonToList(String json) {
        return JSON.parseArray(json, Integer.class);
    }
}