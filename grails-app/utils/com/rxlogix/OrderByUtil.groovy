package com.rxlogix

import org.hibernate.Criteria
import org.hibernate.HibernateException
import org.hibernate.criterion.CriteriaQuery
import org.hibernate.criterion.Order
import org.hibernate.engine.spi.SessionFactoryImplementor

class OrderByUtil {
    static Order booleanOrder(String propertyName, String direction) {
        new Order(propertyName, direction == "asc") {
            String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
                String column = criteriaQuery.getColumnsUsingProjection(criteria, propertyName)[0]
                return """(CASE WHEN ${column} = 1 THEN 'a' ELSE 'z' END) ${direction}""".toString()
            }
        }
    }

    static Order trimOrderIgnoreCase(String propertyName, String direction) {
        new Order(propertyName, direction == "asc") {
            String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
                SessionFactoryImplementor factory = criteriaQuery.getFactory()
                String formulaPrefix = factory.getDialect().getLowercaseFunction().concat("(")
                String formulaPostfix = ")"

                String column = criteriaQuery.getColumnsUsingProjection(criteria, propertyName)[0]
                String trimColumn = "(TRIM(" + column + "))"

                return """${formulaPrefix}${trimColumn}${formulaPostfix} ${direction}""".toString()
            }
        }
    }

    static Order formulaOrder(String formula, String direction) {
        new Order(formula, direction == "asc") {
            String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
                return """${formula} ${direction}""".toString()
            }
        }
    }

    static Order orOrderIgnoreCase(List<String> properties, String direction) {
        new Order(properties.get(0), direction == "asc") {
            String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {

                SessionFactoryImplementor factory = criteriaQuery.getFactory()
                String formulaPrefix = factory.getDialect().getLowercaseFunction().concat("(")
                String formulaPostfix = ")"

                List<String> columnNames = properties.collect {criteriaQuery.getColumnsUsingProjection(criteria, it)[0]}
                return """${formulaPrefix}${buildPropertiesOrSql(columnNames, 0)}${formulaPostfix} ${direction}""".toString()
            }
        }
    }

    static Order concatOrderIgnoreCase(List<String> properties, String direction) {
        new Order(properties.get(0), direction == "asc") {
            String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {

                SessionFactoryImplementor factory = criteriaQuery.getFactory()
                String formulaPrefix = factory.getDialect().getLowercaseFunction().concat("(")
                String formulaPostfix = ")"

                List<String> columnNames = properties.collect {criteriaQuery.getColumnsUsingProjection(criteria, it)[0]}
                String formula = columnNames.join(" || ")

                return """${formulaPrefix}${formula}${formulaPostfix} ${direction}""".toString()
            }
        }
    }

    static Order mapOrderIgnoreCase(String propertyName, Map valueMap, String direction) {
        new Order(propertyName, direction == "asc") {
            String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {

                SessionFactoryImplementor factory = criteriaQuery.getFactory()
                String column = criteriaQuery.getColumnsUsingProjection(criteria, propertyName)[0]
                String formulaPrefix = factory.getDialect().getLowercaseFunction().concat("""(CASE ${column} """)
                String formulaPostfix = "END)"
                String formula = ""

                valueMap.each{ key, value -> formula += """WHEN '${key}' THEN '${value}' """ }

                return """${formulaPrefix}${formula}${formulaPostfix} ${direction}""".toString()
            }
        }
    }

    private static String buildPropertiesOrSql(List<String> properties, Integer index) {
        if (!properties || properties.isEmpty()) {
            return;
        }
        if (index >= properties.size() - 1) {
            return properties.get(properties.size() - 1)
        }
        String sql = """(CASE WHEN ${properties.get(index)} IS NOT NULL THEN ${properties.get(index)} ELSE %s END)""".toString()
        return String.format(sql, buildPropertiesOrSql(properties, index + 1))
    }
}
