package com.acme.coldtrace.platform.assetmanagement.interfaces.acl;

import java.util.List;
import java.util.Optional;

/**
 * Published anti-corruption facade for the asset management bounded context.
 * <p>
 * Monitoring, alerts and reports consume these snapshots instead of importing
 * asset management repositories or aggregates. This keeps cross-context reads
 * explicit and limits the language leaked outside the bounded context.
 *
 * @since 1.0
 */
public interface AssetManagementContextFacade {
    /**
     * Fetches an asset snapshot by organization and asset identifiers.
     *
     * @param organizationId organization identifier
     * @param assetId asset identifier
     * @return asset snapshot when it belongs to the organization
     */
    Optional<AssetSnapshot> fetchAssetByIdAndOrganizationId(Long organizationId, Long assetId);

    /**
     * Fetches an IoT device snapshot by organization and device identifiers.
     *
     * @param organizationId organization identifier
     * @param iotDeviceId IoT device identifier
     * @return IoT device snapshot when it belongs to the organization
     */
    Optional<IoTDeviceSnapshot> fetchIoTDeviceByIdAndOrganizationId(Long organizationId, Long iotDeviceId);

    /**
     * Fetches a gateway snapshot by organization and gateway identifiers.
     *
     * @param organizationId organization identifier
     * @param gatewayId gateway identifier
     * @return gateway snapshot when it belongs to the organization
     */
    Optional<GatewaySnapshot> fetchGatewayByIdAndOrganizationId(Long organizationId, Long gatewayId);

    /**
     * Fetches effective asset settings for an asset.
     *
     * @param organizationId organization identifier
     * @param assetId asset identifier
     * @return asset-specific settings or organization defaults when available
     */
    Optional<AssetSettingsSnapshot> fetchEffectiveAssetSettingsByAssetId(Long organizationId, Long assetId);

    /**
     * Fetches IoT devices assigned to assets in the organization.
     *
     * @param organizationId organization identifier
     * @param assetId optional asset filter
     * @return assigned IoT devices
     */
    List<IoTDeviceSnapshot> fetchAssignedIoTDevices(Long organizationId, Long assetId);

    /**
     * Counts locations for an organization.
     *
     * @param organizationId organization identifier
     * @return number of locations in the organization
     */
    int countLocationsByOrganizationId(Long organizationId);

    /**
     * Counts assets for an organization.
     *
     * @param organizationId organization identifier
     * @return number of assets in the organization
     */
    int countAssetsByOrganizationId(Long organizationId);

    /**
     * Counts IoT devices for an organization.
     *
     * @param organizationId organization identifier
     * @return number of IoT devices in the organization
     */
    int countIotDevicesByOrganizationId(Long organizationId);

    /**
     * Asset data published to other bounded contexts.
     */
    record AssetSnapshot(Long id, Long organizationId, Long locationId, String name) {
    }

    /**
     * IoT device data published to other bounded contexts.
     */
    record IoTDeviceSnapshot(
            Long id,
            Long organizationId,
            Long gatewayId,
            Long assetId,
            String name,
            String status,
            List<String> measurementParameters
    ) {
    }

    /**
     * Gateway data published to other bounded contexts.
     */
    record GatewaySnapshot(Long id, Long organizationId, Long locationId, String status) {
    }

    /**
     * Asset settings data published to other bounded contexts.
     */
    record AssetSettingsSnapshot(
            Double minimumTemperature,
            Double maximumTemperature,
            Double minimumHumidity,
            Double maximumHumidity
    ) {
    }
}
