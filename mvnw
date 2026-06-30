#!/bin/sh
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements. Licensed under the Apache License 2.0.
# http://www.apache.org/licenses/LICENSE-2.0
#
# Apache Maven Wrapper — 3.3.1
# Downloads Maven automatically on first run; no system Maven install required.
# -----------------------------------------------------------------------------

set -e

# Follow symlinks to find the real script location
PRG="$0"
while [ -h "$PRG" ]; do
  ls=$(ls -ld "$PRG")
  link=$(expr "$ls" : '.*-> \(.*\)$')
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG="$(dirname "$PRG")/$link"
  fi
done

APP_HOME="$(cd "$(dirname "$PRG")" && pwd)"

# ---------------------------------------------------------------------------
# On Windows (Git Bash = MINGW, MSYS2 = MSYS, Cygwin) the Unix-style path
# that `pwd` returns (e.g. /h/workspaces/svirerp) is not understood by the
# Windows JVM.  Convert it to a Windows-native form (H:/workspaces/svirerp)
# which Java accepts on all platforms via forward slashes.
# ---------------------------------------------------------------------------
case "$(uname)" in
  CYGWIN*)
    APP_HOME="$(cygpath --path --mixed "$APP_HOME")"
    ;;
  MINGW* | MSYS*)
    # pwd -W returns the Windows-style path with forward slashes
    APP_HOME_W="$(cd "$APP_HOME" && pwd -W 2>/dev/null)" && APP_HOME="$APP_HOME_W" || true
    ;;
esac

WRAPPER_JAR="$APP_HOME/.mvn/wrapper/maven-wrapper.jar"
WRAPPER_PROPS="$APP_HOME/.mvn/wrapper/maven-wrapper.properties"

# ---------------------------------------------------------------------------
# Locate Java
# ---------------------------------------------------------------------------
if [ -n "$JAVA_HOME" ]; then
  JAVA_CMD="$JAVA_HOME/bin/java"
  if [ ! -f "$JAVA_CMD" ]; then
    echo "Error: JAVA_HOME points to an invalid directory: $JAVA_HOME" >&2
    echo "       Update JAVA_HOME to point at a valid JDK installation." >&2
    exit 1
  fi
else
  JAVA_CMD="java"
  if ! command -v java > /dev/null 2>&1; then
    echo "Error: 'java' command not found in PATH and JAVA_HOME is not set." >&2
    echo "       Install a JDK and either set JAVA_HOME or add java to PATH." >&2
    exit 1
  fi
fi

# ---------------------------------------------------------------------------
# Download the wrapper JAR on first run
# ---------------------------------------------------------------------------
if [ ! -f "$WRAPPER_JAR" ]; then
  WRAPPER_URL=""
  if [ -f "$WRAPPER_PROPS" ]; then
    WRAPPER_URL=$(grep -m1 '^wrapperUrl=' "$WRAPPER_PROPS" | cut -d= -f2- | tr -d '\r')
  fi
  if [ -z "$WRAPPER_URL" ]; then
    echo "Error: wrapperUrl not found in $WRAPPER_PROPS" >&2
    exit 1
  fi

  echo "Downloading Maven Wrapper JAR..."
  mkdir -p "$(dirname "$WRAPPER_JAR")"

  if command -v curl > /dev/null 2>&1; then
    curl -fsSL -o "$WRAPPER_JAR" "$WRAPPER_URL" || {
      echo "Error: curl failed to download the Maven Wrapper JAR." >&2; exit 1
    }
  elif command -v wget > /dev/null 2>&1; then
    wget -q -O "$WRAPPER_JAR" "$WRAPPER_URL" || {
      echo "Error: wget failed to download the Maven Wrapper JAR." >&2; exit 1
    }
  else
    echo "Error: Neither curl nor wget found. Install one to enable auto-download." >&2
    exit 1
  fi

  echo "Maven Wrapper JAR downloaded successfully."
fi

# ---------------------------------------------------------------------------
# Run Maven via the wrapper
# ---------------------------------------------------------------------------
exec "$JAVA_CMD" \
  -classpath "$WRAPPER_JAR" \
  "-Dmaven.multiModuleProjectDirectory=$APP_HOME" \
  org.apache.maven.wrapper.MavenWrapperMain "$@"
