<%@ page import="com.rxlogix.user.User" %>
<div class="modal fade" id="addRemoveUserModal"  data-backdrop="static" tabindex="-1" role="dialog" aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header dropdown">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
                <h4 class="modal-title"><g:message code="userGroup.add.remove.users.label" /></h4>
            </div>
            <div class="modal-body action-item-modal-body">
                <g:select id="allowedUsers" name="allowedUsers"
                          from="${allUserList}"
                          optionKey="id" optionValue="fullName"
                          multiple="true"
                          value="${allowedUsers*.id}">
                </g:select>
            </div>
            <div class="modal-footer">
                <div  class="buttons creationButtons">
                    <input id="addRemoveUserButton" type="button" class="btn btn-primary" value="${message(code: "scheduler.select")}" data-evt-clk='{"method": "changeUser", "params": []}'>
                    <button type="button" data-dismiss="modal" class="btn pv-btn-grey" aria-label="Close"><g:message code="app.button.close" /></button>
                </div>
            </div>
        </div>
    </div>
</div>
<asset:javascript src="app/userGroup/addRemoveUser.js"/>
<style type="text/css">
.pickList {margin-left:155px;}
.pickList_list {width: 250px;height: 280px;}
</style>
