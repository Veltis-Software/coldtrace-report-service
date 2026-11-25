package com.acme.coldtrace.platform.aiassistance.application.model;

/** Versioned internal result: either generated content or a controlled failure kind. */
public record AiGenerationReply<T>(AiGeneratedResponse<T> generated, String failure) {}
