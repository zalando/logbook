#!/bin/sh -ex

: ${1?"Usage: $0 <[pre]major|[pre]minor|[pre]patch|prerelease>"}

./mvnw scm:check-local-modification

current=$(git describe --abbrev=0 || echo 0.0.0)
release=$(semver ${current} -i $1 --preid RC)
next=$(semver ${release} -i minor)

./mvnw versions:set -D newVersion=${release}
git commit -am "Release ${release}"
./mvnw clean deploy scm:tag -P release -D tag=${release} -D pushChanges=false

./mvnw versions:set -D newVersion=${next}-SNAPSHOT
git commit -am "Development ${next}-SNAPSHOT"

git push
git push --tags
