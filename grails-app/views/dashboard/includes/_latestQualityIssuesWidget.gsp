<%@ page import="com.rxlogix.enums.PvqTypeEnum" %>
<div class="grid-stack-item-content rx-widget panel">
    <div class="panel-heading pv-sec-heading rx-widget-header">
        <a href="javascript:void(0)" class="rxmain-container-header-label rx-widget-title"
           name="latestQualityIssuesTitle${index}"
           data-url="${createLink(controller: 'quality', action: 'redirectFromWidget')}">
            ${message(code: 'app.widget.button.quality.latestIssues.label')}
        </a>
        <g:render template="includes/widgetButtons" model="[index: index, widget: widget]"/>
    </div>

    <div class="row rx-widget-content nicescroll">
        <div class="latestQualityIssues${index}">
            <span class="latestQualityIssuesWidgetTitle${index}"><span
                    class="lqiTitleContent">${message(code: "app.widget.reportRequest.no.title")}</span><span
                    class="fa fa-edit lqiTitleIcon"></span></span>
            <span class="latestQualityIssuesWidgetSearchForm${index}" style="display: none">
                <div class="alert alert-danger alert-dismissible forceLineWrap errorDiv" role="alert" hidden="hidden">
                    <button type="button" class="close" name="latestQualityIssuesCloseButton${index}"
                    <span aria-hidden="true">&times;</span>
                    <span class="sr-only"><g:message code="default.button.close.label"/></span>
                </button>
                    <p class="errorContent"></p>
                </div>

                <div class="alert alert-success alert-dismissible forceLineWrap successDiv" role="alert"
                     hidden="hidden">
                    <button type="button" class="close" data-dismiss="alert">
                        <span aria-hidden="true">&times;</span>
                        <span class="sr-only"><g:message code="default.button.close.label"/></span>
                    </button>

                    <p><g:message code="app.label.saved"/></p>
                </div>

                <div><input class="form-control" width="100%" maxlength="255" name="lqiTitle"
                            placeholder="${message(code: "placeholder.templateQuery.title")}"></div>

                <div class="row">
                    <div class="col-sm-12">
                        <b><g:message code="qualityModule.manualAdd.errorType.label"/>:</b>
                        <span class="checkbox checkbox-primary latestQualityIssuesWidget">
                            <input type="checkbox" id="quality_case${index}" name="quality_case"/>
                            <label for="quality_case${index}" id="quality_case_lbl${index}"><g:message
                                    code="app.label.case.data.quality"/></label>
                        </span>
                        <span class="checkbox checkbox-primary latestQualityIssuesWidget">
                            <input type="checkbox" id="quality_submission${index}" name="quality_submission"/>
                            <label for="quality_submission${index}" id="quality_submission_lbl${index}"><g:message
                                    code="app.label.submission.quality"/></label>
                        </span>
                    </div>
                </div>
                <button class="btn btn-primary saveLatestQualityIssuesWidget${index}">
                    ${message(code: "default.button.save.label")}
                </button>
                <button class="btn btn-primary latestQualityIssuesWidgetHideButton${index}">
                    ${message(code: "app.label.hideOptions")}
                </button>
            </span>
        </div>

        <div class="pv-caselist">
            <table id="rxTablelatestQualityIssues${index}"
                   class="table table-striped pv-list-table dataTable no-footer">
                <thead>
                <tr>
                    <th><g:message code="app.reportField.masterCaseNum"/></th>
                    <th><g:message code="app.reportField.masterVersionNum"/></th>
                    <th><g:message code="app.reportField.masterCaseReceiptDate"/></th>
                    <th><g:message code="app.reportField.masterRptTypeId"/></th>
                    <th><g:message code="app.reportField.masterCountryId"/></th>
                    <th><g:message code="app.reportField.masterPrimProdName"/></th>
                    <th><g:message code="app.label.errorType"/></th>
                </tr>
                </thead>
            </table>
        </div>
    </div>
</div>
<input type="hidden" id="widgetSettings${index}" value="${widget.reportWidget.settings}"/>
<input type="hidden" id="dataType${index}" value="ALL">
<script>
    $(function () {

        $(function () {
            $('[name="latestQualityIssuesCloseButton${index}"]').on('click', function () {
                var $container = $(".latestQualityIssuesWidgetSearchForm${index}");
                $container.find(".errorContent").html('');
                $container.find(".errorDiv").hide();
            })
        });

        $(function () {
            $('[name="latestQualityIssuesTitle${index}"]').on('click', function () {
                var redirectUrl = $(this).attr("data-url");
                $(this).attr('href', redirectUrl + '?dataType=' + $("#dataType${index}").val());
            });
        })

        var qualityIssuesTable;
        var loadData = function () {
            setCheckboxes();
            checkByDefault('quality');
            qualityIssuesTable = initTable();
            $(".latestQualityIssuesWidget input").on('click', function () {
                var settings = getCurrentSettings();
                $("#widgetSettings${index}").val(JSON.stringify(settings));
                getDataType();
                qualityIssuesTable.draw();
            });
        };

        function checkByDefault(type) {
            var $container = $(".latestQualityIssuesWidgetSearchForm${index}");
            $container.find('input[name=' + type + "_case]").prop('checked', true);
            $container.find('input[name=' + type + "_submission]").prop('checked', true);
        }

        function setCheckboxes() {
            var settingsString = $("#widgetSettings${index}").val();
            if (settingsString) {
                var settings = JSON.parse(settingsString);
                var $container = $(".latestQualityIssuesWidgetSearchForm${index}");
                if (settings.title) {
                    $container.find('input[name=lqiTitle]').val(encodeToHTML(settings.title));
                    $('.latestQualityIssuesWidgetTitle${index} .lqiTitleContent').html(encodeToHTML(settings.title));
                }
                for (var i in settings) {
                    for (var j in settings[i]) {
                        $container.find('input[name=' + i + '_' + settings[i][j] + "]").prop('checked', true);
                    }
                }
            } else {
                checkByDefault('quality');
            }
        }

        function initTable() {
            var rrtable = $("#rxTablelatestQualityIssues${index}").DataTable({
                "layout": {
                    topStart: null,
                    topEnd: {search: {placeholder: 'Search'}},
                    bottomStart: ['pageLength', 'info', {
                        paging: {
                            type: 'full_numbers'
                        }
                    }],
                    bottomEnd: null,
                },
                language: { search: ''},
                "bAutoWidth": false,
                "searching": false,
                "processing": true,
                "serverSide": true,
                "ajax": {
                    "url": latestQualityIssuesUrl,
                    "dataSrc": function (res) {
                        totalFilteredRecord = res["recordsFiltered"]
                        return res["aaData"];
                    },
                    "data": function (d) {
                        d.searchString = d.search.value;
                        d.dataType = getDataType();
                        d.wFilter = $("#widgetSettings${index}").val();
                        if (d.order.length > 0) {
                            d.direction = d.order[0].dir;
                            //Column header mData value extracting
                            d.sort = d.columns[d.order[0].column].data;
                        }
                    }
                },
                fnInitComplete: function () {
                },
                "aaSorting": [0, 'asc'],
                "bLengthChange": true,
                "iDisplayLength": 10,
                "aLengthMenu": [[5, 10, 20, 50, 100], [5, 10, 20, 50, 100]],
                "pagination": true,
                "pagingType": "full_numbers",
                "aoColumns": [
                    {
                        "mData": "masterCaseNum",
                        "bSortable": false,
                        "mRender": function (data, type, row) {
                            return '<a href="${createLink(controller: 'report', action: 'exportSingleCIOMS')}?caseNumber=' + row['masterCaseNum'] + '&versionNumber=' + row['masterVersionNum'] + '" target="_blank">' + row['masterCaseNum'] + '</a>';
                        }
                    },
                    {
                        "mData": "masterVersionNum",
                        "bSortable": false
                    },
                    {
                        "mData": "masterCaseReceiptDate",
                        "bSortable": false,
                        "mRender": function (data, type, row) {
                            if (data) {
                                var d = new Date(data);
                                return moment(d).format(DEFAULT_DATE_DISPLAY_FORMAT);
                            } else {
                                return '';
                            }
                        }
                    },
                    {
                        "mData": "masterRptTypeId",
                        "bSortable": false
                    },
                    {
                        "mData": "masterCountryId",
                        "bSortable": false
                    },
                    {
                        "mData": "masterPrimProdName",
                        "bSortable": false
                    },
                    {
                        "mData": "errorType",
                        "bSortable": false
                    }
                ],

                drawCallback: function (settings) {
                    pageDictionary($('#rxTablelatestQualityIssues${index}_wrapper')[0], settings.aLengthMenu[0][0], settings.json.recordsFiltered);
                },
            });
            return rrtable;
        }

        $('#refresh-widget${index}').hide();
        loadData();

        $(".latestQualityIssuesWidgetTitle${index}").on('click', function () {
            $(".latestQualityIssuesWidgetSearchForm${index}").show();
            $(".latestQualityIssuesWidgetTitle${index}").hide();
            $(this).closest(".rx-widget-content").find(".dataTables_length").parent().show();
        });

        $(".latestQualityIssuesWidgetHideButton${index}").on('click', function () {
            $(".latestQualityIssuesWidgetSearchForm${index}").hide();
            $(".latestQualityIssuesWidgetTitle${index}").show();
        });

        $(".saveLatestQualityIssuesWidget${index}").on('click', function () {
            if (checkIfDataTypeSelected()) {
                return;
            }
            var $container = $(this).parent();
            var settings = getCurrentSettings();
            var settingsString = JSON.stringify(settings);
            $("#widgetSettings${index}").val(settingsString);
            getDataType();
            $.ajax({
                url: "${createLink(controller: 'dashboard', action: 'updateWidgetSettings')}",
                type: 'post',
                data: {id:${widget.reportWidget.id}, data: settingsString},
                dataType: 'html'
            })
                .done(function (data) {
                    $container.find(".successDiv").show();
                    setTimeout(function () {
                        $container.find(".successDiv").hide();
                        $(".latestQualityIssuesWidgetSearchForm${index}").hide();
                        $(".latestQualityIssuesWidgetTitle${index}").show();
                    }, 1000);
                    $('.latestQualityIssuesWidgetTitle${index} .lqiTitleContent').html(encodeToHTML(settings.title));
                })
                .fail(function (err) {
                        var mess = (err.responseJSON.message ? err.responseJSON.message : "") +
                            (err.responseJSON.stackTrace ? "\n" + err.responseJSON.stackTrace : "");
                        $container.find(".errorContent").html(mess);
                        $container.find(".errorDiv").show();
                    }
                );
            $('#refresh-widget${index}').hide();
        });

        function getCurrentSettings() {
            var settings = {};
            $(".latestQualityIssuesWidgetSearchForm${index}").find("input").each(function (i) {
                $this = $(this);
                if ($this.attr("name") === "lqiTitle") {
                    settings.title = $this.val();
                } else if ($this.is(':checked')) {
                    var checked = $this.attr("name").split("_");
                    if (checked[1] !== 'all') {
                        if (!settings[checked[0]]) settings[checked[0]] = [];
                        settings[checked[0]].push(checked[1])
                    }
                }
            });
            return settings;
        }

        function getDataType() {
            var qualityDataType = 'NONE';
            var caseData = document.getElementById('quality_case${index}').checked;
            var submissionData = document.getElementById('quality_submission${index}').checked;
            if (caseData && submissionData) {
                qualityDataType = 'ALL';
            } else {
                if (caseData) {
                    qualityDataType = '${PvqTypeEnum.CASE_QUALITY.name()}';
                } else if (submissionData) {
                    qualityDataType = '${PvqTypeEnum.SUBMISSION_QUALITY.name()}';
                }
            }
            $("#dataType${index}").val(qualityDataType);
            return qualityDataType;
        }

        function checkIfDataTypeSelected() {
            var $container = $(".latestQualityIssuesWidgetSearchForm${index}");
            $container.find(".errorContent").html('');
            $container.find(".errorDiv").hide();
            var caseData = document.getElementById('quality_case${index}').checked;
            var submissionData = document.getElementById('quality_submission${index}').checked;
            if (!caseData && !submissionData) {
                var mess = 'Please select atleast one Error Type';
                $container.find(".errorContent").html(mess);
                $container.find(".errorDiv").show();
                return true;
            }
            return false;
        }
    });
</script>
