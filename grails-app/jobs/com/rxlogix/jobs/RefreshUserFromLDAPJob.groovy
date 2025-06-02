package com.rxlogix.jobs

import grails.core.GrailsApplication
import grails.gorm.multitenancy.WithoutTenant

class RefreshUserFromLDAPJob {

    def userService
    GrailsApplication grailsApplication
    def ldapService
    static concurrent = false
    static group = "RxLogixPVR"

    static triggers = {
        cron name:'refreshUserTrigger', startDelay:10000, cronExpression: '0 30 3 1/1 * ? *'    //Daily at 3:30am
    }

    @WithoutTenant
    def execute() {
        ldapService.mirrorLdapValues()
        log.info "LDAP values mirrored"
    }
}
