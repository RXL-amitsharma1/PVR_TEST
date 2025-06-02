<%--
  Created by IntelliJ IDEA.
  User: khovrachev
  Date: 17.10.2019
  Time: 11:50
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title></title>
</head>

<body>
<g:if test="${params.state?.startsWith('wopi_id')}">
    <script>
        $.ajax({
            url: uploadEntityToOnedriveUrl,
            dataType: 'json',
            data: {id: $(this).attr("id"), entity: $(this).attr("entity")},
            global: false
        })
            .done(function (result) {
                $("#lockCode", window.opener.document).val(result.lockCode);
                location.href = result.url;
            });
    </script>
</g:if>
<g:elseif test="${params.state?.startsWith('wopi_')}">
    <script>
        window.opener.reloadPage();
        location.href = decodeURIComponent('${raw(params.state.split("_")[1])}');
    </script>
</g:elseif>
<g:else>
    <script>
        window.opener.onOneDriveAuthenticated(window);
    </script>
</g:else>
</body>
</html>