var ELEMENT_TYPE = {
  TAG: "TAG",
  ATTRIBUTE: "ATTRIBUTE",
};
var hasNullTitle;

$(function () {
  console.log("Started Loading Document EditXMLTemplate.js");
  viewOnly = $("#editable").val() === "false";
  if (viewOnly) {
    $("input").prop("disabled", true);
    $("#treePanel button").prop("disabled", true);
    $("textarea").prop("disabled", true);
    $("select").prop("disabled", true);
  }

  $("#sourceFieldLabel")
    .select2({ placeholder: $.i18n._("selectOne"), allowClear: true })
      .on("select2:open", function (e) {
        var searchField = $('.select2-dropdown .select2-search__field');
        if (searchField.length) {
          searchField[0].focus();
        }
      })
    .on("select2:select", function (event) {
      var tree = $.ui.fancytree.getTree("#tagsTree");
      if (tree.activeNode) {
        tree.activeNode.data.sourceFieldLabel = $(this).select2("val");
      }
    });

  $("#e2bLocale").select2({ placeholder: $.i18n._("selectOne"), allowClear: true })

  $(function() {
    $("#e2bLocale").trigger("change");
    if ($("#e2bLocale").attr("disabled") == "disabled") {
      $("#e2bLocaleElementName").attr('disabled', true);
    }
  });

  bindSelect2WithUrl(
    $("#selectCLL"),
    specificCllCSQLTemplateSearchUrl,
    templateNameUrl,
    true
  ).on("select2:open", function (e) {
    var searchField = $('.select2-dropdown .select2-search__field');
    if (searchField.length) {
      searchField[0].focus();
    }
  }).on("select2:select", function (e) {
    if (e.params?.data && e.params?.data?.id == "") {
      $(this).parent().addClass("has-error");
    } else {
      $(this).parent().removeClass("has-error");
    }
  });

  bindreportFieldInfoSelect2($("#reportFieldInfo")).on(
    "select2:select",
    function (e) {
      if (e.params.data && e.params.data.id == "") {
        $(this).parent().addClass("has-error");
      } else {
        $(this).parent().removeClass("has-error");
      }
    }
  );

  bindreportFieldInfoSelect2($("#filterFieldInfo"), true).on("select2:open", function (e) {
    var searchField = $('.select2-dropdown .select2-search__field');
    if (searchField.length) {
      searchField[0].focus();
    }
  }).on(
    "select2:select",
    function (e) {
      if (e.params.data && e.params.data.id == "") {
        $(this).parent().addClass("has-error");
      } else {
        $(this).parent().removeClass("has-error");
      }
    }
  );

  $("#selectCLL").on("select2:select", function (event) {
    var tree = $.ui.fancytree.getTree("#tagsTree");
    if (tree.activeNode) {
      tree.activeNode.data.templateId = $(this).select2("val");
      tree.activeNode.data.filterFieldInfo = null;
    }
  });
  $("#filterFieldInfo").on("select2:select", function (event) {
    console.log("FilterFieldInfo Started");
    var tree = $.ui.fancytree.getTree("#tagsTree");
    if (tree.activeNode) {
      var data = $(this).select2("data")?.[0];
      if (data == null) {
        tree.activeNode.data.filterFieldInfo = null;
        tree.activeNode.data.customSQLFilterFiledInfo = null;
      } else if (data.type == "CLL") {
        console.log("Loading filterFieldInfo for CLL");
        tree.activeNode.data.filterFieldInfo = data;
        tree.activeNode.data.customSQLFilterFiledInfo = null;
      } else if (data.type == "CustomSQL") {
        console.log("Loading FilterFieldInfo For CSQL");
        tree.activeNode.data.filterFieldInfo = null;
        tree.activeNode.data.customSQLFilterFiledInfo = data;
      }
    }
    console.log("FilterFieldInfo Ended");
  });
  $("#reportFieldInfo").on("select2:open", function (e) {
    var searchField = $('.select2-dropdown .select2-search__field');
    if (searchField.length) {
      searchField[0].focus();
    }
  }).on("select2:select", function (event) {
    console.log("reportFieldInfo Started");
    var tree = $.ui.fancytree.getTree("#tagsTree");
    if (tree.activeNode) {
      var data = $(this).select2("data")?.[0];
      if (data == null) {
        tree.activeNode.data.reportFieldInfo = null;
        tree.activeNode.data.customSQLFieldInfo = null;
      } else if (data.type == "CLL") {
        console.log("Loading reportFieldInfo for CLL");
        tree.activeNode.data.reportFieldInfo = data;
        tree.activeNode.data.customSQLFieldInfo = null;
      } else if (data.type == "CustomSQL") {
        console.log("Loading reportFieldInfo for CSQL");
        tree.activeNode.data.reportFieldInfo = null;
        tree.activeNode.data.customSQLFieldInfo = data;
      }
      tree.activeNode.data.templateId = data ? data.templateId : null;
    }
    console.log("reportFieldInfo Ended");
  });

  var glyph_opts = {
    map: {
      doc: "glyphicon glyphicon-file",
      docOpen: "glyphicon glyphicon-file",
      checkbox: "glyphicon glyphicon-unchecked",
      checkboxSelected: "glyphicon glyphicon-check",
      checkboxUnknown: "glyphicon glyphicon-share",
      dragHelper: "glyphicon glyphicon-play",
      dropMarker: "glyphicon glyphicon-arrow-right",
      error: "glyphicon glyphicon-warning-sign",
      expanderClosed: "glyphicon glyphicon-menu-right",
      expanderLazy: "glyphicon glyphicon-menu-right", // glyphicon-plus-sign
      expanderOpen: "glyphicon glyphicon-menu-down", // glyphicon-collapse-down
      folder: "glyphicon glyphicon-folder-close",
      folderOpen: "glyphicon glyphicon-folder-open",
      loading: "glyphicon glyphicon-refresh glyphicon-spin",
    },
  };

  $("#tagsTree").fancytree({
    extensions: ["dnd", "edit", "glyph", "wide"],
    activate: function (event, data) {
      // A node was activated: display its title:
      var node = data.node;
      $("#tName").val(encodeToHTML(node.title));
      $("#e2bElement").val(node.data.e2bElement ? node.data.e2bElement : "");
      $("#e2bElementName").val(
        node.data.e2bElementName ? node.data.e2bElementName : ""
      );
      $("#e2bLocaleElementName").val(node.data.e2bLocaleElementName ? node.data.e2bLocaleElementName : "");
      $('#e2bLocale').val(node.data.e2bLocale);
      $("#sourceFieldLabel").val(node.data.sourceFieldLabel ? node.data.sourceFieldLabel : "").trigger('change');
      $("#sourceFieldLabelVal").val(node.data.sourceFieldLabelVal ? node.data.sourceFieldLabelVal : "").trigger('change');
      $("#tagColor").val(node.tagColor);
      $("#" + node.data.type).prop("checked", true);
      showTab($("#" + node.data.type));

      var id = node.data.templateId;
      if (id) {
        $("#selectCllId").attr("href", templateViewUrl + "/" + id);
        $.get(templateNameUrl + "?id=" + node.data.templateId, function (data) {
          if (data.sessionTimeOut == true) {
            data.text = "";
          }
          $("#selectCLL").append(
            new Option(data.text, node.data.templateId, true, true)
          );
          $("#selectCLL").val(id).trigger("change");
        });
      } else {
        $("#selectCLL").val(null).trigger("change");
      }

      if (node.data.filterFieldInfo) {
        $.get(reportFieldInfoNameUrl + "?id=" + node.data.filterFieldInfo.id, function (data) {
            if (!$("#filterFieldInfo").find('option[value="' + node.data.filterFieldInfo.id + '"]').length) {
              $("#filterFieldInfo").append(new Option(data.text, node.data.filterFieldInfo.id, true, true));
            }
            $("#filterFieldInfo").val(node.data.filterFieldInfo.id).trigger("change");
          }
        );
      } else if (node.data.customSQLFilterFiledInfo) {
        if (!$("#filterFieldInfo").find('option[value="' + node.data.customSQLFilterFiledInfo.id + '"]').length) {
          $("#filterFieldInfo").append(new Option(node.data.customSQLFilterFiledInfo.text, node.data.customSQLFilterFiledInfo.id, false, true));
        }
        $("#filterFieldInfo").val(node.data.customSQLFilterFiledInfo.id).trigger("change");
      } else {
        $("#filterFieldInfo").val(null).trigger("change");
      }
      if (node.data.reportFieldInfo) {
        $.get(reportFieldInfoNameUrl + "?id=" + node.data.reportFieldInfo.id, function (data) {
            if (!$("#reportFieldInfo").find(`option[value="${node.data.reportFieldInfo.id}"]`).length) {
              $("#reportFieldInfo").append(new Option(data.text, data.id ?? node.data.reportFieldInfo.id, false, true));
            }
            $("#reportFieldInfo")
              .val(node.data.reportFieldInfo.id)
              .trigger("change");
          }
        );
      } else if (node.data.customSQLFieldInfo) {
        if (!$("#reportFieldInfo").find(`option[value="${node.data.customSQLFieldInfo.id}"]`).length) {
          $("#reportFieldInfo").append(
            new Option(
              node.data.customSQLFieldInfo.text,
              node.data.customSQLFieldInfo.id,
              true,
              true
            )
          );
        }
        $("#reportFieldInfo")
          .val(node.data.customSQLFieldInfo.id)
          .trigger("change");
      } else {
        $("#reportFieldInfo").val(null).trigger("change");
      }
      $('select[name="dateFormat"]').val(node.data.dateFormat);
      $("#value").val(node.data.value);
      if (node.data.elementType == ELEMENT_TYPE.ATTRIBUTE) {
        $(".tag-only-related").hide();
        $(".attribute-only-related").show();
      } else {
        $(".attribute-only-related").hide();
        $(".tag-only-related").show();
      }
      $(".color-picker-option").html("");
      $("#tagColor").val(node.data.tagColor);
      if (node.parent.title != "root") {
        $(".color-picker-option").html(
          '<input type="color" id="colorPicker" data-evt-change=\'{"method": "changeColor", "params": []}\' name="color" value="' +
            node.data.tagColor +
            '">'
        );
      }
      viewOnly = $("#editable").val() === "false";
      if (!viewOnly) {
        $(".fancytree-node")
          .find(".alreadyExist")
          .replaceWith(function () {
            return this.innerHTML;
          });
        $(".fancytree-node")
          .find(".removeIcon")
          .replaceWith(function () {
            return this.innerHTML;
          });
        $(".fancytree-active")
          .last()
          .append(
            "<span data-evt-clk=\'{\"method\": \"deleteTag\", \"params\": []}\' class='pull-right alreadyExist' style='margin-top: 2px; margin-right: 3px;'><i class='fa fa-remove removeIcon' style='cursor:pointer;'></i></span>"
          );
      }
      if (node.data.reportFieldInfo) {
        $("#staticValuePanel").removeClass("active");
        $("#sourceFieldPanel").addClass("active");
        $(".sourceFieldDiv").attr("data-target", "#sourceFieldPanel");
      } else if (node.data.value) {
        $("#sourceFieldPanel").removeClass("active");
        $("#staticValuePanel").addClass("active");
        $(".sourceFieldDiv").attr("data-target", "#staticValuePanel");
      }
      $("#SOURCE_FIELD").trigger("change");
    },
    dnd: {
      focusOnClick: false,
      dragStart: function (node, data) {
        // Prevent to drag root mode
        if (!node.hasChildren()) {
          return false;
        }
        return !viewOnly && node.parent.parent !== null;
      },
      dragEnter: function (node, data) {
        // Prevent to drop root mode
        return (
          !viewOnly &&
          node.parent.parent !== null &&
          data.node.data.elementType == ELEMENT_TYPE.TAG
        );
      },
      dragDrop: function (node, data) {
        data.otherNode.moveTo(node, data.hitMode);
      },
    },
    icon: function (event, data) {
      if (data.node.data.elementType == ELEMENT_TYPE.ATTRIBUTE) {
        return "glyphicon glyphicon-asterisk";
      }
      return "glyphicon glyphicon-tag";
    },
    glyph: glyph_opts,
    quicksearch: true,
    selectMode: 1,
    source: JSON.parse("[" + $("#rootNode").val() + "]"),
    expand: function (event, data) {
      data.node.setActive(true);
    },
  });

  var tree = $.ui.fancytree.getTree("#tagsTree");
  if (tree.getFirstChild()) {
    tree.getFirstChild().setActive(true);
  }

  if (!$("#rootNode").val()) {
    tree.getActiveNode().remove();
    $("#tName").val("");
  }

  $("#addTag").on("click", function () {
    var tree = $.ui.fancytree.getTree("#tagsTree");
    var activeNode = tree.getActiveNode();
    var randomColor = "#000000".replace(/0/g, function () {
      return (~~(Math.random() * 16)).toString(16);
    });
    if (!activeNode) {
      activeNode = tree.getFirstChild();
    }
    if (!activeNode) {
      activeNode = tree.getRootNode();
    }
    if (activeNode.data.elementType == ELEMENT_TYPE.ATTRIBUTE) {
      activeNode = activeNode.parent;
    }
    //alert(activeNode.parent);
    // Append a new child node
    activeNode.setActive(false);
    var node = activeNode.addChildren({
      title: "new_tag",
      key: getNewID(),
      data: {
        elementType: ELEMENT_TYPE.TAG,
        type: "TAG_PROPERTIES",
        orderingNumber: 0,
        tagColor: randomColor,
      },
    });

    activeNode.setExpanded(true, { noEvents: true });
    node.setActive(true);
    if (activeNode.parent != null) {
      if (activeNode.parent.title != "root") {
        var tagColor = node.parent.data.tagColor;
        $(".fancytree-active")
          .find(".fancytree-custom-icon")
          .css("color", tagColor);
        node.data.tagColor = tagColor;
      } else {
        $(".fancytree-active")
          .find(".fancytree-custom-icon")
          .css("color", randomColor);
        node.data.tagColor = randomColor;
      }
      $(".color-picker-option").html("");
      $(".color-picker-option").html(
        '<input type="color" id="colorPicker" data-evt-change=\'{"method": "changeColor", "params": []}\' name="color" value="' +
          node.data.tagColor +
          '">'
      );
    }
  });

  $("#duplicateTag").on("click", function () {
    var tree = $.ui.fancytree.getTree("#tagsTree");
    var activeNode = tree.getActiveNode();
    if (activeNode) {
      var node = activeNode.copyTo(activeNode, "after");
      node.key = getNewID();
      node.setActive(true);
    }
  });

  $('input[name="tagType"]').on("click", function () {
    //jQuery handles UI toggling correctly when we apply "data-target" attributes and call .tab('show')
    //on the <li> elements' immediate children, e.g the <label> elements:
    //$($(".sourceFieldDiv").attr("data-target")).hide();
    showTab($(this));
  });

  $(".pencilOptionSelectedInTag").on("click", function () {
    $("#sourceFieldPanel").toggleClass("active");
    $("#staticValuePanel").toggleClass("active");
    if ($("#sourceFieldPanel").hasClass("active")) {
      $(".sourceFieldDiv").attr("data-target", "#sourceFieldPanel");
    } else if ($("#staticValuePanel").hasClass("active")) {
      $(".sourceFieldDiv").attr("data-target", "#staticValuePanel");
    }

    $("#value").val("").trigger("change");
    $("#reportFieldInfo").val(null).trigger("change");
  });

  function showTab(radioSelector) {
    radioSelector.closest("div").tab("show");
  }

    function checkTitleIsNull(obj) {
        if (obj.title == undefined) {
            return true;
        }

        if (obj.children && obj.children.length > 0) {
            return obj.children.some(function(child) {
                return checkTitleIsNull(child);
            });
        }

        return false;
    }

  $("#templateForm").on("submit", function (event) {
    var templateForm = $(this);
    var tagsTree = templateForm.find("#tagsTree").fancytree("getTree");
    var tagsTreeData = tagsTree.toDict();
    var templateName = templateForm.find("#name").val();
    var e2bLocale = templateForm.find('#e2bLocale').val();
        var e2bLocaleName = templateForm.find('#e2bLocaleElementName').val();
        var errors = [];
    if (!templateName) {
      errors.push($.i18n._("template.name.label.nullable"));
    }
    if (!tagsTreeData) {
      errors.push($.i18n._("template.xml.rootNode.tag.nullable"));
    } else {
        hasNullTitle = tagsTreeData.some(function(item) { return checkTitleIsNull(item); });
    }
    if(tagsTreeData && hasNullTitle){
      errors.push($.i18n._('tagName.xml.rootNode.tag.nullable'));
        }
        if (e2bLocale && !e2bLocaleName) {
            errors.push($.i18n._('e2b.element.name.locale.required'));
    }
    if (!isEmpty(errors)) {
      errorNotification(errors.join("<br>"));
      event.preventDefault();
    } else {
      $("#rootNode").val(JSON.stringify(tagsTreeData[0]));
    }
  });

  $("#SOURCE_FIELD, #TAG_PROPERTIES").on("change", function (event) {
    if ($("#SOURCE_FIELD").is(":checked")) {
      $(".forField").show();
      $("#e2bElement").css("display", "initial");
    } else {
      $(".forField").hide();
    }
  });

  $("#e2bElement").on("change", function (event) {
    var tree = $.ui.fancytree.getTree("#tagsTree");
    if (tree.activeNode) {
      tree.activeNode.data.e2bElement = $("#e2bElement").val();
    }
  });

  $("#e2bElementName").on("change", function (event) {
    var tree = $.ui.fancytree.getTree("#tagsTree");
    if (tree.activeNode) {
      tree.activeNode.data.e2bElementName = $("#e2bElementName").val();
      $("#e2bElementName").setAttribute("title", $("#e2bElementName").val());
    }
  });

  $("#e2bLocaleElementName").change(function (event) {
    var tree = $('#tagsTree').fancytree("getTree");
    if (tree.activeNode) {
      tree.activeNode.data.e2bLocaleElementName = $("#e2bLocaleElementName").val();
      $("#e2bLocaleElementName").setAttribute('title', $("#e2bLocaleElementName").val());
    }
  });

  $('#e2bLocale').on('change', function (event) {
    var tree = $('#tagsTree').fancytree("getTree");
    if (tree.activeNode) {
      tree.activeNode.data.e2bLocale = $(this).val()
    }
    if ($(this).val()) {
      $("#e2bLocaleElementName").attr('disabled', false);
    } else {
      $("#e2bLocaleElementName").val("").attr('disabled', true);
    }
  });

    $("#tName").on("change", function (event) {
    var tree = $.ui.fancytree.getTree("#tagsTree");
    if (tree.activeNode) {
      tree.activeNode.setTitle(encodeToHTML($(this).val()));
      $(".fancytree-active")
        .find(".fancytree-custom-icon")
        .css("color", $("#colorPicker").val());
      tree.activeNode.data.tagColor = $("#colorPicker").val();
    }
    viewOnly = $("#editable").val() === "false";
    if (!viewOnly) {
      $(".fancytree-node")
        .find(".alreadyExist")
        .replaceWith(function () {
          return this.innerHTML;
        });
      $(".fancytree-node")
        .find(".removeIcon")
        .replaceWith(function () {
          return this.innerHTML;
        });
      $(".fancytree-active")
        .last()
        .append(
          "<span data-evt-clk=\'{\"method\": \"deleteTag\", \"params\": []}\' class='pull-right alreadyExist' style='margin-top: 2px; margin-right: 3px;'><i class='fa fa-remove removeIcon' style='cursor:pointer;'></i></span>"
        );
    }
  });

  $("input[name=tagType]").on("change", function (event) {
    var tree = $.ui.fancytree.getTree("#tagsTree");
    if (tree.activeNode && $(this).is(":checked")) {
      tree.activeNode.data.type = $(this).prop("id");
    }
  });

  $(".node-field").on("change", function (event) {
    var tree = $.ui.fancytree.getTree("#tagsTree");
    if (tree.activeNode) {
      tree.activeNode.data[$(this).prop("id")] = $(this).val();
    }
  });

  $('select[name="dateFormat"]').on("change", function (event) {
    var tree = $.ui.fancytree.getTree("#tagsTree");
    if (tree.activeNode) {
      tree.activeNode.data.dateFormat = $(this).val();
    }
  });

  $("#value").on("change", function (event) {
    var tree = $.ui.fancytree.getTree("#tagsTree");
    if (tree.activeNode) {
      tree.activeNode.data.value = $(this).val();
    }
  });

  $(".deleteTag").on("click", function () {
    var tree = $.ui.fancytree.getTree("#tagsTree");
    var activeNode = tree.getActiveNode();
    if (activeNode) {
      var errorMessage = $.i18n._(
        "are.you.sure.you.want.to.delete.selected.tags"
      );
      $("#deleteWarningModal .description").text(errorMessage);
      $("#deleteWarningModal").modal("show");
      $("#deleteButton")
        .off("click")
        .on("click", function () {
          $("#deleteWarningModal").modal("hide");
          activeNode.remove();
          return false;
        });
    }
    return true;
  });

  $("#colorPicker").on("change", function () {
    var colorVal = $("#colorPicker").val();
    $(".fancytree-active")
      .find(".fancytree-custom-icon")
      .css("color", colorVal);
    var tree = $.ui.fancytree.getTree("#tagsTree");
    if (tree.activeNode) {
      tree.activeNode.setColor(colorVal);
    }
  });
  console.log("Ended Loading Document EditXMLTemplate.js");

  $(document).on('click', '[data-evt-clk]', function(e) {
    e.preventDefault();
    const eventData = JSON.parse($(this).attr("data-evt-clk"));
    const methodName = eventData.method;
    const params = eventData.params;
    // Call the method from the eventHandlers object with the params
    if (methodName == 'deleteTag') {
      return deleteTag();
    }
  });

  $(document).on('change', '[data-evt-change]', function() {
    const eventData = JSON.parse($(this).attr("data-evt-change"));
    const methodName = eventData.method;
    const params = eventData.params;
    // Call the method from the eventHandlers object with the params
    if (methodName == 'changeColor') {
      changeColor();
    }
  });

});

var getNewID = function () {
  // Math.random should be unique because of its seeding algorithm.
  // Convert it to base 36 (numbers + letters), and grab the first 9 characters
  // after the decimal.
  return "_" + Math.random().toString(36).substr(2, 9);
};

var bindreportFieldInfoSelect2 = function (selector, activeNodeOnly) {
  var reportFieldInfoUrl = function () {
    var tree = $.ui.fancytree.getTree("#tagsTree");
    var params = { templateId: [] };
    if (tree.activeNode) {
      var node = tree.activeNode;
      if (activeNodeOnly) {
        if (node.data.templateId) {
          params.templateId.push(node.data.templateId);
        }
      } else {
        while (node) {
          if (node.data.templateId) {
            params.templateId.push(node.data.templateId);
          }
          node = node.parent;
        }
      }
    }
    return reportFieldInfoSearchUrl + "?" + $.param(params, true);
  };
  return bindSelect2WithUrl(
    selector,
    reportFieldInfoUrl,
    reportFieldInfoNameUrl,
    true
  );
};

function deleteTag() {
  var tree = $.ui.fancytree.getTree("#tagsTree");
  var activeNode = tree.getActiveNode();
  if (activeNode) {
    var errorMessage = $.i18n._(
      "are.you.sure.you.want.to.delete.selected.tags"
    );
    $("#deleteWarningModal .description").text(errorMessage);
    $("#deleteWarningModal").modal("show");
    $("#deleteButton")
      .off("click")
      .on("click", function () {
        $("#deleteWarningModal").modal("hide");
        activeNode.remove();
        return false;
      });
  }
  return true;
}

function changeColor() {
  var colorVal = $("#colorPicker").val();
  var id = $(".fancytree-active").parent("li").attr("id");
  $("#" + id)
    .find(".fancytree-custom-icon")
    .css("color", colorVal);
  var tree = $.ui.fancytree.getTree("#tagsTree");
  reloadTagColor(tree.activeNode, colorVal);
}

function reloadTagColor(node, colorVal) {
  if (node.hasChildren()) {
    var listOfChild = node.getChildren();
    for (var i = 0; i < listOfChild.length; i++) {
      node.data.tagColor = colorVal;
      reloadTagColor(listOfChild[i], colorVal);
    }
  } else {
    node.data.tagColor = colorVal;
  }
}
