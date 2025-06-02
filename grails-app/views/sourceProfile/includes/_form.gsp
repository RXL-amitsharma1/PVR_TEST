<%@ page import="com.rxlogix.util.ViewHelper;" %>
<div class="row">
    <div class="col-md-8">
        <div class="row form-group">
            <div class="col-md-12">
                <label for="sourceId"><g:message code="app.label.sourceProfile.sourceId"/></label>
                <g:textField disabled="true" name="sourceId" value="${sourceProfileInstance?.sourceId}"
                             class="form-control sourceProfileField "/>
            </div>
        </div>

        <div class="row form-group">
            <div class="col-md-12">
                <label for="sourceName"><g:message code="app.label.sourceProfile.sourceName"/><span
                        class="required-indicator">*</span></label>
                <g:textField name="sourceName" value="${applyCodec(encodeAs:'HTML',raw(sourceProfileInstance?.sourceName))}"
                             class="form-control sourceProfileField"/>
            </div>
        </div>

        <div class="row form-group">
            <div class="col-md-12">
                <label for="sourceAbbrev"><g:message code="app.label.sourceProfile.sourceAbbrev"/><span
                        class="required-indicator">*</span></label>
                <g:textField name="sourceAbbrev" maxlength="5" minlength="3"
                             value="${sourceProfileInstance?.sourceAbbrev}"
                             class="form-control sourceProfileField"/>
            </div>
        </div>

        <div class="row form-group">
            <div class="col-md-12">
                <label for="sourceAbbrev"><g:message code="app.label.sourceProfile.DateRangeTypes"/></label>
                <span class="glyphicon glyphicon-question-sign modal-link" style="cursor:pointer"
                      data-toggle="tooltip" data-placement="right"
                      title="${message(code: 'app.label.sourceProfile.DateRangeTypes.helpText')}"></span>
                <g:select name="dateRangeTypes" from="${ViewHelper.getDateRangeTypeI18n()}" optionKey="name"
                          optionValue="display" value="${sourceProfileInstance?.dateRangeTypes*.id}"
                          class="form-control"/>
            </div>
        </div>

        <div class="row form-group">
            <div class="col-md-12">
                <label aria-disabled="true">
                    <div class="checkbox checkbox-primary" style="display: inline-block;">
                        <g:checkBox name="includeLatestVersionOnly"
                                    value="${sourceProfileInstance?.includeLatestVersionOnly}"
                                    checked="${sourceProfileInstance?.includeLatestVersionOnly}"/>
                        <label for="includeLatestVersionOnly">
                            <g:message code="app.label.sourceProfile.includeLatestVersionOnly"/>
                        </label>

                    </div>
                    <span class="glyphicon glyphicon-question-sign modal-link" style="cursor:pointer"
                          data-toggle="tooltip" data-placement="right"
                          title="${message(code: 'app.label.sourceProfile.includeLatestVersionOnly.helpText')}"></span>
                </label>
            </div>
        </div>

        <div class="row form-group">
            <div class="col-md-12">
                <label aria-disabled="true">
                    <div class="checkbox checkbox-primary">
                        <g:checkBox name="isCentral" class="sourceProfileField"
                                    value="${sourceProfileInstance?.isCentral}"
                                    checked="${sourceProfileInstance?.isCentral}"/>
                        <label for="isCentral">
                            <g:message code="app.label.sourceProfile.isCentral"/>
                        </label>
                    </div>
                </label>
            </div>
        </div>
    </div>
</div>

<script type="application/javascript">
    $("#dateRangeTypes").select2();
    $(function () {
        $('[data-toggle="tooltip"]').tooltip();
    })
</script>
