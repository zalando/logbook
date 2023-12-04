#!/usr/bin/env bash

set -euxo pipefail

: "${1?"Usage: $0 <[pre]major|[pre]minor|[pre]patch|prerelease>"}"
: "${CHANGELOG_GITHUB_TOKEN?"Needs CHANGELOG_GITHUB_TOKEN env var (access token with repo scopes)"}"

./mvnw scm:check-local-modification

[ "$1" == "prerelease" ] && versionsuffix="" || versionsuffix="-"
current=$({ echo 0.0.0; git -c "versionsort.suffix=${versionsuffix}" tag --list --sort=version:refname; } | tail -n1)
release=$(semver "${current}" -i "$1" --preid RC)
next=$(semver "${release}" -i minor)

./mvnw versions:set -D newVersion="${release}"

docker run -it --rm -e CHANGELOG_GITHUB_TOKEN -v "$(pwd)":/usr/local/src/your-app \
    githubchangeloggenerator/github-changelog-generator \
    -u zalando -p logbook \
    --future-release ${release} \
    --exclude-labels "duplicate,question,invalid,wontfix,stale,not-a-bug"

git commit -am "Release ${release}"

./mvnw clean deploy scm:tag -P release -D tag="${release}" -D pushChanges=false -D skipTests -D dependency-check.skip

./mvnw versions:set -D newVersion="${next}-SNAPSHOT"

git commit -am "Development ${next}-SNAPSHOT"

git push --atomic origin main "${release}"
