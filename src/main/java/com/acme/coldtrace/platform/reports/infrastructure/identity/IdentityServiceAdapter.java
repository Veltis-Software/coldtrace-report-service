package com.acme.coldtrace.platform.reports.infrastructure.identity;

import com.acme.coldtrace.platform.iam.interfaces.acl.IamContextFacade;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class IdentityServiceAdapter implements IamContextFacade {
  private final RestClient client;
  public IdentityServiceAdapter(@Value("${coldtrace.identity.url}")String url,
      @Value("${coldtrace.internal.service-key}")String key) {
    if(key==null||key.length()<32)throw new IllegalArgumentException("Internal service key is required");
    var factory=new org.springframework.http.client.JdkClientHttpRequestFactory(
      java.net.http.HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build());
    factory.setReadTimeout(Duration.ofSeconds(5));
    client=RestClient.builder().baseUrl(url).requestFactory(factory).defaultHeader("X-Service-Key",key).build();
  }
  @Override public boolean organizationExists(Long id) {
    return Boolean.TRUE.equals(client.get().uri("/internal/identity/organizations/{id}/exists",id).retrieve().body(Boolean.class));
  }
  @Override public boolean userExistsByIdAndOrganizationId(Long id,Long user) {
    return Boolean.TRUE.equals(client.get().uri("/internal/identity/organizations/{id}/users/{user}/exists",id,user).retrieve().body(Boolean.class));
  }
  @Override public int countUsersByOrganizationId(Long id) {
    Integer count=client.get().uri("/internal/identity/organizations/{id}/users/count",id).retrieve().body(Integer.class);
    if(count==null)throw new IllegalStateException("Identity usage unavailable");return count;
  }
  @Override public List<Long> fetchOrganizationIds() {
    Long[] ids=client.get().uri("/internal/identity/organizations").retrieve().body(Long[].class);
    if(ids==null)throw new IllegalStateException("Identity organizations unavailable");return Arrays.asList(ids);
  }
}
