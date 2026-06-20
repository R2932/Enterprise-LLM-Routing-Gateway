package com.rohit.aigateway.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class OllamaCloudClientService implements AiProviderService {

	private final RestTemplate restTemplate;
	private final String apiUrl;
	private final String apiKey;
	private final String model;

	public OllamaCloudClientService(
			RestTemplate restTemplate,
			@Value("${ollama.api.url}") String apiUrl,
			@Value("${ollama.api.key}") String apiKey,
			@Value("${ollama.api.model}") String model) {
		this.restTemplate = restTemplate;
		this.apiUrl = apiUrl;
		this.apiKey = apiKey;
		this.model = model;
	}

	@Override
	public String askProvider(String prompt) {
		Map<String, Object> body = Map.of(
				"model", model,
				"prompt", prompt,
				"stream", false);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(apiKey);
		HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

		Map<String, Object> response = restTemplate.exchange(
				apiUrl,
				HttpMethod.POST,
				request,
				new ParameterizedTypeReference<Map<String, Object>>() {
				}).getBody();

		return extractResponse(response);
	}

	@Override
	public String getProviderName() {
		return "Ollama Cloud";
	}

	@Override
	public int getPriorityOrder() {
		return 2;
	}

	private String extractResponse(Map<String, Object> response) {
		if (response == null) {
			throw new IllegalStateException("Empty response from Ollama Cloud");
		}

		String text = (String) response.get("response");
		if (text == null || text.isBlank()) {
			throw new IllegalStateException("No text content in Ollama Cloud response");
		}

		return text;
	}

}
