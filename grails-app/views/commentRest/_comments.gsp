<%@ page import="com.rxlogix.util.DateUtil; com.rxlogix.util.RelativeDateConverter" %>
<g:if test="${comments}">
    <g:each in="${comments}" var="comment">
        <div class="comment" style="overflow-wrap: break-word;">
            <div>
                <div class="commenter-info"><span>${comment.createdBy} :</span> ${message(code: 'app.label.added.comment')} - ${DateUtil.StringFromDate(comment.dateCreated, DateUtil.DATEPICKER_FORMAT_AM_PM, timeZone)}</div>

                <div style="float: right">
                    <g:showIfLoggedInUserSame userName="${comment.createdBy}">
                        <asset:image src="icons/trash-icon.png" class="delete click" data-id="${comment.id}"/>
                    </g:showIfLoggedInUserSame>

                </div>

                <div class="clearfix"></div>
            </div>

            <p>${applyCodec(encodeAs:'HTML',(comment.textData))}</p>
        </div>
    </g:each>
</g:if>
<g:else>
    <g:message code="comment.list.empty.message"/>
</g:else>