package com.rxlogix.enums


public enum QualityEntryTypeEnum {
    MANUAL("M"),
    AUTO("A")

    final String value

    String value(){
        return value
    }

    QualityEntryTypeEnum(String value) {
        this.value = value
    }

    String getKey(){
        name()
    }

}
