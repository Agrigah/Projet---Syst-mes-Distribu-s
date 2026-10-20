package com.agrigah.aiagent.web;

import com.agrigah.aiagent.agent.AgentService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin("*")
public class ChatRestController {

    private final AgentService agentService;

    public ChatRestController(AgentService agentService) {
        this.agentService = agentService;
    }

    /** GET http://localhost:8090/chat?question=Liste des clients */
    @GetMapping("/chat")
    public String chat(@RequestParam String question,
                       @RequestParam(defaultValue = "rest-default") String conversationId) {
        return agentService.ask(conversationId, question);
    }

    /** POST http://localhost:8090/chat   body: {"question":"...", "conversationId":"abc"} */
    @PostMapping("/chat")
    public Map<String, String> chatPost(@RequestBody Map<String, String> body) {
        String conversationId = body.getOrDefault("conversationId", "rest-default");
        String answer = agentService.ask(conversationId, body.get("question"));
        return Map.of("answer", answer);
    }
}
