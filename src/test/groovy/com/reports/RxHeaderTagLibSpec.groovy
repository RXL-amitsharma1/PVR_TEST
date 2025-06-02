package com.reports

import com.rxlogix.UserService
import com.rxlogix.user.User
import grails.testing.web.taglib.TagLibUnitTest
import grails.util.Holders
import spock.lang.Specification

/**
 * See the API for usage instructions
 */
class RxHeaderTagLibSpec extends Specification implements TagLibUnitTest<RxHeaderTagLib> {

    def setup() {
    }

    def cleanup() {
    }

    void "test showPVQModule"() {
        given:
        String body = "Test PVQ Module enabled"
        grailsApplication.config.show.pvq.module = true
        expect:
        tagLib.showPVQModule([:], body).toString() == body
    }

    void "test showPVQModule if disabled via config"() {
        given:
        String body = "Test PVQ Module is disabled"
        grailsApplication.config.show.pvq.module = false
        expect:
        tagLib.showPVQModule([:], body).toString() == ''
    }



    void "test applyUserTheme"() {
        given:
        User  user = new User(username: 'username', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user.preference.createdBy = "createdBy"
        user.preference.modifiedBy = "modifiedBy"
        user.preference.timeZone = "UTC"
        user.preference.locale = new Locale("en")
        String code = 'default.home.label'
        String args = ""
        String out = "<div class=\"rxmain-container\">\n" +
                "                            <div class=\"rxmain-container-row rxmain-container-header\">\n" +
                "                                <label class=\"rxmain-container-header-label\">${code}</label></div></div>"

        expect:
        tagLib.renderHeaderTitle([code:code, args:args]).toString() == out
    }


}
