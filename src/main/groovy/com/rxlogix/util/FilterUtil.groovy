package com.rxlogix.util

import com.rxlogix.enums.TemplateTypeEnum
import com.rxlogix.user.Preference
import com.rxlogix.user.User
import grails.util.Holders
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

import java.lang.reflect.Field
import java.util.regex.Matcher
import java.util.regex.Pattern

class FilterUtil {
    static Object convertToJsonFilter(String jsonFilterString) {
        if (jsonFilterString) {
            JsonSlurper jsonSlurper = new JsonSlurper()
            jsonSlurper.parseText(jsonFilterString)
        } else
            return null
    }

    static List<Closure> buildCriteriaForColumnFilter(Object filterJson, User user) {
        List<Closure> ca = []
        filterJson.each { key, valueJson ->
            def type = valueJson.type
            switch (type) {
                case 'text':
                    def name = key
                    def value = valueJson.val
                    if (name && value) {
                        ca.add {
                            ilike(name, "%${value}%")
                        }
                    }
                    break
                case 'number':
                    def name = key
                    def value = null
                    if (name == "dueInDays") {
                        value = valueJson.val as Integer
                    } else {
                        value = valueJson.val as Long
                    }
                    if (name && value) {
                        ca.add {
                            eq(name, value)
                        }
                    }
                    break
                case 'date':
                    def name = key
                    def value = null
                    try {
                        value = DateUtil.parseDateWithTimeZone(valueJson.val as String, DateUtil.DATEPICKER_FORMAT, user.preference.timeZone)
                    } catch (Exception e) {
                        value = DateUtil.parseDateWithTimeZone("00-Jan-0001" as String, DateUtil.DATEPICKER_FORMAT, user.preference.timeZone)
                    }
                    if (name && value) {
                        ca.add {
                            between(name, value, value + 1)
                        }
                    }
                    break
            }
        }
        return ca
    }

    static List<Closure> buildCriteria(Object filterJson, Class clz, Preference userPreference) {
        List<Closure> ca = []
        List ignoreTimeZoneFields = ["dueDate"]

        filterJson.each { key, jsonValue ->
            def type = jsonValue.type

            if (FilterTranslatableUtil.isFilterTranslatable(key as String)) {
                ca.addAll(FilterTranslatableUtil.filterByTranslatable(key as String, type as String, jsonValue.value as String))
                return
            }

            Field fld = getFieldType(clz, key as String)

            Class fieldType = null
            if (fld) {
                fieldType = fld.getType()
            }

            switch (type) {
                case 'text':
                    ca = filterByText(key, jsonValue.value, ca)
                    break
                case 'multi-value':
                    ca = filterByMultiValue(key, jsonValue.value, ca)
                    break
                case 'multi-value-number':
                    ca = filterByMultiValueNumber(key, jsonValue.value, ca)
                    break
                case 'multi-value-text':
                    ca = filterByMultiValueText(key, jsonValue.value, ca)
                    break
                case 'id':
                    ca = filterById(key, jsonValue.value as Long, ca)
                    break
                case 'enum':
                    ca = filterByEnum(key, jsonValue.value, jsonValue.dataType, ca)
                    break
                case 'multi-value-id':
                    List<Long> value = jsonValue.value.collect { it as Long }
                    ca = filterByMultiValueId(key, value, ca)
                    break
                case 'value':
                    ca = filterByValue(key, jsonValue.value, fieldType, ca)
                    break
                case 'range':
                    ca = filterByRange(key, jsonValue.value1, jsonValue.value2, ignoreTimeZoneFields, userPreference, ca)
                    break
                case 'number-range':
                    ca = filterByNumberRange(key, jsonValue.value1 as Long, jsonValue.value2 as Long, ca)
                    break
            }
        }

        return ca
    }

    def static convertToNumber(String theValue, Class fieldType) {
        if (fieldType) {
            if (fieldType == Double.class || fieldType == double.class) {
                theValue.toDouble()
            } else if (fieldType == Integer || fieldType == int.class) {
                theValue.toInteger()
            } else if (fieldType == Long || fieldType == long.class) {
                theValue.toLong()
            } else if (fieldType == Float || fieldType == float.class) {
                theValue.toFloat()
            } else if (fieldType == BigInteger) {
                theValue.toBigInteger()
            } else if (fieldType == BigDecimal) {
                theValue.toBigDecimal()
            } else if (fieldType == Boolean || fieldType == boolean.class) {
                theValue.toBoolean()
            } else
                theValue
        } else {
            if (theValue.isDouble()) {
                theValue.toDouble()
            } else if (theValue.isFloat()) {
                theValue.toFloat()
            } else if (theValue.isInteger()) {
                theValue.toInteger()
            } else if (theValue.isBigInteger()) {
                theValue.toBigInteger()
            } else if (theValue.isLong()) {
                theValue.toLong()
            } else if (theValue.isBigDecimal()) {
                theValue.toBigDecimal()
            } else if (theValue.equalsIgnoreCase("true") || theValue.equalsIgnoreCase("false")) {
                Boolean.parseBoolean(theValue)
            } else
                theValue
        }
    }

    static Field getFieldType(Class clz, String fieldName) {
        if (clz.getDeclaredFields().find { it.getName() == fieldName }) {
            clz.getDeclaredField(fieldName)
        } else {
            Class parentClz = clz.getSuperclass()
            if (parentClz) {
                getFieldType(parentClz, fieldName)
            } else {
                null
            }
        }
    }

    def static valueMatching(String text) {
        def returnVal = null
        if (text) {
            def matchingMap = [
                    "^(>=)(\\s*)(.+)"                 : "ge",
                    "^(<=)(\\s*)(.+)"                 : "le",
                    "^(>)(\\s*)(.+)"                  : 'gt',
                    "^(<)(\\s*)(.+)"                  : 'lt',
                    "^(between)(\\s*)(\\S+)(\\s+)(.+)": "between"
            ]

            matchingMap.find { pattern, v ->
                Pattern r = Pattern.compile(pattern)
                Matcher m = r.matcher(text)

                if (m.matches()) {
                    def op = v
                    def val = m.group(3)

                    if (op == 'between') {
                        def val2 = m.group(5)

                        returnVal = [op, val, val2]
                    } else {
                        returnVal = [op, val]
                    }
                    return true
                }
                return false
            }
        }

        returnVal
    }

    static String buildEnumOptions(Class enumClz) {
        def builder = new JsonBuilder()
        def values = enumClz.values().collect { enumInst ->
            [key: enumInst.getKey(), value: ViewHelper.getMessage(enumInst.getI18nKey())]
        }

        builder values as List
        builder.toString()
    }

    //TODO: Remove this method and usages when XML templates are properly integrated. Please refer ticket: PVR-7866 pull request #3629
    static String buildTemplateTypeEnumOptions(Class enumClz) {
        def xmlOption = Holders.config.getProperty('show.xml.option', Boolean)
        def builder = new groovy.json.JsonBuilder()
        def values = enumClz.values().findAll { enumValue ->
            if (!xmlOption)
                return enumValue != TemplateTypeEnum.ICSR_XML
            else
                return true
        }.collect { enumInst ->
            [key: enumInst.getKey(), value: ViewHelper.getMessage(enumInst.getI18nKey())]
        }

        builder values as List
        builder.toString()
    }

    /* This method can accept both String and String List.
    If String comes then it splits the String across ; and converts it into a String List
    Else itv retuns the String List as is
    */
    static List<String> parseParamList(def filterValues) {
        return filterValues instanceof String[] ? filterValues : filterValues?.split(";")?.findAll { it }
    }

    static List<Closure> filterByText(def name, def value, List<Closure> ca) {
        if (!name || !value) {
            return ca
        }
        if (name != 'email') {
            ca.add {
                (name == 'profileName') ? ilike(name, "$value") : ilike(name, "%$value%")
            }
            return ca
        }
        ca.add {
            or {
                value.split(",")*.trim().findAll { email ->
                    or {
                        iLikeWithEscape('emails.elements', email)
                        iLikeWithEscape('emc.cc', "%$email%")
                    }
                }
            }
        }

        return ca
    }

    static List<Closure> filterByMultiValueText(def name, def value, List<Closure> ca) {
        if (!name || !value) {
            return ca
        }
        ca.add {
            or {
                value.split(",")*.trim().findAll { val ->
                    or {
                        ilike(name, "%$val%")
                    }
                }
            }
        }

        return ca
    }

    static List<Closure> filterById(def name, Long value, List<Closure> ca){
        if (!name || !value) {
            return ca
        }
        ca.add {
            eq("${name}.id", value)
        }
        return ca
    }

    static List<Closure> filterByEnum(def name, def value, def enumType, List<Closure> ca) {
        if (!name || !value) {
            return ca
        }
        if (enumType == "String")
            ca.add {
                eq(name, value)
            }
        else
            ca.add {
                eq(name, Class.forName("com.rxlogix.enums.$enumType").valueOf(value))
            }
        return ca
    }

    static List<Closure> filterByMultiValueId(def name, List<Long> value, List<Closure> ca) {
        if (!name || !value) {
            return ca
        }
        ca.add {
            'in'("${name}.id", value)
        }
        return ca
    }

    static List<Closure> filterByMultiValue(def name, List value, List<Closure> ca) {
        if (!name || !value) {
            return ca
        }
        ca.add {
            'in'("${name}", value)
        }
        return ca
    }

    static List<Closure> filterByMultiValueNumber(def name, List value, List<Closure> ca) {
        if (!name || !value) {
            return ca
        }
        List<Long> valueList = value.collect { Long.parseLong(it.toString()) }
        ca.add {
            'in'("${name}", valueList)
        }
        return ca
    }

    static List<Closure> filterByValue(def name, String value, def fieldType, List<Closure> ca) {
        if (!name || !value) {
            return ca
        }
        def rtVal = valueMatching(value)
        if (rtVal) {
            if (rtVal[0] == 'between') {
                def v1 = convertToNumber(rtVal[1] as String, fieldType)
                def v2 = convertToNumber(rtVal[2] as String, fieldType)
                ca.add {
                    between(name, v1, v2)
                }
            } else {
                ca.add {
                    "${rtVal[0]}"(name, convertToNumber(rtVal[1] as String, fieldType))
                }
            }
        } else {
            def bv = convertToNumber(value, fieldType)
            if(name == 'isDisabled') {
                name = 'isEnabled'
                bv = !bv
            }
            ca.add {
                eq(name, bv)
            }
        }
        return ca
    }

    static List<Closure> filterByRange(def name, def value1, def value2, List ignoreTimeZoneFields, Preference userPreference, List<Closure> ca) {
        boolean ignoreTimeZone = name in ignoreTimeZoneFields
        Date dt1
        Date dt2
        String regexForJpLocale = /^\d{4}\/\d{2}\/\d{2}$/

        if (value1)
            dt1 = ignoreTimeZone ? DateUtil.parseDate(value1 as String, DateUtil.DATEPICKER_FORMAT) : DateUtil.parseDateWithTimeZone(value1 as String, (value1 as String)?.matches(regexForJpLocale)? DateUtil.DATEPICKER_JFORMAT : DateUtil.DATEPICKER_FORMAT, userPreference.timeZone)
        if (value2)
            dt2 = (ignoreTimeZone ? DateUtil.parseDate(value2 as String, DateUtil.DATEPICKER_FORMAT) : DateUtil.parseDateWithTimeZone(value2 as String, (value2 as String)?.matches(regexForJpLocale)? DateUtil.DATEPICKER_JFORMAT : DateUtil.DATEPICKER_FORMAT, userPreference.timeZone)) + 1

        if (value1 && value2) {
            ca.add {
                and {
                    ge(name, dt1)
                    le(name, dt2)
                }
            }
        } else if (value1) {
            ca.add {
                ge(name, dt1)
            }
        } else if (value2) {
            ca.add {
                le(name, dt2)
            }
        }
        return ca
    }

    static List<Closure> filterByNumberRange(def name, Long value1, Long value2, List<Closure> ca) {
        if (value1 && value2) {
            ca.add {
                and {
                    ge(name, value1)
                    le(name, value2)
                }
            }
        } else if (value1) {
            ca.add {
                ge(name, value1)
            }
        } else if (value2) {
            ca.add {
                le(name, value2)
            }
        }
        return ca
    }
}
