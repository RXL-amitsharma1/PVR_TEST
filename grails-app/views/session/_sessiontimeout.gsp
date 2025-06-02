<g:set var="reminderTime"
       value="${grailsApplication.config.getProperty('session.timeout.alert.reminder.time', Integer, 5)}"/>
<g:set var="appBaseUrl" value="${grailsApplication.config.getProperty('grails.appBaseURL')}"/>
<g:set var="logoutUri"
       value="${grailsApplication.config.getProperty('grails.plugin.springsecurity.saml.active', Boolean) ? grailsApplication.config.getProperty('grails.plugin.springsecurity.saml.logout.uri') : grailsApplication.config.getProperty('grails.plugin.springsecurity.logout.uri')}"/>
<g:set var="keepAliveInterval" value="${(session.maxInactiveInterval % 3) * 60}"/>
<asset:javascript src="vendorUi/store.modern.min.js"/>
<asset:javascript src="vendorUi/jquery/customise-jquery-idleTimeout.js"/> %{--Added customised version of jquery-idleTimeout.js--}%
<g:javascript>
    $(function() {
      $(document).idleTimeout({
      redirectUrl:  "${appBaseUrl}/${logoutUri}",
      idleTimeLimit: ${session.maxInactiveInterval}-${reminderTime * 60},
      sessionKeepAliveUrl: "${appBaseUrl}/keep-alive",
      sessionKeepAliveTimer: ${keepAliveInterval ?: 60},
      enableDialog: true,
      dialogDisplayLimit: ${reminderTime * 60},
      activityEvents: 'click keypress',
      customCallback: function() {
          var forms = $('form');
            if(forms.find('.changed-input').length){
                alert($.i18n._('navigateAwayChangesLostMessage'));
                clearFormInputsChangeFlag(forms);
            }
      }
      });
    });
</g:javascript>
