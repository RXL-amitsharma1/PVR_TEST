package com.rxlogix.config

import com.rxlogix.config.metadata.SourceColumnMaster
import com.rxlogix.enums.ReportFieldSelectionTypeEnum
import com.rxlogix.util.DateUtil
import com.rxlogix.util.DbUtil
import com.rxlogix.util.ViewHelper
import com.rxlogix.localization.Localization
import grails.plugins.orm.auditable.AuditEntityIdentifier
import grails.plugins.orm.auditable.CollectionSnapshotAudit

@CollectionSnapshotAudit
class CustomReportField {
    static auditable = true
    ReportField reportField
    ReportFieldGroup fieldGroup
    @AuditEntityIdentifier
    String customName
    String customDescription
    String defaultExpression
    boolean isDeleted = false

    boolean templateCLLSelectable
    boolean templateDTRowSelectable
    boolean templateDTColumnSelectable


    //Standard fields
    Date dateCreated
    Date lastUpdated
    String createdBy
    String modifiedBy

    static transients = ['isBlinded','isProtected']
    boolean isProtected = false
    boolean isBlinded = false

    static mapping = {
        table name: "CUSTOM_RPT_FIELD"
        reportField column: "RPT_FIELD_ID"
        fieldGroup column: "RPT_FIELD_GRPNAME"
        templateCLLSelectable column: "TEMPLT_CLL_SELECTABLE"
        templateDTRowSelectable column: "TEMPLT_DTROW_SELECTABLE"
        templateDTColumnSelectable column: "TEMPLT_DTCOL_SELECTABLE"
        customName column: "NAME"
        customDescription column: "DESCRIPTION"
        defaultExpression column: "DEFAULT_EXPRESSION" ,sqlType: DbUtil.longStringType
        isDeleted column: 'IS_DELETED'
    }

    static constraints = {
        reportField(nullable: false)
        fieldGroup(nullable: false)
        customDescription(nullable: true, maxSize: 2000)
        defaultExpression(nullable: false, maxSize: 32000)
        customName(maxSize: 255,blank: false, validator: { val, obj->
            Boolean exists = false
            exists = ((obj.id ? CustomReportField.countByCustomNameIlikeAndIsDeletedAndIdNotEqual(val, false, obj.id) :
                    CustomReportField.countByCustomNameIlikeAndIsDeleted(val,false)) || Localization.countByTextIlike(val))
            if (exists) return "com.rxlogix.config.CustomReportField.customName.unique"
        })

    }
    Map toMap() {
        [
                id: id,
                name               : customName,
                description        : customDescription,
                defaultExpression  : defaultExpression,
                reportFieldName    : reportField.name,
                reportFieldLabel   : ViewHelper.getMessage("app.reportField."+reportField.name),
                lastUpdated        : lastUpdated.format(DateUtil.DATEPICKER_UTC_FORMAT),
                modifiedBy         : modifiedBy
        ]
    }


    //---facade methods-------------------
    SourceColumnMaster getSourceColumn(Locale locale) {
        return reportField.getSourceColumn(locale)
    }

    String getName(){
        return reportField.name
    }

    public isClobField(Locale locale) {
        return reportField.isClobField(locale)
    }
    String getDictionaryType(){
        return reportField.dictionaryType
    }
    String getDictionaryLevel(){
        return reportField.dictionaryLevel
    }
    String isImportValidatable(){
        return reportField.isImportValidatable()
    }

    String getReportFieldName(){
        return customName
    }

    String getReportFieldDescription(){
        return customDescription
    }

    static List<CustomReportField> searchByType(ReportFieldSelectionTypeEnum reportFieldSelectionTypeEnum){
        switch(reportFieldSelectionTypeEnum) {
            case ReportFieldSelectionTypeEnum.CLL:
                return CustomReportField.findAllByIsDeletedAndTemplateCLLSelectable(false,true)
            case ReportFieldSelectionTypeEnum.DT_COLUMN:
                return CustomReportField.findAllByIsDeletedAndTemplateDTColumnSelectable(false,true)
            case ReportFieldSelectionTypeEnum.DT_ROW:
                return CustomReportField.findAllByIsDeletedAndTemplateDTRowSelectable(false,true)
        }
        return []
    }

    public String toString() {
        return customName
    }

    public boolean checkEquals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false
        CustomReportField that = (CustomReportField) o

        if (isDeleted != that.isDeleted) return false
        if (templateCLLSelectable != that.templateCLLSelectable) return false
        if (templateDTColumnSelectable != that.templateDTColumnSelectable) return false
        if (templateDTRowSelectable != that.templateDTRowSelectable) return false
        if (fieldGroup != that.fieldGroup) return false
        if (customDescription != that.customDescription) return false
        if (defaultExpression != that.defaultExpression) return false
        if (reportField != that.reportField) return false

        return true
    }
}
