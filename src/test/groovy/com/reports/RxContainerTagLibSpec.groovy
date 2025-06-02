package com.reports


import grails.testing.web.taglib.TagLibUnitTest
import spock.lang.Ignore
import spock.lang.Specification

class RxContainerTagLibSpec extends Specification implements TagLibUnitTest<RxContainerTagLib> {

    void "test for search" () {
        given:
        String id = 'test_search'
        String url = 'http://localhost:6060/reports/test'
        String placeholder = 'For testing purpose'
        String out = "\n" +
                "            <input type=\"hidden\" id=\"test_search_JSONUrl\" value=\"${url}\"/>\n" +
                "            <input type=\"hidden\" id=\"${id}.id\" name=\"${id}.id\"  />\n" +
                "            <div class=\"right-inner-addon\">\n" +
                "                <i class=\"glyphicon glyphicon-search\"></i>\n" +
                "                <input type=\"search\" class=\"form-control\" id=\"${id}\" placeholder=\"${placeholder}\"/>\n" +
                "            </div>\n" +
                "        "
        expect:
        tagLib.search([searchID:id, placeholder:placeholder,JSONUrl: url ]).toString() == out
    }


    @Ignore
    void "test for container" () {
        given:
        String title = 'test title'
        String options = 'true'
        String body = "<h1>Just for testing purpose</h1>"
        String closeButton = "Cancel"
        String customButtons = ""

        String out = "<div class=\"rxmain-container\"><div class=\"rxmain-container-inner\"><div class=\"rxmain-container-row rxmain-container-header\"><div class=\"dropdown\"><label class=\"rxmain-container-header-label\" style=\"display: inline\">test title</label><i class=\"pull-right md md-close md-lg rxmain-dropdown-settings\" data-dismiss=\"modal\"></i><i class=\"pull-right dropdown-toggle md md-list md-lg rxmain-dropdown-settings\" id=\"dropdownMenu1\" data-toggle=\"dropdown\"></i><div class=\"pull-right dropdown-menu\" aria-labelledby=\"dropdownMenu1\"><div class=\"rxmain-container-dropdown\"><div><table id=\"tableColumns\" class=\"table table-condensed rxmain-dropdown-settings-table\"><thead><tr><th>app.label.name</th><th>app.label.show</th></tr></thead></table></div></div></div></div></div><div class=\"rxmain-container-content\"><h1>Just for testing purpose</h1></div></div></div>"
        expect:
        tagLib.container([title: title, options: options, closeButton: closeButton, customButtons: customButtons], body).toString() == out
    }
}