package interceptor
/*
TODO :: This Interceptor is used for flash message, as currently it is supported while redirecting in grails 6.2
Afterward we need to look into this in details
 */
class FlashInterceptor {

    FlashInterceptor() {
        matchAll().excludes(controller:"login")
    }


    boolean before() {
        try {
            def flashRedirect = session.getAttribute('request-redirect')
            if (flashRedirect) {
                session.setAttribute('request-redirect', false)
                def flashScope = session.getAttribute('org.grails.FLASH_SCOPE')
                session.setAttribute('org.grails.FLASH_SCOPE', flashScope)
            }
        } catch (Exception ignored) {}
        true
    }

    boolean after() {
        int status = response.status
        if (status == 302) {
            session.setAttribute('request-redirect', true)
            def flashScope = session.getAttribute('org.grails.FLASH_SCOPE')
            session.setAttribute('org.grails.FLASH_SCOPE', flashScope)
        }
        true
    }

}
