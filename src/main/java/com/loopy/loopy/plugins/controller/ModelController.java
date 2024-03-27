package com.loopy.loopy.plugins.controller;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesis;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisParam;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;
import com.alibaba.dashscope.audio.tts.SpeechSynthesisAudioFormat;
import com.alibaba.dashscope.audio.tts.SpeechSynthesisParam;
import com.alibaba.dashscope.audio.tts.SpeechSynthesizer;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.loopy.loopy.plugins.Engine.AbstractEngine;
import com.loopy.loopy.plugins.Engine.EngineFactory;
import com.loopy.loopy.plugins.common.AjaxResult;
import com.loopy.loopy.plugins.common.FormerRequest;
import com.loopy.loopy.plugins.common.ModelData;
import com.loopy.loopy.plugins.common.PostData;
import com.loopy.loopy.plugins.request.ChatRequest;
import com.loopy.loopy.plugins.response.ChatResponse;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.*;


@RestController
@RequestMapping("/model")
public class ModelController {

    private static final Logger logger = LoggerFactory.getLogger(ModelController.class);
    private static final String TONG_YI_API_KEY = "sk-554382667176404bb1c35d59ac5d4096";
    private static final String ALIYUN_CHAT_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";
    private static final Map<String, Queue<PostData>> MULTI_MESSAGES = new HashMap<>();
    private static final int MAX_ATTEMPTS = 3;

    private static FormerRequest FORMER_REQUEST;


    @PostMapping("/generate")
    @ResponseBody
    public AjaxResult generate(@RequestBody PostData postData) {
        String model = getModelConfigurer(postData);
        ChatRequest chatRequest = new ChatRequest(postData.getQuestion(), model);
        String json = JSONUtil.toJsonStr(chatRequest);
        //System.out.println(json);//正式发送给api前,查看请求的主要数据情况
        String result = HttpRequest.post(ALIYUN_CHAT_URL)
                .header("Authorization", "Bearer " + TONG_YI_API_KEY)
                .header("Content-Type", "application/json")
                .body(json)
                .execute().body();
        logger.info(result);
        ChatResponse chatResponse = JSONUtil.toBean(result, ChatResponse.class);
        String text = chatResponse.getOutput().getText();
        return AjaxResult.returnSuccessDataResult(new ModelData("assistant", text));
    }


    @PostMapping("/chat")
    @ResponseBody
    public AjaxResult chat(@RequestBody PostData postData) throws NoApiKeyException, InputRequiredException {
        if (postData.getKey() != null) {
            if (MULTI_MESSAGES.get(postData.getKey()) == null) {
                //首发
                incrementMessages(postData);
                FORMER_REQUEST = getFirstResponse(postData);
                return AjaxResult.returnSuccessDataResult(FORMER_REQUEST.getFormerResult());
            } else {
                if (isAllowedToSendMessage(postData.getKey())) {
                    incrementMessages(postData);
                    FORMER_REQUEST = getNextResponse(postData, FORMER_REQUEST);
                    return AjaxResult.returnSuccessDataResult(FORMER_REQUEST.getFormerResult());
                } else {
                    return AjaxResult.error("很抱歉，目前只支持3轮问答");
                }
            }

        } else {
            return generate(postData);
        }
    }

    @PostMapping("/ti-an")
    @ResponseBody
    public AjaxResult getTianXingResponse(@RequestBody String question) {
        String[] keyWords = {"星座", "天气", "IT资讯", "地图"};
        for (String word : keyWords) {
            if (question.contains(word)) {
                AbstractEngine engine = EngineFactory.getInvokeEngine(word);
                String answer = engine.getAnswer(question);
                return AjaxResult.returnSuccessDataResult(answer);
            }
        }
        return AjaxResult.error("目前没有相关的接口回答您的问题");
    }

    public FormerRequest getFirstResponse(PostData postData) throws NoApiKeyException, InputRequiredException {
        Generation gen = new Generation();
        String model = getModelConfigurer(postData);
        String systemRole;
        if (postData.getPersonality() != null) {
            systemRole = postData.getPersonality();
        } else {
            systemRole = "You are a helpful assistant.";
        }
        Message systemMsg =
                Message.builder().role(Role.SYSTEM.getValue()).content(systemRole).build();
        Message userMsg = Message.builder().role(Role.USER.getValue()).content(postData.getMessages().content).build();
        List<Message> messages = new ArrayList<>();
        messages.add(systemMsg);
        messages.add(userMsg);
        GenerationParam param =
                GenerationParam.builder().model(model).messages(messages)
                        .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                        .topP(0.8)
                        .apiKey(TONG_YI_API_KEY)
                        .build();
        GenerationResult result = gen.call(param);
//        Message m = result.getOutput().getChoices().get(0).getMessage();
        FormerRequest formerRequest = new FormerRequest(result, messages);
        return formerRequest;
    }

    // get the inner model
    private String getModelConfigurer(PostData postData) {
        String upModel = postData.getModel();
        String model = null;
        if (upModel.equals("kimi")) {
            model = postData.getConfig().getKimi().getModel();
        } else if (upModel.equals("qwen")) {
            model = postData.getConfig().getQwen().getModel();
        }

        return model;
    }

    private FormerRequest getNextResponse(@NotNull PostData postData, FormerRequest formerRequest) throws NoApiKeyException, InputRequiredException {
        Generation gen = new Generation();
        String model = getModelConfigurer(postData);
        GenerationParam param =
                GenerationParam.builder().model(model).messages(formerRequest.getMessages())
                        .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                        .topP(0.8)
                        .apiKey(TONG_YI_API_KEY)
                        .build();
        // 添加assistant返回到messages列表，user/assistant消息必须交替出现
        Message formerMessage = formerRequest.getFormerResult().getOutput().getChoices().get(0).getMessage();
        List<Message> latterMessage = formerRequest.getMessages();
        latterMessage.add(formerMessage);
        // new message
        Message userMsg = Message.builder().role(Role.USER.getValue()).content(postData.getMessages().content).build();
        latterMessage.add(userMsg);
        GenerationResult result = gen.call(param);
        FormerRequest formerRequest1 = new FormerRequest(result, formerRequest.getMessages());
        return formerRequest1;
    }

    public boolean isAllowedToSendMessage(String key) {
        Queue<PostData> messages = MULTI_MESSAGES.get(key);

        return messages.size() < MAX_ATTEMPTS;
    }

    public void incrementMessages(PostData postData) {
        MULTI_MESSAGES.computeIfAbsent(postData.getKey(), p -> new LinkedList<>()).add(postData);
    }

}
