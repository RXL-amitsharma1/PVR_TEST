package com.rxlogix.commandObjects

import com.rxlogix.config.Comment
import com.rxlogix.enums.CommentTypeEnum
import grails.validation.Validateable

class CommentCommand implements Validateable{
    Comment comment
    CommentTypeEnum commentType;
    Long ownerId
    String multipleIds

    static constraints = {
        ownerId nullable:false
        commentType nullable: false, blank:false
        multipleIds nullable: true
    }
}
