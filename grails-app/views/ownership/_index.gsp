<%@ page import="com.rxlogix.util.ViewHelper; com.rxlogix.enums.TransferTypeEnum;  com.rxlogix.user.User" %>

<asset:javascript src="app/user/user.js"/>
<script>
    var OWNERSHIP="${TransferTypeEnum.OWNERSHIP}";
    var SHAREWITH="${TransferTypeEnum.SHAREWITH}";
    var SHAREOWNED="${TransferTypeEnum.SHAREOWNED}";
</script>



<div class="row">
    <div class="col-md-12">

        <div class="row">
            <div class="col-md-2">
                <g:select name="transferType"
                      from="${ViewHelper.transferTypeEnum}"
                      optionKey="name" optionValue="display"
                      class="form-control "/>

            </div>
            <div class="col-md-3">
                <div class="ownerSelect">
                <g:select name="newOwner" id="newOwner"
                          from="${User.findAllByEnabled(true) - userInstance}"
                          optionKey="id"
                          optionValue="fullNameAndUserName"
                          class="form-control owner-select"
                          noSelection="${['': message(code: 'select.one')]}"/>
                </div>
                <div class="shareSelect" style="display: none">

                    <g:set var="userService" bean="userService"/>
                    <g:set var="userGroups" value="${userService.getActiveGroups()}"/>
                    <select id="sharedWith" name="sharedWith"  class="form-control owner-select" autocomplete="off">
                        <option value="no">${g.message(code: 'select.one')}</option>
                        <g:if test="${userGroups}">
                            <optgroup label="${g.message(code: 'user.group.label')}">
                                <g:each in="${userGroups}" var="userGroup">
                                    <option value="${userGroup.getReportRequestorKey()}" }>${userGroup.getReportRequestorValue()}</option>
                                </g:each>
                            </optgroup>
                        </g:if>
                        <optgroup label="${g.message(code: 'user.label')}">
                            <g:each in="${userService.getActiveUsers() - userInstance}" var="user">
                                <option value="${user.getReportRequestorKey()}" >${user.getReportRequestorValue()}</option>
                            </g:each>
                        </optgroup>
                    </select>
                </div>

            </div>
            <div class="col-md-7">
                <button type="button" id="transferOwnershipButton" name="transferOwnershipButton" class="btn pv-btn-grey" data-toggle="modal" data-target="#transferOwnershipModal" disabled="disabled">
                    <span class="glyphicon glyphicon-user"></span>
                    ${message(code: 'default.button.ok.label')}
                </button>

            </div>

        </div>

    </div>

</div>
<div class="ownerItems">
    <g:render template="/ownership/itemsToTransfer" model="[formName: 'ownerForm', userInstance: userInstance, items: owned]"/>
</div>
    <div class="shareItems" style="display: none">
        <g:render template="/ownership/itemsToTransfer" model="[formName: 'shareForm', userInstance: userInstance, items: shared]"/>
</div>

<div class="modal fade" id="transferOwnershipModal"  data-backdrop="static" tabindex="-1" role="dialog" aria-labelledby="transferOwnershipModalLabel"
     aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                <h3 class="modal-title" id="transferOwnershipModalLabel"><g:message code="modal.owner.change.title.label" /></h3>
            </div>

            <div class="modal-body">
                <div><g:message code="ownership.change.alert"/></div>

                <p></p>

            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal"><g:message
                        code="default.button.cancel.label"/></button>
                <button id="ownerChangeButton" class="btn btn-danger">
                    <span class="glyphicon glyphicon-user icon-white"></span>
                    ${message(code: 'default.button.transferOwnership.label', default: 'Transfer')}
                </button>
            </div>
        </div><!-- /.modal-content -->
    </div><!-- /.modal-dialog -->
</div><!-- /.modal -->

