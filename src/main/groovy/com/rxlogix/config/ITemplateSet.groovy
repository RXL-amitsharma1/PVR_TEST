package com.rxlogix.config

/**
 * Created by gologuzov on 26.05.17.
 */
interface ITemplateSet {
    Collection<ReportTemplate> getNestedTemplates()
    boolean getLinkSectionsByGrouping()
}