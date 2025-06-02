<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="Advanced Assignment" />
    <title><g:message code="app.advanced.assignment.create.title"/></title>
    <asset:javascript src="app/advancedAssignment.js"/>
</head>

<body>

<div class="container">
    <div class="content">
        <div>
            <div class="col-sm-12">
                <div class="page-title-box">
                    <div class="fixed-page-head">
                        <div class="page-head-lt">
                            <div class="col-md-12">
                                <h5 class="page-header-settings"><g:message code="default.create.label" args="[entityName]"/></h5>
                            </div>
                        </div>


                    </div>
                </div>
            </div>
        </div>
        <div  class="settings-content">
            <rx:container title="${message(code: 'app.label.advanced.assignment.appName')}">
    <div class="container">
      <div class="col-md-12">
    <g:render template="/includes/layout/flashErrorsDivs" bean="${advancedAssignmentInstance}" var="theInstance"/>

    <div class="alert alert-danger alert-dismissible forceLineWrap" role="alert" id="WarningDiv"
         style="display: none">
        <button type="button" class="close WarningDivclose">
            <span aria-hidden="true">&times;</span>
            <span class="sr-only"><g:message code="default.button.close.label"/></span>
        </button>

        <p></p>
    </div>


    <g:form method="post" action="save" class="form-horizontal" name="advancedAssignmentForm">
        <g:render template="includes/form" model="[mode:'create', ownerUsername: ownerUsername, ownerUserId: ownerUserId,
                                                   advancedAssignmentInstance:advancedAssignmentInstance]"/>
       <div class="row">
           <div class="col-md-12">
               <div class="buttonBar">
                   <div class="pull-right">
                       <button name="saveButton" id="saveButton" class="btn btn-primary">
                           ${message(code: 'default.button.save.label')}
                       </button>
                       <button type="button" class="btn pv-btn-grey" data-evt-clk='{"method": "goToUrl", "params": ["advancedAssignment", "index"]}'
                               id="cancelButton">${message(code: "default.button.cancel.label")}</button>
                   </div>
               </div>
           </div>
       </div>
    </g:form>
</rx:container>
      </div>
     </div>
    </div>
  </div>
</div>
</body>
</html>