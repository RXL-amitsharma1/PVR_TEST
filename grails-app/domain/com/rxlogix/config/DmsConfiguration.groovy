package com.rxlogix.config

import com.rxlogix.enums.PageSizeEnum
import com.rxlogix.enums.ReportFormatEnum
import com.rxlogix.enums.SensitivityLabelEnum
import com.rxlogix.util.DbUtil
import grails.converters.JSON
import grails.plugins.orm.auditable.CollectionSnapshotAudit
import grails.plugins.orm.auditable.SectionModuleAudit
import net.sf.dynamicreports.report.constant.PageOrientation
@CollectionSnapshotAudit
@SectionModuleAudit(parentClassName = ['configuration','periodicReportConfiguration'])
class DmsConfiguration {
    static auditable = true
    String folder
    String name
    String description
    String tag
    Boolean isDeleted = false

    ReportFormatEnum format
    Boolean noDocumentOnNoData = false
    PageOrientation pageOrientation
    Boolean showPageNumbering
    Boolean excludeCriteriaSheet
    Boolean excludeAppendix
    Boolean excludeComments
    Boolean excludeLegend
    Boolean showCompanyLogo
    PageSizeEnum paperSize
    SensitivityLabelEnum sensitivityLabel

    static constraints = {
        folder(nullable: true, maxSize: 200)
        name(nullable: true, maxSize: 200)
        description(nullable: true, maxSize: 1000)
        format(nullable: false, maxSize: 200)
        tag(nullable: true, maxSize: 200)
        noDocumentOnNoData nullable:true
        pageOrientation nullable:true
        showPageNumbering nullable:true
        excludeCriteriaSheet nullable:true
        excludeAppendix nullable:true
        excludeComments nullable:true
        excludeLegend nullable: true
        showCompanyLogo nullable:true
        paperSize nullable:true
        sensitivityLabel nullable:true
    }

    static mapping = {
        table name: "DMS_CONFIGURATION"

        folder column: "FOLDER"
        name column: "NAME"
        description column: "DESCRIPTION"
        tag column: "TAG"
        isDeleted column: "IS_DELETED"
        format column: "FORMAT"
        noDocumentOnNoData column: "NO_DOCUMENT_ON_NO_DATA"
        pageOrientation column: "PAGE_ORIENTATION"
        showPageNumbering column: "SHOW_PAGE_NUMBERING"
        excludeCriteriaSheet column: "EXCLUDE_CRITERIA_SHEET"
        excludeAppendix column: "EXCLUDE_APPENDIX"
        excludeComments column: "EXCLUDE_COMMENTS"
        showCompanyLogo column: "SHOW_COMPANY_LOGO"
        excludeLegend column: "EXCLUDE_LEGEND"
        paperSize column: "PAPER_SIZE"
        sensitivityLabel column: "SENSITIVITY_LABEL"
    }

    @Override
    String toString(){
        return "DMS for file [ROOT]${folder ?: ''}/${name ?: ''}"
    }
}
