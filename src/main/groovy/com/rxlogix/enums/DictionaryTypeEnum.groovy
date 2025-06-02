package com.rxlogix.enums

enum DictionaryTypeEnum {

    PRODUCT("productSelection"),
    STUDY("studySelection"),
    EVENT("eventSelection")

    final String val

    DictionaryTypeEnum(String val){
        this.val = val
    }

    String value() { return val }
}