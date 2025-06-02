package com.rxlogix.util

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord

class CsvUtil {
    final static String TOKEN = "_____"

    static List<CSVRecord> parseCsv(String[] headers, Reader reader) {
        List<String> filteredHeaders = []
        int i = 0
        headers.each {
            if (filteredHeaders.contains(it)) {
                filteredHeaders << it + TOKEN + i++
            } else {
                filteredHeaders << it
            }
        }
        String[] filteredHeadersArray = filteredHeaders.toArray()
        def csvParser = CSVFormat.EXCEL.withHeader(filteredHeadersArray).parse(reader)
        csvParser.toList()
    }

    static List parseCsv(Reader reader) {
        List result = []
        Iterable<CSVRecord> records = CSVFormat.RFC4180.parse(reader);
        records.each {
            result << it.toList()
        }
        return result
    }

    static getSourceKey(String key) {
        int i = key.indexOf(TOKEN)
        if (i > 0)
            return key.substring(0, i)
        else
            return key
    }
}
