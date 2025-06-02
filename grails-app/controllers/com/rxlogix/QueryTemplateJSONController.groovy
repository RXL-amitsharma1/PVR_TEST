package com.rxlogix

import com.rxlogix.config.Configuration
import com.rxlogix.config.IcsrReportConfiguration
import com.rxlogix.config.PeriodicReportConfiguration
import com.rxlogix.config.ReportConfiguration
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.util.Holders

import java.nio.file.Files

@Secured(["ROLE_SYSTEM_CONFIGURATION"])
class QueryTemplateJSONController {

    QueryTemplateJSONService queryTemplateJSONService

    public final String DOWNLOADABLE_QUERY_JSON_FILE_NAME = Constants.Queries.pvr
    public final String DOWNLOADABLE_TEMPLATE_JSON_FILE_NAME = Constants.Templates.pvr
    public final String DOWNLOADABLE_DASHBOARD_JSON_FILE_NAME = Constants.Dashboards.pvr
    public final String DOWNLOADABLE_CONFIGURATION_JSON_FILE_NAME = Constants.Configurations.pvr

    def index() {
    }

    def viewConfiguration(Long id) {
        ReportConfiguration configuration = ReportConfiguration.get(id)
        if (configuration instanceof Configuration) {
            redirect(controller: 'configuration', action: "view", id: id)
        } else if (configuration instanceof PeriodicReportConfiguration) {
            redirect(controller: 'periodicReport', action: "view", id: id)
        } else if (configuration instanceof IcsrReportConfiguration) {
            redirect(controller: 'icsrReport', action: "view", id: id)
        } else {
            redirect(controller: 'icsrProfileConfiguration', action: "view", id: id)
        }
    }

    List<Map> queryList() {
        List<Map> queryList = queryTemplateJSONService.fetchAllQueries()
        response.status = 200
        render queryList as JSON
    }

    List<Map> templateList() {
        List<Map> templateList = queryTemplateJSONService.fetchAllTemplates()
        response.status = 200
        render templateList as JSON
    }

    List<Map> configurationList() {
        List<Map> templateList = queryTemplateJSONService.fetchAllConfigurations()
        response.status = 200
        render templateList as JSON
    }

    List<Map> dashboardList() {
        List<Map> templateList = queryTemplateJSONService.fetchAllDashboards()
        response.status = 200
        render templateList as JSON
    }

    def downloadQueryJSON() {
        List jsons = queryTemplateJSONService.fetchQueriesJSON(params.selectedIds)
        if (jsons.size() == 0) {
            flash.error = message(code: 'default.not.downloaded.json')
            redirect action: "index"
            return
        }
        File file = queryTemplateJSONService.writeJsonToFile(jsons, DOWNLOADABLE_QUERY_JSON_FILE_NAME)
        render (status: 200, text: file.name)
    }

    def downloadTemplateJSON() {
        List jsons = queryTemplateJSONService.fetchTemplatesJSON(params.selectedIds)
        if (jsons.size() == 0) {
            flash.error = message(code: 'default.not.downloaded.json')
            redirect action: "index"
            return
        }
        File file = queryTemplateJSONService.writeJsonToFile(jsons, DOWNLOADABLE_TEMPLATE_JSON_FILE_NAME)
        render(status: 200, text: file.name)
    }

    def downloadDashboardJSON() {
        List jsons = queryTemplateJSONService.fetchDashboardJSON(params.selectedIds)
        if (jsons.size() == 0) {
            flash.error = message(code: 'default.not.downloaded.json')
            redirect action: "index"
            return
        }
        File file = queryTemplateJSONService.writeJsonToFile(jsons, DOWNLOADABLE_DASHBOARD_JSON_FILE_NAME)
        render(status: 200, text: file.name)
    }

    def downloadConfigurationJSON() {
        List jsons = queryTemplateJSONService.fetchConfigurationJSON(params.selectedIds)
        if (jsons.size() == 0) {
            flash.error = message(code: 'default.not.downloaded.json')
            redirect action: "index"
            return
        }
        File file = queryTemplateJSONService.writeJsonToFile(jsons, DOWNLOADABLE_CONFIGURATION_JSON_FILE_NAME)
        render(status: 200, text: file.name)
    }

    def renderFile(String name) {
        File file = new File(grailsApplication.config.tempDirectory + '/' + name)
        response.setContentType("application/JSON");
        response.setHeader("Content-Length", String.valueOf(file.size()))
        response.addHeader("Content-Disposition", "attachment; filename=${name.split("_")[0]}")

        OutputStream responseOutputStream = response.getOutputStream()
        responseOutputStream.write(Files.readAllBytes(file.toPath()))
        responseOutputStream.close()
        file.delete()
    }
}
