#!/bin/sh -e

: ${2?"Usage: $0 <release-version> <next-version>"}

mvn scm:check-local-modification

# release
mvn versions:set -D newVersion=$1
git add $(find . -name pom.xml)
git commit -m "Release $1"
mvn clean deploy -P release
mvn scm:tag

# next development version
mvn versions:set -D newVersion=$2-SNAPSHOT
git add $(find . -name pom.xml)
git commit -m "Development $2-SNAPSHOT"
