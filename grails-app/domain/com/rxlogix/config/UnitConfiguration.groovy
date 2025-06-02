package com.rxlogix.config

import com.rxlogix.OrderByUtil
import com.rxlogix.enums.TitleEnum
import com.rxlogix.enums.UnitTypeEnum
import com.rxlogix.hibernate.EscapedILikeExpression
import com.rxlogix.util.MiscUtil
import com.rxlogix.util.ViewHelper
import grails.plugins.orm.auditable.AuditEntityIdentifier
import grails.plugins.orm.auditable.CollectionSnapshotAudit
import org.hibernate.sql.JoinType
import com.rxlogix.user.User

@CollectionSnapshotAudit
class UnitConfiguration {
    static auditable =  true
    @AuditEntityIdentifier
    String unitName
    UnitTypeEnum unitType
    IcsrOrganizationType organizationType
    String organizationCountry
    String preferredLanguage
    String unitRegisteredId
    UnitConfiguration registeredWith
    String address1
    String address2
    String city
    String state
    String postalCode
    String postalCodeExt
    String phone
    String email
    boolean unitRetired
    TitleEnum title
    String firstName
    String middleName
    String lastName
    String department
    String fax
    String xsltName
    String unitOrganizationName
    String organizationName
    Set<Long> allowedAttachments = []
    EmailTemplate emailTemplate
    String unitAttachmentRegId
    String holderId
    String xmlVersion
    String xmlEncoding
    String xmlDoctype
    //Standard fields
    Date dateCreated = new Date()
    Date lastUpdated = new Date()
    String createdBy
    String modifiedBy
    String preferredTimeZone

    static mapping = {
        table name: "UNIT_CONFIGURATION"
        unitName column: "UNIT_NAME"
        unitType column: "UNIT_TYPE"
        organizationType column: "ORG_TYPE_ID"
        organizationCountry column: "ORG_COUNTRY"
        preferredLanguage column: "PREF_LANGUAGE"
        unitRegisteredId column: "UNIT_REGISTERED_ID"
        unitAttachmentRegId column: "UNIT_ATTACHMENT_REG_ID"
        registeredWith column: "REGISTERED_WITH"
        address1 column: "ADDRESS1"
        address2 column: "ADDRESS2"
        city column: "CITY"
        state column: "STATE"
        postalCode column: "POSTAL_CODE"
        postalCodeExt column: "POSTAL_CODE_EXT"
        phone column: "PHONE"
        email column: "EMAIL"
        unitRetired column: "UNIT_RETIRED"
        title column : "TITLE"
        firstName column : "FIRST_NAME"
        middleName column : "MIDDLE_NAME"
        lastName column : "LAST_NAME"
        department column: "DEPARTMENT"
        fax column: "FAX"
        xsltName column: "XSLT_NAME"
        emailTemplate column: "EMAIL_TEMPLATE_ID"
        holderId column:"HOLDER_ID"
        unitOrganizationName column: "UNIT_ORGANIZATION_NAME"
        organizationName column: "ORGANIZATION_NAME"
        allowedAttachments joinTable: [name: "ALLOWED_ATTACHMENTS", column: "ATTACHMENT_ID", key: "UNITI_CONFIG_ID"]
        preferredTimeZone column: "PREFERRED_TIME_ZONE"
        xmlVersion column: 'XML_VERSION'
        xmlEncoding column: 'XML_ENCODING'
        xmlDoctype column: 'XML_DOCTYPE'
    }

    static constraints = {
        unitName nullable: false, validator: { val, obj ->
            // Name is unique to user
            if (!obj.id || obj.isDirty("unitName")) {
                long count = UnitConfiguration.createCriteria().count {
                    eq('unitName', "${val}", [ignoreCase: true])
                    if (obj.id) {
                        ne('id', obj.id)
                    }
                }
                if (count) {
                    return "com.rxlogix.config.UnitConfiguration.unitName.unique"
                }
            }
        }
        unitType(nullable: false)
        emailTemplate(nullable: true)
        holderId(nullable: true, maxSize: 200)
        organizationType(nullable: false)
        organizationCountry(nullable: true)
        preferredLanguage(nullable: false)
        unitRegisteredId(nullable: false)
        unitAttachmentRegId nullable: true
        registeredWith(nullable: true, validator: { val, obj ->
            if(val && val.id == obj.id){
                return "com.rxlogix.config.UnitConfiguration.registeredWith.invalid.self.reference"
            }
            return true
        })
        address1(nullable: true, maxSize: 100)
        address2(nullable: true, maxSize: 100)
        city(nullable: true, maxSize: 35)
        state(nullable: true)
        postalCode(nullable: true)
        postalCodeExt(nullable: true)
        phone(nullable: true)
        email(nullable: true)
        unitRetired(nullable: false)
        title(nullable: true)
        firstName(nullable: true)
        middleName(nullable: true)
        lastName(nullable: true)
        department(nullable: true)
        fax(nullable: true)
        xsltName(nullable: true, validator: { val, obj ->
            if ((!obj.id || obj.isDirty("xsltName") || obj.isDirty("unitType")) && !val && obj.unitType != UnitTypeEnum.SENDER && obj.unitType != null) {
                return "com.rxlogix.config.UnitConfiguration.xsltName.nullable"
            }
            return true
        })
        unitOrganizationName(nullable: true, maxSize: 100)
        organizationName(nullable: true)
        allowedAttachments(nullable: true)
        preferredTimeZone(nullable: true)
        xmlVersion(nullable: true)
        xmlEncoding(nullable: true)
        xmlDoctype(nullable: true)

    }

    static namedQueries = {

        getAllUnitConfigurationBySearchString { String search, String sortBy = null, String sortDirection = "asc" ->
            createAlias("organizationType", "organizationType")
            if (search) {
                or {
                    iLikeWithEscape('unitName', "%${EscapedILikeExpression.escapeString(search)}%")
                    iLikeWithEscape('unitRegisteredId', "%${EscapedILikeExpression.escapeString(search)}%")
                    iLikeWithEscape('organizationType.name', "%${EscapedILikeExpression.escapeString(search)}%")
                    UnitTypeEnum.searchBy(search)?.each {
                        eq('unitType', it)
                    }
                    User.findAllByFullNameIlike("%${search}%").each {
                        eq('createdBy', it.username)
                    }
                }
            }
            if (sortBy) {
                if (sortBy == 'unitType') {
                    order(OrderByUtil.mapOrderIgnoreCase(sortBy, [
                            (UnitTypeEnum.SENDER.value()) : (ViewHelper.getMessage(UnitTypeEnum.SENDER.i18nKey)),
                            (UnitTypeEnum.RECIPIENT.value()) : (ViewHelper.getMessage(UnitTypeEnum.RECIPIENT.i18nKey)),
                            (UnitTypeEnum.BOTH.value()) : (ViewHelper.getMessage(UnitTypeEnum.BOTH.i18nKey))
                    ], sortDirection))
                } else if (sortBy == 'organizationType') {
                    order("organizationType.name", "${sortDirection}")
                } else if (sortBy == 'owner.fullName') {
                    order("createdBy", "${sortDirection}")
                } else {
                    order("${sortBy}", "${sortDirection}")
                }
            }
        }

    }

    String toString() {
        return unitName
    }

}
