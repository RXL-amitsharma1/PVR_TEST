package com.rxlogix.localization

class HelpLink {
    String link
    String page = "*"

    static final Long DEFAULT_ID = 1

    static mapping = {
        table name: "HELP_LINK"
        link column: "LINK"
        page column: "PAGE"
        id generator: 'assigned'
        cache true
    }

    static constraints = {
        link maxSize: 4000
        page maxSize: 4000
    }

    static String getDefaultHelpLink() {
        return get(DEFAULT_ID)?.link?:"#"
    }

    static void setDefaultHelpLink(String link) {
        HelpLink helpLink = get(DEFAULT_ID)
        if (!helpLink) {
            helpLink = new HelpLink(page: "*")
            helpLink.id = DEFAULT_ID
        }
        helpLink.link = link
        helpLink.save(flush: true, failOnError: true)
    }
}
