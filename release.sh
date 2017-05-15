#!/bin/sh -e

: ${2?"Usage: $0 <release-version> <next-version>"}

./mvnw scm:check-local-modification

# release
./mvnw versions:set -D newVersion=$1
git add $(find . -name pom.xml)
git commit -m "Release $1"
./mvnw clean deploy -P release
./mvnw scm:tag

# next development version
./mvnw versions:set -D newVersion=$2-SNAPSHOT
git add $(find . -name pom.xml)
git commit -m "Development $2-SNAPSHOT"

git push
git push --tags
