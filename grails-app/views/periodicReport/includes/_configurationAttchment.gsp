<%@ page import="com.rxlogix.config.PublisherSource; com.rxlogix.user.UserGroup; com.rxlogix.config.BasicPublisherSource" %>
<g:if test="${showAttachmentWarning}">
    <div class="alert alert-warning alert-dismissible forceLineWrap crossDiv" role="alert">
        <button type="button" class="close" data-dismiss="alert">
            <span aria-hidden="true" class="cross">&times;</span>
            <span class="sr-only"><g:message code="default.button.close.label"/></span>
        </button>
        <i class="fa fa-warning"></i>
        <g:message code="app.reportRequest.attachment.warning"/>
    </div>
</g:if>
<g:if test="${attachmentesUpdated}">
    <div class="alert alert-success alert-dismissible forceLineWrap crossDiv" role="alert">
        <button type="button" class="close" data-dismiss="alert">
            <span aria-hidden="true" class="cross">&times;</span>
            <span class="sr-only"><g:message code="default.button.close.label"/></span>
        </button>
        <i class="fa fa-check"></i>
        <g:message code="app.label.PublisherTemplate.additionalSourcesUpdated"/>
    </div>
</g:if>
<g:if test="${attachmentesUpdatedErrorMessage}">
    <div class="alert alert-danger alert-dismissible forceLineWrap crossDiv" role="alert">
        <button type="button" class="close" data-dismiss="alert">
            <span aria-hidden="true" class="cross">&times;</span>
            <span class="sr-only"><g:message code="default.button.close.label"/></span>
        </button>
        <i class="fa fa-times-circle"></i>
        <g:message code="app.label.PublisherTemplate.additionalSourcesUpdatedError"/>
        ${raw(attachmentesUpdatedErrorMessage)}
    </div>
</g:if>
<div class="alert alert-danger alert-dismissible forceLineWrap additionalSourceError" role="alert" style="display: none">
    <button type="button" class="close" data-dismiss="alert">
        <span aria-hidden="true">&times;</span>
        <span class="sr-only"><g:message code="default.button.close.label"/></span>
    </button>
    <g:message code="app.publisher.additionalSource.error"/>
</div>
<g:if test="${publisherMode}">
    <form enctype="multipart/form-data"  method="post" autocomplete="off" action="${createLink(controller: 'periodicReport', action: 'updatePublisherAttachment')}" data-evt-sbt='{"method": "validateAdditionalSourceForm", "params": []}'>
</g:if>
<div class="pv-caselist">
<table width="100%" class="attachmentTable table table-striped pv-list-table dataTable no-footer">
    <thead>
    <tr>
        <th class="col-min-65"><span class="table-add md md-plus theme-color md-lg attachmentSectionAdd"></span></th>
        <th class="col-min-150"><label><g:message code="app.label.PublisherTemplate.attachmentName" /></label></th>
        <th class="col-min-150"><label><g:message code="app.label.PublisherTemplate.visibleFor" /></label></th>
        <th class="col-min-150"><label><g:message code="app.label.PublisherTemplate.type" /></label></th>
        <th class="col-min-150"><label><g:message code="app.label.PublisherTemplate.source" /></label></th>
        <th class="col-min-65"></th>
        <th width="*"><label><g:message code="app.label.PublisherTemplate.path" /></label></th>
    </tr>
    </thead>
    <tbody class="attachmentSectionsTable">
    <tr class="rowTemplate" style="display: none">
        <td>
            <span class='table-remove md md-close pv-cross attachmentSectionRemove'></span>
            <span class='table-add md md-arrow-up pv-cross publisherSectionUp'></span>
            <span class='table-add md md-arrow-down pv-cross publisherSectionDown'></span>
            <input class="form-control" name="attachmentSectionId" type="hidden" value="0">
        </td>
        <td><input name="attachmentName" maxlength="${PublisherSource.constrainedProperties.name.maxSize}" class="form-control"></td>
        <td>
            <g:select name="attachmentSectionUserGroup" from="${UserGroup.findAllByIsDeleted(false).sort{it.name.toUpperCase()}}"
                      class="form-control attachmentSectionUserGroup" optionKey="id" optionValue="name" noSelection="['0': 'Any']"/>
        </td>
        <td>
            <select name="attachmentFileType" class="form-control fileType">
                <g:each in="${BasicPublisherSource.FileType.values()}" var="type">
                    <option value="${type.name()}"><g:message code="${type.i18nKey}"/></option>
                </g:each>
            </select>
        </td>
        <td>
            <select name="attachmentFileSource" class="form-control fileSource" >
                <g:each in="${BasicPublisherSource.Source.values()}" var="source">
                    <option value="${source.name()}"><g:message code="${source.i18nKey}"/></option>
                </g:each>
            </select>
        </td>
        <td></td>
        <td>
            <input name="attachmentPath" class="form-control attachmentPath"  maxlength="${PublisherSource.constrainedProperties.path.maxSize}" style="display: none;">
            <div class="input-group " style="width: 100%; ">
                <input type="text" class="form-control fileName" readonly>
                <label class="input-group-btn">
                    <span class="btn btn-primary inputbtn-height">
                        <g:message code="app.label.attach"/>
                        <input type="file" class="file_input" name="attachmentSectionFile" style="display: none;">
                    </span>
                </label>
            </div>
            <div style="display: none" class="attachmentOneDriveDiv">
                <input type="hidden" name="oneDriveFolderId" >
                <input type="hidden" name="oneDriveSiteId" >
                <input type="hidden" name="oneDriveUserSettings" >
                <div class="input-group">
                    <input class="form-control" readonly name="oneDriveFolderName" >
                    <span class="input-group-btn">
                        <button class="btn btn-primary selectOneDriveFileSelectMode" type="button"><g:message code="scheduler.select"/></button>
                    </span>
                </div>
            </div>
            <div class="container">
             <div class="row attachmentScript" style="display: none">
                <div class="col-md-10">
                    <textarea name="attachmentScript" class="form-control " style="height: 70px; "></textarea>
                </div>
                <div class="col-md-2">
                    <a href="javascript:void(0);" class="ic-sm pv-ic pv-ic-hover testRest" style="margin-left: 5px; line-height: 2.5em;" title="${message(code: "app.label.PublisherTemplate.verify")}"><img class="verify-tooltip" data-tooltip="Verify" src="<g:resource dir="images" file="run.png"/>"></a>
                    <span class="btn-sm fa fa-question-circle modal-link" style="cursor:pointer; margin-top: -10px; font-size: 15px;" data-toggle="modal" data-target="#helpRequestModal"></span>
                </div>
              </div>
            </div>
        </td>

    </tr>

    <g:each var="attachment" in="${attachments?.sort { it.sortNumber }}" status="i">
        <tr>
            <td>
                <span class='table-remove md md md-close pv-cross pv-cross attachmentSectionRemove'></span>
                <span class='table-add md-arrow-up pv-cross publisherSectionUp'></span>
                <span class='table-add md-arrow-down pv-cross publisherSectionDown'></span>
                <input class="form-control" name="attachmentSectionId" type="hidden" value="${attachment.id}">
            </td>
            <td><input name="attachmentName" maxlength="${PublisherSource.constrainedProperties.name.maxSize}" value="${attachment.name}" class="form-control"></td>
            <td>
                <g:select name="attachmentSectionUserGroup" from="${UserGroup.findAllByIsDeleted(false).sort{it.name.toUpperCase()}}"
                    value="${attachment?.userGroup?.id}"
                          class="form-control attachmentSectionUserGroup" optionKey="id" optionValue="name" noSelection="['0': 'Any']"/>
            </td>
            <td>
                <select name="attachmentFileType" class="form-control fileType removeButton">
                    <g:each in="${BasicPublisherSource.FileType.values()}" var="type">
                        <option ${attachment.fileType == type?"selected":""}  value="${type.name()}"><g:message code="${type.i18nKey}"/></option>
                    </g:each>
                </select>
            </td>
            <td>
                <select name="attachmentFileSource" class="form-control fileSource removeButton">
                    <g:each in="${BasicPublisherSource.Source.values()}" var="source">
                        <option ${attachment.fileSource == source?"selected":""} value="${source.name()}"><g:message code="${source.i18nKey}"/></option>
                    </g:each>
                </select>
            </td>
            <td>
                <a href="${createLink(controller: "periodicReport", action: "downloadAttachment")}?id=${attachment.id}&executedAttachment=${publisherMode}" target="_blank"><span class="fa fa-download removeDownloadButton"></span></a>
            </td>
            <td>
                <input name="attachmentPath" value="${attachment.path}" maxlength="${PublisherSource.constrainedProperties.path.maxSize}"  class="form-control attachmentPath removeButton" >
                <div class="input-group" style="display: none">
                    <input type="text" class="form-control fileName" value="${attachment.path}" readonly>
                    <label class="input-group-btn">
                        <span class="btn btn-primary inputbtn-height removeButton">
                            <g:message code="app.label.attach"/>
                            <input type="file" class="file_input removeButton" name="attachmentSectionFile" style="display: none;">
                        </span>
                    </label>
                </div>
                <div style="display: none" class="attachmentOneDriveDiv">
                    <input type="hidden" name="oneDriveFolderId" value="${attachment.oneDriveFolderId}">
                    <input type="hidden" name="oneDriveSiteId" value="${attachment.oneDriveSiteId}">
                    <input type="hidden" name="oneDriveUserSettings" value="${attachment.oneDriveUserSettings?.id}">
                    <div class="input-group">
                        <input class="form-control" readonly name="oneDriveFolderName" value="${attachment.oneDriveFolderName}">
                        <span class="input-group-btn">
                            <button class="btn btn-primary selectOneDriveFileSelectMode removeButton" type="button"><g:message code="scheduler.select"/></button>
                        </span>
                    </div>
                </div>
                <div class="container">
                  <div class="row attachmentScript removeButton">
                    <div class="col-md-10">
                        <textarea name="attachmentScript" class="form-control" style="height: 70px;" >${attachment.script}</textarea>
                    </div>
                    <div class="col-md-2">
                        <a href="javascript:void(0);" class="ic-sm pv-ic pv-ic-hover testRest" style="margin-left: 5px; line-height: 2.5em;" title="${message(code: "app.label.PublisherTemplate.verify")}"><img class="verify-tooltip" data-tooltip="Verify" src="<g:resource dir="images" file="run.png"/>"></a>
                        <span class="btn-sm fa fa-question-circle modal-link" style="cursor:pointer; margin-top: -10px; font-size: 15px;" data-toggle="modal" data-target="#helpRequestModal"></span>
                    </div>
                  </div>
                </div>
            </td>
        </tr>
    </g:each>
    </tbody>
</table>
    <g:if test="${!attachments}">
        <div style="width: 100%; text-align: center; ">
        <g:message code="app.label.PublisherTemplate.noSources" default="No External Attachments found"/>
        </div>
    </g:if>
</div>
<g:if test="${publisherMode}">
    <input type="hidden" name="executedConfigId" value="${executedConfigurationInstanceId}">
    <div style="width: 100%; text-align: right; margin-top: 10px;padding-right: 10px;"><button type="submit" class="btn btn-primary updatebtn">
        <g:message code="app.update.button.label"/>
    </button></div>
    </form>
</g:if>

<input type="hidden" name="attachmentsToDelete" id="attachmentsToDelete" value="">
<div id="testRequestModal" class="modal fade " role="dialog">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <label class="modal-title modalHeader"><g:message code="app.label.PublisherTemplate.verify"/></label>
            </div>
            <div class="modal-body">
                <textarea id="testLog" class="form-control" style="height: 400px"></textarea>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default " data-dismiss="modal"><g:message code="app.button.close"/></button>
            </div>

        </div>

    </div>
</div>
<div id="helpRequestModal" class="modal fade " role="dialog">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <b class="modal-title"><g:message code="app.label.PublisherTemplate.path"/> <g:message code="app.label.help"/></b>
            </div>
            <div class="modal-body">
                <g:message code="app.label.PublisherTemplate.additionalSources.help.text1"/>
               <br>
                <code>&lt;GET or POST&gt; &lt;URL&gt; </code><br> <g:message code="app.label.PublisherTemplate.additionalSources.help.forexample"/>
                <br>
                <code>
                GET https://someservice.com/service
                </code><br><br>

                <g:message code="app.label.PublisherTemplate.additionalSources.help.text2"/><br>
                <code>header &lt;header name&gt;:&lt;value&gt;</code><br>
                <g:message code="app.label.PublisherTemplate.additionalSources.help.forexample"/><br>
                <code> header Content-Type: application/json</code><br><br>

                <g:message code="app.label.PublisherTemplate.additionalSources.help.text3"/><br>
                <code> body &lt;content&gt;</code><br>
                <g:message code="app.label.PublisherTemplate.additionalSources.help.forexample"/><br>
                <code>body {
                        "token": "my-secret-token"
            }</code><br><br>

                <g:message code="app.label.PublisherTemplate.additionalSources.help.text4"/><br>
                <code> var &lt;variable name&gt;=&lt;expression&gt;</code><br>

                            expression can be any string (quotes not required) <g:message code="app.label.PublisherTemplate.additionalSources.help.forexample"/><br>
                <code>var password=Wedr34dff</code><br>

                or<code> eval &lt;groove script&gt;</code> <g:message code="app.label.PublisherTemplate.additionalSources.help.forexample"/><br>
                <code>     var currentDate=eval new Date()</code><br>

                <g:message code="app.label.PublisherTemplate.additionalSources.help.text5"/>
                <code>{message:{id:1, token:1111,…},...}</code> <g:message code="app.label.PublisherTemplate.additionalSources.help.text6"/><br>
                <code>var auth_token=response.message.token</code><br>

                <g:message code="app.label.PublisherTemplate.additionalSources.help.text7"/><code>{{&lt;variable name&gt;}}</code>. <g:message code="app.label.PublisherTemplate.additionalSources.help.forexample"/><br>
                <code>  GET https://someservice.com?id=1&token={{token}}</code><br>

                <g:message code="app.label.PublisherTemplate.additionalSources.help.text8"/><br><br>

                <g:message code="app.label.PublisherTemplate.additionalSources.help.text9"/><br><br>

                <g:message code="app.label.PublisherTemplate.additionalSources.help.text10"/><br>
                <code>
                            GET https://httpbin.org/ip<br>
                            header Accept: application/json<br>
                </code><br>
                <g:message code="app.label.PublisherTemplate.additionalSources.help.text11"/> <br>
                <code>
                            POST https://httpbin.org/post<br>
                            header Content-Type: application/x-www-form-urlencoded<br>
                            body id=999&value=content<br>
                </code><br>
                <g:message code="app.label.PublisherTemplate.additionalSources.help.text12"/>  <br>
                <code>
                            GET https://httpbin.org/basic-auth/user/passwd<br>
                            header Authorization: Basic username password<br>
                </code><br>
                <g:message code="app.label.PublisherTemplate.additionalSources.help.text13"/><br>
                <code>
                            POST https://httpbin.org/post<br>
                            header Content-Type: application/json<br>
                            body {<br>
                            "token": "my-secret-token"<br>
                            }<br>
                            var auth_token=response.json.token<br>
                            GET https://httpbin.org/headers<br>
                            header Authorization: Bearer {{auth_token}}<br>
                </code>

            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default " data-dismiss="modal"><g:message code="app.button.close"/></button>
            </div>

        </div>

    </div>
</div>
<g:render template="/periodicReport/includes/publisherWarningModal"/>

