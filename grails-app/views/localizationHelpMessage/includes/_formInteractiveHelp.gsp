<%@ page import="com.rxlogix.enums.EmailTemplateTypeEnum" %>
<style>
.bootstrap-switch.bootstrap-switch-small .bootstrap-switch-handle-on, .bootstrap-switch.bootstrap-switch-small .bootstrap-switch-handle-off, .bootstrap-switch.bootstrap-switch-small .bootstrap-switch-label {
    font-size: 11px;
}
</style>

<div class="row form-group">
    <div class="col-lg-4">
        <label for="title"><g:message code="app.label.localizationHelp.releaseNotes.title"/></label>
        <input name="title" id="title" value="${instance?.title}" class="form-control " maxlength="4000">
    </div>
    <div class="col-lg-2">
        <div style="margin-top: 22px">
            <label for="published"><g:message code="app.label.systemNotification.publish"/></label>
            <input type="checkbox" name="published" id="published" ${instance?.published?"checked":""} >
        </div>
    </div>


</div>
<div class="row form-group">
    <div class="col-lg-6">
        <label for="page"><g:message code="app.label.interactiveHelp.pages"/></label>
        <textarea id="page" name="page" placeholder="${message(code:'app.label.interactiveHelp.pages.placeholder')}" class="form-control" maxlength="4000" required>${raw(instance?.page)}</textarea>
    </div>
</div>
<div class="row form-group">
    <div class="col-lg-6">
        <label for="description"><g:message code="app.label.localizationHelp.releaseNotes.description"/></label>
        <textarea id="description" style="height: 500px" name="description" class="form-control " required>${raw(instance?.description)}</textarea>
    </div>
    <div class="col-lg-6">
        <label for="description"><g:message code="example"/></label>
        <textarea disabled class="form-control " style="height: 500px" required>
<<<see details on http://bootstraptour.com>>>
{
        debug: true,
        steps: [
        {
        element: $(".rxmain-container")[2],
        title: "Report Sections",
        content: "This is the most important section in report definition. Here we are creating shape and content of the report. ",
        placement: "top",
        backdrop: true,
        backdropPadding: 5
        },
        {
        element: $(".selectTemplate")[0],
        title: "Select Template",
        content: "To create report configuration you should choose template (view of report). <br><br>This field is mandatory.<br><br> Template definition available  on this <a href='f'>page</a> ",
        placement: "top",
        },
        {
        element: $(".selectQuery")[2],
        title: "Select Query",
        content: "You can filter data in you report using predefined queries in this list.<br><br> This field is optional. ",
        placement: "top",
        },
        {
        element: "input[name=reportName]",
        title: "Report Name",
        content: "Please set report name. It should be unique",
        placement: "right",
        },
        {
        element: "#saveAndRunButton",
        title: "Save and Run",
        content: "The simplest report configuration completed, now you can save and run it using this button ",
        placement: "left",
        }
        ]}

        </textarea>
    </div>
</div>

<script>
    $(function () {

        $(":checkbox").bootstrapSwitch('size', 'small');
        $(":checkbox").bootstrapSwitch('onText', 'Yes');
        $(":checkbox").bootstrapSwitch('offText', 'No');
        $(":checkbox").bootstrapSwitch();

        $("form").submit(function(){
           try{
               var x = JSON.parse($("#description").val());
               return true;
           } catch(e){
               alert("Help JSON is not valid, please check!");
               return false;
           }
        });
    });
</script>