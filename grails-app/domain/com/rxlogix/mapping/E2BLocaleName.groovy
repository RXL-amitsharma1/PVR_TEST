package com.rxlogix.mapping

class E2BLocaleName {

    String e2bLocale
    String e2bLocaleElementName

    static mapping = {
        table name: "E2B_LOCALE_NAME"
        id column: "ID"
        e2bLocale column: "LOCALE"
        e2bLocaleElementName column: "LOCALE_ELEMENT_NAME"
        version false
    }

    static constraints = {
        e2bLocale nullable: false
        e2bLocaleElementName nullable: false
    }
}
