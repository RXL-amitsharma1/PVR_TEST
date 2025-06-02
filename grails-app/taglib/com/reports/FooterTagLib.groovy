package com.reports

import grails.util.Holders
import grails.util.Metadata

class FooterTagLib {
    static  namespace = "rx"
    def seedDataService


    /**
     * Render page load statistics and build version number
     */

    def pageInfo = { attrs, body ->

        def now = System.currentTimeMillis()
        def afterView = request.afterView ?: now
        def startTime = request.startTime ?: now
        def gitPropertiesFile = grailsApplication.classLoader.getResource("git.properties")
        def gitVersion = 'UNKNOWN'
        if (gitPropertiesFile) {
            def gitProperties = new Properties()
            gitPropertiesFile.openStream().withStream { inputStream ->
                gitProperties.load(inputStream)
            }
            gitVersion = "${gitProperties.getProperty('git.commit.id.abbrev', 'UNKNOWN')}"
        }
        out << "<p class='right'>Application Version "
        out << rx.buildVersion()
        out<< "PVR_Drop18_HF4_28-May-2025 "
        out << "Git Version: ${gitVersion} "
        out << g.formatDate(date: lookupDate.call(), type: "datetime", style: "LONG", timeZone: attrs.timeZone)
        out << " - Loaded: ${now -afterView}/${now-startTime}ms</p>"
        out << "DB Version: " + "${seedDataService.getDatabaseVersion()} "
    }

    String buildVersion = { attrs ->
        out << lookupVersion.call()
    }

    String buildDate = { attrs ->
        out << lookupDate.call()
    }

    private def lookupDate = { ->
        def buildDate = g.meta(name: 'build.time')
        if ( buildDate ) {
            return Date.parse("dd-MMM-yyyy HH:mm:ss zzz", buildDate)
        }
        return new Date()
    }.memoize()

    private def lookupVersion = { ->
        return "${Metadata.current.getApplicationVersion()} "
    }.memoize()

    def renderSecurityPolicyLink = { attrs ->
        if (!Holders.config.getProperty('pvreports.privacy.policy.link', Boolean)) {
            out << ""
            return
        }
        attrs.url = Holders.config.pvreports.privacy.policy.link
        out << link(attrs) {
            message(code: 'app.security.privacy.policy.link.label')
        }
    }
}
