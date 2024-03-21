package com.loopy.loopy.plugins.response;

//这个是用来处理响应的
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesis;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisParam;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;
import com.alibaba.dashscope.exception.NoApiKeyException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DrawResponse {
    Output output;
    Usage usage;
    String request_id;
    ImageResult imageResult;

    class Output{
        public String task_id;
        public String task_status;

        public HashMap<String, String> results;
    }
    class Usage{
        public int image_count;
    }

    class ImageResult{
        public ImageSynthesisResult imageSynthesisResult;
    }
}

