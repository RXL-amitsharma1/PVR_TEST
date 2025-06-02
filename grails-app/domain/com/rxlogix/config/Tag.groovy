package com.rxlogix.config

import grails.gorm.dirty.checking.DirtyCheck

@DirtyCheck
class Tag {
    String name

    static constraints = {
        name(unique: true, blank: false)
    }

    static mapping = {
        table name: "TAG"
        name column: "NAME"
    }

    def getInstanceIdentifierForAuditLog() {
        return name
    }

    public String toString() {
        return name
    }

    static List<Tag> getAllExcludingQualityTags() {
        // Retrieve all tags excluding those with names containing "PV Quality :"
        return Tag.findAll().findAll { !it.name.contains("PV Quality:") }
    }
}
