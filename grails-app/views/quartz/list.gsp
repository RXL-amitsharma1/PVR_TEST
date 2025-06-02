<%@ page import="org.quartz.Trigger;" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <g:set var="executorThreadInfoService" bean="executorThreadInfoService"/>
        <g:set var="layoutName" value="${grailsApplication.config.quartz?.monitor?.layout}" />
        <meta name="layout" content="${layoutName ?: 'main'}" />
        <title><g:message code="app.quartz.monitoring.title"/></title>
        <link rel="stylesheet" href="${resource(dir: 'css', file: 'quartz-monitor.css', plugin: 'quartz-monitor')}"/>
        <link rel="stylesheet" href="${resource(dir: 'css', file: 'jquery.countdown.css', plugin: 'quartz-monitor')}"/>
        <link rel="stylesheet" href="${resource(dir: 'css', file: 'jquery.clock.css', plugin: 'quartz-monitor')}"/>
    </head>
    <body>
        <div class="body">
            <div id="quartz-title">
                <div class="row">
                    <div class="page-title-box">
                        <div class="fixed-page-head">
                            <div class="page-head-lt" style="width: 100% !important;">
                                <div class="col-sm-4"><h5>PVR QUARTZ JOBS</h5></div>

                                <div class="col-sm-3 col-sm-offset-5 pull right">
                                <div class="col-sm-8 "><h5 class="pull-right">Global Job Control :</h5></div>

                                <div class="col-sm-4" style="margin-top: 2px;">
                                    <g:if test="${jobs.any { it.triggerStatus && it.triggerStatus != Trigger.TriggerState.PAUSED }}">
                                        <a href="<g:createLink action="stopScheduler"/>"><img class="quartz-tooltip"
                                                                                              data-tooltip="PAUSE ALL ACTIVE JOBS"
                                                                                              src="<g:resource
                                                                                                      dir="images"
                                                                                                      file="pause-all.png"
                                                                                                      plugin="quartz-monitor"/>">
                                        </a>
                                    </g:if>
                                    <g:else>
                                        <a href="<g:createLink action="startScheduler"/>"><img class="quartz-tooltip"
                                                                                               data-tooltip="START ALL PAUSED JOBS"
                                                                                               src="<g:resource
                                                                                                       dir="images"
                                                                                                       file="play-all.png"
                                                                                                       plugin="quartz-monitor"/>">
                                        </a>
                                    </g:else>
                                </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

            </div>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>

            <div class="list pv-caselist">
                <table id="quartz-jobs" style="width: 100%" class="table table-striped pv-list-table dataTable no-footer">
                    <thead>
                        <tr>
                            <th style="min-width: 250px">Name</th>
                            <g:if test="${grailsApplication.config.quartz.monitor.showTriggerNames}">
                                <th style="min-width: 150px">Trigger Name</th>
                            </g:if>
                            <th style="min-width: 200px">Last Run</th>
                            <th class="quartz-to-hide" style="min-width: 150px">Result</th>
                            <th style="min-width: 200px">Next Scheduled Run</th>
                            <th style="min-width: 200px">Actions</th>
                            <th style="min-width: 150px"></th>
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${jobs}" status="i" var="job">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                            <td>${message(code: job.name)}</td>
                            <g:if test="${grailsApplication.config.quartz.monitor.showTriggerNames}">
                                <td>${job.trigger?.name}</td>
                            </g:if>
                            <g:set var="tooltip">${job.duration >= 0 ? "Job ran in: " + job.duration + "ms" : (job.error ? "Job threw exception: " + job.error : "")}</g:set>
                            <td class="quartz-tooltip quartz-status ${job.status?:"not-run"}" data-tooltip="${tooltip}" style="padding-left: 30px!important; height: 0!important;">${job.lastRun}</td>
                            <td class="quartz-to-hide">${tooltip}</td>
                            <g:if test="${schedulerInStandbyMode || job.triggerStatus == Trigger.TriggerState.PAUSED}">
                                <td class="hasCountdown countdown_amount">Paused</td>
                            </g:if>
                            <g:else>
                                <td class="quartz-countdown" data-next-run="${job.trigger?.nextFireTime?.time ?: ""}">${job.trigger?.nextFireTime}</td>
                            </g:else>
                            <td class="quartz-actions">
                                <g:if test="${job.status != 'running'}">
                                    <g:if test="${job.trigger}">
                                        <a href="<g:createLink action="stop" params="[jobName:job.name, triggerName:job.trigger.name, triggerGroup:job.trigger.group]"/>"><img class="quartz-tooltip" data-tooltip="Stop job from running again" src="<g:resource dir="images" file="stop.png" plugin="quartz-monitor"/>"></a>
                                        <g:if test="${job.triggerStatus == Trigger.TriggerState.PAUSED}">
                                            <a class="addPriorityAttr" href="<g:createLink action="resume" params="[jobName:job.name, jobGroup:job.group]"/>"><img class="quartz-tooltip" data-tooltip="Resume job schedule" src="<g:resource dir="images" file="resume.png" plugin="quartz-monitor"/>"></a>
                                        </g:if>
                                        <g:elseif test="${job.trigger.mayFireAgain()}">
                                            <a href="<g:createLink action="pause" params="[jobName:job.name, jobGroup:job.group]"/>"><img class="quartz-tooltip" data-tooltip="Pause job schedule" src="<g:resource dir="images" file="pause.png" plugin="quartz-monitor"/>"></a>
                                        </g:elseif>
                                    </g:if>
                                    <g:else>
                                        <a class="addPriorityAttr" href="<g:createLink action="start" params="[jobName:job.name, jobGroup:job.group]"/>"><img class="quartz-tooltip" data-tooltip="Start job schedule" src="<g:resource dir="images" file="start.png" plugin="quartz-monitor"/>"></a>
                                    </g:else>
                                    <a href="<g:createLink action="runNow" params="[jobName:job.name, jobGroup:job.group]"/>"><img class="quartz-tooltip" data-tooltip="Run now" src="<g:resource dir="images" file="run.png" plugin="quartz-monitor"/>"></a>
                                    <g:if test="${job.trigger instanceof org.quartz.CronTrigger}">
                                        <a href="<g:createLink action="editCronTrigger" params="[triggerName:job.trigger.name, triggerGroup:job.trigger.group]"/>"><img class="quartz-tooltip" data-tooltip="Reschedule" src="<g:resource dir="images" file="reschedule.png" plugin="quartz-monitor"/>"></a>
                                    </g:if>
                                </g:if>
                                <g:if test="${message(code: job.name) == 'ReportsExecutorJob'}">
                                    <td>
                                        <g:if test="${!job.trigger || job.triggerStatus == Trigger.TriggerState.PAUSED}">
                                            <div class="checkbox checkbox-primary" style="padding-left:30px;">
                                                <g:checkBox id="runPriorityOnly" name="runPriorityOnly" class="quartz-tooltip" checked="${executorThreadInfoService.getStatusOfRunPriorityOnly()}"  data-tooltip="Prioritized Report Only"/>
                                                <label for="runPriorityOnly" style="padding-left: 0px;">Prioritized Only</label>
                                            </div>
                                        </g:if>
                                        <g:else>
                                            <div class="checkbox checkbox-primary" style="padding-left:30px;">
                                                <g:checkBox name="runPriorityOnly" class="quartz-tooltip" checked="${executorThreadInfoService.getStatusOfRunPriorityOnly()}"  data-tooltip="Prioritized Report Only" disabled="disabled"/>
                                                <label for="runPriorityOnly" style="padding-left: 0px;">Prioritized Only</label>
                                            </div>
                                        </g:else>
                                    </td>
                                </g:if>
                                <g:else>
                                    <td></td>
                                </g:else>
                            </td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="row pull-right">
                <div id="clock" align="center" data-time="${now.time}">
                    <h3>Current Time: ${now}</h3>
                </div>
            </div>
        </div>
        <g:unless test="${grailsApplication.config.quartz.monitor.showCountdown == false}">
            <asset:javascript src="jquery.countdown.js" />
            <asset:javascript src="jquery.color.js" />
        </g:unless>
        <g:unless test="${grailsApplication.config.quartz.monitor.showTickingClock == false}">
            <asset:javascript src="jquery.clock.js" />
        </g:unless>
        <asset:javascript src="quartz-monitor.js" />
        <g:javascript>
            $(document).on('click', '#runPriorityOnly', function (evt) {
                var addPriorityAttr = $(this).parent().parent().parent().find(".addPriorityAttr");
                var removePriorityAttr = addPriorityAttr.attr("href").split("&isRunPriorityOnlyChecked")[0];
                addPriorityAttr.attr("href", removePriorityAttr + "&isRunPriorityOnlyChecked=" + $(this).is(":checked"));
            });
        </g:javascript>
    </body>
</html>
