package interceptor

import groovy.transform.CompileStatic

@CompileStatic
class DictionaryRestInterceptor {

    DictionaryRestInterceptor(){
        match(controller: "studyDictionary", action: '*')
        match(controller: "eventDictionary", action: '*')
        match(controller: "productDictionary", action: '*')
    }

    boolean before() {
        if (!params.currentLang && session['org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE']) {
            params.currentLang = session['org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE']?.toString()
        }
//  TODO      Default Datasource is PVA only
        if (!params.dataSource) {
            params.dataSource = 'pva'
        }
        true
    }

}
