package com.bankai.mcpserver.http;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Optional;

import com.bankai.mcpserver.model.Account;

@Component
@HttpExchange(url = "/accounts")
public interface AccountHttpInterface {

    @GetExchange("/byBank/{bankId}")
    ResponseEntity<List<Account>> getAccountsByBank(@PathVariable Long bankId);

    @GetExchange
    ResponseEntity<List<Account>> checkAllAccountsAvailability();

    @GetExchange("/{accountId}")
    ResponseEntity<Optional<Account>> checkAccountsAvailability(@PathVariable Long accountId);

    @PostExchange
    ResponseEntity<Account> checkCreateAvailability(@RequestBody Account account);

    @DeleteExchange("/{accountId}")
    ResponseEntity<String> checkDeleteAvailability(@PathVariable Long accountId);

    @PutExchange("/{accountId}")
    ResponseEntity<Account> checkUpdateAvailability(@PathVariable Long accountId, @RequestBody Account account);

    @GetExchange("/{accountId}/balance")
    ResponseEntity<Double> checkBalanceAvailability(@PathVariable Long accountId);

    @PostExchange("/{accountId}/deposit")
    ResponseEntity<Account> checkDepositAvailability(@PathVariable Long accountId, @RequestParam double value);

    @PostExchange("/{accountId}/draw")
    ResponseEntity<Account> checkDrawAvailability(@PathVariable Long accountId, @RequestParam double value);
}