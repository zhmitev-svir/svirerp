export const environment = {
  production: false,
  // Relative so requests go through the ng-serve dev proxy (proxy.conf.json)
  // to the backend on :8080 — this keeps the browser's view of the app
  // same-origin in dev, matching production, so session cookies behave
  // identically in both.
  apiUrl: '/api',
};
