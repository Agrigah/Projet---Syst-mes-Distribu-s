package com.agrigah.aiagent.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentConfig {

    private static final String SYSTEM_PROMPT = """
            Tu es un assistant bancaire virtuel pour la banque E-Bank.
            Tu réponds toujours en français, de façon claire, polie et concise.
            Pour toute question sur les clients ou les comptes bancaires, tu DOIS utiliser
            les outils (tools) mis à ta disposition : n'invente jamais de données.
            Si un outil échoue ou si une information n'existe pas, dis-le honnêtement.
            Affiche les soldes avec 2 décimales.
            """;

    @Bean
    public ChatMemory chatMemory() {
        // mémoire de conversation : garde les 20 derniers messages par conversation
        return MessageWindowChatMemory.builder().maxMessages(20).build();
    }

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder,
                                 SyncMcpToolCallbackProvider mcpTools,
                                 ChatMemory chatMemory) {
        return builder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultToolCallbacks(mcpTools)          // outils fournis par le serveur MCP
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }
}
