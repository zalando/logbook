#!/bin/sh -ex

: ${1?"Usage: $0 <major|minor|patch>"}

./mvnw scm:check-local-modification

current=$(git describe --abbrev=0)
release=$(semver -i $1 ${current})
next=$(semver -i minor ${release})

# release
./mvnw versions:set -D newVersion=${release}
git add $(find . -name pom.xml)
git commit -m "Release ${release}"
./mvnw clean deploy -P release
./mvnw scm:tag

# next development version
./mvnw versions:set -D newVersion=${next}-SNAPSHOT
git add $(find . -name pom.xml)
git commit -m "Development ${next}-SNAPSHOT"

git push
git push --tags
