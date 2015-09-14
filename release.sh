#!/usr/bin/env bash

set -e

mvn scm:check-local-modification

# release
mvn versions:set
git add pom.xml
git commit
mvn clean deploy -P release
mvn scm:tag

# next development version
mvn versions:set
git add pom.xml
git commit