package com.loopy.loopy.plugins.Engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Component
public class ITNewsEngine extends AbstractEngine {

    private static final String IT= "IT资讯";

    private static final String TIAN_XING_API_KEY = "48c431c1b759a2b6882e960d24a3403c";

    private static final Logger logger = LoggerFactory.getLogger(ITNewsEngine.class);

    @Override
    public String getAnswer(String question) {
        String iTData = "";
        try {
            URL url = new URL("https://apis.tianapi.com/it/index");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setDoOutput(true);
            conn.setRequestProperty("content-type", "application/x-www-form-urlencoded");
            OutputStream outputStream = conn.getOutputStream();
            String content = "key="+TIAN_XING_API_KEY+"&num=10";
            outputStream.write(content.getBytes());
            outputStream.flush();
            outputStream.close();
            InputStream inputStream = conn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder ti = new StringBuilder();
            String temp = null;
            while (null != (temp = bufferedReader.readLine())) {
                ti.append(temp);
            }
            iTData = ti.toString();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info(iTData);
        return iTData;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        EngineFactory.register(IT, this);
    }
}
