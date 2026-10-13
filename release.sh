#!/usr/bin/env bash

set -euo pipefail

fail() {
    echo "Error: $*" >&2
    exit 1
}

require_unused_tag() {
    if git show-ref --verify --quiet "refs/tags/$1"; then
        fail "Release tag already exists: $1"
    fi
}

usage="Usage: $0 prepare <major|minor|patch|version>
       $0 publish <version> <release-commit> --confirm-publish <version>
       $0 next [major|minor|patch] (default: minor)"

case "${1:-}" in
    prepare)
        [[ $# -eq 2 ]] || fail "$usage"
        ;;
    publish)
        [[ $# -eq 5 && "$4" == "--confirm-publish" && "$5" == "$2" ]] || fail "$usage"
        [[ "$3" =~ ^[0-9a-fA-F]{7,40}$ ]] || fail "Specify the release commit SHA"
        ;;
    next)
        [[ $# -eq 1 || $# -eq 2 ]] || fail "$usage"
        ;;
    -h|--help)
        echo "$usage"
        exit 0
        ;;
    *)
        fail "$usage"
        ;;
esac

version="${2:-minor}"
version_pattern='^(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)(-RC\.(0|[1-9][0-9]*))?$'
case "$version" in
    major|minor|patch)
        [[ "$1" != publish ]] || fail "Publishing requires an explicit version"
        command -v semver >/dev/null || fail "Install the semver CLI to calculate version increments"
        ;;
    *)
        [[ "$1" != next && "$version" =~ $version_pattern ]] || fail "Invalid version or increment: $version"
        ;;
esac
[[ "$1" == next || "$(git branch --show-current)" == main ]] || fail "Start from main"
[[ -z "$(git status --porcelain --untracked-files=all)" ]] || fail "Working tree must be clean"

case "$(git remote get-url origin)" in
    git@github.com:zalando/logbook.git|https://github.com/zalando/logbook.git|ssh://git@github.com/zalando/logbook.git) ;;
    *) fail "origin must point to zalando/logbook" ;;
esac

git fetch --tags origin '+refs/heads/main:refs/remotes/origin/main'
commit="$(git rev-parse HEAD)"
if [[ "$1" == next ]]; then
    current="$(./mvnw -q help:evaluate -Dexpression=project.version -DforceStdout)"
    [[ "$current" =~ $version_pattern ]] || fail "Start the next cycle from a release version, not $current"
    [[ "$(git branch --show-current)" == "$current-release" ]] || fail "Run next on $current-release after committing the release version and notes"
    require_unused_tag "$current"
    version="$(semver "$current" -i "$version")"
    version="$version-SNAPSHOT"
else
    [[ "$commit" == "$(git rev-parse origin/main)" ]] || fail "Update local main to match origin/main first"
    case "$version" in
        major|minor|patch)
            current="$(git tag --list --sort=version:refname | grep -E '^[0-9]+\.[0-9]+\.[0-9]+$' | tail -n 1)" || fail "No stable release tags found; specify an explicit version"
            version="$(semver "$current" -i "$version")"
            echo "Selected release version $version after $current"
            ;;
    esac
    branch="$version-release"
    require_unused_tag "$version"
fi

if [[ "$1" != publish ]]; then
    if [[ "$1" == prepare ]]; then
        git switch -c "$branch"
    fi
    ./mvnw -B versions:set -DnewVersion="$version" -DprocessAllModules=true -DgenerateBackupPoms=false
    if [[ "$1" == prepare ]]; then
        echo "Add curated $version notes to CHANGELOG.md, validate, and commit the release version before running next."
    else
        echo "Release commit for publishing: $commit"
        echo "Prepared $version in all modules on the same branch. Commit this version bump and open one PR."
        echo "Ask the reviewer to use Create a merge commit, not squash or rebase."
    fi
    echo "No publishing, commits, tags, or pushes were performed."
    exit 0
fi

commit="$(git rev-parse --verify "${3}^{commit}")"
git merge-base --is-ancestor "$commit" origin/main || fail "Release commit is not on origin/main. Merge the release PR using a merge commit first."
publish_dir="$(mktemp -d "${TMPDIR:-/tmp}/logbook-publish.XXXXXX")"
git worktree add --detach "$publish_dir" "$commit"
trap 'git worktree remove "$publish_dir" || echo "Inspect the remaining worktree: $publish_dir" >&2' EXIT

(
    cd "$publish_dir"
    project_version="$(./mvnw -q help:evaluate -Dexpression=project.version -DforceStdout)"
    [[ "$project_version" == "$version" ]] || fail "Release commit POM version is $project_version, not $version"
    [[ "$(grep -m 1 '^## ' CHANGELOG.md)" == "## [$version]("* ]] || fail "The release commit's first changelog entry must describe $version"

    echo "Publishing $version from merged release commit $commit. This cannot be undone."
    if ! ./mvnw -B clean deploy -P release -Dcentral.skipPublishing=false; then
        fail "Publishing failed. Check the Central Portal before retrying: the upload may have completed."
    fi
)

git tag -a "$version" "$commit" -m "Release $version"
echo "Published $version; the annotated tag is local only."
echo "Push it manually to trigger GitHub release notes:"
echo "git push --no-follow-tags origin refs/tags/$version:refs/tags/$version"
