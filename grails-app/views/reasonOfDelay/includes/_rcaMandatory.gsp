<%@ page import="com.rxlogix.WorkflowStateController; com.rxlogix.config.WorkflowState; com.rxlogix.enums.ReasonOfDelayAppEnum; grails.util.Holders" %>
<div class="container" id="rcaTab" style="display: none">
    <!-------=========+++++++++++++++++++++---------------RCA MAPPING Code has started--------++++++++++++++++===================----------------->
    <div>
    <div class="alert alert-danger alert-dismissible forceLineWrap errorDiv" role="alert" hidden="hidden">
        <button type="button" class="close errorClose">
            <span aria-hidden="true">&times;</span>
            <span class="sr-only"><g:message code="default.button.close.label"/></span>
        </button>
        <p class="errorContent"></p>
    </div>
    <div class="alert alert-success alert-dismissible forceLineWrap successDiv" role="alert" hidden="hidden">
        <button type="button" class="close successClose">
            <span aria-hidden="true">&times;</span>
            <span class="sr-only"><g:message code="default.button.close.label"/></span>
        </button>
        <p class="successContent"></p>
    </div>
        <div class="panel-group" id="accordion">
            <!-----------------------------accordian 1 has started-------------------------->

            <sec:ifAnyGranted roles="ROLE_PVQ_EDIT">
                <g:if test="${Holders.config.get('pv.app.pvquality.enabled')}">
                    <div class="panel panel-default">
                        <div class="panel-heading">
                            <h4 class="panel-title">
                                <a class="accordion-toggle" data-toggle="collapse" data-parent="#accordion" href="#collapse1"
                                   aria-expanded="true">
                                    <g:message code="label.rcaMapping.app.PVQuality"/>
                                </a>
                            </h4>
                        </div>

                        <div id="collapse1" class="panel-collapse p-10 collapse in" aria-expanded="true">
                            <g:render template="/reasonOfDelay/includes/rcaMandatoryForm"
                                      model="[ownerApp: ReasonOfDelayAppEnum.PVQ]"/>
                        </div>
                    </div>
                </g:if>
            </sec:ifAnyGranted>
            %{--
                        <g:set var="assignedToValue" value="${reportRequestInstance?.assignedGroupTo ? (Constants.USER_GROUP_TOKEN + reportRequestInstance.assignedGroupTo.id) : (reportRequestInstance?.assignedTo ? (Constants.USER_TOKEN + reportRequestInstance.assignedTo.id) : "")}"/>
            --}%
            <!-------------------------------------------------------accordian 1 has closed---------------------------------------------------->

            <!--------------------------------------------------------accordian 2 has started-------------------------------------------------->
            <g:if test="${Holders.config.get('pv.app.pvcentral.enabled')}">
            <sec:ifAnyGranted roles="ROLE_PVC_INBOUND_EDIT">
            <div class="panel panel-default">
                <div class="panel-heading">
                    <h4 class="panel-title">
                        <a class="accordion-toggle collapsed" data-toggle="collapse" data-parent="#accordion"
                           href="#collapse2" aria-expanded="false">
                            <g:message code="label.rcaMapping.app.Inbound"/>
                        </a>
                    </h4>
                </div>

                <div id="collapse2" class="panel-collapse p-10 collapse" aria-expanded="false" style="height: 20px;">
                    <g:render template="/reasonOfDelay/includes/rcaMandatoryForm"
                              model="[ownerApp: ReasonOfDelayAppEnum.PVC_Inbound]"/>
                </div>
            </div>
            </sec:ifAnyGranted>

            <!-------------------------------------------------------accordian 2 has closed---------------------------------------------------->

            <!-------------------------------------------------------accordian 3 has started--------------------------------------------------->
            <sec:ifAnyGranted roles="ROLE_PVC_EDIT">
            <div class="panel panel-default">
                <div class="panel-heading">
                    <h4 class="panel-title">
                        <a class="accordion-toggle collapsed" data-toggle="collapse" data-parent="#accordion"
                           href="#collapse3" aria-expanded="false">
                            <g:message code="label.rcaMapping.app.PVCentral"/>
                        </a>
                    </h4>
                </div>

                <div id="collapse3" class="panel-collapse p-10 collapse" aria-expanded="false" style="height: 20px;">
                    <g:render template="/reasonOfDelay/includes/rcaMandatoryForm"
                              model="[ownerApp: ReasonOfDelayAppEnum.PVC]"/>
                </div>
            </div>
            </sec:ifAnyGranted>
            </g:if>
            <!-------------------------------------------------------accordian 3 has closed--------------------------------------------------------->
        </div>
        <!-------=========+++++++++++++++++++++---------------RCA MAPPING Code has closed---------++++++++++++++++===================----------------->
    </div>
</div>
