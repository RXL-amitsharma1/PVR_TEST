<%@ page import="java.util.Date;com.rxlogix.util.ViewHelper;com.rxlogix.enums.TimeZoneEnum;com.rxlogix.user.UserGroup;com.rxlogix.user.User;" %>
<style>
.scheduler .white-button{
    border-radius: 5px;
    border: 1px #d2d2d2 solid;
    background: white;
    height: 24px;
}

.scheduler .radio-custom.checked:before {
    background:  #79bc25 !important;
    border-color: #79bc25 !important;
}

</style>
<div class="fuelux">
        <div class="form-horizontal scheduler m-t-5 m-l-10" role="form" id="myScheduler">
            <div class="form-group start-datetime">
                <label class="col-sm-2 control-label scheduler-label " for="myStartDate">
                    <g:if test="${mode != 'show'}">
                        <g:message code="scheduler.startDate"/><span class="required-indicator">*</span>
                    </g:if>
                    <g:else>
                        <g:message code="scheduler.startDate"/>
                    </g:else>
                </label>
                <div class="row col-sm-12">
                    <div class="col-sm-5 form-group">
                        <div class="datepicker start-date" id="myDatePicker">
                            <div class="input-group">
                                <input class="form-control" id="MyStartDate" type="text"/>

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
                                                <button type="button" class="btn datepicker-wheels-select"><g:message code="scheduler.select" /> <span
                                                        class="sr-only"><g:message code="scheduler.month.and.year" /></span></button>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="col-sm-4 form-group" id="timeSelect">
                        <label class="sr-only" for="MyStartTime"><g:message code="scheduler.startTime" /></label>

                        <div class="input-group combobox start-time pl-10">
                            <input id="myStartTime" type="text" class="form-control"/>

                            <div class="input-group-btn">
                                <button type="button" class="btn btn-primary dropdown-toggle inputbtn-height" data-toggle="dropdown">
                                    <span class="caret"></span>
                                    <span class="sr-only"><g:message code="scheduler.toggle.dropdown" /></span>
                                </button>

                                <ul class="dropdown-menu dropdown-menu-right" role="menu">
                                    <li><a id="time" href="#"></a></li>
                                </ul>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row form-group timezone-container">
                <label class="col-sm-2 control-label scheduler-label"><g:message code="scheduler.timezone" /></label>

                <div class="col-xs-12 col-sm-12 col-md-12">

                    <div data-resize="auto" class="btn-group selectlist timezone timezone-select-div">
                        <button type="button" data-toggle="dropdown" class="btn white-button dropdown-toggle timezone-button"
                                type="button">
                            <span class="selected-label" style="width: 67px;">Etc/GMT+12 (GMT-12:00)</span>
                            <span class="caret"></span>
                            <span class="sr-only"><g:message code="scheduler.toggle.dropdown" /></span>
                        </button>
                        <ul class="dropdown-menu" role="menu">
                            <g:each in="${TimeZoneEnum.values()}" var="timezone">
                                <li data-name="${timezone?.timezoneId}" data-offset="${timezone?.gmtOffset}"><a
                                        href="#"><g:message
                                            code="${ViewHelper.getMessage(timezone?.i18nKey, timezone?.getGmtOffset())}"/></a>
                                </li>
                            </g:each>
                        </ul>
                        <input type="text" id="timezoneSelect" aria-hidden="true" readonly="readonly"
                               name="TimeZoneSelectlist" class="hidden hidden-field"/>
                    </div>
                </div>
            </div>

            <div class="row form-group repeat-container m-t-5">
                <label class="col-sm-2 control-label scheduler-label"><g:message code="scheduler.repeat" /></label>

                <div class="col-sm-12">

                    <div class="form-group repeat-interval">
                        <div data-resize="auto" class="btn-group selectlist pull-left repeat-options">
                            <button type="button" data-toggle="dropdown" class="btn white-button dropdown-toggle">
                                <span class="selected-label"><g:message code="scheduler.none.run.once" /></span>
                                <span class="caret"></span>
                            </button>
                            <ul class="dropdown-menu" role="menu">
                                <li data-value="none"><a href="#"><g:message code="scheduler.none.run.once" /></a></li>
                                <li data-value="hourly" data-text="${message(code: "scheduler.hours")}"><a href="#"><g:message code="scheduler.hourly" /></a></li>
                                <li data-value="daily" data-text="${message(code: "scheduler.days")}"><a href="#"><g:message code="scheduer.daily" /></a></li>
                                <li data-value="weekdays"><a href="#"><g:message code="scheduler.weekdays" /></a></li>
                                <li data-value="weekly" data-text="${message(code: "scheduler.weeks")}"><a href="#"><g:message code="scheduler.weekly" /></a></li>
                                <li data-value="monthly" data-text="${message(code: "scheduler.months")}"><a href="#"><g:message code="scheduler.monthly" /></a></li>
                                <li data-value="yearly" data-text="${message(code: "scheduler.years")}"><a href="#"><g:message code="scheduler.yearly" /></a></li>
                                <g:if test="${adhoc || isEtlScheduler}">
                                <li data-value="minutely" data-text="${message(code: "scheduler.minutely",default:"Minutes")}"><a href="#"><g:message code="scheduler.minutely" default="Minutes" /></a></li>
                                </g:if>
                            </ul>
                            <input type="text" aria-hidden="true" readonly="readonly" name="intervalSelectlist"
                                   class="hidden hidden-field">
                        </div>

                        <div class="repeat-panel repeat-every-panel repeat-hourly repeat-daily repeat-weekly repeat-monthly repeat-yearly repeat-minutely hide"
                             aria-hidden="true">
                            <label id="MySchedulerEveryLabel"
                                   class="inline-form-text repeat-every-pretext"><g:message code="scheduler.every" /></label>

                            <div class="spinbox digits-3 repeat-every">
                                <input type="text" class="form-control input-mini spinbox-input"
                                       aria-labelledby="MySchedulerEveryLabel">

                                <div class="spinbox-buttons btn-group btn-group-vertical">
                                    <button type="button" class="btn btn-default spinbox-up btn-xs btn-counter">
                                        <span class="glyphicon glyphicon-chevron-up"></span><span
                                            class="sr-only"><g:message code="scheduler.increase" /></span>
                                    </button>
                                    <button type="button" class="btn btn-default spinbox-down btn-xs btn-counter">
                                        <span class="glyphicon glyphicon-chevron-down"></span><span
                                            class="sr-only"><g:message code="scheduler.decrease" /></span>
                                    </button>
                                </div>
                            </div>

                            <div class="inline-form-text repeat-every-text"></div>
                        </div>
                    </div>

                    <div class="form-group repeat-panel repeat-weekly repeat-days-of-the-week hide" aria-hidden="true">
                        <fieldset class="btn-group " data-toggle="buttons">
                            <label id="repeat-weekly-sun" class="btn btn-primary">
                                <input type="checkbox" data-value="SU"><g:message code="scheduler.short.upc.Sunday" />
                            </label>
                            <label id="repeat-weekly-mon" class="btn btn-primary">
                                <input type="checkbox" data-value="MO"><g:message code="scheduler.short.upc.Monday" />
                            </label>
                            <label id="repeat-weekly-tue" class="btn btn-primary">
                                <input type="checkbox" data-value="TU"><g:message code="scheduler.short.upc.Tuesday" />
                            </label>
                            <label id="repeat-weekly-wed" class="btn btn-primary">
                                <input type="checkbox" data-value="WE"> <g:message code="scheduler.short.utc.Wednesday" />
                            </label>
                            <label id="repeat-weekly-thu" class="btn btn-primary">
                                <input type="checkbox" data-value="TH"> <g:message code="scheduler.short.upc.Thursday" />
                            </label>
                            <label id="repeat-weekly-fri" class="btn btn-primary">
                                <input type="checkbox" data-value="FR"> <g:message code="scheduler.short.upc.Friday" />
                            </label>
                            <label id="repeat-weekly-sat" class="btn btn-primary">
                                <input type="checkbox" data-value="SA"> <g:message code="scheduler.short.upc.Saturday" />
                            </label>
                        </fieldset>
                    </div>


                    <div class="repeat-panel repeat-monthly hide" aria-hidden="true">
                        <div class="form-group repeat-monthly-date">
                            <div class="radio pull-left">
                                <label for="repeat-on-day" class="radio-custom">
                                    <input id="repeat-on-day" type="radio" checked="checked" name="repeat-monthly"
                                           value="bymonthday">
                                    <span class="radio-label"><g:message code="scheduler.on.day"/></span>
                                </label>
                            </div>

                            <div data-resize="auto" class="btn-group selectlist pull-left">
                                <button type="button" data-toggle="dropdown" class="btn white-button dropdown-toggle">
                                    <span class="selected-label">1</span>
                                    <span class="caret"></span>
                                </button>
                                <ul class="dropdown-menu" role="menu" style="height:200px; overflow:auto;">
                                    <li data-value="1"><a href="#">1</a></li>
                                    <li data-value="2"><a href="#">2</a></li>
                                    <li data-value="3"><a href="#">3</a></li>
                                    <li data-value="4"><a href="#">4</a></li>
                                    <li data-value="5"><a href="#">5</a></li>
                                    <li data-value="6"><a href="#">6</a></li>
                                    <li data-value="7"><a href="#">7</a></li>
                                    <li data-value="8"><a href="#">8</a></li>
                                    <li data-value="9"><a href="#">9</a></li>
                                    <li data-value="10"><a href="#">10</a></li>
                                    <li data-value="11"><a href="#">11</a></li>
                                    <li data-value="12"><a href="#">12</a></li>
                                    <li data-value="13"><a href="#">13</a></li>
                                    <li data-value="14"><a href="#">14</a></li>
                                    <li data-value="15"><a href="#">15</a></li>
                                    <li data-value="16"><a href="#">16</a></li>
                                    <li data-value="17"><a href="#">17</a></li>
                                    <li data-value="18"><a href="#">18</a></li>
                                    <li data-value="19"><a href="#">19</a></li>
                                    <li data-value="20"><a href="#">20</a></li>
                                    <li data-value="21"><a href="#">21</a></li>
                                    <li data-value="22"><a href="#">22</a></li>
                                    <li data-value="23"><a href="#">23</a></li>
                                    <li data-value="24"><a href="#">24</a></li>
                                    <li data-value="25"><a href="#">25</a></li>
                                    <li data-value="26"><a href="#">26</a></li>
                                    <li data-value="27"><a href="#">27</a></li>
                                    <li data-value="28"><a href="#">28</a></li>
                                    <li data-value="29"><a href="#">29</a></li>
                                    <li data-value="30"><a href="#">30</a></li>
                                    <li data-value="31"><a href="#">31</a></li>
                                </ul>
                                <input type="text" aria-hidden="true" readonly="readonly" name="monthlySelectlist"
                                       class="hidden hidden-field">
                            </div>
                        </div>

                        <div class="repeat-monthly-day form-group">
                            <div class="radio pull-left">
                                <label for="repeat-on-the" class="radio-custom">
                                    <input id="repeat-on-the" type="radio" checked="checked" name="repeat-monthly"
                                           value="bysetpos">
                                    <span class="radio-label"><g:message code="scheduler.on.the"/></span>
                                </label>
                            </div>

                            <div data-resize="auto" class="btn-group selectlist month-day-pos pull-left">
                                <button type="button" data-toggle="dropdown" class="btn white-button dropdown-toggle">
                                    <span class="selected-label"><g:message code="scheduler.first" /></span>
                                    <span class="caret"></span>
                                </button>
                                <ul class="dropdown-menu" role="menu">
                                    <li data-value="1"><a href="#"><g:message code="scheduler.first" /></a></li>
                                    <li data-value="2"><a href="#"><g:message code="scheduler.second" /></a></li>
                                    <li data-value="3"><a href="#"><g:message code="scheduler.third" /></a></li>
                                    <li data-value="4"><a href="#"><g:message code="scheduler.fourth" /></a></li>
                                    <li data-value="-1"><a href="#"><g:message code="scheduler.last" /></a></li>
                                </ul>
                                <input type="text" aria-hidden="true" readonly="readonly" name="monthlySelectlist"
                                       class="hidden hidden-field">
                            </div>

                            <div data-resize="auto" class="btn-group selectlist month-days pull-left">
                                <button type="button" data-toggle="dropdown" class="btn white-button dropdown-toggle">
                                    <span class="selected-label"><g:message code="scheduler.sunday" /></span>
                                    <span class="caret"></span>
                                </button>
                                <ul class="dropdown-menu" role="menu">
                                    <li data-value="SU"><a href="#"><g:message code="scheduler.sunday" /></a></li>
                                    <li data-value="MO"><a href="#"><g:message code="scheduler.monday" /></a></li>
                                    <li data-value="TU"><a href="#"><g:message code="scheduler.tuesday" /></a></li>
                                    <li data-value="WE"><a href="#"><g:message code="scheduler.wednesday" /></a></li>
                                    <li data-value="TH"><a href="#"><g:message code="scheduler.thursday" /></a></li>
                                    <li data-value="FR"><a href="#"><g:message code="scheduler.friday" /></a></li>
                                    <li data-value="SA"><a href="#"><g:message code="scheduler.saturday" /></a></li>
                                    <li data-value="SU,MO,TU,WE,TH,FR,SA"><a href="#"><g:message code="scheduler.day" /></a></li>
                                    <li data-value="MO,TU,WE,TH,FR"><a href="#"><g:message code="scheduler.weekday" /></a></li>
                                    <li data-value="SU,SA"><a href="#"><g:message code="scheduler.weekend.day" /></a></li>
                                </ul>
                                <input type="text" aria-hidden="true" readonly="readonly" name="monthlySelectlist"
                                       class="hidden hidden-field">
                            </div>

                        </div>
                    </div>

                    <div class="repeat-panel repeat-yearly hide" aria-hidden="true">

                        <div class="form-group repeat-yearly-date">

                            <div class="radio pull-left">
                                <label class="radio-custom">
                                    <input class="sr-only" type="radio" checked="checked" name="repeat-yearly"
                                           value="bymonthday">
                                    <span class="radio-label"><g:message code="scheduler.on" /></span>
                                </label>
                            </div>

                            <div data-resize="auto" class="btn-group selectlist year-month pull-left">
                                <button type="button" data-toggle="dropdown" class="btn white-button dropdown-toggle">
                                    <span class="selected-label"><g:message code="scheduler.january" /></span>
                                    <span class="caret"></span>
                                </button>
                                <ul class="dropdown-menu yearly-year-month-select" role="menu">
                                    <li data-value="1"><a href="#"><g:message code="scheduler.january" /></a></li>
                                    <li data-value="2"><a href="#"><g:message code="scheduler.february" /></a></li>
                                    <li data-value="3"><a href="#"><g:message code="scheduler.march" /></a></li>
                                    <li data-value="4"><a href="#"><g:message code="scheduler.april" /></a></li>
                                    <li data-value="5"><a href="#"><g:message code="scheduler.may" /></a></li>
                                    <li data-value="6"><a href="#"><g:message code="scheduler.june" /></a></li>
                                    <li data-value="7"><a href="#"><g:message code="scheduler.july" /></a></li>
                                    <li data-value="8"><a href="#"><g:message code="scheduler.august" /></a></li>
                                    <li data-value="9"><a href="#"><g:message code="scheduler.september" /></a></li>
                                    <li data-value="10"><a href="#"><g:message code="scheduler.october" /></a></li>
                                    <li data-value="11"><a href="#"><g:message code="scheduler.november" /></a></li>
                                    <li data-value="12"><a href="#"><g:message code="scheduler.december" /></a></li>
                                </ul>
                                <input type="text" aria-hidden="true" readonly="readonly" name="monthlySelectlist"
                                       class="hidden hidden-field">
                            </div>

                            <div data-resize="auto" class="btn-group selectlist year-month-day pull-left">
                                <button type="button" data-toggle="dropdown" class="btn white-button dropdown-toggle">
                                    <span class="selected-label">1</span>
                                    <span class="caret"></span>
                                </button>
                                <ul class="dropdown-menu" role="menu" style="height:200px; overflow:auto;">
                                    <li data-value="1"><a href="#">1</a></li>
                                    <li data-value="2"><a href="#">2</a></li>
                                    <li data-value="3"><a href="#">3</a></li>
                                    <li data-value="4"><a href="#">4</a></li>
                                    <li data-value="5"><a href="#">5</a></li>
                                    <li data-value="6"><a href="#">6</a></li>
                                    <li data-value="7"><a href="#">7</a></li>
                                    <li data-value="8"><a href="#">8</a></li>
                                    <li data-value="9"><a href="#">9</a></li>
                                    <li data-value="10"><a href="#">10</a></li>
                                    <li data-value="11"><a href="#">11</a></li>
                                    <li data-value="12"><a href="#">12</a></li>
                                    <li data-value="13"><a href="#">13</a></li>
                                    <li data-value="14"><a href="#">14</a></li>
                                    <li data-value="15"><a href="#">15</a></li>
                                    <li data-value="16"><a href="#">16</a></li>
                                    <li data-value="17"><a href="#">17</a></li>
                                    <li data-value="18"><a href="#">18</a></li>
                                    <li data-value="19"><a href="#">19</a></li>
                                    <li data-value="20"><a href="#">20</a></li>
                                    <li data-value="21"><a href="#">21</a></li>
                                    <li data-value="22"><a href="#">22</a></li>
                                    <li data-value="23"><a href="#">23</a></li>
                                    <li data-value="24"><a href="#">24</a></li>
                                    <li data-value="25"><a href="#">25</a></li>
                                    <li data-value="26"><a href="#">26</a></li>
                                    <li data-value="27"><a href="#">27</a></li>
                                    <li data-value="28"><a href="#">28</a></li>
                                    <li data-value="29" class="day30"><a href="#">29</a></li>
                                    <li data-value="30" class="day30"><a href="#">30</a></li>
                                    <li data-value="31" class="day31"><a href="#">31</a></li>
                                </ul>
                                <input type="text" aria-hidden="true" readonly="readonly" name="monthlySelectlist"
                                       class="hidden hidden-field">
                            </div>
                        </div>

                        <div class="form-group repeat-yearly-day">

                            <div class="radio pull-left"><label class="radio-custom"><input class="sr-only" type="radio"
                                                                                            name="repeat-yearly"
                                                                                            value="bysetpos"> <g:message code="scheduler.on.the"/>
                            </label></div>

                            <div data-resize="auto" class="btn-group selectlist year-month-day-pos pull-left">
                                <button type="button" data-toggle="dropdown" class="btn white-button dropdown-toggle">
                                    <span class="selected-label"><g:message code="scheduler.first" /></span>
                                    <span class="caret"></span>
                                    <span class="sr-only"><g:message code="scheduler.first" /></span>
                                </button>
                                <ul class="dropdown-menu" role="menu">
                                    <li data-value="1"><a href="#"><g:message code="scheduler.first" /></a></li>
                                    <li data-value="2"><a href="#"><g:message code="scheduler.second" /></a></li>
                                    <li data-value="3"><a href="#"><g:message code="scheduler.third" /></a></li>
                                    <li data-value="4"><a href="#"><g:message code="scheduler.fourth" /></a></li>
                                    <li data-value="-1"><a href="#"><g:message code="scheduler.last" /></a></li>
                                </ul>
                                <input type="text" aria-hidden="true" readonly="readonly" name="yearlyDateSelectlist"
                                       class="hidden hidden-field">
                            </div>

                            <div data-resize="auto" class="btn-group selectlist year-month-days pull-left">
                                <button type="button" data-toggle="dropdown" class="btn white-button dropdown-toggle">
                                    <span class="selected-label"><g:message code="scheduler.sunday" /></span>
                                    <span class="caret"></span>
                                    <span class="sr-only"><g:message code="scheduler.sunday" /></span>
                                </button>
                                <ul class="dropdown-menu" role="menu" style="height:200px; overflow:auto;">
                                    <li data-value="SU"><a href="#"><g:message code="scheduler.sunday" /></a></li>
                                    <li data-value="MO"><a href="#"><g:message code="scheduler.monday" /></a></li>
                                    <li data-value="TU"><a href="#"><g:message code="scheduler.tuesday" /></a></li>
                                    <li data-value="WE"><a href="#"><g:message code="scheduler.wednesday" /></a></li>
                                    <li data-value="TH"><a href="#"><g:message code="scheduler.thursday" /></a></li>
                                    <li data-value="FR"><a href="#"><g:message code="scheduler.friday" /></a></li>
                                    <li data-value="SA"><a href="#"><g:message code="scheduler.saturday" /></a></li>
                                    <li data-value="SU,MO,TU,WE,TH,FR,SA"><a href="#"><g:message code="scheduler.day" /></a></li>
                                    <li data-value="MO,TU,WE,TH,FR"><a href="#"><g:message code="scheduler.weekday" /></a></li>
                                    <li data-value="SU,SA"><a href="#"><g:message code="scheduler.weekend.day" /></a></li>
                                </ul>
                                <input type="text" aria-hidden="true" readonly="readonly" name="yearlyDaySelectlist"
                                       class="hidden hidden-field">
                            </div>

                            <div class="inline-form-text repeat-yearly-day-text"><g:message code="scheduler.of" /></div>

                            <div data-resize="auto" class="btn-group selectlist year-month pull-left">
                                <button type="button" data-toggle="dropdown" class="btn white-button dropdown-toggle">
                                    <span class="selected-label"><g:message code="scheduler.january" /></span>
                                    <span class="caret"></span>
                                    <span class="sr-only"><g:message code="scheduler.january" /></span>
                                </button>
                                <ul class="dropdown-menu" role="menu" style="height:200px; overflow:auto;">
                                    <li data-value="1"><a href="#"><g:message code="scheduler.january" /></a></li>
                                    <li data-value="2"><a href="#"><g:message code="scheduler.february" /></a></li>
                                    <li data-value="3"><a href="#"><g:message code="scheduler.march" /></a></li>
                                    <li data-value="4"><a href="#"><g:message code="scheduler.april" /></a></li>
                                    <li data-value="5"><a href="#"><g:message code="scheduler.may" /></a></li>
                                    <li data-value="6"><a href="#"><g:message code="scheduler.june" /></a></li>
                                    <li data-value="7"><a href="#"><g:message code="scheduler.july" /></a></li>
                                    <li data-value="8"><a href="#"><g:message code="scheduler.august" /></a></li>
                                    <li data-value="9"><a href="#"><g:message code="scheduler.september" /></a></li>
                                    <li data-value="10"><a href="#"><g:message code="scheduler.october" /></a></li>
                                    <li data-value="11"><a href="#"><g:message code="scheduler.november" /></a></li>
                                    <li data-value="12"><a href="#"><g:message code="scheduler.december" /></a></li>
                                </ul>
                                <input type="text" aria-hidden="true" readonly="readonly" name="yearlyDaySelectlist"
                                       class="hidden hidden-field">
                            </div>
                        </div>
                    </div>

                </div>
            </div>

            <div class="row form-group repeat-end hide mt-10" aria-hidden="true">
                <label class="col-sm-2 control-label scheduler-label"><g:message code="app.label.end" /></label>

                <div class="col-sm-10 row">
                    <div class="col-sm-4 form-group">
                        <div data-resize="auto" class="btn-group selectlist end-options pull-left">
                            <button type="button" data-toggle="dropdown" class="btn white-button dropdown-toggle">
                                <span class="selected-label"><g:message code="scheduler.never" /></span>
                                <span class="caret"></span>
                                <span class="sr-only"><g:message code="scheduler.never" /></span>
                            </button>
                            <ul class="dropdown-menu" role="menu">
                                <li data-value="never"><a href="#"><g:message code="scheduler.never" /></a></li>
                                <li data-value="after"><a href="#"><g:message code="scheduler.after" /></a></li>
                                <li data-value="date"><a href="#"><g:message code="scheduler.on.date" /></a></li>
                            </ul>
                            <input type="text" aria-hidden="true" readonly="readonly" name="EndSelectlist"
                                   class="hidden hidden-field">
                        </div>
                    </div>

                    <div class="col-sm-6 form-group end-option-panel end-after-panel pull-left hide" aria-hidden="true">
                        <div class="spinbox digits-3 end-after">
                            <label id="MyEndAfter" class="sr-only"><g:message code="scheduler.end.after" /></label>
                            <input type="text" class="form-control input-mini spinbox-input"
                                   aria-labelledby="MyEndAfter">

                            <div class="spinbox-buttons btn-group btn-group-vertical">
                                <button type="button" class="btn btn-default spinbox-up btn-xs btn-counter">
                                    <span class="glyphicon glyphicon-chevron-up"></span><span
                                        class="sr-only"><g:message code="scheduler.increase" /></span>
                                </button>
                                <button type="button" class="btn btn-default spinbox-down btn-xs btn-counter">
                                    <span class="glyphicon glyphicon-chevron-down"></span><span
                                        class="sr-only"><g:message code="scheduler.decrease" /></span>
                                </button>
                            </div>
                        </div>

                        <div class="inline-form-text end-after-text"><g:message code="scheduler.occurrence" /></div>
                    </div>

                    <div class="col-sm-8 form-group end-option-panel end-on-date-panel pull-left hide"
                         aria-hidden="true">
                        <div class="datepicker input-group end-on-date">
                            <div class="input-group">
                                <input class="form-control" type="text"/>

                                <div class="input-group-btn">
                                    <button type="button" class="btn btn-default dropdown-toggle button-spacing"
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
                                                <button type="button" class="datepicker-today">Today</button>
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
                                                <button type="button" class="btn datepicker-wheels-select"><g:message code="scheduler.select" /> <span
                                                        class="sr-only"><g:message code="scheduler.month.and.year" /></span></button>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <g:if test="${isEtlScheduler}">
                <div class="row form-group repeat-container">
                    <label class="pull-left m-l-5"><g:message code="app.label.emailTo"/><span class="required-indicator">*</span></label>
                    <div class="col-md-9">
                        <g:select id="emailToUsers" name="emailToUsers"
                                  from="${[]}" autocomplete="off" data-evt-change='{"method": "validateEmailIsSelectedOrNot", "params": []}'
                                  data-value="${etlScheduleInstance?.emailToUsers}"
                                  class="form-control emailUsers" multiple="true" data-options-url="${createLink(controller: 'email', action: 'allEmails', params: [id: etlScheduleInstance?.id])}"/>
                    </div>
                    <div class="col-md-1">
                        <span class="showEmailConfiguration" style="cursor: pointer;" data-toggle="modal"
                              data-target="#emailConfiguration"><asset:image
                                src="icons/email.png" title="${message(code: 'default.button.addEmailConfiguration.label')}"/></span>
                    </div>

                </div>
                <div class="row form-group repeat-container mt-10 m-r-15">
                    <label class="col-sm-6 control-label scheduler-label"><g:message code="etl.schedule.email.incremental"/></label>
                    <div class="col-md-6">
                        <div class="col-md-4">
                            <div class="digits-3">
                                <input type="number" class="form-control" max="999" min="1" id="sendEmailETLInterval"
                                       name="sendEmailETLInterval"
                                       value="${etlScheduleInstance?.sendEmailETLInterval ? etlScheduleInstance?.sendEmailETLInterval : 1}">
                            </div>
                        </div>
                        <div class="col-md-8" style="padding-top: 5px; ">minutes</div>
                    </div>
                </div>
                <div class="row form-group repeat-container">
                        <div class="col-md-12">
                            <div class="checkbox checkbox-primary">
                                <g:checkBox id="sendSuccessEmail" name="sendSuccessEmail" class=""
                                            value="${etlScheduleInstance?.sendSuccessEmail}"
                                            checked="${etlScheduleInstance?.sendSuccessEmail}"/>
                                <label for="sendSuccessEmail"><g:message code="etl.schedule.send.success.email"/></label>
                            </div>
                        </div>
                </div>
            </g:if>
            <g:elseif test="${isAutoRODSchedular}">
                <div class="row form-group">
                    <label class="pull-left m-l-5"><g:message code="app.label.error.emailTo"/></label>
                <div class="col-xs-12 col-sm-12 col-md-12">
                        <g:set var="users" value="${User.findAllByEnabled(true).sort{it.username}}"/>
                        <select name="emailToUsers" id="emailToUsers" class="form-control select2-box timezone-select-div" multiple="true">
                            <g:each in="${users}" var="user">
                                <option value="${user.email}" ${autoReasonOfDelayInstance?.emailToUsers?.find{it ==user.email} ? 'selected="selected"':''}>${user.fullName} - ${user.email}</option>
                            </g:each>
                        </select>
                        <script>
                            $('.select2-box').select2();
                        </script>
                    </div>
                </div>
            </g:elseif>

        </div>
    </div>