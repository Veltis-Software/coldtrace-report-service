package com.acme.coldtrace.platform.reports.infrastructure.monitoring;

import com.acme.coldtrace.platform.monitoring.interfaces.acl.MonitoringContextFacade;
import com.acme.coldtrace.platform.reports.infrastructure.clients.OwnerClient;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

@Component
public class MonitoringServiceAdapter implements MonitoringContextFacade {
  private final OwnerClient owner;
  public MonitoringServiceAdapter(@Value("${coldtrace.monitoring.url}") String url,
      @Value("${coldtrace.internal.service-key}") String key) { owner = new OwnerClient(url, key); }
  public List<SensorReadingSnapshot> fetchSensorReadingsByOrganizationId(Long org) {
    return owner.read("/internal/monitoring/organizations/{org}/readings?limit=1000", new ParameterizedTypeReference<>() {}, org);
  }
  public Optional<SensorReadingSnapshot> fetchSensorReadingByIdAndOrganizationId(Long org, Long id) {
    return owner.find("/internal/monitoring/organizations/{org}/readings/{id}", SensorReadingSnapshot.class, org, id);
  }
}
