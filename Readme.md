# Enterprise AI/LLM Proxy Gateway

A highly resilient, enterprise-grade AI Gateway built in Spring Boot. This application acts as a middleware proxy between client applications and external Large Language Models (LLMs). It abstracts away provider unreliability by implementing a dynamic, 3-tier failover routing mechanism, strict circuit-breaking timeouts, and in-memory caching to guarantee 100% uptime and optimize cloud token expenditure.

## 🧠 Architectural Overview

The gateway utilizes the Strategy Pattern and a Chain of Responsibility to evaluate and route incoming prompts through a prioritized hierarchy of AI providers. If a primary cloud provider experiences an outage, rate limit, or timeout, the gateway gracefully catches the failure and cascades the request to secondary cloud providers, ultimately falling back to an air-gapped, locally hosted model.

```bash
graph TD;
    Client[Client Application] -->|POST /api/v1/chat| Controller[Gateway Controller]
    Controller --> Cache{In-Memory Cache}
    Cache -->|Cache Hit| Client
    Cache -->|Cache Miss| Router[AiRoutingService]
    
    Router -->|Priority 1| Gemini[Gemini Cloud API]
    Gemini -.->|Timeout / Error| Router
    
    Router -->|Priority 2| OllamaCloud[Ollama Managed Cloud]
    OllamaCloud -.->|Timeout / Error| Router
    
    Router -->|Priority 3| OllamaLocal[Local Air-Gapped Model]
    
    Gemini -->|Success| Cache
    OllamaCloud -->|Success| Cache
    OllamaLocal -->|Success| Cache
```

## 🚀 Core Engineering Features
- Multi-Tier Failover Routing: Dynamically routes requests starting from Google Gemini (Tier 1) -> Ollama Managed Cloud (Tier 2) -> Local Hardware Model (Tier 3).

- Circuit Breaker & Timeout Safety: Engineered a custom RestTemplate with strict 3000ms connect/read timeouts using SimpleClientHttpRequestFactory. This prevents hanging cloud endpoints from causing application thread starvation.

- Sub-Millisecond Caching: Integrated Spring's @Cacheable abstraction. Duplicate queries bypass external network calls entirely, returning responses in <2ms, drastically reducing API latency and token costs.

- Pluggable SoC Architecture: Built on a strict Interface-driven design (AiProviderService). Integrating new LLMs (like OpenAI or Anthropic) requires zero modification to the core routing logic—simply drop in a new class with a defined priority index.


## 📂 Project Structure

```
src/main/java/com/rohit/aigateway/
├── AigatewayApplication.java       # Bootstraps app & initializes caching
├── config/
│   └── RestTemplateConfig.java     # Network timeout rules & connection pooling
├── controller/
│   └── GatewayController.java      # REST entry point (Presentation Layer)
└── service/
    ├── AiProviderService.java      # Interface blueprint for all LLM integrations
    ├── AiRoutingService.java       # The Failover Manager (Chain of Responsibility)
    ├── GeminiClientService.java    # Priority 1 integration
    ├── OllamaCloudClientService.java # Priority 2 integration
    └── DummyClientService.java     # Configurable Mock/Stub for isolated testing
```

## ⚙️ Setup and Installation

- Java Development Kit (JDK) 21+

- Maven 3.8+

- (Optional) Ollama installed locally for air-gapped fallback testing.

### 1. Clone the Repository
```bash
git clone https://github.com/R2932/Enterprise-LLM-Routing-Gateway.git
```
```bash
cd cd enterprise-ai-gateway
```
### 2. Configure Environment Variables
Navigate to src/main/resources/application.yml and provide your respective API keys. You can add any amount of API services.
(Note: If a key is left blank or invalid, the system will intentionally failover to the next tier).
```yml
server:
  port: 8080

gemini:
  api:
    key: "YOUR_GEMINI_KEY"
    url: "[https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent](https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent)"

ollama:
  cloud:
    key: "YOUR_OLLAMA_CLOUD_KEY"
    url: "[https://ollama.com/v1/chat/completions](https://ollama.com/v1/chat/completions)"
    model: "gpt-oss:120b-cloud"
  local:
    url: "http://localhost:11434/api/generate"
    model: "gemma4"

```
### 3. Build and Run
For Mac
```bash
./mvnw spring-boot:run
```
For Windows
```bash
 .\mvnw.cmd spring-boot:run
```
## 📡 API Usage
### Endpoint: 
```POST /api/v1/chat```
### Request:
```
curl -X POST http://localhost:8080/api/v1/chat \
-H "Content-Type: application/json" \
-d '{"prompt": "Explain the concept of eventual consistency in distributed systems."}'
```
### Response:
```
{
  "response": "Eventual consistency is a theoretical guarantee in distributed computing that, provided no new updates are made to a given data item, all reads of that item will eventually return the last updated value..."
}
```
## 🛡️ Future Roadmap
- [ ] Implement Token Bucket Rate Limiting per IP address.

- [ ] Add support for Server-Sent Events (SSE) to stream token responses.

- [ ] Integrate Vector-based Semantic Caching for fuzzy prompt matching.

- PII Redaction (DLP): Automatically mask sensitive data (SSNs, credit cards) before it leaves your network.

- Prompt Injection Defense: Catch and block malicious "jailbreak" attempts locally.

- Client Authentication: Secure your gateway endpoint with JWTs or custom API keys.