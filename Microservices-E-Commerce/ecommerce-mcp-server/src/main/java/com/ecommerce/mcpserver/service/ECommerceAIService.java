package com.ecommerce.mcpserver.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Qualifier;

import com.ecommerce.mcpserver.exchange.ProductHttpInterface;
import com.ecommerce.mcpserver.exchange.ProductHttpInterfaceFallback;
import com.ecommerce.mcpserver.model.Product;



@Service
public class ECommerceAIService {

    private final ProductHttpInterface productHttpInterface;
    private final ProductHttpInterfaceFallback fallback;

    public ECommerceAIService(@Qualifier("produtosHttpInterface") AccountHttpInterface accountHttpInterface, AccountHttpInterfaceFallback fallback) {
        this.accountHttpInterface = accountHttpInterface;
        this.fallback = fallback;
    }

    public List<Account> getAccounts() {
        try {
            return accountHttpInterface.checkAllAccountsAvailability().getBody();
        } catch (Exception e) {
            return fallback.checkAllAccountsAvailability().getBody();
        }
    }

    public Optional<Account> getAccount(Long accountId) {
        try {
            return accountHttpInterface.checkAccountsAvailability(accountId).getBody();
        } catch (Exception e) {
            return fallback.checkAccountsAvailability(accountId).getBody();
        }
    }

    public Account updateAccount(Long accountId, Account account) {
        try {
            return accountHttpInterface.checkUpdateAvailability(accountId, account).getBody();
        } catch (Exception e) {
            return fallback.checkUpdateAvailability(accountId, account).getBody();
        }
    }

    public Account createAccount(Account account) {
        System.out.println("Criando conta " + account.getName() + " com id " + account.getId());
        try {
            return accountHttpInterface.checkCreateAvailability(account).getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return fallback.checkCreateAvailability(account).getBody();
        }
    }

    public String deleteAccount(Long accountId) {
        try {
            return accountHttpInterface.checkDeleteAvailability(accountId).getBody();
        } catch (Exception e) {
            return fallback.checkDeleteAvailability(accountId).getBody();
        }
    }

    public Account depositAccount(Long accountId, double value) {
        System.out.println("Depositando " + value + " na conta " + accountId);
        try {
            return accountHttpInterface.checkDepositAvailability(accountId, value).getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return fallback.checkDepositAvailability(accountId, value).getBody();
        }
    }

    public Account drawAccount(Long accountId, double value) {
        System.out.println("Sacando " + value + " na conta " + accountId);
        try {
            return accountHttpInterface.checkDrawAvailability(accountId, value).getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return fallback.checkDrawAvailability(accountId, value).getBody();
        }
    }

    public double balanceAccount(Long accountId) {
        try {
            return accountHttpInterface.checkBalanceAvailability(accountId).getBody();
        } catch (Exception e) {
            return fallback.checkBalanceAvailability(accountId).getBody();
        }
    }
}

// package com.ecommerce.mcpserver.service;

// import java.util.List;
// import java.util.Optional;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;
// import com.ecommerce.mcpserver.feign.AccountServiceInterface;
// //import com.ecommerce.mcpserver.feign.AccountServiceFallback;
// import com.ecommerce.mcpserver.model.Account;

// @Service
// public class BankAIService {

//     @Autowired
//     private AccountServiceInterface accountServiceInterface;
    
//     public List<Account> getAccounts() { 
//        return accountServiceInterface.checkAllAccountsAvailability().getBody();        
//     }

//     public Optional<Account> getAccount(Long accountId){
//         return accountServiceInterface.checkAccountsAvailability(accountId).getBody();
//     }

//     public Account updateAccount(Long accountId,Account account){
//         return accountServiceInterface.checkUpdateAvailability(accountId,account).getBody();
//     }

//     public Account createAccount(Account account){
//         System.out.println("Criando conta " + account.getName() + " com id " + account.getId());
//         return accountServiceInterface.checkCreateAvailability(account).getBody();
//     }

//     public String deleteAccount(Long accountId){
//         return accountServiceInterface.checkDeleteAvailability(accountId).getBody();
//     }

//     public Account depositAccount(Long accountId, double value){
//         System.out.println("Depositando " + value + " na conta " + accountId);
//         return accountServiceInterface.checkDepositAvailability(accountId,value).getBody();
//     }

//     public Account drawAccount(Long accountId, double value){
//         System.out.println("Sacando " + value + " na conta " + accountId);
//         return accountServiceInterface.checkDrawAvailability(accountId,value).getBody();
//     }

//     public double balanceAccount(Long accountId){
//         return accountServiceInterface.checkBalanceAvailability(accountId).getBody();
//     }
// }

