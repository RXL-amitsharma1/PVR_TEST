package com.reports

import grails.util.Holders

//TODO: Remove this taglib and usages when XML templates are properly integrated. Please refer ticket: PVR-7866 pull request #3629
class ShowXMLOptionTagLib {
    static namespace = "rx"

    def showXMLOption = {attrs, body ->
        if(Holders.config.getProperty('show.xml.option', Boolean)){
            out << body()
        }
    }
}
