package com.rohit.aigateway.service;

public interface AiProviderService {

	String askProvider(String prompt);

	String getProviderName();

	int getPriorityOrder();

}
