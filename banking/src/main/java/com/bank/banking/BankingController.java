package com.bank.banking;

import com.bank.banking.dto.AmountRequest;
import com.bank.banking.dto.CreateAccountRequest;
import com.bank.banking.dto.TransferRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class BankingController {

    private final AccountService accountService;

    // Constructor injection — Spring passes the service automatically
    public BankingController(AccountService accountService) {
        this.accountService = accountService;
    }

    // POST /api/accounts
    @PostMapping("/accounts")
    public ResponseEntity<Account> createAccount(@RequestBody CreateAccountRequest request) {
        Account account = accountService.createAccount(request.getOwnerName());
        return ResponseEntity.ok(account);
    }

    // GET /api/accounts
    @GetMapping("/accounts")
    public ResponseEntity<List<Account>> getAllAccounts() {
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    // GET /api/accounts/{accountNumber}
    @GetMapping("/accounts/{accountNumber}")
    public ResponseEntity<Account> getAccount(@PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.getAccount(accountNumber));
    }

    // POST /api/accounts/{accountNumber}/deposit
    @PostMapping("/accounts/{accountNumber}/deposit")
    public ResponseEntity<Account> deposit(@PathVariable String accountNumber,
                                           @RequestBody AmountRequest request) {
        return ResponseEntity.ok(accountService.deposit(accountNumber, request.getAmount()));
    }

    // POST /api/accounts/{accountNumber}/withdraw
    @PostMapping("/accounts/{accountNumber}/withdraw")
    public ResponseEntity<Account> withdraw(@PathVariable String accountNumber,
                                            @RequestBody AmountRequest request) {
        return ResponseEntity.ok(accountService.withdraw(accountNumber, request.getAmount()));
    }

    // POST /api/transfers
    @PostMapping("/transfers")
    public ResponseEntity<Map<String, String>> transfer(@RequestBody TransferRequest request) {
        accountService.transfer(
                request.getFromAccountNumber(),
                request.getToAccountNumber(),
                request.getAmount()
        );
        return ResponseEntity.ok(Map.of("message", "Transfer successful"));
    }

    // GET /api/accounts/{accountNumber}/transactions
    @GetMapping("/accounts/{accountNumber}/transactions")
    public ResponseEntity<List<Transaction>> transactions(@PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.getAccount(accountNumber).getTransactions());
    }
}