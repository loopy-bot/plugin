package com.loopy.loopy.plugins.response;

import com.alibaba.dashscope.common.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KimiResponse {
    String id;
    String model;
    String object;
    List<Choice> choices;
    Usage usage;
    Long created;


    class Usage{
        int prompt_tokens;
        int completion_tokens;
        int total_tokens;
    }
   public class Choice{
        int index;

        @Getter
        Message message;
        String finish_reason;

    }
}

