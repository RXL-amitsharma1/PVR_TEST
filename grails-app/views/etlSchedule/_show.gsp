<%@ page import="com.rxlogix.enums.EtlStatusEnum; java.text.SimpleDateFormat;com.rxlogix.config.SourceProfile; com.rxlogix.util.DateUtil; grails.util.Environment; com.rxlogix.Constants; org.apache.commons.lang3.text.WordUtils; com.rxlogix.enums.EtlStatusEnum" %>
<!doctype html>
<html>
<body>

<g:set var="column1Width" value="4"/>
<g:set var="column2Width" value="8"/>
<g:set var="startDT"
       value="${DateUtil.StringToDate(etlScheduleInstance?.startDateTime, Constants.DateFormat.WITHOUT_SECONDS)}"/>

<sec:ifAnyGranted roles="ROLE_ADMIN">

    <div class="horizontalRuleFull"></div>

    <div class="row">
        <div class="col-md-7" col-xs-offset-6>

            <div class="row">
                <div class="col-md-${column1Width}"><label><g:message code="etlMaster.scheduleName.label"/></label>
                </div>

                <div class="col-md-${column2Width}">${etlScheduleInstance?.scheduleName}</div>
            </div>

            <div class="row">
                <div class="col-md-${column1Width}"><label><g:message code="etlScheduler.start.date"/></label></div>

                <div class="col-md-${column2Width}"><g:render template="/includes/widgets/dateDisplayWithTimezone"
                                                              model="[date: startDT]"/></div>
            </div>

            <g:each in="${etlScheduleInstance?.repeatInterval?.split(';')}">
                <div class="row">
                    <div class="col-md-${column1Width}">
                        <g:if test="${it?.split('=').head().equals('BYDAY')}"><label><g:message
                                code="etlschedule.by.day"/></label></g:if>
                        <g:elseif test="${it?.split('=').head().equals('BYMONTH')}"><label><g:message
                                code="etlschedule.by.month"/></label></g:elseif>
                        <g:elseif test="${it?.split('=').head().equals('BYMONTHDAY')}"><label><g:message
                                code="etlschedule.day.of.month"/></label></g:elseif>
                        <g:elseif test="${it?.split('=').head().equals('BYSETPOS')}"><label><g:message
                                code="etlSchedule.by.set.pos"/></label></g:elseif>
                        <g:elseif test="${it?.split("=").head().equals('FREQ')}">
                            <label><g:message code="etl.schedule.repeat.frequency"/></label>
                        </g:elseif>
                        <g:elseif test="${it?.split("=").head().equals('INTERVAL')}">
                            <label><g:message code="etl.schedule.repeat.interval"/></label>
                        </g:elseif>
                        <g:else>
                            <label>${WordUtils.capitalizeFully(it?.split("=").head())}</label>
                        </g:else>
                    </div>
                    <div class="col-md-${column2Width}">
                        <g:if test="${it.split("=").head().equals('UNTIL')}">
                            ${DateUtil.SimpleDateReformat(it.split("=").tail().first(), Constants.DateFormat.BASIC_DATE, com.rxlogix.Constants.DateFormat.SIMPLE_DATE)}
                        </g:if>
                        <g:elseif test="${it.split("=").head().equals('FREQ')}">
                            <label class="repeatInterval"><g:message code="etl.schedule.repeat.${WordUtils.capitalizeFully(it.split("=").tail().first())}"/></label>
                        </g:elseif>
                        <g:elseif test="${it.split("=").head().equals('BYDAY')}">
                            ${WordUtils.capitalizeFully(it.split("=").tail().first(), ",".toCharArray())}
                        </g:elseif>
                        <g:else>
                            <g:if test="${it.split("=").tail().size() != 0}">
                                ${it.split("=").tail().first()}
                            </g:if>
                        </g:else>
                    </div>
                </div>
            </g:each>

            <div class="row">
                <div class="col-md-${column1Width}"><label><g:message code="default.button.enable.label"/></label></div>

                <div class="col-md-${column2Width}"><g:formatBoolean boolean="${etlScheduleInstance?.isDisabled}"
                                                                     false="${message(code: "default.button.yes.label")}" true="${message(code: "default.button.no.label")}"/></div>
            </div>

            <div class="row">
                <div class="col-md-${column1Width}"><label><g:message code="pre.mart.etl.execution.status"/></label></div>

                <div class="col-md-${column2Width} preMartEtlStatus">
                    <g:if test="${isPreMartStatusApplicable}">
                        <g:if test="${!preMartEtlStatus}">
                            <span><g:message code="app.label.none"/></span>
                        </g:if>
                        <g:elseif test="${EtlStatusEnum.SUCCESS == preMartEtlStatus}">
                            <span class="label label-success"><g:message code="${preMartEtlStatus.i18nKey}"/></span>
                        </g:elseif>
                        <g:elseif test="${EtlStatusEnum.RUNNING == preMartEtlStatus}">
                            <span class="label label-primary"><g:message code="${preMartEtlStatus.i18nKey}"/></span>
                        </g:elseif>
                        <g:elseif test="${EtlStatusEnum.FAILED == preMartEtlStatus}">
                            <span class="label label-danger"><g:message code="${preMartEtlStatus.i18nKey}"/></span>
                        </g:elseif>
                        <g:else>
                            <span class="label label-primary"><g:message code="${preMartEtlStatus.i18nKey}"/></span>
                        </g:else>
                    </g:if>
                    <g:else>
                        <span class="label label-default"><g:message code="app.label.not.applicable"/></span>
                    </g:else>
                </div>
            </div>

            <div class="row">
                <div class="col-md-${column1Width}"><label><g:message code="pre.mart.etl.lastRun.dateTime"/></label></div>

                <div class="col-md-${column2Width}" preMartLastRunDateTime>
                    <g:if test="${isPreMartStatusApplicable}">
                        <g:if test="${!preMartEtlStatus}">
                            <span><g:message code="app.label.none"/></span>
                        </g:if>
                        <g:else>
                            <g:render template="/includes/widgets/dateDisplayWithTimezone"
                                      model="[date: preMartLastRunDateTime]"/>
                        </g:else>
                    </g:if>
                    <g:else>
                        <span class="label label-default"><g:message code="app.label.not.applicable"/></span>
                    </g:else>
                </div>
            </div>


            <g:if test="${isAffEtlStatusApplicable}">
                <div class="row">
                    <div class="col-md-${column1Width}"><label><g:message code="affiliate.etl.execution.status"/></label></div>

                    <div class="col-md-${column2Width} affEtlStatus">
                        <g:if test="${!affEtlStatus}">
                            <span><g:message code="app.label.none"/></span>
                        </g:if>
                        <g:elseif test="${EtlStatusEnum.SUCCESS == affEtlStatus}">
                            <span class="label label-success"><g:message code="${affEtlStatus.i18nKey}"/></span>
                        </g:elseif>
                        <g:elseif test="${EtlStatusEnum.RUNNING == affEtlStatus}">
                            <span class="label label-primary"><g:message code="${affEtlStatus.i18nKey}"/></span>
                        </g:elseif>
                        <g:elseif test="${EtlStatusEnum.FAILED == affEtlStatus}">
                            <span class="label label-danger"><g:message code="${affEtlStatus.i18nKey}"/></span>
                        </g:elseif>
                        <g:elseif test="${EtlStatusEnum.ETL_PAUSED == etlStatus}">
                            <span class="label label-primary"><g:message code="${etlStatus.i18nKey}"/></span>
                        </g:elseif>
                        <g:else>
                            <span class="label label-primary"><g:message code="${affEtlStatus.i18nKey}"/></span>
                        </g:else>
                    </div>
                </div>

                <div class="row">
                    <div class="col-md-${column1Width}"><label><g:message code="affiliate.etl.lastRun.dateTime"/></label></div>

                    <div class="col-md-${column2Width}">
                        <g:if test="${!affEtlStatus}">
                            <span><g:message code="app.label.none"/></span>
                        </g:if>
                        <g:else>
                            <g:render template="/includes/widgets/dateDisplayWithTimezone"
                                      model="[date: affEtlLastRunDateTime]"/>
                        </g:else>
                    </div>
                </div>
            </g:if>



            <div class="row">
                <div class="col-md-${column1Width}"><label><g:message code="etl.execution.status"/></label></div>

                <div class="col-md-${column2Width} etlStatus">
                    <g:if test="${!etlStatus}">
                        <span><g:message code="etl.execution.no.status"/></span>
                    </g:if>
                    <g:elseif test="${EtlStatusEnum.SUCCESS == etlStatus}">
                        <span class="label label-success"><g:message code="${etlStatus.i18nKey}"/></span>
                    </g:elseif>
                    <g:elseif test="${EtlStatusEnum.RUNNING == etlStatus}">
                        <span class="label label-primary"><g:message code="${etlStatus.i18nKey}"/></span>
                    </g:elseif>
                    <g:elseif test="${EtlStatusEnum.FAILED == etlStatus}">
                        <span class="label label-danger"><g:message code="${etlStatus.i18nKey}"/></span>
                    </g:elseif>
                    <g:elseif test="${EtlStatusEnum.ETL_PAUSED == etlStatus}">
                        <span class="label label-primary"><g:message code="${etlStatus.i18nKey}"/></span>
                    </g:elseif>
                    <g:else>
                        <span class="label label-primary"><g:message code="${etlStatus.i18nKey}"/></span>
                    </g:else>
                </div>
            </div>

            <div class="row">
                <div class="col-md-${column1Width}"><label><g:message code="etl.lastRun.dateTime"/></label></div>

                <div class="col-md-${column2Width}">
                    <g:render template="/includes/widgets/dateDisplayWithTimezone"
                              model="[date: lastRunDateTime]"/>
                </div>
            </div>

        </div>
    </div>

    <div class="modal fade" id="initialEtlModal"  data-backdrop="static" tabindex="-1" role="dialog">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                            aria-hidden="true">&times;</span></button>
                    <h4 class="modal-title"><g:message code="run.initial.etl"/></h4>
                </div>
                <div class="modal-body">
                    <p><g:message code="initialize.etl.now"/></p>
                </div>
                
                <div class="modal-footer">
                    <button type="button" class="btn pv-btn-grey" data-dismiss="modal"><g:message
                            code="default.button.cancel.label"/></button>
                    <button class="btn btn-primary" data-evt-clk='{"method": "goToUrl", "params": ["etlSchedule", "initialize"]}' id="copyBtn">
                        ${message(code: "default.button.changeowner.label")}
                    </button>
                </div>
            </div><!-- /.modal-content -->
        </div><!-- /.modal-dialog -->
    </div><!-- /.modal -->

            <!-- Modal -->
    <div class="modal fade" id="pauseEtlModal"  data-backdrop="static" tabindex="-1" role="dialog">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                            aria-hidden="true">&times;</span></button>
                    <h4 class="modal-title"><g:message code="pause.initial.etl"/></h4>
                </div>

                <div class="modal-body">
                    <div class="alert alert-danger alert-dismissible forceLineWrap" role="alert" id="pauseEtlErrorDiv" style="display: none">
                        <button type="button" class="close" data-dismiss="alert">
                            <span aria-hidden="true">&times;</span>
                            <span class="sr-only"><g:message code="default.button.close.label"/></span>
                        </button>
                        <i class="fa fa-check"></i> <g:message code="app.label.justification.cannotbeblank"/>
                    </div>
                    <p><g:message code="pause.etl.now"/></p>
                    <label style="margin-top: 25px"><g:message code="app.label.justification"/>:</label>
                    <textarea placeholder="<g:message code="placeholder.justification.label"/>" class="form-control" name="pauseJustification" id="pauseJustification" maxlength="4000"></textarea>

                </div>

                <div class="modal-footer">
                    <button type="button" class="btn pv-btn-grey" data-dismiss="modal"><g:message
                            code="default.button.cancel.label"/></button>
                    <button class="btn btn-primary" id="pausedBtn">
                        ${message(code: "default.button.changeowner.label")}
                    </button>
                </div>
            </div><!-- /.modal-content -->
        </div><!-- /.modal-dialog -->
    </div><!-- /.modal -->

            <!-- Modal -->
    <div class="modal fade" id="resumeEtlModal"  data-backdrop="static" tabindex="-1" role="dialog">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                            aria-hidden="true">&times;</span></button>
                    <h4 class="modal-title"><g:message code="run.initial.etl"/></h4>
                </div>

                <div class="modal-body">
                    <p><g:message code="resume.etl.now"/></p>
                </div>

                <div class="modal-footer">
                    <button type="button" class="btn pv-btn-grey" data-dismiss="modal"><g:message
                            code="default.button.cancel.label"/></button>
                    <button class="btn btn-primary" data-evt-clk='{"method": "goToUrl", "params": ["etlSchedule", "resumeEtl"]}' id="resumedBtn">
                        ${message(code: "default.button.changeowner.label")}
                    </button>
                </div>
            </div><!-- /.modal-content -->
        </div><!-- /.modal-dialog -->
    </div><!-- /.modal -->

</sec:ifAnyGranted>

</body>
</html>
