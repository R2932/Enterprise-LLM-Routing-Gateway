package com.rohit.aigateway.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AiRoutingService {

	private static final Logger log = LoggerFactory.getLogger(AiRoutingService.class);

	private final List<AiProviderService> providers;

	public AiRoutingService(List<AiProviderService> providers) {
		this.providers = new ArrayList<>(providers);
		this.providers.sort(Comparator.comparingInt(AiProviderService::getPriorityOrder));
	}

	public String routeRequest(String prompt) {
		for (AiProviderService provider : providers) {
			try {
				return provider.askProvider(prompt);
			}
			catch (Exception e) {
				log.warn(provider.getProviderName() + " failed. Moving to next in chain...");
			}
		}
		throw new RuntimeException("All AI providers are currently down.");
	}

}
