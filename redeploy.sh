#!/usr/bin/env bash
# Stops the running production instance, cleans and rebuilds the Angular UI, cleans and rebuilds
# the backend jar (which bundles the freshly-built UI — see README's "Release build" section), then
# starts the app again with the same flags production has been using.
#
# Run this from a WSL terminal in the repo root — that's where production actually runs (see
# README's Production Access section); it won't find the running process from the Windows side.
#
# One-time setup: cp deploy.env.example deploy.env, then fill in the real encryption key/salt.
# deploy.env is gitignored, same reasoning as application-local.properties — never commit it.
#
# Usage: ./redeploy.sh [--server-only]
#   --server-only   Skip the Angular UI rebuild entirely — the jar bundles whatever's already
#                    sitting in ui/dist (see README: a backend-only build still works, it just
#                    won't have a UI to serve if ui/dist has never been built at all). Use this
#                    for backend-only fixes to redeploy faster, since the UI build is the slow step.

set -euo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")"

SERVER_ONLY=false
for arg in "$@"; do
  case "$arg" in
    --server-only) SERVER_ONLY=true ;;
    *) echo "Unknown argument: $arg (only --server-only is supported)" >&2; exit 1 ;;
  esac
done

JAR="target/svirerp-1.0.0-SNAPSHOT.jar"
LOG="error.log"
PORT=8080
MATCH_PATTERN="svirerp-1.0.0-SNAPSHOT.jar"

if [ ! -f deploy.env ]; then
  echo "Missing deploy.env — run: cp deploy.env.example deploy.env, then fill in the real values." >&2
  exit 1
fi
# shellcheck source=/dev/null
source deploy.env
: "${APP_CORS_ALLOWED_ORIGINS:?deploy.env must set APP_CORS_ALLOWED_ORIGINS}"
: "${APP_SETTINGS_ENCRYPTION_KEY:?deploy.env must set APP_SETTINGS_ENCRYPTION_KEY}"
: "${APP_SETTINGS_ENCRYPTION_SALT:?deploy.env must set APP_SETTINGS_ENCRYPTION_SALT}"

echo "==> Stopping the running instance (if any)..."
PIDS="$(pgrep -f "$MATCH_PATTERN" || true)"
if [ -n "$PIDS" ]; then
  echo "    Found PID(s): $PIDS — sending SIGTERM"
  kill $PIDS
  for _ in $(seq 1 30); do
    pgrep -f "$MATCH_PATTERN" >/dev/null || break
    sleep 1
  done
  if pgrep -f "$MATCH_PATTERN" >/dev/null; then
    echo "    Still running after 30s — sending SIGKILL"
    kill -9 $(pgrep -f "$MATCH_PATTERN") || true
    sleep 1
  fi
else
  echo "    Nothing running."
fi

if [ "$SERVER_ONLY" = true ]; then
  echo "==> --server-only: skipping the Angular UI rebuild, reusing whatever's in ui/dist."
  if [ ! -d ui/dist/svirerp-ui/browser ]; then
    echo "    WARNING: ui/dist/svirerp-ui/browser doesn't exist — the jar will have no UI to serve." >&2
  fi
else
  echo "==> Cleaning and rebuilding the Angular UI..."
  rm -rf ui/dist
  (cd ui && npm ci && npm run build)
fi

echo "==> Cleaning and rebuilding the backend jar..."
./mvnw clean package -DskipTests

echo "==> Starting the app..."
nohup java -jar "$JAR" \
  --spring.profiles.active=local \
  --app.cors.allowed-origins="$APP_CORS_ALLOWED_ORIGINS" \
  --app.settings.encryption-key="$APP_SETTINGS_ENCRYPTION_KEY" \
  --app.settings.encryption-salt="$APP_SETTINGS_ENCRYPTION_SALT" \
  >> "$LOG" 2>&1 &
disown
NEW_PID=$!
echo "    Started with PID $NEW_PID — logging to $LOG"

echo "==> Waiting for it to come up on port $PORT..."
for _ in $(seq 1 160); do
  if ! kill -0 "$NEW_PID" 2>/dev/null; then
    echo "==> Process exited early — check $LOG" >&2
    exit 1
  fi
  if (exec 3<>"/dev/tcp/localhost/$PORT") 2>/dev/null; then
    exec 3>&-
    echo "==> Up and listening on port $PORT (PID $NEW_PID)."
    exit 0
  fi
  sleep 1
done
echo "==> WARNING: port $PORT did not come up within 160s — check $LOG" >&2
exit 1
