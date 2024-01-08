package com.solace.ep.mapper.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MapGlobalProperty {

    protected String globalName;

    protected String globalValue;

    protected String globalDocName;

}
