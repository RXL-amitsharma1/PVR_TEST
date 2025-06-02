<%@ page import="grails.util.Holders; grails.gorm.multitenancy.Tenants" %>
<g:if test="${Holders.config.get('pvreports.multiTenancy.enabled')}">
    <sec:ifAnyGranted roles="ROLE_DEV">
        <label><g:message code="app.label.tenant"/></label>

        <div>
            <g:selectTenant name="tenantId" id="tenantDropDown"
                            value="${(configurationInstance?.tenantId) ?: (Tenants.currentId() as Long)}"
                            optionValue="displayName" optionKey="id"
                            class="form-control tenantDropDown"/>
        </div>
    </sec:ifAnyGranted>
    <sec:ifNotGranted roles="ROLE_DEV">
        <g:hiddenField name="tenantId" value="${(configurationInstance?.tenantId) ?: (Tenants.currentId() as Long)}"/>
    </sec:ifNotGranted>
</g:if>
<g:else>
    <g:hiddenField name="tenantId"
                   value="${(configurationInstance?.tenantId) ?: (Holders.config.get('pvreports.multiTenancy.defaultTenant') as Long)}"/>
</g:else>