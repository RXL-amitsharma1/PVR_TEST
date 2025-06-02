package com.rxlogix.user

class Role implements Comparable<Role> {

    def customMessageService

	String authority
    String description

    //Standard fields
    Date dateCreated
    Date lastUpdated
    String createdBy
    String modifiedBy

    static mapping = {
        cache true
        table name: "ROLE"

        authority column: "AUTHORITY"
        description column: "DESCRIPTION"
    }

    static constraints = {
		authority nullable: false, unique: true, maxSize: 50
        description nullable: true, maxSize: 2000

        createdBy(nullable: false, maxSize: 255)
        modifiedBy(nullable: false, maxSize: 255)
	}

    def getI18nDescription() {
        String i18nMessage =  customMessageService.getMessage("app.roles.description.${authority}", null)
        return i18nMessage ?: description
    }

    def getI18nAuthority() {
        return customMessageService.getMessage("app.role.${authority}")
    }

    Set<User> getUsers() {
        UserRole.findAllByRole(this).collect { it.user } as Set
    }

    @Override
    int compareTo(Role obj) {
        //asc order
        int value = authority <=> obj?.authority
        return value
    }

    @Override
    String toString() {
        return customMessageService.getMessage("app.role.${authority}")
    }
}
