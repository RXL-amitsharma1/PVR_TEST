var ganttChart;
$(function () {
    ganttChart = new JSGantt.GanttChart(document.getElementById('gantt'), 'day');
    if (ganttChart.getDivId() != null) {
        ganttChart.setOptions({
            vCaptionType: 'Complete',  // Set to Show Caption : None,Caption,Resource,Duration,Complete,
            vQuarterColWidth: 36,
            vDateTaskDisplayFormat: 'day dd month yyyy', // Shown in tool tip box
            vDayMajorDateDisplayFormat: 'mon yyyy - Week ww',// Set format to display dates in the "Major" header of the "Day" view
            vWeekMinorDateDisplayFormat: 'dd mon', // Set format to display dates in the "Minor" header of the "Week" view
            vShowTaskInfoLink: 1, // Show link in tool tip (0/1)
            vShowEndWeekDate: 0,  // Show/Hide the date for the last day of the week in header for daily view (1/0)
            vUseSingleCell: 10000, // Set the threshold at which we will only use one cell per table row (0 disables).  Helps with rendering performance for large charts.
            vFormatArr: ['Day', 'Week', 'Month', 'Quarter'], // Even with setUseSingleCell using Hour format on such a large chart can cause issues in some browsers
            // vScrollTo: new Date(),
            vEvents: {
                taskname: console.log,
                res: console.log,
                dur: console.log,
                comp: console.log,
                start: console.log,
                end: console.log,
                planstart: console.log,
                planend: console.log,
                cost: console.log,
                beforeDraw: function(){console.log('before draw listener')},
                afterDraw: function(){ console.log('before after listener')}
            },
            vEventClickRow: clickRow,
        });


        function clickRow(data) {

            var task = data.getAllData();
            if (changeDependence) {
                if (task.pDataObjec.entityid) {
                    location.href = changeDependenceUrl + "?" + changeDependence + "&dependOn=" + task.pDataObjec.entityid + "&dependOnType=" + task.pDataObjec.category + "&backUrl=" + encodeURIComponent(backUrl + "&selectedTab=" + $(".nav .active a").attr("id"));
                }
                changeDependence = false;
                $(".glineitem").removeClass("selectTask");
                return;
            }
            if (task.pDataObjec.category) {
                var modal = $("#taskDetail");
                modal.find("#cfgId").val(task.pDataObjec.cfgid);
                modal.find("#name").val(task.pDataObjec.description);
                modal.find("#id").val(task.pDataObjec.entityid);
                modal.find("#type").val(task.pDataObjec.category);
                modal.find(".type").html($.i18n._('ganttType.' + task.pDataObjec.category));
                modal.find("#dateStart").val(moment(data.getStart()).format(DEFAULT_DATE_DISPLAY_FORMAT));
                modal.find("#dateEnd").val(moment(data.getEnd()).format(DEFAULT_DATE_DISPLAY_FORMAT));
                var assign = task.pDataObjec.assignedtoid ? ("User_" + task.pDataObjec.assignedtoid) : ("UserGroup_" + task.pDataObjec.assignedgrouptoid);
                modal.find("#assignedTo").val(assign).trigger("change");
                //modal.find(".assignedTo").html(task.pRes)
                modal.find("#complete").val(task.pComp);
                modal.find(".link").attr("href", task.pLink)
                modal.find("#backUrl").val(backUrl + "&selectedTab=" + $(".nav .active a").attr("id"))
                if (data.getDepend() && data.getDepend()[0]) {
                    var val = $("#ganttchild_" + data.getDepend()[0] + " .gtaskname").text();
                    $(".dependson").text(val);
                    $("#dropDependence").attr("disabled", false);
                } else {
                    $(".dependson").text("");
                    $("#dropDependence").attr("disabled", true);

                }
                $("#changeDependence, #dropDependence")
                    .attr("data-id", task.pDataObjec.entityid)
                    .attr("data-type", task.pDataObjec.category);

                modal.modal("show");
            }
        }

        ganttChart.setDateTaskTableDisplayFormat("dd-mon-yyyy");
        ganttChart.setUseToolTip(false);
        ganttChart.setUseSort(false);
        JSGantt.parseJSON(ganttUrl, ganttChart);
        ganttChart.Draw();
        if (typeof backUrl != 'undefined') $("#backUrl").val(backUrl);
    }

    $(document).on("click", ".gcharttable [id^='ganttchildrow_']", function () {
        var id = $(this).attr("id").split("_")[1];
        $("#ganttchild_" + id).trigger('click');

    })

    var changeDependence = false;
    $(document).on('keyup', function (e) {
        if (e.key === "Escape") {
            changeDependence = false;
            $(".glineitem").removeClass("selectTask");
        }
    });

    $(document).on("click", "#changeDependence", function () {
        $(".glineitem").addClass("selectTask");
        changeDependence = "id=" + $("#changeDependence").attr("data-id") + "&type=" + $("#changeDependence").attr("data-type");
    });

    $(document).on("click", "#ganttTabLink", function () {
        if (ganttChart.getDivId() != null)
            ganttChart.Draw();
    });

    $(document).on("click", "#dropDependence", function () {
        var id = $("#changeDependence").attr("data-id");
        var type = $("#changeDependence").attr("data-type");
        $('form').remove();
        location.href = changeDependenceUrl + "?id=" + id + "&type=" + type + "&backUrl=" + encodeURIComponent(backUrl + "&selectedTab=" + $(".nav .active a").attr("id"));
    });


    $('#dateStartDiv,#dateEndDiv').datepicker({
        allowPastDates: true,
        momentConfig: {
            format: DEFAULT_DATE_DISPLAY_FORMAT
        }
    });

});