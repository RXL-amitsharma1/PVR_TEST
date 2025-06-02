package com.rxlogix.api


import com.rxlogix.LibraryFilter
import com.rxlogix.config.*
import com.rxlogix.enums.TemplateTypeEnum
import com.rxlogix.mapping.ClDatasheetReassess
import com.rxlogix.util.DateUtil
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.core.GrailsApplication
import grails.gorm.multitenancy.Tenants
import grails.plugin.springsecurity.annotation.Secured
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.grails.web.json.JSONObject

@Secured('permitAll')
class ReportTemplateRestController implements SanitizePaginationAttributes {

    def customMessageService
    def springSecurityService
    def templateService
    def userService
    GrailsApplication grailsApplication
    def xmlTemplateService

    static allowedMethods = [list: 'POST']

    def index() {
        list()
    }

    def list() {
        sanitize(params)
        boolean showXMLOption = grailsApplication.config.show.xml.option ?: false
        LibraryFilter filter = new LibraryFilter(params,userService.getUser(),ReportTemplate)
        List<Long> idsForUser = ReportTemplate.fetchAllIdsBySearchString(filter, showXMLOption, params.sort, params.order).
                list([max  : params.max, offset: params.offset]).collect { it.first() }

        int recordsFilteredCount = ReportTemplate.countRecordsBySearchString(filter, showXMLOption).get()
        List<ReportTemplate> reportTemplateList = idsForUser ? ReportTemplate.getAll(idsForUser) : []
        int recordsTotal = ReportTemplate.countRecordsBySearchString(new LibraryFilter(userService.getUser()), showXMLOption).get()
        render([aaData : reportTemplateList.collect {
            toMap(GrailsHibernateUtil.unwrapIfProxy(it))
        }, recordsTotal: recordsTotal, recordsFiltered: recordsFilteredCount] as JSON)
    }

    def save() {
        // set the schedulingUser and delegate down to the RestfulController
        def schedulingUser = springSecurityService.currentUser
        params.modifiedBy = schedulingUser
        super.save()
    }

    def columns() {
        respond ReportField.list(), [formats: ['json']]
    }

    def getTemplateList(String term, Integer page, Integer max, Boolean showXMLSpecific) {
        if (!max) {
            max = 30
        }
        if (!page) {
            page = 1
        }
        if (term) {
            term = term?.trim()
        }
        render([items : templateService.getTemplateList(term, Math.max(page - 1, 0) * max, max, showXMLSpecific).collect {
            [id: it.id, text: it.text, hasBlanks: it.hasBlanks, qced: it.qced, isFavorite: it.isFavorite, configureAttachments: it.configureAttachments]
        }, total_count: templateService.getTemplateListCount(term, showXMLSpecific)] as JSON)
    }

    def getTemplateSetCLLCSQL(Long oldSelectedValue, String term, Integer page, Integer max) {
        if (!max) {
            max = 30
        }
        if (!page) {
            page = 1
        }
        if (term) {
            term = term?.trim()
        }
        Map templateResultdata = templateService.getTemplateListForTemplateSet(oldSelectedValue, term, Math.max(page - 1, 0) * max, max, null, true)
        render([items : templateResultdata.list.collect {
            [id: it.id, text: it.nameWithDescription, groupingColumns: it.groupingColumns, qced: it.qualityChecked, isFavorite: it.isFavorite]
        }, total_count: templateResultdata.totalCount] as JSON)
    }

    def getTemplateSetCLL(Long oldSelectedValue, String term, Integer page, Integer max, String templateTypeEnum) {
        if (!max) {
            max = 30
        }
        if (!page) {
            page = 1
        }
        if (term) {
            term = term?.trim()
        }
        Map templateResultdata = templateService.getTemplateListForTemplateSet(oldSelectedValue, term, Math.max(page - 1, 0) * max, max, templateTypeEnum as TemplateTypeEnum, false)
        render([items : templateResultdata.list.collect {
            [id: it.id, text: it.nameWithDescription, groupingColumns: it.groupingColumns, qced: it.qualityChecked, isFavorite: it.isFavorite]
        }, total_count: templateResultdata.totalCount] as JSON)
    }

    def getTemplateNameDescription(Long id) {
        ReportTemplate reportTemplate = ReportTemplate.read(id)
        Map result = [
                text: reportTemplate?.nameWithDescription,
                qced: reportTemplate?.qualityChecked,
                isFavorite: reportTemplate.isFavorite(userService.currentUser),
                configureAttachments: reportTemplate?.sectionsName,
        ]

        if ((reportTemplate instanceof CaseLineListingTemplate) && reportTemplate.groupingList) {
            ReportFieldInfoList reportFieldInfoList = reportTemplate.groupingList
            result.groupingColumns = reportFieldInfoList.reportFieldInfoList.collect { reportFieldInfo ->
                reportFieldInfo.reportField.name
            }
        }
        render(result as JSON)
    }

    def getSortOrder() {
        def order = []
        ExecutedCaseLineListingTemplate template = ExecutedCaseLineListingTemplate.get(params.currentId)
        if (template && template.getJSONColumnsSortOrder()) {
            try {
                JSONObject sortOrder = JSON.parse(template.getJSONColumnsSortOrder())
                sortOrder.columns.each {
                    order.add([it.seq, it.sort])
                }
            } catch (Exception e) {
                log.error("Unable to generate columns sort order for ExecutedTemplate.id=${template.id}", e)
            }
            respond order, [formats: ['json']]
        }
        respond order, [formats: ['json']]
    }

    def getReportFieldInfoList() {
        List<Long> templateIds = params.getList("templateId")
        String term = params.term
        if (term) {
            term = term?.trim().toLowerCase()
        }

        List<CaseLineListingTemplate> caseLineListingTemplates = templateIds ? CaseLineListingTemplate.findAllByIdInList(templateIds) : []
        List<CustomSQLTemplate> customSqlTemplates = templateIds ? CustomSQLTemplate.findAllByIdInList(templateIds) : []
        List<ReportTemplate> templates = caseLineListingTemplates + customSqlTemplates
        def result = [items: templates.collect { ReportTemplate template ->
            if(template instanceof CaseLineListingTemplate) {
                List<ReportFieldInfo> fields = template.allSelectedFieldsInfoForXML
                [
                        id      : template.id,
                        text    : template.nameWithDescription,
                        children: fields.inject([]) { filteredField, field ->
                            String fieldText = field.renameValue ?: customMessageService.getMessage("app.reportField.${field.reportField.name}")
                            if (!term || fieldText.toLowerCase().contains(term)) {
                                filteredField.push(
                                        [
                                                id        : field.id,
                                                text      : fieldText,
                                                templateId: template.id,
                                                type      : "CLL"
                                        ])
                            }
                            filteredField
                        }
                ]
            } else if(template instanceof CustomSQLTemplate) {
                List<String> fields = template.columnNamesList.replaceAll(~/[\[\]]/, '').split(",")
                [
                        id: template.id,
                        text: template.nameWithDescription,
                        children: fields.inject([]) {filteredField, field ->
                            String fieldText = field
                            if (!term || fieldText.toLowerCase().contains(term)) {
                                filteredField.push(
                                        [
                                                id        : fieldText,
                                                text      : fieldText,
                                                templateId: template.id,
                                                type      : "CustomSQL"
                                        ])
                            }
                            filteredField
                        }
                ]
            }
        }]
        render(result as JSON)
    }

    def getDatasheetValues() {
        ClDatasheetReassess.withTransaction {
             render ClDatasheetReassess.createCriteria().list {
                projections{
                    property('sheetName')
                    property('hasChildrenFlag')
                }
                eq('tenantId',Tenants.currentId() as Long)
                order("sheetName", "asc")
            }.collect {
                 [sheetName: it[0], hasChildrenFlag: it[1]]
             } as JSON
        }
    }

    def getReportFieldInfoNameDescription(Long id) {
        ReportFieldInfo field = ReportFieldInfo.get(id)
        String value = field ? field.renameValue ?: customMessageService.getMessage("app.reportField.${field.reportField.name}") : null
        render([ text: value] as JSON)
    }

    private Map toMap(ReportTemplate template) {
        Map map = [:]
        map['id'] = template.id
        map['category'] = template.category?.name ?: " ";
        map['name'] = template.name
        map['description'] = template.description
        map['dateCreated'] = template.dateCreated.format(DateUtil.DATEPICKER_UTC_FORMAT)
        map['lastUpdated'] = template.lastUpdated.format(DateUtil.DATEPICKER_UTC_FORMAT)
        map['lastExecuted'] = template.lastExecuted?.format(DateUtil.DATEPICKER_UTC_FORMAT)
        map['tags'] = ViewHelper.getCommaSeperatedFromList(template.tags)
        map['owner'] = template.owner
        map['createdBy'] = template.createdBy
        map['isDeleted'] = template.isDeleted
        map['checkUsage'] = template.countUsage()
        map['qualityChecked'] = template.qualityChecked
        map['templateType'] = ViewHelper.getI18nMessageForString(template.templateType.i18nKey)
        if (template.hasProperty('chartCustomOptions')) {
            map['chartCustomOptions'] = template.chartCustomOptions
        }
        map['useFixedTemplate'] = template.useFixedTemplate
        map['isFavorite'] = template.isFavorite(userService.currentUser)
        return map
    }

    def checkDrillDown() {
        Set result = []
        List<Long> templateIds = params.list("drillDowns[]")?.collect { it as Long }
        if (templateIds)
            ReportTemplate.findAllByIdInList(templateIds).each {
                ReportTemplate template = GrailsHibernateUtil.unwrapIfProxy(it)
                if (!(template.getAllSelectedFieldsInfo().collect {
                    it?.reportField?.name
                }.intersect(params.list("fields[]"))?.size() == params.list("fields[]")?.size())) {
                    result << [id: template.id, name: template.name]
                }
            }
        render result as JSON
    }
}
