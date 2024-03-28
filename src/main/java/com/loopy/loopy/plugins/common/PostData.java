package com.loopy.loopy.plugins.common;

import com.alibaba.dashscope.audio.asr.phrase.AsrPhraseInfo;
import com.loopy.loopy.plugins.model.Kimi;
import com.loopy.loopy.plugins.model.Qwen;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostData {

    String key;

    String model;

    String question;

    String personality;

    Config config;

    public class Config {
        @Getter
        public Kimi kimi;
        @Getter
        public Qwen qwen;

    }

}
