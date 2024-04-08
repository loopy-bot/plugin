package com.loopy.loopy.plugins.model;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class Qwen extends AbstractModel{

    private static final String MODEL= "qwen";

    private static final String TONG_YI_API_KEY = "sk-983cdd6a15684c1ab020bcdf00a82c60";

    private static final Logger logger = LoggerFactory.getLogger(Qwen.class);
    private Configuration config;

     class Configuration extends Config {

     }

     @Override
     public Message reply(List<Message> messages, Config config) throws NoApiKeyException, InputRequiredException {
         Generation gen = new Generation();
         GenerationParam param =
                GenerationParam.builder().model("qwen1.5-72b-chat").messages(messages)
                        .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                        .topP(0.8)
                        .apiKey(TONG_YI_API_KEY)
                        .build();
        GenerationResult result = gen.call(param);
        return result.getOutput().getChoices().get(0).getMessage();
     }

     @Override
     public void afterPropertiesSet() throws Exception {
          ModelFactory.register(MODEL, this);
     }

}
