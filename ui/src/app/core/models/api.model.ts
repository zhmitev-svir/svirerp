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
