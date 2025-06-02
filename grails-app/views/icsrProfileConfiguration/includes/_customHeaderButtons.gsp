<div class="pull-right p-t-l-1">
<!---------dropdown code started----------->
    <a href="#" data-target="#" class="dropdown-toggle header-icon p-r-5" data-toggle="dropdown" aria-expanded="true" style="color:#1e1e1e">
        <img src="/reports/assets/icons/format-list-group.svg" title="${message(code: "app.label.icsr.bulk.actions")}"/>
    </a>
        <ul class="dropdown-menu m-r-30 topNavMenu-">
            <sec:ifAnyGranted roles="ROLE_ICSR_DISTRIBUTION">
                <li><a class="bulkTransmitButton checkPreviousVersionForAllCases theme-color custom-cursor" data-toggle="modal" disabled href="javascript:void(0)"><g:message code="app.label.icsr.bulk.transmit.all"/></a></li>
                <li><a class="bulkSubmitButton bulkSubmitConfirmation theme-color custom-cursor" data-toggle="modal"  disabled href="javascript:void(0)"><g:message code="app.label.icsr.bulk.submit.all"/></a></li>
                <li><a class="bulkDownloadButton theme-color custom-cursor" data-toggle="modal" data-toggle="modal" data-target="#bulkDownloadConfirmationModal" disabled href="javascript:void(0)"><g:message code="app.label.icsr.bulk.download.all"/></a></li>
                <li><a class="bulkRegenerateButton theme-color custom-cursor" data-toggle="modal" data-toggle="modal" data-target="#bulkRegenerateCaseModal" disabled href="javascript:void(0)"><g:message code="app.label.icsr.bulk.regenerate.all"/></a></li>
            </sec:ifAnyGranted>
            <sec:ifNotGranted roles="ROLE_ICSR_DISTRIBUTION">
                <li><a class="bulkDownloadButton theme-color custom-cursor" data-toggle="modal" data-toggle="modal" data-target="#bulkDownloadConfirmationModal" disabled href="javascript:void(0)"><g:message code="app.label.icsr.bulk.download.all"/></a></li>
            </sec:ifNotGranted>
        </ul>

    %{--<li><a class="click table-add addTaskTable" ><g:message code="app.label.task.template.addTasks" default="Add"/></a></li>--}%
<!---------dropdown code closed----------->
<g:link controller="icsr" action="executionStatus">
    <a href="/reports/icsr/executionStatus" style="vertical-align: top;margin-right: -6px;" class="header-icon">
        <img src="/reports/assets/icons/table-clock.svg" title="${message(code: "app.label.icsr.execution.status")}"/>

    </a>
  </g:link>
  <sec:ifAnyGranted roles="ROLE_ICSR_DISTRIBUTION">&nbsp;
      <a data-toggle="modal" class="header-icon" data-target="#addToScheduleManual" href="javascript:void(0)" style="vertical-align: top"><span title="${message(code: "app.label.icsr.manual.schedule")}" class="md md-plus md-lg icon-color"></span></a>
  </sec:ifAnyGranted>
    <g:render template="/includes/widgets/bulkDownloadConfirmation"/>
</div>
