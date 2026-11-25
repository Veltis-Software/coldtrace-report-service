package com.acme.coldtrace.reports.interfaces.rest;

import com.acme.coldtrace.reports.application.ReadingProjection;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Base64;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@ConditionalOnProperty(name = "coldtrace.pubsub.local-push-enabled", havingValue = "true")
public class LocalPushController {
  private final ReadingProjection projection;
  private final ObjectMapper json;

  public LocalPushController(ReadingProjection projection, ObjectMapper json) {
    this.projection = projection;
    this.json = json;
  }

  public record Message(String data) {}

  public record Push(Message message) {}

  @PostMapping("/internal/pubsub/events")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void receive(@RequestBody Push push) throws Exception {
    projection.accept(json.readTree(Base64.getDecoder().decode(push.message().data())));
  }
}
