package com.svivanrilski.svirerp.finance;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class FinanceController {

    private final FinanceService service;

    // ── Fund ─────────────────────────────────────────────────────────────────

    @GetMapping("/api/organizations/{orgId}/funds")
    public Page<Fund> listFunds(@PathVariable UUID orgId, Pageable pageable) {
        return service.findFundsByOrg(orgId, pageable);
    }

    @GetMapping("/api/funds/{id}")
    public Fund getFund(@PathVariable UUID id) {
        return service.findFundById(id);
    }

    @PostMapping("/api/funds")
    public ResponseEntity<Fund> createFund(@Valid @RequestBody Fund fund) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createFund(fund));
    }

    @PutMapping("/api/funds/{id}")
    public Fund updateFund(@PathVariable UUID id, @Valid @RequestBody Fund fund) {
        return service.updateFund(id, fund);
    }

    @DeleteMapping("/api/funds/{id}")
    public ResponseEntity<Void> deleteFund(@PathVariable UUID id) {
        service.deleteFund(id);
        return ResponseEntity.noContent().build();
    }

    // ── Account ──────────────────────────────────────────────────────────────

    @GetMapping("/api/organizations/{orgId}/accounts")
    public Page<Account> listAccounts(@PathVariable UUID orgId, Pageable pageable) {
        return service.findAccountsByOrg(orgId, pageable);
    }

    @GetMapping("/api/organizations/{orgId}/accounts/roots")
    public List<Account> rootAccounts(@PathVariable UUID orgId) {
        return service.findRootAccounts(orgId);
    }

    @GetMapping("/api/accounts/{id}")
    public Account getAccount(@PathVariable UUID id) {
        return service.findAccountById(id);
    }

    @GetMapping("/api/accounts/{id}/balance")
    public BigDecimal accountBalance(@PathVariable UUID id) {
        return service.accountBalance(id);
    }

    @PostMapping("/api/accounts")
    public ResponseEntity<Account> createAccount(@Valid @RequestBody Account account) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createAccount(account));
    }

    @PutMapping("/api/accounts/{id}")
    public Account updateAccount(@PathVariable UUID id, @Valid @RequestBody Account account) {
        return service.updateAccount(id, account);
    }

    @DeleteMapping("/api/accounts/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable UUID id) {
        service.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }

    // ── JournalEntry ──────────────────────────────────────────────────────────

    @GetMapping("/api/organizations/{orgId}/journal-entries")
    public Page<JournalEntry> listEntries(@PathVariable UUID orgId, Pageable pageable) {
        return service.findEntriesByOrg(orgId, pageable);
    }

    @GetMapping("/api/journal-entries/{id}")
    public JournalEntry getEntry(@PathVariable UUID id) {
        return service.findEntryById(id);
    }

    @PostMapping("/api/journal-entries")
    public ResponseEntity<JournalEntry> createEntry(@Valid @RequestBody JournalEntry entry) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createEntry(entry));
    }

    @PutMapping("/api/journal-entries/{id}")
    public JournalEntry updateEntry(@PathVariable UUID id, @Valid @RequestBody JournalEntry entry) {
        return service.updateEntry(id, entry);
    }

    /** Posts a draft entry; supply the approver's person ID as a request parameter. */
    @PostMapping("/api/journal-entries/{id}/post")
    public JournalEntry postEntry(@PathVariable UUID id,
            @RequestParam(required = false) UUID approvedBy) {
        return service.postEntry(id, approvedBy);
    }

    @DeleteMapping("/api/journal-entries/{id}")
    public ResponseEntity<Void> deleteEntry(@PathVariable UUID id) {
        service.deleteEntry(id);
        return ResponseEntity.noContent().build();
    }

    // ── JournalLine ──────────────────────────────────────────────────────────

    @GetMapping("/api/journal-entries/{entryId}/lines")
    public List<JournalLine> listLines(@PathVariable UUID entryId) {
        return service.findLinesByEntry(entryId);
    }

    @GetMapping("/api/journal-lines/{id}")
    public JournalLine getLine(@PathVariable UUID id) {
        return service.findLineById(id);
    }

    @PostMapping("/api/journal-lines")
    public ResponseEntity<JournalLine> createLine(@Valid @RequestBody JournalLine line) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createLine(line));
    }

    @DeleteMapping("/api/journal-lines/{id}")
    public ResponseEntity<Void> deleteLine(@PathVariable UUID id) {
        service.deleteLine(id);
        return ResponseEntity.noContent().build();
    }

    // ── Budget ────────────────────────────────────────────────────────────────

    @GetMapping("/api/organizations/{orgId}/budgets")
    public Page<Budget> listBudgets(@PathVariable UUID orgId, Pageable pageable) {
        return service.findBudgetsByOrg(orgId, pageable);
    }

    @GetMapping("/api/budgets/{id}")
    public Budget getBudget(@PathVariable UUID id) {
        return service.findBudgetById(id);
    }

    @PostMapping("/api/budgets")
    public ResponseEntity<Budget> createBudget(@Valid @RequestBody Budget budget) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createBudget(budget));
    }

    @PutMapping("/api/budgets/{id}")
    public Budget updateBudget(@PathVariable UUID id, @Valid @RequestBody Budget budget) {
        return service.updateBudget(id, budget);
    }

    @DeleteMapping("/api/budgets/{id}")
    public ResponseEntity<Void> deleteBudget(@PathVariable UUID id) {
        service.deleteBudget(id);
        return ResponseEntity.noContent().build();
    }

    // ── BankAccount ───────────────────────────────────────────────────────────

    @GetMapping("/api/organizations/{orgId}/bank-accounts")
    public Page<BankAccount> listBankAccounts(@PathVariable UUID orgId, Pageable pageable) {
        return service.findBankAccountsByOrg(orgId, pageable);
    }

    @GetMapping("/api/bank-accounts/{id}")
    public BankAccount getBankAccount(@PathVariable UUID id) {
        return service.findBankAccountById(id);
    }

    @PostMapping("/api/bank-accounts")
    public ResponseEntity<BankAccount> createBankAccount(@Valid @RequestBody BankAccount bankAccount) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createBankAccount(bankAccount));
    }

    @PutMapping("/api/bank-accounts/{id}")
    public BankAccount updateBankAccount(@PathVariable UUID id, @Valid @RequestBody BankAccount bankAccount) {
        return service.updateBankAccount(id, bankAccount);
    }

    @DeleteMapping("/api/bank-accounts/{id}")
    public ResponseEntity<Void> deleteBankAccount(@PathVariable UUID id) {
        service.deleteBankAccount(id);
        return ResponseEntity.noContent().build();
    }

    // ── BankTransaction ───────────────────────────────────────────────────────

    @GetMapping("/api/bank-accounts/{accountId}/transactions")
    public Page<BankTransaction> listTransactions(@PathVariable UUID accountId, Pageable pageable) {
        return service.findTransactionsByAccount(accountId, pageable);
    }

    @GetMapping("/api/bank-transactions/{id}")
    public BankTransaction getTransaction(@PathVariable UUID id) {
        return service.findTransactionById(id);
    }

    @PostMapping("/api/bank-transactions")
    public ResponseEntity<BankTransaction> createTransaction(@Valid @RequestBody BankTransaction tx) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createTransaction(tx));
    }

    @PutMapping("/api/bank-transactions/{id}")
    public BankTransaction updateTransaction(@PathVariable UUID id, @Valid @RequestBody BankTransaction tx) {
        return service.updateTransaction(id, tx);
    }

    @DeleteMapping("/api/bank-transactions/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable UUID id) {
        service.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }

    // ── BankReconciliation ────────────────────────────────────────────────────

    @GetMapping("/api/bank-accounts/{accountId}/reconciliations")
    public Page<BankReconciliation> listReconciliations(@PathVariable UUID accountId, Pageable pageable) {
        return service.findReconciliationsByAccount(accountId, pageable);
    }

    @GetMapping("/api/bank-reconciliations/{id}")
    public BankReconciliation getReconciliation(@PathVariable UUID id) {
        return service.findReconciliationById(id);
    }

    @PostMapping("/api/bank-reconciliations")
    public ResponseEntity<BankReconciliation> createReconciliation(@Valid @RequestBody BankReconciliation recon) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createReconciliation(recon));
    }

    @PutMapping("/api/bank-reconciliations/{id}")
    public BankReconciliation updateReconciliation(@PathVariable UUID id,
            @Valid @RequestBody BankReconciliation recon) {
        return service.updateReconciliation(id, recon);
    }

    @DeleteMapping("/api/bank-reconciliations/{id}")
    public ResponseEntity<Void> deleteReconciliation(@PathVariable UUID id) {
        service.deleteReconciliation(id);
        return ResponseEntity.noContent().build();
    }

    // ── ReconciliationItem ────────────────────────────────────────────────────

    @GetMapping("/api/bank-reconciliations/{reconciliationId}/items")
    public Page<ReconciliationItem> listItems(@PathVariable UUID reconciliationId, Pageable pageable) {
        return service.findItemsByReconciliation(reconciliationId, pageable);
    }

    @GetMapping("/api/reconciliation-items/{id}")
    public ReconciliationItem getItem(@PathVariable UUID id) {
        return service.findItemById(id);
    }

    @PostMapping("/api/reconciliation-items")
    public ResponseEntity<ReconciliationItem> createItem(@Valid @RequestBody ReconciliationItem item) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createItem(item));
    }

    @PutMapping("/api/reconciliation-items/{id}")
    public ReconciliationItem updateItem(@PathVariable UUID id,
            @Valid @RequestBody ReconciliationItem item) {
        return service.updateItem(id, item);
    }

    @DeleteMapping("/api/reconciliation-items/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable UUID id) {
        service.deleteItem(id);
        return ResponseEntity.noContent().build();
    }
}
