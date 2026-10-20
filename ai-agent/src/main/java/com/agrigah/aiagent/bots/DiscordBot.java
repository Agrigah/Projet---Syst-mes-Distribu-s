package com.agrigah.aiagent.bots;

import com.agrigah.aiagent.agent.AgentService;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Bot Discord (JDA). Actif seulement si la variable DISCORD_BOT_TOKEN est définie.
 * Pense à activer "MESSAGE CONTENT INTENT" dans le portail développeur Discord.
 */
@Component
@ConditionalOnExpression("!'${discord.bot.token:}'.isEmpty()")
public class DiscordBot extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(DiscordBot.class);

    private final AgentService agent;
    private final String token;

    public DiscordBot(AgentService agent, @Value("${discord.bot.token}") String token) {
        this.agent = agent;
        this.token = token;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(this)
                .build();
        log.info("Bot Discord démarré");
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        String question = event.getMessage().getContentRaw();
        String conversationId = "discord-" + event.getChannel().getId();

        Thread.ofVirtual().start(() -> {
            event.getChannel().sendTyping().queue();
            String answer;
            try {
                answer = agent.ask(conversationId, question);
            } catch (Exception e) {
                answer = "Désolé, une erreur est survenue : " + e.getMessage();
            }
            if (answer == null || answer.isBlank()) answer = "(pas de réponse)";
            if (answer.length() > 1900) answer = answer.substring(0, 1900) + "…";   // limite Discord : 2000
            event.getChannel().sendMessage(answer).queue();
        });
    }
}
