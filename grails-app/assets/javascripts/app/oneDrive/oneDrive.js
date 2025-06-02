$(function () {
    var parent
    $(document).on("click", ".downloadUrl", function () {
        var $this = $(this);
        showDownloadModal($this.attr("data-id"), $this.attr("data-name"), $this.attr("data-url"), false, $this.attr("data-postParams"), $this.attr("data-uploadOnly"))
    });
    $(document).on("click", ".selectOneDrive", function () {
        showCustomLoader();
        var $this = $(this);
        oneDriveFileSelectMode = false
        showDownloadModal($this.attr("data-id"), $this.attr("data-name"), $this.attr("data-url"), true, $this.attr("data-postParams"), $this.attr("data-uploadOnly"))
    });
    $(document).on("click", ".selectOneDriveFileSelectMode", function () {
        showCustomLoader();
        var $this = $(this);
        oneDriveFileSelectMode = true;
        parent = $this.closest(".attachmentOneDriveDiv");
        showDownloadModal($this.attr("data-id"), $this.attr("data-name"), $this.attr("data-url"), true, $this.attr("data-postParams"), $this.attr("data-uploadOnly"))
    });
    $(document).on("click", ".createOneDriveFolder", function () {
        showCustomLoader();
        $.ajax({
            type: "get",
            url: oneDriveNewFolderUrl,
            data: {
                id: currentFolderId,
                folderName: $(".oneDriveNewFolderName").val(),
                siteId: currentSiteId
            },
            global: false,
            dataType: 'json'
        })
            .done(function (result) {
                var lasIndex = breadCramp.length - 1;
                showOneDriveFolders(breadCramp[lasIndex].id, breadCramp[lasIndex].name, breadCramp[lasIndex].siteId, true);
            })
            .fail(function (err) {
                hideCustomLoader();
                showOneDriveError(err);
            });
    });
    $(document).on("click", ".uploadToOneDrive", function () {
        showCustomLoader();
        $.ajax({
            type: "post",
            url: oneDriveUploadUrl,
            data: {
                id: currentFolderId,
                name: $("#oneDriveUploadAs").val(),
                siteId: currentSiteId,
                url: oneDriveFileUrl,
                postParams: oneDrivePostParams
            },
            global: false,
            dataType: 'json'
        })
            .done(function (result) {
                var $oneDriveFrame = $("#oneDriveFrame");
                $oneDriveFrame.empty();
                $oneDriveFrame.append("<div class=\"alert alert-success\" role=\"alert\">" + $.i18n._('oneDrive.successUpload') + "</div>");
                hideCustomLoader();
            })
            .fail(function (err) {
                hideCustomLoader();
                showOneDriveError(err);
            });
    });

    $(document).on("click", ".selectOneDriveFolder", function () {
        var oneDriveFolderName = "";
        for (i in breadCramp) {
            oneDriveFolderName += "/" + breadCramp[i].name;
        }
        $("#oneDriveFolderName").val(oneDriveFolderName);
        $("#oneDriveFolderId").val(currentFolderId);
        $("#oneDriveSiteId").val(currentSiteId);
        $("#oneDriveUserSettings").val(oneDriveUserSettings);
        $("#downloadModal").modal("hide");
    });

    $(document).on("click", ".selectOneDriveFile", function () {
        var oneDriveFolderName = "";
        for (i in breadCramp) {
            oneDriveFolderName += "/" + breadCramp[i].name;
        }
        var fileName = oneDriveFolderName + "/" + $(this).attr("data-name")
        parent.find("[name=oneDriveFolderName]").val(fileName);
        parent.find("[name=oneDriveFolderId]").val($(this).attr("data-id"));
        parent.find("[name=oneDriveSiteId]").val(currentSiteId);
        parent.find("[name=oneDriveUserSettings]").val(oneDriveUserSettings);
        $("#downloadModal").modal("hide");
    });

    if (oneDriveWopiProvider) {
        $(document).on("click", ".wopiLink", function (e) {
            e.preventDefault();
            var url = $(this).attr("href")
            showLoader();
            $.ajax({
                url: checkOneDriveLoginUrl,
                global: false,
                dataType: 'json'
            })
                .done(function (result) {
                    if (result != "no") {
                        window.open(url, '_blank');
                    } else {
                        wopiRedirectUrl = url;
                        window.open(authUrlWopi + encodeURIComponent(url), '_blank');
                    }
                    hideLoader();
                })
                .fail(function (err) {
                    console.log(err);
                });
        });

        $(document).on("click", ".oneDriveEditorLink", function (e) {
            showLoader();
            var id = $(this).attr("data-id");
            var entity = $(this).attr("data-entity")
            $.ajax({
                url: checkOneDriveLoginUrl,
                global: false,
                dataType: 'json'
            })
                .done(function (result) {
                    if (result != "no") {
                        $.ajax({
                            url: uploadEntityToOnedriveUrl,
                            data: {id: id, entity: entity},
                            global: false,
                            dataType: 'json'
                        })
                            .done(function (result) {
                                $("#lockCode").val(result.lockCode);
                                $('<a />', {'href': result.url, 'target': '_blank'}).get(0).trigger('click');
                                hideLoader();
                            });

                    } else {
                        $('<a />', {
                            'href': authUrlWopi + "id_" + id + "_entity_" + entity,
                            'target': '_blank'
                        }).get(0).trigger('click');
                    }
                    hideLoader();
                })
                .fail(function (err) {
                    console.log(err);
                });
        });
    }
});

function reloadPage() {
    location.reload(true);
}

function onOneDriveAuthenticated(authWindow) {
    authWindow.close();
    showCustomLoader();
    $.ajax({
        url: checkOneDriveLoginUrl,
        global: false,
        dataType: 'json'
    })
        .done(function (result) {
            if (result != "no") {
                initOneDriveModal(result)
            } else {
                showOneDriveError(err);
            }
            hideCustomLoader();
        })
        .fail(function (err) {
            hideCustomLoader();
            showOneDriveError(err);
        });
}

var oneDriveFileSelectMode = false
var oneDriveSelectMode = false;
var oneDriveFileUrl;
var oneDriveFileName;
var currentFolderId;
var currentSiteId;
var oneDriveUserSettings;
var oneDrivePostParams;
var breadCramp = [];
var wopiRedirectUrl
var showDownloadModal = function (id, name, url, select, postParams, uploadOnly) {
    oneDriveSelectMode = select;
    if (oneDriveEnabled || select) {
        showCustomLoader();
        $(".downloadFileRef").attr("href", url);
        $("#downloadModal").modal("show");
        oneDriveFileUrl = url;
        oneDriveFileName = name;
        oneDrivePostParams = postParams;
        if (oneDriveSelectMode) {
            $(".downloadMode").hide();
            $(".selectMode").show();
        } else {
            $(".downloadMode").show();
            $(".selectMode").hide();
            $("#oneDriveUploadAs").val(oneDriveFileName);
            $("#downloadModal").find(".modalHeaderSpan").html("<b><span class=\"fa fa-download\" ></span> " + $.i18n._('oneDrive.file') + oneDriveFileName + "</b>");
            if (uploadOnly) {
                $(".downloadFileRef ").hide();
            } else {
                $(".downloadFileRef ").show();
            }
        }
        if (oneDriveFileSelectMode) {
            $(".selectOneDriveFolder").hide();
        }
        checkOneDriveLoggedIn();
    } else {
        location.href = url;
    }
};

function initOneDriveModal(settings) {
    oneDriveUserSettings = settings;
    $.ajax({
        url: listSitesUrl,
        global: false,
        dataType: 'json'
    })
        .done(function (sites) {
            $(".hideOnLogin").show();
            var html = "<select id='oneDriveSiteSelect' class='form-control'><option value='drive'>" + $.i18n._('oneDrive.root') + "</option>";
            for (var i in sites) {
                html += "<option value='" + sites[i].siteId + "'>" + sites[i].name + "</option>";
            }
            $("#oneDriveSiteList").html(html);
            hideCustomLoader();
            $("#oneDriveSiteSelect").select2().on("change", function (e) {
                var val = $(this).select2('data');
                showOneDriveFolders("root", val.text, val.id, false)
            });
            showOneDriveFolders("root", $.i18n._('oneDrive.root'), "drive", false);
        })
        .fail(function (err) {
            showOneDriveError(err);
        });
}

function checkOneDriveLoggedIn() {
    $.ajax({
        url: checkOneDriveLoginUrl,
        global: false,
        dataType: 'json'
    })
        .done(function (result) {
            if (result != "no") {
                initOneDriveModal(result)
            } else {
                var $oneDriveFrame = $("#oneDriveFrame");
                $oneDriveFrame.empty();
                $(".hideOnLogin").hide();
                $oneDriveFrame.append("<a href=\"javascript:oneDrivePopup(authUrl)\" class=\"btn btn-success btn-xs\"> " + $.i18n._('oneDrive.logIn') + "</a>");
                hideCustomLoader();
            }
        })
        .fail(function (err) {
            hideCustomLoader();
            showOneDriveError(err);
        });
}

function showOneDriveFolders(id, name, siteId, isBreadCramp) {
    showCustomLoader();
    $.ajax({
        url: oneDriveFoldersUrl + "?id=" + id + "&siteId=" + siteId,
        global: false,
        dataType: 'json'
    })
        .done(function (folders) {
            var $oneDriveFrame = $("#oneDriveFrame");
            $oneDriveFrame.empty();
            if (isBreadCramp) {
                var newBreadCramp = [];
                for (var i in breadCramp) {
                    newBreadCramp.push(breadCramp[i]);
                    if (breadCramp[i].id === id) break;
                }
                breadCramp = newBreadCramp
            } else {
                if (id == "root") {
                    breadCramp = []
                }
                breadCramp.push({name: name, id: id, siteId: siteId})
            }
            currentFolderId = id;
            currentSiteId = siteId;
            var html = "<div style='margin-bottom: 5px; margin-top: 5px; font-size: 16px'>";
            html += '<button class="btn btn-xs btn-success"  data-toggle="modal" data-target="#addFolderOneDrive" ><span class="fa fa-plus" title="Create Folder"></span></button>';

            for (i in breadCramp) {
                html += "<a href='javascript:showOneDriveFolders(\"" + breadCramp[i].id + "\",\"" + breadCramp[i].name + "\",\"" + breadCramp[i].siteId + "\",true)'>/ <span class=\"fa fa-folder-open\" ></span>" + breadCramp[i].name + "</a> ";
            }

            html += "</div><div style=\"height: 240px; overflow: auto\">";
            for (i in folders) {
                if (folders[i].folder) {
                    html += "<div style='margin-left: 10px;margin-bottom: 3px'><a href='javascript:showOneDriveFolders(\"" + folders[i].id + "\",\"" + folders[i].name + "\",\"" + folders[i].siteId + "\",false)'><span class=\"fa fa-folder\" ></span> " + folders[i].name + "</a></div>";
                } else {
                    var file = folders[i];
                    var selectBtn = "<span class=\"fa fa-file\" ></span> ";
                    if (oneDriveFileSelectMode) {
                        html += "<div style='margin-left: 10px;margin-bottom: 3px'>" +
                            "<a class=' selectOneDriveFile greenHover'  data-id='" + file.id + "' data-name='" + file.name + "' data-id='" + file.id + "'><span class=\"fa fa-file\" ></span> " +
                            file.name + "</a></div>"
                    } else {
                        html += "<div style='margin-left: 10px;margin-bottom: 3px'><span class=\"fa fa-file\" ></span> " + file.name + "</div>"
                    }
                }
            }
            html += "</div>";

            $oneDriveFrame.html(html);
            hideCustomLoader();
        })
        .fail(function (err) {
            hideCustomLoader();
            showOneDriveError(err);
        });
}

function showOneDriveError(err) {
    if (err.status == 401) {
        var $oneDriveFrame = $("#oneDriveFrame");
        $oneDriveFrame.empty();
        $oneDriveFrame.append("<a href=\"javascript:oneDrivePopup(authUrl)\" class=\"btn btn-success btn-xs\"> " + $.i18n._('oneDrive.logIn') + "</a>");
    } else {
        var $oneDriveFrame = $("#oneDriveFrame");
        $oneDriveFrame.empty();
        $oneDriveFrame.append("<div class=\"alert alert-success\" role=\"alert\">" + $.i18n._('oneDrive.serverError') + err.statusText + "</div>");
    }
}

function oneDrivePopup(url) {
    var width = 525,
        height = 525,
        screenX = window.screenX,
        screenY = window.screenY,
        outerWidth = window.outerWidth,
        outerHeight = window.outerHeight;

    var left = screenX + Math.max(outerWidth - width, 0) / 2;
    var top = screenY + Math.max(outerHeight - height, 0) / 2;

    var features = [
        "width=" + width,
        "height=" + height,
        "top=" + top,
        "left=" + left,
        "status=no",
        "resizable=yes",
        "toolbar=no",
        "menubar=no",
        "scrollbars=yes"];
    var popup = window.open(url, "oauth", features.join(","));
    if (!popup) {
        alert("failed to pop up auth window");
    }

    popup.focus();
}

function showCustomLoader() {
    $('.custom-loader').show();
    $('.uploadToOneDrive').attr("disabled", true);
}

function hideCustomLoader() {
    $('.custom-loader').hide();
    $('.uploadToOneDrive').attr("disabled", false);
}