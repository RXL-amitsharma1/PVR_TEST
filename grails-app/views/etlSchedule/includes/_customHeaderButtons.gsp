<%@ page import="com.rxlogix.enums.EtlStatusEnum;"  %>
<style>
.rxmain-dropdown-settings {
    font-size : 18px;
}
</style>
<sec:ifAnyGranted roles="ROLE_ADMIN">
    <div class="pull-right">
        <g:link controller="etlSchedule" action="edit"><i class='md md-pencil rxmain-dropdown-settings' title="<g:message code="default.edit.label" args="[entityName]"/>"></i></g:link>
        <g:if test="${!etlScheduleInstance?.isInitial}">
            <g:if test="${etlStatus == EtlStatusEnum.RUNNING || affEtlStatus == EtlStatusEnum.RUNNING}">
%{--                <a href="#" class="runOrPauseBtn rxmain-dropdown-settings" data-toggle="modal" data-target="#pauseEtlModal"><i class='md md-pause' title="<g:message code="pause.initial.etl"/>"></i></a>--}%
            </g:if>
            <g:else>
                <a href="#" class="runOrPauseBtn rxmain-dropdown-settings" data-toggle="modal" data-target="#initialEtlModal"><i class='md md-play' title="<g:message code="run.initial.etl"/>"></i></a>
            </g:else>
%{--            <g:if test="${etlStatus == EtlStatusEnum.FAILED || etlStatus == EtlStatusEnum.ETL_PAUSED || affEtlStatus == EtlStatusEnum.FAILED || affEtlStatus == EtlStatusEnum.ETL_PAUSED}">--}%
%{--                <a href="#" class="resumeBtn rxmain-dropdown-settings" data-toggle="modal" data-target="#resumeEtlModal"><i class="md md-skip-next-circle-outline" title="<g:message code="resume.etl"/>"></i></a> &nbsp;--}%
%{--            </g:if>--}%
        </g:if>
    </div>
</sec:ifAnyGranted>