package com.acme.coldtrace.platform.reports.infrastructure.assets;

import com.acme.coldtrace.platform.assetmanagement.interfaces.acl.AssetManagementContextFacade;
import com.acme.coldtrace.platform.reports.infrastructure.clients.OwnerClient;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

@Component
public class AssetServiceAdapter implements AssetManagementContextFacade {
  private static final String ROOT = "/internal/assets/organizations/{organizationId}";
  private final OwnerClient owner;
  public AssetServiceAdapter(@Value("${coldtrace.asset.url}") String url,
      @Value("${coldtrace.internal.service-key}") String key) { owner = new OwnerClient(url, key); }
  public Optional<AssetSnapshot> fetchAssetByIdAndOrganizationId(Long org, Long id) {
    return owner.find(ROOT + "/assets/{id}", AssetSnapshot.class, org, id);
  }
  public Optional<IoTDeviceSnapshot> fetchIoTDeviceByIdAndOrganizationId(Long org, Long id) {
    return owner.find(ROOT + "/devices/{id}", IoTDeviceSnapshot.class, org, id);
  }
  public Optional<GatewaySnapshot> fetchGatewayByIdAndOrganizationId(Long org, Long id) {
    return owner.find(ROOT + "/gateways/{id}", GatewaySnapshot.class, org, id);
  }
  public Optional<AssetSettingsSnapshot> fetchEffectiveAssetSettingsByAssetId(Long org, Long id) {
    return owner.find(ROOT + "/assets/{id}/settings", AssetSettingsSnapshot.class, org, id);
  }
  public List<IoTDeviceSnapshot> fetchAssignedIoTDevices(Long org, Long asset) {
    var type = new ParameterizedTypeReference<List<IoTDeviceSnapshot>>() {};
    return asset == null ? owner.read(ROOT + "/assigned-devices", type, org)
        : owner.read(ROOT + "/assigned-devices?assetId={id}", type, org, asset);
  }
  private record Usage(int locations, int assets, int iotDevices) {}
  private Usage usage(Long org) { return owner.find(ROOT + "/usage", Usage.class, org).orElseThrow(); }
  public int countLocationsByOrganizationId(Long org) { return usage(org).locations(); }
  public int countAssetsByOrganizationId(Long org) { return usage(org).assets(); }
  public int countIotDevicesByOrganizationId(Long org) { return usage(org).iotDevices(); }
}
