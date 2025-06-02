<html>
    <head>
        <title><g:message code="app.login.title"/></title>
        <link rel="shortcut icon" href="${assetPath(src: 'favicon.ico')}" type="image/x-icon">
        <asset:stylesheet href="login.css"/>
        <style>
            body{
                font-size: 14px;
            }

            footer.page-footer .footer-copyright {
                overflow: hidden;
                color: rgba(255,255,255,.6);
                background-color: rgba(0,0,0,.2);
            }

            footer.page-footer {
                position: absolute;
                bottom: 0;
                width: 100%;
                text-align: center;
            }

            .alert{
                padding:15px 35px 15px 15px !important;
                background: rgba(255,255,255,0.2);
            }
            .alert-warning {
                background-color: rgba(255, 170, 0, 0.2) !important;
                border-color: rgba(255, 170, 0, 0.5) !important;
                color: #ffaa00 !important;
            }
            .alert-dismissible button.close {
                margin: -18px -28px;
                color: inherit;
                padding: 0;
                cursor: pointer;
                background: 0 0;
                border: 0;
                float: right;
                font-size: 21px;
                text-shadow: 0 1px 0 #fff;
                filter: alpha(opacity = 20);
                opacity: .2;
            }
            a.policy-link {
                color: rgba(255,255,255,.6);
            }

            a.policy-link:hover {
                color: white;
            }
            button#loginSubmit, input{
                outline:none;
            }
            .loginFieldUser{
                margin-top:32px !important;
            }
            .loginFieldPass{
                margin-top:12px !important;
            }
        </style>
        <asset:stylesheet href="vendorUi/bootstrap/bootstrap.min.css"/>
        <asset:stylesheet href="fontAwesome/font-awesome.css"/>
        <asset:stylesheet href="cookiecuttr.css"/>
        <asset:javascript src="vendorUi/jquery/jquery-3.7.1.min.js"/>
        <asset:javascript src="/vendorUi/jquery/jquery.cookie-1.4.1.min.js"/>
        <asset:javascript src="/vendorUi/jquery/customise-jquery.cookiecuttr.js"/>
        <meta name="instance-name" content="${g.renderInstanceName(roles:"ROLE_DEV")}"/>
        <meta http-equiv="refresh" content="${sessionTimeOutInterval}" />
    </head>

    <body>
        <div class="container">

            <div id="loginStyle">
                <form action='${postUrl ?: '/login/authenticate'}' method='POST' id="loginForm" role="form" autocomplete="off">
                    <div id="loginTable">
                        <div id="loginHeader">
                            <div>
                                <asset:image src="pv_reports_logo.png" id="loginLogo" />
                            </div>
                            <div>
                                <label id="loginLabel"><g:message code="log.in" /></label>
                            </div>
                        </div>

                        <div id="loginUserContainer">
                            <input type="text" name="${usernameParameter ?: 'username'}" id="username" class="loginFieldUser" autocomplete="off" placeholder="${message(code: 'user.username.label')}" required autofocus>
                        </div>

                        <div id="loginPassContainer">
                            <input type="password" name="${passwordParameter ?: 'password'}" id="password" class="loginFieldPass" autocomplete="off" placeholder="${message(code: 'user.password.label')}" required>
                        </div>

                        <div id="loginFooter">
                            <button id="loginSubmit" type="submit"><g:message code="log.in" /></button>
                        </div>

                        <rx:isCsrfProtectionEnabled>
                            <input type="hidden" name="${_csrf?.parameterName}" value="${_csrf?.token}"/>
                        </rx:isCsrfProtectionEnabled>
                    </div>

                    <div class="text-center">
                        <div class="login_message text-white"><em style="font-style: inherit"><g:message
                                code="app.restricted.use.message"/></em></div>
                    </div>

                    <div class="margin20Top">
                        <g:render template="/includes/layout/flashErrorsDivs"/>
                    </div>

                </form>
            </div>
        </div>
    <!-- Footer -->
    <footer class="page-footer font-small pt-4">
        <!-- Copyright -->
        <div class="footer-copyright text-center py-3"><g:message code="app.copyright.message"
                                                                  args="[new Date().format('yyyy')]"/> <rx:renderSecurityPolicyLink class="policy-link" target="_blank"/>
        </div>
        <!-- Copyright -->
    </footer>
    <script type='text/javascript'>
        <!--
        (function () {
            $.cookieCuttr({
                cookieAnalyticsMessage: '${message(code: 'app.cookies.message')}',
                cookieAcceptButtonText: '${message(code: 'app.cookies.accept.button')}',
                secure: ${!!grails.util.Holders.config.pvreports.cookie.secured}
            });
            document.forms['loginForm'].elements['${usernameParameter ?: 'username'}'].focus();
            sessionStorage.removeItem('theme');
            sessionStorage.removeItem("module");
            localStorage.removeItem("module");
        })();
        // -->
    </script>
    <asset:javascript src="vendorUi/bootstrap/bootstrap.min.js"/>
    </body>
</html>
