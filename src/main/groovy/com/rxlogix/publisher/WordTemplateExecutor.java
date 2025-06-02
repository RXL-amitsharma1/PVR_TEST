package com.rxlogix.publisher;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import groovy.util.Eval;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.docx4j.Docx4J;
import org.docx4j.TextUtils;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.convert.in.xhtml.XHTMLImporterImpl;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.AlternativeFormatInputPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.*;

import javax.imageio.ImageIO;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigInteger;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class WordTemplateExecutor {
    public PublisherExecutionLog log = new PublisherExecutionLog();

    public WordTemplateExecutor(PublisherExecutionLog log) {
        this.log = log;
    }

    public byte[] generateFromTemplate(InputStream template, List<Parameter> paramsList) throws Exception {
        log.append("Starting template generation");
        log.append("Initial parameter list:");
        log.writeToLog(paramsList);

        Map<String, Object> simpleParams = new HashMap<>();
        Map<String, List<Object>> wordParams = new HashMap<>();
        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(template);
        MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
        log.append("Opening word document successful");
        // VariablePrepare.prepare(wordMLPackage); - removed because it remove spaces between words in some cases
        // documentPart.variableReplace(mapping);  - do not work properly as well

        for (Parameter param : paramsList) {
            if (param.value == null) continue;
            if (param.type == ParameterType.STRING) {
                if (param.value.toString() == "") continue;
                simpleParams.put(param.name, param.value.toString());
            } else if (param.type == ParameterType.HTML) {
                try {
                    XHTMLImporterImpl importer = new XHTMLImporterImpl(wordMLPackage);
                    if (param.value != null && param.value.toString().length() > 0)
                        wordParams.put("${" + param.name + "}", importer.convert(param.value.toString().replaceAll("&nbsp;", " ").replaceAll("<a[^>]+>", "").replaceAll("</a>", ""), "file://"));
                } catch (Exception e) {
                    log.logError("Error occurred converting html from parameter " + param.name + "to word objects", e);
                }
            } else if (param.type == ParameterType.WORD) {
                if (((List<Object>) param.value).size() == 0) continue;
                for (Object obj : (List<Object>) param.value) {
                    if (obj instanceof P)
                        ((P) obj).getContent().stream()
                                .filter((o) -> (((o instanceof JAXBElement) && ((JAXBElement) o).getName().getLocalPart().equals("bookmarkEnd")))
                                        || ((o instanceof JAXBElement) && ((JAXBElement) o).getName().getLocalPart().equals("bookmarkStart")))
                                .collect(Collectors.toList())
                                .forEach((o) -> ((P) obj).getContent().remove(o));
                }
                wordParams.put("${" + param.name + "}", (List<Object>) param.value);
            } else if (param.type == ParameterType.DATA) {
                simpleParams.put(param.name, param.value);
            }
        }
        log.append("Converting parameters complete");

        log.append("Prepared " + wordParams.size() + " word part parameters : " + String.join(", ", wordParams.keySet()));
        log.writeToLog(simpleParams);

        fixVariables(documentPart);
        log.append("Splited by formatting parameter names restored");

        proceedLoops(documentPart, simpleParams);
        log.append("Loop execution complete, parameters after loop execution:");
        log.writeToLog(simpleParams);


        Map<String, Object> result = parseDocx(documentPart, false);

        List<Object> textNodes = (List<Object>) result.get("textNodes");
        List<Object> rootNodes = (List<Object>) result.get("rootNodes");

        processVariables(rootNodes, simpleParams);
        log.append("Calculating parameters in DEFINITION block complete, parameters:");
        log.writeToLog(simpleParams);

        proceedIfConditions(rootNodes, simpleParams);
        log.append("Execution of conditions complete");

        if (wordParams.size() > 0) {
            runReplacements(wordMLPackage.getMainDocumentPart(), wordParams);
            log.append("Word parts parameters inserted");
        }
        if (simpleParams.size() > 0) {
            replaceVariables(textNodes, simpleParams);
            log.append("Parameters inserted");
        }

        tableEmptyCellHeightFix(documentPart);
        log.append("Table Empty Cell Height Fixed");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Docx4J.save(wordMLPackage, out);
        log.append("Result word document complete");
        return out.toByteArray();
    }

    //table empty cell height fix: after importing report table all empty cells has default height (bigger then cell font size),
    // it breaks table formatting. Here we set height for each empty cell manually.
    private void tableEmptyCellHeightFix(MainDocumentPart documentPart) throws Exception {
        String textNodesXPath = "//w:tc";
        final List<Tc> tclist = Lists.newArrayList();
        new TraversalUtil(documentPart, new TraversalUtil.CallbackImpl() {
            @Override
            public List<Object> apply(Object o) {
                if (o instanceof Tc) {
                    tclist.add((Tc) o);
                }

                return null;
            }
        });
        for ( Tc tc:tclist) {
            for (Object c : tc.getContent()) {
                if (c instanceof P) {
                    P p = (P) c;
                    StringWriter txt = new StringWriter();
                    TextUtils.extractText(p, txt);
                    if (txt.toString().trim().length() == 0) {
                        PPr ppr = p.getPPr();
                        if (ppr == null) {
                            ppr = new PPr();
                            p.setPPr(ppr);
                        }
                        ParaRPr paraRPr = new ParaRPr();
                        HpsMeasure m = new HpsMeasure();
                        m.setVal(new BigInteger("0"));
                        paraRPr.setSz(m);
                        paraRPr.setSzCs(m);
                        ppr.setRPr(paraRPr);
                    }
                }
            }

        }
    }

    //need to be fixed, sometimes it includes </w:t><w:br/><w:t> as text instead of new line tags
    private static String newlineToBreakHack(String r) {
        if (r == null) return "not defined";
        try {
            StringTokenizer st = new StringTokenizer(r, "\n\r\f"); // tokenize on the newline character, the carriage-return character, and the form-feed character
            StringBuilder sb = new StringBuilder();

            boolean firsttoken = true;
            while (st.hasMoreTokens()) {
                String line = (String) st.nextToken();
                if (firsttoken) {
                    firsttoken = false;
                } else {
                    sb.append("</w:t><w:br/><w:t>");
                }
                sb.append(line);
            }
            return sb.toString();
        } catch (Exception e) {
            System.out.println("Error adding new line tags to " + r);
            e.printStackTrace();
        }
        return r;
    }


    private void runReplacements(final MainDocumentPart mainPart, final Map<String, List<Object>> replacements) {
        Preconditions.checkNotNull(mainPart, "the supplied main doc part may not be null!");
        Preconditions.checkNotNull(replacements, "replacements may not be null!");

        // look for all P elements in the specified object
        final List<P> paragraphs = Lists.newArrayList();
        new TraversalUtil(mainPart, new TraversalUtil.CallbackImpl() {
            @Override
            public List<Object> apply(Object o) {
                if (o instanceof P) {
                    paragraphs.add((P) o);
                }

                return null;
            }
        });

        // run through all found paragraphs to located identifiers
        for (final P paragraph : paragraphs) {
            // check if this is one of our identifiers
            final StringWriter paragraphText = new StringWriter();
            try {
                TextUtils.extractText(paragraph, paragraphText);
            } catch (Exception ex) {
                log.logWarning("Unknown exception occurred in runReplacements trying to fetch text from w:p", ex);
            }

            final String identifier = paragraphText.toString();
            if (identifier != null && replacements.containsKey(identifier)) {
                final List<Object> listToModify;

                if (paragraph.getParent() instanceof Tc) {
                    // paragraph located in table-cell
                    final Tc parent = (Tc) paragraph.getParent();
                    listToModify = parent.getContent();
                } else {
                    // paragraph located in main document part
                    listToModify = mainPart.getContent();
                }

                if (listToModify != null) {
                    final int index = listToModify.indexOf(paragraph);
                    Preconditions.checkState(index > -1, "could not located the paragraph in the specified list!");

                    // remove the paragraph from it's current index
                    listToModify.remove(index);

                    // add the converted HTML paragraphs
                    listToModify.addAll(index, replacements.get(identifier));
                }
            }
        }
    }

    public static void convertPageToImage(File srcFile, int pageNumber, File resultFile) throws IOException {
        int page = pageNumber - 1;

        PDDocument document = PDDocument.load(srcFile);
        if (page < 0 || page > document.getNumberOfPages())
            throw new IllegalArgumentException("Wrong page number! It should be >= 1 and <=total pages in the document. ");
        PDFRenderer pdfRenderer = new PDFRenderer(document);

        PDPage pd = document.getPage(page);
        pd.setCropBox(new PDRectangle(70, 0, pd.getCropBox().getWidth() - 220, pd.getCropBox().getHeight()));
        BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);
        ImageIO.write(bim, "png", resultFile);
        document.close();
    }

    static List<Text> getAllTextElementsFromObject(Object obj) {
        List<Text> result = new ArrayList<Text>();
        if (obj instanceof List) {
            for (Object o : (List) obj) {
                result.addAll(getAllTextElementsFromObject(o));
            }
        }
        if (obj instanceof JAXBElement) {
            obj = ((JAXBElement<?>) obj).getValue();
        }

        if (obj.getClass().equals(Text.class)) {
            result.add((Text) obj);
        } else if (obj instanceof ContentAccessor) {
            List<?> children = ((ContentAccessor) obj).getContent();
            for (Object child : children) {
                result.addAll(getAllTextElementsFromObject(child));
            }
        }
        return result;
    }

    private void replaceVariables(List<Object> elements, Map<String, Object> values) {
        for (int i = 0; i < elements.size(); i++) {
            if (elements.get(i) == null || !(elements.get(i) instanceof Text)) continue;
            Text el = (Text) elements.get(i);
            String txt = el.getValue() == null ? "" : el.getValue();
            int start = txt.indexOf("${");
            while (start > -1) {
                int end = txt.indexOf("}", start);
                if (end > -1) {
                    String placeholder = txt.substring(start + 2, end);
                    Object val = values.get(placeholder);
                    if (val != null) {
                        String replacedValue = txt.substring(0, start) + val.toString() + txt.substring(end + 1);
                        el.setValue(replacedValue);
                        txt = replacedValue;
                    }
                }
                start++;
                start = txt.indexOf("${", start);
            }
        }
    }

    public static Map<String, Object> parseDocx(MainDocumentPart mainDocumentPart, Boolean withBookmarks) throws Exception {
        StringBuilder text = new StringBuilder();
        List<Object> textNodes = new LinkedList<Object>();
        textNodes.addAll(fetchText(mainDocumentPart));
        List<Object> rootNodes = new LinkedList<>(appendRootElements(mainDocumentPart.getContent(),withBookmarks));

        RelationshipsPart relationshipPart = mainDocumentPart.getRelationshipsPart();
        List<Relationship> relationships = relationshipPart.getRelationships().getRelationship();
        for (Relationship r : relationships) {
            Object part = relationshipPart.getPart(r);
            if (part instanceof AlternativeFormatInputPart) {
                WordprocessingMLPackage wordML = WordprocessingMLPackage.load(new ByteArrayInputStream(((AlternativeFormatInputPart) part).getBytes()));
                MainDocumentPart main = wordML.getMainDocumentPart();
                textNodes.addAll(fetchText(main));
                rootNodes.addAll(appendRootElements(main.getContent(),withBookmarks));
            } else if ((part instanceof FooterPart) || (part instanceof HeaderPart)) {
                textNodes.addAll(fetchText(part));
                rootNodes.addAll(appendRootElements(((ContentAccessor) part).getContent(),withBookmarks));
            }
        }
        for (Object o : rootNodes) {
            StringWriter txt = new StringWriter();
            TextUtils.extractText(o, txt);
            text.append((txt.toString()) + "\n");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("text", text.toString());
        result.put("textNodes", textNodes);
        result.put("rootNodes", rootNodes);
        return result;

    }

    public static List<Object> getParagraphsAsWordObjects(List<Object> rootNodes, String from, String to, boolean includeLeft, boolean includeRight) throws Exception {
        boolean startAppend = false;
        List<Object> target = new LinkedList<>();
        for (Object obj : rootNodes) {
            String txt = "";
            if (obj instanceof P) {
                final StringWriter paragraphText = new StringWriter();
                TextUtils.extractText((P) obj, paragraphText);
                txt = paragraphText.toString();
            }
            if (includeLeft && txt.contains(from)) startAppend = true;
            if (!includeRight && txt.contains(to)) break;
            if (startAppend) target.add(obj);

            if (!includeLeft && txt.contains(from)) startAppend = true;
            if (includeRight && txt.contains(to)) break;
        }
        return target;
    }

    private static boolean isBookmarkEnd(Object o, BigInteger id) {
        if ((o instanceof JAXBElement) && ((JAXBElement) o).getName().getLocalPart().equals("bookmarkEnd")) {
            if (((CTMarkupRange) ((JAXBElement) o).getValue()).getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isBookmarkStart(Object o, String name) {
        if ((o instanceof JAXBElement) && ((JAXBElement) o).getName().getLocalPart().equals("bookmarkStart")) {
            if (((CTBookmark) ((JAXBElement) o).getValue()).getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public static List<Object> getBookmarkAsWordObjects(List<Object> rootNodes, String from, String to, boolean includeLeft, boolean includeRight) throws Exception {
        List<Object> target = new LinkedList<>();
        BigInteger fromId = new BigInteger("-1");
        BigInteger toId = new BigInteger("-1");
        boolean startAppend = false;
        boolean stopAppend = false;
        boolean insideStartP = false;
        nodes:
        for (Object obj : rootNodes) {
            if (obj instanceof P) {
                insideStartP = false;
                for (Object o : ((P) obj).getContent()) {
                    if (isBookmarkStart(o, from)) {
                        fromId = ((CTBookmark) ((JAXBElement) o).getValue()).getId();
                        if (includeLeft) startAppend = true;
                        insideStartP = true;
                    }
                    if (!includeLeft && isBookmarkEnd(o, fromId)) {
                        startAppend = true;
                        if (insideStartP) continue nodes;
                    }
                    if (isBookmarkStart(o, to)) {
                        toId = ((CTBookmark) ((JAXBElement) o).getValue()).getId();
                        if (!includeRight) stopAppend = true;
                    }
                    if (isBookmarkEnd(o, toId)) {
                        stopAppend = true;
                    }
                    if (stopAppend) break;
                }
            } else if (isBookmarkEnd(obj, toId)) {
                stopAppend = true;
            } else if (!includeLeft && isBookmarkEnd(obj, fromId)) {
                startAppend = true;
            }
            if (!includeRight && stopAppend) break;
            if (startAppend && ((obj instanceof P) || ((obj instanceof JAXBElement) && ((JAXBElement) obj).getName().getLocalPart().equals("tbl"))))
                target.add(obj);
            if (stopAppend) break;
        }
        if (!stopAppend) return new LinkedList<>();
        return target;
    }
    public static List<Object> getSectionAsWordObjects(List<Object> textNodes, List<Object> rootNodes, String from, String to) {

        List<Object> target = new LinkedList<>();
        String fromid = null, toid = null;
        for (Object obj : textNodes) {
            Text text = (Text) obj;
            String textValue = text.getValue();
            Object o = ((R) (((Text) obj).getParent())).getParent();
            if ((textValue != null) && textValue.equalsIgnoreCase(from)) {
                if (o instanceof P.Hyperlink) fromid = ((P.Hyperlink) o).getAnchor();
            }
            if ((textValue != null) && textValue.equalsIgnoreCase(to)) {
                if (o instanceof P.Hyperlink) toid = ((P.Hyperlink) o).getAnchor();
            }

            if ((fromid != null) && (toid != null)) break;
        }

        boolean startAppend = false;
        l1:
        for (Object obj : rootNodes) {
            if (obj instanceof P)
                for (Object o : ((P) obj).getContent()) {
                    if (o instanceof JAXBElement)
                        if (((JAXBElement) o).getValue() instanceof CTBookmark) {
                            if (((CTBookmark) ((JAXBElement) o).getValue()).getName().equals(fromid))
                                startAppend = true;
                            if (((CTBookmark) ((JAXBElement) o).getValue()).getName().equals(toid)) break l1;
                        }
                }
            if (startAppend) target.add(obj);
        }
        return target;
    }

    private static List<Object> appendRootElements(List<Object> from, Boolean withBookmarks) {
        List<Object> rootNodes = new LinkedList<>();
        for (Object o : from) {
            if (o instanceof P) {
                rootNodes.add(o);
            } else
                if ((o instanceof Tbl)
                        || ((o instanceof JAXBElement) && ((JAXBElement) o).getName().getLocalPart().equals("tbl"))
                        ||(withBookmarks && ((o instanceof JAXBElement) && ((JAXBElement) o).getName().getLocalPart().equals("bookmarkEnd")))
                        ||(withBookmarks &&  ((o instanceof JAXBElement) && ((JAXBElement) o).getName().getLocalPart().equals("bookmarkStart")))
                ) {
                    rootNodes.add(o);
            }
        }
        return rootNodes;
    }

    public static Map<String, List<String>> fetchParameters(InputStream template) throws Exception {

        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(template);
        MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();

        Map<String, Object> result = parseDocx(documentPart, false);
        List<Object> rootNodes = (List<Object>) result.get("rootNodes");
        String text = (String) result.get("text");

        Map<String, List<String>> out = new HashMap<>();
        List<String> variable = new LinkedList<>();
        List<String> manual = new LinkedList<>();
        List<String> comment = new LinkedList<>();
        //List for invalid parameters
        List<String> invalidVariable = new LinkedList<>();
        out.put("variable", variable);
        out.put("manual", manual);
        out.put("comment", comment);
        out.put("invalidVariable", invalidVariable);
        List<String> definitionRows = fetchDefinition(rootNodes);
        List<String> skip = new LinkedList<String>();
        if (definitionRows.size() > 0) {
            for (String row : definitionRows) {
                row = row.trim();
                Pattern pattern = Pattern.compile("#INPUTVAR +(BOOLEAN|TEXT|DATA) +[a-zA-Z][a-zA-Z0-9]* *.+");
                Matcher matcher = pattern.matcher(row);
                if (matcher.matches()) {
                    String[] part = row.split(" ");
                    String type = part[1];
                    String variableName = part[2];
                    String description = row.substring(row.indexOf(variableName) + variableName.length()).trim();
                    variable.add(variableName + "::" + type + "::" + description);
                }
                pattern = Pattern.compile("#VAR +(BOOLEAN|TEXT|DATA) +[a-zA-Z][a-zA-Z0-9]* *= *.+");
                matcher = pattern.matcher(row);
                if (matcher.matches()) {
                    String[] part = row.split(" +");
                    String variableName = part[2];
                    skip.add(variableName + "::");
                }
            }
        }
        if (variable.size() == 0) {
            Set<String> names = new HashSet<>();
            /*
             * Fetching all strings from text matching ${param}, including valid and invalid
             * both type of params as invalid parameter should also be shown to user
             * as part of error message.
             */
            Matcher matcher = Pattern.compile("\\$\\{([^}]*)\\}").matcher(text);
            while (matcher.find()) {
                String m = matcher.group();
                if(m.contains("#")) {
                    m = m.substring(2, m.length() - 1).replaceAll(" +", "").split("\\.|\\[")[0];
                    if (m.startsWith("#IF")) m = m.substring(3).trim();
                    if (m.startsWith("#EACH")) {
                        m = m.substring(5).trim();
                        if (m.indexOf("FROM") > -1) m = m.substring(0, m.indexOf("FROM")).trim();
                    }
                    if (m.startsWith("#ENDIF")) continue;
                    if (m.startsWith("#ENDEACH")) continue;
                    if (m.startsWith("#ITEM")) continue;
                } else {
                    m = m.substring(2, m.length() - 1).split("\\.|\\[")[0];
                }
                names.add(m);
            }
            Set<String> uniqueIgnoreCaseSet = new HashSet<>();
            names.forEach(p -> {
                    //valid parameters having alphanumeric and underscore only
                    if (p.matches("^[\\w]+$") && p.length()<256) {
                        if(!uniqueIgnoreCaseSet.contains(p.toUpperCase())){
                            variable.add(p + "::TEXT:: ");
                            uniqueIgnoreCaseSet.add(p.toUpperCase());
                        }
                    }else{
                        //invalid parameters having atleast one special character apart from underscore
                        invalidVariable.add(p + "::TEXT:: ");
                    }
                }
            );
        }
        if (skip.size() > 0) {
            List<String> out2 = new LinkedList<>();
            l:
            for (String param : variable) {
                for (String toSkip : skip) {
                    if (param.startsWith(toSkip)) continue l;
                }
                out2.add(param);
            }
            out.put("variable", out2);
        }
        //manual
        Matcher matcher = Pattern.compile("(?s)\\[\\[.*?\\]\\]").matcher(text);
        while (matcher.find()) {
            manual.add(matcher.group());
        }
        //comment
        matcher = Pattern.compile("(?s)/\\*.*?\\*/").matcher(text);
        while (matcher.find()) {
            comment.add(matcher.group());
        }

        return out;
    }

    private static List<String> fetchDefinition(List<Object> rootNodes) {
        List<String> definitionRows = new LinkedList();
        boolean fetching = false;
        for (int i = 0; i < rootNodes.size(); i++) {
            Object obj = rootNodes.get(i);
            if (obj instanceof P) {
                P p = (P) obj;
                String txt = TextUtils.getText(p);
                if (txt.trim().startsWith("#DEFINITIONSTART")) fetching = true;
                if (fetching) {
                    if (txt != null && txt.trim().length() > 0 && txt.startsWith("#") && !txt.startsWith("#//"))
                        definitionRows.add(txt.trim());
                    ((ContentAccessor) p.getParent()).getContent().remove(p);
                }
                if (txt.trim().startsWith("#DEFINITIONEND")) break;
            }
        }
        return definitionRows;
    }

    private void processVariables(List<Object> rootNodes, Map<String, Object> param) {
        List<String> definitionRows = fetchDefinition(rootNodes);
        for (String row : definitionRows) {
            Pattern pattern = Pattern.compile("#VAR +(BOOLEAN|TEXT|DATA) +[a-zA-Z][a-zA-Z0-9]* *= *.+");
            Matcher matcher = pattern.matcher(row);
            if (matcher.matches()) {
                String[] part = row.split(" +");
                String type = part[1];
                String variableName = part[2];
                String expression = row.substring(row.indexOf("=") + 1).replaceAll("ctx\\.", "x.").replaceAll("“", "\"").replaceAll("”", "\"");
                try {
                    Object value = Eval.x(param, expression);
                    if (type.equals("BOOLEAN")) param.put(variableName, (Boolean) value);
                    else if (type.equals("TEXT")) param.put(variableName, value.toString());
                    else param.put(variableName, value);
                } catch (Exception e) {
                    log.logError("Error occurred in processVariables executing " + expression, e);
                }
            } else {
                if  (row.indexOf("#DEFINITION")==-1)
                    log.logWarning("Could not parse row '" + row + "' from DEFINITION block  - ignore it ", null);
            }
        }


    }

    private void proceedLoops(MainDocumentPart mainDocumentPart, Map<String, Object> param) throws Exception {
        int it = 0;
        boolean foundLoop = true;
        while (foundLoop) {
            List<Object> rootNodes = appendRootElements(mainDocumentPart.getContent(), false);
            foundLoop = proceedOneLoop(rootNodes, param, it);
            if (++it > 1000) throw new Exception("Bad Loop grammar");
        }
    }

    private boolean proceedOneLoop(List<Object> rootNodes, Map<String, Object> param, int loopNumber) {
        boolean fetching = false;
        int position = 0;
        String variableName = "";
        int iteratorStart = 0;
        List<P> content = new LinkedList<>();
        boolean foundLoop = false;
        Collection data = null;
        for (int i = 0; i < rootNodes.size(); i++) {
            Object obj = rootNodes.get(i);
            if (obj instanceof P) {
                P p = (P) obj;
                String txt = TextUtils.getText(p);
                txt = txt != null ? txt.trim() : "";
                int startIndex = txt.indexOf("${#EACH");
                if (startIndex == 0) {
                    int itFrom = txt.indexOf("FROM ");
                    if (itFrom > -1) {
                        iteratorStart = new Integer(txt.substring(itFrom + 5, txt.indexOf("}")));
                    }
                    position = ((ContentAccessor) p.getParent()).getContent().indexOf(p);
                    variableName = itFrom > -1 ? txt.substring(7, itFrom).trim() : txt.substring(7, txt.indexOf("}")).trim();
                    data = (Collection) param.get(variableName);
                    if (data != null) {
                        foundLoop = true;
                        fetching = true;
                        ((ContentAccessor) p.getParent()).getContent().remove(p);
                    }
                    continue;
                }
                if (startIndex > 0) {
                    //todo:error!!!
                }
                if ((startIndex == -1) && fetching) {
                    startIndex = txt.indexOf("${#ENDEACH");

                    if (startIndex == 0) {
                        ((ContentAccessor) p.getParent()).getContent().remove(p);
                        break;
                    }
                    if (startIndex > 0) {
                        //todo:error!!!
                    }
                }
                if (fetching) {
                    content.add(p);
                    ((ContentAccessor) p.getParent()).getContent().remove(p);
                }

            }
        }
        Map<String, String> ifVariables = new HashMap<>();
        data = (Collection) param.get(variableName);
        if ((content.size() > 0) && (data != null) && (data.size() > iteratorStart)) {
            int rowNumber = -1;
            int varNumber = 0;
            for (Object dataItem : data) {
                rowNumber++;
                if (rowNumber < iteratorStart) continue;
                for (P p : content) {
                    P newP = XmlUtils.deepCopy(p);
                    List<Text> textList = fetchNotNullTextElementsFromP(newP);
                    for (int i = 0; i < textList.size(); i++) {
                        String txt = textList.get(i).getValue() == null ? "" : textList.get(i).getValue();
                        int fromIndex = -1;
                        do {
                            int start = txt.indexOf("${", fromIndex + 1);
                            fromIndex = start;

                            if (start > -1) {
                                int end = txt.indexOf("}", start);
                                if (end > -1) {
                                    String placeholder = txt.substring(start + 2, end).trim();
                                    if (placeholder.contains("#ITEM")) {
                                        String expression = placeholder.substring(placeholder.indexOf("#ITEM")).trim();
                                        if (placeholder.startsWith("#ENDIF")) {
                                            String replacedValue = txt.substring(0, start) + "${#ENDIF " + ifVariables.get(expression) + "}" + txt.substring(end + 1);
                                            fromIndex++;
                                            varNumber++;
                                            textList.get(i).setValue(replacedValue);
                                            txt = replacedValue;
                                        } else {
                                            Object val = null;
                                            try {
                                                val = Eval.x(dataItem, expression.replaceAll("#ITEM", "x").replaceAll("“", "\"").replaceAll("”", "\""));
                                            } catch (Exception e) {
                                                log.logError("Error occurred evaluating value in loop: " + expression, e);
                                            }
                                            if (val == null) val = "";
                                            ifVariables.put(expression, variableName + "_" + loopNumber + "_" + varNumber);
                                            param.put(variableName + "_" + loopNumber + "_" + varNumber, val);
                                            String replacedValue = txt.substring(0, start) + (placeholder.startsWith("#IF") ? "${#IF " : "${") + variableName + "_" + loopNumber + "_" + varNumber + "}" + txt.substring(end + 1);
                                            fromIndex++;
                                            varNumber++;
                                            textList.get(i).setValue(replacedValue);
                                            txt = replacedValue;
                                        }

                                    }
                                }
                            }
                        } while (fromIndex > -1);
                    }
                    ((ContentAccessor) p.getParent()).getContent().add(position, newP);
                    newP.setParent(p.getParent());
                    position++;

                }

            }

        }
        return foundLoop;
    }

    private void proceedIfConditions(List<Object> rootNodes, Map<String, Object> param) throws Exception {
        Boolean paramValue = null;
        String variableName = null;
        int startI = 0;
        int shift = 0;
        int startIndex = 0;
        List<Object> target = new LinkedList<>();
        for (int i = 0; i < rootNodes.size(); i++) {
            String txt = "";
            Object obj = rootNodes.get(i);
            if (obj instanceof P) {
                if (variableName == null) {
                    txt = TextUtils.getText((P) obj);
                    if (txt != null)
                        txt = txt.trim();
                    else
                        txt = "";

                    startIndex = txt.indexOf("${#IF", shift);
                    if (startIndex > -1) {
                        variableName = txt.substring(startIndex + 5, txt.indexOf("}", startIndex + 5));
                        if (variableName != null && variableName.length() > 0) {
                            variableName = variableName.trim();
                            String[] exp = variableName.split("\\.|\\[");
                            Object value = null;
                            if (exp.length > 0) {
                                try {
                                    value = Eval.x(param.get(exp[0]), variableName.replaceAll(exp[0], "x").replaceAll("“", "\"").replaceAll("”", "\""));
                                } catch (Exception e) {
                                    log.logError("Error occurred in evaluating if condition: " + variableName, e);
                                }
                            } else {
                                value = param.get(variableName);
                            }
                            if (value != null) {
                                if (value instanceof Boolean) {
                                    paramValue = (Boolean) value;
                                } else {
                                    String v = value.toString().trim();
                                    paramValue = !(v.equals("false") || v.equals("FALSE") || v.equals("NO") || v.equals("") || v.equals("0"));
                                }
                            }
                        }
                        startI = i;
                    }
                }
                shift = 0;
                if (variableName != null) {
                    if (paramValue == null) {
                        boolean complete = skipContent((P) obj, variableName);
                        if (complete) {
                            variableName = null;
                            paramValue = null;
                            if (i == startI) {
                                shift = startIndex + 1;
                            } else {
                                shift = 0;
                            }
                            i--;
                        }
                    } else {
                        boolean complete = paramValue ? removeVariable((P) obj, variableName) : removeContent((P) obj, variableName, startI != i);
                        if (complete) {
                            variableName = null;
                            paramValue = null;
                            i = startI - 1;
                        }
                    }
                }
            } else if (paramValue != null && variableName != null && !paramValue) {
                if ((obj instanceof Tbl))
                    ((ContentAccessor) ((Tbl) obj).getParent()).getContent().remove(obj);
                if ((obj instanceof JAXBElement) && ((JAXBElement) obj).getName().getLocalPart().equals("tbl")) {
                    ((ContentAccessor) ((Tbl) ((JAXBElement) obj).getValue()).getParent()).getContent().remove(obj);


                }
            }
        }
    }

    private boolean removeVariable(P p, String variableName) {
        return removeVariableOrContent(p, variableName, false, false);
    }

    private boolean removeContent(P p, String variableName, Boolean continieRemoving) {
        return removeVariableOrContent(p, variableName, true, continieRemoving);
    }

    private static List<Text> fetchNotNullTextElementsFromP(P p) {
        List<R> runs = new LinkedList<>();
        for (Object o : p.getContent()) {
            if (o instanceof R)
                runs.add((R) o);
        }
        List<Text> result = new LinkedList<>();
        for (R r : runs) {
            JAXBElement el = (JAXBElement) r.getContent().stream().filter(it ->
                    (it instanceof JAXBElement) &&
                            ((JAXBElement) it).getValue() instanceof Text)
                    .findFirst().orElse(null);
            if (el != null && el.getValue() != null) {
                result.add((Text) el.getValue());
            }
        }
        return result;
    }

    private boolean skipContent(P p, String variableName) {
        String txt = TextUtils.getText(p);
        if (!txt.contains(variableName)) {
            return false;
        }
        String regexpEndIf = "\\$\\{#ENDIF +\\Q" + variableName + "\\E *\\}";
        Pattern pattern = Pattern.compile(regexpEndIf);
        Matcher matcher = pattern.matcher(txt);
        if (matcher.find()) {
            return true;
        }
        return false;
    }

    private boolean removeVariableOrContent(P p, String variableName, Boolean removecontent, Boolean continieRemoving) {
        String regexpIf = "\\$\\{#IF +\\Q" + variableName + "\\E *\\}";
        String regexpEndIf = "\\$\\{#ENDIF +\\Q" + variableName + "\\E *\\}";
        if (!TextUtils.getText(p).contains(variableName)) {
            if (removecontent) {
                if ((p.getParent() != null) && (((ContentAccessor) p.getParent()).getContent() != null))
                    ((ContentAccessor) p.getParent()).getContent().remove(p);
            }
            return false;
        }
        boolean removing = continieRemoving;
        for (Text t : fetchNotNullTextElementsFromP(p)) {
            String txt = t.getValue();
            if (txt.trim().length() > 0) {
                Pattern pattern = Pattern.compile(regexpEndIf);
                Matcher matcher = pattern.matcher(txt);
                if (removecontent && removing) {
                    if (matcher.find()) {
                        t.setValue(txt.substring(matcher.end()));
                        removeIfEmpty(p);
                        return true;
                    } else {
                        p.getContent().remove(t.getParent());
                        removeIfEmpty(p);
                        continue;
                    }
                }
                int startIndex = txt.indexOf("${#IF");
                if (startIndex > -1) {
                    if (removecontent) {

                        if (matcher.find()) {
                            t.setValue(txt.substring(matcher.end()));
                            removeIfEmpty(p);
                            return true;
                        }
                        t.setValue(txt.substring(0, startIndex));
                        removeIfEmpty(p);
                        removing = true;

                    } else {
                        String withoutIf = txt.replaceAll(regexpIf, "");
                        String withoutEndIf = withoutIf.replaceAll(regexpEndIf, "");
                        t.setValue(withoutEndIf);
                        removeIfEmpty(p);
                        if (!withoutEndIf.equals(withoutIf)) {
                            return true;
                        } else {
                            continue;
                        }
                    }
                }
                if (!removecontent) {
                    String withoutEndIf = txt.replaceAll(regexpEndIf, "");
                    t.setValue(withoutEndIf);
                    if (!withoutEndIf.equals(txt)) {
                        removeIfEmpty(p);
                        return true;
                    }
                }
            } else {
                if (removecontent && removing) p.getContent().remove(t.getParent());
            }
        }
        return false;
    }

    private static void removeIfEmpty(P p) {
        String resultText = TextUtils.getText(p);
        if ((resultText == null) || (resultText.trim().length() == 0))
            if ((p.getParent() != null) && (((ContentAccessor) p.getParent()).getContent() != null))
                ((ContentAccessor) p.getParent()).getContent().remove(p);
    }


    private void fixVariables(MainDocumentPart mainDocumentPart) throws JAXBException, Docx4JException {
        String textNodesXPath = "//w:t";
        StringBuilder text = new StringBuilder();
        List<Text> textNodes = fetchText(mainDocumentPart);

        RelationshipsPart relationshipPart = mainDocumentPart.getRelationshipsPart();
        List<Relationship> relationships = relationshipPart.getRelationships().getRelationship();
        for (Relationship r : relationships) {
            Object part = relationshipPart.getPart(r);
            if (part instanceof AlternativeFormatInputPart) {
                WordprocessingMLPackage wordML = WordprocessingMLPackage.load(new ByteArrayInputStream(((AlternativeFormatInputPart) part).getBytes()));
                MainDocumentPart main = wordML.getMainDocumentPart();
                textNodes.addAll(fetchText(main));
            } else if ((part instanceof FooterPart) || (part instanceof HeaderPart)) {
                textNodes.addAll(fetchText(part));
            }
        }
        fixVariablesInElements(textNodes);
    }

    static List<Text> fetchText(Object parent) {
        final List<Text> result = Lists.newArrayList();
        new TraversalUtil(parent, new TraversalUtil.CallbackImpl() {
            @Override
            public List<Object> apply(Object o) {
                if (o instanceof Text) {
                    result.add((Text) o);
                }

                return null;
            }
        });
        return result;
    }

    private void fixVariablesInElements(List<Text> elements) {
        for (int i = 0; i < elements.size(); i++) {
            Text el = elements.get(i);
            String txt = el.getValue() == null ? "" : el.getValue();
            int start = txt.lastIndexOf("${");
            if (start > -1) {
                int end = txt.indexOf("}", start);
                if (end == -1) {
                    for (int j = i + 1; j < elements.size(); j++) {
                        Text nextEl = elements.get(j);
                        String nextTxt = (nextEl.getValue() == null ? "" : nextEl.getValue());
                        el.setValue(el.getValue() + nextTxt);
                        nextEl.setValue("");
                        if (nextTxt.contains("}")) {
                            el.setSpace(nextEl.getSpace());
                            nextEl.setSpace(null);
                            i--;
                            break;
                        }
                        nextEl.setSpace(null);
                    }
                }
            }
            if (txt.endsWith("$")) {
                for (int j = i + 1; j < elements.size(); j++) {
                    if ((elements.get(j).getValue() != null) && elements.get(j).getValue().startsWith("{")) {
                        el.setValue(txt + "{");
                        Text next = elements.get(j);
                        next.setValue(next.getValue().substring(1));
                        i--;
                    }

                }
            }
        }
    }

//    public byte[] removeComments(InputStream is) throws Exception {
//        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(is);
//        MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
//        Map<String, Object> result = parseDocx(documentPart);
//        List<Object> rootNodes = (List<Object>) result.get("rootNodes");
//        String text = (String) result.get("text");
//        Boolean removing=false;
//
//        for (int i = 0; i < rootNodes.size(); i++) {
//
//            String txt = "";
//            Object obj = rootNodes.get(i);
//            if (obj instanceof P) {
//                P p = (P) obj;
//                txt = TextUtils.getText(p);
//                if(txt.indexOf("/*")>-1) {
//                    removing=true;
//                }
//                for (Text t : fetchNotNullTextElementsFromP(p)) {
//                    txt = t.getValue();
//                    if (txt.trim().length() > 0) {
//                        Pattern pattern = Pattern.compile(regexpEndIf);
//                        Matcher matcher = pattern.matcher(txt);
//                        if (removecontent && removing) {
//
//                        }
//                    }
//                }
//                if(removing ){
//                    if (!TextUtils.getText(p).contains("*/")) {
//                            if ((p.getParent() != null) && (((ContentAccessor) p.getParent()).getContent() != null))
//                                ((ContentAccessor) p.getParent()).getContent().remove(p);
//                    }
//                }
//            }else if (removing) {
//                if ((obj instanceof Tbl))
//                    ((ContentAccessor) ((Tbl) obj).getParent()).getContent().remove(obj);
//                if ((obj instanceof JAXBElement) && ((JAXBElement) obj).getName().getLocalPart().equals("tbl")) {
//                    ((ContentAccessor) ((Tbl) ((JAXBElement) obj).getValue()).getParent()).getContent().remove(obj);
//                }
//            }
//        }
//
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        Docx4J.save(wordMLPackage, out);
//        return out.toByteArray();
//    }
//
//    void removeCommentFromP(P p, Boolean cntinue){
//        for (Text t : fetchNotNullTextElementsFromP(p)) {
//            String txt = t.getValue();
//            if(txt.indexOf())
//            if (txt.trim().length() > 0) {
//                Pattern pattern = Pattern.compile(regexpEndIf);
//                Matcher matcher = pattern.matcher(txt);
//                if (removecontent && removing) {
//
//                }
//            }
//        }
//    }

    public static Parameter stringParam(String name, Object value) {
        return new Parameter(name, value, ParameterType.STRING);
    }

    public static Parameter htmlParam(String name, Object value) {
        return new Parameter(name, value, ParameterType.HTML);
    }

    public static Parameter dataParam(String name, Object value) {
        return new Parameter(name, value, ParameterType.DATA);
    }

    public static Parameter wordParam(String name, Object value) {
        return new Parameter(name, value, ParameterType.WORD);
    }

    public static class Parameter {
        public String name;
        public Object value;
        public ParameterType type;

        public Parameter(String name, Object value) {
            this(name, value, ParameterType.STRING);
        }

        public Parameter(String name, Object value, ParameterType type) {
            this.name = name;
            if (value == null)
                this.value = "";
            else
                this.value = value;
            this.type = type;
        }

    }

    public static enum ParameterType {
        STRING, HTML, DATA, WORD
    }

}
