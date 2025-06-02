<g:if test="${actionName == 'create' || actionName == 'save' || actionName == 'run'}">
    <g:set var="createMode" value="${true}"/>
</g:if>

<g:if test="${actionName == 'edit' || actionName == 'update' || actionName == 'disable' || actionName == 'enable'|| actionName == 'editConfig'}">
    <g:set var="editMode" value="${true}"/>
</g:if>

<g:render template="includes/reportConfigurationSection" model="[configurationInstance: configurationInstance, sourceProfiles: sourceProfiles, editMode: editMode]"/>

<g:render template="/configuration/includes/reportSectionsSection"
          model="[configurationInstance: configurationInstance, isForPeriodicReport: false, isForIcsrReport: true]"/>

<g:render template="/configuration/includes/reportDetailsSection"
          model="[configurationInstance: configurationInstance, isForPeriodicReport: false, isForIcsrReport: true]"/>

<g:render template="/configuration/includes/deliveryOptionsSection"
          model="[configurationInstance: configurationInstance, isForPeriodicReport: false, isForIcsrReport: true]"/>