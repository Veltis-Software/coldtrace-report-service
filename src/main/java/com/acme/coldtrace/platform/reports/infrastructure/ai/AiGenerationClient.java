package com.acme.coldtrace.platform.reports.infrastructure.ai;

import com.acme.coldtrace.platform.aiassistance.application.commandservices.*;
import com.acme.coldtrace.platform.aiassistance.application.model.*;
import com.acme.coldtrace.platform.aiassistance.domain.model.commands.*;
import com.acme.coldtrace.platform.shared.application.result.Result;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Remote provider port; this service keeps ownership of its operational transaction. */
@Component
public class AiGenerationClient implements AiAssistanceCommandService {
  private final RestClient ai;
  public AiGenerationClient(@Value("${coldtrace.ai.url}") String url,
      @Value("${coldtrace.internal.service-key}") String key) {
    if (key == null || key.length() < 32) throw new IllegalArgumentException("Workload key is required");
    var factory = new JdkClientHttpRequestFactory(java.net.http.HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build());
    factory.setReadTimeout(Duration.ofSeconds(35));
    ai = RestClient.builder().baseUrl(url).requestFactory(factory).defaultHeader("X-Service-Key",key).build();
  }
  private <T> Result<AiGeneratedResponse<T>,AiAssistanceFailure> generate(String path, Object command,
      ParameterizedTypeReference<AiGenerationReply<T>> type) {
    try {
      var reply = ai.post().uri("/internal/ai/v1/generations/" + path).body(command).retrieve().body(type);
      if (reply == null) return Result.failure(new AiAssistanceFailure.ProviderUnavailable());
      if (reply.generated() != null && reply.failure() == null) return Result.success(reply.generated());
      if (reply.failure() == null || reply.generated() != null) return Result.failure(new AiAssistanceFailure.InvalidStructuredOutput("Invalid generation envelope"));
      AiAssistanceFailure failure = switch (reply.failure()) {
        case "ProviderDisabled" -> new AiAssistanceFailure.ProviderDisabled();
        case "ProviderTimeout" -> new AiAssistanceFailure.ProviderTimeout();
        case "ProviderNotConfigured" -> new AiAssistanceFailure.ProviderNotConfigured("remote");
        case "UnsupportedProvider" -> new AiAssistanceFailure.UnsupportedProvider("remote");
        case "InvalidStructuredOutput" -> new AiAssistanceFailure.InvalidStructuredOutput("Provider returned invalid content");
        default -> new AiAssistanceFailure.ProviderUnavailable();
      };
      return Result.failure(failure);
    } catch (RestClientException unavailable) { return Result.failure(new AiAssistanceFailure.ProviderUnavailable()); }
  }
  public Result<AiGeneratedResponse<IncidentResolutionPlanDraft>,AiAssistanceFailure> handle(GenerateIncidentResolutionPlanDraftCommand command) {
    return generate("incident-plan", command, new ParameterizedTypeReference<>() {});
  }
  public Result<AiGeneratedResponse<DashboardInterpretationDraft>,AiAssistanceFailure> handle(GenerateDashboardInterpretationCommand command) {
    return generate("dashboard", command, new ParameterizedTypeReference<>() {});
  }
  public Result<AiGeneratedResponse<ComplianceSummaryDraft>,AiAssistanceFailure> handle(GenerateComplianceSummaryCommand command) {
    return generate("compliance-summary", command, new ParameterizedTypeReference<>() {});
  }
}
