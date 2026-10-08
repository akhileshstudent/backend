#!/usr/bin/env sh
set -eu

MAVEN_VERSION=3.9.11
MAVEN_USER_HOME=${MAVEN_USER_HOME:-"$HOME/.m2"}
WRAPPER_ROOT="$MAVEN_USER_HOME/wrapper/dists/hosteldekho-maven-$MAVEN_VERSION"
MAVEN_HOME="$WRAPPER_ROOT/apache-maven-$MAVEN_VERSION"

if [ ! -x "$MAVEN_HOME/bin/mvn" ]; then
  mkdir -p "$WRAPPER_ROOT"
  ARCHIVE="$WRAPPER_ROOT/apache-maven-$MAVEN_VERSION-bin.zip"
  curl -fsSL --retry 3 "https://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.zip" -o "$ARCHIVE"
  unzip -q -o "$ARCHIVE" -d "$WRAPPER_ROOT"
  rm -f "$ARCHIVE"
fi

exec "$MAVEN_HOME/bin/mvn" "$@"
