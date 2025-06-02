grails.gorm.default.mapping = {
    id column: "ID"
    dateCreated column: "DATE_CREATED"
    lastUpdated column: "LAST_UPDATED"
    createdBy column: "CREATED_BY"
    modifiedBy column: "MODIFIED_BY"
    version column: "VERSION"
    autowire true
}

grails.gorm.default.constraints = {
    createdBy maxSize: 50
    modifiedBy maxSize: 50
}