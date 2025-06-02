package com.rxlogix.enums


public enum PriorityEnum {

    HIGH("High"),
    MEDIUM("Medium"),
    LOW("Low");

    final String value

    PriorityEnum(String value){
        this.value = value
    }

    //Used to get to key for dropdown lists
    String getKey() {
        name()
    }

    public getI18nKey() {
        return "app.priority.${this.name()}"
    }

}