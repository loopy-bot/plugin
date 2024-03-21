package com.loopy.loopy.plugins.Engine;

import org.springframework.beans.factory.InitializingBean;

public abstract class AbstractEngine implements InitializingBean {

    public String getAnswer(String question) {
        throw new UnsupportedOperationException();
    }
}
