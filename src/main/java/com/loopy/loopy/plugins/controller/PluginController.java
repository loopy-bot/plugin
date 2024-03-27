package com.loopy.loopy.plugins.controller;


import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;

import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesis;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisParam;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;
import com.alibaba.dashscope.audio.tts.SpeechSynthesisAudioFormat;
import com.alibaba.dashscope.audio.tts.SpeechSynthesisParam;
import com.alibaba.dashscope.audio.tts.SpeechSynthesizer;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.loopy.loopy.plugins.common.AjaxResult;
import com.loopy.loopy.plugins.common.FileData;
import com.loopy.loopy.plugins.common.PluginData;
import com.loopy.loopy.plugins.request.ChatRequest;
import com.loopy.loopy.plugins.response.ChatResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.*;


@RestController
@RequestMapping("/plugin")
public class PluginController {

    @Value("${file.audio_path}")
    public String audioPath;

    @Value("${file.image_path}")
    public String imagePath;

    private static final Logger logger = LoggerFactory.getLogger(PluginController.class);
    private static final String TONG_YI_API_KEY = "sk-554382667176404bb1c35d59ac5d4096";
    private static final String ALIYUN_CHAT_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";
    private static final String AUDIO_MODEL = "sambert-zhistella-v1";


    @PostMapping("/audio")
    public AjaxResult syncTextToAudio(@RequestBody String question) {
        ChatResponse chatResponse = (ChatResponse) audioChat(question).get("data");
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

        long currentTime = System.currentTimeMillis();
        String path = audioPath + "/output" + currentTime + ".wav";
        File file = new File(path);
        // 调用call方法，传入param参数，获取合成音频
        ByteBuffer audio = synthesizer.call(param);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(audio.array());
            logger.info("synthesis done!");
            String name = "output" + currentTime + ".wav";
            String url = "/audio/output" + currentTime + ".wav";
            PluginData pluginData = new PluginData("file", null, new FileData(name, url));
            return AjaxResult.returnSuccessDataResult(pluginData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public AjaxResult audioChat(@RequestBody String question) {
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


    @PostMapping("/draw")
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
        String filePath;
        List<Map<String, String>> results = result.getOutput().getResults();
        PluginData pluginData = null;
        for (Map<String, String> stringStringMap : results) {
            String url = stringStringMap.get("url");
            logger.info(url);
            filePath = saveImage(url);
            String fileName = filePath.replace("/image/", "");
            stringStringMap.put("url", filePath);
            pluginData = new PluginData("file", null, new FileData(fileName, filePath));
        }
        return AjaxResult.returnSuccessDataResult(pluginData);
    }


    public String saveImage(String imageUrl) {
        try {
            long currentTime = System.currentTimeMillis();
            // 创建URL对象
            URL url = new URL(imageUrl);
            // 打开连接
            InputStream inputStream = url.openStream();

            String fileName = imagePath + "/" + currentTime + ".png";
            // 创建文件输出流
            FileOutputStream outputStream = new FileOutputStream(fileName);

            // 创建缓冲区
            byte[] buffer = new byte[4096];
            int bytesRead;

            // 将输入流的内容读到缓冲区，然后写入文件输出流
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            // 关闭流
            outputStream.close();
            inputStream.close();

            logger.info("图片已保存至：" + imagePath);
            return "/image/" + currentTime + ".png";
        } catch (IOException e) {
            e.printStackTrace();
            logger.info("图片保存失败：" + e.getMessage());
        }
        return null;
    }

}
