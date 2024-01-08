package com.solace.ep.mapper.model;

public class MapValidateSchemaJson extends AbstractMapValidateSchema {
    
    public MapValidateSchemaJson( String schemaContents ) {
        super(schemaContents);
    }

    public MapValidateSchemaJson( String schemaContents, String validateSchemaDocName ) {
        super(schemaContents, validateSchemaDocName);
    }

}
