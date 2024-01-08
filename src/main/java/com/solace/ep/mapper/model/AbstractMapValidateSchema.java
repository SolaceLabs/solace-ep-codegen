package com.solace.ep.mapper.model;

import lombok.Getter;
import lombok.Setter;

public abstract class AbstractMapValidateSchema {
    
    @Getter
    @Setter
    protected String validateSchemaDocName = "Validate Schema";

    @Getter
    protected String validateSchemaContents = null;

    @Getter
    private byte[] md5Digest = null;

    public AbstractMapValidateSchema( String schemaContents ) {
        setValidateSchemaContents(schemaContents);
    }

    public AbstractMapValidateSchema( String schemaContents, String validateSchemaDocName ) {
        this.validateSchemaDocName = validateSchemaDocName;
        setValidateSchemaContents(schemaContents);
    }

    public void setValidateSchemaContents( String schemaContents ) {
        validateSchemaContents = schemaContents;
        md5Digest = MapUtils.getMd5Digest(schemaContents);
    }
}
