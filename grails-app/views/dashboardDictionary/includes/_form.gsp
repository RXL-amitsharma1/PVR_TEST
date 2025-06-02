<g:set var="isMain" value="${dashboard?.dashboardType== com.rxlogix.enums.DashboardEnum.PVQ_MAIN ||dashboard?.dashboardType== com.rxlogix.enums.DashboardEnum.PVR_MAIN }"/>
<g:set var="isPublic" value="${dashboard?.dashboardType== com.rxlogix.enums.DashboardEnum.PVQ_PUBLIC ||dashboard?.dashboardType== com.rxlogix.enums.DashboardEnum.PVR_PUBLIC }"/>
<div class="row form-group">
    <div class="col-lg-6">
        <label for="label"><g:message code="app.label.dashboardDictionary.label" /><span class="required-indicator">*</span></label>
        <input id="label" name="label" value="${dashboard?.label}"  class="form-control" maxlength="255"/>
    </div>
</div>

<div class="row form-group">
    <div class="col-lg-6">
        <label for="dashboardType"><g:message code="app.label.dashboardDictionary.type" /><span class="required-indicator">*</span></label>
        <g:if test="${isMain}">
            <select   class="form-control " disabled>
                <option selected >${g.message(code: dashboard.dashboardType.i18nKey)}</option>
            </select>
            <input type="hidden" name="dashboardType" value="${dashboard.dashboardType}">
        </g:if>
        <g:else>
            <g:select name="dashboardType"
                  from="${com.rxlogix.util.ViewHelper.getDashboardEnum()}"
                  optionValue="display" optionKey="name"
                  value="${dashboard?.dashboardType}"
                  class="form-control "/>
        </g:else>
    </div>
</div>

<div class="row form-group">
    <div class="col-xs-6">
        <g:set var="sharedWithUsers" value="${dashboard?.sharedWith?.id}"/>
        <g:set var="sharedWithGroups" value="${dashboard?.sharedWithGroup?.id}"/>
        <label><g:message code="shared.with"/></label>
        <select id="sharedWith" name="sharedWith" multiple="true" class="form-control" autocomplete="off" ${isPublic?"":"disabled"}>
            <g:if test="${userGroups}">
                <optgroup label="${g.message(code: 'user.group.label')}">
                    <g:each in="${userGroups}" var="userGroup">
                        <option value="${userGroup.getReportRequestorKey()}" ${sharedWithGroups?.contains(userGroup.id) ? 'selected="selected"' : ''}>${userGroup.getReportRequestorValue()}</option>
                    </g:each>
                </optgroup>
            </g:if>
            <optgroup label="${g.message(code: 'user.label')}">
                <g:each in="${users}" var="user">
                    <option value="${user.getReportRequestorKey()}" ${sharedWithUsers?.contains(user.id) ? 'selected="selected"' : ''}>${user.getReportRequestorValue()}</option>
                </g:each>
            </optgroup>
        </select>
    </div>
</div>
