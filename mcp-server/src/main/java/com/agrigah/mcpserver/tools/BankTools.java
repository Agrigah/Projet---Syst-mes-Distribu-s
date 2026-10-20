package com.agrigah.mcpserver.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Fonctionnalités des micro-services exposées via MCP.
 * Chaque méthode @Tool devient un "tool" que le LLM peut appeler.
 */
@Service
public class BankTools {

    private static final ParameterizedTypeReference<List<Map<String, Object>>> LIST_TYPE =
            new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE =
            new ParameterizedTypeReference<>() {};

    private final RestClient gateway;

    public BankTools(@Value("${bank.gateway.url}") String gatewayUrl) {
        this.gateway = RestClient.create(gatewayUrl);
    }

    // ---------------- Customer-Service ----------------

    @Tool(description = "Retourne la liste de tous les clients de la banque (id, nom, email)")
    public List<Map<String, Object>> getAllCustomers() {
        return gateway.get().uri("/customers").retrieve().body(LIST_TYPE);
    }

    @Tool(description = "Retourne un client à partir de son identifiant numérique")
    public Map<String, Object> getCustomerById(
            @ToolParam(description = "Identifiant du client, ex: 1") Long customerId) {
        return gateway.get().uri("/customers/{id}", customerId).retrieve().body(MAP_TYPE);
    }

    @Tool(description = "Crée un nouveau client dans la banque")
    public Map<String, Object> createCustomer(
            @ToolParam(description = "Nom du client") String name,
            @ToolParam(description = "Adresse email du client") String email) {
        return gateway.post().uri("/customers")
                .body(Map.of("name", name, "email", email))
                .retrieve().body(MAP_TYPE);
    }

    // ---------------- EBank-Service ----------------

    @Tool(description = "Retourne la liste de tous les comptes bancaires avec le propriétaire (customer) de chaque compte")
    public List<Map<String, Object>> getAllBankAccounts() {
        return gateway.get().uri("/accounts").retrieve().body(LIST_TYPE);
    }

    @Tool(description = "Retourne un compte bancaire à partir de son identifiant (UUID)")
    public Map<String, Object> getBankAccountById(
            @ToolParam(description = "Identifiant UUID du compte bancaire") String accountId) {
        return gateway.get().uri("/accounts/{id}", accountId).retrieve().body(MAP_TYPE);
    }

    @Tool(description = "Retourne tous les comptes bancaires d'un client donné")
    public List<Map<String, Object>> getBankAccountsByCustomer(
            @ToolParam(description = "Identifiant numérique du client, ex: 1") Long customerId) {
        return gateway.get().uri("/accounts/customer/{id}", customerId).retrieve().body(LIST_TYPE);
    }

    @Tool(description = "Crée un nouveau compte bancaire pour un client")
    public Map<String, Object> createBankAccount(
            @ToolParam(description = "Type de compte : CURRENT-ACCOUNT ou SAVING-ACCOUNT") String type,
            @ToolParam(description = "Solde initial") double balance,
            @ToolParam(description = "Identifiant du client propriétaire") Long customerId) {
        return gateway.post().uri("/accounts")
                .body(Map.of("type", type, "balance", balance, "customerId", customerId))
                .retrieve().body(MAP_TYPE);
    }
}
