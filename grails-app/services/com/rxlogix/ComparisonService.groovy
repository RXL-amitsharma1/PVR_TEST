package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.enums.EvaluateCaseDateEnum
import com.rxlogix.enums.ExecutingEntityTypeEnum
import com.rxlogix.enums.ReportExecutionStatusEnum
import com.rxlogix.enums.UnitTypeEnum
import com.rxlogix.user.User
import com.rxlogix.util.MiscUtil
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord
import org.apache.commons.io.IOUtils
import org.grails.web.json.JSONElement
import org.xmlunit.builder.DiffBuilder
import org.xmlunit.builder.Input
import org.xmlunit.diff.Diff

import java.util.zip.GZIPInputStream

class ComparisonService {
    def userService
    def configurationService
    def CRUDService
    def templateService
    def dynamicReportService
    def executedConfigurationService
    def queryService


    Map fetchFiles(byte[] data, int level) {
        Map result = [:]
        TarArchiveInputStream subreportsInputStream = new TarArchiveInputStream(new GZIPInputStream(new ByteArrayInputStream(data)))
        TarArchiveEntry entry = subreportsInputStream.getNextTarEntry();
        while (entry != null) {
            String fileName = entry.name
            if (fileName.contains("/")) {
                fileName = fileName.substring(fileName.indexOf("/") + 1)
            }
            if (level == 0) result.put(fileName, fetchFiles(IOUtils.toByteArray(subreportsInputStream), 1))
            String id = fileName.tokenize(".")[0]
            String name = id
            try {
                if (id != "grouping") name = ReportTemplate.get(id as Long)?.name
            }catch(Exception e){
                //ignoring
            }
            if(!name) name = id
            if (level == 1) result.put(name, new String(IOUtils.toByteArray(subreportsInputStream), "UTF-8"))
            entry = subreportsInputStream.getNextTarEntry();
        }
        return result
    }

    Set parseCsv(String theString) {
        Reader reader = new StringReader(theString)
        Set result = []
        Iterable<CSVRecord> records = CSVFormat.RFC4180.parse(reader);
        records.each {
            result << it.join("~@~")
        }
        return result
    }

    Set parseJson(String theString, sectionResultDTO) {
        def jsonSlurper = new JsonSlurper()
        jsonSlurper.parseText(theString ?: "{}").collect {
            StringBuilder visibleRow = new StringBuilder("{")
            sectionResultDTO.headerKeys.eachWithIndex { key, i ->
                if (i > 0) visibleRow.append(',')
                visibleRow.append('"' + key + '":"' + it[key] + '"')
            }
            visibleRow.append("}")
            visibleRow.toString()
        }.toSet()
    }

    def getCaseFormData(byte[] data) {
        return fetchFiles(data, 0)
    }

    String getStringContent(ReportResult r) {
        if (r?.data?.value) {
            def reportResultInputStream = new GZIPInputStream(new ByteArrayInputStream(r?.data?.decryptedValue))
            return new Scanner(reportResultInputStream).useDelimiter("\\A").next()
        }
        return ""
    }

    def compareCllDtSections(ReportResult r1, ReportResult r2, Class clazz, SectionComparisonResultDTO sectionResultDTO) {
        if (clazz == ExecutedTemplateSet) {
            ExecutedReportConfiguration rc1 = r1.executedTemplateQuery.executedConfiguration
            ExecutedReportConfiguration rc2 = r2.executedTemplateQuery.executedConfiguration
            Map r1data = getCaseFormData(r1?.data?.decryptedValue)
            Map r2data = getCaseFormData(r2?.data?.decryptedValue)

            boolean eq = true
            r1data.each { r1name, r1zip ->
                Map r2zip = r2data.get(r1name)
                if (!r2zip) {
                    sectionResultDTO.log += rc1.reportName + " v." + rc1.numOfExecutions + " " + ViewHelper.getMessage("app.comparison.log.hascase") + " ${r1name}" + ViewHelper.getMessage("app.comparison.log.doesnotexist")
                    eq = false
                } else {
                    r1zip.each { r1cvsName, r1cvs ->
                        String r2cvs = r2zip.get(r1cvsName)
                        if (!r2cvs) {
                            sectionResultDTO.log += rc1.reportName + " v." + rc1.numOfExecutions + " " + ViewHelper.getMessage("app.comparison.log.hassection") + " ${r1name}->${r1cvsName}" + ViewHelper.getMessage("app.comparison.log.doesnotexist")
                            eq = false
                        } else {
                            if (r2cvs != r1cvs) {
                                sectionResultDTO.log += rc1.reportName + " v." + rc1.numOfExecutions + " " + ViewHelper.getMessage("app.comparison.log.hassection") + " ${r1name}->${r1cvsName} that has different content with compared one<br>"
//                                sectionResultDTO.log.append("------file content for" + rc1.reportName + " v." + rc1.numOfExecutions + "---------")
//                                sectionResultDTO.log.append(r1cvs)
//                                sectionResultDTO.log.append("------------------------")
//                                sectionResultDTO.log.append("------file content for" + rc2.reportName + " v." + rc2.numOfExecutions + "---------")
//                                sectionResultDTO.log.append(r2cvs)
//                                sectionResultDTO.log.append("------------------------")
                                eq = false
                            }
                        }
                    }
                }
            }

            r2data.each { r2name, r2zip ->
                Map r1zip = r1data.get(r2name)
                if (!r1zip) {
                    sectionResultDTO.log += rc2.reportName + " v." + rc2.numOfExecutions + " " + ViewHelper.getMessage("app.comparison.log.hascase") + "  ${r2name} " + ViewHelper.getMessage("app.comparison.log.doesnotexist")
                    eq = false
                } else {
                    r2zip.each { r2cvsName, r2cvs ->
                        String r1cvs = r1zip.get(r2cvsName)
                        if (!r1cvs) {
                            sectionResultDTO.log += rc2.reportName + " v." + rc2.numOfExecutions + " " + ViewHelper.getMessage("app.comparison.log.hassection") + " ${r2name}->${r2cvsName}" + ViewHelper.getMessage("app.comparison.log.doesnotexist")
                            eq = false
                        }
                    }
                }
            }
            if (eq) {
                sectionResultDTO.log += ViewHelper.getMessage("app.comparison.log.sectionsEquals")
                sectionResultDTO.sectionsAreEqual = true
            }
            return
        }
        def r1Content = getStringContent(r1)
        def r2Content = getStringContent(r2)
        if (r1Content == r2Content) {
            sectionResultDTO.log += ViewHelper.getMessage("app.comparison.log.sectionsEquals")
            sectionResultDTO.sectionsAreEqual = true
        } else {
            Set<String> r1ContentList
            Set<String> r2ContentList
            if (clazz in [ExecutedCaseLineListingTemplate, ExecutedCustomSQLTemplate, ExecutedNonCaseSQLTemplate]) {
                r1ContentList = parseCsv(r1Content)
                r2ContentList = parseCsv(r2Content)
            } else if (clazz == ExecutedDataTabulationTemplate) {
                r1ContentList = parseJson(r1Content, sectionResultDTO)
                r2ContentList = parseJson(r2Content, sectionResultDTO)
            } else if (clazz == ExecutedXMLTemplate) {
                sectionResultDTO.log += "NOT SUPPORTED"
                sectionResultDTO.supported = false
                return
            } else {
                sectionResultDTO.log += "NOT SUPPORTED"
                sectionResultDTO.supported = false
                return
            }
            Set<String> r1Dif = r1ContentList - r2ContentList
            Set<String> r2Dif = r2ContentList - r1ContentList
            if (r2Dif || r1Dif) {
                ExecutedReportConfiguration rc1 = r1.executedTemplateQuery.executedConfiguration
                ExecutedReportConfiguration rc2 = r2.executedTemplateQuery.executedConfiguration
                sectionResultDTO.log += ViewHelper.getMessage("app.comparison.log.sectionsNotEquals")
                sectionResultDTO.log += rc1.reportName + " v." + rc1.numOfExecutions + " " + ViewHelper.getMessage("app.comparison.log.has") + " " + r1ContentList.size() + " " + ViewHelper.getMessage("app.comparison.log.rowsand") + " " + r1Dif.size() + " " + ViewHelper.getMessage("app.comparison.log.donotmatch")
                sectionResultDTO.log += rc2.reportName + " v." + rc2.numOfExecutions + " " + ViewHelper.getMessage("app.comparison.log.has") + " " + r2ContentList.size() + " " + ViewHelper.getMessage("app.comparison.log.rowsand") + " " + r2Dif.size() + " " + ViewHelper.getMessage("app.comparison.log.donotmatch")
                if (r1Dif) {
                    sectionResultDTO.report1Row = r1Dif
                }
                if (r2Dif) {
                    sectionResultDTO.report2Row = r2Dif
                }
            } else {
                sectionResultDTO.log += ViewHelper.getMessage("app.comparison.log.sectionsEquals")
                sectionResultDTO.sectionsAreEqual = true
            }
        }
    }

    List getCasesForIcsrReports(ExecutedReportConfiguration config1, ExecutedReportConfiguration config2) {
        Map cases1, cases2
        IcsrReportCase.withNewSession {
            cases1 = IcsrReportCase.findAllByExIcsrTemplateQueryId(config1.executedTemplateQueries[0].id).collectEntries { [(it.caseNumber + "_v." + it.versionNumber): it] }
            cases2 = IcsrReportCase.findAllByExIcsrTemplateQueryId(config2.executedTemplateQueries[0].id).collectEntries { [(it.caseNumber + "_v." + it.versionNumber): it] }

        }
        return [cases1, cases2]
    }

    String getXmlForIcsr(ExecutedReportConfiguration config, IcsrReportCase case1) {
        return dynamicReportService.createXMLReport(config.executedTemplateQueries[0], false, [caseNumber: case1.caseNumber, versionNumber: case1.versionNumber]).text
    }

    ComparisonResultDTO compareIcsrReports(ExecutedReportConfiguration config1, ExecutedReportConfiguration config2, ComparisonResultDTO resultDTO) {
        Map cases1, cases2
        (cases1, cases2) = getCasesForIcsrReports(config1, config2)

        cases1.each { key, case1 ->
            SectionComparisonResultDTO sectionResultDTO = new SectionComparisonResultDTO()
            sectionResultDTO.title = ViewHelper.getMessage("app.comparison.log.compOfXml") + " " + key
            sectionResultDTO.type = "ICSR"
            resultDTO.sections << sectionResultDTO
            IcsrReportCase case2 = cases2.get(key)
            if (!case2) {
                sectionResultDTO.log += ViewHelper.getMessage( "app.comparison.log.case") + " " + key + " " + ViewHelper.getMessage( "app.comparison.log.doesNotExist") + " " + resultDTO.reportName2
                sectionResultDTO.sectionsAreEqual = false
                resultDTO.reportsAreEqual = false
            } else {
                cases2.remove(key)
                String xml1 = getXmlForIcsr(config1, case1)
                String xml2 = getXmlForIcsr(config2, case1)
                Diff diff = DiffBuilder.compare(Input.fromString(xml1))
                        .withTest(Input.fromString(xml2))
                        .ignoreComments()
                        .ignoreWhitespace()
                        .normalizeWhitespace()
                        .ignoreElementContentWhitespace()
                        .build()
                if (diff.differences?.size() > 0) {
                    sectionResultDTO.log += ViewHelper.getMessage("app.comparison.log.differences") + " <br><br>"
                    sectionResultDTO.log += diff.differences.collect { it.toString().replaceAll("<", "&lt;").replaceAll(">", "&gt;") }.join("<br><br>")
                    sectionResultDTO.sectionsAreEqual = false
                    resultDTO.reportsAreEqual = false
                } else {
                    sectionResultDTO.log += ViewHelper.getMessage("app.comparison.log.XMLequal")
                    sectionResultDTO.sectionsAreEqual = true
                }
            }
        }
        if (cases2.size() > 0) {
            cases2.each { key, value ->
                SectionComparisonResultDTO sectionResultDTO = new SectionComparisonResultDTO()
                sectionResultDTO.title = ViewHelper.getMessage("app.comparison.log.compOfXml") + " " +key
                sectionResultDTO.type = "ICSR"
                resultDTO.sections << sectionResultDTO
                sectionResultDTO.log += ViewHelper.getMessage("app.comparison.log.case") + " " + key + " " + ViewHelper.getMessage("app.comparison.log.doesNotExist") + " " + resultDTO.reportName1
                sectionResultDTO.sectionsAreEqual = false
                resultDTO.reportsAreEqual = false
            }
        }
        return resultDTO
    }

    ComparisonResultDTO compareReports(ExecutedReportConfiguration config1, ExecutedReportConfiguration config2) {
        ComparisonResultDTO resultDTO = new ComparisonResultDTO()
        resultDTO.reportName1 = "${config1.reportName} v${config1.numOfExecutions}"
        resultDTO.reportName2 = "${config2.reportName} v${config2.numOfExecutions}"
        try {
            if (config1 instanceof ExecutedIcsrReportConfiguration) return compareIcsrReports(config1, config2, resultDTO)
            if (config1 instanceof ExecutedPeriodicReportConfiguration) {
                if (config1.status == ReportExecutionStatusEnum.GENERATED_CASES) {
                    resultDTO.log = ViewHelper.getMessage("app.comparison.log.agg")
                    resultDTO.message = resultDTO.log
                    resultDTO.reportsAreEqual = false
                    resultDTO.supported = false
                    return resultDTO
                }
            }
            Map<ExecutedTemplateQuery, List<ExecutedTemplateQuery>> tq1 = config1.getSectionExTempQueriesMap()
            Map<ExecutedTemplateQuery, List<ExecutedTemplateQuery>> tq2 = config2.getSectionExTempQueriesMap()
            if (tq1.size() != tq2.size()) {
                resultDTO.log += ViewHelper.getMessage("app.comparison.log.diffnumber") + "<br>"
                resultDTO.message = ViewHelper.getMessage("app.comparison.log.diffnumber")
                resultDTO.reportsAreEqual = false
                resultDTO.supported = false
                return resultDTO
            } else {
                resultDTO.log += "<br><br>.........!!!!!!!!!!${ViewHelper.getMessage("app.comparison.log.comp")} " + config1.reportName + " " + ViewHelper.getMessage("app.comparison.log.and") + " " + config2.reportName + "!!!!!!!!!...............<br>"
            }
            Set<ExecutedTemplateQuery> rootTqSet1 = tq1.keySet()
            Set<ExecutedTemplateQuery> rootTqSet2 = tq2.keySet()
            for (int i = 0; i < rootTqSet1.size(); i++) {
                resultDTO.log += "<br><br>${ViewHelper.getMessage("app.comparison.log.compsect")} " + i + " ...... : " + rootTqSet1[i].usedTemplate.class
                SectionComparisonResultDTO sectionResultDTO = new SectionComparisonResultDTO()
                sectionResultDTO.title = ViewHelper.getMessage("app.comparison.log.sect") + " ${rootTqSet1[i].getUsedTemplate().getName()}\""
                setType(rootTqSet1[i].getUsedTemplate(), sectionResultDTO, rootTqSet1[i].reportResult)
                resultDTO.sections << sectionResultDTO
                if (rootTqSet1[i].executedTemplate.name != rootTqSet2[i].executedTemplate.name) {
                    sectionResultDTO.log += ViewHelper.getMessage("app.comparison.log.difftemplate")
                    sectionResultDTO.sectionsAreEqual = false
                    resultDTO.reportsAreEqual = false
                    continue
                }
                compareCllDtSections(rootTqSet1[i].reportResult, rootTqSet2[i].reportResult, rootTqSet1[i].usedTemplate.class, sectionResultDTO)
                if (!sectionResultDTO.sectionsAreEqual) resultDTO.reportsAreEqual = false


                //---nested templates for template set
                if (tq1.values()[i].size() > 1) {
                    Set<ExecutedTemplateQuery> nestedTQ1 = tq1.values()[i]
                    Set<ExecutedTemplateQuery> nestedTQ2 = tq2.values()[i]
                    if (nestedTQ1.size() == nestedTQ2.size()) {
                        for (int j = 0; j < nestedTQ1.size(); j++) {
                            SectionComparisonResultDTO sectionResultDTO2 = new SectionComparisonResultDTO(hasParent: true)
                            sectionResultDTO2.title = ViewHelper.getMessage("app.comparison.log.sect") + rootTqSet1[i].getUsedTemplate().getName() + ViewHelper.getMessage("app.comparison.log.test") + nestedTQ1[j].getUsedTemplate().getName() + "\""
                            setType(nestedTQ1[j].getUsedTemplate(), sectionResultDTO2, nestedTQ1[j].reportResult)
                            resultDTO.sections << sectionResultDTO2
                            if (nestedTQ1[j].executedTemplate.name != nestedTQ1[j].executedTemplate.name) {
                                sectionResultDTO2.log += ViewHelper.getMessage("app.comparison.log.difftemplate")
                                sectionResultDTO2.sectionsAreEqual = false
                                resultDTO.reportsAreEqual = false
                                continue
                            }
                            compareCllDtSections(nestedTQ1[j].reportResult, nestedTQ2[j].reportResult, nestedTQ1[j].usedTemplate.class, sectionResultDTO2)
                        }
                    }
                }
            }
            return resultDTO
        } catch (Exception e) {
            resultDTO.log = resultDTO.log ?: "" + ViewHelper.getMessage("app.comparison.log.err") + e.getMessage()
            resultDTO.message = ViewHelper.getMessage("app.comparison.log.err") + e.getMessage()
            resultDTO.reportsAreEqual = false
            resultDTO.supported = false
            log.error(ViewHelper.getMessage("app.comparison.log.err") + e.getMessage(), e);
            return resultDTO
        }
    }

    void setType(def template, SectionComparisonResultDTO section, ReportResult reportResult) {
        if (template instanceof ExecutedDataTabulationTemplate) {
            section.type = "DT"
            section.header = JSON.parse(reportResult.data.crossTabHeader).collect { it.collect { k, v -> v?.replaceAll("\n", "<br>") }[0] }
            section.headerKeys = JSON.parse(reportResult.data.crossTabHeader).collect { it.collect { k, v -> k }[0] }
        } else if (template instanceof com.rxlogix.config.ExecutedTemplateSet) {
            section.type = "TS"
        } else {
            section.type = "CLL"
            if (template instanceof ExecutedCaseLineListingTemplate) {
                section.header = template.getAllSelectedFieldsInfo().collect { it.renameValue ?: (ViewHelper.getMessage('app.reportField.' + it.reportField.name) ?: it.reportField.name) }
            } else {
                section.header = templateService.getColumnNamesList(template.columnNamesList)
            }
        }
    }

    static class ComparisonResultDTO {
        ComparisonResultDTO() {}

        ComparisonResultDTO(JSONElement json) {
            reportName1 = json.reportName1
            reportName2 = json.reportName2
            log = json.log
            reportsAreEqual = json.reportsAreEqual
            sections = []
            for (def it : json.sections) {
                sections << new SectionComparisonResultDTO(
                        title: it.title,
                        type: it.type,
                        header: it.header,
                        headerKeys: it.headerKeys,
                        hasParent: it.hasParent,
                        log: it.log,
                        sectionsAreEqual: it.sectionsAreEqual,
                        supported: it.supported,
                        report1Row: it.report1Row,
                        report2Row: it.report2Row
                )
            }
        }
        String reportName1
        String reportName2
        String log = ""
        Boolean reportsAreEqual = true
        Boolean supported = true
        String message
        List<SectionComparisonResultDTO> sections = []
    }

    static class SectionComparisonResultDTO {
        String title
        String type
        List header
        List headerKeys
        Boolean hasParent = false
        String log = ""
        Boolean sectionsAreEqual = false
        Boolean supported = true
        Set<String> report1Row = []
        Set<String> report2Row = []
    }


    ReportConfiguration createCopy(ExecutedReportConfiguration exc, User owner, String namePrefix, Date nextRunDate, Boolean runDraft = true, ExecutedCaseSeries useCaseSeries = null) {
        if (exc.instanceOf(ExecutedConfiguration))
            return copyAdhoc(exc, owner, namePrefix, nextRunDate, useCaseSeries)
        else if (exc.instanceOf(ExecutedPeriodicReportConfiguration))
            return copyAggregate(exc, owner, namePrefix, nextRunDate, runDraft)
        else
            return copyIcsr(exc, owner, namePrefix, nextRunDate)

    }


    Configuration copyAdhoc(ExecutedConfiguration configuration, User owner, String namePrefix, Date nextRunDate, ExecutedCaseSeries useCaseSeries) {
        DeliveryOption deliveryOption = new DeliveryOption(sharedWith: [owner])

        Map pars = [
                reportName                    : generateUniqueName(configuration, owner, namePrefix + "_", "_" + configuration.numOfExecutions),
                owner                         : owner,
                nextRunDate                   : nextRunDate,
                scheduleDateJSON              : "{\"startDateTime\":\"${nextRunDate.format("yyyy-MM-dd'T'HH:mm")}Z\",\"timeZone\":{\"text\":\"(GMT +00:00) UTC\\n                                \",\"selected\":true,\"offset\":\"+00:00\",\"name\":\"UTC\"},\"recurrencePattern\":\"FREQ=DAILY;INTERVAL=1;COUNT=1\"}",
                description                   : configuration.description,
                isDeleted                     : false,
                isEnabled                     : true,
                tags                          : configuration.tags,
                dateRangeType                 : configuration.dateRangeType,
                sourceProfile                 : configuration.sourceProfile,
                productSelection              : configuration.productSelection,
                studySelection                : configuration.studySelection,
                eventSelection                : configuration.eventSelection,
                configSelectedTimeZone        : configuration.configSelectedTimeZone,
                asOfVersionDate               : configuration.asOfVersionDate,
                evaluateDateAs                : configuration.evaluateDateAs,
                excludeFollowUp               : configuration.excludeFollowUp,
                includeLockedVersion          : configuration.includeLockedVersion,
                includeAllStudyDrugsCases     : configuration.includeAllStudyDrugsCases,
                adjustPerScheduleFrequency    : configuration.adjustPerScheduleFrequency,
                suspectProduct                : configuration.suspectProduct,
                tenantId                      : configuration.tenantId,
                deliveryOption                : deliveryOption,
                createdBy                     : owner.username,
                modifiedBy                    : owner.username,
                excludeNonValidCases          : configuration?.excludeNonValidCases,
                excludeDeletedCases          : configuration?.excludeDeletedCases,
                limitPrimaryPath              : configuration?.limitPrimaryPath,
                blankValuesJSON               : configuration?.blankValuesJSON,
                includeMedicallyConfirmedCases: configuration?.includeMedicallyConfirmedCases,
                emailConfiguration            : null,
                qualityChecked                : false,
                useCaseSeries                 : useCaseSeries ?: configuration.usedCaseSeries,
                isMultiIngredient             : configuration?.isMultiIngredient,
                includeWHODrugs               : configuration?.includeWHODrugs
        ]
        if (configuration.evaluateDateAs == EvaluateCaseDateEnum.LATEST_VERSION) {
            pars.evaluateDateAs = EvaluateCaseDateEnum.VERSION_ASOF
            pars.asOfVersionDate = configuration.lastRunDate
        }//todo: add warning for EvaluateCaseDateEnum.ALL_VERSIONS as it is not supported

        Configuration newConfig = new Configuration(pars)
        if (configuration.executedGlobalDateRangeInformation) {
            newConfig.globalDateRangeInformation = new GlobalDateRangeInformation(MiscUtil.getObjectProperties(configuration.executedGlobalDateRangeInformation, GlobalDateRangeInformation.propertiesToUseForCopying))
            newConfig.globalDateRangeInformation.dateRangeEnum = DateRangeEnum.CUSTOM
            newConfig.globalDateRangeInformation.reportConfiguration = newConfig
        }
        if (configuration.executedGlobalQuery) {
            newConfig.globalQuery = queryService.createExecutedQuery(configuration.executedGlobalQuery)
            newConfig.globalQuery?.originalQueryId = newConfig.globalQuery?.id
            newConfig.globalQuery?.save()
        }
        configuration.executedGlobalQueryValueLists?.each {
            QueryValueList queryValueList = new QueryValueList(query: it.query)
            it.parameterValues.each {
                if (it.hasProperty('reportField')) {
                    QueryExpressionValue qev = new QueryExpressionValue()
                    qev.key = it.key
                    qev.reportField = it.reportField
                    qev.operator = it.operator
                    qev.value = it.value
                    qev.specialKeyValue = it.specialKeyValue
                    queryValueList.addToParameterValues(qev)
                } else {
                    CustomSQLValue csv = new CustomSQLValue()
                    csv.key = it.key
                    csv.value = it.value
                    queryValueList.addToParameterValues(csv)
                }
            }
            newConfig.addToGlobalQueryValueLists(queryValueList)
        }
        newConfig.includeNonSignificantFollowUp = configuration.includeNonSignificantFollowUp
        configuration.executedTemplateQueries.each { ExecutedTemplateQuery it ->

            TemplateQuery tq = new TemplateQuery(dateRangeInformationForTemplateQuery: new DateRangeInformation(
                    dateRangeEndAbsolute: it.executedDateRangeInformationForTemplateQuery.dateRangeEndAbsolute,
                    dateRangeStartAbsolute: it.executedDateRangeInformationForTemplateQuery.dateRangeStartAbsolute,
                    dateRangeEnum: DateRangeEnum.CUSTOM),
                    queryLevel: it.queryLevel, createdBy: owner.username,
                    modifiedBy: owner.username, header: it.header, title: it.title, footer: it.footer,
                    headerProductSelection: it.headerProductSelection, headerDateRange: it.headerDateRange,
                    draftOnly: it.draftOnly, blindProtected: it.blindProtected, privacyProtected: it.privacyProtected, displayMedDraVersionNumber: it.displayMedDraVersionNumber ?: false
            )
            createTemplateAndQuery(tq, it)
            configurationService.copyBlankValues(tq, it.executedQueryValueLists, it.executedTemplateValueLists)
            newConfig.addToTemplateQueries(tq)
        }
        CRUDService.save(newConfig)
    }

    IcsrReportConfiguration copyIcsr(ExecutedIcsrReportConfiguration configuration, User owner, String namePrefix, Date nextRunDate) {
        DeliveryOption deliveryOption = new DeliveryOption(sharedWith: [owner])
        IcsrReportConfiguration newIcsrReportConfiguration = new IcsrReportConfiguration(MiscUtil.getObjectProperties(configuration, IcsrReportConfiguration.propertiesToUseWhileCopying))
        if (configuration.evaluateDateAs == EvaluateCaseDateEnum.LATEST_VERSION) {
            newIcsrReportConfiguration.evaluateDateAs = EvaluateCaseDateEnum.VERSION_ASOF
            newIcsrReportConfiguration.asOfVersionDate = configuration.lastRunDate
        }
        newIcsrReportConfiguration.emailConfiguration = null
        newIcsrReportConfiguration.reportName = generateUniqueName(configuration, owner, namePrefix + "_", "_" + configuration.numOfExecutions)
        newIcsrReportConfiguration.owner = owner
        newIcsrReportConfiguration.createdBy = owner.username
        newIcsrReportConfiguration.modifiedBy = owner.username
        newIcsrReportConfiguration.deliveryOption = deliveryOption
        newIcsrReportConfiguration.nextRunDate = nextRunDate
        newIcsrReportConfiguration.scheduleDateJSON = "{\"startDateTime\":\"${nextRunDate.format("yyyy-MM-dd'T'HH:mm")}Z\",\"timeZone\":{\"text\":\"(GMT +00:00) UTC\\n                                \",\"selected\":true,\"offset\":\"+00:00\",\"name\":\"UTC\"},\"recurrencePattern\":\"FREQ=DAILY;INTERVAL=1;COUNT=1\"}"
        newIcsrReportConfiguration.isDeleted = false
        newIcsrReportConfiguration.isEnabled = true
        newIcsrReportConfiguration.dmsConfiguration = null
        newIcsrReportConfiguration.setReferenceProfile(IcsrProfileConfiguration.findByReportName(configuration.referenceProfileName))
        newIcsrReportConfiguration.recipientOrganization = UnitConfiguration.findByUnitName(configuration.recipientOrganizationName)
        newIcsrReportConfiguration.senderOrganization = UnitConfiguration.findByUnitName(configuration.senderOrganizationName)
        newIcsrReportConfiguration.tenantId = configuration.tenantId
        configuration.reportingDestinations?.each {
            newIcsrReportConfiguration.addToReportingDestinations(it)
        }
        if (configuration.executedGlobalDateRangeInformation) {
            newIcsrReportConfiguration.globalDateRangeInformation = new GlobalDateRangeInformation(MiscUtil.getObjectProperties(configuration.executedGlobalDateRangeInformation, GlobalDateRangeInformation.propertiesToUseForCopying))
            newIcsrReportConfiguration.globalDateRangeInformation.dateRangeEnum = DateRangeEnum.CUSTOM
            newIcsrReportConfiguration.globalDateRangeInformation.reportConfiguration = newIcsrReportConfiguration
        }
        if (configuration.executedGlobalQuery) {
            newIcsrReportConfiguration.globalQuery = queryService.createExecutedQuery(configuration.executedGlobalQuery)
            newIcsrReportConfiguration.globalQuery?.originalQueryId = newIcsrReportConfiguration.globalQuery?.id
            newIcsrReportConfiguration.globalQuery?.save()
        }
        configuration.executedGlobalQueryValueLists?.each {
            QueryValueList queryValueList = new QueryValueList(query: it.query)
            it.parameterValues.each {
                if (it.hasProperty('reportField')) {
                    queryValueList.addToParameterValues(new QueryExpressionValue(key: it.key,
                            reportField: it.reportField, operator: it.operator, value: it.value, specialKeyValue: it.specialKeyValue))
                } else {
                    queryValueList.addToParameterValues(new CustomSQLValue(key: it.key,
                            value: it.value))
                }
            }
            newIcsrReportConfiguration.addToGlobalQueryValueLists(queryValueList)
        }
        configuration.executedTemplateQueries.each {
            IcsrTemplateQuery tq = new IcsrTemplateQuery(MiscUtil.getObjectProperties(it, IcsrTemplateQuery.propertiesToUseWhileCopying))
            createTemplateAndQuery(tq, it)
            tq.createdBy = owner
            tq.modifiedBy = owner
            tq.dateRangeInformationForTemplateQuery = new DateRangeInformation(MiscUtil.getObjectProperties(it.executedDateRangeInformationForTemplateQuery, DateRangeInformation.propertiesToUseWhileCopying))
            tq.dateRangeInformationForTemplateQuery.dateRangeEnum = DateRangeEnum.CUSTOM
            tq.dateRangeInformationForTemplateQuery.templateQuery = tq
            newIcsrReportConfiguration.addToTemplateQueries(configurationService.copyBlankValues(tq, it.executedQueryValueLists, it.executedTemplateValueLists))
        }
        CRUDService.save(newIcsrReportConfiguration)
    }

    PeriodicReportConfiguration copyAggregate(ExecutedPeriodicReportConfiguration configuration, User owner, String namePrefix, Date nextRunDate, Boolean runDraft) {
        DeliveryOption deliveryOption = new DeliveryOption(sharedWith: [owner])
        PeriodicReportConfiguration newPeriodicReportConfiguration = new PeriodicReportConfiguration(MiscUtil.getObjectProperties(configuration, PeriodicReportConfiguration.propertiesToUseWhileCopying))
        if (configuration.evaluateDateAs == EvaluateCaseDateEnum.LATEST_VERSION) {
            newPeriodicReportConfiguration.evaluateDateAs = EvaluateCaseDateEnum.VERSION_ASOF
            newPeriodicReportConfiguration.asOfVersionDate = configuration.lastRunDate
        }//todo: add warning for EvaluateCaseDateEnum.ALL_VERSIONS as it is not supported

        newPeriodicReportConfiguration.emailConfiguration = null
        newPeriodicReportConfiguration.reportName = generateUniqueName(configuration, owner, namePrefix + "_", "_" + configuration.numOfExecutions)
        newPeriodicReportConfiguration.owner = owner
        newPeriodicReportConfiguration.createdBy = owner.username
        newPeriodicReportConfiguration.modifiedBy = owner.username
        newPeriodicReportConfiguration.deliveryOption = deliveryOption
        newPeriodicReportConfiguration.nextRunDate = nextRunDate
        newPeriodicReportConfiguration.scheduleDateJSON = "{\"startDateTime\":\"${nextRunDate.format("yyyy-MM-dd'T'HH:mm")}Z\",\"timeZone\":{\"text\":\"(GMT +00:00) UTC\\n                                \",\"selected\":true,\"offset\":\"+00:00\",\"name\":\"UTC\"},\"recurrencePattern\":\"FREQ=DAILY;INTERVAL=1;COUNT=1\"}"
        newPeriodicReportConfiguration.isDeleted = false
        newPeriodicReportConfiguration.isEnabled = true
        newPeriodicReportConfiguration.generateCaseSeries = configuration.hasGeneratedCasesData
        if (newPeriodicReportConfiguration.generateCaseSeries) {
            newPeriodicReportConfiguration.generateDraft = runDraft
        }
        configuration.reportingDestinations?.each {
            newPeriodicReportConfiguration.addToReportingDestinations(it)
        }
        if (configuration.executedGlobalDateRangeInformation) {
            newPeriodicReportConfiguration.globalDateRangeInformation = new GlobalDateRangeInformation(MiscUtil.getObjectProperties(configuration.executedGlobalDateRangeInformation, GlobalDateRangeInformation.propertiesToUseForCopying))
            newPeriodicReportConfiguration.globalDateRangeInformation.dateRangeEnum = DateRangeEnum.CUSTOM
            newPeriodicReportConfiguration.globalDateRangeInformation.reportConfiguration = newPeriodicReportConfiguration
        }
        if (configuration.executedGlobalQuery) {
            newPeriodicReportConfiguration.globalQuery = queryService.createExecutedQuery(configuration.executedGlobalQuery)
            newPeriodicReportConfiguration.globalQuery?.originalQueryId = newPeriodicReportConfiguration.globalQuery?.id
            newPeriodicReportConfiguration.globalQuery?.save()
        }
        configuration.executedGlobalQueryValueLists?.each {
            QueryValueList queryValueList = new QueryValueList(query: it.query)
            it.parameterValues.each {
                if (it.hasProperty('reportField')) {
                    QueryExpressionValue qev = new QueryExpressionValue()
                    qev.key = it.key
                    qev.reportField = it.reportField
                    qev.operator = it.operator
                    qev.value = it.value
                    qev.specialKeyValue = it.specialKeyValue
                    queryValueList.addToParameterValues(qev)
                } else {
                    CustomSQLValue csv = new CustomSQLValue()
                    csv.key = it.key
                    csv.value = it.value
                    queryValueList.addToParameterValues(csv)
                }
            }
            newPeriodicReportConfiguration.addToGlobalQueryValueLists(queryValueList)
        }
        configuration.executedTemplateQueries.each {
            TemplateQuery tq = new TemplateQuery(MiscUtil.getObjectProperties(it, TemplateQuery.propertiesToUseWhileCopying))
            createTemplateAndQuery(tq,it)
            tq.createdBy = owner
            tq.modifiedBy = owner
            tq.dateRangeInformationForTemplateQuery = new DateRangeInformation(MiscUtil.getObjectProperties(it.executedDateRangeInformationForTemplateQuery, DateRangeInformation.propertiesToUseWhileCopying))
            tq.dateRangeInformationForTemplateQuery.dateRangeEnum = DateRangeEnum.CUSTOM
            tq.dateRangeInformationForTemplateQuery.templateQuery = tq
            newPeriodicReportConfiguration.addToTemplateQueries(configurationService.copyBlankValues(tq, it.executedQueryValueLists, it.executedTemplateValueLists))
        }

        CRUDService.save(newPeriodicReportConfiguration)
    }

    private createTemplateAndQuery(TemplateQuery tq, ExecutedTemplateQuery executedTemplateQuery) {
        tq.template = executedConfigurationService.createReportTemplate(executedTemplateQuery.executedTemplate)
        tq.template.originalTemplateId = tq.template.id
        tq.template.save()
        if (executedTemplateQuery.executedQuery) {
            tq.query = queryService.createExecutedQuery(executedTemplateQuery.executedQuery)
            tq.query.originalQueryId = tq.query.id
            tq.query.save()
        }
    }

    private String generateUniqueName(BaseConfiguration config, User user, String prefix, String postfix) {

        String newName = configurationService.trimName(prefix, config.reportName, postfix)
        if ((config.instanceOf(ExecutedPeriodicReportConfiguration) && PeriodicReportConfiguration.findByReportNameAndOwnerAndIsDeleted(newName, user, false))
                || (config.instanceOf(ExecutedConfiguration) && Configuration.findByReportNameAndOwnerAndIsDeleted(newName, user, false))
                || (config.instanceOf(ExecutedIcsrReportConfiguration) && ExecutedIcsrReportConfiguration.findByReportNameAndOwnerAndIsDeleted(newName, user, false))) {
            int count = 1
            newName = configurationService.trimName(prefix, config.reportName + postfix, " ($count)")
            while ((config.instanceOf(ExecutedPeriodicReportConfiguration) && PeriodicReportConfiguration.countByReportNameIlikeAndOwnerAndIsDeleted(newName, user, false))
                    || (config.instanceOf(ExecutedConfiguration) && Configuration.countByReportNameIlikeAndOwnerAndIsDeleted(newName, user, false))
                    || (config.instanceOf(ExecutedIcsrReportConfiguration) && ExecutedIcsrReportConfiguration.countByReportNameIlikeAndOwnerAndIsDeleted(newName, user, false))) {
                newName = configurationService.trimName(prefix, config.reportName + postfix, " (${++count})")
            }
        }
        return newName
    }

    protected cloneErrorToResult(ComparisonQueue q, ExecutedReportConfiguration copy) {
        return new ComparisonResult(
                entityId1: q.entityId1,
                entityId2: copy?.id ?: 0,
                entityName1: q.entityName1,
                entityName2: copy?.reportName ?: "-",
                entityType: q.entityType,
                result: false,
                data: "{}",
                supported: false,
                message: q.message
        )
    }

    void compareJob() {
        ComparisonQueue.findAllByStatus(ComparisonQueue.Status.WAITING)?.collect { it }?.each {
            ExecutedReportConfiguration source = ExecutedReportConfiguration.get(it.entityId1)
            ReportConfiguration copyConfig = ReportConfiguration.get(it.entityId2)
            ExecutedReportConfiguration copy = ExecutedReportConfiguration.findByReportNameAndOwnerAndNumOfExecutions(copyConfig.reportName, copyConfig.owner, 1)
            if (copy && ExecutionStatus.findByExecutedEntityIdAndExecutionStatusNotInList(copy.id, ReportExecutionStatusEnum.getInProgressStatusesList())) {
                if (copy.status == ReportExecutionStatusEnum.ERROR) {
                    it.status = ComparisonQueue.Status.ERROR
                    it.message = ViewHelper.getMessage("app.comparison.copy.execution.error");
                    it.save(flush: true)
                    cloneErrorToResult(it, copy).save(flush: true, failOnError: true)
                } else {
                    try {
                        ComparisonResultDTO compareResult = compareReports(source, copy)

                        ComparisonResult result = new ComparisonResult(
                                entityId1: source.id,
                                entityId2: copy.id,
                                entityName1: it.entityName1,
                                entityName2: copy.reportName,
                                entityType: it.entityType,
                                result: compareResult.reportsAreEqual,
                                data: new JsonBuilder(compareResult).toPrettyString(),
                                supported: compareResult.supported,
                                message: compareResult.message
                        )
                        result.save()
                        it.status = ComparisonQueue.Status.COMPLETED
                        it.save(flush: true)
                    } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        PrintWriter pw = new PrintWriter(sw);
                        e.printStackTrace(pw);
                        it.status = ComparisonQueue.Status.ERROR
                        it.message = ViewHelper.getMessage("app.comparison.copy.comparison.error") + " " + sw.toString();
                        it.save(flush: true)
                        cloneErrorToResult(it, copy).save(flush: true, failOnError: true)
                    }
                }
            } else if ((!copy && !copyConfig.isEnabled) ||
                    (copy && !copyConfig.isEnabled && (!ExecutionStatus.findByExecutedEntityId(copy.id)))) {
                it.status = ComparisonQueue.Status.ERROR
                it.message = ViewHelper.getMessage("app.comparison.copy.execution.cancel");
                it.save(flush: true)
                cloneErrorToResult(it, null).save(flush: true, failOnError: true)
            }
        }

    }

    ComparisonResultDTO compareAndSave(ExecutedReportConfiguration source, ExecutedReportConfiguration copy) {
        ComparisonResultDTO compareResult = compareReports(source, copy)

        ComparisonResult result = new ComparisonResult(
                entityId1: source.id,
                entityId2: copy.id,
                entityName1: source.reportName + "_v" + source.numOfExecutions,
                entityName2: copy.reportName + "_v" + copy.numOfExecutions,
                entityType: source.class.getName(),
                result: compareResult.reportsAreEqual,
                data: new JsonBuilder(compareResult).toPrettyString(),
                supported: compareResult.supported,
                message: compareResult.message
        )
        result.save(flush: true)
        compareResult
    }
}