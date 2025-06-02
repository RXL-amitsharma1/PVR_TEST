<%@ page import="com.rxlogix.enums.CommentTypeEnum; com.rxlogix.enums.QualityEntryTypeEnum; com.rxlogix.enums.QualityIssueTypeEnum; com.rxlogix.enums.QualityTypeEnum; groovy.json.JsonOutput" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.pvc.task.caseform.title"/></title>

    <style>
        .case-form-header-bar {
            z-index: 6;
            background-color: #FFFFFF;
            padding: 4px 46px 0 0;
            position: fixed;
            top: 70px;
            width: 100%;
        }
        .case-form-header-icon-control {
            margin: 0 8px 8px 4px;
            max-width: 12px !important;
        }
        .case-form-header-icon-control i {
            color: black;
        }
    </style>
</head>

<body>
<div class="content">
    <div class="container">
        <div class="row case-form-header-bar">
            <div class="col-md-2">
                <label style="padding-left: 5px;"><g:message code="app.caseNumber.label"/>:</label>
                <a href='${createLink(controller: 'report', action: 'drillDown')}?caseNumber=${params.caseNumber}&versionNumber=${params.versionNumber}' target="_blank">${caseNumber}</a>
            </div>
            <div class="col-md-2">
                <label><g:message code="app.label.quality.caseVersion"/>:</label> ${versionNumber}
            </div>

            <div style="position: absolute; right: 4px; height: 26px; width: 120px;">
                <div class="case-form-header-icon-control col-md-1-half">
                    <a href="javascript:void(0)" class="showHideEmptyFields">
                        <i class="fa fa-eye" aria-hidden="true" title="${message(code: 'app.label.quality.showEmptyFields')}"></i>
                    </a>
                </div>
                <div class="case-form-header-icon-control col-md-1-half">
                    <a href="javascript:void(0)" class="showHideAttachments">
                        <i class="fa fa-paperclip" aria-hidden="true" title="${message(code: 'app.label.quality.showHideAttachments')}"></i>
                    </a>
                </div>
            </div>
        </div>
        <div class="row mt-40" style="padding-bottom: 5px;">
            <div class="col-md-12">
            <table class="caseContentTable"  style="width: 100%; padding: 5px">
                <tr>
                    <td class="resizable" style="width:50%;vertical-align: top;padding-right: 3px;;">
                        <div class=" caseContent" style="width: 100%; height: 100%;overflow-y: auto;overflow-x: hidden">
                            <g:render template="/quality/includes/caseFormCaseInfo" />
                        </div>
                    </td>
                    <td class="resizable">
                        <div class="attachmentContent"  style="width: 100%; height: 100%;padding-left: 3px;">
                            <g:render template="/quality/includes/caseFormAttachment" />
                        </div>
                    </td>
                </tr>

        </table>
        </div>
        </div>
    </div>
</div>

<g:javascript type="text/javascript">
    var downloadUrl = "${createLink(controller: "quality", action: "viewSourceDocument")}";
    var caseNumber = '${params.caseNumber}';
    var versionNumber = '${params.versionNumber}';
</g:javascript>
<asset:javascript src="vendorUi/shorten/jquery.shorten.js"/>
<asset:javascript src="app/reasonOfDelay/pvc_case_form.js"/>
<asset:javascript src="app/configuration/configurationCommon.js"/>

</body>
</html>