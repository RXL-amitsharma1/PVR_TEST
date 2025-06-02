package com.rxlogix

import com.rxlogix.config.IcsrOrganizationType

class OrganizationTypeService {
    def userService
    def sqlGenerationService

    def getAllIcsrOrganizationType(){
        Integer langId = sqlGenerationService.getPVALanguageId(userService.currentUser?.preference?.locale?.toString() ?: 'en')
        return IcsrOrganizationType.findAllByLangId(langId)
    }

}
