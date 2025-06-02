package com.rxlogix.dynamicReports.functions;

import com.rxlogix.DynamicReportService;
import com.rxlogix.dynamicReports.util.KeyFieldTextMeasurer;
import com.rxlogix.util.MiscUtil;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.data.JRXmlDataSource;
import net.sf.jasperreports.functions.annotations.Function;
import net.sf.jasperreports.functions.annotations.FunctionCategories;
import net.sf.jasperreports.functions.annotations.FunctionParameter;
import net.sf.jasperreports.functions.annotations.FunctionParameters;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.*;

@FunctionCategories({CIOMS_I_Form.class})
public class CIOMS_I_FormFunctions {

    private static final int DEFAULT_HEAD_ROWS_COUNT = 2;
    private static final String XML_PATTERN_STR = "(?s)<(\\S+?)(.*?)>(.*?)</\\1>";
    private static final Log log = LogFactory.getLog(CIOMS_I_FormFunctions.class);

    @Function("SUB_STRING")
    @FunctionParameters({@FunctionParameter("text"), @FunctionParameter("offset")})
    public static String SUB_STRING(String text, int offset) {
        if(text == null){
            return "";
        }
        if (offset > text.length()) {
            log.info("Error: Text size is less than offset");
            log.info("Offset: " + offset + " TextSize: " + text.length() + " Text: " + text);
        }
        return text.substring(Math.min(offset, text.length()));
    }

    @Function("XML_DATA_SOURCE")
    @FunctionParameters({@FunctionParameter("xmlData"), @FunctionParameter("isFirstPage"),
            @FunctionParameter("headRowsCount")})
    public static JRDataSource XML_DATA_SOURCE(String xmlData, Boolean isFirstPage) {
        return XML_DATA_SOURCE(xmlData, isFirstPage, DEFAULT_HEAD_ROWS_COUNT);
    }

    public static JRDataSource XML_DATA_SOURCE(String xmlData, Boolean isFirstPage, Integer headRowsCount) {
        if (xmlData != null && xmlData.trim().length() > 0) {
            try {
                xmlData = removeLeadingAndTrailingSpaces(xmlData);
                InputStream in = new ByteArrayInputStream(xmlData.getBytes("UTF-8"));
                String selectExpression = MessageFormat.format("/XML_START/ROW[position(){0}{1}]",
                        isFirstPage ? "<=" : ">", headRowsCount);
                JRDataSource dataSource = new JRXmlDataSource(in, selectExpression) {
                    protected Object convertStringValue(String text, Class<?> valueClass) {
                        if(String.class.equals(valueClass)) {
                            text = text.trim();
                        }
                        return super.convertStringValue(text, valueClass);
                    }
                };
                return dataSource;
            } catch (Exception e) {
                log.error(e.getMessage());
                return new JREmptyDataSource();
            }
        } else {
            return new JREmptyDataSource();
        }
    }

    @Function("STRING_DATA_SOURCE")
    @FunctionParameters({@FunctionParameter("stringData")})
    public static JRDataSource STRING_DATA_SOURCE(String stringData) {
        List<Map<String, String>> tableData = new ArrayList<Map<String, String>>();
        if (stringData != null && stringData.trim().length() > 0) {
            try {
                List<String> list1 = Arrays.asList(stringData.split("\n"));
                Iterator<String> valuesIterator = list1.iterator();
                while (valuesIterator.hasNext()) {
                    Map<String, String> productMap = new HashMap<String, String>();
                    productMap.put("MPT_LIST", valuesIterator.next());
                    tableData.add(productMap);
                }
                JRDataSource dataSource = new JRBeanCollectionDataSource(tableData);
                return dataSource;
            } catch (Exception e) {
                log.error(e.getMessage());
                return new JREmptyDataSource();
            }
        } else {
            return new JREmptyDataSource();
        }
    }

    @Function("XML_DATA_SIZE")
    @FunctionParameters({@FunctionParameter("xmlData")})
    public static int XML_DATA_SIZE(String xmlData) {
        if (xmlData != null && xmlData.trim().length() > 0) {
            XPath xpath = XPathFactory.newInstance().newXPath();
            String expression = "/XML_START/ROW";
            InputSource inputSource = new InputSource(new StringReader(xmlData));
            try {
                NodeList nodes = (NodeList) xpath.evaluate(expression, inputSource, XPathConstants.NODESET);
                return nodes.getLength();
            } catch (XPathExpressionException e) {
                log.error(e.getMessage());
            }
        }
        return 0;
    }

    @Function("ALL_ROWS")
    @FunctionParameters({@FunctionParameter("data")})
    public static String ALL_ROWS(String data) {
        List<String> items = parseItemList(data);
        return StringUtils.join(items, "\n");
    }

    @Function("ALL_ROWS_MEDWATCH")
    @FunctionParameters({@FunctionParameter("data")})
    public static String ALL_ROWS_MEDWATCH(String data) {
        List<String> items = parseItemList(data);
        items = MiscUtil.appendsToList(items);
        return StringUtils.join(items, "\n");
    }

    @Function("HEAD_ROWS")
    @FunctionParameters({@FunctionParameter("data"), @FunctionParameter("headRowsCount")})
    public static String HEAD_ROWS(String data) {
        return HEAD_ROWS(data, DEFAULT_HEAD_ROWS_COUNT);
    }

    public static String HEAD_ROWS(String data, Integer headRowsCount) {
        List<String> items = parseItemList(data);
        if (items.size() > headRowsCount) {
            items = items.subList(0, headRowsCount);
        }
        return StringUtils.join(items, "\n");
    }

    @Function("TAIL_ROWS")
    @FunctionParameters({@FunctionParameter("data"), @FunctionParameter("headRowsCount")})
    public static String TAIL_ROWS(String data) {
        return TAIL_ROWS(data, DEFAULT_HEAD_ROWS_COUNT);
    }

    public static String TAIL_ROWS(String data, Integer headRowsCount) {
        List<String> items = parseItemList(data);
        if (items.size() > headRowsCount) {
            items = items.subList(headRowsCount, items.size());
        } else {
            items = new ArrayList<>();
        }
        return StringUtils.join(items, "\n");
    }

    @Function("HAS_TAIL_ROWS")
    @FunctionParameters({@FunctionParameter("data"), @FunctionParameter("headRowsCount")})
    public static Boolean HAS_TAIL_ROWS(String data) {
        return HAS_TAIL_ROWS(data, DEFAULT_HEAD_ROWS_COUNT);
    }

    public static Boolean HAS_TAIL_ROWS(String data, Integer headRowsCount) {
        return parseItemList(data).size() > headRowsCount;
    }

    private static List<String> parseItemList(String data) {
        List<String> list = new ArrayList<>();
        if (data != null) {
            data = removeLeadingAndTrailingSpaces(data);
            if (data.matches(XML_PATTERN_STR)) {
                list = parseXmlItemList(data);
            } else {
                list = parseStringItemList(data);
            }
        }
        return list;
    }

    @Function("GET_CASE_SUMMARY")
    @FunctionParameters({@FunctionParameter("otherSerCriteria"), @FunctionParameter("describeReaction"),
            @FunctionParameter("caseDescription"), @FunctionParameter("caseComment")})
    public static String GET_CASE_SUMMARY(String otherSerCriteria, String describeReaction,
                                          String caseDescription, String caseComment) {
        StringBuilder sb = new StringBuilder();
        if (otherSerCriteria != null && !otherSerCriteria.trim().isEmpty()) {
            sb.append(otherSerCriteria);
        }
        if (describeReaction != null && !describeReaction.trim().isEmpty()) {
            sb.append("\n");
            sb.append(ALL_ROWS(describeReaction));
        }
        if (caseDescription != null && !caseDescription.trim().isEmpty()) {
            sb.append("\n\n");
            sb.append("Case Description: ");
            sb.append(caseDescription);
        }
        if (caseComment != null && !caseComment.trim().isEmpty()) {
            sb.append("\n\n");
            sb.append("Case Comment: ");
            sb.append(ALL_ROWS(caseComment));
        }
        return sb.toString();
    }

    @Function("IS_FIELD_PARTIALLY_RENDERED")
    @FunctionParameters({@FunctionParameter("jasperReportsContext"), @FunctionParameter("key")})
    public static Boolean IS_FIELD_PARTIALLY_RENDERED(JasperReportsContext jasperReportsContext, String... keys) {
        for (String key : keys) {
            if (KeyFieldTextMeasurer.isFieldPartiallyRendered(jasperReportsContext, key)) {
                return true;
            }
        }
        return false;
    }

    @Function("GET_FIELD_OFFSET")
    @FunctionParameters({@FunctionParameter("jasperReportsContext"), @FunctionParameter("key")})
    public static Integer GET_FIELD_OFFSET(JasperReportsContext jasperReportsContext, String key) {
        return KeyFieldTextMeasurer.getFieldTextOffset(jasperReportsContext, key);
    }

    @Function("IS_FIELD_TRUNCATED")
    @FunctionParameters({
            @FunctionParameter("fieldValue"),
            @FunctionParameter("xmlData"),
            @FunctionParameter("xPath"),
            @FunctionParameter("skipRegex")})
    public static Boolean IS_FIELD_TRUNCATED(String fieldValue, String xmlData, String xPath) {
        return IS_FIELD_TRUNCATED(fieldValue, xmlData, xPath, null);
    }

    public static Boolean IS_FIELD_TRUNCATED(String fieldValue, String xmlData, String xPath, String skipRegex) {
        List<String> items = parseItemList(fieldValue);
        List<String> values = parseXmlItemList(xmlData, xPath);
        boolean isTruncated = false;
        for (int i = 0; i < 2; i++) {
            if (values.size() > i) {
                if (items.size() > i) {
                    String value = values.get(i);
                    if (skipRegex != null) {
                        value = value.replaceFirst(skipRegex, "");
                    }
                    isTruncated |= !items.get(i).contains(value.trim());
                } else {
                    isTruncated = true;
                }
            }
        }
        return isTruncated;
    }

    private static List<String> parseStringItemList(String stringData) {
        return Arrays.asList(stringData.split("\r?\n"));
    }

    private static List<String> parseXmlItemList(String xmlData) {
        return parseXmlItemList(xmlData, "/*/*");
    }

    private static List<String> parseXmlItemList(String xmlData, String xPath) {
        List<String> list = new ArrayList<>();
        if (xmlData != null && xmlData.trim().length() > 0) {
            try {
                Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                        .parse(new ByteArrayInputStream(xmlData.getBytes("UTF-8")));
                XPathFactory xPathfactory = XPathFactory.newInstance();
                XPath xpath = xPathfactory.newXPath();
                XPathExpression expr = xpath.compile(xPath);
                NodeList nodeList = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
                for (int i = 0; i < nodeList.getLength(); ++i) {
                    list.add(nodeList.item(i).getTextContent());
                }
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        return list;
    }

    private static String removeLeadingAndTrailingSpaces(String text) {
        // Remove leading and/or trailing spaces and tabulations
        return text != null ? text.replaceAll("(?m)^[ \t]+|[ \t]+$", "") : text;
    }

}