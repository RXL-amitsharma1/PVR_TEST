package com.rxlogix.dto

import groovy.transform.CompileStatic
import groovy.transform.ToString;

@CompileStatic
@ToString(includes = ['status', 'message'])
public class ApiResponseDTO {
    String message
    int status
    List<String> errors
}
