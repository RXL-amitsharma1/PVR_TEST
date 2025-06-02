<div class="row">

    %{--<div class="col-md-2 width-auto">
        <div class="fuelux ">
            <div class="col-xs-6" id="qualityFrom">
                <div class="row form-inline">
                    <div class="col-xs-5 datePickerMargin labelMargin ">
                        <label class="no-bold"><g:message code="app.dateFilter.from"/></label>
                    </div>

                    <div class="col-xs-7 datePickerMargin">
                        <div class="input-group">
                            <input placeholder="${message(code: 'select.start.date')}"
                                   class="form-control"
                                   id="dateFrom" type="text"/>
                            <g:render template="/includes/widgets/datePickerTemplate"/>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="col-md-2 width-auto">
        <div class="fuelux ">
            <div class="col-xs-6" id="qualityTo">
                <div class="row form-inline">
                    <div class="col-xs-3 datePickerMargin labelMargin ">
                        <label class="no-bold"><g:message code="app.dateFilter.to"/></label>
                    </div>

                    <div class="col-xs-9 datePickerMargin">
                        <div class="input-group">
                            <input placeholder="${message(code: "select.end.date")}"
                                   class="form-control"
                                   id="dateTo" type="text"/>
                            <g:render template="/includes/widgets/datePickerTemplate"/>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>--}%

    <div class="col-md-2 width-auto" style="padding-left: 15px !important">
        <sec:ifAnyGranted roles="ROLE_PVQ_EDIT">
        <div class="row form-inline"><a href="#" data-toggle="modal"
                                    data-target="#adHocAlertModal"
                                    style="text-decoration: none" class="btn btn-primary"
                                    data-evt-clk='{"method": "updateAdHocField", "params": []}'>
            <span class="glyphicon glyphicon-plus icon-white"></span> <g:message
                    code="qualityModule.ad.hoc.alert.button"/></a>
        </div>
        </sec:ifAnyGranted>
    </div>
</div>


<div class="contentBox contentBoxContainer row">
    <!--<div class="col-md-10"></div>
    <div class="col-md-2">
        <div class="fuelux ">
            <div class="col-xs-12" id="errTypeSelectDiv">
                <div class="row form-inline col-xs-12">
                    <div class="col-xs-5 input-group">
                        <select class="form-control select2-box col-xs-12" id="selectErrorType">
                        </select>
                    </div>
                </div>
            </div>
        </div>
    </div>-->
    <div id="qualityChart"></div>
</div>