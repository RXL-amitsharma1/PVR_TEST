package com.rxlogix.enums

/**
 * Created by gologuzov on 23.09.17.
 */
enum XMLNodeType {
    TAG_PROPERTIES,
    SOURCE_FIELD

    String getI18nKey() {
        return "app.XMLNodeType.${this.name()}"
    }
}
