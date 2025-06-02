<script>
    <g:set var="oneDriveService" bean="oneDriveRestService"/>
    var authUrl = "${grailsApplication.config.oneDrive.auth.url}?client_id=${grailsApplication.config.oneDrive.clientId}&scope=files.readwrite.all%20Sites.ReadWrite.All%20ChannelMessage.Send&state=webform&&response_type=code&redirect_uri=${oneDriveService.getCallBackUrl()}"
    var authUrlWopi = "${grailsApplication.config.oneDrive.auth.url}?client_id=${grailsApplication.config.oneDrive.clientId}&scope=files.readwrite.all%20Sites.ReadWrite.All&response_type=code&redirect_uri=${oneDriveService.getCallBackUrl()}&state=wopi_"
    var oneDriveWopiProvider = ${grailsApplication.config.officeOnline.oneDriveProvider.enabled};
    var oneDriveFoldersUrl = "${createLink(controller: "oneDrive", action: "folders")}";
    var listSitesUrl = "${createLink(controller: "oneDrive", action: "listSites")}";
    var oneDriveUploadUrl = "${createLink(controller: "oneDrive", action: "upload")}${_csrf?("?"+_csrf?.parameterName+"="+_csrf?.token):""}";
    var checkOneDriveLoginUrl = "${createLink(controller: "oneDrive", action: "checkLogin")}";
    var oneDriveNewFolderUrl = "${createLink(controller: "oneDrive", action: "newFolder")}";
    var uploadEntityToOnedriveUrl = "${createLink(controller: "wopi", action: "uploadEntityToOnedrive")}";
</script>

<div id="downloadModal" class="modal fade" role="dialog">
    <div class="modal-dialog">

        <!-- Modal content-->
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h5 class="modal-title modalHeader"><span class="selectMode"><g:message code="app.label.oneDrive.selectFolder"/></span><span class="downloadMode modalHeaderSpan"></span></h5>
            </div>

            <div class="modal-body" style="position: relative" >
                <div class="custom-loader pvloader" style="position: absolute"></div>

                <div class="row">
                    <div class="col-md-1 hideOnLogin" style="margin-top: 10px;"><label><g:message code="app.label.oneDrive.site" default="Site:"/></label></div>

                    <div class="col-md-11">
                        <div id="oneDriveSiteList" >

                        </div>
                    </div>
                </div>

                <div id="oneDriveFrame" style="height: 250px;">

                </div>
            </div>

            <div class="modal-footer">
                <div class="row downloadMode" style="margin-bottom: 10px;margin-top: -10px;">
                    <div class="col-md-2 hideOnLogin" style="margin-top: 10px;"><label><g:message code="app.label.oneDrive.uploadAs" default="Upload As"/></label></div>
                    <div class="col-md-10 hideOnLogin"><input id="oneDriveUploadAs" class="form-control"></div>
                </div>
                <a href="javascript:void(0)" class="btn btn-primary selectOneDriveFolder selectMode"> <g:message code="app.label.oneDrive.selectFolder"/></a>
                <a href="javascript:void(0)" class="btn btn-primary downloadFileRef downloadMode"><span class="fa fa-download"></span> <g:message code="app.label.download"/></a>
                <a href='javascript:void(0)' class='btn btn-primary uploadToOneDrive downloadMode'><span class="fa fa-upload"></span> <g:message code="app.label.oneDrive.uploadToOneDrive"/></a>
                <button type="button" class="btn btn-default " data-dismiss="modal"><g:message code="app.button.close"/></button>

            </div>
        </div>

    </div>
</div>

<div id="addFolderOneDrive" class="modal fade" role="dialog">
    <div class="modal-dialog " role="document">
        <div class="modal-content">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal">&times;</button>
            <h5 class="modal-title"><g:message code="app.label.oneDrive.createFolder" default="Create folder"/></h5>
        </div>
        <div class="modal-body">
            <input class="form-control oneDriveNewFolderName">
        </div>
        <div class="modal-footer">
            <button type="button" class="btn btn-primary createOneDriveFolder" data-dismiss="modal"><g:message code="default.button.ok.label"/></button>
            <button type="button" class="btn btn-default " data-dismiss="modal"><g:message code="default.button.cancel.label"/></button>
        </div>
        </div>
    </div>
</div>
<asset:javascript src="/app/oneDrive/oneDrive.js"/>