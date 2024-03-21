package com.loopy.loopy.plugins.engine;

import org.springframework.stereotype.Component;

@Component
public class AudioPlugin extends SelectPluginEngine{
    @Override
    protected void preDeal() {
        System.out.println("Audio Plugin");
    }
}
