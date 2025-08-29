package org.example.tools;

import java.util.List;
import java.util.Map;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class WeatherTools {

  public static final String GEOCODING_API_URL = "https://geocoding-api.open-meteo.com/v1/search?name=";
  public static final String FORECAST_URL_PATTERN = "https://api.open-meteo.com/v1/forecast?latitude=%s&longitude=%s&daily=temperature_2m_max,temperature_2m_min&timezone=auto";
  public static final String WEATHER_DATA_NOT_AVAILABLE = "Weather data not available.";
  public static final String RESULTS = "results";
  public static final String LATITUDE = "latitude";
  public static final String LONGITUDE = "longitude";
  private final RestTemplate restTemplate = new RestTemplate();

  @Tool(description = "Get 7-day weather forecast for a city")
  public String forecast(String city) {

    String geoUrl = GEOCODING_API_URL + city;
    Map response = restTemplate.getForObject(geoUrl, Map.class);

    if (response != null && response.containsKey(RESULTS)) {
      Map firstResult = ((List<Map>) response.get(RESULTS)).get(0);

      String url = String.format(
          FORECAST_URL_PATTERN,
          firstResult.get(LATITUDE),
          firstResult.get(LONGITUDE));

      response = restTemplate.getForObject(url, Map.class);
      return response != null ? response.toString() : WEATHER_DATA_NOT_AVAILABLE;

    }
    return WEATHER_DATA_NOT_AVAILABLE;

  }

}
