package com.rxlogix.api

import spock.lang.Specification
import spock.lang.Unroll

class SanitizePaginationAttributesSpec extends Specification implements SanitizePaginationAttributes {

    def "Canary"() {
        expect:
        true
    }

    @Unroll("Test pagination params for input attributes in sanitize #description and expect result set in params as max=#rMax, offset=#rOffset, sort=#rSort, order=#rOrder, searchString=#rSearchString as result")
    void "Test pagination params for input attributes in sanitize"() {
        given: "Params as a map"
        ParamsObj params = new ParamsObj(offset: offset, max: max, length: length, start: start, sort: sort, order: order, direction: direction, searchString: searchString)
        params.metaClass.int = { String key ->
            return delegate?."$key"?.toInteger()
        }

        and:
        def sanitizePaginationAttributes = new Object() as SanitizePaginationAttributes

        when:
        sanitizePaginationAttributes.sanitize(params)

        then:
        params.max == rMax
        params.offset == rOffset
        params.sort == rSort
        params.order == rOrder
        params.searchString == rSearchString

        where:
        description                        | max  | length | offset | start | sort             | order  | direction | searchString || rMax | rOffset | rSort            | rOrder | rSearchString
        "with all null attributes"         | null | null   | null   | null  | null             | null   | null      | null         || 50   | 0       | 'dateCreated'    | 'desc' | null
        "with max as 10"                   | 10   | null   | null   | null  | null             | null   | null      | null         || 10   | 0       | 'dateCreated'    | 'desc' | null
        "with length as 10"                | null | 10     | null   | null  | null             | null   | null      | null         || 10   | 0       | 'dateCreated'    | 'desc' | null
        "with start as 20"                 | null | null   | null   | 20    | null             | null   | null      | null         || 50   | 20      | 'dateCreated'    | 'desc' | null
        "with offset as 20"                | null | null   | 20     | null  | null             | null   | null      | null         || 50   | 20      | 'dateCreated'    | 'desc' | null
        "with max as 10, start as 20"      | 10   | null   | null   | 20    | null             | null   | null      | null         || 10   | 20      | 'dateCreated'    | 'desc' | null
        "with max as 10, length as 20"     | 10   | 20     | null   | null  | null             | null   | null      | null         || 20   | 0       | 'dateCreated'    | 'desc' | null
        "with offset as 20, length as 30"  | null | 30     | 20     | null  | null             | null   | null      | null         || 30   | 20      | 'dateCreated'    | 'desc' | null
        "with offset as 30, start as 90"   | null | null   | 30     | 90    | null             | null   | null      | null         || 50   | 90      | 'dateCreated'    | 'desc' | null
        "with start as 80, length as 10"   | null | 10     | null   | 80    | null             | null   | null      | null         || 10   | 80      | 'dateCreated'    | 'desc' | null
        "with order as desc"               | null | null   | null   | null  | null             | 'desc' | null      | null         || 50   | 0       | 'dateCreated'    | 'desc' | null
        "with direction as asc"            | null | null   | null   | null  | null             | null   | 'asc'     | null         || 50   | 0       | 'dateCreated'    | 'asc'  | null
        "with sort as createdBy"           | null | null   | null   | null  | 'createdBy'      | null   | null      | null         || 50   | 0       | 'owner.fullName' | 'desc' | null
        "with sort as dateCreated"         | null | null   | null   | null  | 'dateCreated'    | null   | null      | null         || 50   | 0       | 'dateCreated'    | 'desc' | null
        "with sort as owner.fullName"      | null | null   | null   | null  | 'owner.fullName' | null   | null      | null         || 50   | 0       | 'owner.fullName' | 'desc' | null
        "with seachString as 'User Name'"  | null | null   | null   | null  | null             | null   | null      | 'User Name'  || 50   | 0       | 'dateCreated'    | 'desc' | 'User Name'
        "with seachString as empty String" | null | null   | null   | null  | null             | null   | null      | ''           || 50   | 0       | 'dateCreated'    | 'desc' | ''
        "with seachString as 'username'"   | null | null   | null   | null  | null             | null   | null      | 'username'   || 50   | 0       | 'dateCreated'    | 'desc' | 'username'

    }
}

class ParamsObj {
    Integer offset
    Integer max
    Integer start
    String length
    String sort
    String direction
    String searchString
    String order
}
