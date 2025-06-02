<g:if test="${actionName == 'create' || actionName == 'save' || actionName == 'run'}" >
    <g:set var="createMode" value="${true}"/>
</g:if>

<g:if test="${actionName == 'edit' || actionName == 'update' || actionName == 'disable' || actionName == 'enable'}" >
    <g:set var="editMode" value="${true}"/>
</g:if>

<g:render template="includes/selectionCriteria" model="[configurationInstance: configurationInstance, sourceProfiles: sourceProfiles, editMode: editMode]"/>

<g:render template="includes/queryComplianceSection" model="[configurationInstance: configurationInstance]"/>

<g:render template="includes/senderDetailsSection" model="[configurationInstance: configurationInstance]"/>