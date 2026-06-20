package com.rohit.aigateway.service;

import java.util.List;
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
public class GeminiClientService implements AiProviderService {

	private final RestTemplate restTemplate;
	private final String apiKey;
	private final String apiUrl;

	public GeminiClientService(
			RestTemplate restTemplate,
			@Value("${gemini.api.key}") String apiKey,
			@Value("${gemini.api.url}") String apiUrl) {
		this.restTemplate = restTemplate;
		this.apiKey = apiKey;
		this.apiUrl = apiUrl;
	}

	@Override
	@SuppressWarnings("unchecked")
	public String askProvider(String prompt) {
		Map<String, Object> body = Map.of(
				"contents", List.of(
						Map.of("parts", List.of(
								Map.of("text", prompt)))));

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

		Map<String, Object> response = restTemplate.exchange(
				apiUrl + "?key=" + apiKey,
				HttpMethod.POST,
				request,
				new ParameterizedTypeReference<Map<String, Object>>() {
				}).getBody();

		if (response == null) {
			throw new IllegalStateException("Empty response from Gemini");
		}

		List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
		if (candidates == null || candidates.isEmpty()) {
			throw new IllegalStateException("No candidates in Gemini response");
		}

		Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
		List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
		String text = (String) parts.get(0).get("text");
		if (text == null || text.isBlank()) {
			throw new IllegalStateException("No text content in Gemini response");
		}

		return text;
	}

	@Override
	public String getProviderName() {
		return "Gemini";
	}

	@Override
	public int getPriorityOrder() {
		return 1;
	}

}
