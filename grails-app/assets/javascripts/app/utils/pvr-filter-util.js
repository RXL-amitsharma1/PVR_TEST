// This script id a dependent of _pvr_template_utils.js

var pvr = pvr || {};

pvr.filter_util = (function () {
  var FILTER_PANEL_WIDTH = 400;
  var FILTER_SLIDE_DURATION = 300;

  var process_filters = function (filter_defs, filter_group) {
    var regular_data = {};
    var groups = {};

    _.each(filter_defs, function (item) {
      var filter_group_key = filter_group[0].key;
      if (item.filter_group) {
        filter_group_key = item.filter_group;
      }
      if (_.isUndefined(item.group)) {
        if (!regular_data[filter_group_key]) {
          regular_data[filter_group_key] = [];
        }
        regular_data[filter_group_key].push(item);
      } else {
        if (!groups[filter_group_key]) {
          groups[filter_group_key] = {};
        }
        if (
          _.isNull(groups[filter_group_key][item.group]) ||
          _.isUndefined(groups[filter_group_key][item.group])
        ) {
          groups[filter_group_key][item.group] = [];
        }
        if (!groups[filter_group_key]) {
          groups[filter_group_key] = {};
        }
        groups[filter_group_key][item.group].push(item);
      }
    });

    return [regular_data, groups];
  };

  var hide_filter_panel = function (container_id) {
    $(container_id).toggle("slide", {
      direction: "right",
      duration: FILTER_SLIDE_DURATION,
    });
  };

  var show_filter_panel = function (container_id) {
    $(container_id).toggle("slide", {
      direction: "right",
      duration: FILTER_SLIDE_DURATION,
    });
  };

  var apply_filter = function (container_id) {
    const filter = compose_filter(container_id + " .filter-input");
    hide_filter_panel(container_id);
    set_quality_screen_condition();
    if (_.isEmpty(filter)) return "";

    return filter;
  };

  var set_quality_screen_condition = function () {
    //set apply event for quality screens
    if (document.getElementById("qualityScreen") != undefined) {
      isAppliedAdvancedFilter = true;
    }
  };

  var get_view_port_dim = function () {
    return { width: $(window).width(), height: $(window).height() };
  };

  var clear_filter = function (container_id) {
    $(container_id).find(".filter-input:not(:disabled)").val("");
    $(container_id).find("input.filter-input[type='checkbox']").prop("checked", false);
    $(container_id).find(".filter-select2").val("").trigger("change");
    $(container_id).find(".rptField-select2").val("").trigger("change");
    $(container_id).find(".select2-offscreen").val("").trigger("change");
  };

  var compose_filter = function (filterInputSelector) {
      var isValidDate = function(dateStr) {
          return moment(dateStr, DEFAULT_DATE_DISPLAY_FORMAT, true).isValid();
      };
      var create_filter_unit = function (type, name, value, order, dataType) {
      var unit = {};
      switch (type) {
        case "range":
          unit = { type: "range", name: name };
          unit["value" + order] = value;
          break;
        case "number-range":
          unit = { type: "number-range", name: name };
          unit["value" + order] = value * 60 * 1000;
          break;
        case "value":
        case "boolean":
          unit = { type: "value", name: name, value: value };
          break;
        case "enum":
          unit = { type: "enum", name: name, value: value, dataType: dataType };
          break;
        case "text":
        case "multi-value-text":
        case "multi-value-number":
        case "multi-value":
        case "id":
        case "multi-value-id":
        case "date":
        case "manual":
          unit = { type: type, name: name, value: value };
          break;
        case "select":
        case "select2":
          unit = { type: "text", name: name, value: value };
          break;
        case 'rptField-select2':
          unit = {type: 'rptField-select2', name: name, value: value};
          break;
      }

      return unit;
    };

    return _.reduce($(filterInputSelector), function (memo, next) {
        var type = $(next).data("type");
        var name = $(next).data("name");

        if (
          !(
            _.isNull(type) ||
            _.isUndefined(type) ||
            _.isNull(name) ||
            _.isUndefined(name)
          )
        ) {
          switch (type) {
            case "date-range":
              var order = $(next).data("order");
              var val = $(next).val();
              if (!(_.isNull(val) || _.isUndefined(val) || _.isEmpty(val))) {
                if (isValidDate(val)) {
                    var unit = create_filter_unit("range", name, val, order);
                    if (!(_.isNull(memo[name]) || _.isUndefined(memo[name]))) {
                        memo[name] = _.extend(memo[name], unit);
                    } else {
                        memo[name] = unit;
                    }
                }
              }
              break;
            case "number-range":
              var order = $(next).data("order");
              var val = $(next).val();
              if (!(_.isNull(val) || _.isUndefined(val) || _.isEmpty(val))) {
                var unit = create_filter_unit("number-range", name, val, order);

                if (!(_.isNull(memo[name]) || _.isUndefined(memo[name]))) {
                  memo[name] = _.extend(memo[name], unit);
                } else {
                  memo[name] = unit;
                }
              }
              break;
            case "select2-multi-id":
              var val = $(next).select2("val");
              if (!(_.isNull(val) || _.isUndefined(val) || _.isEmpty(val))) {
                var unit = create_filter_unit("multi-value-id", name, val);
                memo[name] = unit;
              }
              break;
            case "select2-multi-value":
              var val = $(next).select2("val");
              if (!(_.isNull(val) || _.isUndefined(val) || _.isEmpty(val))) {
                const valueType = $(next).data("value-type");
                var unit = create_filter_unit(valueType === 'number' ? 'multi-value-number' : 'multi-value', name, val);
                memo[name] = unit;
              }
              break;
            case "date":
                var val = $(next).val();
                if (!(_.isNull(val) || _.isUndefined(val) || _.isEmpty(val))) {
                    if (isValidDate(val)) {
                        var unit = create_filter_unit('date', name, val);
                        if (!(_.isNull(memo[name]) || _.isUndefined(memo[name]))) {
                            memo[name] = _.extend(memo[name], unit);
                        } else {
                            memo[name] = unit;
                        }
                    }
                }
                break;
            case "text":
            case "multi-value-text":
            case "select":
            case "select2":
              var val = $(next).val();
              if (!(_.isNull(val) || _.isUndefined(val) || _.isEmpty(val))) {
                var unit = create_filter_unit(type, name, val);
                memo[name] = unit;
              }
              break;
            case "select2-manual":
              var val = $(next).val();
              if (!(_.isNull(val) || _.isUndefined(val) || _.isEmpty(val))) {
                var unit = create_filter_unit("manual", name, val);
                memo[name] = unit;
              }
              break;
            case "boolean":
              var val = $(next).select2("val");
              var unit = create_filter_unit("boolean", name, val);
              if (!(_.isNull(val) || _.isEmpty(val))) {
                memo[name] = unit;
              }

              break;
            case "select2-id":
              var val = $(next).val();
              if (!(_.isNull(val) || _.isUndefined(val) || _.isEmpty(val))) {
                var unit = create_filter_unit("id", name, val);
                memo[name] = unit;
              }
              break;
            case "id":
              var val = $(next).val();
              if (!(_.isNull(val) || _.isUndefined(val) || _.isEmpty(val))) {
                var unit = create_filter_unit("id", name, val);
                memo[name] = unit;
              }
              break;
            case "select2-enum":
              var val = $(next).val();
              var dataType = $(next).data("dataType");
              if (!(_.isNull(val) || _.isUndefined(val) || _.isEmpty(val))) {
                var unit = create_filter_unit("enum", name, val, -1, dataType);
                memo[name] = unit;
              }
              break;
            case "rptField-select2":
              var val = $(next).select2("val");
              if (!(_.isNull(val) || _.isUndefined(val) || _.isEmpty(val))) {
                  var unit = create_filter_unit('rptField-select2', name, val);
                  memo[name] = unit;
              }
              break;
            case "value":
            default:
              var val = $(next).val();
              if (!(_.isNull(val) || _.isUndefined(val) || _.isEmpty(val))) {
                var unit = create_filter_unit("value", name, val);
                memo[name] = unit;
              }
              break;
          }

          //set selection as init data for async select2 initialization from preferences/session storage
          if (!_.isEmpty(memo[name]) && $(next).data('async-select') === true) {
            const selectData = $(next).select2('data');
            memo[name].initData = $.map(!_.isEmpty(selectData) ? selectData : [], function (dataItem) {
              return {id: dataItem.id, text: dataItem.text};
            })
          }
        }
        return memo;
      }, {});
  };

  var isFilterFilled = function () {
    var found = _.find($(".filter-input"), function (ele) {
      var v = $(ele).val();
      return !(_.isEmpty(v) || _.isNull(v));
    });

    return !(_.isNull(found) || _.isUndefined(found));
  };

  var construct_right_filter_panel = function (opts) {
    var container_id = opts.container_id;
    var filter_defs = opts.filter_defs;
    var column_cnt = opts.column_count;
    var done_func = opts.done_func;

    if (opts.panel_width) {
      FILTER_PANEL_WIDTH = opts.panel_width;
    }
    var filter_group;
    if (opts.filter_group) {
      filter_group = opts.filter_group;
    } else {
      filter_group = [{ key: "default" }];
    }
    var handle_filter_toggle_button = function () {
      $(".lib-filter").on("click", function () {
        if ($(container_id).is(":visible")) {
          hide_filter_panel("#" + container_id);
        } else {
          position_filter("#" + container_id);
          show_filter_panel("#" + container_id);
        }
      });
    };

    var processed_data = process_filters(filter_defs, filter_group);
    var content = "";
    for (var i = 0; i < filter_group.length; i++) {
      var filtergroupkey = filter_group[i].key;
      var format_filter_group_content = "";

      if (processed_data[0][filtergroupkey]) {
        format_filter_group_content = format_filter_data(
          processed_data[0][filtergroupkey],
          column_cnt
        );
      }
      if (processed_data[1][filtergroupkey]) {
        format_filter_group_content += format_group_data(
          processed_data[1][filtergroupkey],
          column_cnt
        );
      }
      if (i > 0 && $.trim(format_filter_group_content) != "") {
        content += " <hr> ";
      }
      content += format_filter_group_content;
    }
    //var regular_content = format_filter_data(processed_data[0], column_cnt);
    //var group_content = format_group_data(processed_data[1], column_cnt);

    var filter_panel = pvr.common_util.render_tmpl("filter_panel", {
      id: container_id,
      filter_body: content,
      apply: $.i18n._('app.advancedFilter.apply'),
      clear: $.i18n._('app.advancedFilter.clear')
    });
    var htmlEle = $.parseHTML(filter_panel);
    const $filterPanelContent = $(htmlEle);
    $filterPanelContent.appendTo('body');

    $(".filter-select2")
      .select2({
        //some placeholder is required for allowClear
        placeholder: '',
        allowClear: true,
      })
      .val(null)
      .trigger("change");

    $(".filter-select2").css({
      display: "block",
    });

    $(".filter-date-picker").datepicker({
      allowPastDates: true,
      date: null,
      momentConfig: {
        culture: userLocale,
        format: DEFAULT_DATE_DISPLAY_FORMAT,
      },
    });
    $(".datepicker input.form-control").addClass("filter-input");
    $("#" + container_id)
      .find(".apply-button")
      .on("click", function (evt) {
        evt.preventDefault();
        var filterStr = apply_filter("#" + container_id);
        done_func(filterStr);
        if (isFilterFilled()) {
          fillFilter(true);
        } else {
          fillFilter(false);
        }
        return false;
      });

    $("#" + container_id)
      .find(".clear-button")
      .on("click", function (evt) {
        evt.preventDefault();
        clear_filter("#" + container_id);
        fillFilter(false);
        return false;
      });

    $("#" + container_id)
      .find("input.filter-input")
      .on("keypress", function (e) {
        if (e.which == 13) {
          e.preventDefault();
          done_func(apply_filter("#" + container_id));
          return false;
        }
      });

    position_filter("#" + container_id);
    $("#" + container_id).hide();
    handle_filter_toggle_button();

    pvr.common_util.setCalanderPosition();

    $("#mainContent,.topbar,#sidebar-menu").on("click", function () {
      $("#" + container_id).hide();
    });

    $("#" + container_id)
      .find(".close")
      .on("click", function (evt) {
        $("#" + container_id).hide();
      });

    $(window).on("resize", function (evt) {
      position_filter("#" + container_id);
    });

    $("select[data-type='rptField-select2']").each(function () {
      var field = $(this);
      $(field).select2({
        minimumInputLength: 0,
        separator: ";",
        multiple: true,
        closeOnSelect: false,
        ajax: {
          dataType: "json",
          url: possibleValuesUrl,
          data: function (params) {
            return {
              field: field.data("name"),
              term: params.term ? params.term :"",
              lang: userLocale,
              page: params.page,
              max: 30,
            };
          },
          processResults: function (data) {
            var isMore = 30 == data.length;
            return {
              results: data,
              pagination: {
                more: isMore,
              },
            };
          },
        },
      });
    });
    $(".rptField-select2").css({
      display: "block",
    });
  };

  var construct_column_level_filter = function (opts) {
    var table_id = opts.table_id;
    var $table = opts.table;
    var done_func = opts.done_func;
    $(table_id + " thead tr")
      .clone(true)
      .appendTo(table_id + " thead")
      .addClass("column_filter_input")
      .hide();
    $(table_id + " thead tr:eq(1) th").each(function (i) {
      var title = $(this).text();
      var id = $(this).data("id");
      var type = $(this).data("type");
      var disabled = type == "disabled" ? "disbaled" : "";
      if (id) {
        var input =
          '<input type="text" disabled="disabled" class="form-control" style="width: 98%" placeholder="' +
          title +
          '" data-id="' +
          id +
          '" data-type=' +
          type +
          " />";
        $(this).html(input);
        var timer = null;
        $("input", this).on("keyup change", function (e) {
          if (timer) clearTimeout(timer);
          timer = setTimeout(function () {
            var searchJSON = {};
            $(table_id + " thead tr:eq(1) th input").each(function (i) {
              searchJSON[$(this).attr("data-id")] = {
                type: $(this).attr("data-type"),
                val: $(this).val(),
              };
            });
            if (JSON.stringify(searchJSON) != $table.search()) {
              done_func(searchJSON);
            }
          }, 1000);
        });
      } else {
        $(this).html("");
      }
    });
    $(".column-filter-toggle").on("click", function () {
      $(table_id + " thead tr:eq(1) th").each(function (i) {
        var visible = $(table_id).DataTable().column(i).visible();
        if (!visible) {
          $(".column_filter_input")
            .find("th:eq(" + i + ")")
            .hide();
        }
      });
      $(".column_filter_input").toggle("slow", function () {
        // check paragraph once toggle effect is completed
        if (!$(".column_filter_input").is(":visible")) {
          $(".column_filter_input").trigger("removeSearch");
        }
      });
    });
    $(".column_filter_input").on("removeSearch", function () {
      var hasSearchData = false;
      $(table_id + " thead tr:eq(1) th input").each(function (i) {
        if ($(this).val()) {
          hasSearchData = true;
        }
        $(this).val("");
      });
      if (hasSearchData) {
        $table.draw();
      }
    });
  };

  var position_filter = function (filter_container_id) {
    // Initiate div position
    var view_port_dim = get_view_port_dim();
    var offset = $(".lib-filter").offset();
    var top = offset.top < 0 ? 25 : offset.top + 25;
    $(filter_container_id)
      .css("top", top + "px")
      .css("width", FILTER_PANEL_WIDTH + "px")
      .css("position", "absolute")
      .css("margin-left", view_port_dim.width - FILTER_PANEL_WIDTH - 25 + "px");
  };

  var update_filter_icon_state = function ($table, filterData) {
    const $filterIcon = $table.closest('.rxmain-container').find('.lib-filter');
    if ($filterIcon.length === 0) {
      return;
    }

    if (_.isEmpty(filterData)) {
      $filterIcon.removeClass('filter-applied');
    } else if (!$filterIcon.hasClass('filter-applied')) {
      $filterIcon.addClass('filter-applied');
    }
  }

  var fillFilter = function (filled) {
    if (filled) {
      $(".lib-filter").css("color", "black");
    } else $(".lib-filter").css("color", "#999");
  };

  var format_filter_data = function (filter_defs, column_cnt) {
    if (filter_defs === null) {
      return "";
    } else {
      var rowCount = Math.ceil(filter_defs.length / column_cnt);
      var col_width = Math.floor(12 / column_cnt);

      var output = "";
      for (var r = 0; r < rowCount; r++) {
        var rowContent = "<div class='row'>";

        for (var c = 0, idx = 0; c < column_cnt; c++) {
          idx = r * column_cnt + c;
          if (idx < filter_defs.length) {
            rowContent +=
              '<div class="col-sm-' +
              col_width +
              '">' +
              '<label class="col-sm-5" style="text-align: right;"><b>' +
              filter_defs[idx].label +
              ":</b></label>" +
              '<div class="col-sm-7" style="vertical-align: bottom;">' +
              build_filter(filter_defs[idx]) +
              "</div>" +
              "</div>\n";
          } else {
            rowContent += '<div class="col-sm-' + col_width + '"></div>';
          }
        }
        rowContent += "</div>";
        output += rowContent + "\n";
      }
      return output;
    }
  };

  var format_group_data = function (group_data, column_cnt) {
    if (group_data) {
      var content = "";
      var col_width = Math.floor(12 / column_cnt);
      _.each(_.pairs(group_data), function (items) {
        var rowContent = "";
        var sortedItems = _.sortBy(items[1], function (i) {
          return i.group_order;
        });
        rowContent += "<div class='row'>";
        if (sortedItems[0].type == "number-range") {
          rowContent +=
            '<div class="col-sm-' +
            col_width +
            '">' +
            '<label class="col-sm-5" style="text-align: right;"><b>' +
            sortedItems[0].label +
            ":</b></label>" +
            '<div class="col-sm-3">' +
            build_filter(sortedItems[0]) +
            "</div>" +
            '<label class="col-sm-1" style="text-align: right;">' +
            sortedItems[1].label +
            "</label>" +
            '<div class="col-sm-3">' +
            build_filter(sortedItems[1]) +
            "</div>" +
            "</div>\n";
        } else {
          _.each(sortedItems, function (item) {
            rowContent +=
              '<div class="col-sm-' +
              col_width +
              '">' +
              '<label class="col-sm-5" style="text-align: right;"><b>' +
              item.label +
              ":</b></label>" +
              '<div class="col-sm-7">' +
              build_filter(item) +
              "</div>" +
              "</div>\n";
          });
        }
        rowContent += "</div>";
        content += rowContent;
      });
      return content;
    } else {
      return "";
    }
  };

  var build_filter = function (meta_data) {
    var handle_ajax = function (meta) {
      var tmp = "";

      //skip async filters due to integrated into extended data table
      if (!meta.ajax.async) {
        $.ajax({
          url: meta.ajax.url,
          async: false,
          dataType: "json",
        }).done(function (data) {
          tmp = meta.ajax.data_handler(data);
        }).fail(function (data) {
          meta.ajax.error_handler(data);
        });
      }

      return tmp;
    };

    function getAsyncSelectAttr(metaData) {
      if (!_.isEmpty(metaData) && !_.isEmpty(metaData.ajax) && metaData.ajax.async === true) {
        return 'data-async-select="true"';
      }
      return '';
    }

    var content = "";
    switch (meta_data.type) {
      case "boolean":
        var attr_text =
          "data-name='" +
          meta_data.name +
          "' data-type='" +
          meta_data.type +
          "'";
        content =
          "<select data-placeholder='' style='width: 100%;' id='testselect' class='filter-input form-control filter-select2' " +
          attr_text +
          ">\n";
        content += "<option value='true'>True</option>\n";
        content += "<option value='false'>False</option>\n";
        content += "</select>";
        break;
      case "date-range":
        var attr_text =
          "data-name='" +
          meta_data.group +
          "' data-type='date-range' data-order='" +
          meta_data.group_order +
          "'";
        content =
          "<div>" +
          pvr.common_util.render_tmpl("datepicker", {
            id: "tmp-id",
            title: "Title",
            cls: "filter-date-picker",
            extra_attrs: attr_text,
          }) +
          "</div>";
        break;
      case "date":
        var attr_text =
          "data-name='" +
          meta_data.name +
          "' data-type='" +
          meta_data.type +
          "'";
        content =
          "<div>" +
          pvr.common_util.render_tmpl("datepicker", {
            id: "tmp-id",
            title: "Title",
            cls: "filter-date-picker",
            extra_attrs: attr_text,
          }) +
          "</div>";
        break;
      case "select":
        content =
          "<select style='width: 100%;' " + getAsyncSelectAttr(meta_data) + " data-name='" +
          meta_data.name +
          "' data-type='" +
          meta_data.type +
          "'>";
        if (meta_data.ajax) {
          content += handle_ajax(meta_data);
        }
        content += "</select>";
        break;
      case "select2":
      case "select2-id":
        content =
          "<select style='width: 100%;' " + getAsyncSelectAttr(meta_data) + " class='filter-input form-control filter-select2' " +
          "data-name='" +
          meta_data.name +
          "' data-type='" +
          meta_data.type +
          "' data-placeholder=''>";
        if (meta_data.ajax) {
          content += handle_ajax(meta_data);
        }
        if (meta_data.data) {
            content += build_options(meta_data.data, 'key', 'value', true);
        }
        content += "</select>";
        break;
      case "select2-users":
        if (meta_data.multiple) multipleSelect = " multiple= 'multiple' ";
        content =
          "<select style='width: 100%;' class='filter-input form-control filter-select2' " +
          "data-name='" +
          meta_data.name +
          "' data-type='" +
          meta_data.type +
          "'" +
          multipleSelect +
          ">";
        content += build_optionAssignees(meta_data.data, "key", "value", true);
        content += "</select>";
        break;
      case "id":
      case "rptField-select2":
      case "select2-manual":
      case "select2-enum":
        var placeholder = meta_data.type === "id" ? $.i18n._("selectOne") : "";
        _.each(meta_data.data, function (data_item) {
          if (data_item["selected"]) {
            placeholder = data_item["placeholder"];
          }
        });
        var multipleSelect = "";
        if (meta_data.multiple) multipleSelect = " multiple= 'multiple' ";
        content =
          "<select style='width: 100%;' data-placeholder ='" +
          placeholder +
          "' data-name='" +
          meta_data.name +
          "'" +
          " class='filter-input form-control filter-select2' data-type='" +
          meta_data.type +
          "' data-data-type='" +
          meta_data.data_type +
          "'" +
          multipleSelect +
          ">";
        content += build_options(meta_data.data, "key", "value", true);
        content += "</select>";
        break;
      case "select2-multi-value":
      case "select2-multi-id":
        content =
          "<select style='width: 100%;' " + getAsyncSelectAttr(meta_data) + " data-name='" +
          meta_data.name +
          "'" +
          " class='filter-input form-control filter-select2' multiple='multiple' data-value-type='" + (meta_data.valueType || '')  + "' data-type='" +
          meta_data.type +
          "'>";
        if (meta_data.ajax) {
          var opts = handle_ajax(meta_data);
          content += opts;
        }
        content += "</select>";
        break;
      case "number-range":
        content =
          "<input class='filter-input form-control' style='width:100%' data-type='" +
          meta_data.type +
          "' type='number' data-name='" +
          meta_data.group +
          "' data-order= '" +
          meta_data.group_order +
          "' onkeypress='return (event.keyCode >47 && event.keyCode <58)' min='0'>";
        break;
      case "number":
        content =
          "<input class='filter-input form-control' style='width:100%' data-type='" +
          meta_data.type +
          "' type='number' data-type='" +
          meta_data.type +
          "' " +
          " data-name='" +
          meta_data.name +
          "' onkeypress='return (event.keyCode >47 && event.keyCode <58)' min='0'>";
        break;
      case "natural-number":
        content =
          "<input class='filter-input natural-number-filter form-control' style='width:100%' data-type='" +
          meta_data.type +
          "' type='number' data-type='" +
          meta_data.type +
          "' " +
          " data-name='" +
          meta_data.name +
          "' min='0' max='" +
          (meta_data.maxValue ? meta_data.maxValue : "999999999999999") +
          "'>";
        break;
      case "multi-varchar":
        content =
          "<input class='filter-input form-control caseNum-filter' style='width: 85%' data-type='VARCHAR2' " +
          " data-name='" +
          meta_data.name +
          "'><i class='fa fa-pencil-square-o copy-n-paste modal-link' data-toggle='modal' data-target='#copyAndPasteModal' style='margin-top: -5px; cursor: pointer;'></i>";
        break;
      case "text":
      case "multi-value-text":
        var valueAttribute = meta_data.value ? " value='" + meta_data.value + "'" : "";
        var disabledAttribute = meta_data.disabled == true ? " disabled='disabled'" : "";
        content = "<input class='filter-input form-control " + meta_data.type +
            "' style='width: 100%;' data-type='" +
            meta_data.type + "' " + " data-name='" + meta_data.name + "' maxlength=" +
            (meta_data.maxlength ? meta_data.maxlength : 255) + valueAttribute + disabledAttribute + ">";
        break;
      case "value":
      default:
        content =
          "<input class='filter-input form-control " +
          meta_data.type +
          "' style='width: 100%;' data-type='" +
          meta_data.type +
          "' " +
          " data-name='" +
          meta_data.name +
          "' maxlength=" +
          (meta_data.maxlength ? meta_data.maxlength : 255) +
          ">";
    }

    return content;
  };

  var build_options = function (data, value_fld, label_fld, empty_selection) {
    var options;
    _.each(data, function (data_item) {
      options +=
        "<option value='" +
        data_item[value_fld] +
        "'>" +
        data_item[label_fld].replaceAll("<", "&lt").replaceAll(">", "&gt") +
        "</option>\n";
    });

    return options;
  };

  var build_optionAssignees = function (
    data,
    value_fld,
    label_fld,
    empty_selection
  ) {
    var userCount = 0;
    var groupCount = 0;
    var options;
    _.each(data, function (data_item) {
      if (data_item[value_fld].startsWith("UserGroup_")) {
        if (groupCount === 0) {
          options += "<optgroup label='" + $.i18n._("userGroup") + "'>\n";
        }
        ++groupCount;
      }

      if (data_item[value_fld].startsWith("User_")) {
        if (userCount === 0) {
          if (groupCount > 0) options += "</optgroup>\n";
          options += "<optgroup label='" + $.i18n._("user") + "'>\n";
        }
        ++userCount;
      }
      options +=
        "<option value='" +
        data_item[value_fld] +
        "'>" +
        data_item[label_fld] +
        "</option>\n";
    });
    if (userCount > 0 || groupCount > 0) options += "</optgroup>\n";
    return options;
  };

  return {
    construct_right_filter_panel: construct_right_filter_panel,
    compose_filter: compose_filter,
    build_options: build_options,
    build_optionAssignees: build_optionAssignees,
    construct_column_level_filter: construct_column_level_filter,
    update_filter_icon_state: update_filter_icon_state
  };
})();
