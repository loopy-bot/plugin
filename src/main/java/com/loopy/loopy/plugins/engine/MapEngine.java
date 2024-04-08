package com.loopy.loopy.plugins.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MapEngine extends AbstractEngine{

    private static final String MAP = "地图";

     private static final Logger logger = LoggerFactory.getLogger(MapEngine.class);

    private static final String CITY_LOOKUP_URL = "https://apis.tianapi.com/citylookup/index";

    private static final String TIAN_XING_API_KEY = "48c431c1b759a2b6882e960d24a3403c";

    @Override
    public String getAnswer(String question) {
        String city = extractCity(question);
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
            String content = "key="+TIAN_XING_API_KEY+"&area="+city;
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
        return tianApiData;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        EngineFactory.register(MAP, this);
    }

    public static String extractCity(String text) {
        // 这个正则表达式可以根据需要进行调整，以适应不同的城市命名模式
        Pattern pattern = Pattern.compile("([\\p{L}]+市|[\\p{L}]+县|[\\p{L}]+自治区|[\\p{L}]+市自治区)");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            String cityName = matcher.group(1);
            cityName = cityName.replace("市", "").replace("县", "")
                    .replace("自治区", "")
                    .replace("市自治区", "");
            return cityName;
        }
        return null;
    }
}
