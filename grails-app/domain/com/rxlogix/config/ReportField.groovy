package com.rxlogix.config

import com.rxlogix.NonCacheSelectableList
import com.rxlogix.SelectableList
import com.rxlogix.config.metadata.SourceColumnMaster
import com.rxlogix.enums.DictionaryTypeEnum
import com.rxlogix.hibernate.EscapedILikeExpression
import com.rxlogix.util.DbUtil
import com.rxlogix.util.ViewHelper
import grails.plugins.orm.auditable.AuditEntityIdentifier
import grails.plugins.orm.auditable.CollectionSnapshotAudit
import com.rxlogix.embaseOperators.embaseCombined
import com.rxlogix.embaseOperators.embasePhrase
import com.rxlogix.embaseOperators.embaseExact

@CollectionSnapshotAudit
class ReportField implements Serializable{
    static auditable = true


    public static final String CASE_NUM_ARGUS_NAME = "CASE_NUM"
    private static final int CASE_NUM_MIN_COLUMNS = 7
    private static final int DEFAULT_MIN_COLUMNS = 0
    public static final String AGENCIES_NAME_KEY = "reportsAgencyId"

    def messageSource

    @AuditEntityIdentifier
    String name
    String description
    String transform
    ReportFieldGroup fieldGroup
    String sourceColumnId
    Class dataType
    boolean isText = false      //denotes whether we show this element as select2 in the UI or not
    Class listDomainClass
    String lmSQL
    boolean querySelectable
    boolean templateCLLSelectable
    boolean templateDTRowSelectable
    boolean templateDTColumnSelectable
    DictionaryTypeEnum dictionaryType
    Integer dictionaryLevel

    boolean isDeleted = false
    String dateFormatCode
    boolean isAutocomplete
    String preQueryProcedure
    String postQueryProcedure
    String preReportProcedure
    Integer fixedWidth
    Integer widthProportionIndex
    boolean override = true
    Integer sourceId = 0
    boolean isCreatedByUser = false

    static transients = ['displayName', 'isBlinded', 'isProtected']
    boolean isBlinded = false
    boolean isProtected = false
    boolean isUrlField

    static mapping = {
        cache: "read-only"
        version: false
        /**
         * Because we are heavily dependent on fieldGroup and sourceColumn eagerly fetch them
         * when loading this object
         */
        fieldGroup lazy: false, cascade: 'none'

        table name: "RPT_FIELD"

        name column: "NAME"
        description column: "DESCRIPTION"
        transform column: "TRANSFORM"
        fieldGroup column: "RPT_FIELD_GRPNAME"
        sourceColumnId column: "SOURCE_COLUMN_MASTER_ID"
        dataType column: "DATA_TYPE"
        isText column: "IS_TEXT"
        listDomainClass column: "LIST_DOMAIN_CLASS"
        lmSQL column: "LMSQL", sqlType: DbUtil.longStringType
        querySelectable column: "QUERY_SELECTABLE"
        templateCLLSelectable column: "TEMPLT_CLL_SELECTABLE"
        templateDTRowSelectable column: "TEMPLT_DTROW_SELECTABLE"
        templateDTColumnSelectable column: "TEMPLT_DTCOL_SELECTABLE"
        dictionaryType column: "DIC_TYPE"
        dictionaryLevel column: "DIC_LEVEL"
        isDeleted column: "IS_DELETED"
        dateFormatCode column: "DATE_FORMAT"
        isAutocomplete column: "ISAUTOCOMPLETE"
        preQueryProcedure column: "PRE_QUERY_PROCEDURE"
        postQueryProcedure column: "POST_QUERY_PROCEDURE"
        preReportProcedure column: "PRE_REPORT_PROCEDURE"
        fixedWidth column: "FIXED_WIDTH"
        widthProportionIndex column: "WIDTH_PROPORTION_INDEX"
        override column: "OVERRIDE"
        sourceId column: "SOURCE_ID"
        isCreatedByUser column: "IS_CREATED_BY_USER"
        isUrlField column: "IS_URL_FIELD"
    }

    static constraints = {
        name(maxSize: 128, blank: false)
        description(nullable: true, maxSize: 255)
        transform(nullable: true)
        listDomainClass(nullable: true)
        lmSQL(maxSize: 32 * 1024, nullable: true)
        dictionaryType(nullable: true)
        dictionaryLevel(nullable: true)
        preQueryProcedure(nullable: true)
        postQueryProcedure(nullable: true)
        preReportProcedure(nullable: true)
        sourceColumnId nullable: true
        dateFormatCode nullable: true, validator: { val, obj ->
            if (!val && obj.isDate()) {
                return false
            }

        }
        dataType nullable: true
        fixedWidth(nullable: true)
        widthProportionIndex(nullable: true)
        isUrlField(nullable: true)
    }

    static namedQueries = {
        fetchAllByFieldGroupHavingQuerySelectable { ReportFieldGroup reportFieldGroup ->
            eq('fieldGroup', reportFieldGroup)
            'fieldGroup' {
                eq('isDeleted', false)
            }
            eq('querySelectable', true)
            eq('isDeleted', false)
        }

        fetchAllByFieldGroupHavingTemplateCLLSelectable { ReportFieldGroup reportFieldGroup ->
            eq('fieldGroup', reportFieldGroup)
            'fieldGroup' {
                eq('isDeleted', false)
            }
            eq('templateCLLSelectable', true)
            eq('isDeleted', false)
        }

        fetchAllByFieldGroupHavingTemplateDTRowSelectable { ReportFieldGroup reportFieldGroup ->
            eq('fieldGroup', reportFieldGroup)
            'fieldGroup' {
                eq('isDeleted', false)
            }
            eq('templateDTRowSelectable', true)
            eq('isDeleted', false)
        }

        fetchAllByFieldGroupAndTemplateDTColumnSelectable { ReportFieldGroup reportFieldGroup ->
            eq('fieldGroup', reportFieldGroup)
            'fieldGroup' {
                eq('isDeleted', false)
            }
            eq('templateDTColumnSelectable', true)
            eq('isDeleted', false)
        }

        fetchAllByFieldGroupAndTemplateCLLSelectableOrTemplateDTRowSelectableOrTemplateDTColumnSelectable { ReportFieldGroup reportFieldGroup ->
            eq('fieldGroup', reportFieldGroup)
            'fieldGroup' {
                eq('isDeleted', false)
            }
            eq('isDeleted', false)
            or {
                eq('templateDTColumnSelectable', true)
                eq('templateCLLSelectable', true)
                eq('templateDTRowSelectable', true)
            }
        }

        fetchAllReportFieldBySearchString { String search ->
            if (search) {
                or {
                    iLikeWithEscape('name', "%${EscapedILikeExpression.escapeString(search)}%")
                    iLikeWithEscape('description', "%${EscapedILikeExpression.escapeString(search)}%")
                }
            }
            eq('isDeleted', false)
        }
    }

    public boolean isString() {
        return String.class.equals(dataType)
    }

    public boolean isNumber() {
        return Number.class.isAssignableFrom(dataType)
    }

    public boolean isDate() {
        return Date.class.equals(dataType)
    }

    public boolean isEmbase() {
        return embaseCombined.class.equals(dataType) || embasePhrase.class.equals(dataType) || embaseExact.class.equals(dataType)
    }

    public boolean hasSelectableList() {
        if (listDomainClass) {
            return SelectableList.isAssignableFrom(listDomainClass)
        }
        return false
    }

    public isClobField(Locale locale) {
        // TODO need to fix dependency on locale
        return SourceColumnMaster.countByReportItemAndColumnTypeAndLangInList(sourceColumnId, 'C', [locale.toString(), "*"]) ? true : false
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        ReportField that = (ReportField) o

        if (name != that.name) return false
        if (isDeleted != that.isDeleted) return false
        if (isText != that.isText) return false
        if (querySelectable != that.querySelectable) return false
        if (templateCLLSelectable != that.templateCLLSelectable) return false
        if (templateDTColumnSelectable != that.templateDTColumnSelectable) return false
        if (templateDTRowSelectable != that.templateDTRowSelectable) return false
        if (sourceColumnId != that.sourceColumnId) return false
        if (dataType != that.dataType) return false
        if (dateFormatCode != that.dateFormatCode) return false
        if (description != that.description) return false
        if (dictionaryLevel != that.dictionaryLevel) return false
        if (dictionaryType != that.dictionaryType) return false
        if (fieldGroup != that.fieldGroup) return false
        if (listDomainClass != that.listDomainClass) return false
        if (lmSQL != that.lmSQL) return false
        if (transform != that.transform) return false
        if (isAutocomplete != that.isAutocomplete) return false
        if (preQueryProcedure != that.preQueryProcedure) return false
        if (postQueryProcedure != that.postQueryProcedure) return false
        if (preReportProcedure != that.preReportProcedure) return false
        if (fixedWidth != that.fixedWidth) return false
        if (widthProportionIndex != that.widthProportionIndex) return false
        if (override != that.override) return false
        if (sourceId != that.sourceId) return false
        if (isCreatedByUser != that.isCreatedByUser) return false
        if (isUrlField != that.isUrlField) return false

        return true
    }

    int hashCode() {
        int result
        result = (name != null ? name.hashCode() : 0)
        result = 31 * result + (isDeleted ? 1 : 0)
        result = 31 * result + (description != null ? description.hashCode() : 0)
        result = 31 * result + (transform != null ? transform.hashCode() : 0)
        result = 31 * result + (fieldGroup != null ? fieldGroup.hashCode() : 0)
        result = 31 * result + (sourceColumnId != null ? sourceColumnId.hashCode() : 0)
        result = 31 * result + (dataType != null ? dataType.hashCode() : 0)
        result = 31 * result + (isText ? 1 : 0)
        result = 31 * result + (listDomainClass != null ? listDomainClass.hashCode() : 0)
        result = 31 * result + (lmSQL != null ? lmSQL.hashCode() : 0)
        result = 31 * result + (querySelectable ? 1 : 0)
        result = 31 * result + (templateCLLSelectable ? 1 : 0)
        result = 31 * result + (templateDTRowSelectable ? 1 : 0)
        result = 31 * result + (templateDTColumnSelectable ? 1 : 0)
        result = 31 * result + (dictionaryType != null ? dictionaryType.hashCode() : 0)
        result = 31 * result + (dictionaryLevel != null ? dictionaryLevel.hashCode() : 0)
        result = 31 * result + (isAutocomplete ? 1 : 0)
        result = 31 * result + (preQueryProcedure != null ? preQueryProcedure.hashCode() : 0)
        result = 31 * result + (postQueryProcedure != null ? postQueryProcedure.hashCode() : 0)
        result = 31 * result + (preReportProcedure != null ? preReportProcedure.hashCode() : 0)
        result = 31 * result + (dateFormatCode != null ? dateFormatCode.hashCode() : 0)
        result = 31 * result + (fixedWidth != null ? fixedWidth.hashCode() : 0)
        result = 31 * result + (widthProportionIndex != null ? widthProportionIndex.hashCode() : 0)
        result = 31 * result + (override ? 1 : 0)
        result = 31 * result + (sourceId != null ? sourceId.hashCode() : 0)
        result = 31 * result + (isCreatedByUser ? 1 : 0)
        result = 31 * result + (isUrlField ? 1 : 0)
        return result
    }

    String getDateFormat(String lang) {
        if (!dateFormatCode) {
            return "dd-MM-yyyy"
        }
        String value = ""
        try {
            value = messageSource.getMessage(dateFormatCode, [].toArray(), new Locale(lang))
        } catch (NoSuchMessageException) {
            log.error("No localization date code value found for ${dateFormatCode} with field name as ${name}")
            value = dateFormatCode
        }
        return value
    }

    String getLmSql(String lang) {
        if (!lang) {
            return ""
        }
        String value = ""
        try {
            value = messageSource.getMessage(lmSQL, [].toArray(), new Locale(lang))
        } catch (NoSuchMessageException) {
            log.error("No localization lmsql code value found for ${lmSQL} with field name as ${name}")
            value = lmSQL
        }
        return value
    }

    SourceColumnMaster getSourceColumn(Locale locale) {
        return SourceColumnMaster.findByReportItemAndLangInList(sourceColumnId, [locale.toString(), "*"])
    }


    String getDisplayName() {
        ViewHelper.getMessage("app.reportField.${name}")
    }

    String getDisplayName(Locale locale) {
        ViewHelper.getMessage("app.reportField.${name}",null ,null ,locale)
    }

    transient boolean isImportValidatable(List<String> caseNumberFieldNamesList = null) {
        List<String> caseNumberFieldNames = caseNumberFieldNamesList ?: SourceProfile.fetchAllCaseNumberFieldNames()
        if (listDomainClass || (dictionaryType && dictionaryLevel) || name in caseNumberFieldNames) {
            return true
        }
        return false
    }

    String getReportFieldName(){
        return ViewHelper.getMessage("app.reportField.${name}")
    }

    String getReportFieldDescription(){
        return ViewHelper.getMessage("app.reportField.${name}.label.description")
    }

    boolean isNonCacheSelectable() {
        return (listDomainClass?.name == NonCacheSelectableList.name && isAutocomplete)
    }

    public String toString() {
        return ViewHelper.getMessage("app.reportField.${name}")
    }

    public static void copyObj(ReportField sourceObj,ReportField targetObj) {
        targetObj.with {
            description = sourceObj.description
            transform =sourceObj.transform
            fieldGroup = sourceObj.fieldGroup
            sourceColumnId = sourceObj.sourceColumnId
            dataType = sourceObj.dataType
            isText = sourceObj.isText
            listDomainClass = sourceObj.listDomainClass
            lmSQL = sourceObj.lmSQL
            querySelectable = sourceObj.querySelectable
            templateCLLSelectable = sourceObj.templateCLLSelectable
            templateDTRowSelectable = sourceObj.templateDTRowSelectable
            templateDTColumnSelectable = sourceObj.templateDTColumnSelectable
            isDeleted = sourceObj.isDeleted
            dateFormatCode = sourceObj.dateFormatCode
            dictionaryType = sourceObj.dictionaryType
            dictionaryLevel = sourceObj.dictionaryLevel
            isAutocomplete = sourceObj.isAutocomplete
            preQueryProcedure = sourceObj.preQueryProcedure
            postQueryProcedure = sourceObj.postQueryProcedure
            preReportProcedure = sourceObj.preQueryProcedure
            fixedWidth = sourceObj.fixedWidth
            widthProportionIndex = sourceObj.widthProportionIndex
            sourceId = sourceObj.sourceId
            isUrlField = sourceObj.isUrlField
        }
    }
}
