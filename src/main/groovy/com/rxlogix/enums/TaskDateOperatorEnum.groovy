package com.rxlogix.enums

/**
 * Created by Chetan on 3/8/2016.
 */
enum TaskDateOperatorEnum {

    PLUS("+"),
    MINUS("-");

    final String value

    TaskDateOperatorEnum(String value){
        this.value = value
    }

    //Used to get to key for dropdown lists
    String getKey() {
        name()
    }

}