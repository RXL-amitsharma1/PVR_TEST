<%@ page import="org.grails.orm.hibernate.cfg.GrailsHibernateUtil;" %>
<g:set var="executedConfigurationInstance" value="${reportResult.executedTemplateQuery.executedConfiguration}"/>
<g:set var="executedTemplateInstance" value="${GrailsHibernateUtil.unwrapIfProxy(reportResult.executedTemplateQuery.executedTemplate)}"/>
<g:set var="action" value="show"/>
<g:set var="id" value="${reportResult.executedTemplateQuery.executedConfiguration.id}"/>

<g:if test="${reportResult.executedTemplateQuery.isVisible()}">
    <g:render template="show"
          model="[executedConfigurationInstance: executedConfigurationInstance, hasRemovedCases: hasRemovedCases,
                  executedTemplateInstance: executedTemplateInstance, filename: reportName, action: action, id: id,
                  reportType: reportType, reportResult: reportResult, isLargeReportResult: isLargeReportResult,
                  isPeriodicReport: isPeriodicReport, caseNumberFieldName: executedConfigurationInstance.sourceProfile.caseNumberFieldName,
                  configurationInstance: configurationInstance, sourceProfiles: sourceProfiles, configType: configType, editable: editable,
                  ciomsITemplateId: ciomsITemplateId,warningMsg: warningMsg, isNuprCsv: isNuprCsv, includeCaseNumber: includeCaseNumber, showNuprCaseNumCheckbox: showNuprCaseNumCheckbox]"/>

                                                      configurationInstance: configurationInstance, sourceProfiles: sourceProfiles, configType: configType, editable: editable]"/>
</g:if>
<g:else>
    <span class="fa fa-ban" title="${g.message(code: 'app.label.PublisherTemplate.error.file.message')}"></span> ${attachment.name}
</g:else>