/** Spring Data Page response envelope */
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number; // 0-based current page index
}

/** Parameters for paginated GET requests */
export interface PageParams {
  page: number;
  size: number;
  sort?: string;
}

export const DEFAULT_PAGE_PARAMS: PageParams = { page: 0, size: 20 };

/** Standard error envelope returned by the Spring GlobalExceptionHandler */
export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  fields?: Record<string, string>; // populated on validation failures
}

/** Response shape of POST /api/organizations/{orgId}/members/import — a synthesized
 * batch-operation summary, not a persisted entity, so it lives here rather than
 * in domain.model.ts. */
export interface MemberImportResult {
  created: number;
  updated: number;
  failed: MemberImportRowError[];
}

export interface MemberImportRowError {
  rowNumber: number;
  email: string | null;
  message: string;
}

/** Response shape of GET/PUT /api/settings — admin-only runtime config. `value`
 * is always null for SECRET settings; `hasValue` tells the UI whether one is
 * configured without ever exposing it. */
export interface AppSetting {
  key: string;
  value: string | null;
  valueType: 'STRING' | 'SECRET' | 'BOOLEAN' | 'NUMBER';
  description?: string;
  hasValue: boolean;
}

/** Response shape of GET /api/zeffy-imports/{batchId}/summary — a synthesized preview
 * projection, not a persisted entity. */
export interface ZeffyImportSummary {
  batchId: string;
  totalRows: number;
  readyCount: number;
  duplicateCount: number;
  skippedStatusCount: number;
  unmappedCampaignCount: number;
  errorCount: number;
  committedCount: number;
  newPersonCount: number;
  newMemberCount: number;
  totalAmountReady: number;
  unmappedCampaignTitles: string[];
}

/** Response shape of POST /api/organizations/{orgId}/zeffy-imports/{batchId}/commit. */
export interface ZeffyImportCommitResult {
  batchId: string;
  committed: number;
  failed: number;
  stillUnmappedCampaign: number;
}

/** Request body for POST /api/organizations/{orgId}/zeffy-campaign-mappings/bulk. */
export interface ZeffyCampaignMappingRequest {
  campaignTitle: string;
  fundId: string;
}

/** Response shape of POST /api/organizations/{orgId}/members/recompute-tiers. */
export interface RecomputeTiersResult {
  membersProcessed: number;
}
