package com.loopy.loopy.plugins.Engine;

import org.springframework.util.StringUtils;

import java.util.HashMap;

public class EngineFactory {

    private static HashMap<String, AbstractEngine> pluginMap = new HashMap<>();

    public static AbstractEngine getInvokeEngine(String str){
        return pluginMap.get(str);
    }

    public static void register(String str, AbstractEngine engine){
        if (StringUtils.isEmpty(str) || engine == null){
            return;}
        pluginMap.put(str, engine);
    }
}
