package com.solace.ep.mapper.model;

import com.solace.ep.mapper.MapUtils;

import lombok.Getter;

public abstract class AbstractMapValidateSchema {
    
    @Getter
    protected String validateSchemaContents = null;

    @Getter
    private byte[] md5Digest = null;

    public AbstractMapValidateSchema() {
    }

    public AbstractMapValidateSchema( String schemaContents ) {
        setValidateSchemaContents(schemaContents);
    }

    public void setValidateSchemaContents( String schemaContents ) {
        validateSchemaContents = schemaContents;
        md5Digest = MapUtils.getMd5Digest(schemaContents);
    }
}
