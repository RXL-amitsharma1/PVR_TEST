<g:if test="${actionName == 'create' || actionName == 'save' || actionName == 'run'}">
    <g:set var="createMode" value="${true}"/>
</g:if>

<g:if test="${actionName == 'edit' || actionName == 'update' || actionName == 'disable' || actionName == 'enable'}">
    <g:set var="editMode" value="${true}"/>
</g:if>


<g:render template="includes/profileConfigurationSection"
          model="[configurationInstance: configurationInstance, sourceProfiles: sourceProfiles, fieldProfiles: fieldProfiles, createMode: createMode]"/>

<g:render template="includes/reportConfigurationSection"
          model="[configurationInstance: configurationInstance, sourceProfiles: sourceProfiles]"/>

<g:render template="includes/reportSectionsSection" model="[configurationInstance: configurationInstance]"/>

<g:render template="includes/additionalDetailsSection" model="[configurationInstance: configurationInstance]"/>

<g:if test="${configurationInstance?.e2bDistributionChannel?.id}">
    <input type="hidden" name="e2bDistributionChannel.id" value="${configurationInstance?.e2bDistributionChannel?.id}"/>
</g:if>

<g:render template="includes/e2bSection"
          model="[configurationInstance: configurationInstance, e2bChannelSelected: configurationInstance.hasAnyE2BDistributionChannel(), e2bChannelSelectedAsEmail: configurationInstance.hasEmailAsE2BDistributionChannel()]"/>
