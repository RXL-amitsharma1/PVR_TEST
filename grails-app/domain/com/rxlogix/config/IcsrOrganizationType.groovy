package com.rxlogix.config

class IcsrOrganizationType {

    Integer org_name_id
    String name
    String description
    String e2bR2
    String e2bR3
    boolean display = false
    boolean isActive = false
    Integer tenantId
    String langId

    static mapping = {
        version: false
        table name: "ICSR_ORGANIZATION_TYPE"
        org_name_id column: "ORG_NAME_ID"
        name column: "NAME"
        description column: "DESCRIPTION"
        e2bR2 column: "E2B_R2"
        e2bR3 column: "E2B_R3"
        display column: "DISPLAY"
        isActive column: "IS_ACTIVE"
        tenantId column: "TENANT_ID"
        langId column: "LANG_ID"
    }

    static constraints = {
        org_name_id(nullable: false)
        name(nullable: false)
        description(nullable: true)
        e2bR2(nullable: true)
        e2bR3(nullable: true)
        tenantId(nullable: true)
    }

    String toString() {
        return name
    }

    boolean equals(o) {

        if (this.is(o)) return true
        if (!(o instanceof IcsrOrganizationType)) return false

        IcsrOrganizationType that = (IcsrOrganizationType) o

        if (org_name_id != that.org_name_id) return false
        if (name != that.name) return false
        if (e2bR2 != that.e2bR2) return false
        if (e2bR3 != that.e2bR3) return false
        if (display != that.display) return false
        if (isActive != that.isActive) return false
        if (tenantId != that.tenantId) return false
        if (langId != that.langId) return false

        return true
    }

    public static void copyObj(IcsrOrganizationType sourceObj, IcsrOrganizationType targetObj) {
        targetObj.with {
            org_name_id = sourceObj.org_name_id
            name = sourceObj.name
            e2bR2 = sourceObj.e2bR2
            e2bR3 = sourceObj.e2bR3
            display = sourceObj.display
            isActive = sourceObj.isActive
            tenantId = sourceObj.tenantId
            langId = sourceObj.langId
        }
    }
}
