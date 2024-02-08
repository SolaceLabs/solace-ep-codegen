package com.solace.ep.muleflow.mapper.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchemaInstance {
    
    private String name;

    private String version;

    private String suffix;

    private String fileName;

    private String payload;

}
