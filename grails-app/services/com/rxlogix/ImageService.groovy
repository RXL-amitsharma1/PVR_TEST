package com.rxlogix

import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional

@Transactional
class ImageService {

    def assetResourceLocator
    GrailsApplication grailsApplication

    def InputStream getImage(String filename) {

        InputStream inputStream

        File file = new File((grailsApplication.config.externalDirectory as String) + filename)

        if (file?.exists() && file?.size() > 0) {
            //Show the user provided version
            inputStream = file.newInputStream()
        } else {
            //Show the version we deliver with the app
            inputStream = assetResourceLocator.findAssetForURI(filename).inputStream
        }

        inputStream
    }

    InputStream getCompanyImage(){
        InputStream inputStream = getImage("company-logo.png")
        inputStream
    }

    InputStream getConfidentialLogo(){
        InputStream inputStream = getImage("sensitivity-label-confidential.png")
        inputStream
    }

    InputStream getFDALogo(){
        InputStream inputStream = getImage("fda-logo.png")
        inputStream
    }

    InputStream getMedwatchLogo(){
        InputStream inputStream = getImage("medWatch-logo.png")
        inputStream
    }
}
