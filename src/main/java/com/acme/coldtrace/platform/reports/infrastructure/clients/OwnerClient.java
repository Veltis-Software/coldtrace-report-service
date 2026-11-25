package com.acme.coldtrace.platform.reports.infrastructure.clients;

import java.time.Duration;
import java.util.Optional;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

/** Bounded owner queries; only an explicit 404 represents a missing resource. */
public final class OwnerClient {
  private final RestClient client;
  public OwnerClient(String url, String key) {
    if (key == null || key.length() < 32) throw new IllegalArgumentException("Workload key is required");
    var factory = new JdkClientHttpRequestFactory(java.net.http.HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(2)).build());
    factory.setReadTimeout(Duration.ofSeconds(5));
    client = RestClient.builder().baseUrl(url).requestFactory(factory).defaultHeader("X-Service-Key", key).build();
  }
  public <T> Optional<T> find(String path, Class<T> type, Object... variables) {
    try {
      var body = client.get().uri(path, variables).retrieve().body(type);
      if (body == null) throw unavailable(null);
      return Optional.of(body);
    } catch (HttpClientErrorException.NotFound missing) {
      return Optional.empty();
    } catch (RestClientException failure) {
      throw unavailable(failure);
    }
  }
  public <T> T read(String path, ParameterizedTypeReference<T> type, Object... variables) {
    try {
      var body = client.get().uri(path, variables).retrieve().body(type);
      if (body == null) throw unavailable(null);
      return body;
    } catch (RestClientException failure) { throw unavailable(failure); }
  }
  private ResponseStatusException unavailable(Exception cause) {
    return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Owner service unavailable", cause);
  }
}
