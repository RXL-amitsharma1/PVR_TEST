package com.rxlogix.config.publisher

import com.rxlogix.CustomMessageService
import com.rxlogix.hibernate.EscapedILikeExpression
import com.rxlogix.util.MiscUtil

class PublisherTemplateParameter {
    String name
    String title
    String description
    String value
    Type type
    boolean hidden = false

    static belongsTo = [template: PublisherTemplate]

    static mapping = {
        table('publisher_tpl_prm')
    }

    static constraints = {
        name(validator: {val, obj ->
            if(val){
                String regEx = "^[\\w_]+\$";
                if(!val.matches(regEx)){
                    return "invalid.special.characters"
                }
            }
        })
        value(validator: { val, obj ->
            if (obj.type == Type.TEXT) {
                if (val && val.length() > 4000) {
                    return ['maxSize.exceeded', obj.name, '4000']
                }
            }
            if (obj.type == Type.QUESTIONNAIRE) {
                if (val && val.length() > 32000) {
                    return ['questionnaire.maxSize.exceeded', obj.name, '32000']
                }
            }
        })

        title(validator: { val, obj ->
            if (val && val.length() > 4000)
                return ['maxSize.exceeded', obj.name, '4000']
        })

        description(validator: { val, obj ->
            if (val && val.length() > 4000)
                return ['maxSize.exceeded', obj.name, '4000']
        })
        //todo add validator
//        name(validator: {val, obj ->
//
//            if (!obj.id || obj.isDirty("name")) {
//                long count = PublisherTemplateParameter.createCriteria().count{
//                    ilike('name', "${val}")
//                    eq('type', obj.type)
//                    eq('template', obj.template)
//                    if (obj.id){ne('id', obj.id)}
//                }
//                if (count) {
//                    return "com.rxlogix.config.publisher.publisherTemplateParameter.name.unique.per.template"
//                }
//            }
//        })
        description(nullable: true)
        value(nullable: true)

    }

    static namedQueries = {
        publisherTemplateParameterByTemplateAndSearchString { PublisherTemplate template, String search ->
            eq('template', template)
            if (search) {
                or {
                    iLikeWithEscape('name', "%${EscapedILikeExpression.escapeString(search)}%")
                    iLikeWithEscape('title', "%${EscapedILikeExpression.escapeString(search)}%")
                    iLikeWithEscape('description', "%${EscapedILikeExpression.escapeString(search)}%")
                    iLikeWithEscape('value', "%${EscapedILikeExpression.escapeString(search)}%")
                    Type.searchBy(search)?.each {
                        eq('type', it)
                    }
                }
            }
        }
    }

    public enum Type {
        TEXT,
        CODE,
        QUESTIONNAIRE


        public static List<Type> searchBy(String search) {
            if (!search) {
                return []
            }
            search = search.toLowerCase()
            CustomMessageService customMessageService = MiscUtil.getBean("customMessageService")
            values().findAll{customMessageService.getMessage("app.PublisherTemplateParameter.Type." + it.name()).toLowerCase().contains(search) }
        }
    }
}
