<%@ page import="com.rxlogix.config.publisher.PublisherTemplateParameter; com.rxlogix.config.publisher.PublisherTemplate"%>

<g:javascript>
        var fetchParametersUrl="${createLink(controller: 'publisherTemplate', action: 'fetchParameters')}";
        var fetchParametersOneDriveUrl="${createLink(controller: 'publisherTemplate', action: 'fetchParametersFromOneDrive')}";
</g:javascript>
<input type="hidden" name="parameters.hidden" />
<div class="row form-group">
    <div class="alert alert-danger alert-dismissible forceLineWrap errorDiv" role="alert" hidden="hidden" id="errorDiv">
        <button type="button" class="close" name="pubDocTemplateButton">
            <span aria-hidden="true">&times;</span>
            <span class="sr-only"><g:message code="default.button.close.label"/></span>
        </button>
        <p class="errorContent"></p>
    </div>
    <div class="alert alert-warning alert-dismissible forceLineWrap warningDiv" role="alert" hidden="hidden" id="warningDiv">
        <button type="button" class="close" name="pubDocWarningButton">
            <span aria-hidden="true">&times;</span>
            <span class="sr-only"><g:message code="default.button.close.label"/></span>
        </button>
        <p class="warningContent docWarning" hidden="hidden"></p>
        <p class="warningContent parametersWarning" hidden="hidden"></p>
    </div>
    <div class="col-lg-6">
        <label for="name"><g:message code="app.label.name" /><span class="required-indicator">*</span></label>
        <input required id="name" name="name" value="${instance?.name}"  maxlength="${PublisherTemplate.constrainedProperties.name.maxSize}" class="form-control"/>
    </div>
</div>

<div class="row form-group">
    <div class="col-lg-6">
        <label for="description"><g:message code="app.label.description" /></label>
        <g:textField id="description" name="description" value="${instance?.description}" maxlength="${PublisherTemplate.constrainedProperties.description.maxSize}" class="form-control"/>
    </div>
</div>

<div class="row form-group">
    <div class="col-lg-6">
         <sec:ifAnyGranted roles="ROLE_QUALITY_CHECK">
            <div class="checkbox checkbox-primary">
                <g:checkBox name="qualityCheck" id="qualityCheck"
                            value="${instance?.qualityChecked}"
                            />
                <label for="qualityCheck">
                        <g:message code="app.label.qualityChecked"/>
                </label>
            </div>
        </sec:ifAnyGranted>
    </div>
</div>

<div class="row form-group form-lg">
    <div class="col-lg-5">
        <label for="description"><g:message code="app.label.publisher.word.attachment" /><span class="required-indicator">*</span></label>
        <div class="input-group" style="width: 100%;">
            <g:textField name="file_name" class="form-control" id="file_name" value="${instance?.fileName}" readonly="readonly" />
            <label class="input-group-btn">
                <span class="btn btn-primary inputbtn-height">
                    <g:message code="app.label.attach"/>
                    <input type="file" id="file_input" accept=".doc,.docx,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document" name="file" value="${instance?.template}" style="display: none;">
                </span>
            </label>
        </div>
    </div>
    <g:if test="${grailsApplication.config.officeOnline.enabled && (params.action=="edit")}">
        <div class="col-lg-1 width-auto">
            <a style="margin-top: 25px" class='btn btn-primary oneDriveEditorLink' title="${message(code: "default.button.edit.label")}" href="javascript:void(0)" data-entity="PublisherTemplate" data-id="${instance.id}">
                <span class="md md-pencil " style="cursor: pointer"></span>
            </a>
        </div>
    </g:if>
    <div class="col-lg-3">
        <button class="btn btn-primary mt-25" disabled type="button" id="fetchButton"><g:message code="app.label.PublisherTemplate.fetchParameters" default="Fetch Params"/></button>
        <button class="btn btn-primary mt-25" disabled type="button" id="fetchButtonOneDrive" style="display: none"><g:message code="app.label.PublisherTemplate.fetchParameters" default="Fetch Params"/></button>
        <button class="btn btn-primary ml-7 mt-25" type="button" id="clearParamButton"><g:message code="app.label.PublisherTemplate.clearParameters" default="Clear Parameters"/></button>
    </div>
</div>

<div class="row form-group">
    <div class="pv-caselist" style="padding-top: 10px; padding-bottom: 10px">
    <table width="100%" class="table parameterTable table-striped pv-list-table dataTable no-footer">
        <thead>
            <th><label><g:message code="app.label.PublisherTemplate.PublisherTemplateParameter.hidden"/></label></th>
            <th><label><g:message code="app.label.PublisherTemplate.PublisherTemplateParameter.name"/></label></th>
            <th><label><g:message code="app.label.PublisherTemplate.PublisherTemplateParameter.type"/></label></th>
            <th><label><g:message code="app.label.PublisherTemplate.PublisherTemplateParameter.title"/></label></th>
            <th> <label><g:message code="app.label.PublisherTemplate.PublisherTemplateParameter.description"/></label></th>
            <th><label><g:message code="app.label.PublisherTemplate.PublisherTemplateParameter.value"/></label>
                <span class="fa fa-question-circle modal-link" style="cursor:pointer" data-toggle="modal" data-target="#publisherHelpModal"></span>
            </th>
        </thead>
            <tr class="parameterTemplateRow" style="display: none;background-color:#FFFFFF">
            <td class="parameterTemplateRowCheckbox">
              <div class="checkbox checkbox-primary text-center">
                  <g:checkBox name="hidden" disabled="disabled" value=""/>
                <label for="hidden">
                </label>
              </div>
            </td>
            <td><g:textField name="parameters.name" value=""  readOnly="readonly" class="form-control"/></td>
            <td><select name="parameters.type" class="form-control parameterTypeSelect">
                <option value="${PublisherTemplateParameter.Type.TEXT}">${message(code:"app.PublisherTemplateParameter.Type."+PublisherTemplateParameter.Type.TEXT.name())+"/"+message(code:"app.PublisherTemplateParameter.Type."+PublisherTemplateParameter.Type.CODE.name())}</option>
                <option value="${PublisherTemplateParameter.Type.QUESTIONNAIRE}">${message(code:"app.PublisherTemplateParameter.Type."+PublisherTemplateParameter.Type.QUESTIONNAIRE.name())}</option>
            </select>
            </td>
            <td><g:textField name="parameters.title" value=""  class="form-control" maxlength="4000"/></td>
            <td><g:textField name="parameters.description" value=""  class="form-control" maxlength="4000"/></td>
            <td><g:textField name="parameters.value" value=""  class="form-control" maxlength="4000"/>
                <span class="md md-pencil pv-cross editQuestButton" style="display: none"></span>
            </td>
        </tr>
    <g:each in="${instance?.parameters}" var="parameter">
        <tr class="parameterTemplateRow">
            <td class="parameterTemplateRowCheckbox">
                <div class="checkbox checkbox-primary text-center">
                    <g:if test="${parameter.value && parameter.value.trim().size() > 0}">
                        <g:checkBox name="hidden" id="hidden"  value="${parameter?.hidden}" />
                    </g:if>
                    <g:else>
                        <g:checkBox name="hidden" id="hidden"  value="${parameter?.hidden}" disabled="true"/>
                    </g:else>
                <label for="hidden"></label>
                </div>
             </td>
            <td><g:textField name="parameters.name" value="${parameter.name}" readOnly="readonly" class="form-control" /></td>
            <td><select name="parameters.type" class="form-control parameterTypeSelect">
                <option ${parameter.type==PublisherTemplateParameter.Type.TEXT?"selected":""} value="${PublisherTemplateParameter.Type.TEXT}">${message(code:"app.PublisherTemplateParameter.Type."+PublisherTemplateParameter.Type.TEXT.name())+"/"+message(code:"app.PublisherTemplateParameter.Type."+PublisherTemplateParameter.Type.CODE.name())}</option>
                <option ${parameter.type==PublisherTemplateParameter.Type.QUESTIONNAIRE?"selected":""} value="${PublisherTemplateParameter.Type.QUESTIONNAIRE}">${message(code:"app.PublisherTemplateParameter.Type."+PublisherTemplateParameter.Type.QUESTIONNAIRE.name())}</option>
            </select>
            </td>
            <td><g:textField name="parameters.title" value="${parameter.title}"  class="form-control" /></td>
            <td><g:textField name="parameters.description" value="${parameter.description}"  class="form-control" /></td>
            <td><g:textField name="parameters.value" value="${parameter.value}"  class="form-control"  style="${parameter.type==PublisherTemplateParameter.Type.QUESTIONNAIRE?'display: none':''}" />
                <span class="md md-pencil pv-cross editQuestButton" style="${parameter.type!=PublisherTemplateParameter.Type.QUESTIONNAIRE?'display: none':''}"></span></td>
        </tr>
    </g:each>
    </table>
    </div>
</div>
<div id="questModal" class="modal fade " role="dialog">
    <div class="modal-dialog modal-lg">

        <!-- Modal content-->
        <div class="modal-content">


                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal">&times;</button>
                    <h5 class="modal-title modalHeader"><g:message code="app.label.PublisherTemplate.questionnaire"/></h5>
                </div>

                <div class="modal-body">
                            <div>
                                <label><g:message code="app.label.PublisherTemplate.question" default="Question"/></label>
                                <input name="question" id="question" class="form-control" maxlength="4000">
                                <hr>
                            </div>
                            <table class="table questTable" >
                                <thead><tr>
                                    <th><span class="md md-plus addQuestAnswer pv-cross"></span></th>
                                    <th><g:message code="app.label.PublisherTemplate.answer" default="Answer"/></th>
                                    <th><g:message code="app.label.PublisherTemplate.value" default="Value"/></th>
                                </tr></thead>
                                <tr class="answerValue">
                                    <td><span class="md md-close pv-cross removeQuestAnswer"></span></td>
                                    <td><input name="questAnswer" class="form-control questAnswer" maxlength="4000"></td>
                                    <td><textarea name="questValue" class="form-control questValue" maxlength="4000" style="height: 80px;"></textarea></td>
                                </tr>
                            </table>
                </div>

                <div class="modal-footer">
                    <button type="button" class="btn btn-default questSave" ><g:message code="app.update.button.label"/></button>
                    <button type="button" class="btn btn-default " data-dismiss="modal"><g:message code="app.button.close"/></button>
                </div>

        </div>

    </div>
</div>
<g:render template="/publisherTemplate/includes/publisherHelp" />