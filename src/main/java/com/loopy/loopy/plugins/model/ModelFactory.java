package com.loopy.loopy.plugins.model;

import org.springframework.util.StringUtils;

import java.util.HashMap;

public class ModelFactory {

    private static HashMap<String, AbstractModel> modelMap = new HashMap<>();

    public static AbstractModel getInvokeModel(String str){
        return modelMap.get(str);
    }

    public static void register(String str, AbstractModel model){
        if (StringUtils.isEmpty(str) || model == null){
            return;}
        modelMap.put(str, model);
    }
}
