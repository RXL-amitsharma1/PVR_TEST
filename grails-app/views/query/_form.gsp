<%@ page import="com.rxlogix.config.Query; com.rxlogix.enums.QueryTarget; com.rxlogix.Constants;com.rxlogix.util.ViewHelper;grails.plugin.springsecurity.SpringSecurityUtils" %>
<g:hiddenField name="owner" id="owner" value="${query?.owner?.id ?: currentUser.id}"/>
<rx:container title="${fromEdit ? message(code: 'app.label.editQuery') : message(code: 'app.label.basic.information')}" bean="${query}">
<div class="row">
    <div class="col-xs-4">
        <div class="row">
            <div class="col-xs-12 ${hasErrors(bean: query, field: "name", "has-error")}">
                <label><g:message code="app.label.queryName"/><span class="required-indicator">*</span></label>
                <g:textField name="name"
                             maxlength="${Query.constrainedProperties.name.maxSize}"
                             placeholder="${g.message(code: 'input.name.placeholder')}"
                             value="${query?.name}"
                             class="form-control"/>
            </div>
        </div>

        <div class="row">
            <div class="col-xs-12">
                <label><g:message code="app.label.description"/></label>
                <g:render template="/includes/widgets/descriptionControl" model="[ value:query?.description, maxlength: Query.constrainedProperties.description.maxSize]"/>

            </div>
        </div>
    </div>

    <div class="col-xs-3">
        <div class="row">
            <div class="col-xs-12 ${hasErrors(bean: query, field: "tags", "has-error")}">
                <g:render template="/includes/widgets/tagsSelect2" model="['domainInstance': query]" />
            </div>
        </div>

        <div class="row">
            <div class="col-xs-12">
                <script>
                    sharedWithListUrl = "${createLink(controller: 'userRest', action: 'sharedWithList')}";
                    sharedWithValuesUrl = "${createLink(controller: 'userRest', action: 'sharedWithValues')}";
                    $(function () {
                        bindShareWith($('.sharedWithControl'), sharedWithListUrl, sharedWithValuesUrl, "100%")
                    });
                </script>
                <label><g:message code="shared.with"/></label>
                <g:set var="sharedWithValue" value="${ ((query?.shareWithGroups?.collect{Constants.USER_GROUP_TOKEN + it.id}?:[]) + (query?.shareWithUsers?.collect{Constants.USER_TOKEN + it.id}?:[]))?.join(";")}"/>
                <select class="sharedWithControl form-control" id="sharedWith" name="sharedWith" data-value="${sharedWithValue ?: Constants.USER_TOKEN+currentUser?.id}"></select>
            </div>
        </div>
    </div>



    <div class="col-xs-2">
        <sec:ifAnyGranted roles="ROLE_DEV">
            <div class="row">
                <label><g:message code="app.label.QueryType"/></label><br/>
                <g:select name="queryDropdown" id="queryDropdown" noSelection="${['': message(code: 'select.one')]}"
                          from="${ViewHelper.getQueryDropdownEnumI18n()}"
                          optionValue="display" optionKey="name"
                          value="${ViewHelper.getSelectedQueryType(query)}"
                          class="form-control"
                          disabled = "${SpringSecurityUtils.ifNotGranted("ROLE_DEV")}"/>
            </div>
        </sec:ifAnyGranted>

        <g:if test="${grailsApplication.config.pvsignal.url !=""}">
            <div class="row">
                <g:if test="${QueryTarget.getQueryTarget().size() > 1}">
                    <label><g:message code="app.label.queryTarget"/></label><br/>
                    <g:select name="queryTarget" id="queryTarget"
                              from="${QueryTarget.getQueryTarget()}"
                              optionValue="${{ msgKey -> message(code: msgKey.getI18nKey()) }}"
                              value="${query?.queryTarget?.name() ?: QueryTarget.REPORTS.name()}"
                              class="form-control"/>
                </g:if>
            </div>
        </g:if>
    </div>
    <div class="col-xs-2">
        <sec:ifAnyGranted roles="ROLE_ADMIN">
            <div class="row">
                <div class="col-xs-12">
                        <label><g:message code="app.label.owner"/></label>
                        <input disabled type="text" name="owner" class="form-control"
                               value="${query?.owner?.fullName ?: currentUser.fullName}"/>
                </div>
            </div>
        </sec:ifAnyGranted>

        <sec:ifAnyGranted roles="ROLE_QUALITY_CHECK">
            <div class="row" style="margin-top: 28px;">
                <div class="col-xs-12">
                    <div class="checkbox checkbox-primary">
                        <g:checkBox name="qualityChecked" value="${query?.qualityChecked}"
                                    checked=""/>
                        <label for="qualityChecked">
                            <g:message code="app.label.qualityChecked"/>
                        </label>
                    </div>
                </div>
            </div>
        </sec:ifAnyGranted>

        <sec:ifNotGranted roles="ROLE_DEV">
            <g:hiddenField name="nonValidCases" value="${query?.nonValidCases ? query?.nonValidCases : false}"/>
            <g:hiddenField name="icsrPadderAgencyCases" value="${query?.icsrPadderAgencyCases ? query?.icsrPadderAgencyCases : false}"/>
        </sec:ifNotGranted>
    </div>
</div>
</rx:container>
<div class="row">
    <div class="col-xs-12 col-lg-12">
        <g:render template="tabContent" model="['query':query, 'editable':editable, sourceProfiles:sourceProfiles,currentUser:currentUser]" />
    </div>
</div>
