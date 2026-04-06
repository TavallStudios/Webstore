#!/bin/sh
set -eu

BASE_DIR="$(cd "$(dirname "$0")" && pwd)"
WRAPPER_DIR="$BASE_DIR/.mvn/wrapper"
MAVEN_VERSION="3.9.11"
ARCHIVE_NAME="apache-maven-${MAVEN_VERSION}-bin.zip"
ARCHIVE_PATH="$WRAPPER_DIR/$ARCHIVE_NAME"
MAVEN_HOME="$WRAPPER_DIR/apache-maven-${MAVEN_VERSION}"

if [ ! -x "$MAVEN_HOME/bin/mvn" ]; then
  mkdir -p "$WRAPPER_DIR"
  if [ ! -f "$ARCHIVE_PATH" ]; then
    if command -v curl >/dev/null 2>&1; then
      curl -fsSL "https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/${ARCHIVE_NAME}" -o "$ARCHIVE_PATH"
    else
      wget -q "https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/${ARCHIVE_NAME}" -O "$ARCHIVE_PATH"
    fi
  fi
  unzip -oq "$ARCHIVE_PATH" -d "$WRAPPER_DIR"
fi

exec "$MAVEN_HOME/bin/mvn" "$@"
