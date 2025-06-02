<%@ page import="com.rxlogix.config.ReportTemplate; grails.plugin.springsecurity.SpringSecurityUtils; com.rxlogix.Constants; com.rxlogix.config.ReportField; com.rxlogix.config.Category; com.rxlogix.enums.TemplateTypeEnum" %>

<g:hiddenField name="owner" id="owner" value="${reportTemplateInstance?.owner?.id ?: currentUser.id}"/>
<div class="row">
    <div class="col-xs-3">
        <div class="row">
            <div class="col-xs-12 ${hasErrors(bean: reportTemplateInstance, field: "name", "has-error")}">
                <label><g:message code="app.label.templateName" /><span class="required-indicator">*</span></label>
                <g:textField name="name"
                             placeholder="${g.message(code: 'input.name.placeholder')}"
                             maxlength="${ReportTemplate.constrainedProperties.name.maxSize}"
                             value="${reportTemplateInstance?.name}"
                             class="form-control" readonly="${ciomsITemplate}"/>
            </div>
        </div>
        <div class="row">
            <div class="col-xs-12">
                <label><g:message code="app.label.description" /></label>

                <g:render template="/includes/widgets/descriptionControl" model="[value:reportTemplateInstance?.description, maxlength: ReportTemplate.constrainedProperties.description.maxSize]"/>

            </div>
        </div>
    </div>
    <div class="col-xs-3">
        <div class="row">
            <div class="col-xs-12">
                <label><g:message code="app.label.category" /></label>
                <g:select id="category"
                          name="category.id"
                          from="${Category.list()}"
                          value="${reportTemplateInstance?.category?.id}"
                          optionKey="id"
                          optionValue="name"
                          class="form-control"/>
                <g:hiddenField name="categoryVal" id="categoryVal" value="${reportTemplateInstance?.category?.id?:null}"/>
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
                <g:set var="sharedWithValue" value="${ ((reportTemplateInstance?.shareWithGroups?.collect{Constants.USER_GROUP_TOKEN + it.id}?:[]) + (reportTemplateInstance?.shareWithUsers?.collect{Constants.USER_TOKEN + it.id}?:[]))?.join(";")}"/>
                <select class="sharedWithControl form-control" id="sharedWith" name="sharedWith" data-value="${sharedWithValue ?: Constants.USER_TOKEN+currentUser?.id}"></select>
            </div>
        </div>
    </div>

    <div class="col-xs-3">
        <sec:ifAnyGranted roles="ROLE_ADMIN">
            <div class="row">
                <div class="col-xs-12">
                    <label><g:message code="app.label.owner"/></label>
                    <input disabled type="text" name="owner" class="form-control"
                           value="${reportTemplateInstance?.owner?.fullName ?: currentUser.fullName}"/>
                </div>
            </div>
        </sec:ifAnyGranted>
        <div class="row" style="margin-top: 7px">
                <sec:ifAnyGranted roles="ROLE_QUALITY_CHECK">
                    <div class="col-md-6 ">
                        <div class="checkbox checkbox-primary">
                            <g:checkBox name="qualityChecked" value="${reportTemplateInstance?.qualityChecked}" checked=""/>
                            <label for="qualityChecked" class="no-bold add-cursor">
                                <g:message code="app.label.qualityChecked"/>
                            </label>
                        </div>
                    </div>
                </sec:ifAnyGranted>
                <g:if test="${reportTemplateInstance.templateType == TemplateTypeEnum.CASE_LINE || reportTemplateInstance.templateType == TemplateTypeEnum.DATA_TAB || reportTemplateInstance.templateType == TemplateTypeEnum.CUSTOM_SQL || reportTemplateInstance.templateType == TemplateTypeEnum.NON_CASE }">
                    <div class="col-md-6 ">
                        <div class="checkbox checkbox-primary">
                            <g:checkBox name="interactiveOutput" value="${reportTemplateInstance?.interactiveOutput}" checked="${reportTemplateInstance?.interactiveOutput}" />
                            <label for="interactiveOutput" class="no-bold add-cursor">
                                <g:message code="app.label.outputInteractive"/>
                            </label>
                        </div>
                    </div>
                </g:if>
            </div>
    </div>
    <div class="col-xs-3">
        <div class="row">
            <div class="col-xs-12">
                <g:render template="/includes/widgets/tagsSelect2" model="['domainInstance': reportTemplateInstance]" />
            </div>
        </div>
    </div>
    <g:if test="${reportTemplateInstance.templateType != TemplateTypeEnum.ICSR_XML}">
        <sec:ifAnyGranted roles="ROLE_DEV">
            <div class="col-xs-3">
                <div class="row">
                    <div class="col-xs-12">
                        <label><g:message code="app.label.fixedTemplate"/></label>
                        <input id="fixedTemplateName" readonly="true" type="text" name="fixedTemplateName" class="form-control"
                               value="${reportTemplateInstance?.fixedTemplate?.data ? reportTemplateInstance.fixedTemplate.name : ''}"/>
                        <input id="fixedTemplateFile" type="file" name="fixedTemplateFile" style="visibility: hidden; margin-bottom: 0px; height: 0px"/>
                    </div>
                </div>
                <div class="row">
                    <div class="col-xs-12 m-t-5" style="margin-left:2.5px;">
                        <div class="checkbox checkbox-primary">
                            <g:checkBox name="useFixedTemplate"
                                        value="${reportTemplateInstance?.useFixedTemplate}"
                                        checked="${reportTemplateInstance?.useFixedTemplate}" disabled="${ciomsITemplate}"/>
                            <label for="useFixedTemplate">
                                <g:message code="app.label.useFixedTemplate"/>
                            </label>
                        </div>
                        <button type="button" class="btn btn-primary fileupload-new fixedTemplateButton" id="browseFixedTemplate"><g:message code="app.label.browse"/></button>
                        <button type="button" class="btn btn-primary fileupload-exists fixedTemplateButton" data-dismiss="fileupload" id="deleteFixedTemplate" ${(ciomsITemplate)?"disabled":""}><g:message code="app.label.delete"/></button>
                        <g:if test="${actionType != 'save'}">
                            <g:link class="btn btn-primary fixedTemplateButton downloadFixedTemplate" action="getFixedTemplateFile" id="${reportTemplateInstance.id}"><g:message code="app.label.download"/></g:link>
                        </g:if>
                    </div>
                </div>
            </div>
        </sec:ifAnyGranted>
    </g:if>
</div>
<div class="row">
    <div class="col-xs-12 col-lg-12">
        <g:render template="includes/templateType"
                  model="['reportTemplateInstance': reportTemplateInstance, currentUser: currentUser, selectedLocale: selectedLocale, ciomsITemplate: ciomsITemplate, sourceProfiles: sourceProfiles]"/>
    </div>
</div>