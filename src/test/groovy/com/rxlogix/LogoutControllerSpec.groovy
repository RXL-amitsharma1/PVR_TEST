package com.rxlogix


import grails.plugin.springsecurity.SpringSecurityUtils
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import grails.util.Holders
import org.springframework.security.web.RedirectStrategy
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([SpringSecurityUtils])
class LogoutControllerSpec extends Specification implements DataTest, ControllerUnitTest<LogoutController>{

    void "test index, when condition is false"(){
        given:
        SpringSecurityUtils.metaClass.static.getSecurityConfig = {-> return [logout: [postOnly: false]]}
        def mockRedirectStrategy= Mock( RedirectStrategy )
        controller.redirectStrategy = mockRedirectStrategy
        when:
        controller.index()
        then:
        response.status == 200
    }

    void "test index, when condition is true"(){
        given:
        SpringSecurityUtils.metaClass.static.getSecurityConfig = {-> return [logout: [postOnly: true]]}
        when:
        controller.index()
        then:
        response.status == 405
    }

    void "Test local, --failure"(){
        given:
        Holders.config.grails.plugin.springsecurity.saml.active=false
        when:
        controller.local()
        then:
        response.status==302
        response.redirectedUrl=="/"
    }

    void "Test local,--Success"(){
        given:
        Holders.config.grails.plugin.springsecurity.saml.active=true
        when:
        controller.local()
        then:
        response.status==200
    }
}
