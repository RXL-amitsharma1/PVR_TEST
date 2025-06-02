package com.rxlogix

import com.rxlogix.config.IcsrOrganizationType
import com.rxlogix.config.UnitConfiguration
import com.rxlogix.enums.UnitTypeEnum
import com.rxlogix.util.DateUtil
import grails.converters.JSON
import grails.gorm.transactions.ReadOnly
import org.jfree.util.UnitType


class UnitConfigurationService {

    def getNotRetiredUnitConfigurationList(){
        return UnitConfiguration.findAllByUnitRetired(false)
    }

    def getAllUnitRegWithBasedOnParam(organizationTypeId){
        return UnitConfiguration.findAllByOrganizationType(IcsrOrganizationType.get(organizationTypeId))
    }

    @ReadOnly
    List getAllRecipientsList() {
        List<UnitConfiguration> unitConfigurationList = UnitConfiguration.findAllByUnitTypeInList([UnitTypeEnum.RECIPIENT, UnitTypeEnum.BOTH, UnitTypeEnum.SENDER])
        return unitConfigurationList.size() ? unitConfigurationList.collect() { toMap(it) } : null
    }

    private Map toMap(UnitConfiguration unitConfiguration) {
        return [organizationId     : unitConfiguration.id,
                organizationName   : unitConfiguration.unitName,
                country            : unitConfiguration.organizationCountry,
                unitType           : unitConfiguration.unitType?.name(),
                unitRetired        : unitConfiguration.unitRetired,
                dateCreated        : unitConfiguration.dateCreated.format(DateUtil.DATEPICKER_UTC_FORMAT),
                lastUpdated        : unitConfiguration.lastUpdated.format(DateUtil.DATEPICKER_UTC_FORMAT),
                createdBy          : unitConfiguration.createdBy,
                modifiedBy         : unitConfiguration.modifiedBy,
                organizationType   : unitConfiguration.organizationType?.org_name_id,
                unitRegisteredId   : unitConfiguration.unitRegisteredId,
                registeredWith     : unitConfiguration.registeredWith?.unitName,
                address1           : unitConfiguration.address1,
                address2           : unitConfiguration.address2,
                city               : unitConfiguration.city,
                state              : unitConfiguration.state,
                postalCode         : unitConfiguration.postalCode,
                postalCodeExt      : unitConfiguration.postalCodeExt,
                phone              : unitConfiguration.phone,
                email              : unitConfiguration.email,
                title              : unitConfiguration.title?.name(),
                firstName          : unitConfiguration.firstName,
                middleName         : unitConfiguration.middleName,
                lastName           : unitConfiguration.lastName,
                department         : unitConfiguration.department,
                fax                : unitConfiguration.fax,
                xsltName           : unitConfiguration.xsltName,
                companyName        : unitConfiguration.organizationName,
                attachmentClassification : unitConfiguration.allowedAttachments,
                preferredTimeZone  : unitConfiguration.preferredTimeZone,
                holderId           : unitConfiguration.holderId,
                preferredLanguage  : unitConfiguration.preferredLanguage,
                unitOrganizationName : unitConfiguration.unitOrganizationName
        ]
    }

}
