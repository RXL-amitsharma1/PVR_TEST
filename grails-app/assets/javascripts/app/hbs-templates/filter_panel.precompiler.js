(function() {
  var template = Handlebars.template, templates = Handlebars.templates = Handlebars.templates || {};
templates['filter_panel.hbs'] = template({"compiler":[8,">= 4.3.0"],"main":function(container,depth0,helpers,partials,data) {
    var stack1, helper, alias1=depth0 != null ? depth0 : (container.nullContext || {}), alias2=container.hooks.helperMissing, alias3="function", lookupProperty = container.lookupProperty || function(parent, propertyName) {
        if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
          return parent[propertyName];
        }
        return undefined
    };

  return "<div id='"
    + container.escapeExpression(((helper = (helper = lookupProperty(helpers,"id") || (depth0 != null ? lookupProperty(depth0,"id") : depth0)) != null ? helper : alias2),(typeof helper === alias3 ? helper.call(alias1,{"name":"id","hash":{},"data":data,"loc":{"start":{"line":1,"column":9},"end":{"line":1,"column":15}}}) : helper)))
    + "' class=\"panel filter-panel  panel-zindex\">\n    <div class=\"filter-panel-heading\">\n        <label class=\"panel-title theme-color\">Filters</label>\n        <button type=\"button\" class=\"close\" style=\"opacity:1.2;\">\n            <span aria-hidden=\"true\">&times;</span>\n            <span class=\"sr-only\"><g:message code=\"default.button.close.label\"/></span>\n        </button>\n    </div>\n\n    <div class=\"panel-body panel-zindex\">"
    + ((stack1 = ((helper = (helper = lookupProperty(helpers,"filter_body") || (depth0 != null ? lookupProperty(depth0,"filter_body") : depth0)) != null ? helper : alias2),(typeof helper === alias3 ? helper.call(alias1,{"name":"filter_body","hash":{},"data":data,"loc":{"start":{"line":10,"column":41},"end":{"line":10,"column":58}}}) : helper))) != null ? stack1 : "")
    + "</div>\n    <div class=\"row\" style=\" padding-right: 25px; padding-bottom:5px;\">\n        <button class=\"btn btn-default pull-right clear-button\" style=\"margin-right: 10px;\">"
    + ((stack1 = ((helper = (helper = lookupProperty(helpers,"clear") || (depth0 != null ? lookupProperty(depth0,"clear") : depth0)) != null ? helper : alias2),(typeof helper === alias3 ? helper.call(alias1,{"name":"clear","hash":{},"data":data,"loc":{"start":{"line":12,"column":92},"end":{"line":12,"column":103}}}) : helper))) != null ? stack1 : "")
    + "</button>\n        <button class=\"btn btn-primary pull-right apply-button\" style=\"margin-right: 10px;\">"
    + ((stack1 = ((helper = (helper = lookupProperty(helpers,"apply") || (depth0 != null ? lookupProperty(depth0,"apply") : depth0)) != null ? helper : alias2),(typeof helper === alias3 ? helper.call(alias1,{"name":"apply","hash":{},"data":data,"loc":{"start":{"line":13,"column":92},"end":{"line":13,"column":103}}}) : helper))) != null ? stack1 : "")
    + "</button>\n    </div>\n</div>";
},"useData":true});
})();