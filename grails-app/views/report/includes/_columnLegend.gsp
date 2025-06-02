<%@ page import="org.grails.orm.hibernate.cfg.GrailsHibernateUtil;" %>
<g:each var="executedTemplateQuery"
           in="${GrailsHibernateUtil.unwrapIfProxy(executedConfigurationInstance.executedTemplateQueries)}">
    <g:each var="reportFieldInfo"
        in="${(GrailsHibernateUtil.unwrapIfProxy(executedTemplateQuery.executedTemplate)).getAllSelectedFieldsInfo()}">
        <g:set var="legend" value="${reportFieldInfo?.newLegendValue ?: message(code: "app.reportField.${reportFieldInfo?.reportField?.name}.label.legend", default: null)}"/>
        <g:if test="${legend}">
            <g:hiddenField name="${reportFieldInfo?.reportField?.name}Legend" value="${legend}"/>
        </g:if>
    </g:each>
</g:each>