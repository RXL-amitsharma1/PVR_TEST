<%@ page import="com.rxlogix.util.ViewHelper" %>
<g:if test="${deliveryOption?.emailToUsers}">
    <div class="row">
        <div class="col-xs-12">
            <g:if test = "${instance?.emailConfiguration!=null && !instance?.emailConfiguration.isDeleted}">
                <label><g:message code="app.label.emailTo"/>  <asset:image src="/icons/email-secure.png"/></label>
            </g:if>
            <g:else>
                <label><g:message code="app.label.emailTo"/> <asset:image src="/icons/email.png"/></label>
            </g:else>
            <g:each in="${deliveryOption?.emailToUsers}">
                <div>${it}</div>
            </g:each>
        </div>
    </div>

    <g:if test="${!instance.emailConfiguration?.isDeleted && instance.emailConfiguration?.cc}">
        <div class="row">
            <div class="col-xs-12">
                <label><g:message code="app.label.emailConfiguration.cc"/></label>
                <g:each in="${instance.emailConfiguration?.cc?.split(',')}">
                    <div>${it}</div>
                </g:each>
            </div>
        </div>
    </g:if>
    <g:if test="${deliveryOption?.additionalAttachments}">
        <div class="row">
            <div class="col-xs-12">
                <table width="100%">
                    <tr>
                        <th align="center"><g:message code="file.format"/></th>
                        <th align="center"><g:message code="app.label.reportSections"/></th>
                    </tr>
                    <g:each var="attach" in="${grails.converters.JSON.parse(deliveryOption?.additionalAttachments)}">
                        <tr>
                            <td>${attach.formats.join(";")}</td>
                            <g:if test="${instance instanceof com.rxlogix.config.ExecutedReportConfiguration}">
                                <td>${attach.sections.collect { ((it as Integer) < instance?.executedTemplateQueries?.size()) ? instance?.executedTemplateQueries?.get(it as Integer)?.executedTemplate?.name : " - " }.join(";")}</td>
                            </g:if>
                            <g:else>
                                <td>${attach.sections.collect { ((it as Integer) < instance?.templateQueries?.size()) ? instance?.templateQueries?.get(it as Integer)?.template?.name : " - " }.join(";")}</td>
                            </g:else>
                        </tr>
                    </g:each>
                </table>
            </div>
        </div>
    </g:if>
    <g:else>
        <g:if test="${!(instance instanceof com.rxlogix.config.IcsrReportConfiguration)}">
        <div class="row">
            <div class="col-xs-12">
                <div><label><g:message code="email.attachment.format"/></label></div>
                ${deliveryOption?.attachmentFormats?.join(", ")}
            </div>
        </div>
        </g:if>
    </g:else>
</g:if>