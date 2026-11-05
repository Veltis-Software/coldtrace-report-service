package com.acme.coldtrace.platform.reports.infrastructure.maintenance;

import com.acme.coldtrace.platform.maintenancemanagement.interfaces.acl.MaintenanceManagementContextFacade;
import com.acme.coldtrace.platform.reports.infrastructure.clients.OwnerClient;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

@Component
public class MaintenanceServiceAdapter implements MaintenanceManagementContextFacade {
  private final OwnerClient owner;
  public MaintenanceServiceAdapter(@Value("${coldtrace.maintenance.url}") String url,
      @Value("${coldtrace.internal.service-key}") String key) { owner = new OwnerClient(url, key); }
  public List<MaintenanceScheduleSnapshot> fetchMaintenanceSchedulesByOrganizationIdAndAssetId(Long org, Long asset) {
    return owner.read("/internal/maintenance/organizations/{org}/schedules?assetId={asset}", new ParameterizedTypeReference<>() {}, org, asset);
  }
  public List<TechnicalServiceRequestSnapshot> fetchTechnicalServiceRequestsByOrganizationIdAndIncidentId(Long org, Long incident) {
    return owner.read("/internal/maintenance/organizations/{org}/requests?incidentId={incident}", new ParameterizedTypeReference<>() {}, org, incident);
  }
  public List<TechnicalServiceRequestSnapshot> fetchTechnicalServiceRequestsByOrganizationIdAndAssetId(Long org, Long asset) {
    return owner.read("/internal/maintenance/organizations/{org}/requests?assetId={asset}", new ParameterizedTypeReference<>() {}, org, asset);
  }
}
