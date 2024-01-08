package com.solace.ep.mapper.model;

public class MapValidateSchemaXml extends AbstractMapValidateSchema {
    
    public MapValidateSchemaXml( String schemaContents ) {
        super(schemaContents);
    }

    public MapValidateSchemaXml( String schemaContents, String validateSchemaDocName ) {
        super(schemaContents, validateSchemaDocName);
    }

}
