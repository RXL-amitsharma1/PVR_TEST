package com.rxlogix.config

class PvrTags {

    String name
    String type

    static mapping = {
        datasource "pva"
        table name: "TAG_LIST"
        cache: "read-only"
        version false

        id column: "TAG_ID"
        name column: "TAG_TEXT"
        type column: "TAG_DESC"
    }

    static constraints = {
        name nullable: false
    }

    public String toString() {
        return name
    }
}
