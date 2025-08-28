package org.example.tools;

import java.util.List;
import java.util.Map;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class WeatherTools {

  private final RestTemplate restTemplate = new RestTemplate();

  @Tool(description = "Get 7-day weather forecast for a city")
  public String forecast(String city) {

    String geoUrl = "https://geocoding-api.open-meteo.com/v1/search?name=" + city;
    Map response = restTemplate.getForObject(geoUrl, Map.class);

    if (response != null && response.containsKey("results")) {
      Map firstResult = ((List<Map>) response.get("results")).get(0);

      String url = "https://api.open-meteo.com/v1/forecast?latitude=" + firstResult.get("latitude") +
          "&longitude=" + firstResult.get("longitude") +
          "&daily=temperature_2m_max,temperature_2m_min&timezone=auto";

      response = restTemplate.getForObject(url, Map.class);
      return response != null ? response.toString() : "Weather data not available.";

    }
    return "Weather data not available.";

  }

}
