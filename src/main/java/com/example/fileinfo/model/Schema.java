package com.example.fileinfo.model;

import java.util.List;


public class Schema {
    private String recordRoot;
    private List<FieldDefinition> fields;

    // Getters y Setters
    public String getRecordRoot() { return recordRoot; }
    public void setRecordRoot(String recordRoot) { this.recordRoot = recordRoot; }
    public List<FieldDefinition> getFields() { return fields; }
    public void setFields(List<FieldDefinition> fields) { this.fields = fields; }
}