package com.rohit.aigateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.rohit.aigateway.service.AiRoutingService;

@RestController
public class GatewayController {

	private final AiRoutingService aiRoutingService;

	public GatewayController(AiRoutingService aiRoutingService) {
		this.aiRoutingService = aiRoutingService;
	}

	@PostMapping("/api/v1/chat")
	public String chat(@RequestBody ChatRequest request) {
		String prompt = request.getPrompt();
		if (prompt == null || prompt.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "prompt must not be null or blank");
		}
		return aiRoutingService.routeRequest(prompt);
	}

	public static class ChatRequest {

		private String prompt;

		public String getPrompt() {
			return prompt;
		}

		public void setPrompt(String prompt) {
			this.prompt = prompt;
		}
	}

}
