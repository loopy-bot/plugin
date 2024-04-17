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


     @Override
     public Message reply(List<Message> messages, Config configuration) throws NoApiKeyException, InputRequiredException {
         Generation gen = new Generation();
         rebuildQwenConfig(configuration.getQwenDTO());
         GenerationParam param =
                GenerationParam.builder().model(configuration.getQwenDTO().getModel()).messages(messages)
                        .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                        .seed(configuration.getQwenDTO().getSeed())
                        .maxTokens(configuration.getQwenDTO().getMaxTokens())
                        .topP(configuration.getQwenDTO().getTopP())
                        .topK(configuration.getQwenDTO().getTopK())
                        .repetitionPenalty(configuration.getQwenDTO().getRepetitionPenalty())
                        .temperature(configuration.getQwenDTO().getTemperature())
                        .stopString(configuration.getQwenDTO().getStop())
                        .enableSearch(configuration.getQwenDTO().getEnableSearch())
                        .incrementalOutput(configuration.getQwenDTO().getIncrementalOutput())
                        .apiKey(TONG_YI_API_KEY)
                        .build();
        GenerationResult result = gen.call(param);
        return result.getOutput().getChoices().get(0).getMessage();
     }

     @Override
     public void afterPropertiesSet() throws Exception {
          ModelFactory.register(MODEL, this);
     }

     private void rebuildQwenConfig(Config.QwenDTO config){
         if (config.getSeed() == null){
             config.setSeed(1234);
         }
         if (config.getModel() == null || config.getModel().isEmpty()){
             config.setModel("qwen1.5-72b-chat");
         }
         if(config.getMaxTokens() == null){
             config.setMaxTokens(1500);
         }
         if(config.getTopP() == null){
             config.setTopP(0.8);
         }
         if(config.getTopK() == null){
             config.setTopK(101);
         }
         if(config.getRepetitionPenalty() == null){
             config.setRepetitionPenalty(1.1f);
         }
         if (config.getTemperature() == null){
             config.setTemperature(0.85f);
         }
         if (config.getStop() == null || config.getStop().isEmpty()){
             config.setStop("结束对话");
         }
         if(config.getEnableSearch() == null){
             config.setEnableSearch(false);
         }
         if(config.getIncrementalOutput() == null){
             config.setIncrementalOutput(false);
         }
     }

}
