package com.acme.coldtrace.reports.infrastructure;

import com.acme.coldtrace.shared.IdentitySession;
import java.time.Duration;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;
import org.springframework.web.server.ResponseStatusException;

@Component
public class IdentityGateway {
  private final RestClient client;
  public IdentityGateway(@Value("${coldtrace.identity.url}") String url) {
    var factory = new org.springframework.http.client.JdkClientHttpRequestFactory(
        java.net.http.HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build());
    factory.setReadTimeout(Duration.ofSeconds(3));
    client = RestClient.builder().baseUrl(url).requestFactory(factory).build();
  }
  public IdentitySession session(String bearer) {
    if (bearer == null || !bearer.startsWith("Bearer ")) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    try {
      var value = client.get().uri("/api/v1/session/context").header("Authorization", bearer)
          .retrieve().body(IdentitySession.class);
      if (value == null || value.userId() == null || value.organizationId() == null)
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
      return value;
    } catch (HttpClientErrorException e) { throw new ResponseStatusException(HttpStatus.UNAUTHORIZED); }
      catch (RestClientException e) { throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Identity unavailable"); }
  }
  public List<UserRef> members(String bearer, IdentitySession session) {
    try {
      var values = client.get().uri("/api/v1/organizations/{id}/users", session.organizationId())
          .header("Authorization", bearer).retrieve().body(Member[].class);
      if (values == null) return List.of();
      return Arrays.stream(values).filter(user -> Objects.equals(user.organizationId(), session.organizationId()))
          .map(user -> new UserRef(user.id(), user.firstName() + " " + user.lastName())).toList();
    } catch (RestClientException e) { throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Identity unavailable"); }
  }
  public record Member(Long id, Long organizationId, String firstName, String lastName) {}
  public record UserRef(Long userId, String fullName) {}
}
