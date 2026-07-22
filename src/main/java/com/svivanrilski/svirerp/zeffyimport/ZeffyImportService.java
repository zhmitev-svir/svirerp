package com.svivanrilski.svirerp.zeffyimport;

import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.svivanrilski.svirerp.common.ResourceNotFoundException;
import com.svivanrilski.svirerp.finance.Account;
import com.svivanrilski.svirerp.finance.FinanceService;
import com.svivanrilski.svirerp.finance.Fund;
import com.svivanrilski.svirerp.membership.MembershipService;
import com.svivanrilski.svirerp.organization.Organization;
import com.svivanrilski.svirerp.organization.OrganizationService;
import com.svivanrilski.svirerp.person.Person;
import com.svivanrilski.svirerp.person.PersonService;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Orchestrates the Zeffy CSV import: preview (parse + persist every row with a computed outcome,
 * no writes to Person/Member/MemberPayment/JournalEntry yet), campaign-to-fund mapping, and
 * commit (apply every still-eligible row via {@link ZeffyImportRowApplier}, a different bean, so
 * each row gets its own transaction).
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ZeffyImportService {

    private static final int OUTCOME_DETAIL_MAX_LENGTH = 500;

    private static final String COL_PAYMENT_DATE = "Payment Date (America/Chicago)";
    private static final String COL_PAYMENT_TIME = "Payment Time (America/Chicago)";
    private static final String COL_AMOUNT = "Total Amount";
    private static final String COL_PAYMENT_STATUS = "Payment Status";
    private static final String COL_PAYOUT_DATE = "Payout Date";
    private static final String COL_FIRST_NAME = "First Name";
    private static final String COL_LAST_NAME = "Last Name";
    private static final String COL_EMAIL = "Email";
    private static final String COL_ADDRESS = "Address";
    private static final String COL_CITY = "City";
    private static final String COL_POSTAL_CODE = "Postal Code";
    private static final String COL_STATE = "State";
    private static final String COL_COUNTRY = "Country";
    private static final String COL_TAX_RECEIPT_NUMBER = "Tax Receipt #";
    private static final String COL_TAX_RECEIPT_URL = "Tax Receipt URL";
    private static final String COL_CAMPAIGN_TITLE = "Campaign Title";

    private static final List<DateTimeFormatter> DATE_FORMATS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("M/d/yyyy"),
            DateTimeFormatter.ofPattern("M/d/yy"));

    /** Columns that are real dates in an .xlsx export — extracted as an actual date value rather
     *  than trusting the cell's display format (see {@link #cellToText}). Deliberately does NOT
     *  include Payment Time: Excel also flags time-only cells as "date formatted" (a time is a
     *  fractional day internally), but that column is only ever used as opaque dedupe-key text. */
    private static final Set<String> DATE_COLUMNS = Set.of(COL_PAYMENT_DATE, COL_PAYOUT_DATE);

    private final ZeffyImportBatchRepository batchRepo;
    private final ZeffyImportRowRepository rowRepo;
    private final ZeffyCampaignMappingRepository mappingRepo;
    private final ZeffyImportRowApplier rowApplier;
    private final OrganizationService orgService;
    private final PersonService personService;
    private final MembershipService membershipService;
    private final FinanceService financeService;

    public record CampaignMappingRequest(String campaignTitle, UUID fundId) {
    }

    public record ZeffyImportSummary(
            UUID batchId,
            int totalRows,
            long readyCount,
            long duplicateCount,
            long skippedStatusCount,
            long unmappedCampaignCount,
            long errorCount,
            long committedCount,
            long newPersonCount,
            long newMemberCount,
            BigDecimal totalAmountReady,
            List<String> unmappedCampaignTitles) {
    }

    public record ZeffyImportCommitResult(UUID batchId, int committed, int failed, int stillUnmappedCampaign) {
    }

    // ── Batches ──────────────────────────────────────────────────────────────

    /** Defaults to newest-first only when the caller didn't ask for a specific column sort. */
    public Page<ZeffyImportBatch> findBatchesByOrg(UUID orgId, Pageable pageable) {
        Pageable effective = pageable.getSort().isUnsorted()
                ? PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "uploadedAt"))
                : pageable;
        return batchRepo.findByOrgId(orgId, effective);
    }

    public ZeffyImportBatch findBatchById(UUID id) {
        return batchRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ZeffyImportBatch", id));
    }

    public Page<ZeffyImportRow> findRows(UUID batchId, Pageable pageable) {
        return rowRepo.findByBatchIdOrderByRowNumber(batchId, pageable);
    }

    // ── Preview ──────────────────────────────────────────────────────────────

    @Transactional
    public ZeffyImportBatch previewImport(UUID orgId, MultipartFile file) {
        Organization org = orgService.findById(orgId);
        ZeffyImportBatch batch = batchRepo.save(ZeffyImportBatch.builder()
                .org(org)
                .fileName(file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload")
                .status("previewed")
                .rowCount(0)
                .build());

        List<Map<String, String>> rawRows;
        try {
            rawRows = parseFile(file);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read uploaded Zeffy file", e);
        }

        Set<String> seenDedupeKeysInBatch = new HashSet<>();
        int rowCount = 0;
        for (Map<String, String> raw : rawRows) {
            int rowNumber = rowCount + 2; // header is row 1; first data row is row 2
            ZeffyImportRow row = ZeffyImportRow.builder()
                    .batch(batch)
                    .org(org)
                    .rowNumber(rowNumber)
                    .outcome("pending_preview")
                    .isNewPerson(false)
                    .isNewMember(false)
                    .build();
            try {
                populateRawFields(row, raw);
                computeOutcome(row, orgId, seenDedupeKeysInBatch);
            } catch (Exception ex) {
                row.setOutcome("error");
                row.setOutcomeDetail(truncate(ex.getMessage()));
            }
            rowRepo.save(row);
            rowCount++;
        }

        batch.setRowCount(rowCount);
        return batchRepo.save(batch);
    }

    /** Dispatches on file extension — Zeffy's real export is an .xlsx spreadsheet; .csv is also
     *  accepted since that's a common "save as" alternative. */
    private List<Map<String, String>> parseFile(MultipartFile file) throws IOException {
        String name = file.getOriginalFilename();
        String lower = name != null ? name.toLowerCase() : "";
        try (InputStream in = file.getInputStream()) {
            if (lower.endsWith(".xlsx") || lower.endsWith(".xls")) {
                return parseExcel(in);
            }
            return parseCsv(in);
        }
    }

    private List<Map<String, String>> parseCsv(InputStream inputStream) throws IOException {
        List<Map<String, String>> rows = new ArrayList<>();
        InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        CSVParser parser = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setTrim(true)
                .setIgnoreEmptyLines(true)
                .build()
                .parse(reader);
        for (CSVRecord record : parser) {
            Map<String, String> raw = new LinkedHashMap<>();
            for (String header : parser.getHeaderNames()) {
                String value = record.isSet(header) ? record.get(header) : null;
                raw.put(header, (value == null || value.isBlank()) ? null : value);
            }
            rows.add(raw);
        }
        return rows;
    }

    private List<Map<String, String>> parseExcel(InputStream inputStream) throws IOException {
        List<Map<String, String>> rows = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();
            Iterator<Row> rowIterator = sheet.rowIterator();
            if (!rowIterator.hasNext()) {
                return rows;
            }

            Row headerRow = rowIterator.next();
            int lastCol = headerRow.getLastCellNum();
            List<String> headers = new ArrayList<>();
            for (int i = 0; i < lastCol; i++) {
                Cell cell = headerRow.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                headers.add(cell != null ? formatter.formatCellValue(cell).trim() : "");
            }

            while (rowIterator.hasNext()) {
                Row excelRow = rowIterator.next();
                Map<String, String> raw = new LinkedHashMap<>();
                boolean anyValue = false;
                for (int i = 0; i < headers.size(); i++) {
                    String header = headers.get(i);
                    if (header.isBlank()) continue;
                    Cell cell = excelRow.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    String value = cellToText(cell, header, formatter);
                    if (value != null) anyValue = true;
                    raw.put(header, value);
                }
                if (anyValue) {
                    rows.add(raw);
                }
            }
        }
        return rows;
    }

    /** Payment Date / Payout Date are real Excel date cells — extracted as an actual LocalDate
     *  (ISO-formatted) rather than trusting the cell's display format, which varies. Every other
     *  column is rendered as its displayed text via DataFormatter (mirrors what a human sees, and
     *  handles currency-formatted amount cells like "$1,200.00" — parseAmount strips the symbols). */
    private String cellToText(Cell cell, String header, DataFormatter formatter) {
        if (cell == null) return null;
        String value;
        if (DATE_COLUMNS.contains(header) && cell.getCellType() == CellType.NUMERIC
                && DateUtil.isCellDateFormatted(cell)) {
            value = cell.getLocalDateTimeCellValue().toLocalDate().toString();
        } else {
            value = formatter.formatCellValue(cell).trim();
        }
        return value.isBlank() ? null : value;
    }

    private void populateRawFields(ZeffyImportRow row, Map<String, String> raw) {
        row.setPaymentDate(parseDate(raw.get(COL_PAYMENT_DATE)));
        row.setPaymentTime(raw.get(COL_PAYMENT_TIME));
        row.setAmount(parseAmount(raw.get(COL_AMOUNT)));
        row.setPaymentStatus(raw.get(COL_PAYMENT_STATUS));
        row.setPayoutDate(parseDate(raw.get(COL_PAYOUT_DATE)));
        row.setFirstName(raw.get(COL_FIRST_NAME));
        row.setLastName(raw.get(COL_LAST_NAME));
        row.setEmail(raw.get(COL_EMAIL));
        row.setAddress(raw.get(COL_ADDRESS));
        row.setCity(raw.get(COL_CITY));
        row.setPostalCode(raw.get(COL_POSTAL_CODE));
        row.setState(raw.get(COL_STATE));
        row.setCountry(raw.get(COL_COUNTRY));
        row.setTaxReceiptNumber(raw.get(COL_TAX_RECEIPT_NUMBER));
        row.setTaxReceiptUrl(raw.get(COL_TAX_RECEIPT_URL));
        row.setCampaignTitle(raw.get(COL_CAMPAIGN_TITLE));
    }

    /**
     * Sets dedupeKey/isNewPerson/isNewMember and the row's outcome. Order matters: a row that's
     * both a duplicate and unmapped is reported as a duplicate (it won't be applied either way,
     * and duplicate is the more informative reason).
     */
    private void computeOutcome(ZeffyImportRow row, UUID orgId, Set<String> seenDedupeKeysInBatch) {
        if (row.getEmail() == null || row.getEmail().isBlank()) {
            row.setOutcome("error");
            row.setOutcomeDetail("Missing email");
            return;
        }

        Optional<Person> existingPerson = personService.findByEmailIfExists(row.getEmail());
        row.setIsNewPerson(existingPerson.isEmpty());
        row.setIsNewMember(existingPerson.isEmpty()
                || !membershipService.hasMembership(existingPerson.get().getId(), orgId));

        String dedupeKey = (row.getTaxReceiptNumber() != null && !row.getTaxReceiptNumber().isBlank())
                ? row.getTaxReceiptNumber()
                : row.getEmail() + "|" + row.getPaymentDate() + "|" + row.getPaymentTime() + "|" + row.getAmount();
        row.setDedupeKey(dedupeKey);

        if (seenDedupeKeysInBatch.contains(dedupeKey)
                || rowRepo.existsByOrgIdAndDedupeKeyAndOutcome(orgId, dedupeKey, "committed")) {
            row.setOutcome("duplicate");
            return;
        }
        seenDedupeKeysInBatch.add(dedupeKey);

        if (!"succeeded".equalsIgnoreCase(row.getPaymentStatus())) {
            row.setOutcome("skipped_status");
            row.setOutcomeDetail(row.getPaymentStatus());
            return;
        }

        if (row.getCampaignTitle() != null && !row.getCampaignTitle().isBlank()) {
            Optional<ZeffyCampaignMapping> mapping =
                    mappingRepo.findByOrgIdAndCampaignTitleIgnoreCase(orgId, row.getCampaignTitle());
            if (mapping.isEmpty()) {
                row.setOutcome("unmapped_campaign");
                row.setOutcomeDetail(row.getCampaignTitle());
                return;
            }
            row.setFund(mapping.get().getFund());
        }

        row.setOutcome("ready");
    }

    public ZeffyImportSummary getSummary(UUID batchId) {
        ZeffyImportBatch batch = findBatchById(batchId);
        List<ZeffyImportRow> rows = rowRepo.findByBatchIdOrderByRowNumber(batchId);

        Map<String, Long> countsByOutcome = rows.stream()
                .collect(Collectors.groupingBy(ZeffyImportRow::getOutcome, Collectors.counting()));

        List<String> unmappedCampaignTitles = rows.stream()
                .filter(r -> "unmapped_campaign".equals(r.getOutcome()))
                .map(ZeffyImportRow::getCampaignTitle)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();

        BigDecimal totalAmountReady = rows.stream()
                .filter(r -> "ready".equals(r.getOutcome()) && r.getAmount() != null)
                .map(ZeffyImportRow::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long newPersonCount = rows.stream().filter(ZeffyImportRow::getIsNewPerson).count();
        long newMemberCount = rows.stream().filter(ZeffyImportRow::getIsNewMember).count();

        return new ZeffyImportSummary(
                batch.getId(),
                rows.size(),
                countsByOutcome.getOrDefault("ready", 0L),
                countsByOutcome.getOrDefault("duplicate", 0L),
                countsByOutcome.getOrDefault("skipped_status", 0L),
                countsByOutcome.getOrDefault("unmapped_campaign", 0L),
                countsByOutcome.getOrDefault("error", 0L),
                countsByOutcome.getOrDefault("committed", 0L),
                newPersonCount,
                newMemberCount,
                totalAmountReady,
                unmappedCampaignTitles);
    }

    // ── Campaign mappings ────────────────────────────────────────────────────

    public List<ZeffyCampaignMapping> findMappingsByOrg(UUID orgId) {
        return mappingRepo.findByOrgId(orgId);
    }

    @Transactional
    public List<ZeffyCampaignMapping> upsertCampaignMappings(UUID orgId, List<CampaignMappingRequest> requests) {
        Organization org = orgService.findById(orgId);
        List<ZeffyCampaignMapping> saved = new ArrayList<>();
        for (CampaignMappingRequest req : requests) {
            Fund fund = financeService.findFundById(req.fundId());
            ZeffyCampaignMapping mapping = mappingRepo
                    .findByOrgIdAndCampaignTitleIgnoreCase(orgId, req.campaignTitle())
                    .orElseGet(() -> ZeffyCampaignMapping.builder()
                            .org(org)
                            .campaignTitle(req.campaignTitle())
                            .build());
            mapping.setFund(fund);
            saved.add(mappingRepo.save(mapping));
        }
        return saved;
    }

    @Transactional
    public void deleteMapping(UUID id) {
        if (!mappingRepo.existsById(id)) throw new ResourceNotFoundException("ZeffyCampaignMapping", id);
        mappingRepo.deleteById(id);
    }

    // ── Commit ───────────────────────────────────────────────────────────────

    /**
     * Deliberately NOT a single wrapping transaction (overrides the class-level readOnly default
     * with NOT_SUPPORTED, rather than plain @Transactional): rowApplier.applyRow is a different
     * bean, so it only gets its own fresh transaction per row if this method has no ambient
     * transaction of its own for it to join. Confirmed the hard way — an earlier version of this
     * method was @Transactional, which meant applyRow's default REQUIRED propagation joined it,
     * so one row's exception marked the *whole batch's* transaction rollback-only, throwing
     * UnexpectedRollbackException instead of isolating the failure to that row.
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public ZeffyImportCommitResult commitImport(UUID orgId, UUID batchId) {
        ZeffyImportBatch batch = findBatchById(batchId);
        if (!batch.getOrg().getId().equals(orgId)) {
            throw new IllegalArgumentException("Batch does not belong to this organization");
        }
        if ("committed".equals(batch.getStatus())) {
            throw new IllegalArgumentException("This import batch has already been committed");
        }

        membershipService.ensureZeffyTierTypesSeeded(orgId);
        financeService.findAccountsByOrg(orgId, PageRequest.of(0, 1)); // triggers lazy chart-of-accounts seed
        Account categoryAccount = financeService.findAccountByNumber(orgId, "4010");
        Account depositAccount = financeService.findAccountByNumber(orgId, "1010");

        List<ZeffyImportRow> rows = rowRepo.findByBatchIdOrderByRowNumber(batchId);
        int committed = 0;
        int failed = 0;
        int stillUnmapped = 0;
        for (ZeffyImportRow row : rows) {
            if (!"ready".equals(row.getOutcome()) && !"unmapped_campaign".equals(row.getOutcome())) continue;
            try {
                rowApplier.applyRow(row.getId(), depositAccount.getId(), categoryAccount.getId());
                String outcomeAfter = rowRepo.findById(row.getId())
                        .map(ZeffyImportRow::getOutcome)
                        .orElse("error");
                if ("committed".equals(outcomeAfter)) {
                    committed++;
                } else if ("unmapped_campaign".equals(outcomeAfter)) {
                    stillUnmapped++;
                }
            } catch (Exception ex) {
                rowApplier.markRowError(row.getId(), ex.getMessage());
                failed++;
            }
        }

        batch.setStatus("committed");
        batch.setCommittedAt(OffsetDateTime.now());
        batchRepo.save(batch);

        return new ZeffyImportCommitResult(batchId, committed, failed, stillUnmapped);
    }

    // ── Parsing helpers ──────────────────────────────────────────────────────

    /** A blank Total Amount is treated the same as an explicit 0 — both import as Follower with
     *  no Finance posting (see ZeffyImportRowApplier), rather than failing the row at commit time. */
    private BigDecimal parseAmount(String raw) {
        if (raw == null) return BigDecimal.ZERO;
        String cleaned = raw.replaceAll("[^0-9.\\-]", "");
        if (cleaned.isBlank()) return BigDecimal.ZERO;
        try {
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Total Amount must be a number, got: " + raw);
        }
    }

    private LocalDate parseDate(String raw) {
        if (raw == null || raw.isBlank()) return null;
        for (DateTimeFormatter fmt : DATE_FORMATS) {
            try {
                return LocalDate.parse(raw, fmt);
            } catch (DateTimeParseException ignored) {
                // try next format
            }
        }
        throw new IllegalArgumentException("Unrecognized date format: " + raw);
    }

    private String truncate(String detail) {
        if (detail == null) return null;
        return detail.length() > OUTCOME_DETAIL_MAX_LENGTH ? detail.substring(0, OUTCOME_DETAIL_MAX_LENGTH) : detail;
    }
}
