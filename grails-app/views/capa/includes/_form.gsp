<%@ page import="com.rxlogix.config.CorrectiveAction; com.rxlogix.util.ViewHelper" %>
<div class="row form-group">
    <div class="col-lg-12">
        <label for="label"><g:message code="app.label.dashboardDictionary.label" /><span class="required-indicator">*</span></label>
        <input required  id="label" name="label" maxlength="${CorrectiveAction.constrainedProperties.textDesc.maxSize}" value="" class="form-control"/>
    </div>
    <input id="objectId" name="objectId" class="hidden"/>
</div>

<div class="row form-group">
    <div class="col-xs-12">
        <label for="ownerApp"><g:message code="label.owner.app"/></label>
        <div>
            <g:select name="ownerApp" id="ownerApp" from="${ViewHelper.getRODAppTypeEnum()}"
                      optionKey="name"
                      optionValue="display"/>
        </div>
    </div>
</div>

<div class="bs-callout bs-callout-info" id="warningNote">
    <h5><g:message code="app.label.note" /> : <g:message code="app.pvc.label.validation.note" /></h5>
</div>