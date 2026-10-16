# Contributing

Thanks for helping improve Logbook. The [README](../README.md) describes the library and its [contribution overview](../README.md#getting-involvedcontributing).

## Propose a change

- Do not push directly to `main`. Work on a feature branch, using a fork if you do not have write access, and open a pull request.
- Keep the pull request focused. Describe what changed, why, and how you validated it, including any checks not run. Link a related issue when there is one; an issue is not required for every change.
- Resolve all review discussions before a pull request can be merged. A pull request needs approval from at least one reviewer.
- Follow the existing code style and conventions.

## Development and validation

Use the checked-in Maven wrapper (`./mvnw`) so the project’s Maven version is used.
Read the Java compiler settings in the [root POM](../pom.xml) and any overrides in the module’s POM.
For local verification, use the JDK configured in the [build workflow](workflows/build.yaml).

For code changes, run the full verification before submitting:

```sh
./build.sh
```

This runs `./mvnw verify -B`. You can also invoke that command directly. A focused verification while iterating on `logbook-core` is:

```sh
./mvnw -pl logbook-core -am verify -B
```

Focused verification does not replace full verification for code changes. The `./build.sh --compile` and `./build.sh --no-test-install` options skip tests and are not validation of a change.

Code changes need meaningful tests for their behavior and must maintain the project’s 100% code coverage expectation. `verify` runs the configured JaCoCo line and branch coverage checks. Update relevant documentation when behavior or configuration changes.

Documentation-only changes do not need artificial code tests. Check that the content is accurate, examples are consistent with the project, and links work.

## Design and implementation

- Logbook observes HTTP traffic for logging. Integrations and filters should not change the application’s HTTP behavior; filters change what appears in the log.
- Keep integrations and dependencies optional where they are optional today. Prefer composing existing extension points over adding special cases.
- Preserve public APIs, defaults, and logged metadata unless the change requires otherwise. If a change to a public API, module, dependency, or default is uncertain, discuss the trade-off in the pull request before proceeding.
- See the README’s [processing phases](../README.md#phases) and the relevant integration documentation before changing behavior.

## Privacy and attribution

Use synthetic fixtures in tests and examples. Never publish credentials or other secrets. If a reproducer is based on real traffic, remove sensitive and personal data and confirm you have permission to share it. Retain required attribution and license notices for material you reuse.

## Signed commits

The [pull request checklist](PULL_REQUEST_TEMPLATE.md) requires cryptographically signed commits. Configure Git commit signing and sign each commit, for example:

```sh
git commit -S -m "Describe the change"
```

GitHub’s [commit-signing guide](https://docs.github.com/en/authentication/managing-commit-signature-verification/signing-commits) covers GPG, SSH, and S/MIME signatures. The `-s` option adds a sign-off trailer; it is not a cryptographic signature. The checklist asks for signed commits, not a DCO sign-off.

## AI-assisted contributions

Contributors remain responsible for understanding, reviewing, and validating everything they submit, including its correctness, tests, documentation, and licensing. Be prepared to explain and maintain your changes. The same contribution standards apply regardless of the tools used.

For AI coding agents working in this repository, see the [agent instructions](../AGENTS.md).
