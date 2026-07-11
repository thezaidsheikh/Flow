package com.project.flow.execution.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class ExecutionTemplateResolverTest {

    private final ExecutionTemplateResolver resolver = new ExecutionTemplateResolver();

    @Test
    void shouldResolveNestedPlaceholdersFromExecutionContext() {
        WorkflowExecutionContext context = new WorkflowExecutionContext(
            Map.of("repository", "octocat/Hello-World"),
            Map.of("base", "main")
        );
        context.setPreviousNodeOutput(Map.of("branch", "feature/test"));

        Map<String, Object> resolved = resolver.resolveMap(
            Map.of(
                "repository", "${trigger.repository}",
                "base", "${variables.base}",
                "head", "${previous.branch}",
                "title", "Create PR for ${previous.branch}"
            ),
            context
        );

        assertEquals("octocat/Hello-World", resolved.get("repository"));
        assertEquals("main", resolved.get("base"));
        assertEquals("feature/test", resolved.get("head"));
        assertEquals("Create PR for feature/test", resolved.get("title"));
    }

    @Test
    void shouldResolveNestedMapsAndLists() {
        WorkflowExecutionContext context = new WorkflowExecutionContext(Map.of("name", "Flow"), Map.of());

        Object resolved = resolver.resolveValue(
            Map.of(
                "message", "Hello ${trigger.name}",
                "items", java.util.List.of("${trigger.name}", "literal")
            ),
            context
        );

        assertTrue(resolved instanceof Map<?, ?>);
        Map<?, ?> resolvedMap = (Map<?, ?>) resolved;
        assertEquals("Hello Flow", resolvedMap.get("message"));
        assertEquals(java.util.List.of("Flow", "literal"), resolvedMap.get("items"));
    }
}
