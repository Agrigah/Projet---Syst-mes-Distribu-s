# Projet E-Bank – Micro-services + Agent IA (Spring Boot 4.1 / Spring Cloud / Spring AI 2.0)

## Architecture

| Module | Port | Rôle |
|---|---|---|
| discovery-service | 8761 | Eureka Server |
| customer-service | 8056 | Gestion des clients (H2) |
| ebank-service | 8057 | Comptes bancaires (H2) + **OpenFeign** vers customer + **Circuit Breaker Resilience4J** |
| gateway-service | 9999 | Spring Cloud Gateway (routes `lb://`) |
| **mcp-server** | 8058 | Serveur MCP : expose les micro-services comme outils (`/mcp`) |
| **ai-agent** | 8090 | Agent IA (Spring AI + OpenAI) + client MCP + API REST + page web + bots Telegram/Discord |

Flux : `Utilisateur → ai-agent → (MCP) → mcp-server → gateway → customer/ebank-service`

## Prérequis
- Java 25, accès Internet (Maven)
- Une clé OpenAI **dans une variable d'environnement** (ne jamais l'écrire dans le code) :
  - Windows : `setx OPENAI_API_KEY "sk-..."` puis rouvrir le terminal
  - Linux/Mac : `export OPENAI_API_KEY=sk-...`

## Démarrage
Double-clic sur `start-all.bat` (Windows), ou manuellement, **dans cet ordre** :
`discovery-service → customer-service → ebank-service → gateway-service → mcp-server → ai-agent`
(dans chaque dossier : `mvnw spring-boot:run`).

## Partie 1 – Tests
- Eureka : http://localhost:8761
- Swagger customer : http://localhost:8056/swagger-ui/index.html
- Swagger ebank : http://localhost:8057/swagger-ui/index.html
- Via la gateway : `http://localhost:9999/customers`, `http://localhost:9999/accounts`
- Collection Postman : `postman/Ebank.postman_collection.json` (Import dans Postman)
- **Test Feign** : `GET /accounts` → chaque compte contient maintenant son `customer`.
- **Test Circuit Breaker** : arrête customer-service puis appelle `GET /accounts` →
  `customer.name = "Service client indisponible"` (fallback) au lieu d'une erreur 500.
  Relance customer-service : après ~10 s le circuit se referme.

## Partie 2 – Tests
1. **MCP** : `npx @modelcontextprotocol/inspector` → Transport *Streamable HTTP*, URL `http://localhost:8058/mcp`
   → *List Tools* (7 outils) puis appelle `getAllCustomers`.
2. **Chatbot REST** : `GET http://localhost:8090/chat?question=Liste des clients` (Postman)
3. **Interface web** : http://localhost:8090
4. **Telegram** : créer un bot avec @BotFather → copier le token →
   `setx TELEGRAM_BOT_TOKEN "123:ABC"` → relancer ai-agent → écrire au bot.
5. **Discord** : https://discord.com/developers/applications → New Application → Bot →
   activer **MESSAGE CONTENT INTENT** → copier le token → `setx DISCORD_BOT_TOKEN "..."` →
   inviter le bot (OAuth2 → URL Generator : scope `bot`, permissions *Send Messages* + *Read Messages*) → relancer ai-agent.

