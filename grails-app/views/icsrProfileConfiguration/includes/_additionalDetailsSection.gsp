<%@ page import="com.rxlogix.Constants; com.rxlogix.user.User; com.rxlogix.config.UnitConfiguration" %>

<div class="rxmain-container rxmain-container-top">
    <g:set var="userService" bean="userService"/>
    <g:set var="currentUser" value="${userService.getUser()}"/>

    <div class="rxmain-container-inner">
        <div class="rxmain-container-row rxmain-container-header">
            <i class="fa fa-caret-down fa-lg click" data-evt-clk='{"method": "hideShowContent", "params": []}'></i>
            <label class="rxmain-container-header-label click" data-evt-clk='{"method": "hideShowContent", "params": []}'>
                <g:message code="app.label.icsr.profile.conf.additionalDetails"/>
            </label>
        </div>

        <div class="rxmain-container-content rxmain-container-show">
            <div class="row form-group">
                <div class="col-xs-4">
                    <div class="email-to-Select" style="padding-bottom: 10px;">
                        <g:renderClosableInlineAlert id="email-invalid-alert" type="danger" />
                        <label for="emailUsers"><g:message code="app.label.emailTo"/></label>
                        <g:select id="emailUsers"
                                  name="deliveryOption.emailToUsers"
                                  from="${[]}"
                                  data-value="${configurationInstance?.deliveryOption?.emailToUsers?.join(",")}"
                                  class="form-control emailUsers" multiple="true"
                                  data-options-url="${createLink(controller: 'email', action: 'allEmails', params: [id: configurationInstance?.id])}"/>
                        <i class="fa fa-pencil-square-o copyPasteEmailButton" style="padding-top:25px;"></i>
                    </div>

                    <div class="shareWithUser">
                        <script>
                            sharedWithListUrl = "${createLink(controller: 'userRest', action: 'sharedWithList')}";
                            sharedWithValuesUrl = "${createLink(controller: 'userRest', action: 'sharedWithValues')}";
                            $(function () {
                                bindShareWith($('.sharedWithControl'), sharedWithListUrl, sharedWithValuesUrl,"100%")
                            });
                        </script>
                        <g:set var="sharedWithValue" value="${ (configurationInstance?.shareWithGroups?.collect{Constants.USER_GROUP_TOKEN + it.id} + configurationInstance?.shareWithUsers?.collect{Constants.USER_TOKEN + it.id})?.join(";")}"/>
                        <g:if test="${!sharedWithValue}">
                            <g:set var="sharedWithValue" value="${ ([Constants.USER_TOKEN + currentUser.id]).join(";")}"/>
                        </g:if>
                        <div>
                            <label for="sharedWith"><g:message code="shared.with"/></label>
                        <select class="sharedWithControl form-control" id="sharedWith" name="sharedWith" data-value="${sharedWithValue}"></select>
                        </div>
                    </div>
                </div>

                <div class="col-xs-4 ${configurationInstance.pvqType ? "hidden" : ""}">
                    <div >
                        <label for="description"><g:message code="app.label.reportDescription"/></label>
                        <g:render template="/includes/widgets/descriptionControl" model="[name:'description', value:configurationInstance?.description, maxlength: 4000]"/>

                    </div>
                </div>

                <div class="col-xs-4 ">
                    <div class="col-md-12" style="margin-top:29px;">
                        <sec:ifAnyGranted roles="ROLE_QUALITY_CHECK">
                            <div class="checkbox checkbox-primary">
                                <g:checkBox name="qualityChecked" id="qualityChecked"
                                            value="${configurationInstance?.qualityChecked}"
                                            checked=""/>
                                <label for="qualityChecked">
                                    <g:message code="app.label.qualityChecked"/>
                                </label>
                            </div>
                        </sec:ifAnyGranted>
                    </div>
                </div>

            </div>
        </div>
    </div>
</div>
