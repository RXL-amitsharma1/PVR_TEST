    <a href="javascript:void(0);" class="rxmain-container-header-label pull-right chart-running"  style="display: ${!widget.running?'none':'block'}"
       data-toggle="tooltip" data-placement="bottom" title="${message(code: 'app.chartWidget.running', encodeAs: 'html')}"><i class="fa fa-spinner fa-spin fa-lg"></i></a>
    <a href="javascript:void(0);" id="edit-widget${index}" data-params='{"widgetId": "${widget.reportWidget.id}"}' data-editable="${widget.isEditable}"
       class="edit-widget rxmain-container-header-label pull-right" style="display: ${(widget.running || !widget.isEditable || !isEditable)?'none':'block'}"
       data-toggle="tooltip" data-placement="bottom" title="${message(code: 'default.button.edit.label')}"><i class="md-lg md-pencil"></i></a>
    <a href="javascript:void(0);" id="refresh-widget${index}" data-params='{"widgetId": "${widget.reportWidget.id}"}' data-id="${widget.reportWidget.reportConfiguration?.id}"
       class="refresh-widget rxmain-container-header-label pull-right" style="display: ${(widget.running || !widget.isViewable ||!isEditable)?'none':'block'}"
       data-toggle="tooltip" data-placement="bottom" title="${message(code: 'default.button.refreshWidget.label')}"><i class="md-lg md-refresh"></i></a>
<a href="javascript:void(0);" id="remove-widget${index}" data-url="removeWidgetUrl" data-params='{"widgetId": "${widget.reportWidget.id}"}'
   class="remove-widget rxmain-container-header-label pull-right"
   data-toggle="tooltip" data-placement="bottom" title="${message(code: 'default.button.removeWidget.label')}"><i class="md-lg md-close"></i></a>