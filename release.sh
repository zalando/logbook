#!/usr/bin/env bash

set -euxo pipefail

: "${1?"Usage: $0 <[pre]major|[pre]minor|[pre]patch|prerelease>"}"
: "${CHANGELOG_GITHUB_TOKEN?"Needs CHANGELOG_GITHUB_TOKEN env var"}"

./mvnw scm:check-local-modification

current=$({ echo 0.0.0; git tag --list --sort=version:refname; } | tail -n1)
release=$(semver "${current}" -i "$1" --preid RC)
next=$(semver "${release}" -i minor)

./mvnw versions:set -D newVersion="${release}"
git commit -am "Release ${release}"
./mvnw clean deploy scm:tag -P release -D tag="${release}" -D pushChanges=false -D skipTests -D dependency-check.skip

./mvnw versions:set -D newVersion="${next}-SNAPSHOT"

git push --atomic origin main "${release}"

docker run -it --rm -e CHANGELOG_GITHUB_TOKEN -v "$(pwd)":/usr/local/src/your-app \
    githubchangeloggenerator/github-changelog-generator -u zalando -p logbook
git commit -am "Development ${next}-SNAPSHOT"

git push
