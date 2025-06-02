package com.rxlogix.publisher;

import groovy.transform.CompileStatic;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CompileStatic
public class PublisherExecutionLog {

    public int errors = 0;
    public int warnings = 0;
    public String fatal;
    private StringBuilder log = new StringBuilder();


    public PublisherExecutionLog() {
    }

    public PublisherExecutionLog(Map in) {
        errors = (int) in.get("errors");
        warnings = (int) in.get("warnings");
        fatal = (String) in.get("fatal");
        log = new StringBuilder((String) in.get("fatal"));
    }

    public String getLog() {
        return log.toString();
    }

    public void append(String text) {
        log.append("\n").append(text);
    }

    public Map<String, Object> toMap() {
        Map<String, Object> out = new HashMap<>();
        out.put("errors", errors);
        out.put("warnings", warnings);
        out.put("fatal", fatal);
        out.put("log", log.toString());
        return out;
    }


    public void writeToLog(Map<String, Object> paramsList) {
        for (String key : paramsList.keySet()) {
            Object value = paramsList.get(key);
            String valString;
            if (value == null)
                valString = null;
            else if (value instanceof Collection) {
                valString = "[DATA:" + value.getClass() + "]  with " + ((Collection) value).size() + " entries";
            } else {
                valString = value.toString();
                if (valString.length() > 100) valString = valString.substring(0, 100) + "...";
            }

            log.append("\n\t\t").append(key).append(" = ").append(valString);
        }
    }

    public void writeToLog(List<WordTemplateExecutor.Parameter> paramsList) {
        for (WordTemplateExecutor.Parameter p : paramsList) {
            String valString;
            if (p.value == null)
                valString = null;
            else {
                if (p.type == WordTemplateExecutor.ParameterType.WORD) {
                    valString = "[Word objects:" + p.value.getClass() + "]";
                } else if (p.type == WordTemplateExecutor.ParameterType.DATA) {
                    valString = "[DATA:" + p.value.getClass() + "]";
                } else {
                    valString = p.value.toString();
                    if (valString.length() > 100) valString = valString.substring(0, 100) + "...";
                }
            }


            log.append("\n\t\t").append(p.name).append("(").append(p.type.name()).append(") = ").append(valString);
        }
    }

    public void logError(String message, Exception e) {
        StringWriter trace = new StringWriter();
        e.printStackTrace(new PrintWriter(trace));
        errors++;
        log.append("\nERROR!!! ").append(message).append("\n").append(trace.toString());
    }

    public void logWarning(String message, Exception e) {
        PrintWriter writer = new PrintWriter(new StringWriter());
        if (e != null) e.printStackTrace(writer);
        warnings++;
        log.append("\nWARNING!!! ").append(message).append("\n");
    }
}
