package com.rxlogix.api

import com.rxlogix.UserService
import com.rxlogix.config.IcsrOrganizationType

import com.rxlogix.mapping.AllowedAttachment
import com.rxlogix.config.UnitConfiguration
import com.rxlogix.util.DateUtil
import com.rxlogix.util.MiscUtil
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.rest.RestfulController
import com.rxlogix.util.ViewHelper

@Secured('permitAll')
class UnitConfigurationRestController extends RestfulController implements SanitizePaginationAttributes{

    def unitConfigurationService
    def userService
    def sqlGenerationService

    UnitConfigurationRestController() {
        super(UnitConfiguration)
    }

    def list() {
        sanitize(params)
        Integer langId = sqlGenerationService.getPVALanguageId(userService.currentUser?.preference?.locale?.toString() ?: 'en')
        def unitConfigurationNameQuery = UnitConfiguration.getAllUnitConfigurationBySearchString(params.searchString, params.sort, params.order)
        List<UnitConfiguration> unitConfigurationList = unitConfigurationNameQuery.list([max: params.max, offset: params.offset])
        List<Map> unitConfigurations = unitConfigurationList.collect{
            [id:it.id, unitName:it.unitName, unitType:it.unitType ? message(code: it.unitType?.i18nKey):'', organizationType:getOrganizationTypeByPreference(it.organizationType, langId), unitRegisteredId:it.unitRegisteredId, unitRetired: it.unitRetired, dateCreated: it.dateCreated.format(DateUtil.DATEPICKER_UTC_FORMAT), lastUpdated: it.lastUpdated.format(DateUtil.DATEPICKER_UTC_FORMAT), createdBy: userService.getUserByUsername(it.createdBy).getFullName()]
        }
        render([aaData: unitConfigurations, recordsTotal: UnitConfiguration.getAllUnitConfigurationBySearchString(null).count(), recordsFiltered: unitConfigurationNameQuery.count()] as JSON)
    }

    def getOrganizationTypeByPreference(Object orgType, Integer langId){
        IcsrOrganizationType orgName = IcsrOrganizationType.findByOrg_name_idAndLangId(orgType?.org_name_id, langId)
        if (!orgName) {
            Integer defaultLangId = sqlGenerationService.getPVALanguageId('en')
            orgName = IcsrOrganizationType.findByOrg_name_idAndLangId(orgType?.org_name_id, defaultLangId)
        }
        return orgName?.name
    }

    def searchDataBasedOnParam(params) {
        Integer langId = sqlGenerationService.getPVALanguageId(userService.currentUser?.preference?.locale?.toString() ?: 'en')
        UnitConfiguration unitConfigurationInstance = UnitConfiguration.get(params.id)
        render([organizationType: [id: unitConfigurationInstance.organizationType.id, name: getOrganizationTypeByPreference(unitConfigurationInstance.organizationType, langId)], organizationCountry: ViewHelper.getOrganizationCountryNameByPreference(unitConfigurationInstance?.organizationCountry)] as JSON)
    }
    
    def getAllowedAttachments(String term, Integer page, Integer max) {
        Integer langId = sqlGenerationService.getPVALanguageId(userService.currentUser?.preference?.locale?.toString() ?: 'en')
        List allowedAttachments = []
        if (!max) {
            max = 30
        }
        if (!page) {
            page = 1
        }
        if (term) {
            term = term?.trim()
        }
        AllowedAttachment.withTransaction {
            def allowedAttachmentNamedQuery = AllowedAttachment.getAllAllowedAttachmentBySearchStringAndlangId(term, langId)
            allowedAttachments = allowedAttachmentNamedQuery.list([offset: Math.max(page - 1, 0) * max, max: max]).collect { [id: it.id, text: it.name] }
        }
        
        render([items : allowedAttachments, total_count: allowedAttachments.size()] as JSON)
    }
}
