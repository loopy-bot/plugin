package com.loopy.loopy.plugins.model;

import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import org.springframework.beans.factory.InitializingBean;

import java.util.List;

public abstract class AbstractModel implements InitializingBean {

    public Message reply(List<Message> messages, Config config) throws NoApiKeyException, InputRequiredException {
        throw new UnsupportedOperationException();
    }

}
