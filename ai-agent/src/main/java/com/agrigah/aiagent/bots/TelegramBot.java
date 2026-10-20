package com.agrigah.aiagent.bots;

import com.agrigah.aiagent.agent.AgentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Bot Telegram par "long polling" (aucune URL publique / webhook nécessaire).
 * Actif seulement si la variable TELEGRAM_BOT_TOKEN est définie.
 */
@Component
@ConditionalOnExpression("!'${telegram.bot.token:}'.isEmpty()")
public class TelegramBot {

    private static final Logger log = LoggerFactory.getLogger(TelegramBot.class);
    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE =
            new ParameterizedTypeReference<>() {};

    private final AgentService agent;
    private final RestClient telegram;
    private volatile boolean running = true;

    public TelegramBot(AgentService agent, @Value("${telegram.bot.token}") String token) {
        this.agent = agent;
        this.telegram = RestClient.create("https://api.telegram.org/bot" + token);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        Thread.ofVirtual().name("telegram-poller").start(this::poll);
        log.info("Bot Telegram démarré (long polling)");
    }

    @SuppressWarnings("unchecked")
    private void poll() {
        long offset = 0;
        while (running) {
            try {
                Map<String, Object> response = telegram.get()
                        .uri("/getUpdates?timeout=30&offset={offset}", offset)
                        .retrieve().body(MAP_TYPE);
                if (response == null) continue;
                List<Map<String, Object>> updates = (List<Map<String, Object>>) response.get("result");
                if (updates == null) continue;

                for (Map<String, Object> update : updates) {
                    offset = ((Number) update.get("update_id")).longValue() + 1;
                    Map<String, Object> message = (Map<String, Object>) update.get("message");
                    if (message == null || message.get("text") == null) continue;

                    long chatId = ((Number) ((Map<String, Object>) message.get("chat")).get("id")).longValue();
                    String text = (String) message.get("text");
                    // chaque message est traité dans son propre thread virtuel
                    Thread.ofVirtual().start(() -> handle(chatId, text));
                }
            } catch (Exception e) {
                log.warn("Erreur Telegram : {}", e.getMessage());
                try { Thread.sleep(3000); } catch (InterruptedException ie) { return; }
            }
        }
    }

    private void handle(long chatId, String text) {
        String answer;
        try {
            answer = agent.ask("telegram-" + chatId, text);
        } catch (Exception e) {
            answer = "Désolé, une erreur est survenue : " + e.getMessage();
        }
        if (answer != null && answer.length() > 4000) answer = answer.substring(0, 4000) + "…";
        telegram.post().uri("/sendMessage")
                .body(Map.of("chat_id", chatId, "text", answer == null ? "(pas de réponse)" : answer))
                .retrieve().toBodilessEntity();
    }
}
