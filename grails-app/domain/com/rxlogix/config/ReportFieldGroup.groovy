package com.rxlogix.config

class ReportFieldGroup {
    String name
    boolean isDeleted = false
    int priority = 0


    static mapping = {
        cache: "read-only"
        version: false

        table name: "RPT_FIELD_GROUP"
        id name: "name", generator: "assigned"
        name column: "NAME"
        isDeleted column: "IS_DELETED"
        priority column: "PRIORITY"
    }

    static constraints = {
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof ReportFieldGroup)) return false

        ReportFieldGroup that = (ReportFieldGroup) o
        if (name != that.name) return false
        if (priority != that.priority) return false
        return true
    }

    int hashCode() {
        int result
        result = (name != null ? name.hashCode() : 0)
//        result = 31 * result + id.hashCode()
//        result = 31 * result + version.hashCode()
        result = 31 * result + priority
        return result
    }

    public String toString() {
        return name
    }
}
