#!/bin/sh -ex

: "${1?"Usage: $0 <[pre]major|[pre]minor|[pre]patch|prerelease>"}"

./mvnw scm:check-local-modification

current=$({ echo 0.0.0; git tag --list --sort=version:refname; } | tail -n1)
release=$(semver "${current}" -i "$1" --preid RC)
next=$(semver "${release}" -i minor)

git checkout -b "release/${release}"

./mvnw versions:set -D newVersion="${release}"
git commit -am "Release ${release}"
./mvnw clean deploy scm:tag -P release -D tag="${release}" -D pushChanges=false -D skipTests -D dependency-check.skip

./mvnw versions:set -D newVersion="${next}-SNAPSHOT"
git commit -am "Development ${next}-SNAPSHOT"

git push
git push --tags

git checkout main
git branch -D "release/${release}"
