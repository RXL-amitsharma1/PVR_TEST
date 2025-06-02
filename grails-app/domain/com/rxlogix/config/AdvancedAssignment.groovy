package com.rxlogix.config

import com.rxlogix.enums.AdvancedAssignmentCategoryEnum
import com.rxlogix.user.User

class AdvancedAssignment {

    String name
    AdvancedAssignmentCategoryEnum category
    User assignedUser
    String description
    Boolean qualityChecked = false
    String assignmentQuery
    Boolean isDeleted = false

    //Standard fields
    Date dateCreated
    Date lastUpdated
    String createdBy
    String modifiedBy

    Long tenantId

    static mapping = {
        table name: "ADVANCED_ASSIGNMENT"
        id column: 'ID', generator: "sequence", params: [sequence: "ADVANCED_ASSIGNMENT_ID"]
        name column: "NAME"
        category column: "CATEGORY"
        assignedUser column: "ASSIGNED_USER"
        description column: "DESCRIPTION"
        qualityChecked column: "QUALITY_CHECKED"
        assignmentQuery column: "ASSIGNMENT_QUERY"
        isDeleted column: "ISDELETED"
        dateCreated column: 'DATE_CREATED'
        lastUpdated column: 'LAST_UPDATED'
        createdBy column: 'CREATED_BY'
        modifiedBy column: 'MODIFIED_BY'
        tenantId column: 'TENANT_ID'
        version false
    }


    static constraints = {
        category nullable: true
        description nullable: true
    }

    Map toAdvancedAssignmentMap() {
        [
                id: id,
                name: name,
                category: category?.name(),
                assignedUser: assignedUser.getFullName(),
                description: description,
                qualityChecked: qualityChecked
        ]
    }
}
