package com.rxlogix.mapping

class PreferredLanguage implements Serializable {
    Long id
    String langCode
    String name
    Integer langId

    static constraints = {
        name nullable: true
    }

    static mapping = {
        datasource "pva"
        table "VW_LLN_LANGUAGE"

        cache: "read-only"
        version false

        id column: "LANGUAGE_ID"
        langCode column: "ISO_CODE_639_2"
        name column: "LANGUAGE"
        langId column: "LANG_ID"
    }

    String toString() {
        return name
    }

}
