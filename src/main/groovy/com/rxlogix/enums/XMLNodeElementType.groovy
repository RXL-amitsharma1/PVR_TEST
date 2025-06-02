package com.rxlogix.enums

/**
 * Created by gologuzov on 23.09.17.
 */
enum XMLNodeElementType {
    TAG,
    ATTRIBUTE

    String getI18nKey() {
        return "app.XMLNodeElementType.${this.name()}"
    }
}
