package com.agrigah.mcpserver;

import com.agrigah.mcpserver.tools.BankTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class McpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpServerApplication.class, args);
    }

    /** Publie toutes les méthodes @Tool de BankTools comme outils MCP. */
    @Bean
    public ToolCallbackProvider bankToolCallbacks(BankTools bankTools) {
        return MethodToolCallbackProvider.builder().toolObjects(bankTools).build();
    }
}
