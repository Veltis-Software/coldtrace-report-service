package com.acme.coldtrace.platform.aiassistance.application.model;

import java.util.Map;

/**
 * Backend-owned prompt template and variables for a structured AI request.
 *
 * @param systemInstruction system message that constrains model behavior
 * @param userTemplate user prompt template rendered with Spring AI PromptTemplate
 * @param variables template variables supplied by an application use case
 * @since 1.0
 */
public record AiStructuredPrompt(
        String systemInstruction,
        String userTemplate,
        Map<String, Object> variables
) {
    public AiStructuredPrompt {
        if (systemInstruction == null || systemInstruction.isBlank()) {
            throw new IllegalArgumentException("ai-assistance.prompt.error.system-instruction.required");
        }
        if (userTemplate == null || userTemplate.isBlank()) {
            throw new IllegalArgumentException("ai-assistance.prompt.error.user-template.required");
        }
        variables = variables == null ? Map.of() : Map.copyOf(variables);
    }
}
