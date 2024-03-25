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
import com.alibaba.dashscope.utils.JsonUtils;
import com.loopy.loopy.plugins.Engine.AbstractEngine;
import com.loopy.loopy.plugins.Engine.EngineFactory;
import com.loopy.loopy.plugins.common.AjaxResult;
import com.loopy.loopy.plugins.common.FormerRequest;
import com.loopy.loopy.plugins.common.PostData;
import com.loopy.loopy.plugins.request.ChatRequest;
import com.loopy.loopy.plugins.response.ChatResponse;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;


@RestController
@RequestMapping("/plugin")
public class PluginController {

    private static final Logger logger = LoggerFactory.getLogger(PluginController.class);
    private static final String TONG_YI_API_KEY = "sk-554382667176404bb1c35d59ac5d4096";
    private static final String ALIYUN_CHAT_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";
    private static final String AUDIO_MODEL = "sambert-zhistella-v1";
    private static final Map<String, Queue<PostData>> MULTI_MESSAGES = new HashMap<>();
    private static final int MAX_ATTEMPTS = 3;

    private static FormerRequest FORMER_REQUEST;

    @PostMapping("/audio")
    @ResponseBody
    public ResponseEntity<byte[]> syncTextToAudio(@RequestBody String question) {
        ChatResponse chatResponse = (ChatResponse) chat(question).get("data");
        ChatResponse.Output output = chatResponse.getOutput();
        String text = output.getText();
        SpeechSynthesizer synthesizer = new SpeechSynthesizer();
        SpeechSynthesisParam param = SpeechSynthesisParam.builder()
                .model(AUDIO_MODEL)
                .text(text)
                .sampleRate(48000)
                .format(SpeechSynthesisAudioFormat.WAV)
                .apiKey(TONG_YI_API_KEY)
                .build();

        File file = new File("output.wav");
        // 调用call方法，传入param参数，获取合成音频
        ByteBuffer audio = synthesizer.call(param);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(audio.array());
            logger.info("synthesis done!");
            return new ResponseEntity<>(audio.array(), HttpStatus.OK);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//        System.exit(0);

    }

    @PostMapping("/chat")
    @ResponseBody
    public AjaxResult chat(@RequestBody String question) {

        ChatRequest chatRequest = new ChatRequest(question);
        String json = JSONUtil.toJsonStr(chatRequest);
        //System.out.println(json);//正式发送给api前,查看请求的主要数据情况
        String result = HttpRequest.post(ALIYUN_CHAT_URL)
                .header("Authorization", "Bearer " + TONG_YI_API_KEY)
                .header("Content-Type", "application/json")
                .body(json)
                .execute().body();
        logger.info(result);
        return AjaxResult.returnSuccessDataResult(JSONUtil.toBean(result, ChatResponse.class));
    }

    @PostMapping("/chats")
    @ResponseBody
    public AjaxResult chats(@RequestBody PostData postData) throws NoApiKeyException, InputRequiredException {
        if (postData.getKey() != null){
            if (MULTI_MESSAGES.get(postData.getKey()) == null){
                //首发
                incrementMessages(postData);
                FORMER_REQUEST = getFirstResponse(postData);
                return AjaxResult.returnSuccessDataResult(FORMER_REQUEST.getFormerResult());
            }
            else {
                if (isAllowedToSendMessage(postData.getKey())) {
                    incrementMessages(postData);
                    FORMER_REQUEST = getNextResponse(postData, FORMER_REQUEST);
                    return AjaxResult.returnSuccessDataResult(FORMER_REQUEST.getFormerResult());
                } else {
                    return AjaxResult.error("很抱歉，目前只支持3轮问答");
                }
            }

        }
        else{
            return chat(postData.getMessages().content);
        }
    }


    @PostMapping("/draw")
    @ResponseBody
    public AjaxResult drawPlugin(@RequestBody String prompt) throws NoApiKeyException {

        ImageSynthesis is = new ImageSynthesis();
        ImageSynthesisParam param =
                ImageSynthesisParam.builder()
                        .apiKey(TONG_YI_API_KEY)
                        .model(ImageSynthesis.Models.WANX_V1)
                        .n(4)
                        .size("1024*1024")
                        .prompt(prompt)
                        .build();

        ImageSynthesisResult result = is.call(param);
        System.out.println(result);
        return AjaxResult.returnSuccessDataResult(result);
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

//    @GetMapping("/ip")
//    public String getRequestIP(HttpServletRequest request) {
//        System.out.println(request.getRemoteAddr());
//        return request.getRemoteAddr();
//    }


//    public byte[] byteBufferToByteArray(ByteBuffer byteBuffer){
//        int len = byteBuffer.limit() - byteBuffer.position();
//        byte[] bytes = new byte[len];
//        byteBuffer.get(bytes);
//        return bytes;
//
//    }
//
//    public NativeArrayBuffer bytesToArrayBuffer(byte[] bytes) {
//        int len = bytes.length;
//        NativeArrayBuffer newBuf = new NativeArrayBuffer(len);
//        System.arraycopy(bytes, 0, newBuf.getBuffer(), 0, len);
//        return newBuf;
//    }


    public FormerRequest getFirstResponse(PostData postData) throws NoApiKeyException, InputRequiredException {
        Generation gen = new Generation();
        String systemRole;
        if (postData.getPersonality() != null){
            systemRole = postData.getPersonality();
        }
        else {
            systemRole = "You are a helpful assistant.";
        }
        Message systemMsg =
                Message.builder().role(Role.SYSTEM.getValue()).content(systemRole).build();
        Message userMsg = Message.builder().role(Role.USER.getValue()).content(postData.getMessages().content).build();
        List<Message> messages = new ArrayList<>();
        messages.add(systemMsg);
        messages.add(userMsg);
        GenerationParam param =
                GenerationParam.builder().model(postData.getModel()).messages(messages)
                        .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                        .topP(0.8)
                        .apiKey(TONG_YI_API_KEY)
                        .build();
        GenerationResult result = gen.call(param);
//        Message m = result.getOutput().getChoices().get(0).getMessage();
        FormerRequest formerRequest = new FormerRequest(result, messages);
        return formerRequest;
    }

    private FormerRequest getNextResponse(@NotNull PostData postData, FormerRequest formerRequest) throws NoApiKeyException, InputRequiredException {
        Generation gen = new Generation();
        GenerationParam param =
                GenerationParam.builder().model(postData.getModel()).messages(formerRequest.getMessages())
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

    public boolean isAllowedToSendMessage(String key){
		Queue<PostData> messages = MULTI_MESSAGES.get(key);

        return messages.size() < MAX_ATTEMPTS;
    }

	public void incrementMessages(PostData postData){
		MULTI_MESSAGES.computeIfAbsent(postData.getKey(), p -> new LinkedList<>()).add(postData);
	}

}
