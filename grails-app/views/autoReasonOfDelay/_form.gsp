<g:if test="${actionName == 'create' || actionName == 'save' || actionName == 'run'}" >
    <g:set var="createMode" value="${true}"/>
</g:if>

<g:if test="${actionName == 'edit' || actionName == 'update' || actionName == 'disable' || actionName == 'enable'}" >
    <g:set var="editMode" value="${true}"/>
</g:if>

<g:render template="/autoReasonOfDelay/includes/selectionCriteria" model="[autoReasonOfDelayInstance: autoReasonOfDelayInstance, sourceProfiles: sourceProfiles]"/>

<g:render template="/autoReasonOfDelay/includes/reasonOfDelaySection" model="[autoReasonOfDelayInstance: autoReasonOfDelayInstance]"/>

<g:render template="/autoReasonOfDelay/includes/autoRODSchedular" model="[autoReasonOfDelayInstance: autoReasonOfDelayInstance]"/>