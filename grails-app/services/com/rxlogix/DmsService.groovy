package com.rxlogix

import com.rxlogix.cmis.AdapterFactory
import com.rxlogix.cmis.AdapterInterface
import com.rxlogix.config.ApplicationSettings
import com.rxlogix.config.BaseCaseSeries
import com.rxlogix.config.DmsConfiguration
import com.rxlogix.config.ExecutedIcsrProfileConfiguration
import com.rxlogix.config.ExecutedIcsrReportConfiguration
import com.rxlogix.config.ExecutedPeriodicReportConfiguration
import com.rxlogix.config.SharedWith
import com.rxlogix.enums.AuditLogCategoryEnum
import com.rxlogix.enums.ReportExecutionStatusEnum
import com.rxlogix.util.AuditLogConfigUtil
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.core.GrailsApplication

class DmsService {

    def dynamicReportService
    AdapterInterface adapter
    def emailService
    GrailsApplication grailsApplication

    void upload(File reportFile, String subfolder, String name, String description, String tag, String sensitivity, String author,
               Object object) {
        if (!adapter) {
            String dmsSetting = ApplicationSettings.first()?.dmsIntegration
            if (dmsSetting) {
                def settingsJSON = JSON.parse(dmsSetting)
                adapter = AdapterFactory.getAdapter(settingsJSON);
            } else {
                throw new Exception("DMS is not enabled but configured in the report for file name as ${name} - ${author}. Please update report configuration again.")
            }
        }
            adapter.load(reportFile, subfolder, name, description, tag, sensitivity, author, object)
        try {
            String message = "Uploaded ${name}${object?.numOfExecutions ? ('-' + object?.numOfExecutions) : ''} ${object?.dmsConfiguration?.format?.name()} on ${adapter.settings?.rootFolder ?: ''}${subfolder ?: ''}"
            AuditLogConfigUtil.logChanges(object, [state: message], [state: ""], Constants.AUDIT_LOG_UPDATE, "DMS File Upload")
        } catch (Exception ex) {
            log.error("Failed to audit log event" + ex)
        }

    }

    def clear() {
        adapter = null
    }

    def uploadReport(def configuration, String format = null, List sections = null) {
            if ((configuration instanceof ExecutedIcsrProfileConfiguration) || (configuration instanceof ExecutedIcsrReportConfiguration) || (configuration instanceof BaseCaseSeries) || (configuration.dmsConfiguration == null || configuration.dmsConfiguration?.isDeleted) || (configuration.dmsConfiguration.noDocumentOnNoData && !configuration.containsData)) return
            def reportParams = [
                    pageOrientation     : configuration.dmsConfiguration.pageOrientation,
                    paperSize           : configuration.dmsConfiguration.paperSize,
                    sensitivityLabel    : configuration.dmsConfiguration.sensitivityLabel,
                    showPageNumbering   : configuration.dmsConfiguration.showPageNumbering,
                    excludeCriteriaSheet: configuration.dmsConfiguration.excludeCriteriaSheet,
                    excludeAppendix     : configuration.dmsConfiguration.excludeAppendix,
                    excludeComments     : configuration.dmsConfiguration.excludeComments,
                    showCompanyLogo     : configuration.dmsConfiguration.showCompanyLogo,
                    excludeLegend       : configuration.dmsConfiguration.excludeLegend,
                    outputFormat        : format ?: configuration.dmsConfiguration.format.name(),
                    advancedOptions     : "1"
            ]

            if(sections && sections.size() > 0 && !sections.find{!it}){
                reportParams.sectionsToExport = sections
            }
            File reportFile
            if (configuration instanceof ExecutedPeriodicReportConfiguration && configuration.status == ReportExecutionStatusEnum.GENERATED_CASES) {
                reportFile = dynamicReportService.createCaseListReport(configuration.caseSeries, reportParams)
            } else {
                reportFile = dynamicReportService.createMultiTemplateReport(configuration, reportParams)
            }
        upload(reportFile, configuration.dmsConfiguration.folder,
                configuration.dmsConfiguration.name ?: configuration.reportName,
                configuration.dmsConfiguration.description ?: configuration.description,
                configuration.dmsConfiguration.tag ?: configuration.tags?.name?.join((",")),
                ViewHelper.getMessage(configuration.dmsConfiguration.sensitivityLabel.getI18nKey()),
                configuration.owner.fullName, configuration)

            if (configuration.executedDeliveryOption?.sharedWith) {
                List<SharedWith> sharedWith = configuration.executedDeliveryOption.sharedWith
                String url = grailsApplication.config.grails.appBaseURL + "/report/showFirstSection/" + configuration.id
                emailService.sendEmail(
                        sharedWith.collect { it.email } as List<String>,
                        ViewHelper.getMessage('app.dms.file.transmitted.message', [url, configuration.reportName,configuration.dmsConfiguration.name ?: configuration.reportName].toArray()),
                        true,
                        ViewHelper.getMessage('app.dms.file.transmitted.title'),
                        null
                )
            }
        }


    List<String> getFolderList(String folder,Object object) {
        if (!adapter) {
            def settings = JSON.parse(ApplicationSettings.first().dmsIntegration)
            adapter = AdapterFactory.getAdapter(settings);
        }
        adapter.getFolderList(folder,object)
    }
}
