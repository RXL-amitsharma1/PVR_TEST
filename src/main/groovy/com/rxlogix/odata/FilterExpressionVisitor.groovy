package com.rxlogix.odata

import org.apache.olingo.commons.api.edm.EdmEnumType
import org.apache.olingo.commons.api.edm.EdmType
import org.apache.olingo.commons.api.http.HttpStatusCode
import org.apache.olingo.commons.core.edm.primitivetype.EdmDateTimeOffset
import org.apache.olingo.commons.core.edm.primitivetype.EdmString
import org.apache.olingo.server.api.ODataApplicationException
import org.apache.olingo.server.api.uri.UriInfoResource
import org.apache.olingo.server.api.uri.UriResource
import org.apache.olingo.server.api.uri.UriResourcePrimitiveProperty
import org.apache.olingo.server.api.uri.queryoption.expression.*

import java.util.concurrent.atomic.AtomicInteger

class FilterExpressionVisitor implements ExpressionVisitor<String> {

    private List allowedFields;
    private Map values = [:]
    AtomicInteger paramCounter = new AtomicInteger(0)

    FilterExpressionVisitor(allowedFields) {
        this.allowedFields = allowedFields
    }

    Map getValues() {
        return values.collectEntries { key, val ->
            [key.substring(1), val] //removing ":"

        }
    }

    @Override
    String visitMember(Member member) throws ExpressionVisitException, ODataApplicationException {

        final List<UriResource> uriResourceParts = member.resourcePath?.uriResourceParts?:[]

        // Allows only primitive properties.
        if (uriResourceParts.size() == 1 && uriResourceParts.get(0) instanceof UriResourcePrimitiveProperty) {
            UriResourcePrimitiveProperty uriResourceProperty = (UriResourcePrimitiveProperty) uriResourceParts.get(0)
            String columnName = allowedFields.find { it.label == uriResourceProperty.getProperty().getName() }.columnName
            if (columnName) return columnName
        }
        throw new ODataApplicationException("Only primitive properties are implemented in filter expressions", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH)
    }

    @Override
    String visitLiteral(Literal literal) throws ExpressionVisitException, ODataApplicationException {

        // String literals start and end with an single quotation mark
        String literalAsString = literal.getText();
        if (literal.getType() instanceof EdmString) {
            String stringLiteral = ""
            if (literal.getText().length() > 2) {
                stringLiteral = literalAsString.substring(1, literalAsString.length() - 1)
            }
            String paramName = ":param_" + paramCounter.addAndGet(1)
            values << [(paramName): stringLiteral]
            return paramName
        } else if (literal.getType() instanceof EdmDateTimeOffset) {
            return " TO_TIMESTAMP_TZ('" + literalAsString + "','YYYY-MM-DD\"T\"hh24:mi:sstzh:tzm')"
        } else {
            try {
                return Integer.parseInt(literalAsString)
            } catch (NumberFormatException e) {
                throw new ODataApplicationException("Unsupported literal type", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
            }
        }
    }

    @Override
    public String visitUnaryOperator(UnaryOperatorKind operator, String operand) throws ExpressionVisitException, ODataApplicationException {

        if (operator == UnaryOperatorKind.NOT && operand instanceof Boolean) {
            return " not "
        } else if (operator == UnaryOperatorKind.MINUS && operand instanceof Integer) {
            return " - "
        }
        throw new ODataApplicationException("Invalid type for unary operator",
                HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
    }

    @Override
    String visitBinaryOperator(BinaryOperatorKind operator, String left, String right) throws ExpressionVisitException, ODataApplicationException {
        String mod = ""
        String op = ""
        switch (operator) {
            case BinaryOperatorKind.ADD: op = " + "; break;
            case BinaryOperatorKind.MOD: mod = "mod"; op = ","; break;
            case BinaryOperatorKind.MUL: op = " * "; break;
            case BinaryOperatorKind.DIV: op = " / "; break;
            case BinaryOperatorKind.SUB: op = " - "; break;

            case BinaryOperatorKind.EQ: op = " = "; break;
            case BinaryOperatorKind.NE: op = " <> "; break;
            case BinaryOperatorKind.GE: op = " >= "; break;
            case BinaryOperatorKind.GT: op = " > "; break;
            case BinaryOperatorKind.LE: op = " <= "; break;
            case BinaryOperatorKind.LT: op = " < "; break;

            case BinaryOperatorKind.AND: op = " and "; break;
            case BinaryOperatorKind.OR: op = " or "; break;
            default: throw new ODataApplicationException("Binary operation " + operator.name() + " is not implemented",
                    HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
        }
        return mod + "(" + left + op + right + ")"
    }

    @Override
    String visitMethodCall(MethodKind methodCall, List<String> parameters) throws ExpressionVisitException, ODataApplicationException {
        String valueParam1 = parameters.size() > 0 ? parameters.get(0): null
        String valueParam2 = parameters.size() > 1 ? parameters.get(1): null
        String valueParam3 = parameters.size() > 2 ? parameters.get(2): null

        switch (methodCall) {
            case MethodKind.CONTAINS:
                if (valueParam2.startsWith(":param_")) {
                    values[valueParam2] = "%" + values[valueParam2] + "%"
                }
                return "(" + valueParam1 + " like " + valueParam2 + ")"
            case MethodKind.STARTSWITH:
                if (valueParam2.startsWith(":param_")) {
                    values[valueParam2] = values[valueParam2] + "%"
                }
                return "(" + valueParam1 + " like " + valueParam2 + ")"
            case MethodKind.ENDSWITH:
                if (valueParam2.startsWith(":param_")) {
                    values[valueParam2] = "%" + values[valueParam2]
                }
                return "(" + valueParam1 + " like " + valueParam2 + ")"
            case MethodKind.LENGTH:
                return "LENGTH(" + valueParam1 + ")"
            case MethodKind.INDEXOF:
                return "INSTR(" + valueParam1 + "," + valueParam2 + ")"
            case MethodKind.SUBSTRING:
                if (parameters.size() > 2) {
                    return "SUBSTR(" + valueParam1 + " , " + valueParam2 + " , " + valueParam3 + ")"
                } else
                    return "SUBSTR(" + valueParam1 + " , " + valueParam2 + ")"
            case MethodKind.TOLOWER:
                return "LOWER(" + valueParam1 + ")"
            case MethodKind.TOUPPER:
                return "UPPER(" + valueParam1 + ")"
            case MethodKind.TRIM:
                return "TRIM(BOTH ' ' FROM  " + valueParam1 + ")"
            case MethodKind.CONCAT:
                return "(" + valueParam1 + " || " + valueParam2 + ")"
            case MethodKind.YEAR:
                return "EXTRACT(YEAR FROM " + valueParam1 + ")"
            case MethodKind.MONTH:
                return "EXTRACT(MONTH FROM " + valueParam1 + ")"
            case MethodKind.DAY:
                return "EXTRACT(DAY FROM " + valueParam1 + ")"
            case MethodKind.HOUR:
                return "EXTRACT(HOUR FROM " + valueParam1 + ")"
            case MethodKind.MINUTE:
                return "EXTRACT(MINUTE FROM " + valueParam1 + ")"
            case MethodKind.SECOND:
                return "EXTRACT(SECOND FROM " + valueParam1 + ")"
            case MethodKind.NOW:
                return "CURRENT_DATE"
            case MethodKind.ROUND:
                return "ROUND(" + valueParam1 + ",1)"
            case MethodKind.FLOOR:
                return "FLOOR(" + valueParam1 + ")"
            case MethodKind.CEILING:
                return "CEIL(" + valueParam1 + ")"
        }

        // ------ not supported -----
        //        FRACTIONALSECONDS("fractionalseconds"),
        //        TOTALSECONDS("totalseconds"), DATE("date"), TIME("time"),
        //        TOTALOFFSETMINUTES("totaloffsetminutes"),
        //        MINDATETIME("mindatetime"),
        //        MAXDATETIME("maxdatetime"),
        //        GEODISTANCE("geo.distance"),
        //        GEOLENGTH("geo.length"),
        //        GEOINTERSECTS("geo.intersects"),
        //        CAST("cast"),
        //        ISOF("isof");
        throw new ODataApplicationException("Method call " + methodCall + " not implemented", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH)
    }

    @Override
    public String visitTypeLiteral(EdmType type) throws ExpressionVisitException, ODataApplicationException {
        throw new ODataApplicationException("Type literals are not implemented",
                HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
    }

    @Override
    public String visitAlias(String aliasName) throws ExpressionVisitException, ODataApplicationException {
        throw new ODataApplicationException("Aliases are not implemented",
                HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
    }

    @Override
    public String visitEnum(EdmEnumType type, List<String> enumValues)
            throws ExpressionVisitException, ODataApplicationException {
        throw new ODataApplicationException("Enums are not implemented",
                HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
    }

    @Override
    String visitBinaryOperator(BinaryOperatorKind binaryOperatorKind, String s, List<String> list) throws ExpressionVisitException, ODataApplicationException {
        return null
    }

    @Override
    public String visitLambdaExpression(String lambdaFunction, String lambdaVariable, Expression expression)
            throws ExpressionVisitException, ODataApplicationException {
        throw new ODataApplicationException("Lamdba expressions are not implemented",
                HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
    }

    @Override
    public String visitLambdaReference(String variableName)
            throws ExpressionVisitException, ODataApplicationException {
        throw new ODataApplicationException("Lamdba references are not implemented",
                HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
    }

}
