package com.rxlogix.odata

import org.apache.olingo.commons.api.edm.EdmEntitySet
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind
import org.apache.olingo.commons.api.edm.FullQualifiedName
import org.apache.olingo.commons.api.ex.ODataRuntimeException
import org.apache.olingo.server.api.uri.UriInfo
import org.apache.olingo.server.api.uri.UriResource
import org.apache.olingo.server.api.uri.UriResourceEntitySet

import java.sql.Types

class OdataUtils {
    private static final ThreadLocal<String> threadLocalScope = new ThreadLocal<>();

    final static String getDsName() {
        return threadLocalScope.get();
    }

    final static void setDsName(String dsName) {
        threadLocalScope.set(dsName);
    }

    static FullQualifiedName getEdmPrimitiveTypeKindName(int type) {
        Map typeMap = [
                (Types.BIGINT)       : EdmPrimitiveTypeKind.Int64.getFullQualifiedName(),
                (Types.BIT)          : EdmPrimitiveTypeKind.Byte.getFullQualifiedName(),
                (Types.BINARY)       : EdmPrimitiveTypeKind.Binary.getFullQualifiedName(),
                (Types.BLOB)         : EdmPrimitiveTypeKind.Stream.getFullQualifiedName(),
                (Types.BOOLEAN)      : EdmPrimitiveTypeKind.Boolean.getFullQualifiedName(),
                (Types.CHAR)         : EdmPrimitiveTypeKind.String.getFullQualifiedName(),
                (Types.CLOB)         : EdmPrimitiveTypeKind.String.getFullQualifiedName(),//???
                (Types.DATE)         : EdmPrimitiveTypeKind.DateTimeOffset.getFullQualifiedName(),
                (Types.DECIMAL)      : EdmPrimitiveTypeKind.Decimal.getFullQualifiedName(),
                (Types.DOUBLE)       : EdmPrimitiveTypeKind.Double.getFullQualifiedName(),
                (Types.FLOAT)        : EdmPrimitiveTypeKind.Single.getFullQualifiedName(),
                (Types.INTEGER)      : EdmPrimitiveTypeKind.Int32.getFullQualifiedName(),
                (Types.LONGNVARCHAR) : EdmPrimitiveTypeKind.String.getFullQualifiedName(),
                (Types.LONGVARBINARY): EdmPrimitiveTypeKind.Binary.getFullQualifiedName(),
                (Types.LONGVARCHAR)  : EdmPrimitiveTypeKind.String.getFullQualifiedName(),
                (Types.NCHAR)        : EdmPrimitiveTypeKind.String.getFullQualifiedName(),
                (Types.NCLOB)        : EdmPrimitiveTypeKind.String.getFullQualifiedName(),//??
                (Types.NUMERIC)      : EdmPrimitiveTypeKind.Int64.getFullQualifiedName(), //todo:may be float
                (Types.NVARCHAR)     : EdmPrimitiveTypeKind.String.getFullQualifiedName(),
                (Types.REAL)         : EdmPrimitiveTypeKind.Double.getFullQualifiedName(),
                (Types.SMALLINT)     : EdmPrimitiveTypeKind.Int16.getFullQualifiedName(),
                (Types.TIME)         : EdmPrimitiveTypeKind.TimeOfDay.getFullQualifiedName(),
                (Types.TIMESTAMP)    : EdmPrimitiveTypeKind.DateTimeOffset.getFullQualifiedName(),
                (Types.TINYINT)      : EdmPrimitiveTypeKind.Byte.getFullQualifiedName(),
                (Types.VARBINARY)    : EdmPrimitiveTypeKind.Binary.getFullQualifiedName(),
                (Types.VARCHAR)      : EdmPrimitiveTypeKind.String.getFullQualifiedName(),

                // not supported:
                //(Types.SQLXML):??
                //(Types.STRUCT):??
                //(Types.NULL):??
                //(Types.ROWID):??
                //(Types.REF):??
                //(Types.OTHER):??
        ]
        return typeMap[type]
    }

    static Object toType(value, int type) {
        if (type in [Types.CHAR, Types.CLOB, Types.LONGNVARCHAR, Types.LONGVARCHAR, Types.NCHAR, Types.NCLOB, Types.NVARCHAR, Types.VARCHAR]) return value as String
        if (type in [Types.BIGINT, Types.NUMERIC]) return value as Long
        if (type == Types.DATE) return value as Date
        if (type == Types.TIMESTAMP) {
            if (value instanceof oracle.sql.TIMESTAMP) {
                return java.sql.Timestamp.valueOf(value.toString())
            } else {
                return value as Date
            }
        }
        if (type == Types.BOOLEAN) return value as Boolean
        if (type == Types.BIT) return value as Byte
        if (type == Types.DOUBLE) return value as Double
        if (type == Types.FLOAT) return value as Float
        if (type == Types.INTEGER) return value as Integer
        if (type == Types.REAL) return value as Float
        if (type == Types.SMALLINT) return value as Short
        if (type == Types.TINYINT) return value as Byte

        // not supported:
        //(Types.BINARY) : EdmPrimitiveTypeKind.Binary.getFullQualifiedName(),
        //(Types.BLOB)   : EdmPrimitiveTypeKind.Stream.getFullQualifiedName(),
        //(Types.DECIMAL)      : EdmPrimitiveTypeKind.Decimal.getFullQualifiedName(),
        //(Types.LONGVARBINARY): EdmPrimitiveTypeKind.Binary.getFullQualifiedName(),
        //(Types.VARBINARY)    : EdmPrimitiveTypeKind.Binary.getFullQualifiedName(),
        //(Types.SQLXML):??
        //(Types.STRUCT):??
        //(Types.NULL):??
        //(Types.ROWID):??
        //(Types.REF):??
        //(Types.OTHER):??

        return value
    }

    static URI createId(String entitySetName, Object id) {
        try {
            return new URI(entitySetName + "(" + String.valueOf(id) + ")");
        } catch (URISyntaxException e) {
            throw new ODataRuntimeException("Unable to create id for entity: " + entitySetName, e);
        }
    }

    static Boolean isClob(type) {
        return (type == Types.CLOB)
    }

    static EdmEntitySet getEdmEntitySet(UriInfo uriInfo) {
        List<UriResource> resourceParts = uriInfo.getUriResourceParts();
        UriResourceEntitySet uriEntityset = (UriResourceEntitySet) resourceParts.get(0);
        return uriEntityset.getEntitySet();
    }

}

