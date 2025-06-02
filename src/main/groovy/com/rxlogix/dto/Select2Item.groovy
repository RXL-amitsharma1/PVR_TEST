package com.rxlogix.dto

import groovy.transform.CompileStatic
import groovy.transform.ToString

@CompileStatic
@ToString(includes = ['id', 'text'])
class Select2Item {
    String id
    String text
}
