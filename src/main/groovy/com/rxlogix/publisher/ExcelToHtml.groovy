/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
package com.rxlogix.publisher

import groovy.transform.CompileStatic
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.openxml4j.exceptions.InvalidFormatException
import org.apache.poi.ss.format.CellFormat
import org.apache.poi.ss.format.CellFormatResult
import org.apache.poi.ss.usermodel.*
import org.apache.poi.util.IOUtils
import org.apache.poi.xssf.usermodel.XSSFWorkbook

/**
 * This example shows how to display a spreadsheet in HTML using the classes for
 * spreadsheet display.
 */

@CompileStatic
public class ExcelToHtml {
    private final Workbook wb;
    private final Appendable output;
    private boolean completeHTML;
    private Formatter out;
    private boolean gotBounds;
    private int firstColumn;
    private int endColumn;
    private HtmlHelper helper;
    private int sheetNumber = 0;


    private static final String DEFAULTS_CLASS = "excelDefaults";
    private static final String COL_HEAD_CLASS = "colHeader";
    private static final String ROW_HEAD_CLASS = "rowHeader";

    private static final Map<HorizontalAlignment, String> HALIGN = mapFor(
            HorizontalAlignment.LEFT, "left",
            HorizontalAlignment.CENTER, "center",
            HorizontalAlignment.RIGHT, "right",
            HorizontalAlignment.FILL, "left",
            HorizontalAlignment.JUSTIFY, "left",
            HorizontalAlignment.CENTER_SELECTION, "center");

    private static final Map<VerticalAlignment, String> VALIGN = mapFor(
            VerticalAlignment.BOTTOM, "bottom",
            VerticalAlignment.CENTER, "middle",
            VerticalAlignment.TOP, "top");

    private static final Map<BorderStyle, String> BORDER = mapFor(
            BorderStyle.DASH_DOT, "dashed 1pt",
            BorderStyle.DASH_DOT_DOT, "dashed 1pt",
            BorderStyle.DASHED, "dashed 1pt",
            BorderStyle.DOTTED, "dotted 1pt",
            BorderStyle.DOUBLE, "double 3pt",
            BorderStyle.HAIR, "solid 1px",
            BorderStyle.MEDIUM, "solid 2pt",
            BorderStyle.MEDIUM_DASH_DOT, "dashed 2pt",
            BorderStyle.MEDIUM_DASH_DOT_DOT, "dashed 2pt",
            BorderStyle.MEDIUM_DASHED, "dashed 2pt",
            BorderStyle.NONE, "none",
            BorderStyle.SLANTED_DASH_DOT, "dashed 2pt",
            BorderStyle.THICK, "solid 3pt",
            BorderStyle.THIN, "dashed 1pt");


    private static <K, V> Map<K, V> mapFor(Object... mapping) {
        Map<K, V> map = new HashMap<K, V>();
        for (int i = 0; i < mapping.length; i += 2) {
            map.put((K) mapping[i], (V) mapping[i + 1]);
        }
        return map;
    }

    /**
     * Creates a new converter to HTML for the given workbook.
     *
     * @param wb The workbook.
     * @param output Where the HTML output will be written.
     *
     * @return An object for converting the workbook to HTML.
     */
    public static ExcelToHtml create(Workbook wb, Appendable output) {
        return new ExcelToHtml(wb, output);
    }

    /**
     * Creates a new converter to HTML for the given workbook.  If the path ends
     * with "<tt>.xlsx</tt>" an {@link org.apache.poi.xssf.usermodel.XSSFWorkbook} will be used; otherwise
     * this will use an {@link org.apache.poi.hssf.usermodel.HSSFWorkbook}.
     *
     * @param path The file that has the workbook.
     * @param output Where the HTML output will be written.
     *
     * @return An object for converting the workbook to HTML.
     */
    public static ExcelToHtml create(String path, Appendable output)
            throws IOException {
        return create(new FileInputStream(path), output);
    }

    /**
     * Creates a new converter to HTML for the given workbook.  This attempts to
     * detect whether the input is XML (so it should create an {@link
     * XSSFWorkbook} or not (so it should create an {@link org.apache.poi.hssf.usermodel.HSSFWorkbook}).
     *
     * @param in The input stream that has the workbook.
     * @param output Where the HTML output will be written.
     *
     * @return An object for converting the workbook to HTML.
     */
    public static ExcelToHtml create(InputStream _in, Appendable output)
            throws IOException {
        try {
            Workbook wb = WorkbookFactory.create(_in);
            return create(wb, output);
        } catch (InvalidFormatException e) {
            throw new IllegalArgumentException("Cannot create workbook from stream", e);
        }
    }

    private ExcelToHtml(Workbook wb, Appendable output) {
        if (wb == null) {
            throw new NullPointerException("wb");
        }
        if (output == null) {
            throw new NullPointerException("output");
        }
        this.wb = wb;
        this.output = output;
        setupColorMap();
    }

    private void setupColorMap() {
        if (wb instanceof HSSFWorkbook) {
            helper = new HSSFHtmlHelper((HSSFWorkbook) wb);
        } else if (wb instanceof XSSFWorkbook) {
            helper = new XSSFHtmlHelper();
        } else {
            throw new IllegalArgumentException(
                    "unknown workbook type: " + wb.getClass().getSimpleName());
        }
    }

    /**
     * Run this class as a program
     *
     * @param args The command line arguments.
     *
     * @throws Exception Exception we don't recover from.
     */
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("usage: ExcelToHtml inputWorkbook outputHtmlFile");
            return;
        }

        ExcelToHtml toHtml = create(args[0], new PrintWriter(new FileWriter(args[1])));
        toHtml.setCompleteHTML(true);
        toHtml.printPage();
    }

    public void setCompleteHTML(boolean completeHTML) {
        this.completeHTML = completeHTML;
    }

    public void printPage() throws IOException {
        try {
            ensureOut();
            if (completeHTML) {
                out.format(
                        "<?xml version=\"1.0\" encoding=\"iso-8859-1\" ?>%n");
                out.format("<html>%n");
                out.format("<head>%n");
            }
            printInlineStyle();
            if (completeHTML) {
                out.format("</head>%n");
                out.format("<body>%n");
            }
            printSheets();
            if (completeHTML) {
                out.format("</body>%n");
                out.format("</html>%n");
            }
        } finally {
            IOUtils.closeQuietly(out);
            if (output instanceof Closeable) {
                IOUtils.closeQuietly((Closeable) output);
            }
        }
    }

    public int getSheetNumber() {
        return sheetNumber
    }

    public void setSheetNumber(int sheetNumber) {
        this.sheetNumber = sheetNumber
    }

    private void printInlineStyle() {
        //out.format("<link href=\"excelStyle.css\" rel=\"stylesheet\" type=\"text/css\">%n");
        out.format("<style type=\"text/css\">%n");
        printStyles();
        out.format("</style>%n");
    }

    private void ensureOut() {
        if (out == null) {
            out = new Formatter(output);
        }
    }

    public void printStyles() {
        ensureOut();

        // First, copy the base css
        BufferedReader _in = null;
        try {
            _in = new BufferedReader(new InputStreamReader(
                    getClass().getResourceAsStream("/excelStyle.css")));
            String line;
            while ((line = _in.readLine()) != null) {
                out.format("%s%n", line);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Reading standard css", e);
        } finally {
            IOUtils.closeQuietly(_in);
        }

        // now add css for each used style
        Set<CellStyle> seen = new HashSet<CellStyle>();
        for (int i = 0; i < wb.getNumberOfSheets(); i++) {
            Sheet sheet = wb.getSheetAt(i);
            Iterator<Row> rows = sheet.rowIterator();
            while (rows.hasNext()) {
                Row row = rows.next();
                for (Cell cell : row) {
                    CellStyle style = cell.getCellStyle();
                    if (!seen.contains(style)) {
                        printStyle(style);
                        seen.add(style);
                    }
                }
            }
        }
    }

    private void printStyle(CellStyle style) {
        out.format(".%s .%s {%n", DEFAULTS_CLASS, styleName(style));
        styleContents(style);
        out.format("}%n");
    }

    private void styleContents(CellStyle style) {
        styleOut("text-align", style.getAlignment(), HALIGN);
        styleOut("vertical-align", style.getVerticalAlignment(), VALIGN);
        fontStyle(style);
        borderStyles(style);
        helper.colorStyles(style, out);
    }

    private void borderStyles(CellStyle style) {
        styleOut("border-left", style.getBorderLeft(), BORDER);
        styleOut("border-right", style.getBorderRight(), BORDER);
        styleOut("border-top", style.getBorderTop(), BORDER);
        styleOut("border-bottom", style.getBorderBottom(), BORDER);
    }

    private void fontStyle(CellStyle style) {
        Font font = wb.getFontAt(style.getFontIndex());

        if (font.getBold()) {
            out.format("  font-weight: bold;%n");
        }
        if (font.getItalic()) {
            out.format("  font-style: italic;%n");
        }

        int fontheight = font.getFontHeightInPoints();
        if (fontheight == 9) {
            //fix for stupid ol Windows
            fontheight = 10;
        }
        out.format("  font-size: %dpt;%n", fontheight);

        // Font color is handled with the other colors
    }

    private String styleName(CellStyle style) {
        if (style == null) {
            style = wb.getCellStyleAt((short) 0);
        }
        StringBuilder sb = new StringBuilder();
        Formatter fmt = new Formatter(sb);
        try {
            fmt.format("style_%02x", style.getIndex());
            return fmt.toString();
        } finally {
            fmt.close();
        }
    }

    private <K> void styleOut(String attr, K key, Map<K, String> mapping) {
        String value = mapping.get(key);
        if (value != null) {
            out.format("  %s: %s;%n", attr, value);
        }
    }

    private static CellType ultimateCellType(Cell c) {
        CellType type = c.getCellType();
        if (type == CellType.FORMULA) {
            type = c.getCachedFormulaResultType();
        }
        return type;
    }

    private void printSheets() {
        ensureOut();
        Sheet sheet = wb.getSheetAt(sheetNumber);
        printSheet(sheet);
    }

    public void printSheet(Sheet sheet) {
        ensureOut();
        out.format("<table class=\"%s\">%n", DEFAULTS_CLASS);
        printCols(sheet);
        printSheetContent(sheet);
        out.format("</table>%n");
    }

    private void printCols(Sheet sheet) {
        //  out.format("<col/>%n");
        ensureColumnBounds(sheet);
//        for (int i = firstColumn; i < endColumn; i++) {
//            out.format("<col/>%n");
//        }
    }

    private void ensureColumnBounds(Sheet sheet) {
        if (gotBounds) {
            return;
        }

        Iterator<Row> iter = sheet.rowIterator();
        firstColumn = (iter.hasNext() ? Integer.MAX_VALUE : 0);
        endColumn = 0;
        while (iter.hasNext()) {
            Row row = iter.next();
            short firstCell = row.getFirstCellNum();
            if (firstCell >= 0) {
                firstColumn = Math.min(firstColumn, firstCell);
                endColumn = Math.max(endColumn, row.getLastCellNum());
            }
        }
        gotBounds = true;
    }

    private void printColumnHeads() {
        out.format("<thead>%n");
        out.format("  <tr class=\"%s\">%n", COL_HEAD_CLASS);
        out.format("    <th class=\"%s\">&#x25CA;</th>%n", COL_HEAD_CLASS);
        //noinspection UnusedDeclaration
        StringBuilder colName = new StringBuilder();
        for (int i = firstColumn; i < endColumn; i++) {
//            colName.setLength(0);
//            int cnum = i;
//            for(;;) {
//                colName.insert(0, (char) ('A' + cnum % 26));
//                cnum /= 26;
//                if(cnum <= 0) break;
//            }
//            out.format("    <th class=%s>%s</th>%n", COL_HEAD_CLASS, colName);
            out.format("    <th >hh</th>");


        }
        out.format("  </tr>%n");
        out.format("</thead>%n");
    }

    private void printSheetContent(Sheet sheet) {
        //printColumnHeads();

        out.format("<tbody>%n");
        Iterator<Row> rows = sheet.rowIterator();
        while (rows.hasNext()) {
            Row row = rows.next();

            out.format("  <tr>%n");
            //out.format("    <td class=%s>%d</td>%n", ROW_HEAD_CLASS, row.getRowNum() + 1);
            for (int i = firstColumn; i < endColumn; i++) {
                String content = "&nbsp;";
                String attrs = "";
                CellStyle style = null;
                if (i >= row.getFirstCellNum() && i < row.getLastCellNum()) {
                    Cell cell = row.getCell(i);
                    if (cell != null) {
                        style = cell.getCellStyle();
                        attrs = tagStyle(cell, style);
                        //Set the value that is rendered for the cell
                        //also applies the format
                        CellFormat cf = CellFormat.getInstance(
                                style.getDataFormatString());
                        CellFormatResult result = cf.apply(cell);
                        content = result.text;
                        if (content.equals("")) {
                            content = "&nbsp;";
                        }
                    }
                }
                out.format("    <td class=\"%s\" %s>%s</td>%n", styleName(style),
                        attrs, content);
            }
            out.format("  </tr>%n");
        }
        out.format("</tbody>%n");
    }

    private String tagStyle(Cell cell, CellStyle style) {
        if (style.getAlignment() == HorizontalAlignment.GENERAL) {
            switch (ultimateCellType(cell)) {
                case CellType.STRING:
                    return "style=\"text-align: left;\"";
                case CellType.BOOLEAN:
                case CellType.ERROR:
                    return "style=\"text-align: center;\"";
                case CellType.NUMERIC:
                default:
                    // "right" is the default
                    break;
            }
        }
        return "";
    }
}