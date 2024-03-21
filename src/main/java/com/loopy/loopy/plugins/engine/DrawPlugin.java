package com.loopy.loopy.plugins.engine;

import org.springframework.stereotype.Component;

@Component
public class DrawPlugin extends SelectPluginEngine{

    @Override
    protected void preDeal() {
        // TODO document why this method is empty
        System.out.println("Draw Plugin");
    }
}
