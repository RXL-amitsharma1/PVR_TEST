package com.rxlogix

import com.rxlogix.config.ApplicationSettings
import grails.util.Holders

class ApplicationSettingsService {

    static transactional = false

    Boolean dmsIntegration = null

    boolean hasDmsIntegration() {
        if (dmsIntegration == null)
            dmsIntegration = ApplicationSettings.first().dmsIntegration as Boolean
        return dmsIntegration
    }

    void reload() {
        dmsIntegration = ApplicationSettings.first().dmsIntegration as Boolean
    }

    void dmsCacheRefresh(){
        Holders.applicationContext.getBean("dmsService").clear()
        reload()
    }
}
