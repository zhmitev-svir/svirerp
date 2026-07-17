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
  orgId: string;
  title: string;
  description?: string;
  startDate: string;
  endDate?: string;
  location?: string;
  eventType?: string;
  isPublic?: boolean;
  maxAttendees?: number;
  createdById?: string;
  createdAt?: string;
}

export interface ChurchEvent {
  id: string;
  calendarEventId: string;
  serviceType?: string;
  presider?: string;
}

export interface EventRegistration {
  id: string;
  eventId: string;
  personId: string;
  registeredAt?: string;
  status?: string;
  notes?: string;
}

export interface EventResource {
  id: string;
  eventId: string;
  resourceName: string;
  quantity?: number;
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
  orgId: string;
  name: string;
  description?: string;
  isRestricted?: boolean;
  isActive?: boolean;
}

export interface Account {
  id: string;
  orgId: string;
  code: string;
  name: string;
  accountType: 'asset' | 'liability' | 'equity' | 'revenue' | 'expense';
  parentAccountId?: string;
  isSystem?: boolean;
  isActive?: boolean;
  description?: string;
}

export interface JournalEntry {
  id: string;
  orgId: string;
  reference: string;
  entryDate: string;
  description?: string;
  status: 'draft' | 'posted' | 'void';
  createdById?: string;
  approvedById?: string;
  createdAt?: string;
}

export interface JournalLine {
  id: string;
  journalEntryId: string;
  accountId: string;
  fundId?: string;
  debit: number;
  credit: number;
  memo?: string;
}

export interface BankAccount {
  id: string;
  orgId: string;
  accountId?: string;
  institutionName: string;
  accountName: string;
  accountNumber: string;
  routingNumber?: string;
  accountType: 'checking' | 'savings' | 'money_market' | 'other';
  isActive?: boolean;
}

export interface BankTransaction {
  id: string;
  bankAccountId: string;
  transactionDate: string;
  amount: number;
  transactionType: 'debit' | 'credit' | 'check' | 'transfer' | 'fee' | 'interest' | 'other';
  description?: string;
  status: 'pending' | 'cleared' | 'void';
  isReconciled?: boolean;
}

export interface BankReconciliation {
  id: string;
  bankAccountId: string;
  statementDate: string;
  statementBalance: number;
  bookBalance: number;
  difference?: number;
  status: 'in_progress' | 'completed' | 'discrepancy';
  reconciledAt?: string;
}

export interface ReconciliationItem {
  id: string;
  reconciliationId: string;
  bankTransactionId: string;
  isCleared: boolean;
  itemType: 'transaction' | 'deposit_in_transit' | 'outstanding_check' | 'adjustment';
  notes?: string;
}
