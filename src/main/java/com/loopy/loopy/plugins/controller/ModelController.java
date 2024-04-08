package com.loopy.loopy.plugins.controller;


import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.loopy.loopy.plugins.engine.AbstractEngine;
import com.loopy.loopy.plugins.engine.EngineFactory;
import com.loopy.loopy.plugins.common.AjaxResult;

import com.loopy.loopy.plugins.common.PostData;
import com.loopy.loopy.plugins.model.AbstractModel;
import com.loopy.loopy.plugins.model.ModelFactory;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.List;


@RestController
@RequestMapping("/model")
public class ModelController {

    private static final Map<String, Queue<Message>> MULTI_MESSAGES = new HashMap<>();


    @PostMapping("/generate")
    public static AjaxResult generate(@RequestBody PostData postData) throws NoApiKeyException, InputRequiredException {
        return AjaxResult.returnSuccessDataResult(getSingleChatResult(postData));
    }


    @PostMapping("/chat")
    public static AjaxResult chat(@RequestBody PostData postData) throws NoApiKeyException, InputRequiredException {
        return AjaxResult.returnSuccessDataResult(getMultipleChatsResult(postData));
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

    private static Message getSingleChatResult(PostData postData) throws NoApiKeyException, InputRequiredException {
        String modelName = postData.getModel();
        List<Message> messageList = new ArrayList<>();
        Message message = new Message();
        message.setContent(postData.getQuestion());
        message.setRole("user");
        messageList.add(message);

        AbstractModel model = ModelFactory.getInvokeModel(modelName);
        return model.reply(messageList, postData.getConfig());
    }

    private static Message getMultipleChatsResult(PostData postData) throws NoApiKeyException, InputRequiredException {
        String modelName = postData.getModel();
        String personality = "assistant";
        if (postData.getPersonality() != null) {
            personality = postData.getPersonality();
        }
        Message systemMsg =
                Message.builder().role(Role.SYSTEM.getValue()).content(personality).build();
        List<Message> messageList = new ArrayList<>();
        messageList.add(systemMsg);
        if (MULTI_MESSAGES.get(postData.getKey()) != null) {
            messageList.addAll(MULTI_MESSAGES.get(postData.getKey()));
        }
        Message message = new Message();
        message.setContent(postData.getQuestion());
        message.setRole("user");

        messageList.add(message);

        AbstractModel model = ModelFactory.getInvokeModel(modelName);
        Message messageAnswer = model.reply(messageList, postData.getConfig());

        if (messageList.size() == 8) {
            messageList.remove(0);
            messageList.remove(0);
            messageList.remove(0);
            messageList.remove(messageList.size() - 1);
            MULTI_MESSAGES.get(postData.getKey()).clear();
            MULTI_MESSAGES.get(postData.getKey()).addAll(messageList);
        }

        if (messageAnswer.getContent() != null) {
            incrementMessages(postData.getKey(), message);
            incrementMessages(postData.getKey(), messageAnswer);
        }

        return messageAnswer;
    }

    private static void incrementMessages(String key, Message message) {
        MULTI_MESSAGES.computeIfAbsent(key, p -> new LinkedList<>()).add(message);
    }

}
