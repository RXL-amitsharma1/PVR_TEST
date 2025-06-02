<script>
    var reportFieldsForQueryUrl = "${createLink(controller: 'queryRest', action: 'reportFieldsForQueryValue')}";
</script>
<div id="toAddContainerQEV" class="toAddContainerQEV ${!qbeForm?"row":"col-xs-4"} queryBlankContainer mt-5 parent-with-hidden-child">
    %{--Yes, the inline styling is bad, but the alignment has to be properly refactored since errorMessageOperator is used--}%
    %{--in multiple places and we need this bug fixed ASAP for a drop to ALSC.  Speed vs. Quality:  Episode 26--}%
    <div class="col-xs-3 col-md-offset-7 errorMessageOperator" style="padding-left: 15px">
        <g:message code="app.query.value.invalid.number" />
    </div>

    <div class="col-xs-4 expressionsNoPadFirst" style="position: relative;padding-left: 40px!important; ${qbeForm?"display: none":""}">
        <i data-toggle="popover" id="selectPopover" data-trigger="hover"  data-container="body" data-content="${message(code: ('app.query.field.no.description'))}" class="pv-ic templateQueryIcon templateViewButton glyphicon glyphicon-info-sign iPopover" style="cursor: pointer;padding-right: 10px;position: absolute;z-index: 1;top: 3px;left: 15px;"></i>
        <select name="selectField" readonly="true" class="form-control expressionField">
        </select>
    </div>

    <div class="col-xs-2 expressionsOperator" style="${qbeForm?"display: none":""}">
        <g:select name="selectOperator" from="${[]}"
                  class="form-control expressionOp selectOperator"
            readonly="true"
                  noSelection="['': 'Select Operator']"/>
    </div>

    <div class="col-xs-5 expressionsNoPad">
        <g:textField name="selectValue"
                     class="form-control expressionValueText" placeholder="Value"/>
    </div>

    <div class="col-xs-5 expressionsNoPad">
        <select name="selectSelect"
                  class="form-control expressionValueSelect"
                multiple="true"></select>
    </div>

    <div class="col-xs-5 expressionsNoPad">
        <select hidden="hidden" name="selectSelectAuto"
                class="form-control selectSelectAuto expressionValueSelectAuto"></select>
    </div>

    <div class="col-xs-5 expressionsNoPad">
        <select hidden="hidden" name="selectSelectNonCache"
                          class="form-control expressionValueSelectNonCache selectSelectNonCache"></select>
    </div>

    <div class="col-xs-5 fuelux expressionsNoPad">
        <div class="datepicker expressionValueDate">
            <div class="input-group">
                <input class="form-control expressionValueDateInput" name="selectDate"
                       type="text"/>

                <div class="input-group-btn">
                    <button type="button" class="btn btn-primary dropdown-toggle"
                            data-toggle="dropdown">
                        <span class="glyphicon glyphicon-calendar"></span>
                        <span class="sr-only"><g:message code="scheduler.toggleCalendar" /></span>
                    </button>

                    <div class="dropdown-menu dropdown-menu-right datepicker-calendar-wrapper rx-datepicker"
                         role="menu">
                        <div class="datepicker-calendar">
                            <div class="datepicker-calendar-header">
                                <button type="button" class="prev"><span
                                        class="glyphicon glyphicon-chevron-left"></span><span
                                        class="sr-only"><g:message code="scheduler.previousMonth" /></span></button>
                                <button type="button" class="next"><span
                                        class="glyphicon glyphicon-chevron-right"></span><span
                                        class="sr-only"><g:message code="scheduler.nextMonth" /></span></button>
                                <button type="button" class="title">
                                    <span class="month">
                                        <span data-month="0"><g:message code="scheduler.january" /></span>
                                        <span data-month="1"><g:message code="scheduler.february" /></span>
                                        <span data-month="2"><g:message code="scheduler.march" /></span>
                                        <span data-month="3"><g:message code="scheduler.april" /></span>
                                        <span data-month="4"><g:message code="scheduler.may" /></span>
                                        <span data-month="5"><g:message code="scheduler.june" /></span>
                                        <span data-month="6"><g:message code="scheduler.july" /></span>
                                        <span data-month="7"><g:message code="scheduler.august" /></span>
                                        <span data-month="8"><g:message code="scheduler.september" /></span>
                                        <span data-month="9"><g:message code="scheduler.october" /></span>
                                        <span data-month="10"><g:message code="scheduler.november" /></span>
                                        <span data-month="11"><g:message code="scheduler.december" /></span>
                                    </span> <span class="year"></span>
                                </button>
                            </div>
                            <table class="datepicker-calendar-days">
                                <thead>
                                <tr>
                                    <th><g:message code="scheduler.short.sunday" /></th>
                                    <th><g:message code="scheduler.short.monday" /></th>
                                    <th><g:message code="scheduler.short.tuesday" /></th>
                                    <th><g:message code="scheduler.short.wednesday" /></th>
                                    <th><g:message code="scheduler.short.thursday" /></th>
                                    <th><g:message code="scheduler.short.friday" /></th>
                                    <th><g:message code="scheduler.short.saturday" /></th>
                                </tr>
                                </thead>
                                <tbody></tbody>
                            </table>

                            <div class="datepicker-calendar-footer">
                                <button type="button" class="datepicker-today"><g:message code="scheduler.today" /></button>
                            </div>
                        </div>

                        <div class="datepicker-wheels" aria-hidden="true">
                            <div class="datepicker-wheels-month">
                                <h2 class="header"><g:message code="scheduler.month" /></h2>
                                <ul>
                                    <li data-month="0"><button type="button"><g:message code="scheduler.short.january" /></button></li>
                                    <li data-month="1"><button type="button"><g:message code="scheduler.short.february" /></button></li>
                                    <li data-month="2"><button type="button"><g:message code="scheduler.short.march" /></button></li>
                                    <li data-month="3"><button type="button"><g:message code="scheduler.short.April" /></button></li>
                                    <li data-month="4"><button type="button"><g:message code="scheduler.may" /></button></li>
                                    <li data-month="5"><button type="button"><g:message code="scheduler.short.june" /></button></li>
                                    <li data-month="6"><button type="button"><g:message code="scheduler.short.july" /></button></li>
                                    <li data-month="7"><button type="button"><g:message code="scheduler.short.august" /></button></li>
                                    <li data-month="8"><button type="button"><g:message code="schedulere.short.september" /></button></li>
                                    <li data-month="9"><button type="button"><g:message code="scheduler.short.octoberr" /></button></li>
                                    <li data-month="10"><button type="button"><g:message code="scheduler.short.november" /></button></li>
                                    <li data-month="11"><button type="button"><g:message code="scheduler.short.december" /></button></li>
                                </ul>
                            </div>

                            <div class="datepicker-wheels-year">
                                <h2 class="header"><g:message code="scheduler.year" /></h2>
                                <ul></ul>
                            </div>

                            <div class="datepicker-wheels-footer clearfix">
                                <button type="button" class="btn datepicker-wheels-back"><span
                                        class="glyphicon glyphicon-arrow-left"></span><span
                                        class="sr-only"><g:message code="scheduler.return.to.calendar" /></span></button>
                                <button type="button"
                                        class="btn datepicker-wheels-select"><g:message code="scheduler.select" /> <span
                                        class="sr-only"><g:message code="scheduler.month.and.year" /></span></button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="col-xs-1 expressionsNoPad">
        <div>
            <div class="col-md-5 hidden-child">
                <i class="fa fa-pencil-square-o copy-n-paste modal-link" data-toggle="modal" style="margin-left: -4px;margin-top:2px;padding: 0; color: hsla(120, 60%, 40%, 1.0) !important;" data-target="#copyAndPasteModal"></i>
            </div>
            <div class="col-md-6 hidden-child">
                <i class="fa fa-search" style="display: none;padding: 0;font-size: 19px;color: hsla(120, 60%, 40%, 1.0) !important;" data-toggle="modal" data-target="#eventModal" data-hide-dictionary-group="true"></i>
            </div>
        </div>
    </div>
    <g:render template="/query/copyPasteModal"/>

    <div style="display: none; width: 0" hidden="hidden">
        <input class="qevReportField" value="${qev?.reportField?.name}" />
        <input class="qevOperator" value="${qev?.operator}" />
        <input class="qevValue" value="${qev?.value}" />
        <input class="qevKey" value="${qev?.key}" />
        <input class="qevSpecialKeyValue" value="${qev?.specialKeyValue}" />
        <input class="isFromCopyPaste" value="${qev?.isFromCopyPaste}" />
    </div>
</div>