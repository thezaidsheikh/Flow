package com.project.flow.execution.state;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class ExecutionTemplateResolver {

    private static final Pattern DOLLAR_PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^}]+)}");
    private static final Pattern CURLY_PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{([^}]+)}}");

    public Map<String, Object> resolveMap(Map<String, Object> source, WorkflowExecutionContext context) {
        Map<String, Object> resolved = new LinkedHashMap<>();
        source.forEach((key, value) -> resolved.put(key, resolveValue(value, context)));
        return resolved;
    }

    public Object resolveValue(Object value, WorkflowExecutionContext context) {
        if (value instanceof String stringValue) {
            // Try ${...} syntax first
            Matcher dollarMatcher = DOLLAR_PLACEHOLDER_PATTERN.matcher(stringValue);
            if (dollarMatcher.matches()) {
                return context.resolvePath(dollarMatcher.group(1));
            }

            StringBuffer buffer = new StringBuffer();
            boolean found = false;
            while (dollarMatcher.find()) {
                found = true;
                Object replacement = context.resolvePath(dollarMatcher.group(1));
                dollarMatcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement == null ? "" : replacement.toString()));
            }
            if (found) {
                dollarMatcher.appendTail(buffer);
                return buffer.toString();
            }

            // Try {{...}} syntax as fallback
            Matcher curlyMatcher = CURLY_PLACEHOLDER_PATTERN.matcher(stringValue);
            if (curlyMatcher.matches()) {
                return context.resolvePath(curlyMatcher.group(1));
            }

            buffer = new StringBuffer();
            found = false;
            while (curlyMatcher.find()) {
                found = true;
                Object replacement = context.resolvePath(curlyMatcher.group(1));
                curlyMatcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement == null ? "" : replacement.toString()));
            }
            if (found) {
                curlyMatcher.appendTail(buffer);
                return buffer.toString();
            }
        }

        if (value instanceof Map<?, ?> map) {
            Map<String, Object> nested = new LinkedHashMap<>();
            map.forEach((key, nestedValue) -> nested.put(String.valueOf(key), resolveValue(nestedValue, context)));
            return nested;
        }

        if (value instanceof Iterable<?> iterable) {
            java.util.List<Object> resolved = new java.util.ArrayList<>();
            iterable.forEach(item -> resolved.add(resolveValue(item, context)));
            return resolved;
        }

        return value;
    }
}
