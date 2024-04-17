package com.loopy.loopy.plugins.model;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import com.alibaba.dashscope.common.Message;

import com.loopy.loopy.plugins.request.KimiRequest;
import com.loopy.loopy.plugins.response.KimiResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


import java.util.List;



@Component
public class Kimi extends AbstractModel{

     private static final String MODEL= "kimi";
     private static final String KIMI_API_KEY = "Bearer eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJ1c2VyLWNlbnRlciIsImV4cCI6MTcxODg2ODQ2MSwiaWF0IjoxNzExMDkyNDYxLCJqdGkiOiJjbnVqNXJlY3A3ZjRzbXNsbTY1MCIsInR5cCI6InJlZnJlc2giLCJzdWIiOiJjbDRiYTQxcDJrMWZpY2wzbHJ1ZyIsInNwYWNlX2lkIjoiY2w0YmE0MXAyazFmaWNsM2xydTAiLCJhYnN0cmFjdF91c2VyX2lkIjoiY2w0YmE0MXAyazFmaWNsM2xydWcifQ.aNd4hu6sOJXSUZZSiDk2V1c6aki-m-NF1EorCzrIXaex76LUYFZqAHelrYr_k11dF-BNybmgGzUHyaHbZ9CPxw";
     private static final String KIMI_CHAT_URL = "http://123.60.1.214:8000/v1/chat/completions";
     private static final Logger logger = LoggerFactory.getLogger(Kimi.class);

     @Override
     public Message reply(List<Message> messages, Config config) {

          KimiRequest kimiRequest = new KimiRequest(MODEL, messages, true, false);
          String json = JSONUtil.toJsonStr(kimiRequest);
          KimiResponse kimiResponse;
          String result = null;
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
          return kimiResponse.getChoices().get(0).getMessage();
     }

     @Override
     public void afterPropertiesSet() throws Exception {
          ModelFactory.register(MODEL, this);
     }
}
