package com.rxlogix.dto;

public class AuditTrailChildDTO {

    String propertyName
    String oldValue
    String newValue

    AuditTrailChildDTO(String propertyName, String oldValue, String newValue){
        this.propertyName = propertyName
        this.oldValue = oldValue
        this.newValue = newValue
    }
}
