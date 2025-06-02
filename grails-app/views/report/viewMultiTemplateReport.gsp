<%@ page import="com.rxlogix.enums.CommentTypeEnum" %>
<g:set var="action" value="viewMultiTemplateReport"/>

<g:render template="show"
          model="[executedConfigurationInstance: executedConfigurationInstance, filename: reportName, action: action,configurationInstance : configurationInstance, configType: configType,
                  id: executedConfigurationInstance?.id, reportType: reportType,sourceProfiles: sourceProfiles,
                  comments: executedConfigurationInstance.comments, commentAssociatedWitId: executedConfigurationInstance.id,
                  commentType: CommentTypeEnum.EXECUTED_CONFIGURATION, isLargeReportResult: isLargeReportResult,isPeriodicReport: isPeriodicReport, ciomsITemplateId: ciomsITemplateId, isNuprCsv: isNuprCsv, includeCaseNumber: includeCaseNumber, showNuprCaseNumCheckbox: showNuprCaseNumCheckbox]"/>
