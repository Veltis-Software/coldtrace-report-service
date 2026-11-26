package com.acme.coldtrace.platform.reports.infrastructure.alerts;

import com.acme.coldtrace.platform.alerts.interfaces.acl.AlertsContextFacade;
import com.acme.coldtrace.platform.reports.infrastructure.clients.OwnerClient;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

@Component
public class AlertServiceAdapter implements AlertsContextFacade {
  private final OwnerClient owner;
  public AlertServiceAdapter(@Value("${coldtrace.alert.url}") String url,
      @Value("${coldtrace.internal.service-key}") String key) { owner = new OwnerClient(url, key); }
  public List<IncidentSnapshot> fetchIncidentsByOrganizationId(Long org) {
    return owner.read("/internal/alerts/organizations/{org}/incidents", new ParameterizedTypeReference<>() {}, org);
  }
  public Optional<IncidentSnapshot> fetchIncidentByIdAndOrganizationId(Long org, Long id) {
    return owner.find("/internal/alerts/organizations/{org}/incidents/{id}", IncidentSnapshot.class, org, id);
  }
}
