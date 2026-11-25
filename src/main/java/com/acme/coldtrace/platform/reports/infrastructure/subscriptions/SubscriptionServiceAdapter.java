package com.acme.coldtrace.platform.reports.infrastructure.subscriptions;

import com.acme.coldtrace.platform.billing.interfaces.acl.SubscriptionBillingContextFacade;
import java.time.Duration;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

/** Reads entitlement decisions from the Subscription owner over its internal API. */
@Component
public class SubscriptionServiceAdapter implements SubscriptionBillingContextFacade {
  private final RestClient client;

  public SubscriptionServiceAdapter(
      @Value("${coldtrace.subscription.url}") String url,
      @Value("${coldtrace.internal.service-key}") String key) {
    if (key == null || key.length() < 32) throw new IllegalArgumentException("A service key of at least32 characters is required");
    var factory = new org.springframework.http.client.JdkClientHttpRequestFactory(
        java.net.http.HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build());
    factory.setReadTimeout(Duration.ofSeconds(5));
    client = RestClient.builder().baseUrl(url).requestFactory(factory)
        .defaultHeader("X-Service-Key", key).build();
  }

  @Override
  public void initializeBaseSubscriptionForOrganization(Long organizationId) {
    throw new UnsupportedOperationException("Only Identity initializes organization subscriptions");
  }

  @Override
  public Optional<OrganizationEntitlementsSnapshot> fetchEntitlementsByOrganizationId(Long id) {
    try {
      var snapshot = client.get().uri("/internal/organizations/{id}/entitlements", id)
          .retrieve().body(OrganizationEntitlementsSnapshot.class);
      if (snapshot == null || !id.equals(snapshot.organizationId())) throw unavailable(null);
      return Optional.of(snapshot);
    } catch (RestClientException error) {
      throw unavailable(error);
    }
  }

  @Override
  public Optional<EntitlementCheckSnapshot> checkEntitlement(Long id, String entitlement) {
    try {
      var decision=client.get().uri("/internal/organizations/{id}/entitlements/{key}",id,entitlement)
        .retrieve().body(EntitlementCheckSnapshot.class);
      if (decision == null || !id.equals(decision.organizationId())
          || !entitlement.equals(decision.key()) || decision.enabled() == null) throw unavailable(null);
      return Optional.of(decision);
    } catch (RestClientException error) {
      throw unavailable(error);
    }
  }

  @Override
  public boolean canUseEntitlement(Long id, String entitlement) {
    return checkEntitlement(id,entitlement).map(d -> Boolean.TRUE.equals(d.enabled())).orElse(false);
  }

  private static ResponseStatusException unavailable(Exception cause) {
    return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
        "Subscription decision unavailable", cause);
  }

}
