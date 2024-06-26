package com.loopy.loopy.plugins.response;

import com.alibaba.dashscope.common.Message;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KimiResponse {

    @JsonProperty("id")
    private String id;
    @JsonProperty("model")
    private String model;
    @JsonProperty("object")
    private String object;
    @JsonProperty("choices")
    private List<ChoicesDTO> choices;
    @JsonProperty("usage")
    private UsageDTO usage;
    @JsonProperty("created")
    private Integer created;

    @NoArgsConstructor
    @Data
    public static class UsageDTO {
        @JsonProperty("prompt_tokens")
        private Integer promptTokens;
        @JsonProperty("completion_tokens")
        private Integer completionTokens;
        @JsonProperty("total_tokens")
        private Integer totalTokens;
    }

    @NoArgsConstructor
    @Data
    public static class ChoicesDTO {
        @JsonProperty("index")
        private Integer index;
        @JsonProperty("message")
        private Message message;
        @JsonProperty("finish_reason")
        private String finishReason;

    }
}

