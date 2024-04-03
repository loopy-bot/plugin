package com.loopy.loopy.plugins.controller;


import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
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
import com.loopy.loopy.plugins.request.KimiRequest;
import com.loopy.loopy.plugins.response.ChatResponse;
import com.loopy.loopy.plugins.response.KimiResponse;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.List;


@RestController
@RequestMapping("/model")
public class ModelController {

    private static final Logger logger = LoggerFactory.getLogger(ModelController.class);
    private static final String TONG_YI_API_KEY = "sk-554382667176404bb1c35d59ac5d4096";
    private static final String KIMI_API_KEY = "Bearer eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJ1c2VyLWNlbnRlciIsImV4cCI6MTcxODg2ODQ2MSwiaWF0IjoxNzExMDkyNDYxLCJqdGkiOiJjbnVqNXJlY3A3ZjRzbXNsbTY1MCIsInR5cCI6InJlZnJlc2giLCJzdWIiOiJjbDRiYTQxcDJrMWZpY2wzbHJ1ZyIsInNwYWNlX2lkIjoiY2w0YmE0MXAyazFmaWNsM2xydTAiLCJhYnN0cmFjdF91c2VyX2lkIjoiY2w0YmE0MXAyazFmaWNsM2xydWcifQ.aNd4hu6sOJXSUZZSiDk2V1c6aki-m-NF1EorCzrIXaex76LUYFZqAHelrYr_k11dF-BNybmgGzUHyaHbZ9CPxw";
    private static final String ALIYUN_CHAT_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";
    private static final String KIMI_CHAT_URL = "http://123.60.1.214:8000/v1/chat/completions";
    private static final Map<String, Queue<Message>> MULTI_MESSAGES = new HashMap<>();
    private static final int MAX_ATTEMPTS = 7;

    private static FormerRequest formerRequest;

    private static String personality;


    @PostMapping("/generate")
    public static AjaxResult generate(@RequestBody PostData postData) throws IOException {
        String model = getModelConfigurer(postData);
        String text = "";
        if (model.equals("kimi")){
            KimiResponse kimiResponse = getKimiResult(postData);
            KimiResponse.ChoicesDTO choice = kimiResponse.getChoices().get(0);
            text = choice.getMessage().getContent();
        } else if (model.equals("qwen1.5-72b-chat")) {
            text = getQwenResult(postData, model);
        }
        return AjaxResult.returnSuccessDataResult(new ModelData("assistant", text));
    }


    @PostMapping("/chat")
    public static AjaxResult chat(@RequestBody PostData postData) throws NoApiKeyException, InputRequiredException, IOException {
        if (postData.getKey() != null) {
            if (getModelConfigurer(postData).equals("kimi")){
                KimiResponse kimiResponse = getKimiResult(postData);
                KimiResponse.ChoicesDTO choice = kimiResponse.getChoices().get(0);
                return AjaxResult.returnSuccessDataResult(choice.getMessage());
            }
            else{
                if (MULTI_MESSAGES.get(postData.getKey()) == null) {
                    //首发
                    Message firstAnswer = getFirstResponse(postData);
                    return AjaxResult.returnSuccessDataResult(firstAnswer.getContent());
                } else {
                    Message nextAnswer = getNonFirstResult(postData);
                    return AjaxResult.returnSuccessDataResult(nextAnswer);
                }
            }


        } else {
            return generate(postData);
        }
    }

    @NotNull
    private static Message getNonFirstResult(PostData postData) throws NoApiKeyException, InputRequiredException {
        Message nextAnswer = getNextResponse(postData);
        return nextAnswer;
    }

    @PostMapping("/ti-an")
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

    private static Message getFirstResponse(PostData postData) throws NoApiKeyException, InputRequiredException {
        Generation gen = new Generation();
        String model = getModelConfigurer(postData);
        if (postData.getPersonality() != null) {
            personality = postData.getPersonality();
        } else {
            personality = "assistant";
        }
        Message systemMsg =
                Message.builder().role(Role.SYSTEM.getValue()).content(personality).build();
        Message userMsg = Message.builder().role(Role.USER.getValue()).content(postData.getQuestion()).build();
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
        Message firstAnswer = result.getOutput().getChoices().get(0).getMessage();
        incrementMessages(postData.getKey(), systemMsg);
        incrementMessages(postData.getKey(), userMsg);
        incrementMessages(postData.getKey(), firstAnswer);
        return firstAnswer;

    }

    private static KimiResponse getKimiResult(PostData postData) throws IOException {
        String model = getModelConfigurer(postData);
        List<Message> messageList = new ArrayList<>();
        Message message = new Message();
        message.setContent(postData.getQuestion());
        message.setRole("user");
        messageList.add(message);
        URL url = new URL("http://123.60.1.214:8000/v1/chat/completions");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        KimiResponse kimiResponse;
        String result = null;
        KimiRequest kimiRequest = new KimiRequest(model, messageList, true, false);
        String json = JSONUtil.toJsonStr(kimiRequest);
        try {
            result = HttpRequest.post(KIMI_CHAT_URL)
                    .header("Authorization", KIMI_API_KEY)
                    .header("Content-Type", "application/json")
                    .body(json)
                    .execute().body();
            // Process the result
        } catch (Exception e) {
            // Handle the exception
            e.printStackTrace();
        } finally {
            logger.info(result);
            kimiResponse = JSONUtil.toBean(result, KimiResponse.class);
        }

        return kimiResponse;
    }
    // get the inner model
    private static String getModelConfigurer(PostData postData) {
        String upModel = postData.getModel();
        String model = "";
        if (upModel.equals("kimi")) {
//            model = postData.getConfig().getKimi().getModel();
            model = "kimi";
        } else if (upModel.equals("qwen")) {
//            model = postData.getConfig().getQwen().getModel();
            model = "qwen1.5-72b-chat";
        }

        return model;
    }

    private static Message getNextResponse(@NotNull PostData postData) throws NoApiKeyException, InputRequiredException {
        Generation gen = new Generation();
        String model = getModelConfigurer(postData);
        Queue<Message> messages = MULTI_MESSAGES.get(postData.getKey());
        List<Message> messageList = new LinkedList<>(messages);
        GenerationParam param =
                GenerationParam.builder().model(model).messages(messageList)
                        .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                        .topP(0.8)
                        .apiKey(TONG_YI_API_KEY)
                        .build();

        if (messageList.size() == 7){
            messageList.remove(1);
            messageList.remove(1);
        }
        MULTI_MESSAGES.get(postData.getKey()).clear();
        MULTI_MESSAGES.get(postData.getKey()).addAll(messageList);
        // new question
        Message userMsg = Message.builder().role(Role.USER.getValue()).content(postData.getQuestion()).build();
        messageList.add(userMsg);
        GenerationResult result = gen.call(param);
        Message nextAnswer = result.getOutput().getChoices().get(0).getMessage();
        incrementMessages(postData.getKey(),userMsg);
        incrementMessages(postData.getKey(),nextAnswer);
        return nextAnswer;
    }


    private static String getQwenResult(PostData postData, String model){
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
        return text;
    }

//    private static boolean isAllowedToSendMessage(String key) {
//        Queue<Message> messages = MULTI_MESSAGES.get(key);
//
//        return messages.size() < MAX_ATTEMPTS;
//    }

    private static void incrementMessages(String key, Message message) {
        MULTI_MESSAGES.computeIfAbsent(key, p -> new LinkedList<>()).add(message);
    }
    
//    private static void removeHeadMessage(String key){
//        Queue<Message> messageQueue = MULTI_MESSAGES.get(key);
//        if (messageQueue != null){
//            messageQueue.poll();
//        }
//    }

}
