package com.loopy.loopy.plugins.request;

import com.alibaba.dashscope.common.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KimiRequest {
    String model;
    List<Message> messages;
    boolean use_search;
    boolean stream;


}
