/**
 * Base path for every API call.
 *
 * The same value works in both environments: the Angular dev server proxies `/api` to
 * `localhost:8080` (see `proxy.conf.json`) and nginx proxies it to the API container in Docker. The
 * browser therefore only ever talks to its own origin.
 */
export const API_BASE_URL = '/api';
