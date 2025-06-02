package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.dynamicReports.reportTypes.TemplateSetCsvDataSource
import com.rxlogix.enums.AuditLogCategoryEnum
import com.rxlogix.enums.ReportResultStatusEnum
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.util.AuditLogConfigUtil
import com.rxlogix.util.MiscUtil
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import groovy.json.JsonSlurper
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.io.IOUtils

import java.util.zip.GZIPInputStream

class ReportService {
    def userService

    ExecutedReportConfiguration findExecutedReportConfigurationById(Long id) {
        return id ? ExecutedReportConfiguration.read(id) : null
    }

    @Transactional
    def changeReportResultStatus(def reportStatus, User user, ExecutedReportConfiguration report) {
        ReportResultStatusEnum status = ReportResultStatusEnum.valueOf(reportStatus)
        if (status) {
            SharedWith shared = SharedWith.findByUserAndExecutedConfiguration(user, report)
            shared.status = status
            shared.save()
        }
    }

    @Transactional
    def deleteExecutedConfig(User user, ExecutedReportConfiguration executedConfiguration) {
        if (executedConfiguration) {
            ExecutedReportUserState state = ExecutedReportUserState.findByUserAndExecutedConfiguration(user, executedConfiguration)
            if (!state) {
                state = new ExecutedReportUserState(user: user, executedConfiguration: executedConfiguration, isArchived: false)
            }
            state.isDeleted = true
            state.save()
        }
    }

    @Transactional
    boolean toggleIsArchived(User user, ExecutedReportConfiguration executedConfiguration) {
        if (executedConfiguration) {
            ExecutedReportUserState state = ExecutedReportUserState.findByUserAndExecutedConfiguration(user, executedConfiguration)
            if (!state) {
                state = new ExecutedReportUserState(user: user, executedConfiguration: executedConfiguration, isDeleted: false, isArchived: executedConfiguration.archived as Boolean)
            }
            state.isArchived = !state.isArchived
            state.save()
            return state.isArchived
        }
        return false
    }

    Map<String, ?> getOutputJSONROW(ReportResultData resultData, int rowId){
        InputStream is = new GZIPInputStream(new BufferedInputStream(new ByteArrayInputStream(resultData?.decryptedValue)))
        return new JsonSlurper().parse(is).get(rowId)
    }

    List<Tuple2<String, String>> getCrosstabCaseIds(Map<String, ?> row, String columnName) {
        String clColumnName = "CL_${columnName}"
        String value = row.get(clColumnName)
        List<Tuple2<String, String>> caseIds = []
        if (value) {
            // Single column
            addCaseIds(caseIds, value)
        }
        return caseIds
    }

    private void addCaseIds(List<Tuple2<String, String>> caseIds, String value) {
        value?.split(",").collect {
            List pair = it.trim().split("\\\$")
            caseIds.add(new Tuple2(pair[0] ?: '', pair[1] ?: ''))
        }
    }

    def setFavorite(ExecutedReportConfiguration executedConfiguration, Boolean isFavorite) {
        User user = userService.getUser()
        if (executedConfiguration) {
            ExecutedReportUserState state = ExecutedReportUserState.findByUserAndExecutedConfiguration(user, executedConfiguration)
            if (!state) {
                state = new ExecutedReportUserState(user: user, executedConfiguration: executedConfiguration, isDeleted: false, isArchived: false)
            }
            state.isFavorite = isFavorite ? true : null
            state.save(flush: true)
        }
    }

    List<Tuple2<String, Integer>> getCaseNumberAndVersions(ReportResult reportResult) {
        ReportTemplate executedTemplate = reportResult.executedTemplateQuery.executedTemplate
        List<Tuple2<String, Integer>> result
        byte[] value = reportResult?.data?.decryptedValue
        if (value) {
            if (executedTemplate instanceof CaseLineListingTemplate) {
                CaseLineListingTemplate cllTemplate = (CaseLineListingTemplate) executedTemplate
                String[] columnNamesList = cllTemplate.fieldNameWithIndex
                String caseNumberColumnName = columnNamesList.find {
                    it.startsWith(reportResult.sourceProfile.caseNumberFieldName)
                }
                String versionNumberColumnName = columnNamesList.find { it.startsWith("masterVersionNum") }
                if (caseNumberColumnName) {
                    result = []
                    Reader reader = new InputStreamReader(new GZIPInputStream(new ByteArrayInputStream(value)))
                    reader.withReader {
                        CSVParser csvParser = CSVFormat.DEFAULT.withHeader(columnNamesList).parse(reader)
                        csvParser.getRecords().each { row ->
                            String caseNumber = row.get(caseNumberColumnName)
                            Integer versionNumber = null
                            if (versionNumberColumnName) {
                                versionNumber = (row.get(versionNumberColumnName)?.isInteger()) ? row.get(versionNumberColumnName).toInteger() : null
                            }
                            result.push(new Tuple2(caseNumber, versionNumber))
                        }
                    }
                }
            } else if (executedTemplate instanceof CustomSQLTemplate && executedTemplate.ciomsI) {
                result = []
                CustomSQLTemplate ciomsTemplate = (CustomSQLTemplate) executedTemplate
                Reader reader = new InputStreamReader(new GZIPInputStream(new ByteArrayInputStream(value)))
                reader.withReader {
                    String caseNumberColumnName = ReportResultService.CIOMS_I_CASE_NUMBER_FIELD_NAME
                    String[] columnNamesList = JSON.parse(ciomsTemplate?.columnNamesList)
                    CSVParser csvParser = CSVFormat.DEFAULT.withHeader(columnNamesList).parse(reader)
                    csvParser.getRecords().each { row ->
                        String caseNumber = row.get(caseNumberColumnName)
                        Integer versionNumber = null
                        result.push(new Tuple2(caseNumber, versionNumber))
                    }
                }
            } else if (executedTemplate instanceof XMLTemplate) {
                result = []
                TarArchiveInputStream tarArchiveInputStream = null
                TarArchiveInputStream subreportsInputStream = null
                try {
                    tarArchiveInputStream = new TarArchiveInputStream(new GZIPInputStream(new BufferedInputStream(new ByteArrayInputStream(reportResult?.data?.decryptedValue))));
                    while (tarArchiveInputStream.nextEntry != null) {
                        if (tarArchiveInputStream.currentEntry.name.endsWith('.tar.gz')) {
                            subreportsInputStream = new TarArchiveInputStream(new GZIPInputStream(tarArchiveInputStream));
                            TarArchiveEntry entry = subreportsInputStream.getNextTarEntry()
                            while (entry != null) {
                                String fileName = entry.name
                                if (fileName.contains("/")) {
                                    fileName = fileName.substring(fileName.indexOf("/") + 1)
                                }
                                if (fileName == TemplateSetCsvDataSource.GROUPING_FILE_NAME) {
                                    String jsonText = new InputStreamReader(new ByteArrayInputStream(IOUtils.toByteArray(subreportsInputStream))).text
                                    def groupingData = JSON.parse(jsonText)
                                    result.push(new Tuple2(String.valueOf(groupingData.collect { k, v -> v }.join()), null))
                                }
                                entry = subreportsInputStream.getNextTarEntry();
                            }
                        }
                    }
                } finally {
                    subreportsInputStream?.close()
                    tarArchiveInputStream?.close()
                }
            }
        }
        return result
    }

    Long updateExecutedConfiguration(ExecutedReportConfiguration executedReportConfiguration, List<String> sharedWithUser, List<String> sharedWithGroup) {
        sharedWithUser.each {
            User user = User.findByUsernameIlike(it)
            if(user && !(executedReportConfiguration.executedDeliveryOption.sharedWith.find { it.id == user.id })) {
                executedReportConfiguration.executedDeliveryOption.addToSharedWith(user)
            }
        }
        sharedWithGroup.each {
            UserGroup group = UserGroup.findByName(it)
            if(group && !executedReportConfiguration.executedDeliveryOption.sharedWithGroup.contains(group)) {
                executedReportConfiguration.executedDeliveryOption.addToSharedWithGroup(group)
            }
        }
        executedReportConfiguration.save(flush: true, failOnError: true)
        return executedReportConfiguration.id
    }

    def updateAuditLogShareWith(def theInstance, List<User> oldUserList, List<UserGroup> oldGroupList, def parentObject, boolean isCaseSeries) {
        // Only proceed with auditing if there are changes in shared users or groups
        if (oldUserList == theInstance.sharedWith && oldGroupList == theInstance.sharedWithGroup) {
            return
        }
        AuditLogConfigUtil.logChanges(parentObject,
                [
                        sharedWith     : theInstance.sharedWith?.collect { it.getFullNameAndUserName() }?.join(",") ?: "",
                        sharedWithGroup: theInstance.sharedWithGroup?.collect { it.name }?.join(",") ?: ""
                ],
                [
                        sharedWith     : oldUserList?.collect { it.getFullNameAndUserName() }?.join(",") ?: "",
                        sharedWithGroup: oldGroupList?.collect { it.name }?.join(",") ?: ""
                ], Constants.AUDIT_LOG_UPDATE)

    }

    List updateChangesForShare(def theInstance, List<User> userList, List<UserGroup> groupList, String description, def parentObject) {
        List changesMade = []
        if (userList != theInstance.sharedWith){
            changesMade << [fieldName: ViewHelper.getMessage("app.label.generatedReports.sharedWith"),
                            entityName: description,
                            originalValue: userList ?: Constants.AuditLog.NO_VALUE, newValue: theInstance.sharedWith ?: Constants.AuditLog.NO_VALUE,
                            entityId: parentObject.id]
        }

        if(groupList != theInstance.sharedWithGroup){
            changesMade << [fieldName: ViewHelper.getMessage("app.label.generatedReports.sharedWithGroup"),
                            entityName: description,
                            originalValue: groupList ?: Constants.AuditLog.NO_VALUE, newValue: theInstance.sharedWithGroup ?: Constants.AuditLog.NO_VALUE,
                            entityId: parentObject.id]
        }
        return changesMade
    }

}
