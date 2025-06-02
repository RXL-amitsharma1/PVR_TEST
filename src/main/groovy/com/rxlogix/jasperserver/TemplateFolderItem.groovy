package com.rxlogix.jasperserver

import com.rxlogix.config.ReportTemplate
import grails.util.Holders
/**
 * Created by gologuzov on 30.01.17.
 * A workarond class for API milestone 1. Next milestone this class will be moved to model
 */
class TemplateFolderItem extends Folder {
    RepositoryTemplateService repositoryTemplateService = Holders.applicationContext.getBean("repositoryTemplateService")

    TemplateFolderItem(ReportTemplate template, Folder parent) {
        setName(String.valueOf(template.id))
        setLabel(template.name)
        setParent(parent)

        def dataFolder = repositoryTemplateService.createDataFolder(template)
        addSubFolder(dataFolder)
        def mainReport = repositoryTemplateService.createMainReport(template)
        dataFolder.addChild(mainReport)
        def reportUnit = repositoryTemplateService.createReportUnit(template, mainReport)
        addChild(reportUnit)
    }
}
