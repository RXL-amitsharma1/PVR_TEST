package com.rxlogix

import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import org.apache.http.HttpStatus

@Secured(["isAuthenticated()"])
class LookupController {

    def lookup() {
        String name = params.get('name')
        name = name.capitalize()

        def clz = getDomainClass(name)
        if (clz) {
            render status: HttpStatus.SC_OK, contentType: 'application/json', text: clz."list"() as JSON
        } else {
            render status: HttpStatus.SC_OK, contentType: 'application/json', text: [] as JSON
        }
    }

    private Class getDomainClass(String clzName) {
        try {
            Class.forName("com.rxlogix.config.$clzName")
        } catch (Throwable t) {
            log.error("Error occured for looking up $clzName", t)
            null
        }
    }
}
