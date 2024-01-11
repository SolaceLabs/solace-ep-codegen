package com.solace.ep.mapper.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MapGlobalProperty {

    protected String globalName;

    protected String globalValue;

}
