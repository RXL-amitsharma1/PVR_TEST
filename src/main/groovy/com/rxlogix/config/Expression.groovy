package com.rxlogix.config

import com.rxlogix.enums.QueryOperatorEnum
import com.rxlogix.hibernate.EscapedILikeExpression
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import jdk.internal.reflect.FieldAccessor
import sun.reflect.ReflectionFactory

import java.lang.reflect.Field
import java.lang.reflect.Modifier


class Expression {

    private static Logger logger = LoggerFactory.getLogger(getClass())

    ReportField reportField
    String value
    QueryOperatorEnum operator

    public boolean equals(Expression other) {
        if (this.reportField != other.reportField
                || this.value != other.value
                || this.operator != other.operator) {
            return false
        }
        return true
    }

    //    We need to use following method while creating queries and putting value instead of getValue().
    String getNormalizeValue() {
        logger.debug("Expression Replacing for : ${value}")
        return value?.replaceAll("'", "''")
    }

    void test(){
        def field = EscapedILikeExpression.getDeclaredField('ESCAPE_CHAR_QUERY')
        field.setAccessible(true);
        // next we change the modifier in the Field instance to
        // not be final anymore, thus tricking reflection into
        // letting us modify the static final field
        Field modifiersField =
                Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        int modifiers = modifiersField.getInt(field);
        // blank out the final bit in the modifiers int
        modifiers &= ~Modifier.FINAL;
        modifiersField.setInt(field, modifiers);
        FieldAccessor fa = ReflectionFactory.getReflectionFactory().newFieldAccessor(
                field, false
        );
        fa.set(null, (" ESCAPE N'" + EscapedILikeExpression.ESCAPE_CHAR + "'"));
    }

}
