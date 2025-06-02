package com.rxlogix.public_api

import grails.converters.JSON
import org.springframework.security.access.annotation.Secured
import com.rxlogix.mapping.OrganizationCountry

@Secured('permitAll')
class PublicUnitConfigurationController {

    def unitConfigurationService

    def fetchRecipientDetailList() {
        def recipientList = unitConfigurationService.getAllRecipientsList()
        OrganizationCountry.withNewSession{
            recipientList.each{
                it.country=OrganizationCountry.findByName(it.country)?.id
            }
        }
        render([result: recipientList, totalCount: recipientList ? recipientList.size() : 0] as JSON)
    }
}
