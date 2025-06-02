package com.rxlogix.dynamicReports

import com.rxlogix.enums.ReportFormatEnum
import com.rxlogix.enums.ReportThemeEnum
import net.sf.dynamicreports.report.base.expression.AbstractValueFormatter
import net.sf.dynamicreports.report.builder.MarginBuilder
import net.sf.dynamicreports.report.builder.ReportTemplateBuilder
import net.sf.dynamicreports.report.builder.component.ComponentBuilder
import net.sf.dynamicreports.report.builder.datatype.BigDecimalType
import net.sf.dynamicreports.report.builder.style.FontBuilder
import net.sf.dynamicreports.report.builder.style.PenBuilder
import net.sf.dynamicreports.report.builder.style.StyleBuilder
import net.sf.dynamicreports.report.builder.tableofcontents.TableOfContentsCustomizerBuilder
import net.sf.dynamicreports.report.constant.*
import net.sf.dynamicreports.report.definition.ReportParameters
import org.springframework.context.i18n.LocaleContextHolder

import java.awt.*

import static net.sf.dynamicreports.report.builder.DynamicReports.*

public class Templates {
    public static final StyleBuilder rootStyle
    public static final StyleBuilder boldStyle
    public static final StyleBuilder italicStyle
    public static final StyleBuilder boldCenteredStyle
    public static final StyleBuilder bold14CenteredStyle
    public static final StyleBuilder criteriaSectionTitleStyle
    public static final StyleBuilder criteriaNameStyle
    public static final StyleBuilder criteriaValueStyle
    public static final StyleBuilder columnStyle
    public static final StyleBuilder columnHeaderStyle
    public static final StyleBuilder columnTitleStyle
    public static final StyleBuilder columnStyleXLSX
    public static final StyleBuilder columnStyleMarkupHTML
    public static final StyleBuilder horizontalListColumnTitleStyle
    public static final StyleBuilder subtotalStyle

    public static final StyleBuilder pageHeader_HeaderStyle
    public static final StyleBuilder pageHeader_ReportHeaderStyle
    public static final StyleBuilder pageHeader_TitleStyle
    public static final StyleBuilder pageHeader_DateRangeStyle
    public static final StyleBuilder pageFooterStyle
    public static final StyleBuilder groupStyle
    public static final StyleBuilder groupTitleStyle
    public static final StyleBuilder groupHeaderStyle
    public static final StyleBuilder groupFooterStyle
    public static final StyleBuilder emptyPaddingStyle

    public static final StyleBuilder subReportPageHeaderStyle
    public static subTotalBackgroundColor

    public static final StyleBuilder colspanStyle

    public static final ComponentBuilder<?, ?> pageNumberingComponent
    public static final PenBuilder horizontalLine
    public static final PenBuilder horizontalThickLine
    public static final PenBuilder columnBorderLine
    public static final PenBuilder emptyBorderLine
    public static final ReportTemplateBuilder reportTemplate
    public static final CurrencyType currencyType
    public static final TableOfContentsCustomizerBuilder tableOfContentsCustomizer

    // Custom colors
    public static final grey = new Color(220, 220, 220)
    public static final darkGrey = new Color(180, 180, 180)
    //public static final orange = new Color(254, 207, 127)
    //public static final darkOrange = new Color(254, 170, 34)
    //public static final subTotalOrange = new Color(242, 235, 220)
    //public static final lightBlack = new Color(51, 51, 51)
    //public static final lightGrey = new Color(242, 242, 242)
    //public static final blue = new Color(0, 0, 128)
    public static final watermarkGrey = new Color(232, 231, 230)
    public static final cyanGrey = new Color(190, 190, 190)

    public static final String DEFAULT_FONT_NAME = "Arial Unicode"

    static {
        def formatParameter = CustomReportParameters.outputFormat.jasperValue.expression
        def isHtmlOutputExp = exp.jasperSyntax("${formatParameter} == null || \"${ReportFormatEnum.HTML.name()}\".equals(${formatParameter})")
        def isXlsxOutputExp = exp.jasperSyntax("\"${ReportFormatEnum.XLSX.name()}\".equals(${formatParameter})")
        def isOtherOutputExp = exp.jasperSyntax("!(${isHtmlOutputExp.expression} || ${isXlsxOutputExp.expression})")

        def htmlConditionalStyle = stl.conditionalStyle(isHtmlOutputExp)
                .setFontSize(14)

        def othersConditionalStyle = stl.conditionalStyle(isOtherOutputExp)
                .setPadding(5)

        rootStyle = stl.style()
                //.setName("rootStyle")
                .setFontSize(9)
                .addConditionalStyle(htmlConditionalStyle)
                .addConditionalStyle(othersConditionalStyle)

        boldStyle = stl.style(rootStyle)
                //.setName("boldStyle")
                .bold()

        italicStyle = stl.style(rootStyle)
                .setName("italicStyle")
                .italic()
        boldCenteredStyle = stl.style(boldStyle).setTextAlignment(HorizontalTextAlignment.CENTER, VerticalTextAlignment.MIDDLE)
        bold14CenteredStyle = stl.style(boldCenteredStyle).setFontSize(14)
        columnBorderLine = stl.pen1Point().setLineWidth(0.5 as Float).setLineColor(grey)
        emptyBorderLine = stl.pen1Point().setLineWidth(0.0 as Float)

        criteriaValueStyle = stl.style(rootStyle)
                .setName("criteriaValueStyle")
                .setTextAlignment(HorizontalTextAlignment.LEFT, VerticalTextAlignment.TOP)
                .setPadding(1)
                .setLineSpacing(LineSpacing.SINGLE)
                .setFont(getDefaultFontStyle())
        criteriaNameStyle = stl.style(criteriaValueStyle)
                .setName("criteriaNameStyle")
                .setBold(true)

        criteriaSectionTitleStyle = stl.style(criteriaNameStyle)
                .setName("criteriaSectionTitleStyle")
                .setBold(true)
                .setFontSize(10)

        columnStyle = stl.style(rootStyle)
                .setName("columnStyle")
                .setVerticalTextAlignment(VerticalTextAlignment.TOP)
                .setTopBorder(columnBorderLine)
                .setBottomBorder(columnBorderLine)
                .setLeftPadding(1)
                .setRightPadding(1)
                .setLineSpacing(LineSpacing.SINGLE)
                .setTextAlignment(HorizontalTextAlignment.LEFT, VerticalTextAlignment.TOP)

        columnTitleStyle = stl.style(rootStyle)
                .setName("columnTitleStyle")
                .setBold(true)
                //.setForegroundColor(lightBlack)
                //.setBackgroundColor(orange)
                .setTextAlignment(HorizontalTextAlignment.LEFT, VerticalTextAlignment.TOP)
                .setPadding(1)
                .setLineSpacing(LineSpacing.SINGLE)

        columnHeaderStyle = stl.style()
                .setName("columnHeaderStyle")
                //.setBackgroundColor(orange)

        columnStyleXLSX = stl.style()
                .setName("columnStyleXLSX")
                .setPadding(4)
                .setTextAlignment(HorizontalTextAlignment.LEFT, VerticalTextAlignment.TOP)

        columnStyleMarkupHTML = stl.style(columnStyleXLSX)
                .setName("columnStyleMarkupHTML")
                .setMarkup(Markup.HTML)

        horizontalListColumnTitleStyle = stl.style(columnTitleStyle)
                .setName("horizontalListColumnTitleStyle")
                .setTextAlignment(HorizontalTextAlignment.LEFT, VerticalTextAlignment.TOP)

        groupStyle = stl.style(boldStyle)
                .setName("groupStyle")
                .setHorizontalTextAlignment(HorizontalTextAlignment.LEFT)

        groupTitleStyle = stl.style(boldStyle)
                .setName("groupTitleStyle")

        groupHeaderStyle = stl.style()
                .setName("groupHeaderStyle")
                .setTopPadding(2)
        groupFooterStyle = stl.style()
                .setName("groupFooterStyle")
                .setBottomPadding(0)
        emptyPaddingStyle = stl.style()
                .setName("emptyPaddingStyle")
                .setPadding(0)
        subtotalStyle = stl.style(boldStyle)
                .setName("subtotalStyle")
                .setTopBorder(stl.pen1Point())

        pageHeader_HeaderStyle = stl.style(boldStyle)
                .setName("pageHeader_HeaderStyle")
                .setFontSize(10)
                .setBottomPadding(5)
                .setTextAlignment(HorizontalTextAlignment.CENTER, VerticalTextAlignment.MIDDLE)

        pageHeader_ReportHeaderStyle = stl.style(boldStyle)
                .setName("pageHeader_ReportHeaderStyle")
                .setFontSize(12)
                .setBottomPadding(5)
                .setTextAlignment(HorizontalTextAlignment.CENTER, VerticalTextAlignment.MIDDLE)
                .addConditionalStyle(stl.conditionalStyle(isHtmlOutputExp)
                    .setFontSize(16))

        pageHeader_TitleStyle = stl.style(boldStyle)
                .setName("pageHeader_TitleStyle")
                .setFontSize(10)
                .setBottomPadding(5)
                .setTextAlignment(HorizontalTextAlignment.CENTER, VerticalTextAlignment.MIDDLE)
                .addConditionalStyle(stl.conditionalStyle(isHtmlOutputExp)
                .setFontSize(16))
        pageHeader_DateRangeStyle = stl.style(boldStyle)
                .setName("pageHeader_DateRangeStyle")
                .setFontSize(9)
                .setBottomPadding(5)
                .setTextAlignment(HorizontalTextAlignment.CENTER, VerticalTextAlignment.MIDDLE)

        pageFooterStyle = stl.style(rootStyle)
                //.setName("pageFooterStyle")

        tableOfContentsCustomizer = tableOfContentsCustomizer().setHeadingStyle(0, stl.style(boldStyle))

        horizontalLine = stl.pen1Point().setLineColor(darkGrey).setLineWidth(0.5 as Float)
        horizontalThickLine = stl.pen1Point().setLineColor(cyanGrey).setLineWidth(1.0 as Float)

        colspanStyle = stl.style(rootStyle)
                .setName("colspanStyle")
                .setHorizontalTextAlignment(HorizontalTextAlignment.LEFT)
                .setLeftPadding(40)

        // Custom numbering component for correct using in fixed Jasper templates through API
        pageNumberingComponent = createPageNumberComponent()

        currencyType = new CurrencyType()

        subReportPageHeaderStyle = stl.style(pageHeader_HeaderStyle)
                .setName("subReportPageHeaderStyle")
                .setFontSize(9)
                .setHorizontalTextAlignment(HorizontalTextAlignment.LEFT)
               // .setBackgroundColor(lightGrey)
                .setPadding(2)

        reportTemplate = template()
        //todo:  this locale cannot be hardcoded
                .setLocale(Locale.ENGLISH)
                .setPageFormat(PageType.LETTER, PageOrientation.LANDSCAPE) //11x8.5 inches, 72px/inch
                .setPageMargin(getPageMargin(PageOrientation.LANDSCAPE))
                .setDefaultFont(defaultFont.setFontSize(12))
                .setGroupHeaderStyle(groupHeaderStyle)
                .setGroupFooterStyle(groupFooterStyle)
                .setSubtotalStyle(subtotalStyle)
                .crosstabHighlightEvenRows()
                .setTableOfContentsCustomizer(tableOfContentsCustomizer)
        applyDefaultTheme()
    }

    public static MarginBuilder getPageMargin(PageOrientation pageOrientation) {
        if (pageOrientation == PageOrientation.LANDSCAPE) {
            return margin().setLeft(27).setBottom(27).setRight(27).setTop(15)// 3/8, 3/8, 3/8, 3/4 inches
        } else {
            return margin().setLeft(54).setBottom(27).setRight(27).setTop(27)// 3/4, 3/8, 3/8, 3/8 inches
        }
    }

    private static FontBuilder getDefaultFont() {
        return FontBuilder.newInstance().setFontName(DEFAULT_FONT_NAME)
    }

    public static FontBuilder getDefaultFontStyle() {
        return getDefaultFont().setFontSize(9)
    }

    public static FontBuilder getDefaultFontStyleHTML() {
        return getDefaultFont().setFontSize(14)
    }

    public static CurrencyValueFormatter createCurrencyValueFormatter(String label) {
        return new CurrencyValueFormatter(label)
    }

    public static FontBuilder getWatermarkFont() {
        return getDefaultFont().setFontSize(120).setBold(true)
    }

    public static class CurrencyType extends BigDecimalType {
        private static final long serialVersionUID = 1L

        @Override
        public String getPattern() {
            return "\$ #,###.00"
        }
    }

    private static createPageNumberComponent() {
        def currentPageNumberingStyle = stl.style(pageFooterStyle)
                .setName("currentPageNumberingStyle")
                .setTextAlignment(HorizontalTextAlignment.RIGHT, VerticalTextAlignment.MIDDLE)
                .setPadding(0)
        def currentPageComponent = cmp.text(exp.jasperSyntax("\"Page \" + \$V{PAGE_NUMBER}"))
                .setEvaluationTime(Evaluation.PAGE)
                .setStyle(currentPageNumberingStyle)
        def totalPagesNumberingStyle = stl.style(pageFooterStyle)
                .setName("totalPagesNumberingStyle")
                .setTextAlignment(HorizontalTextAlignment.LEFT, VerticalTextAlignment.MIDDLE)
                .setPadding(0)
        def totalPagesComponent = cmp.text(exp.jasperSyntax("\" of \" + \$V{PAGE_NUMBER}"))
                .setEvaluationTime(Evaluation.REPORT)
                .setStyle(totalPagesNumberingStyle)
        return cmp.horizontalList(currentPageComponent, totalPagesComponent)
    }

    public static void applyTheme(String name) {
        ReportThemeEnum theme = ReportThemeEnum.searchByName(name)
        if (!theme) {
            applyDefaultTheme()
        } else {
            applyThemeInternal(theme)
        }
    }

    public static void applyDefaultTheme() {
        applyThemeInternal(ReportThemeEnum.GRADIENT_BLUE)
    }

    private static void applyThemeInternal(ReportThemeEnum theme) {
        // Set background color
        columnTitleStyle.setBackgroundColor(theme.columnHeaderBackgroundColor)
        columnHeaderStyle.setBackgroundColor(theme.columnHeaderBackgroundColor)
        // Set foreground color
        columnTitleStyle.setForegroundColor(theme.columnHeaderForegroundColor)
        subTotalBackgroundColor = theme.subTotalBackgroundColor
    }

    private static class CurrencyValueFormatter extends AbstractValueFormatter<String, Number> {
        private static final long serialVersionUID = 1L

        private String label

        public CurrencyValueFormatter(String label) {
            this.label = label
        }

        @Override
        public String format(Number value, ReportParameters reportParameters) {
            return label + currencyType.valueToString(value, reportParameters.getLocale())
        }
    }
}