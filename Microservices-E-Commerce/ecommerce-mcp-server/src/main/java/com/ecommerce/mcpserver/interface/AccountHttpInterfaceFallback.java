package com.ecommerce.mcpserver.interface;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.ecommerce.mcpserver.model.Order;
import com.ecommerce.mcpserver.model.Product;

@Component
public class AccountHttpInterfaceFallback implements AccountHttpInterface {

    private static final Logger logger = LoggerFactory.getLogger(AccountHttpInterfaceFallback.class);

    @Override
    public ResponseEntity<List<Account>> getAccountsByBank(Long bankId) {
        logger.warn("Fallback acionado para getAccountsByBank para bankId: {}. Retornando lista vazia.", bankId);
        return ResponseEntity.status(503).body(Collections.emptyList());
    }

    @Override
    public ResponseEntity<List<Account>> checkAllAccountsAvailability() {
        logger.warn("Fallback acionado para checkAllAccountsAvailability. Retornando lista vazia.");
        return ResponseEntity.status(503).body(Collections.emptyList());
    }

    @Override
    public ResponseEntity<Optional<Account>> checkAccountsAvailability(Long accountId) {
        logger.warn("Fallback acionado para checkAccountsAvailability para accountId: {}. Retornando Optional vazio.", accountId);
        return ResponseEntity.status(503).body(Optional.empty());
    }

    @Override
    public ResponseEntity<Account> checkCreateAvailability(Account account) {
        logger.warn("Fallback acionado para checkCreateAvailability. Retornando nova Account.", account);
        return ResponseEntity.status(503).body(new Account());
    }

    @Override
    public ResponseEntity<String> checkDeleteAvailability(Long accountId) {
        logger.warn("Fallback acionado para checkDeleteAvailability para accountId: {}. Retornando mensagem de erro.", accountId);
        return ResponseEntity.status(503).body("O serviço de deletar contas está indisponível.");
    }

    @Override
    public ResponseEntity<Account> checkUpdateAvailability(Long accountId, Account account) {
        logger.warn("Fallback acionado para checkUpdateAvailability para accountId: {}. Retornando nova Account.", accountId);
        return ResponseEntity.status(503).body(new Account());
    }

    @Override
    public ResponseEntity<Double> checkBalanceAvailability(Long accountId) {
        logger.warn("Fallback acionado para checkBalanceAvailability para accountId: {}. Retornando 0.0.", accountId);
        return ResponseEntity.status(503).body(0.0);
    }

    @Override
    public ResponseEntity<Account> checkDepositAvailability(Long accountId, double value) {
        logger.warn("Fallback acionado para checkDepositAvailability para accountId: {}. Retornando nova Account.", accountId);
        return ResponseEntity.status(503).body(new Account());
    }

    @Override
    public ResponseEntity<Account> checkDrawAvailability(Long accountId, double value) {
        logger.warn("Fallback acionado para checkDrawAvailability para accountId: {}. Retornando nova Account.", accountId);
        return ResponseEntity.status(503).body(new Account());
    }
}
