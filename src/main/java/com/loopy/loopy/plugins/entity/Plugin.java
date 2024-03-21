package com.loopy.loopy.plugins.entity;

import cn.hutool.core.lang.hash.Hash;
import com.loopy.loopy.plugins.enums.MethodType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Data
@AllArgsConstructor
public class Plugin {

    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private String description;
    private String type;
    private String url;
    private MethodType method;
    private Long params;
    private String responseType;
}
