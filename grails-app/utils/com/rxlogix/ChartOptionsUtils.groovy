package com.rxlogix

import com.rxlogix.json.JsonOutput
import com.rxlogix.util.MiscUtil

class ChartOptionsUtils {
    def static deserialize(String source, def target = null) {
        if (target == null) {
            target = [:]
        }
        def options = MiscUtil.parseJsonText(source)
        return deepMerge(options, target)
    }

    def static serialize(def object) {
        JsonOutput.toJson(object)
    }

    static String serializeToHtml(Map object) {
        List functions = []
        findFunctionsInNode(object, functions)

        String outString = JsonOutput.toJson(object)
        functions?.each {
            String f = it.replaceAll("\n", "\\\\n").replaceAll("\"", '\\\\"')
            outString = outString.replace('"' + f + '"', it)
        }
        return outString
    }

    static void findFunctionsInNode(Map node, List functions) {
        if (!node) return
        if (!(node instanceof Map)) return
        for (String key : node.keySet()) {
            if (node[key] instanceof List) {
                for (Object item : node[key]) {
                    if (item instanceof Map) {
                        findFunctionsInNode(item, functions)
                    }
                }
            }
            if (node[key] instanceof Map) {
                findFunctionsInNode(node[key], functions)
            } else if ((node[key] instanceof String) && (node[key].trim().startsWith("function"))) {
                functions << node[key]
            }
        }
    }

    public static def deepMerge(def source, def target) {
        if (source instanceof Map && target instanceof Map) {
            for (String key : source.keySet()) {
                Object value = source.get(key);
                if (!target.containsKey(key)) {
                    // new value for "key":
                    target.put(key, value);
                } else {
                    // existing value for "key" - recursively deep merge:
                    if (value instanceof Map || value instanceof List) {
                        deepMerge(value, target.get(key));
                    } else {
                        target.put(key, value);
                    }
                }
            }
        } else if (source instanceof List && target instanceof List) {
            source.eachWithIndex{ def entry, int i ->
                if (target.size() <= i) {
                    target.add(entry)
                } else {
                    deepMerge(entry, target[i])
                }
            }
        }
        return target;
    }
}
