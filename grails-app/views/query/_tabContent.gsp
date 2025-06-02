<%@ page import="com.rxlogix.config.SourceProfile; com.rxlogix.util.ViewHelper;com.rxlogix.config.Query; com.rxlogix.reportTemplate.ReassessListednessEnum; com.rxlogix.enums.QueryTypeEnum; com.rxlogix.util.DateUtil" %>
<style>
#newTabContent .nav .active a {
    color: #fff!important;
    padding: 0 10px;
    background: #4c5667!important;
    border: 0 solid #ccc!important;
    margin-top: 5px!important;
    border-radius: 18px!important;
    line-height: 25px!important;
    font-weight: 300!important;
    margin-right: 5px !important;
    margin-left: 5px !important;
}

#newTabContent .nav li > a {
    font-size: 12px!important;
    padding: 0 10px;
    color: #414658;
    line-height: 23px!important;
    font-weight: 600!important;
    border: 1px solid #b9b5b5!important;
    background: 0 0!important;
    margin-top: 5px!important;
    border-radius: 18px!important;
    margin-right: 5px !important;
    margin-left: 5px !important;
}
</style>
<rx:container title="&nbsp;" >
<div role="tabpanel" id="newTabContent" xmlns="http://www.w3.org/1999/html" style="margin-top: -42px;    margin-left: -10px;margin-right: -10px;">

    <!-- Nav tabs -->
    <ul class="nav nav-tabs" role="tablist">
        <li role="presentation" class="active" id="queryBuilderTab"><a href="#queryBuilder" aria-controls="QUERY_BUILDER" role="tab"
            data-toggle="tab" id="queryBuilderLink"><g:message code="app.queryType.QUERY_BUILDER"/></a></li>
        <sec:ifAnyGranted roles="ROLE_QUERY_ADVANCED">
            <li role="presentation" id="setBuilderTab"><a href="#setBuilder" aria-controls="SET_BUILDER" role="tab"
                                                         data-toggle="tab" id="setBuilderLink"><g:message code="app.queryType.SET_BUILDER"/></a></li>
            <li role="presentation" id="customSQLTab"><a href="#customSQL" aria-controls="CUSTOM_SQL" role="tab"
                data-toggle="tab" id="customSQLLink"><g:message code="app.queryType.CUSTOM_SQL"/></a></li>
        </sec:ifAnyGranted>
    </ul>

    <!-- Tab panes -->
    <div class="tab-content">
        <div role="tabpanel" class="tab-pane active tabBorder tabBorderFirst" id="queryBuilder">
            <div class="row">
                <div class="col-xs-10">
                    <g:if test="${editable}">
                        <div class="row">
                            <div class="col-xs-2">
                                <label><g:message code="app.label.queryCriteria"/><span class="required-indicator">*</span></label>
                            </div>

                            <g:if test="${sourceProfiles?.size() > 1}">
                                <div class="col-xs-4 ">
                                    <div class="form-inline">
                                        <label><g:message code="userGroup.source.profiles.label"/></label>
                                        <g:select name="sourceProfile.id" id="sourceProfile" style="width: 100px"
                                                  from="${sourceProfiles}"
                                                  optionValue="sourceName" optionKey="sourceId"
                                                  value="${SourceProfile.fetchAllDataSource().sourceId}"
                                                  class="form-control"/>
                                    </div>
                                </div>
                            </g:if>
                        </div>

                        <div class="form-group row loading">
                            <i class="fa fa-refresh fa-spin"></i>
                        </div>
                    </g:if>

                    <div class="form-group row doneLoading addContainerTopmost" id="addContainerWithSubmit">
                        <g:render template="/query/toAddExtraValues" />
                        <g:render template="/query/toAddContainer"
                                  model="[sourceProfiles: sourceProfiles, currentUser: currentUser]"/>
                    </div>

                    <div hidden="hidden">
                        <input name="JSONQuery" id="queryJSON" value="${query?.queryType == QueryTypeEnum.QUERY_BUILDER ? query?.JSONQuery : null}"/>
                        <a href="http://jsonlint.com/" target="_blank" rel="noopener noreferrer">Prettify my JSON here!</a>

                        <div>has blanks?
                            <input name="hasBlanks" id="hasBlanksQuery" value=""/>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="loading container">
                    <i class="fa fa-refresh fa-spin"></i>
                </div>
                <div id="builderAll" class="builderAll doneLoading">
                </div>
            </div>
            <div id="extraOptions" class="row extraOptions" hidden="hidden">
                <div class="col-xs-3">
                    <label><g:message code="app.label.reassessListedness"/>: </label>
                    <g:select class="form-control" name="reassessListedness"
                              from="${ViewHelper.getReassessListedness()}" optionKey="name" optionValue="display"
                              value="${query?.queryType == QueryTypeEnum.QUERY_BUILDER ? query?.reassessListedness : null }"/>
                </div>

                <div class="col-xs-2" id="customDateSelector" style="display: ${query instanceof Query && query?.reassessListedness && query?.reassessListedness == ReassessListednessEnum.CUSTOM_START_DATE ? 'block' : 'none'}">
                    <label></label>
                    <div class="fuelux p-t-5">
                        <div class="datepicker input-group" id="customDatePicker">
                            <g:textField name="reassessListednessDate" class="form-control"
                            value="${renderShortFormattedDate(date: query instanceof Query ? query?.reassessListednessDate : null)}"
                            />
                            <g:render template="/includes/widgets/datePickerTemplate"/>
                        </div>
                    </div>
                </div>

                <div class="col-xs-4">
                    <label></label>
                    <div class="checkbox checkbox-primary p-t-10">
                        <g:checkBox name="reassessForProduct"
                                    value="${query?.queryType == QueryTypeEnum.QUERY_BUILDER ? query?.reassessForProduct : false }"/>
                        <label for="reassessForProduct">
                            <g:message code="app.label.reassessForProduct"/>
                        </label>
                    </div>
                </div>
            </div>
            <g:render plugin="pv-dictionary" template="/plugin/dictionary/dictionaryModals"/>
        </div>


        <div role="tabpanel" class="tab-pane tabBorder" id="setBuilder">
            <div class="row">
                <div class="col-xs-7">
                    <g:if test="${editable}">
                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="app.label.querySelect"/><span class="required-indicator">*</span></label>
                            </div>
                        </div>

                        <div class="form-group row loading">
                            <i class="fa fa-refresh fa-spin"></i>
                        </div>
                    </g:if>

                    <div class="form-group row doneLoading addContainerTopmost" id="addContainerWithSubmit">
                        <g:render template="/query/toAddContainerSet" model="['editable': editable, 'isExecuted': isExecuted, query: query]" />

                        <div class="col-xs-1 expressionsNoPad">
                            <g:submitButton type="button" name="Add" id="addQuery" value="${message(code: "default.button.add.label")}" class="btn btn-primary" />
                        </div>
                    </div>

                    <div hidden="hidden">
                        <input name="JSONQuery" id="setJSON" value="${query?.queryType == QueryTypeEnum.SET_BUILDER ? query?.JSONQuery : null}"/>
                        <a href="http://jsonlint.com/" target="_blank" rel="noopener noreferrer"><g:message code="prettify.my.json.here" /></a>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="loading container">
                    <i class="fa fa-refresh fa-spin"></i>
                </div>
                <div id="setBuilderAll" class="builderAll doneLoading">
                </div>
            </div>
        </div>

        <div role="tabpanel" class="tab-pane tabBorder" id="customSQL">
            <g:if test="${editable}">
                <pre class="textSegment"><g:message code="${grailsApplication.config.source.profile.lam.irt.enabled ? "tabContent.select.multiple.datasources" : "tabContent.select"}" /></pre>
                <div class="expandingArea">
                    <pre><span></span><br></pre>
                    <g:textArea class="sqlBox form-control" name="customSQLQuery" value="${query?.queryType == QueryTypeEnum.CUSTOM_SQL ? query?.customSQLQuery : null}"/>
                </div>

                <div class="bs-callout bs-callout-info">
                    <h5><g:message code="app.label.note" />:</h5>
                    <div><g:message code="app.query.customSQL.skipValidation" /></div>
                </div>

                <div class="bs-callout bs-callout-info">
                    <h5><g:message code="example" />:</h5>
                    <div class="text-muted"><pre>where cm.occured_country_id = 223</pre></div>
                </div>
            </g:if>
            <g:else>
                <pre><g:message code="${grailsApplication.config.source.profile.lam.irt.enabled ? "tabContent.select.multiple.datasources" : "tabContent.select"}" /></pre>
                <pre>${query?.queryType == QueryTypeEnum.CUSTOM_SQL ? query?.customSQLQuery : null}</pre>
            </g:else>
        </div>
    </div>

    <div hidden="hidden">
        <input name="queryType" id="queryType" value="${query?.queryType}"/>
    </div>

</div>

<g:render plugin="pv-dictionary" template="/plugin/dictionary/copyPasteModal"/>
<asset:javascript src="/plugin/dictionary/dictionaryMultiSearch.js"/>

    </rx:container>
