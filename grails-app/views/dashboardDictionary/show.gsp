<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="Dashboard"/>
    <title><g:message code="default.view.title" args="[entityName]"/></title>
</head>

<body>
    <div class="content">
        <div class="container">
            <div>
                <rx:container title="${message(code: 'default.view.label', args: [entityName])}">

                    <g:render template="/includes/layout/flashErrorsDivs" bean="${dashboard}" var="theInstance"/>

                    <div class="container-fluid">
                        <div class="row">
                            <div class="col-xs-12">
                                <div class="row">
                                    <div class="col-xs-6">
                                        <div class="row">
                                            <div class="col-xs-12">
                                                <label><g:message code="app.label.dashboardDictionary.label"/></label>

                                                <div>${dashboard.label}</div>
                                            </div>
                                        </div>

                                        <div class="row">
                                            <div class="col-xs-12">
                                                <label><g:message code="app.label.dashboardDictionary.owner"/></label>

                                                <div>${dashboard.owner}</div>
                                            </div>
                                        </div>

                                        <div class="row">
                                            <div class="col-xs-12">
                                                <label><g:message code="app.label.dashboardDictionary.type"/></label>

                                                <div>${g.message(code:dashboard.dashboardType.getI18nKey())}</div>
                                            </div>
                                        </div>

                                        <div class="row">
                                            <div class="col-xs-12">
                                                <label><g:message code="app.label.dashboardDictionary.sharedWithUsers"/></label>

                                                <div>${dashboard.sharedWith.collect{it.fullName}.join(", ")}</div>
                                            </div>
                                        </div>
                                        <div class="row">
                                            <div class="col-xs-12">
                                                <label><g:message code="app.label.dashboardDictionary.sharedWithGroups"/></label>

                                                <div>${dashboard.sharedWithGroup.collect{it.name}.join(", ")}</div>
                                            </div>
                                        </div>


                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-xs-12">
                                <div class="pull-right">
                                    <g:link controller="dashboardDictionary" action="edit" id="${dashboard.id}"
                                            class="btn btn-primary"><g:message
                                            code="default.button.edit.label"/></g:link>
                                    <g:link url="${createLink(controller: 'dashboard', action: 'index')+"?id="+dashboard.id}"
                                            class="btn pv-btn-grey"><g:message code="app.label.dashboard.setup"/></g:link>
                                    <g:link url="#" data-toggle="modal" data-target="#deleteModal" data-instancetype="dashboard"
                                            data-instanceid="${dashboard.id}"
                                            data-instancename="${dashboard.label}"
                                            class="btn pv-btn-grey"><g:message code="default.button.delete.label"/></g:link>
                                </div>
                            </div>
                        </div>
                    </div>
                    <br><br><br>
                    <textarea style="width: 100%; height: 150px">${raw(json)}</textarea>
                    <g:render template="/includes/widgets/dateCreatedLastUpdated" bean="${dashboard}"
                              var="theInstance"/>

                    <g:form controller="${controller}" method="delete">
                        <g:render template="/includes/widgets/deleteRecord"/>
                    </g:form>

                </rx:container>
            </div>

        </div>
    </div>
</body>
</html>