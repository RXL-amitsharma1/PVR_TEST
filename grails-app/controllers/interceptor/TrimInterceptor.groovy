package interceptor


class TrimInterceptor {

    TrimInterceptor(){
        match(controller: "configuration")
    }

    boolean before() {
        if (params && params['reportName']){
                params['reportName'] = params['reportName'].toString().trim()
        }
        return true
    }

    boolean after() { true }

    void afterView() {
        // no-op
    }
}
