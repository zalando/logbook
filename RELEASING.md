# Releasing

Start from a clean, up-to-date `main` with `origin` pointing to `zalando/logbook`.

1. Run `./release.sh prepare minor` (or `major` / `patch`) to calculate a version
   from the latest stable release tag. Explicit versions, such as
   `./release.sh prepare 4.3.0-RC.0`, are also supported. The command creates
   `<version>-release` and updates all module POMs. Add a curated section at the
   top of `CHANGELOG.md`, including any breaking changes and migration instructions.
2. Validate and commit the release version and notes. From that clean release
   branch, run `./release.sh next`. It prints the release commit SHA and updates
   all module POMs to the next **minor SNAPSHOT**, without switching branches.
   Use `next major` or `next patch` to override the increment.
3. Commit the SNAPSHOT bump as a second commit containing **only version changes**,
   then push the branch and open **one PR**. Ask the reviewer to select
   **Create a merge commit**, not squash or rebase, after review and CI pass.
   Any review corrections must be included in the release commit; amend it and
   recreate the SNAPSHOT commit if needed, using the updated release SHA.
4. After merging, update local `main`, then run
   `./release.sh publish <version> <release-commit> --confirm-publish <version>`.
   The script requires that release commit to be reachable from `origin/main`.
   It builds and publishes that exact commit in a temporary worktree, waits for
   Central publication, and creates its local annotated tag. `main` keeps the
   next SNAPSHOT version. The script never pushes.
5. Verify publication, then push only that tag:
   `git push --no-follow-tags origin refs/tags/<version>:refs/tags/<version>`.
   This triggers the existing GitHub release-notes workflow.

Preparation cannot publish. Maven Central uploads are also disabled by default
in the release profile; the confirmed publish command explicitly enables them.
The `semver` CLI is needed for version increments (`npm install -g semver`).
No GitHub token or Docker image is needed.

If publishing fails or succeeds without creating the local tag, **check the
Central Portal before retrying**. Publication is irreversible; do not redeploy
an already-published version. If only tag pushing fails, retry that push, not publishing.
