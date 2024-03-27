package com.loopy.loopy.plugins.common;

import java.util.HashMap;
import java.util.Map;

public class AjaxResult extends HashMap<String, Object> {
    private static final long serialVersionUID = 1L;

    /**
     * 初始化一个新创建的 Message 对象
     */
    public AjaxResult() {
    }

    /**
     * 返回错误消息
     *
     * @return 错误消息
     */
    public static AjaxResult error() {
        return error(1, "Operation Failed");
    }

    /**
     * 返回错误消息
     *
     * @param msg 内容
     * @return 错误消息
     */
    public static AjaxResult error(String msg) {
        return error(500, msg);
    }

    /**
     * 返回错误消息
     *
     * @param code 错误码
     * @param msg  内容
     * @return 错误消息
     */
    public static AjaxResult error(int code, String msg) {
        AjaxResult json = new AjaxResult();
        json.put("code", code);
        json.put("msg", msg);
        return json;
    }

    /**
     * 返回成功消息
     *
     * @param msg 内容
     * @return 成功消息
     */
    public static AjaxResult success(String msg) {
        AjaxResult json = new AjaxResult();
        json.put("msg", msg);
        json.put("code", 0);
        return json;
    }

    /**
     * 返回成功消息
     *
     * @return 成功消息
     */
    public static AjaxResult success() {
        return AjaxResult.success("Operation success");
    }

    /**
     * 返回成功消息
     *
     * @param key   键值
     * @param value 内容
     * @return 成功消息
     */
    @Override
    public AjaxResult put(String key, Object value) {
        super.put(key, value);
        return this;
    }

    public static AjaxResult returnSuccessDataResult(Object value) {
        AjaxResult json = new AjaxResult();
        json.put("msg", "success");
        json.put("code", 200);
        json.put("data", value);
        return json;
    }

    public static AjaxResult returnSuccessDataResult(Object value, String key, Object keyValue) {
        AjaxResult json = new AjaxResult();
        json.put("msg", "success");
        json.put("code", 0);
        json.put("data", value);
        json.put(key, keyValue);
        return json;
    }

    public static AjaxResult returnSuccessDataResult(Object value, Map<String, Object> map) {
        AjaxResult json = new AjaxResult();
        json.put("msg", "success");
        json.put("code", 0);
        json.put("data", value);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            json.put(entry.getKey(), entry.getValue());
        }
        return json;
    }
}
