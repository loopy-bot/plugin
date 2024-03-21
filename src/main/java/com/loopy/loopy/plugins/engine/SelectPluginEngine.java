package com.loopy.loopy.plugins.engine;

public abstract class SelectPluginEngine {


    protected abstract void preDeal();

    public final void execute(){
        preDeal();
    }


}
