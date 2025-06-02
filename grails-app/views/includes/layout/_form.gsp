<div class="row form-group">

    <div class="col-lg-12">
        <label for="label"><g:message code="app.label.dashboardDictionary.label" /><span class="required-indicator">*</span></label>
        <input required  id="label" name="label" value=""  class="form-control"/>
    </div>
</div>

<div class="row form-group">
    <div class="col-lg-12">
        <label for="dashboardType"><g:message code="app.label.dashboardDictionary.type" /><span class="required-indicator">*</span></label>
        <select name="dashboardType" id="dashboardType" class="form-control "></select>
    </div>
</div>

<div class="row form-group">
    <div class="col-xs-12">
        <label for = "sharedWith"><g:message code="shared.with"/></label>
        <select id="sharedWith" name="sharedWith" multiple="multiple" class="form-control" autocomplete="off">
            <optgroup label="${g.message(code: 'user.group.label')}">
            </optgroup>
            <optgroup label="${g.message(code: 'user.label')}">
            </optgroup>
        </select>
    </div>
</div>

<div class="row form-group">
    <div class="col-lg-12">
        <label for="dashboardIcon"><g:message code="app.label.dashboardDictionary.icon" /></label>
        <select id="dashboardIcon" name="dashboardIcon" class="form-control" >
            <option value="md md-dashboard">md md-dashboard</option>
            <option value="md md-settings">md md-settings</option>
            <option value="md md-bell">md md-bell</option>
        </select>
    </div>
</div>
