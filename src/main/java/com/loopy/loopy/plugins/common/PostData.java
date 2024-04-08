package com.loopy.loopy.plugins.common;


import com.loopy.loopy.plugins.model.Config;
import lombok.AllArgsConstructor;
import lombok.Data;
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


}
