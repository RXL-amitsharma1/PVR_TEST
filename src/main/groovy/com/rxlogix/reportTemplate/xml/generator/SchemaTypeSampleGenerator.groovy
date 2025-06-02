package com.rxlogix.reportTemplate.xml.generator

import org.apache.xmlbeans.GDate
import org.apache.xmlbeans.GDateBuilder
import org.apache.xmlbeans.GDuration
import org.apache.xmlbeans.GDurationBuilder
import org.apache.xmlbeans.SchemaType
import org.apache.xmlbeans.SimpleValue
import org.apache.xmlbeans.XmlAnySimpleType
import org.apache.xmlbeans.XmlDate
import org.apache.xmlbeans.XmlDateTime
import org.apache.xmlbeans.XmlDecimal
import org.apache.xmlbeans.XmlDuration
import org.apache.xmlbeans.XmlGDay
import org.apache.xmlbeans.XmlGMonth
import org.apache.xmlbeans.XmlGMonthDay
import org.apache.xmlbeans.XmlGYear
import org.apache.xmlbeans.XmlGYearMonth
import org.apache.xmlbeans.XmlInteger
import org.apache.xmlbeans.XmlObject
import org.apache.xmlbeans.XmlTime
import org.apache.xmlbeans.impl.util.HexBin

import javax.xml.namespace.QName
import java.text.DateFormat
import java.text.SimpleDateFormat

class SchemaTypeSampleGenerator {
    private static final String XMLNS = "urn:hl7-org:v3"
    private static final DateFormat TIME_FORMATTER = new SimpleDateFormat("yyyyMMddHHmmss.S")

    // a bit from the Aenid
    private static final String[] WORDS = [
            "ipsa", "iovis", "rapidum", "iaculata", "e", "nubibus", "ignem",
            "disiecitque", "rates", "evertitque", "aequora", "ventis",
            "illum", "exspirantem", "transfixo", "pectore", "flammas",
            "turbine", "corripuit", "scopuloque", "infixit", "acuto",
            "ast", "ego", "quae", "divum", "incedo", "regina", "iovisque",
            "et", "soror", "et", "coniunx", "una", "cum", "gente", "tot", "annos",
            "bella", "gero", "et", "quisquam", "numen", "iunonis", "adorat",
            "praeterea", "aut", "supplex", "aris", "imponet", "honorem",
            "talia", "flammato", "secum", "dea", "corde", "volutans",
            "nimborum", "in", "patriam", "loca", "feta", "furentibus", "austris",
            "aeoliam", "venit", "hic", "vasto", "rex", "aeolus", "antro",
            "luctantis", "ventos", "tempestatesque", "sonoras",
            "imperio", "premit", "ac", "vinclis", "et", "carcere", "frenat",
            "illi", "indignantes", "magno", "cum", "murmure", "montis",
            "circum", "claustra", "fremunt", "celsa", "sedet", "aeolus", "arce",
            "sceptra", "tenens", "mollitque", "animos", "et", "temperat", "iras",
            "ni", "faciat", "maria", "ac", "terras", "caelumque", "profundum",
            "quippe", "ferant", "rapidi", "secum", "verrantque", "per", "auras",
            "sed", "pater", "omnipotens", "speluncis", "abdidit", "atris",
            "hoc", "metuens", "molemque", "et", "montis", "insuper", "altos",
            "imposuit", "regemque", "dedit", "qui", "foedere", "certo",
            "et", "premere", "et", "laxas", "sciret", "dare", "iussus", "habenas"
    ]


    private static final String[] DNS1 = ["corp", "your", "my", "sample", "company", "test", "any"]
    private static final String[] DNS2 = ["com", "org", "com", "gov", "org", "com", "org", "com", "edu"]


    private Map<QName, String> values = [
            (QName.valueOf("{${XMLNS}}ts")): TIME_FORMATTER.format(Calendar.instance.time)
    ]

    private Random _picker = new Random(1)

    String nextWord() {
        return pick(WORDS)
    }

    String sampleDataForSimpleType(SchemaType sType) {
        if (XmlObject.type == sType)
            return "anyType"

        if (XmlAnySimpleType.type == sType)
            return "anySimpleType"

        if (sType.getSimpleVariety() == SchemaType.LIST) {
            SchemaType itemType = sType.getListItemType()
            StringBuffer sb = new StringBuffer()
            int length = pickLength(sType)
            if (length > 0)
                sb.append(sampleDataForSimpleType(itemType))
            for (int i = 1; i < length; i += 1) {
                sb.append(' ')
                sb.append(sampleDataForSimpleType(itemType))
            }
            return sb.toString()
        }

        if (sType.getSimpleVariety() == SchemaType.UNION) {
            SchemaType[] possibleTypes = sType.getUnionConstituentTypes()
            if (possibleTypes.length == 0)
                return ""
            return sampleDataForSimpleType(possibleTypes[pick(possibleTypes.length)])
        }

        XmlAnySimpleType[] enumValues = sType.getEnumerationValues()
        if (enumValues != null && enumValues.length > 0) {
            return enumValues[pick(enumValues.length)].getStringValue()
        }

        switch (sType.getPrimitiveType().getBuiltinTypeCode()) {
            case SchemaType.BTC_ANY_TYPE:
            case SchemaType.BTC_ANY_SIMPLE:
                return "anything"
            case SchemaType.BTC_BOOLEAN:
                return pick(2) == 0 ? "true" : "false"
            case SchemaType.BTC_BASE_64_BINARY:
                String result
                try {
                    result = new String(Base64.encode(formatToLength(pick(WORDS), sType).getBytes("utf-8")))
                }
                catch (java.io.UnsupportedEncodingException e) {  /* Can't possibly happen */
                }
                return result
            case SchemaType.BTC_HEX_BINARY:
                return HexBin.encode(formatToLength(pick(WORDS), sType))
            case SchemaType.BTC_ANY_URI:
                return formatToLength("http://www." + pick(DNS1) + "." + pick(DNS2) + "/" + pick(WORDS) + "/" + pick(WORDS), sType)
            case SchemaType.BTC_QNAME:
                return formatToLength("qname", sType)
            case SchemaType.BTC_NOTATION:
                return formatToLength("notation", sType)
            case SchemaType.BTC_FLOAT:
                return "1.5E2"
            case SchemaType.BTC_DOUBLE:
                return "1.051732E7"
            case SchemaType.BTC_DECIMAL:
                switch (closestBuiltin(sType).getBuiltinTypeCode()) {
                    case SchemaType.BTC_SHORT:
                        return formatDecimal("1", sType)
                    case SchemaType.BTC_UNSIGNED_SHORT:
                        return formatDecimal("5", sType)
                    case SchemaType.BTC_BYTE:
                        return formatDecimal("2", sType)
                    case SchemaType.BTC_UNSIGNED_BYTE:
                        return formatDecimal("6", sType)
                    case SchemaType.BTC_INT:
                        return formatDecimal("3", sType)
                    case SchemaType.BTC_UNSIGNED_INT:
                        return formatDecimal("7", sType)
                    case SchemaType.BTC_LONG:
                        return formatDecimal("10", sType)
                    case SchemaType.BTC_UNSIGNED_LONG:
                        return formatDecimal("11", sType)
                    case SchemaType.BTC_INTEGER:
                        return formatDecimal("100", sType)
                    case SchemaType.BTC_NON_POSITIVE_INTEGER:
                        return formatDecimal("-200", sType)
                    case SchemaType.BTC_NEGATIVE_INTEGER:
                        return formatDecimal("-201", sType)
                    case SchemaType.BTC_NON_NEGATIVE_INTEGER:
                        return formatDecimal("200", sType)
                    case SchemaType.BTC_POSITIVE_INTEGER:
                        return formatDecimal("201", sType)
                    case SchemaType.BTC_DECIMAL:
                    default:
                        return formatDecimal("1000.00", sType)
                }
            case SchemaType.BTC_STRING:
                String result
                switch (closestBuiltin(sType).getBuiltinTypeCode()) {
                    case SchemaType.BTC_STRING:
                    case SchemaType.BTC_NORMALIZED_STRING:
                        result = values[sType.name] ?: "string"
                        break
                    case SchemaType.BTC_TOKEN:
                        result = values[sType.name] ?: "token"
                        break
                    default:
                        result = values[sType.name] ?: "string"
                        break
                }
                return formatToLength(result, sType)
            case SchemaType.BTC_DURATION:
                return formatDuration(sType)

            case SchemaType.BTC_DATE_TIME:
            case SchemaType.BTC_TIME:
            case SchemaType.BTC_DATE:
            case SchemaType.BTC_G_YEAR_MONTH:
            case SchemaType.BTC_G_YEAR:
            case SchemaType.BTC_G_MONTH_DAY:
            case SchemaType.BTC_G_DAY:
            case SchemaType.BTC_G_MONTH:
                return formatDate(sType)
            case SchemaType.BTC_NOT_BUILTIN:
            default:
                return ""

        }
    }

    private int pick(int n) {
        return _picker.nextInt(n)
    }

    private String pick(String[] a) {
        return a[pick(a.length)]
    }

    private int pickLength(SchemaType sType) {
        XmlInteger length = (XmlInteger) sType.getFacet(SchemaType.FACET_LENGTH)
        if (length != null)
            return length.getBigIntegerValue().intValue()
        XmlInteger min = (XmlInteger) sType.getFacet(SchemaType.FACET_MIN_LENGTH)
        XmlInteger max = (XmlInteger) sType.getFacet(SchemaType.FACET_MAX_LENGTH)
        int minInt, maxInt
        if (min == null)
            minInt = 0
        else
            minInt = min.getBigIntegerValue().intValue()
        if (max == null)
            maxInt = Integer.MAX_VALUE
        else
            maxInt = max.getBigIntegerValue().intValue()
        // We try to keep the length of the array within reasonable limits,
        // at least 1 item and at most 3 if possible
        if (minInt == 0 && maxInt >= 1)
            minInt = 1
        if (maxInt > minInt + 2)
            maxInt = minInt + 2
        if (maxInt < minInt)
            maxInt = minInt
        return minInt + pick(maxInt - minInt)
    }

    /**
     * Formats a given string to the required length, using the following operations:
     * - append the source string to itself as necessary to pass the minLength
     * - truncate the result of previous step, if necessary, to keep it within minLength.
     */
    private String formatToLength(String s, SchemaType sType) {
        String result = s
        try {
            SimpleValue min = (SimpleValue) sType.getFacet(SchemaType.FACET_LENGTH)
            if (min == null)
                min = (SimpleValue) sType.getFacet(SchemaType.FACET_MIN_LENGTH)
            if (min != null) {
                int len = min.getIntValue()
                while (result.length() < len)
                    result = result + result
            }
            SimpleValue max = (SimpleValue) sType.getFacet(SchemaType.FACET_LENGTH)
            if (max == null)
                max = (SimpleValue) sType.getFacet(SchemaType.FACET_MAX_LENGTH)
            if (max != null) {
                int len = max.getIntValue()
                if (result.length() > len)
                    result = result.substring(0, len)
            }
        }
        catch (Exception e) // intValue can be out of range
        {
        }
        return result
    }

    private String formatDecimal(String start, SchemaType sType) {
        BigDecimal result = new BigDecimal(start)
        XmlDecimal xmlD
        xmlD = (XmlDecimal) sType.getFacet(SchemaType.FACET_MIN_INCLUSIVE)
        BigDecimal min = xmlD != null ? xmlD.getBigDecimalValue() : null
        xmlD = (XmlDecimal) sType.getFacet(SchemaType.FACET_MAX_INCLUSIVE)
        BigDecimal max = xmlD != null ? xmlD.getBigDecimalValue() : null
        boolean minInclusive = true, maxInclusive = true
        xmlD = (XmlDecimal) sType.getFacet(SchemaType.FACET_MIN_EXCLUSIVE)
        if (xmlD != null) {
            BigDecimal minExcl = xmlD.getBigDecimalValue()
            if (min == null || min.compareTo(minExcl) < 0) {
                min = minExcl
                minInclusive = false
            }
        }
        xmlD = (XmlDecimal) sType.getFacet(SchemaType.FACET_MAX_EXCLUSIVE)
        if (xmlD != null) {
            BigDecimal maxExcl = xmlD.getBigDecimalValue()
            if (max == null || max.compareTo(maxExcl) > 0) {
                max = maxExcl
                maxInclusive = false
            }
        }
        xmlD = (XmlDecimal) sType.getFacet(SchemaType.FACET_TOTAL_DIGITS)
        int totalDigits = -1
        if (xmlD != null) {
            totalDigits = xmlD.getBigDecimalValue().intValue()

            StringBuffer sb = new StringBuffer(totalDigits)
            for (int i = 0; i < totalDigits; i++)
                sb.append('9')
            BigDecimal digitsLimit = new BigDecimal(sb.toString())
            if (max != null && max.compareTo(digitsLimit) > 0) {
                max = digitsLimit
                maxInclusive = true
            }
            digitsLimit = digitsLimit.negate()
            if (min != null && min.compareTo(digitsLimit) < 0) {
                min = digitsLimit
                minInclusive = true
            }
        }

        int sigMin = min == null ? 1 : result.compareTo(min)
        int sigMax = max == null ? -1 : result.compareTo(max)
        boolean minOk = sigMin > 0 || sigMin == 0 && minInclusive
        boolean maxOk = sigMax < 0 || sigMax == 0 && maxInclusive

        // Compute the minimum increment
        xmlD = (XmlDecimal) sType.getFacet(SchemaType.FACET_FRACTION_DIGITS)
        int fractionDigits = -1
        BigDecimal increment
        if (xmlD == null)
            increment = new BigDecimal(1)
        else {
            fractionDigits = xmlD.getBigDecimalValue().intValue()
            if (fractionDigits > 0) {
                StringBuffer sb = new StringBuffer("0.")
                for (int i = 1; i < fractionDigits; i++)
                    sb.append('0')
                sb.append('1')
                increment = new BigDecimal(sb.toString())
            } else
                increment = new BigDecimal(1.0)
        }

        if (minOk && maxOk) {
            // OK
        } else if (minOk && !maxOk) {
            // TOO BIG
            if (maxInclusive)
                result = max
            else
                result = max.subtract(increment)
        } else if (!minOk && maxOk) {
            // TOO SMALL
            if (minInclusive)
                result = min
            else
                result = min.add(increment)
        } else {
            // MIN > MAX!!
        }

        // We have the number
        // Adjust the scale according to the totalDigits and fractionDigits
        int digits = 0
        BigDecimal ONE = new BigDecimal(BigInteger.ONE)
        for (BigDecimal n = result; n.abs().compareTo(ONE) >= 0; digits++)
            n = n.movePointLeft(1)

        if (fractionDigits > 0)
            if (totalDigits >= 0)
                result = result.setScale(Math.max(fractionDigits, totalDigits - digits))
            else
                result = result.setScale(fractionDigits)
        else if (fractionDigits == 0)
            result = result.setScale(0)

        return result.toString()
    }

    private String formatDuration(SchemaType sType) {
        XmlDuration d =
                (XmlDuration) sType.getFacet(SchemaType.FACET_MIN_INCLUSIVE)
        GDuration minInclusive = null
        if (d != null)
            minInclusive = d.getGDurationValue()

        d = (XmlDuration) sType.getFacet(SchemaType.FACET_MAX_INCLUSIVE)
        GDuration maxInclusive = null
        if (d != null)
            maxInclusive = d.getGDurationValue()

        d = (XmlDuration) sType.getFacet(SchemaType.FACET_MIN_EXCLUSIVE)
        GDuration minExclusive = null
        if (d != null)
            minExclusive = d.getGDurationValue()

        d = (XmlDuration) sType.getFacet(SchemaType.FACET_MAX_EXCLUSIVE)
        GDuration maxExclusive = null
        if (d != null)
            maxExclusive = d.getGDurationValue()

        GDurationBuilder gdurb = new GDurationBuilder()
        BigInteger min, max

        gdurb.setSecond(pick(800000))
        gdurb.setMonth(pick(20))

        // Years
        // Months
        // Days
        // Hours
        // Minutes
        // Seconds
        // Fractions
        if (minInclusive != null) {
            if (gdurb.getYear() < minInclusive.getYear())
                gdurb.setYear(minInclusive.getYear())
            if (gdurb.getMonth() < minInclusive.getMonth())
                gdurb.setMonth(minInclusive.getMonth())
            if (gdurb.getDay() < minInclusive.getDay())
                gdurb.setDay(minInclusive.getDay())
            if (gdurb.getHour() < minInclusive.getHour())
                gdurb.setHour(minInclusive.getHour())
            if (gdurb.getMinute() < minInclusive.getMinute())
                gdurb.setMinute(minInclusive.getMinute())
            if (gdurb.getSecond() < minInclusive.getSecond())
                gdurb.setSecond(minInclusive.getSecond())
            if (gdurb.getFraction().compareTo(minInclusive.getFraction()) < 0)
                gdurb.setFraction(minInclusive.getFraction())
        }

        if (maxInclusive != null) {
            if (gdurb.getYear() > maxInclusive.getYear())
                gdurb.setYear(maxInclusive.getYear())
            if (gdurb.getMonth() > maxInclusive.getMonth())
                gdurb.setMonth(maxInclusive.getMonth())
            if (gdurb.getDay() > maxInclusive.getDay())
                gdurb.setDay(maxInclusive.getDay())
            if (gdurb.getHour() > maxInclusive.getHour())
                gdurb.setHour(maxInclusive.getHour())
            if (gdurb.getMinute() > maxInclusive.getMinute())
                gdurb.setMinute(maxInclusive.getMinute())
            if (gdurb.getSecond() > maxInclusive.getSecond())
                gdurb.setSecond(maxInclusive.getSecond())
            if (gdurb.getFraction().compareTo(maxInclusive.getFraction()) > 0)
                gdurb.setFraction(maxInclusive.getFraction())
        }

        if (minExclusive != null) {
            if (gdurb.getYear() <= minExclusive.getYear())
                gdurb.setYear(minExclusive.getYear() + 1)
            if (gdurb.getMonth() <= minExclusive.getMonth())
                gdurb.setMonth(minExclusive.getMonth() + 1)
            if (gdurb.getDay() <= minExclusive.getDay())
                gdurb.setDay(minExclusive.getDay() + 1)
            if (gdurb.getHour() <= minExclusive.getHour())
                gdurb.setHour(minExclusive.getHour() + 1)
            if (gdurb.getMinute() <= minExclusive.getMinute())
                gdurb.setMinute(minExclusive.getMinute() + 1)
            if (gdurb.getSecond() <= minExclusive.getSecond())
                gdurb.setSecond(minExclusive.getSecond() + 1)
            if (gdurb.getFraction().compareTo(minExclusive.getFraction()) <= 0)
                gdurb.setFraction(minExclusive.getFraction().add(new BigDecimal(0.001)))
        }

        if (maxExclusive != null) {
            if (gdurb.getYear() > maxExclusive.getYear())
                gdurb.setYear(maxExclusive.getYear())
            if (gdurb.getMonth() > maxExclusive.getMonth())
                gdurb.setMonth(maxExclusive.getMonth())
            if (gdurb.getDay() > maxExclusive.getDay())
                gdurb.setDay(maxExclusive.getDay())
            if (gdurb.getHour() > maxExclusive.getHour())
                gdurb.setHour(maxExclusive.getHour())
            if (gdurb.getMinute() > maxExclusive.getMinute())
                gdurb.setMinute(maxExclusive.getMinute())
            if (gdurb.getSecond() > maxExclusive.getSecond())
                gdurb.setSecond(maxExclusive.getSecond())
            if (gdurb.getFraction().compareTo(maxExclusive.getFraction()) > 0)
                gdurb.setFraction(maxExclusive.getFraction())
        }

        gdurb.normalize()
        return gdurb.toString()
    }

    private String formatDate(SchemaType sType) {
        GDateBuilder gdateb = new GDateBuilder(new Date(1000L * pick(365 * 24 * 60 * 60) + (30L + pick(20)) * 365 * 24 * 60 * 60 * 1000))
        GDate min = null, max = null
        GDate temp

        // Find the min and the max according to the type
        switch (sType.getPrimitiveType().getBuiltinTypeCode()) {
            case SchemaType.BTC_DATE_TIME:
                XmlDateTime x = (XmlDateTime) sType.getFacet(SchemaType.FACET_MIN_INCLUSIVE)
                if (x != null)
                    min = x.getGDateValue()
                x = (XmlDateTime) sType.getFacet(SchemaType.FACET_MIN_EXCLUSIVE)
                if (x != null)
                    if (min == null || min.compareToGDate(x.getGDateValue()) <= 0)
                        min = x.getGDateValue()

                x = (XmlDateTime) sType.getFacet(SchemaType.FACET_MAX_INCLUSIVE)
                if (x != null)
                    max = x.getGDateValue()
                x = (XmlDateTime) sType.getFacet(SchemaType.FACET_MAX_EXCLUSIVE)
                if (x != null)
                    if (max == null || max.compareToGDate(x.getGDateValue()) >= 0)
                        max = x.getGDateValue()
                break
            case SchemaType.BTC_TIME:
                XmlTime x = (XmlTime) sType.getFacet(SchemaType.FACET_MIN_INCLUSIVE)
                if (x != null)
                    min = x.getGDateValue()
                x = (XmlTime) sType.getFacet(SchemaType.FACET_MIN_EXCLUSIVE)
                if (x != null)
                    if (min == null || min.compareToGDate(x.getGDateValue()) <= 0)
                        min = x.getGDateValue()

                x = (XmlTime) sType.getFacet(SchemaType.FACET_MAX_INCLUSIVE)
                if (x != null)
                    max = x.getGDateValue()
                x = (XmlTime) sType.getFacet(SchemaType.FACET_MAX_EXCLUSIVE)
                if (x != null)
                    if (max == null || max.compareToGDate(x.getGDateValue()) >= 0)
                        max = x.getGDateValue()
                break
            case SchemaType.BTC_DATE:
                XmlDate x = (XmlDate) sType.getFacet(SchemaType.FACET_MIN_INCLUSIVE)
                if (x != null)
                    min = x.getGDateValue()
                x = (XmlDate) sType.getFacet(SchemaType.FACET_MIN_EXCLUSIVE)
                if (x != null)
                    if (min == null || min.compareToGDate(x.getGDateValue()) <= 0)
                        min = x.getGDateValue()

                x = (XmlDate) sType.getFacet(SchemaType.FACET_MAX_INCLUSIVE)
                if (x != null)
                    max = x.getGDateValue()
                x = (XmlDate) sType.getFacet(SchemaType.FACET_MAX_EXCLUSIVE)
                if (x != null)
                    if (max == null || max.compareToGDate(x.getGDateValue()) >= 0)
                        max = x.getGDateValue()
                break
            case SchemaType.BTC_G_YEAR_MONTH:
                XmlGYearMonth x = (XmlGYearMonth) sType.getFacet(SchemaType.FACET_MIN_INCLUSIVE)
                if (x != null)
                    min = x.getGDateValue()
                x = (XmlGYearMonth) sType.getFacet(SchemaType.FACET_MIN_EXCLUSIVE)
                if (x != null)
                    if (min == null || min.compareToGDate(x.getGDateValue()) <= 0)
                        min = x.getGDateValue()

                x = (XmlGYearMonth) sType.getFacet(SchemaType.FACET_MAX_INCLUSIVE)
                if (x != null)
                    max = x.getGDateValue()
                x = (XmlGYearMonth) sType.getFacet(SchemaType.FACET_MAX_EXCLUSIVE)
                if (x != null)
                    if (max == null || max.compareToGDate(x.getGDateValue()) >= 0)
                        max = x.getGDateValue()
                break
            case SchemaType.BTC_G_YEAR:
                XmlGYear x = (XmlGYear) sType.getFacet(SchemaType.FACET_MIN_INCLUSIVE)
                if (x != null)
                    min = x.getGDateValue()
                x = (XmlGYear) sType.getFacet(SchemaType.FACET_MIN_EXCLUSIVE)
                if (x != null)
                    if (min == null || min.compareToGDate(x.getGDateValue()) <= 0)
                        min = x.getGDateValue()

                x = (XmlGYear) sType.getFacet(SchemaType.FACET_MAX_INCLUSIVE)
                if (x != null)
                    max = x.getGDateValue()
                x = (XmlGYear) sType.getFacet(SchemaType.FACET_MAX_EXCLUSIVE)
                if (x != null)
                    if (max == null || max.compareToGDate(x.getGDateValue()) >= 0)
                        max = x.getGDateValue()
                break
            case SchemaType.BTC_G_MONTH_DAY:
                XmlGMonthDay x = (XmlGMonthDay) sType.getFacet(SchemaType.FACET_MIN_INCLUSIVE)
                if (x != null)
                    min = x.getGDateValue()
                x = (XmlGMonthDay) sType.getFacet(SchemaType.FACET_MIN_EXCLUSIVE)
                if (x != null)
                    if (min == null || min.compareToGDate(x.getGDateValue()) <= 0)
                        min = x.getGDateValue()

                x = (XmlGMonthDay) sType.getFacet(SchemaType.FACET_MAX_INCLUSIVE)
                if (x != null)
                    max = x.getGDateValue()
                x = (XmlGMonthDay) sType.getFacet(SchemaType.FACET_MAX_EXCLUSIVE)
                if (x != null)
                    if (max == null || max.compareToGDate(x.getGDateValue()) >= 0)
                        max = x.getGDateValue()
                break
            case SchemaType.BTC_G_DAY:
                XmlGDay x = (XmlGDay) sType.getFacet(SchemaType.FACET_MIN_INCLUSIVE)
                if (x != null)
                    min = x.getGDateValue()
                x = (XmlGDay) sType.getFacet(SchemaType.FACET_MIN_EXCLUSIVE)
                if (x != null)
                    if (min == null || min.compareToGDate(x.getGDateValue()) <= 0)
                        min = x.getGDateValue()

                x = (XmlGDay) sType.getFacet(SchemaType.FACET_MAX_INCLUSIVE)
                if (x != null)
                    max = x.getGDateValue()
                x = (XmlGDay) sType.getFacet(SchemaType.FACET_MAX_EXCLUSIVE)
                if (x != null)
                    if (max == null || max.compareToGDate(x.getGDateValue()) >= 0)
                        max = x.getGDateValue()
                break
            case SchemaType.BTC_G_MONTH:
                XmlGMonth x = (XmlGMonth) sType.getFacet(SchemaType.FACET_MIN_INCLUSIVE)
                if (x != null)
                    min = x.getGDateValue()
                x = (XmlGMonth) sType.getFacet(SchemaType.FACET_MIN_EXCLUSIVE)
                if (x != null)
                    if (min == null || min.compareToGDate(x.getGDateValue()) <= 0)
                        min = x.getGDateValue()

                x = (XmlGMonth) sType.getFacet(SchemaType.FACET_MAX_INCLUSIVE)
                if (x != null)
                    max = x.getGDateValue()
                x = (XmlGMonth) sType.getFacet(SchemaType.FACET_MAX_EXCLUSIVE)
                if (x != null)
                    if (max == null || max.compareToGDate(x.getGDateValue()) >= 0)
                        max = x.getGDateValue()
                break
        }

        if (min != null && max == null) {
            if (min.compareToGDate(gdateb) >= 0) {
                // Reset the date to min + (1-8) hours
                Calendar c = gdateb.getCalendar()
                c.add(Calendar.HOUR_OF_DAY, pick(8))
                gdateb = new GDateBuilder(c)
            }
        } else if (min == null && max != null) {
            if (max.compareToGDate(gdateb) <= 0) {
                // Reset the date to max - (1-8) hours
                Calendar c = gdateb.getCalendar()
                c.add(Calendar.HOUR_OF_DAY, 0 - pick(8))
                gdateb = new GDateBuilder(c)
            }
        } else if (min != null && max != null) {
            if (min.compareToGDate(gdateb) >= 0 || max.compareToGDate(gdateb) <= 0) {
                // Find a date between the two
                Calendar c = min.getCalendar()
                Calendar cmax = max.getCalendar()
                c.add(Calendar.HOUR_OF_DAY, 1)
                if (c.after(cmax)) {
                    c.add(Calendar.HOUR_OF_DAY, -1)
                    c.add(Calendar.MINUTE, 1)
                    if (c.after(cmax)) {
                        c.add(Calendar.MINUTE, -1)
                        c.add(Calendar.SECOND, 1)
                        if (c.after(cmax)) {
                            c.add(Calendar.SECOND, -1)
                            c.add(Calendar.MILLISECOND, 1)
                            if (c.after(cmax))
                                c.add(Calendar.MILLISECOND, -1)
                        }
                    }
                }
                gdateb = new GDateBuilder(c)
            }
        }

        gdateb.setBuiltinTypeCode(sType.getPrimitiveType().getBuiltinTypeCode())
        if (pick(2) == 0)
            gdateb.clearTimeZone()
        return gdateb.toString()
    }

    private SchemaType closestBuiltin(SchemaType sType) {
        while (!sType.isBuiltinType())
            sType = sType.getBaseType()
        return sType
    }

    /**
     * Cracks a combined QName of the form URL:localname
     */
    static QName crackQName(String qName) {
        String ns
        String name

        int index = qName.lastIndexOf(':')
        if (index >= 0) {
            ns = qName.substring(0, index)
            name = qName.substring(index + 1)
        } else {
            ns = ""
            name = qName
        }

        return new QName(ns, name)
    }
}
