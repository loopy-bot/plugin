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
import com.loopy.loopy.plugins.request.ChatRequest;
import com.loopy.loopy.plugins.response.ChatResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/plugin")
public class PluginController {

    @Value("${api.key}")
    private String tongYiApiKey;

    private static final Logger logger = LoggerFactory.getLogger(PluginController.class);
    private static final String TIANXING_APIKEY = "";
    private static final String ALIYUN_CHAT_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";
    private static final String CITY_LOOKUP_URL = "https://apis.tianapi.com/citylookup/index";
    private static final String AUDIO_MODEL = "sambert-zhistella-v1";

//    @Resource
//    private SelectPluginEngine selectPluginEngine;

    @PostMapping("/chat")
    public AjaxResult chat(String question) {

        ChatRequest chatRequest = new ChatRequest(question);
        String json = JSONUtil.toJsonStr(chatRequest);
        //System.out.println(json);//正式发送给api前,查看请求的主要数据情况
        String result = HttpRequest.post(ALIYUN_CHAT_URL)
                .header("Authorization", "Bearer " + tongYiApiKey)
                .header("Content-Type", "application/json")
                .body(json)
                .execute().body();
        logger.info(result);
        return AjaxResult.returnSuccessDataResult(JSONUtil.toBean(result, ChatResponse.class));
    }

    @PostMapping("/draw")
    public AjaxResult drawPlugin(String prompt) throws NoApiKeyException {

        ImageSynthesis is = new ImageSynthesis();
        ImageSynthesisParam param =
                ImageSynthesisParam.builder()
                        .apiKey(tongYiApiKey)
                        .model(ImageSynthesis.Models.WANX_V1)
                        .n(4)
                        .size("1024*1024")
                        .prompt(prompt)
                        .build();

        ImageSynthesisResult result = is.call(param);
        System.out.println(result);
        return AjaxResult.returnSuccessDataResult(result);
    }

    @PostMapping("/map")
    public AjaxResult getRelatedCity(String city) {
        String tianApiData = "";
        try {
            URL url = new URL(CITY_LOOKUP_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setDoOutput(true);
            conn.setRequestProperty("content-type", "application/x-www-form-urlencoded");
            OutputStream outputStream = conn.getOutputStream();
            String content = "key="+TIANXING_APIKEY+"&area="+city;
            outputStream.write(content.getBytes());
            outputStream.flush();
            outputStream.close();
            InputStream inputStream = conn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder tianapi = new StringBuilder();
            String temp = null;
            while (null != (temp = bufferedReader.readLine())) {
                tianapi.append(temp);
            }
            tianApiData = tianapi.toString();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info(tianApiData);
        return AjaxResult.returnSuccessDataResult(tianApiData);
    }


//    @GetMapping("/ip")
//    public String getRequestIP(HttpServletRequest request) {
//        System.out.println(request.getRemoteAddr());
//        return request.getRemoteAddr();
//    }

    @PostMapping("/audio")
    public ResponseEntity<byte[]> syncTextToAudio(String question){
        ChatResponse chatResponse = (ChatResponse) chat(question).get("data");
        ChatResponse.Output output = chatResponse.getOutput();
        String text = output.getText();
        SpeechSynthesizer synthesizer = new SpeechSynthesizer();
        SpeechSynthesisParam param = SpeechSynthesisParam.builder()
          .model(AUDIO_MODEL)
          .text(text)
          .sampleRate(48000)
          .format(SpeechSynthesisAudioFormat.WAV)
          .apiKey(tongYiApiKey)
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


}
