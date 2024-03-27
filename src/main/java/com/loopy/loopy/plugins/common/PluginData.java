package com.loopy.loopy.plugins.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PluginData {
    String type;
    String text;
    FileData fileData;

}
