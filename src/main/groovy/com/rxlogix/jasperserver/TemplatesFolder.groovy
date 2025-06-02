package com.rxlogix.jasperserver

import com.rxlogix.LibraryFilter
import com.rxlogix.UserService
import com.rxlogix.config.ReportTemplate
import grails.util.Holders

/**
 * Created by gologuzov on 30.01.17.
 *  * A workarond class for API milestone 1. Next milestone this class will be moved to model
 */
class TemplatesFolder extends Folder {

    TemplatesFolder() {
        setName("templates")
        setLabel("Templates")
    }

    @Override
    Set<Folder> getSubFolders() {
        super.subFolders?.clear()
        UserService userService = Holders.applicationContext.getBean("userService")
        List<Long> idsForUser = ReportTemplate.fetchAllIdsBySearchString(new LibraryFilter(userService.getUser())).list().collect {
            it.first()
        }
        List<ReportTemplate> reportTemplateList = []
        idsForUser?.collate(999)?.each {
            reportTemplateList += ReportTemplate.findAllByIdInList(it)
        }
        reportTemplateList.each { reportTemplate ->
            addSubFolder(new TemplateFolderItem(reportTemplate, this))
        }
        return super.subFolders
    }

    @Override
    public Folder findSubFolder(String name) {
        return super.subFolders?.find {it.name == name}
    }
}
