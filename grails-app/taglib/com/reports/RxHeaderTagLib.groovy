package com.reports

import grails.util.Holders

class RxHeaderTagLib {

    static namespace = "rx"

    def userService

    def renderHeaderTitle = { attr ->
        def content = """<div class="rxmain-container">
                            <div class="rxmain-container-row rxmain-container-header">
                                <label class="rxmain-container-header-label">"""
        content += message(code: attr.code, args: attr.args)
        content += """</label></div></div>"""

        out << content
    }

    def applyUserTheme = { attr ->
        String userTheme = null
        try {
            userTheme = userService.currentUser?.preference?.theme
        } catch (e) {
            log.error(e.message)
        }
        if (userTheme == 'solid_orange') {
            out << asset.stylesheet(href: 'theme_solid_orange.css')
        } else if (userTheme == 'solid_blue') {
            out << asset.stylesheet(href: 'theme_solid_blue.css')
        } else if (userTheme == 'solid_golden_grey') {
            out << asset.stylesheet(href: 'theme_solid_golden_grey.css')
        } else {
            out << asset.stylesheet(href: 'theme_gradient_blue.css')
        }
    }

    def showPVQModule = { attr, body ->
        if (Holders.config.getProperty('show.pvq.module', Boolean)) {
            out << body().toString()
        }
    }
    def showPVPModule = { attr, body ->
        if (Holders.config.getProperty('show.pvp.module', Boolean)) {
            out << body().toString()
        }

    }
    def showPVCModule = { attr, body ->
        if (Holders.config.getProperty('show.pvc.module', Boolean)) {
            out << body().toString()
        }
    }
}