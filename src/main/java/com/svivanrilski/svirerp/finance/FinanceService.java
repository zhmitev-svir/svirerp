package org.svir.svirerp.finance;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.svir.svirerp.common.ResourceNotFoundException;
import org.svir.svirerp.organization.Organization;
import org.svir.svirerp.organization.OrganizationService;
import org.svir.svirerp.person.Person;
import org.svir.svirerp.person.PersonService;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FinanceService {

    private static final Set<String> FUND_TYPES =
            Set.of("unrestricted", "temporarily_restricted", "permanently_restricted");
    private static final Set<String> ACCOUNT_TYPES =
            Set.of("asset", "liability", "equity", "revenue", "expense");
    private static final Set<String> NORMAL_BALANCES = Set.of("debit", "credit");
    private static final Set<String> ENTRY_TYPES =
            Set.of("general", "adjusting", "closing", "reversing", "opening");
    private static final Set<String> ENTRY_STATUSES = Set.of("draft", "posted", "void");
    private static final Set<String> TX_TYPES =
            Set.of("debit", "credit", "check", "transfer", "fee", "interest", "other");
    private static final Set<String> TX_STATUSES = Set.of("pending", "cleared", "void");
    private static final Set<String> RECON_STATUSES =
            Set.of("in_progress", "completed", "discrepancy");
    private static final Set<String> ITEM_TYPES =
            Set.of("transaction", "deposit_in_transit", "outstanding_check", "adjustment");
    private static final Set<String> BUDGET_PERIODS =
            Set.of("annual", "q1", "q2", "q3", "q4", "monthly");

    private final FundRepository fundRepo;
    private final AccountRepository accountRepo;
    private final JournalEntryRepository journalEntryRepo;
    private final JournalLineRepository journalLineRepo;
    private final BudgetRepository budgetRepo;
    private final BankAccountRepository bankAccountRepo;
    private final BankTransactionRepository bankTxRepo;
    private final BankReconciliationRepository reconciliationRepo;
    private final ReconciliationItemRepository reconItemRepo;
    private final OrganizationService orgService;
    private final PersonService personService;
    private final EntityManager entityManager;

    // ── Fund ─────────────────────────────────────────────────────────────────

    public Page<Fund> findFundsByOrg(UUID orgId, Pageable pageable) {
        return fundRepo.findByOrgId(orgId, pageable);
    }

    public Fund findFundById(UUID id) {
        return fundRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fund", id));
    }

    @Transactional
    public Fund createFund(Fund fund) {
        validate(FUND_TYPES, fund.getFundType(), "fund type");
        Organization org = orgService.findById(fund.getOrg().getId());
        fund.setOrg(org);
        return fundRepo.save(fund);
    }

    @Transactional
    public Fund updateFund(UUID id, Fund patch) {
        validate(FUND_TYPES, patch.getFundType(), "fund type");
        Fund existing = findFundById(id);
        existing.setFundName(patch.getFundName());
        existing.setFundCode(patch.getFundCode());
        existing.setFundType(patch.getFundType());
        existing.setDescription(patch.getDescription());
        existing.setIsRestricted(patch.getIsRestricted());
        existing.setRestrictionPurpose(patch.getRestrictionPurpose());
        existing.setIsActive(patch.getIsActive());
        existing.setOpeningBalance(patch.getOpeningBalance());
        return fundRepo.save(existing);
    }

    @Transactional
    public void deleteFund(UUID id) {
        if (!fundRepo.existsById(id)) throw new ResourceNotFoundException("Fund", id);
        fundRepo.deleteById(id);
    }

    // ── Account ──────────────────────────────────────────────────────────────

    public Page<Account> findAccountsByOrg(UUID orgId, Pageable pageable) {
        return accountRepo.findByOrgId(orgId, pageable);
    }

    /** Returns root accounts; clients can traverse childAccounts for the full hierarchy. */
    public List<Account> findRootAccounts(UUID orgId) {
        return accountRepo.findByOrgIdAndParentAccountIsNull(orgId);
    }

    public Account findAccountById(UUID id) {
        return accountRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", id));
    }

    @Transactional
    public Account createAccount(Account account) {
        validate(ACCOUNT_TYPES, account.getAccountType(), "account type");
        validate(NORMAL_BALANCES, account.getNormalBalance(), "normal balance");
        Organization org = orgService.findById(account.getOrg().getId());
        account.setOrg(org);
        if (account.getParentAccount() != null) {
            Account parent = findAccountById(account.getParentAccount().getId());
            account.setParentAccount(parent);
        }
        return accountRepo.save(account);
    }

    @Transactional
    public Account updateAccount(UUID id, Account patch) {
        validate(ACCOUNT_TYPES, patch.getAccountType(), "account type");
        validate(NORMAL_BALANCES, patch.getNormalBalance(), "normal balance");
        Account existing = findAccountById(id);
        if (existing.getIsSystem()) {
            throw new IllegalArgumentException("System accounts cannot be modified");
        }
        existing.setAccountNumber(patch.getAccountNumber());
        existing.setAccountName(patch.getAccountName());
        existing.setAccountType(patch.getAccountType());
        existing.setAccountSubtype(patch.getAccountSubtype());
        existing.setNormalBalance(patch.getNormalBalance());
        existing.setIsActive(patch.getIsActive());
        existing.setDescription(patch.getDescription());
        return accountRepo.save(existing);
    }

    @Transactional
    public void deleteAccount(UUID id) {
        Account account = findAccountById(id);
        if (account.getIsSystem()) throw new IllegalArgumentException("System accounts cannot be deleted");
        accountRepo.deleteById(id);
    }

    // ── JournalEntry ──────────────────────────────────────────────────────────

    public Page<JournalEntry> findEntriesByOrg(UUID orgId, Pageable pageable) {
        return journalEntryRepo.findByOrgId(orgId, pageable);
    }

    public JournalEntry findEntryById(UUID id) {
        return journalEntryRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("JournalEntry", id));
    }

    @Transactional
    public JournalEntry createEntry(JournalEntry entry) {
        validate(ENTRY_TYPES, entry.getEntryType(), "entry type");
        validate(ENTRY_STATUSES, entry.getStatus(), "entry status");
        Organization org = orgService.findById(entry.getOrg().getId());
        entry.setOrg(org);
        if (entry.getCreatedBy() != null) {
            entry.setCreatedBy(personService.findById(entry.getCreatedBy().getId()));
        }
        return journalEntryRepo.save(entry);
    }

    /**
     * Posts a draft journal entry.  Verifies that the lines balance (totalDebit == totalCredit)
     * before changing status to 'posted'.  The DB CHECK constraint is the final guard.
     */
    @Transactional
    public JournalEntry postEntry(UUID id, UUID approvedById) {
        JournalEntry entry = findEntryById(id);
        if (!"draft".equals(entry.getStatus())) {
            throw new IllegalArgumentException("Only draft entries can be posted");
        }
        if (entry.getTotalDebit().compareTo(entry.getTotalCredit()) != 0) {
            throw new IllegalArgumentException("Journal entry does not balance: debit="
                    + entry.getTotalDebit() + " credit=" + entry.getTotalCredit());
        }
        entry.setStatus("posted");
        entry.setApprovedAt(OffsetDateTime.now());
        if (approvedById != null) {
            Person approver = personService.findById(approvedById);
            entry.setApprovedBy(approver);
        }
        return journalEntryRepo.save(entry);
    }

    @Transactional
    public JournalEntry updateEntry(UUID id, JournalEntry patch) {
        JournalEntry existing = findEntryById(id);
        if ("posted".equals(existing.getStatus())) {
            throw new IllegalArgumentException("Posted entries cannot be edited — void and re-enter");
        }
        validate(ENTRY_TYPES, patch.getEntryType(), "entry type");
        existing.setEntryDate(patch.getEntryDate());
        existing.setDescription(patch.getDescription());
        existing.setReference(patch.getReference());
        existing.setEntryType(patch.getEntryType());
        existing.setTotalDebit(patch.getTotalDebit());
        existing.setTotalCredit(patch.getTotalCredit());
        return journalEntryRepo.save(existing);
    }

    @Transactional
    public void deleteEntry(UUID id) {
        JournalEntry entry = findEntryById(id);
        if ("posted".equals(entry.getStatus())) {
            throw new IllegalArgumentException("Posted entries cannot be deleted — void instead");
        }
        journalEntryRepo.deleteById(id);
    }

    // ── JournalLine ──────────────────────────────────────────────────────────

    public List<JournalLine> findLinesByEntry(UUID journalEntryId) {
        return journalLineRepo.findByJournalEntryId(journalEntryId);
    }

    public JournalLine findLineById(UUID id) {
        return journalLineRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("JournalLine", id));
    }

    @Transactional
    public JournalLine createLine(JournalLine line) {
        validateJournalLineSides(line);
        JournalEntry entry = findEntryById(line.getJournalEntry().getId());
        if ("posted".equals(entry.getStatus())) {
            throw new IllegalArgumentException("Cannot add lines to a posted journal entry");
        }
        Account account = findAccountById(line.getAccount().getId());
        line.setJournalEntry(entry);
        line.setAccount(account);
        if (line.getFund() != null) {
            line.setFund(findFundById(line.getFund().getId()));
        }
        return journalLineRepo.save(line);
    }

    @Transactional
    public void deleteLine(UUID id) {
        JournalLine line = findLineById(id);
        if ("posted".equals(line.getJournalEntry().getStatus())) {
            throw new IllegalArgumentException("Cannot remove lines from a posted journal entry");
        }
        journalLineRepo.deleteById(id);
    }

    // ── Budget ────────────────────────────────────────────────────────────────

    public Page<Budget> findBudgetsByOrg(UUID orgId, Pageable pageable) {
        return budgetRepo.findByOrgId(orgId, pageable);
    }

    public Budget findBudgetById(UUID id) {
        return budgetRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", id));
    }

    @Transactional
    public Budget createBudget(Budget budget) {
        validate(BUDGET_PERIODS, budget.getPeriod(), "budget period");
        Organization org = orgService.findById(budget.getOrg().getId());
        Account account = findAccountById(budget.getAccount().getId());
        budget.setOrg(org);
        budget.setAccount(account);
        if (budget.getFund() != null) {
            budget.setFund(findFundById(budget.getFund().getId()));
        }
        return budgetRepo.save(budget);
    }

    @Transactional
    public Budget updateBudget(UUID id, Budget patch) {
        validate(BUDGET_PERIODS, patch.getPeriod(), "budget period");
        Budget existing = findBudgetById(id);
        existing.setBudgetedAmount(patch.getBudgetedAmount());
        existing.setPeriod(patch.getPeriod());
        existing.setNotes(patch.getNotes());
        existing.setIsApproved(patch.getIsApproved());
        existing.setApprovedDate(patch.getApprovedDate());
        return budgetRepo.save(existing);
    }

    @Transactional
    public void deleteBudget(UUID id) {
        if (!budgetRepo.existsById(id)) throw new ResourceNotFoundException("Budget", id);
        budgetRepo.deleteById(id);
    }

    // ── BankAccount ───────────────────────────────────────────────────────────

    public Page<BankAccount> findBankAccountsByOrg(UUID orgId, Pageable pageable) {
        return bankAccountRepo.findByOrgId(orgId, pageable);
    }

    public BankAccount findBankAccountById(UUID id) {
        return bankAccountRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BankAccount", id));
    }

    @Transactional
    public BankAccount createBankAccount(BankAccount bankAccount) {
        Organization org = orgService.findById(bankAccount.getOrg().getId());
        Account glAccount = findAccountById(bankAccount.getGlAccount().getId());
        bankAccount.setOrg(org);
        bankAccount.setGlAccount(glAccount);
        return bankAccountRepo.save(bankAccount);
    }

    @Transactional
    public BankAccount updateBankAccount(UUID id, BankAccount patch) {
        BankAccount existing = findBankAccountById(id);
        existing.setInstitutionName(patch.getInstitutionName());
        existing.setAccountName(patch.getAccountName());
        existing.setAccountNumberMasked(patch.getAccountNumberMasked());
        existing.setRoutingNumberMasked(patch.getRoutingNumberMasked());
        existing.setAccountType(patch.getAccountType());
        existing.setCurrency(patch.getCurrency());
        existing.setCurrentBalance(patch.getCurrentBalance());
        existing.setOpenedDate(patch.getOpenedDate());
        existing.setIsActive(patch.getIsActive());
        existing.setIsPrimary(patch.getIsPrimary());
        existing.setContactName(patch.getContactName());
        existing.setContactPhone(patch.getContactPhone());
        existing.setNotes(patch.getNotes());
        return bankAccountRepo.save(existing);
    }

    @Transactional
    public void deleteBankAccount(UUID id) {
        if (!bankAccountRepo.existsById(id)) throw new ResourceNotFoundException("BankAccount", id);
        bankAccountRepo.deleteById(id);
    }

    // ── BankTransaction ───────────────────────────────────────────────────────

    public Page<BankTransaction> findTransactionsByAccount(UUID bankAccountId, Pageable pageable) {
        return bankTxRepo.findByBankAccountId(bankAccountId, pageable);
    }

    public BankTransaction findTransactionById(UUID id) {
        return bankTxRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BankTransaction", id));
    }

    @Transactional
    public BankTransaction createTransaction(BankTransaction tx) {
        validate(TX_TYPES, tx.getTransactionType(), "transaction type");
        validate(TX_STATUSES, tx.getStatus(), "transaction status");
        BankAccount bankAccount = findBankAccountById(tx.getBankAccount().getId());
        tx.setBankAccount(bankAccount);
        if (tx.getJournalEntry() != null) {
            tx.setJournalEntry(findEntryById(tx.getJournalEntry().getId()));
        }
        return bankTxRepo.save(tx);
    }

    @Transactional
    public BankTransaction updateTransaction(UUID id, BankTransaction patch) {
        validate(TX_TYPES, patch.getTransactionType(), "transaction type");
        validate(TX_STATUSES, patch.getStatus(), "transaction status");
        BankTransaction existing = findTransactionById(id);
        existing.setTransactionRef(patch.getTransactionRef());
        existing.setTransactionDate(patch.getTransactionDate());
        existing.setPostedDate(patch.getPostedDate());
        existing.setAmount(patch.getAmount());
        existing.setTransactionType(patch.getTransactionType());
        existing.setDescription(patch.getDescription());
        existing.setPayee(patch.getPayee());
        existing.setCheckNumber(patch.getCheckNumber());
        existing.setStatus(patch.getStatus());
        existing.setIsReconciled(patch.getIsReconciled());
        if (patch.getJournalEntry() != null) {
            existing.setJournalEntry(findEntryById(patch.getJournalEntry().getId()));
        }
        return bankTxRepo.save(existing);
    }

    @Transactional
    public void deleteTransaction(UUID id) {
        BankTransaction tx = findTransactionById(id);
        if (tx.getIsReconciled()) {
            throw new IllegalArgumentException("Cannot delete a reconciled transaction");
        }
        bankTxRepo.deleteById(id);
    }

    // ── BankReconciliation ────────────────────────────────────────────────────

    public Page<BankReconciliation> findReconciliationsByAccount(UUID bankAccountId, Pageable pageable) {
        return reconciliationRepo.findByBankAccountId(bankAccountId, pageable);
    }

    public BankReconciliation findReconciliationById(UUID id) {
        return reconciliationRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BankReconciliation", id));
    }

    @Transactional
    public BankReconciliation createReconciliation(BankReconciliation recon) {
        validate(RECON_STATUSES, recon.getStatus(), "reconciliation status");
        BankAccount bankAccount = findBankAccountById(recon.getBankAccount().getId());
        recon.setBankAccount(bankAccount);
        if (recon.getReconciledBy() != null) {
            recon.setReconciledBy(personService.findById(recon.getReconciledBy().getId()));
        }
        BankReconciliation saved = reconciliationRepo.save(recon);
        // Refresh so the GENERATED ALWAYS 'difference' column is populated from the DB.
        entityManager.refresh(saved);
        return saved;
    }

    @Transactional
    public BankReconciliation updateReconciliation(UUID id, BankReconciliation patch) {
        validate(RECON_STATUSES, patch.getStatus(), "reconciliation status");
        BankReconciliation existing = findReconciliationById(id);
        existing.setStatementDate(patch.getStatementDate());
        existing.setStatementBalance(patch.getStatementBalance());
        existing.setBookBalance(patch.getBookBalance());
        existing.setStatus(patch.getStatus());
        existing.setReconciledAt(patch.getReconciledAt());
        existing.setNotes(patch.getNotes());
        BankReconciliation saved = reconciliationRepo.save(existing);
        // Refresh to get the recomputed 'difference' value.
        entityManager.refresh(saved);
        return saved;
    }

    @Transactional
    public void deleteReconciliation(UUID id) {
        if (!reconciliationRepo.existsById(id)) throw new ResourceNotFoundException("BankReconciliation", id);
        reconciliationRepo.deleteById(id);
    }

    // ── ReconciliationItem ────────────────────────────────────────────────────

    public Page<ReconciliationItem> findItemsByReconciliation(UUID reconciliationId, Pageable pageable) {
        return reconItemRepo.findByReconciliationId(reconciliationId, pageable);
    }

    public ReconciliationItem findItemById(UUID id) {
        return reconItemRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ReconciliationItem", id));
    }

    @Transactional
    public ReconciliationItem createItem(ReconciliationItem item) {
        validate(ITEM_TYPES, item.getItemType(), "item type");
        BankReconciliation recon = findReconciliationById(item.getReconciliation().getId());
        BankTransaction tx = findTransactionById(item.getBankTransaction().getId());
        if (reconItemRepo.existsByReconciliationIdAndBankTransactionId(recon.getId(), tx.getId())) {
            throw new IllegalArgumentException("Transaction " + tx.getId()
                    + " is already in this reconciliation");
        }
        item.setReconciliation(recon);
        item.setBankTransaction(tx);
        return reconItemRepo.save(item);
    }

    @Transactional
    public ReconciliationItem updateItem(UUID id, ReconciliationItem patch) {
        ReconciliationItem existing = findItemById(id);
        existing.setIsCleared(patch.getIsCleared());
        existing.setItemType(patch.getItemType());
        existing.setNotes(patch.getNotes());
        return reconItemRepo.save(existing);
    }

    @Transactional
    public void deleteItem(UUID id) {
        if (!reconItemRepo.existsById(id)) throw new ResourceNotFoundException("ReconciliationItem", id);
        reconItemRepo.deleteById(id);
    }

    // ── Reporting helpers ─────────────────────────────────────────────────────

    public BigDecimal accountBalance(UUID accountId) {
        return journalLineRepo.netBalanceForAccount(accountId);
    }

    // ── Internal validators ───────────────────────────────────────────────────

    private void validate(Set<String> allowed, String value, String label) {
        if (value != null && !allowed.contains(value)) {
            throw new IllegalArgumentException("Invalid " + label + ": '" + value
                    + "'. Allowed: " + allowed);
        }
    }

    private void validateJournalLineSides(JournalLine line) {
        boolean hasDebit = line.getDebitAmount() != null && line.getDebitAmount().compareTo(BigDecimal.ZERO) > 0;
        boolean hasCredit = line.getCreditAmount() != null && line.getCreditAmount().compareTo(BigDecimal.ZERO) > 0;
        if (hasDebit == hasCredit) { // both zero or both non-zero
            throw new IllegalArgumentException(
                    "Exactly one of debitAmount or creditAmount must be positive (got debit="
                            + line.getDebitAmount() + ", credit=" + line.getCreditAmount() + ")");
        }
    }
}
