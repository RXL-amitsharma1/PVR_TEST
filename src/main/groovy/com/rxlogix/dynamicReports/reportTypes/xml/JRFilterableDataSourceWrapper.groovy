package com.rxlogix.dynamicReports.reportTypes.xml

import groovy.util.logging.Slf4j
import net.sf.jasperreports.engine.JRDataSource
import net.sf.jasperreports.engine.JRException
import net.sf.jasperreports.engine.JRField
import net.sf.jasperreports.engine.JRRewindableDataSource
import net.sf.jasperreports.engine.data.JRCsvDataSource

/**
 * An implementation of a Filterable JRDataSource. This implementation is a wrapper for
 * any of the existing JRDataSource implementations. This should aid in testing and allow
 * for backwards compatibility.
 *
 */
@Slf4j
class JRFilterableDataSourceWrapper implements JRRewindableDataSource {
    /*
     * Private reference to an instantiated JRDataSource
     */
    private JRDataSource dataSource

    /*
     * Place to store our filters
     */
    private Map<JRField, ?> filters = [:]

    /**
     * JRFilterableDataSourceWrapper Constructor
     *
     * @param dataSource
     */
    JRFilterableDataSourceWrapper(JRDataSource dataSource) {
        this.dataSource = dataSource
    }

    /**
     * Delegate to the instantiated JRCsvDataSource
     *
     * @see JRCsvDataSource#getFieldValue(JRField)
     */
    def getFieldValue(JRField jrField) throws JRException {
        return dataSource.getFieldValue(jrField)
    }

    /**
     * Return the Map of filters.
     * This method makes sure a valid Map object is always returned.
     *
     * @return java.util.Map
     */
    Map getFilters() throws JRException {
        return filters
    }

    /**
     * Delegate to the instantiated JRRewindableDataSource
     *
     * @see JRRewindableDataSource#moveFirst()
     */
    void moveFirst() throws JRException {
        if (dataSource instanceof JRRewindableDataSource) {
            ((JRRewindableDataSource) dataSource).moveFirst()
        } else {
            throw new JRException(dataSource.getClass().getName() + " does not implement the JRRewindableDataSource interface!")
        }
    }

    /**
     * This method calls the instantiated JRDatasource object's next() method and
     * checks a set of filters against the data in the row. If any filter fails or
     * ds.next() returns false, then we will return false.
     *
     * @see JRDataSource#next()
     */
    boolean next() throws JRException {
        boolean valid
        while ((valid = dataSource.next())) {
            for (Map.Entry<JRField, ?> entry : filters.entrySet()) {
                def value = entry.value
                try {
                    if (value != getFieldValue(entry.key)) {
                        // failed the test
                        valid = false
                        break
                    }
                } catch (JRException e) {
                    log.error("Error on getting value for column name ${entry.key.name}", e)
                }
            }
            /*
             * if valid == true, then all of the filters passed the test
             */
            if (valid) {
                break
            }
        }
        /*
         * true: when has next row AND all filters passed
         * false: either there is not a next row OR at least one filter failed
         */
        return valid
    }

    /**
     * Add a filter
     *
     * @param field
     * @param value
     */
    void addFilter(JRField field, Object value) {
        filters[field] = value
    }
}