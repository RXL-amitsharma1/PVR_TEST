package com.rxlogix.public_api

import com.rxlogix.config.ReportTemplate
import com.rxlogix.user.User
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

@Secured(['permitAll'])
class PublicTemplateRestController {

    def templateService

    /**
     * Method to fetch the template based on the passed user.
     * @return : List<Map[id,name,description]>
     * @input : username,search,max,offset
     */
    def getTemplatesByUser() {
        List<Map> templateList = []
        int totalCount = 0
        User user = User.findByUsernameIlikeAndEnabled(params.username, true)
        String search = ""
        if (user) {
            search = params.search
            templateList = templateService.getTemplatesByUser(user, search, params.int('max'), params.int('offset'))
            totalCount = templateService.countTemplatesByUser(user, search)
        }
        render([templateList: templateList, totalCount: totalCount] as JSON)
    }

    /**
     * Method to fetch the Cioms1Id.
     * @return : cioms1Id
     */
    def getCioms1Id() {
        Long cioms1Id = ReportTemplate.cioms1Id()
        render([cioms1Id: cioms1Id] as JSON)
    }

    /**
     * Method to fetch the nameID listof Templates.
     * @return : Map
     * @input : templateList
     */
    def templateIdNameList() {
        List<String> templateIdList = params.templateList.split(",")
        List<Map> templateIdNameList = templateService.generateTemplateListByIDs(templateIdList)
        render([templateIdNameList: templateIdNameList] as JSON)
    }

    /**
     * Method to fetch the list of template with all fields based on the passed user.
     * @input  : String username, params
     * @return : Map result = {'aaData' : List<ReportTemplate>, 'recordsTotal': totalRecordsCount, 'recordsFiltered': filteredRecordsCount}* @input : params: username, max, offset, columnName(sort), dir(direction of sort), searchString
     *
     * // search String not working on dates
     */
    def getTemplatesDetailByUser(String username) {
        log.info('getTemplatesDetailByUser called')
        User user = User.findByUsernameIlikeAndEnabled(username, true)
        Map result = [:]
        if (user) {
            result = templateService.getTemplatesDetailByUser(user, params.int('max', 0), params.int('offset', 0), params.searchString?.trim(), params.columnName, params.dir)
        }
        render(result as JSON)
    }
}
