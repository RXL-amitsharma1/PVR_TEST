<%@ page import="com.rxlogix.util.ViewHelper" %>
<div class="rxmain-container rxmain-container-top e2BConfiguration"  <g:if test="${(actionName == 'create') || (actionName == 'save' && (e2bChannelSelected || e2bChannelSelectedAsEmail)) || (editMode == true && (e2bChannelSelected || e2bChannelSelectedAsEmail))}"> style="display:block;" </g:if> <g:else> style="display:none;" </g:else>>
    <div class="rxmain-container-row rxmain-container-header">
        <label class="rxmain-container-header-label">
            <g:message code="app.label.icsr.profile.conf.e2b.distribution.settings"/>
        </label>
    </div>

    <div class="rxmain-container-content rxmain-container-show">
        <div class="row">
            <div class="col-md-3 e2bDistReportFormat">
                <label><g:message code="app.label.icsr.profile.conf.reportFormat"/><span class="required-indicator">*</span></label>
                <g:select name="e2bDistributionChannel.reportFormat" from="${ViewHelper.getE2BReportFormatEnumI18n()}" class="form-control select2-box" id="e2bDistReportFormat"
                          optionKey="name" optionValue="display" noSelection="${['': message(code: 'select.one')]}"
                          value="${configurationInstance?.e2bDistributionChannel?.reportFormat}"/>
            </div>

            <div class="col-md-3 e2bDistOutgoingFolder">
                <label><g:message code="app.label.icsr.profile.conf.outgoingFolder"/><span class="required-indicator">*</span></label>
                <g:selectE2BOutgoingFolder name="e2bDistributionChannel.outgoingFolder" class="form-control select2-box" id="e2bDistOutgoingFolder" value="${configurationInstance?.e2bDistributionChannel?.outgoingFolder}" noSelection="${['': message(code: 'select.one')]}"/>
            </div>

            <div class="col-md-3 e2bDistIncomingFolder">
                <label><g:message code="app.label.icsr.profile.conf.incomingFolder" /></label>
                <g:selectE2BIncomingFolder name="e2bDistributionChannel.incomingFolder" class="form-control select2-box" id="e2bDistIncomingFolder" value="${configurationInstance?.e2bDistributionChannel?.incomingFolder}" noSelection="${['': message(code: 'select.one')]}"/>
            </div>

            %{--<div class="col-md-3" style="margin-top: 20px">
                <div id="needPaperReport" class="checkbox checkbox-primary">
                    <g:checkBox id="needPaperReport"
                                name="needPaperReport"
                                value="${configurationInstance?.needPaperReport}"
                                checked="${configurationInstance?.needPaperReport}"/>
                    <label for="needPaperReport">
                        <g:message code="app.label.icsr.profile.conf.need.paper.report"/>
                    </label>
                </div>
            </div>--}%
        </div>
    </div>
</div>