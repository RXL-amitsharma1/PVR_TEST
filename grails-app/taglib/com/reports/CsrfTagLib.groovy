package com.reports

import grails.util.Holders

class CsrfTagLib {

    static namespace = "rx"

    def isCsrfProtectionEnabled = {attrs, body ->
        if(Holders.config.getProperty('csrfProtection.enabled',Boolean)){
            out << body()
        }
    }
}
