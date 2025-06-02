<g:if test="${actionName == 'create' || actionName == 'save' || actionName == 'run'}">
    <g:set var="createMode" value="${true}"/>
</g:if>

<g:if test="${actionName == 'edit' || actionName == 'update' || actionName == 'disable' || actionName == 'enable' || actionName == 'editConfig'}">
    <g:set var="editMode" value="${true}"/>
</g:if>

<g:set var="userService" bean="userService"/>
<g:set var="currentUser" value="${userService.currentUser}"/>
<g:javascript>
    var isUserBlinded = ${currentUser.isBlinded};
    var isUserRedacted = ${currentUser.isProtected};
</g:javascript>

<g:render template="/periodicReport/includes/reportConfigurationSection" model="[configurationInstance: configurationInstance, editMode: editMode]"/>

<g:render template="/configuration/includes/reportSectionsSection"
          model="[configurationInstance: configurationInstance, isForPeriodicReport: true]"/>

<g:render template="/configuration/includes/reportDetailsSection"
          model="[configurationInstance: configurationInstance, isForPeriodicReport: true, allPublisherContributors: allPublisherContributors]"/>

<g:render template="/configuration/includes/deliveryOptionsSection"
          model="[configurationInstance: configurationInstance, isForPeriodicReport: true]"/>