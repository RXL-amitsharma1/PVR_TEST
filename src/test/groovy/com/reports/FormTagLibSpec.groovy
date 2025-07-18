package com.reports


import grails.testing.web.taglib.TagLibUnitTest
import spock.lang.Specification

class FormTagLibSpec extends Specification implements TagLibUnitTest<FormTagLib> {

    void "test localeSelect"() {


        
        given:
        String attr = "name = 'test'"
        String formedTags = '<select name="test" id="test" >\r\n<option value="en" selected="selected" >English</option>\r\n<option value="ja" >Japanese</option>\r\n</select>'
        expect:
        tagLib.localeSelect([name:'test']).toString() == formedTags
    }

}
