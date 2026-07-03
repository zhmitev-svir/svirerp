/** Response shape of GET /api/auth/me */
export interface AuthenticatedUser {
  email: string | null;
  name: string | null;
  provider: 'google' | 'local-admin';
}
