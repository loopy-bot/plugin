package com.loopy.loopy.plugins.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Parameter {

    @Id
    @GeneratedValue
    private Long id;

    private String systemRole;
    private String systemContent;
    private String userRole;
    private String userContent;


}
