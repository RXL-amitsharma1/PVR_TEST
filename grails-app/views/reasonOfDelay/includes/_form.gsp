<%@ page import="com.rxlogix.util.ViewHelper" %>
<div class="row form-group">
    <div class="col-lg-12">
        <label for="label"><g:message code="app.label.dashboardDictionary.label" /><span class="required-indicator">*</span></label>
        <input required  id="label" name="label" value=""  class="form-control"/>
    </div>
    <input id="objectId" name="objectId" class="hidden"/>
</div>

<div class="row form-group lateTypeDiv">
    <div class="col-xs-12">
        <label for="lateType"><g:message code="label.late.type"/></label>
        <div>
            <select name="lateType" id="lateType" class="form-control" autocomplete="off"></select>
        </div>
    </div>
</div>

<div class="row form-group mappingDiv forceLineWrap">
    <div class="col-xs-12">
        <label for="mapping"><g:message code="shared.with"/></label>
        <select id="mapping" name="mapping" multiple class="form-control" autocomplete="off"></select>
    </div>
</div>

<div class="row form-group rootCauseSubDiv">
    <div class="col-xs-12">
        <label for="rootCauseSub"><g:message code="app.pvc.RootCauseSubCategory"/></label>
        <select id="rootCauseSub" name="rootCauseSub" multiple class="form-control" autocomplete="off"></select>
    </div>
</div>
<div class="row form-group rootCauseClassDiv">
    <div class="col-xs-12">
        <label for="rootCauseClass"><g:message code="app.pvc.RootCauseClass"/></label>
        <select id="rootCauseClass" name="rootCauseClass" multiple class="form-control" autocomplete="off"></select>
    </div>
</div>

<div class="row form-group">
    <div class="col-xs-12">
        <label for="ownerApp"><g:message code="label.owner.app"/></label>
        <div>
            <g:select name="ownerApp" id="ownerApp" from="${ViewHelper.getRODAppTypeEnum()}"
                      optionKey="name"
                      optionValue="display"/>
            <div class="pull-right">
            <div class="checkbox checkbox-primary">
                <g:checkBox name="hide" id = "hide"/>
                <label for="hide">
                    <g:message code="app.RCA.hidden" />
                </label>
            </div>
        </div>
        </div>
    </div>
</div>

<div class="bs-callout bs-callout-info" id="warningNote">
    <h5><g:message code="app.label.note" /> : <g:message code="app.pvc.label.validation.note" /></h5>
</div>


<div class="modal fade" id="errorModal" data-backdrop="static" tabindex="-1" role="dialog" aria-labelledby="warningModalLabel"
     aria-hidden="true">
<div class="modal-dialog">
    <div class="modal-content">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
            <h4 class="modal-title" id="warningModalLabel">Error!</h4>
        </div>

        <div class="modal-body">

            <div class="description" style="font-weight:bold;"></div>

        </div>

        <div class="modal-footer">
            <button type="button" class="btn btn-default" data-dismiss="modal"><g:message
                    code="default.button.ok.label"/></button>
        </div>
    </div>
</div>
</div>