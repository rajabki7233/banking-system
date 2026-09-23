package com.bank.banking;

import com.bank.banking.repository.AccountRepository;
import com.bank.banking.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public AccountService(AccountRepository accountRepository,
                          TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    // ---------- CREATE ----------
    @Transactional
    public Account createAccount(String ownerName) {
        String accountNumber = generateAccountNumber();

        Account account = new Account(accountNumber, ownerName);
        return accountRepository.save(account);
    }

    // ---------- READ ----------
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public Account getAccount(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountNumber));
    }

    // ---------- DEPOSIT ----------
    @Transactional
    public Account deposit(String accountNumber, BigDecimal amount) {
        validateAmount(amount);

        Account account = getAccount(accountNumber);
        account.setBalance(account.getBalance().add(amount));

        Account saved = accountRepository.save(account);
        saveTransaction(saved, "DEPOSIT", amount, null);
        return saved;
    }

    // ---------- WITHDRAW ----------
    @Transactional
    public Account withdraw(String accountNumber, BigDecimal amount) {
        validateAmount(amount);

        Account account = getAccount(accountNumber);

        if (account.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds");
        }

        account.setBalance(account.getBalance().subtract(amount));

        Account saved = accountRepository.save(account);
        saveTransaction(saved, "WITHDRAWAL", amount, null);
        return saved;
    }

    // ---------- TRANSFER ----------
    @Transactional
    public void transfer(String fromNumber, String toNumber, BigDecimal amount) {
        validateAmount(amount);

        if (fromNumber.equals(toNumber)) {
            throw new RuntimeException("Cannot transfer to same account");
        }

        Account from = getAccount(fromNumber);
        Account to = getAccount(toNumber);

        if (from.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds");
        }

        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));

        accountRepository.save(from);
        accountRepository.save(to);

        saveTransaction(from, "TRANSFER_OUT", amount, toNumber);
        saveTransaction(to, "TRANSFER_IN", amount, fromNumber);
    }

    // ---------- HISTORY ----------
    public List<Transaction> getTransactions(String accountNumber) {
        Account account = getAccount(accountNumber);
        return transactionRepository.findByAccountIdOrderByCreatedAtDesc(account.getId());
    }

    // ---------- HELPERS ----------
    private void saveTransaction(Account account, String type, BigDecimal amount, String related) {
        Transaction tx = new Transaction(account, type, amount, account.getBalance(), related);
        transactionRepository.save(tx);
    }

    private String generateAccountNumber() {
        int random = ThreadLocalRandom.current().nextInt(100000, 999999);
        return "ACC-" + random;
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Amount must be positive");
        }
    }
}