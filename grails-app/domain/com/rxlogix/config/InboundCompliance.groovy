package com.rxlogix.config

import com.rxlogix.Constants
import com.rxlogix.enums.DictionaryTypeEnum
import com.rxlogix.hibernate.EscapedILikeExpression
import com.rxlogix.util.MiscUtil
import com.rxlogix.util.ViewHelper
import grails.plugins.orm.auditable.CollectionSnapshotAudit
import org.hibernate.criterion.CriteriaSpecification
@CollectionSnapshotAudit
class InboundCompliance extends BaseInboundCompliance {
    static auditable =  [ignore:["executing"]]
    transient def userService

    GlobalDateRangeInbound globalDateRangeInbound

    List<QueryCompliance> queriesCompliance = []

    boolean isTemplate = false
    boolean isICInitialize = false
    Date lastRunDate

    static hasMany = [queriesCompliance: QueryCompliance]

    static mappedBy = [queriesCompliance: 'inboundCompliance']


    static constraints = {
        senderName(validator: {val, obj ->
            if (!val || !val.trim()) {
                return "com.rxlogix.config.InboundCompliance.senderName.nullable"
            }
            //Unique sender Name condition
            if (!obj.id || obj.isDirty("senderName") || obj.isDirty("owner")) {
                long count = InboundCompliance.createCriteria().count{
                    ilike('senderName', "${val}")
                    eq('isDeleted', false)
                    if (obj.id){
                        ne('id', obj.id)
                    }
                }
                if (count) {
                    return "com.rxlogix.config.InboundCompliance.senderName.unique"
                }
            }
        })
        globalDateRangeInbound nullable: true
        queriesCompliance(nullable: false, minSize: 1)
        isTemplate validator: { val, obj ->
            if (val && obj.userService?.springSecurityService?.loggedIn && !obj.userService?.currentUser?.getAuthorities()?.find { it.authority in [Constants.Roles.CONFIG_TMPLT_CREATOR, Constants.Roles.ADMIN, Constants.Roles.SUPER_ADMIN] }) {
                return "com.rxlogix.config.ReportConfiguration.create.template.forbidden"
            }
        }
        isICInitialize nullable: false
        lastRunDate(nullable: true)
    }

    static mapping = {
        table name: "INBOUND_COMPLIANCE"
        globalDateRangeInbound column: "GLOBAL_DATA_RANGE_INBOUND_ID"
        queriesCompliance joinTable: [name: "QUERY_COMPLIANCE", column: "ID", key: "INBOUND_COMPLIANCE_ID"], indexColumn: [name: "QUERY_COMPLIANCE_IDX"], cascade: "all-delete-orphan"
        tags joinTable: [name: "INBOUND_COMPLIANCE_TAGS", column: "TAG_ID", key: "INBOUND_COMPLIANCE_ID"], indexColumn: [name: "INBOUND_COM_TAG_IDX"]
        poiInputsParameterValues joinTable: [name: "INBOUND_POI_PARAMS", column: "PARAM_ID", key: "INBOUND_COMPLIANCE_ID"]
        isTemplate column: "IS_TEMPLATE"
        isICInitialize column: "IS_IC_INITIALIZE"
        lastRunDate column: "LAST_RUN_DATE"
    }

    static namedQueries = {

        countAllInboundComplianceBySearch { String search ->
            createAlias('owner', 'ownerAlias')
            projections {
                countDistinct("id")
            }
            getAllInboundComplianceBySearchStringQuery(search)
        }
        getAllInboundComplianceBySearchString {  String search ->
            createAlias('owner', 'ownerAlias')
            projections {
                distinct('id')
                property('senderName')
                property('description')
                property('qualityChecked')
                property('dateCreated')
                property('lastUpdated')
                property('ownerAlias.fullName', 'ownerFullName')
            }
            getAllInboundComplianceBySearchStringQuery(search)
        }
        getAllInboundComplianceBySearchStringQuery{ String search ->
            eq('isDeleted', false)
            if (search) {
                createAlias('tags', 'tag', CriteriaSpecification.LEFT_JOIN)
                or {
                    iLikeWithEscape('senderName', "%${EscapedILikeExpression.escapeString(search)}%")
                    iLikeWithEscape('description', "%${EscapedILikeExpression.escapeString(search)}%")
                    iLikeWithEscape('ownerAlias.fullName', "%${EscapedILikeExpression.escapeString(search)}%")
                    iLikeWithEscape('tag.name', "%${EscapedILikeExpression.escapeString(search)}%")
                }
            }
        }
    }

    transient String getProductsString() {
        return productSelection ? ViewHelper.getDictionaryValues(productSelection, DictionaryTypeEnum.PRODUCT) : ""
    }

    transient String getValidProductGroupSelection() {
        if (productGroupSelection && productGroupSelection != "[]") {
            return productGroupSelection
        }
        return null
    }

    transient String getStudiesString() {
        return studySelection ? ViewHelper.getDictionaryValues(studySelection, DictionaryTypeEnum.STUDY) : ""
    }

    transient String getNameWithDescription() {
        return "$senderName - $owner"
    }

    @Override
    public String toString() {
        super.toString()
    }

    Map appendAuditLogCustomProperties(Map newValues, Map oldValues) {
        if (newValues && oldValues && oldValues?.keySet()?.contains("globalDateRangeInbound")) {
            withNewSession {
                InboundCompliance inbound = InboundCompliance.read(id);
                if (oldValues?.keySet()?.contains("globalDateRangeInbound"))
                    oldValues.put("globalDateRangeInbound", inbound.globalDateRangeInbound?.toString())
            }
        }
        return [newValues: newValues, oldValues: oldValues]
    }

}