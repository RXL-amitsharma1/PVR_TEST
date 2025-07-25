package com.rxlogix

import grails.util.GrailsWebMockUtil
import grails.web.mime.MimeType
import groovy.transform.CompileStatic
import org.grails.plugins.testing.GrailsMockHttpServletRequest
import org.grails.plugins.testing.GrailsMockHttpServletResponse
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.junit.Ignore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.context.request.RequestContextHolder
import spock.lang.Specification

@CompileStatic
abstract class BaseControllerIntegrationSpec extends Specification {

    static String FORM_CONTENT_TYPE = MimeType.FORM.name

    @Autowired
    WebApplicationContext ctx

    void setup() {
        MockHttpServletRequest request = new GrailsMockHttpServletRequest(ctx.servletContext)
        MockHttpServletResponse response = new GrailsMockHttpServletResponse()
        GrailsWebMockUtil.bindMockWebRequest(ctx, request, response)
        currentRequestAttributes.setControllerName(controllerName)
    }

    @Ignore
    abstract String getControllerName()

    @Ignore
    protected GrailsWebRequest getCurrentRequestAttributes() {
        return (GrailsWebRequest) RequestContextHolder.currentRequestAttributes()
    }

    void cleanup() {
        RequestContextHolder.resetRequestAttributes()
    }

    @Ignore
    def autowire(Class clazz) {
        def bean = clazz.newInstance()
        ctx.autowireCapableBeanFactory.autowireBeanProperties(bean, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false)
        bean
    }


    @Ignore
    def autowire(def bean) {
        ctx.autowireCapableBeanFactory.autowireBeanProperties(bean, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false)
        bean
    }
}