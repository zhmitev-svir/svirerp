// ─── Person ────────────────────────────────────────────────────────────────
export interface Person {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  addressLine1?: string;
  city?: string;
  state?: string;
  zip?: string;
  dateOfBirth?: string;
  createdAt?: string;
}

// ─── Organization ───────────────────────────────────────────────────────────
export interface Organization {
  id: string;
  name: string;
  legalName?: string;
  taxIdEin?: string;
  nonprofitType?: string;
  missionStatement?: string;
  addressLine1?: string;
  addressLine2?: string;
  city?: string;
  state?: string;
  zip?: string;
  country?: string;
  phone?: string;
  email?: string;
  website?: string;
  foundedDate?: string;
  fiscalYearStart?: string;
  logoUrl?: string;
  createdAt?: string;
  updatedAt?: string;
}

// ─── Membership ─────────────────────────────────────────────────────────────
// Entities have no DTO layer and serialize directly, so relations come back
// as full nested objects (not flat *Id strings) — e.g. `org: {...}`, not `orgId`.
export interface MembershipType {
  id: string;
  org: Organization;
  name: string;
  description?: string;
  annualFee: number;
  durationMonths: number;
  isActive: boolean;
  canVote: boolean;
  benefits?: string;
  maxMembers?: number;
  createdAt?: string;
}

export interface Member {
  id: string;
  person: Person;
  org: Organization;
  membershipType: MembershipType;
  memberNumber?: string;
  joinDate: string;
  expiryDate?: string;
  status: 'active' | 'inactive' | 'suspended' | 'expired' | 'pending';
  emailOptIn: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface MemberPayment {
  id: string;
  member: Member;
  amount: number;
  paymentDate: string;
  paymentMethod?: 'cash' | 'check' | 'credit_card' | 'ach' | 'online' | 'other';
  transactionRef?: string;
  periodStart?: string;
  periodEnd?: string;
  status: 'pending' | 'completed' | 'failed' | 'refunded';
  notes?: string;
}

// ─── Governance ─────────────────────────────────────────────────────────────
export interface Trustee {
  id: string;
  person: Person;
  org: Organization;
  title?: string;
  role: string;
  termStart: string;
  termEnd?: string;
  isActive: boolean;
  isOfficer: boolean;
  notes?: string;
  createdAt?: string;
}

export interface TrusteeDocument {
  id: string;
  trustee: Trustee;
  documentType: string;
  fileUrl: string;
  uploadedAt?: string;
  notes?: string;
}

export interface Committee {
  id: string;
  orgId: string;
  name: string;
  description?: string;
  isActive?: boolean;
}

export interface CommitteeMeeting {
  id: string;
  committeeId: string;
  title: string;
  meetingDate: string;
  location?: string;
  meetingType: 'regular' | 'special' | 'emergency' | 'annual';
  status: 'scheduled' | 'completed' | 'cancelled' | 'postponed';
}

export interface CommitteeResolution {
  id: string;
  committeeId: string;
  meetingId?: string;
  title: string;
  description?: string;
  status: 'passed' | 'failed' | 'tabled' | 'withdrawn';
  votedAt?: string;
}

// General board/trustee meeting record — org-level, not tied to a committee.
export interface MeetingMinutes {
  id: string;
  org: Organization;
  meetingDate: string;
  title: string;
  summary?: string;
  createdAt?: string;
}

export interface ActionItem {
  id: string;
  meetingMinutes: MeetingMinutes;
  assigneeTrustee?: Trustee;
  note: string;
  priority: 'high' | 'normal' | 'low';
  dueDate?: string;
  status: 'new' | 'planned' | 'done';
  notes?: string;
  createdAt?: string;
  updatedAt?: string;
}

// ─── Events ─────────────────────────────────────────────────────────────────
export interface CalendarEvent {
  id: string;
  org: Organization;
  createdBy?: Person;
  title: string;
  description?: string;
  eventType?: string;
  startDatetime: string;
  endDatetime?: string;
  isAllDay?: boolean;
  location?: string;
  virtualLink?: string;
  isRecurring?: boolean;
  recurrenceRule?: string;
  status: 'scheduled' | 'cancelled' | 'completed' | 'postponed';
  visibility: 'public' | 'members_only' | 'internal';
  capacity?: number;
  publishToOfficial?: boolean;
  googleOfficialEventId?: string;
  googleOfficialSyncError?: string;
  publishToInternal?: boolean;
  googleInternalEventId?: string;
  googleInternalSyncError?: string;
  createdAt?: string;
}

export interface ChurchEvent {
  id: string;
  calendarEvent: CalendarEvent;
  serviceType?: string;
  liturgicalSeason?: string;
  officiant?: string;
  sermonTitle?: string;
  scriptureReadings?: string;
  musicSelections?: string;
  specialInstructions?: string;
  offeringCollected?: number;
  attendanceCount?: number;
}

export interface EventRegistration {
  id: string;
  event: CalendarEvent;
  person: Person;
  registeredAt?: string;
  status: 'registered' | 'attended' | 'cancelled' | 'waitlisted' | 'no_show';
  feePaid?: number;
  ticketNumber?: string;
  notes?: string;
}

export interface EventResource {
  id: string;
  event: CalendarEvent;
  resourceType: string;
  resourceName: string;
  assignedTo?: string;
  notes?: string;
}

// ─── Volunteers ──────────────────────────────────────────────────────────────
export interface VolunteerArea {
  id: string;
  org: Organization;
  name: string;
  description?: string;
  isActive?: boolean;
}

export interface Volunteer {
  id: string;
  person: Person;
  org: Organization;
  contactPerson?: Person;
  onboardDate: string;
  isActive?: boolean;
  skills?: string;
  availability?: string;
  notes?: string;
  areas?: VolunteerArea[];
}

export interface VolunteerHour {
  id: string;
  volunteerId: string;
  date: string;
  hours: number;
  description?: string;
  status: 'pending' | 'approved' | 'rejected';
  eventId?: string;
}

// ─── Finance ─────────────────────────────────────────────────────────────────
export interface Fund {
  id: string;
  org: Organization;
  fundName: string;
  fundCode: string;
  fundType: 'unrestricted' | 'temporarily_restricted' | 'permanently_restricted';
  description?: string;
  isRestricted?: boolean;
  restrictionPurpose?: string;
  isActive?: boolean;
  openingBalance: number;
}

/** Powers the "project financial status" view — GET /api/funds/{id}/summary. */
export interface FundSummary {
  openingBalance: number;
  totalIncome: number;
  totalExpense: number;
  balance: number;
}

export interface Account {
  id: string;
  org: Organization;
  parentAccount?: Account;
  accountNumber: string;
  accountName: string;
  accountType: 'asset' | 'liability' | 'equity' | 'revenue' | 'expense';
  accountSubtype?: string;
  normalBalance: 'debit' | 'credit';
  isActive?: boolean;
  isSystem?: boolean;
  description?: string;
  createdAt?: string;
}

export interface Vendor {
  id: string;
  org: Organization;
  name: string;
  category?: string;
  contactName?: string;
  phone?: string;
  email?: string;
  addressLine1?: string;
  city?: string;
  state?: string;
  zip?: string;
  notes?: string;
  isActive?: boolean;
  createdAt?: string;
}

export interface ServiceRequest {
  id: string;
  org: Organization;
  requestorPerson?: Person;
  serviceType: 'wedding' | 'baptism' | 'funeral' | 'memorial' | 'blessing' | 'other';
  requestedDate?: string;
  agreedAmount: number;
  status: 'requested' | 'scheduled' | 'completed' | 'cancelled';
  churchEvent?: ChurchEvent;
  notes?: string;
  createdAt?: string;
}

export interface JournalEntry {
  id: string;
  org: Organization;
  createdBy?: Person;
  approvedBy?: Person;
  entryNumber?: string;
  entryDate: string;
  description?: string;
  reference?: string;
  entryType: 'general' | 'adjusting' | 'closing' | 'reversing' | 'opening';
  status: 'draft' | 'posted' | 'void';
  totalDebit: number;
  totalCredit: number;
  // Transaction tags set by the Record Income / Record Expense flow — see RecordIncomeRequest /
  // RecordExpenseRequest. Denormalized for the transaction list; the ledger truth is the lines below.
  paymentMethod?: 'cash' | 'check' | 'zeffy' | 'bank_transfer' | 'card' | 'other';
  checkNumber?: string;
  payer?: Person;
  vendor?: Vendor;
  serviceRequest?: ServiceRequest;
  categoryAccount?: Account;
  fund?: Fund;
  createdAt?: string;
  approvedAt?: string;
}

export interface RecordIncomeRequest {
  entryDate: string;
  amount: number;
  description?: string;
  categoryAccountId: string;
  depositAccountId: string;
  fundId?: string;
  payerId?: string;
  serviceRequestId?: string;
  paymentMethod: 'cash' | 'check' | 'zeffy' | 'bank_transfer' | 'card' | 'other';
  checkNumber?: string;
}

export interface RecordExpenseRequest {
  entryDate: string;
  amount: number;
  description?: string;
  categoryAccountId: string;
  paymentAccountId: string;
  fundId?: string;
  vendorId?: string;
  paymentMethod: 'cash' | 'check' | 'zeffy' | 'bank_transfer' | 'card' | 'other';
  checkNumber?: string;
}

export interface JournalLine {
  id: string;
  journalEntry: JournalEntry;
  account: Account;
  fund?: Fund;
  debitAmount: number;
  creditAmount: number;
  memo?: string;
}

export interface BankAccount {
  id: string;
  org: Organization;
  glAccount: Account;
  institutionName: string;
  accountName: string;
  accountNumberMasked?: string;
  routingNumberMasked?: string;
  accountType: 'checking' | 'savings' | 'money_market' | 'cd' | 'investment';
  currency?: string;
  currentBalance: number;
  openedDate?: string;
  isActive?: boolean;
  isPrimary?: boolean;
  contactName?: string;
  contactPhone?: string;
  notes?: string;
  updatedAt?: string;
}

export interface BankTransaction {
  id: string;
  bankAccount: BankAccount;
  journalEntry?: JournalEntry;
  transactionRef?: string;
  transactionDate: string;
  postedDate?: string;
  amount: number;
  transactionType: 'debit' | 'credit' | 'check' | 'transfer' | 'fee' | 'interest' | 'other';
  description?: string;
  payee?: string;
  checkNumber?: string;
  status: 'pending' | 'cleared' | 'void';
  isReconciled?: boolean;
  importedAt?: string;
}

export interface BankReconciliation {
  id: string;
  bankAccount: BankAccount;
  reconciledBy?: Person;
  statementDate: string;
  statementBalance: number;
  bookBalance: number;
  difference?: number;
  status: 'in_progress' | 'completed' | 'discrepancy';
  reconciledAt?: string;
  notes?: string;
}

export interface ReconciliationItem {
  id: string;
  reconciliation: BankReconciliation;
  bankTransaction: BankTransaction;
  isCleared: boolean;
  itemType: 'transaction' | 'deposit_in_transit' | 'outstanding_check' | 'adjustment';
  notes?: string;
}
