---
name: citrus-set-camel-version
description: '# Set Camel Versions'
metadata:
  user-invocable: true
  disable-model-invocation: true
---

# Set Camel Versions

Align dependency and plugin versions in this repository's Maven `pom.xml` files with a specific Apache Camel release version.

## Usage

```
/citrus-set-camel-version <release.version>
```

**Arguments:**
- `<release.version>` - A valid Apache Camel release version (e.g., `4.21.0`, `4.22.0`)

**Examples:**
```
/citrus-set-camel-version 4.21.0
/citrus-set-camel-version 4.22.0
```

## Instructions

### 1. Validate Input

Extract `<release.version>` from the argument. It must be a valid Camel version string (e.g., `4.21.0`). If missing or malformed, ask the user to provide a valid version.

### 2. Fetch the Camel Parent POM

Fetch the Camel parent POM for the given release version:

```
https://raw.githubusercontent.com/apache/camel/refs/tags/camel-<release.version>/parent/pom.xml
```

Extract **all version properties** from the `<properties>` section (those ending in `-version` or `.version`). If the fetch fails, report the error and stop.

### 3. Read the Citrus Root POM

Read the root `pom.xml` of this repository and extract all version properties from its `<properties>` section.

### 4. Compare and Identify Updates

First, update the `apache.camel.version` property in the Citrus POM to the target release version — this is the Camel dependency itself.

Then, for each **other** version property in the Citrus POM, find the corresponding property in the Camel POM by matching on the dependency/library they refer to (property names may differ between projects). This includes both dependency versions and Maven plugin versions. Classify each match:

- **Outdated**: Citrus version is older than the Camel version — these will be updated
- **Same**: Versions match — no action needed
- **Newer**: Citrus version is newer than the Camel version — keep the Citrus version, report at the end
- **No match**: Property exists only in Citrus or only in Camel — skip

### 5. Apply Version Updates

Update all **outdated** version properties in the root `pom.xml`. Do NOT touch properties where the Citrus version is already equal to or newer than the Camel version.

### 6. Update Hardcoded Camel Version References

Search the repository for hardcoded references to the **old** Camel version and update them to the new version. Key locations include:

- **JBang default version**: `tools/jbang/src/main/java/org/citrusframework/jbang/cli/CitrusJBangMain.java` — update the `CAMEL_VERSION_DEFAULT` constant
- **Any other files** referencing the old Camel version string directly

### 7. Sync Camel Test Infrastructure Catalog

When the Camel version changes, the set of available Camel test infrastructure services may change (services added or removed). Check the new Camel version for changes to `camel-test-infra-*` modules and update:

- **Catalog control files**: `tools/schema-generator/src/test/resources/control/citrus-catalog-aggregate-infra-services.json` and `citrus-catalog-aggregate-test-actions.json` — add entries for new services, remove entries for deleted services
- **Test assertions**: `endpoints/citrus-camel/src/test/java/org/citrusframework/camel/actions/infra/InfraServiceUtilsTest.java` — update the expected service name list in `shouldListInfraServices()`

To discover changes, compare the Camel test infrastructure modules between the old and new version tags (e.g., list directories under `test-infra/` in the Camel repository for both versions).

### 8. Build and Fix Tests

Run a module-specific build for any modules with changed files to verify compilation and tests pass. If tests fail due to the version updates (e.g., updated test expectations, changed APIs), fix them.

### 9. Report Results

After all updates are applied, produce a summary with three sections:

1. **Updated versions** — table with: Property | Old Version | New Version
2. **Newer versions in Citrus** — table with: Property | Citrus Version | Camel Version (these were intentionally kept)
3. **Test fixes** — list any test or source files that needed adjustment beyond the version property changes

### 10. Constraints

- **Only update outdated versions** — never downgrade a version that is already newer in Citrus
- **Module-specific builds** — always run `mvn` in the module directory where changes occurred; do NOT parallelize Maven jobs
- **No new dependencies** — this skill only aligns existing shared dependency versions
- **Preserve property naming** — do not rename Citrus properties to match Camel naming conventions
