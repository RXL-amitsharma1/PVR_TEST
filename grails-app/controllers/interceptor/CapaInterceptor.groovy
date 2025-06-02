package interceptor

import grails.config.Config
import grails.core.support.GrailsConfigurationAware

class CapaInterceptor implements GrailsConfigurationAware {

      Boolean pvCentralEnabled
      Boolean showPvcModule
      Boolean pvQualityEnabled
      Boolean showPvqModule

      CapaInterceptor() {
            match(controller: "capa")
      }

      void setConfiguration(Config config) {
            pvCentralEnabled = config.getProperty('pv.app.pvcentral.enabled', Boolean, false)
            showPvcModule    = config.getProperty('show.pvc.module', Boolean, false)
            pvQualityEnabled = config.getProperty('pv.app.pvquality.enabled', Boolean, false)
            showPvqModule    = config.getProperty('show.pvq.module', Boolean, false)
      }

      boolean before() {
            if (!((pvCentralEnabled && showPvcModule) || (pvQualityEnabled && showPvqModule))) {
                  redirect(controller: 'errors', action: 'forbidden')
                  return false
            }
            return true
      }

}