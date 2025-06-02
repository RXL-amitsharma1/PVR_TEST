package com.rxlogix.config

import com.rxlogix.enums.WidgetTypeEnum
import grails.plugins.orm.auditable.CollectionSnapshotAudit
import grails.plugins.orm.auditable.SectionModuleAudit

@CollectionSnapshotAudit
@SectionModuleAudit(parentClassName = ['dashboard'])
class ReportWidget {
    static auditable = [ignoreEvents:["onDelete"]]
    WidgetTypeEnum widgetType
    ReportConfiguration reportConfiguration
    Integer x
    Integer y
    Integer width
    Integer height
    Boolean autoPosition
    String settings
    Integer sectionNumber

    static mapping = {
        table "RWIDGET"
    }

    static constraints = {
        widgetType(nullable: false)
        reportConfiguration(nullable: true)
        x(nullable: false, min:0)
        y(nullable: false, min: 0)
        width(nullable: false, min: 0)
        height(nullable: false, min: 0)
        autoPosition(nullable: true)
        sectionNumber(nullable: true)
        settings(nullable: true, maxSize: 4000)
    }

    public String toString() {
        String widgetName
        if(reportConfiguration)
            widgetName = widgetType.name() + ' (' + reportConfiguration.reportName + ')'
        else
            widgetName = widgetType.name()
        return widgetName
    }
}
