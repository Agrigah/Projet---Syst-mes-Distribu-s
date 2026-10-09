@echo off
REM ============================================================
REM  Demarre toute l'architecture dans l'ordre (Windows)
REM  1) Definis ta cle OpenAI AVANT de lancer ce script :
REM       setx OPENAI_API_KEY "sk-..."     (puis rouvre le terminal)
REM  2) Optionnel : TELEGRAM_BOT_TOKEN et DISCORD_BOT_TOKEN
REM ============================================================
set ROOT=%~dp0

start "1-discovery-service" cmd /k "cd /d %ROOT%discovery-service && mvnw.cmd spring-boot:run"
timeout /t 25 /nobreak >nul
start "2-customer-service"  cmd /k "cd /d %ROOT%customer-service && mvnw.cmd spring-boot:run"
start "3-ebank-service"     cmd /k "cd /d %ROOT%ebank-service && mvnw.cmd spring-boot:run"
timeout /t 20 /nobreak >nul
start "4-gateway-service"   cmd /k "cd /d %ROOT%gateway-service && mvnw.cmd spring-boot:run"
timeout /t 15 /nobreak >nul
start "5-mcp-server"        cmd /k "cd /d %ROOT%mcp-server && mvnw.cmd spring-boot:run"
timeout /t 20 /nobreak >nul
start "6-ai-agent"          cmd /k "cd /d %ROOT%ai-agent && mvnw.cmd spring-boot:run"
