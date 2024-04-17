package com.loopy.loopy.plugins.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class Config {

    @JsonProperty("kimi")
    private KimiDTO kimi;
    @JsonProperty("qwen")
    private QwenDTO qwen;

    public QwenDTO getQwenDTO() {
        return qwen;
    }

    @NoArgsConstructor
    @Data
    public static class KimiDTO {
    }

    @NoArgsConstructor
    @Data
    public static class QwenDTO {
        @JsonProperty("model")
        private String model;
        @JsonProperty("seed")
        private Integer seed;
        @JsonProperty("max_tokens")
        private Integer maxTokens;
        @JsonProperty("top_p")
        private Double topP;
        @JsonProperty("top_k")
        private Integer topK;
        @JsonProperty("repetition_penalty")
        private Float repetitionPenalty;
        @JsonProperty("temperature")
        private Float temperature;
        @JsonProperty("stop")
        private String stop;
        @JsonProperty("enable_search")
        private Boolean enableSearch;
        @JsonProperty("incremental_output")
        private Boolean incrementalOutput;
    }
}
