package com.loopy.loopy.plugins.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostData {

    String key;

    String model;

    Messages messages;

    String personality;


//    Config config;

    public class Messages {
        public String role;
        public String content;
    }

//    class Config {
//
//    }

}
