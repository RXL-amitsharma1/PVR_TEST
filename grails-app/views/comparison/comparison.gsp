<%@ page import="grails.converters.JSON; com.rxlogix.util.ViewHelper; com.rxlogix.enums.PeriodicReportTypeEnum; com.rxlogix.config.PeriodicReportConfiguration; com.rxlogix.config.ReportConfiguration; grails.plugin.springsecurity.SpringSecurityUtils" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.comparison.title"/></title>
    <asset:javascript src="app/configuration/configurationCommon.js"/>
</head>

<body>
<div class="content">
    <div class="container">
        <div class="pv-caselist">
            <rx:container title="Comparison of ${result.reportName1} and ${result.reportName2}">

                <g:render template="/includes/layout/flashErrorsDivs"/>
                <b><g:message code="app.comparison.title"/></b> <b style="color: ${result.reportsAreEqual ? "green" : "red"} ">
                <g:if test="${result.reportsAreEqual}"><g:message code="app.comparison.equal"/></g:if><g:else><g:message code="app.comparison.notequal"/></g:else></b>
                <g:if test="${!result.reportsAreEqual}">
                    <br><br>


                    <g:each in="${result.getSections()}" var="section" status="i">

                        <div class="rxmain-container rxmain-container-top">
                            <div class="rxmain-container-inner">
                                <div class="rxmain-container-row rxmain-container-header" style="margin-left: ${section.hasParent ? '20px' : '0'}">

                                    <i class="fa fa-caret-right fa-lg click" data-evt-clk='{"method": "hideShowContent", "params": []}'></i>
                                    <label class="rxmain-container-header-label click" data-evt-clk='{"method": "hideShowContent", "params": []}'>
                                        ${section.title}
                                        <g:if test="${!section.supported}">
                                            :<b style="color: red"><g:message code="app.comparison.notSupported"/></b>
                                        </g:if>
                                        <g:else>
                                            :<b style="color: ${section.sectionsAreEqual ? "green" : "red"} "><g:if test="${section.sectionsAreEqual}"><g:message code="app.comparison.sectionsEquals"/></g:if><g:else><g:message code="app.comparison.sectionsNotEquals"/></g:else></b>
                                        </g:else>
                                    </label>
                        <g:if test="${section.supported && !section.sectionsAreEqual && (section.type in["DT", "CLL"])}">
                                    <a href="${createLink(controller: "comparison", action: "exportToExcel")}?id=${params.id}&index=${i}" style='cursor: pointer' class='fa fa-download pull-right pt-5 p-r-5'></a>
                        </g:if>
                                </div>

                                <div class="rxmain-container-content rxmain-container-hide" style="overflow-x: auto;">
                                    <g:if test="${section.supported && !section.sectionsAreEqual}">
                                        ${raw(section.log.toString())}
                                        <g:if test="${section.type == "DT"}">
                                            <table class="table" style="width: 100%">
                                                <thead>
                                                <tr>
                                                    <g:each in="${section.header}" var="th">
                                                        <th>${raw(th)}</th>
                                                    </g:each>
                                                </tr>
                                                </thead>
                                                <tbody>
                                                <tr><td colspan="${section.header.size()}"><b>${section.getReport1Row()?.size() ?: 0} <g:message code="app.comparison.uniqueRows"/> ${result.reportName1}</b>
                                                </td>
                                                </tr>
                                                <g:each in="${section.getReport1Row()?.sort()}" var="tr">
                                                    <tr>
                                                        <g:set var="row" value="${JSON.parse(tr)}"/>
                                                        <g:each in="${section.headerKeys}" var="td">
                                                            <td>${raw(row[td])}</td>
                                                        </g:each>
                                                    </tr>
                                                </g:each>
                                                <tr><td colspan="${section.header.size()}"><b>${section.getReport2Row()?.size() ?: 0} <g:message code="app.comparison.uniqueRows"/> ${result.reportName2}</b>
                                                </td>
                                                </tr>
                                                <g:each in="${section.getReport2Row()?.sort()}" var="tr">
                                                    <tr>
                                                        <g:set var="row" value="${JSON.parse(tr)}"/>
                                                        <g:each in="${section.headerKeys}" var="td">
                                                            <td>${raw(row[td])}</td>
                                                        </g:each>
                                                    </tr>
                                                </g:each>
                                                </tbody>
                                            </table>
                                        </g:if>
                                        <g:elseif test="${section.type == "CLL"}">
                                            <table class="table">
                                                <thead>
                                                <tr>
                                                    <g:each in="${section.header}" var="th">
                                                        <th>${th}</th>
                                                    </g:each>
                                                </tr>
                                                </thead>
                                                <tbody>
                                                <tr><td colspan="${section.header.size()}"><b>${section.getReport1Row()?.size() ?: 0} <g:message code="app.comparison.uniqueRows"/>  ${result.reportName1}</b>
                                                </td>
                                                </tr>
                                                <g:each in="${section.getReport1Row()}" var="tr">
                                                    <tr>
                                                        <g:each in="${tr.split("~@~")}" var="td">
                                                            <td>${td}</td>
                                                        </g:each>
                                                    </tr>
                                                </g:each>
                                                <tr><td colspan="${section.header.size()}"><b>${section.getReport2Row()?.size() ?: 0} <g:message code="app.comparison.uniqueRows"/>  ${result.reportName2}</b>
                                                </td>
                                                </tr>
                                                <g:each in="${section.getReport2Row()}" var="tr">
                                                    <tr>
                                                        <g:each in="${tr.split("~@~")}" var="td">
                                                            <td>${td}</td>
                                                        </g:each>
                                                    </tr>
                                                </g:each>
                                                </tbody>
                                            </table>
                                        </g:elseif>
                                    </g:if>
                                </div>
                            </div>
                        </div>
                    </g:each>
                </g:if>
            </rx:container>
        </div>
    </div>
</div>
</body>
