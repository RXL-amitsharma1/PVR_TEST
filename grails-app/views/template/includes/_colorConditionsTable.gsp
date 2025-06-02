<style>
    .colorParameterTable .selectedRow {
        background-color: #CBE8F6;
    }
</style>
<div class="row d-t-border- colorParameterTable">
    <div class="rxmain-container-header">
        <a class="theme-color conditional-strip" href="javascript:void(0)"><i class="openCloseIcon fa fa-lg fa-caret-right "></i> <span><g:message code="app.dataTabulation.ConditionalFormatting"/></span></a>

        <span class="addParameter md md-plus md-lg m-t-5" style= "cursor: pointer; float:right"></span>
        <span class="colorConditionDown md md-arrow-down md-lg m-t-5" style= "cursor: pointer; float:right"></span>
        <span class="colorConditionUp md md-arrow-up md-lg m-t-5" style= "cursor: pointer; float:right"></span>
    </div>
</div>
   <div class="m-t-10 color-section" style="display: none">
    <table width="100%" class="table colorParameterTable m-t-0 m-b-0">
        <thead>

        <tr class="colorParameterTemplateRow row-bg " style="display: none">

            <td width="100%">
                <input type="hidden" class="colorConditionRowJson">

                <b><g:message code="app.dataTabulation.if"/></b>&nbsp;
                <span class="conditionsSpan">?</span>

                <b>&nbsp;<g:message code="app.dataTabulation.then"/></b>
                <span class="rowFormatSample"  style="background: #ffffff;padding: 3px;border-radius: 4px;border: #cccccc solid 1px;"></span>
            </td>

            <td>
                <span class="glyphicon glyphicon-edit  showColorConditionModal" style="cursor:pointer; " data-toggle="modal" data-target="#colorConditionModal"></span>
            </td>
            <td>
                <span style="cursor: pointer;" class="m-t-5 md-lg md-close sectionRemove"></span>
            </td>
        </tr>
        </thead>
        <tbody>

        </tbody>
    </table>
   </div>