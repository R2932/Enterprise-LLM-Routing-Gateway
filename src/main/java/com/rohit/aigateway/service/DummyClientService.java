package com.rohit.aigateway.service;

import org.springframework.stereotype.Service;

@Service
public class DummyClientService implements AiProviderService {

    @Override
    public int getPriorityOrder() {
        // Priority 2: It will wait for Gemini (Priority 1) to fail first
        return 2; 
    }

    @Override
    public String getProviderName() {
        return "Dummy Mock Model";
    }

    @Override
    public String askProvider(String prompt) {
        // Instead of making a network call, we just return a hardcoded success string!
        System.out.println("--> Dummy Model was called successfully!");
        return "SUCCESS! The router skipped the broken Gemini and reached the Dummy Model. You asked: '" + prompt + "'";
    }
}