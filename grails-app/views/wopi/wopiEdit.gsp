<%--
  Created by IntelliJ IDEA.
  User: sergey
  Date: 01.10.2020
  Time: 14:58
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title></title>
</head>

<body>
<script>
    console.log(window.parent.location);
    console.log(parent.location);
    console.log(parent.window.location);
   // window.opener.reloadPage();
    location.href='${raw(url)}';
</script>
</body>
</html>