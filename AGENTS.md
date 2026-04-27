# Nuxeo Labs — Agent Guide

## Project

Nuxeo LTS 2025 plugin (Java 21, Maven) providing a collection of reusable Automation operations, Automation scripting helpers, and miscellaneous utilities developed by the Nuxeo Solution Engineering team.

- **Parent**: `org.nuxeo:nuxeo-parent:2025.16`
- **GroupId**: `org.nuxeo.labs`
- **Version**: `2025.1.0-SNAPSHOT`
- **Target Nuxeo version**: `2025.*`

## Modules

| Module | Purpose |
|--------|---------|
| `nuxeo-labs-operations` | Automation operations: blob handling, image sheet builder, HTTP client |
| `nuxeo-labs-automation-helpers` | Automation scripting context helper: server-side file system utilities |
| `nuxeo-labs-utils` | Misc utilities: ordered `StringList`-backed `PageProvider` |
| `nuxeo-labs-mp` | Nuxeo Marketplace package containing all three bundles above |
| `nuxeo-labs-operations-mp` | Nuxeo Marketplace package containing the operations bundle only |

Key paths in the core modules:

- Java sources: `<module>/src/main/java/org/nuxeo/labs/`
- Component XMLs: `<module>/src/main/resources/OSGI-INF/`
- Bundle manifests: `<module>/src/main/resources/META-INF/MANIFEST.MF`
- Tests: `<module>/src/test/java/org/nuxeo/labs/`

## Build & Test Commands

```bash
# Full build (all modules)
mvn clean install

# Build skipping tests
mvn clean install -DskipTests

# Run tests only in the operations module
mvn test -pl nuxeo-labs-operations

# Run a single test class
mvn test -pl nuxeo-labs-operations -Dtest=ImagesSheetTest

# Run tests in the automation helpers module
mvn test -pl nuxeo-labs-automation-helpers
```

No CI workflows, no linter, no formatter configured in this repo. The `nuxeo-parent` POM may enforce Spotless but `nuxeo.skip.enforcer=true` is set.

## Current Code

### `nuxeo-labs-operations` (`Bundle-SymbolicName: org.nuxeo.labs.operations`)

**Operations:**

- **`GetManagedBlobOp`** (`@Operation id = "GetManagedBlob"`) — Fetches a managed blob from a named `BlobProvider` using a given key, MIME type, filename, and length.
- **`HTTPCall`** (`@Operation id = "HTTPlabs.Call"`) — Generic HTTP client supporting GET, POST, PUT, DELETE, etc. Returns a `StringBlob` with JSON `{ status, statusMessage, error, result }`. Headers can be supplied as a `Properties` key-value list or raw JSON string.
- **`HTTPDownloadFile`** (`@Operation id = "HTTPlabs.DownloadFile"`) — Downloads a remote file via HTTP GET and returns a `FileBlob`. Stores status JSON in the `OperationContext` under key `"httpDownloadFileStatus"`.
- **`ImagesSheetBuilderOp`** (`@Operation id = "ImagesSheet.Build"`) — Builds a JPEG contact sheet from a `DocumentModelList` or `BlobList` using ImageMagick's `montage` command. Exposes tile, label, geometry, colors, font, and picture view parameters.
- **`ImagesSheetBuilderCustomOp`** (`@Operation id = "ImagesSheet.CustomBuild"`) — Like `ImagesSheet.Build` but accepts a custom contributed command line name and extra parameters as a `Properties` map.

**Helpers:**

- **`ImagesSheetBuilder`** — Plain Java helper (not an `@Operation`) used internally by the two image sheet operations. Builds a montage via `CommandLineExecutorService`. Default command: `IM-montage`.
- **`HTTPUtils`** — Static utility: `addHeaders(HttpURLConnection, Properties, String)` used by both HTTP operations.

**OSGI-INF contributions:**

- `nuxeo-labs-operations.xml` — Registers all 5 operations.
- `nuxeo-labs-operations-conversions.xml` — Registers the `IM-montage` command line (ImageMagick `montage`).

### `nuxeo-labs-automation-helpers` (`Bundle-SymbolicName: nuxeo-labs-automation-helpers`)

- **`FileUtils`** — Implements `ContextHelper`; registered as `"FileUtils"` in Automation scripting context. Provides server-side file system operations callable from JavaScript Automation scripts: `readFileToBlob`, `readFileToText`, `createFile`, `appendToFile`, `deleteFile`, `saveBlob`, `getFiles`, `getFolders`, `createFolder`.

> [!WARNING]
> Never expose path parameters to REST callers — this helper is intended for trusted server-side scripting only.

**OSGI-INF contributions:**

- `AutomationHelpers-contrib.xml` — Registers `FileUtils` as a context helper.

### `nuxeo-labs-utils` (`Bundle-SymbolicName: org.nuxeo.labs.utils`)

- **`StringListPageProvider`** — Extends `AbstractPageProvider<DocumentModel>`. Returns an ordered list of documents whose IDs are stored in a multi-valued String field (xpath) on the current document. Falls back to `CollectionConstants.COLLECTION_DOCUMENT_IDS_PROPERTY_NAME` if no xpath is configured and the current document is a Collection.

**OSGI-INF contributions:**

- `nuxeo-labs-utils-stringlist_pageprovider.xml` — Registers the `order_for_stringlist` page provider (xpath must be overridden in a project-specific contribution).

## Adding New Code

### New Automation Operation

1. Create the Java class annotated with `@Operation(id = "...")` containing `@OperationMethod`
2. Create or update an OSGI-INF XML to register it via `<extension target="org.nuxeo.ecm.core.operation.OperationServiceComponent" point="operations">`
3. List the XML in `META-INF/MANIFEST.MF` under `Nuxeo-Component:` (continuation lines start with a single space)
4. MANIFEST.MF **must end with a trailing newline** or the last header is silently dropped

### New Automation Context Helper

1. Create a class implementing `org.nuxeo.ecm.automation.context.ContextHelper`
2. Register it via `<extension target="org.nuxeo.ecm.core.operation.OperationServiceComponent" point="contextHelpers">`
3. The `id` attribute determines how it is accessed in JavaScript Automation (e.g., `id="FileUtils"` → `FileUtils.myMethod()`)
4. Register the XML in `MANIFEST.MF`

### New CommandLine Contribution

1. Add a `<command>` entry to an OSGI-INF XML targeting `org.nuxeo.ecm.platform.commandline.executor.service.CommandLineExecutorComponent`, point `command`
2. Use `#{paramName}` placeholders in the parameter string; pass them via `CmdParameters`

### Dependencies

Module POMs declare dependencies **without `<version>` tags** — versions are managed by `nuxeo-parent`. Key compile dependencies:

- `nuxeo-labs-operations`: `nuxeo-platform-commandline-executor`, `nuxeo-platform-imaging-core`
- `nuxeo-labs-automation-helpers`: `nuxeo-automation-core`, `commons-io`
- `nuxeo-labs-utils`: `nuxeo-runtime`, `nuxeo-core-api`, `nuxeo-platform-collections-core`

## Critical Pitfalls

These will cause silent failures or build errors if ignored:

- **NOT Spring**: No `@Autowired`, `@Component`, `@Service`. Use Nuxeo's component model (`DefaultComponent`, `Framework.getService()`)
- **Jakarta, not javax**: This is LTS 2025 (Java 21 era) — EE imports use `jakarta.*`, **not** `javax.*` (note: `javax.imageio` from the JDK is unaffected and stays as-is)
- **JUnit 4 only**: Use `@RunWith(FeaturesRunner.class)` + `@Features(...)` + `@Deploy(...)`. No JUnit 5
- **Log4j2 only**: `LogManager.getLogger(MyClass.class)`. No SLF4J, no JCL (`commons-logging`), no `System.out.println`
- **No raw Jackson for REST**: Use Nuxeo's `MarshallerRegistry` framework
- **No JPA**: Document storage uses `CoreSession` / `DocumentModel` API
- **MANIFEST.MF trailing newline**: The file must end with a newline or the last `Nuxeo-Component` entry is silently ignored

## Testing Patterns

```java
@RunWith(FeaturesRunner.class)
@Features(AutomationFeature.class)
@Deploy("org.nuxeo.labs.operations")
public class TestMyOperation {

    @Inject
    protected CoreSession session;

    @Inject
    protected AutomationService automationService;

    @Test
    public void testSomething() throws Exception {
        // ...
    }
}
```

- `@Deploy("bundle.symbolic.name")` — the symbolic name is in `MANIFEST.MF` (`Bundle-SymbolicName`)
- `@Deploy("bundle:OSGI-INF/test-contrib.xml")` — deploy test-only XML contributions
- `TransactionalFeature.nextTransaction()` — flush async work between steps
- Disable async picture generation in picture-related tests by deploying `disable-default-picture-generation-contrib.xml`

Note: some HTTP tests (`HTTPTest`) are skipped unless specific environment variables (`NXLABS_TEST_HTTPCCAL_NUXEO_SERVER_DOC_URL`, etc.) are set.

## Local References (optional)

If the Nuxeo LTS 2025 source code or other plugin examples are available locally, working with local files is faster and avoids network calls. Ask the user for local paths before falling back to GitHub.

Prompt the user with:

> "Do you have the Nuxeo LTS 2025 source code cloned locally? If so, what is the path? (e.g., `~/GitHub/nuxeo/nuxeo-lts`). Otherwise, I'll use the GitHub repository."

Similarly, for plugin examples:

> "Do you have any Nuxeo plugin examples locally? (e.g., `nuxeo-labs-dynamic-fields`, `nuxeo-labs-signature-webui`). If so, what are the paths? Otherwise, I'll browse them on GitHub."

### Fallback URLs

- Nuxeo LTS 2025: https://github.com/nuxeo/nuxeo-lts (branch `2025`)
- Plugin examples: https://github.com/nuxeo-sandbox/nuxeo-labs-dynamic-fields, https://github.com/nuxeo-sandbox/nuxeo-labs-signature-webui

## Code Style

### Java

- 4-space indent, **no tabs**, no trailing spaces, K&R braces, ~120 char lines
- Use modern Java: `var`, records, pattern matching `instanceof`, switch expressions, text blocks, `String.formatted()`
- **No wildcard imports**. Import order: static, `java.*`, `jakarta.*`, `org.*`, `com.*`
- Always use braces for `if`/`else` blocks (even single-line)
- No `final` on method parameters or local variables
- No `private` methods/fields (use `protected`); exceptions: `log` and `serialVersionUID`
- No `final` classes or methods (hinders extensibility)
- Use `i++` (not `++i`) for simple increments
- Use `Objects.requireNonNull` for null checks
- Logging: parameterized messages `log.debug("Processing: {}", docId)`, use `if (log.isDebugEnabled())` for non-constant messages
- Javadoc first sentence: 3rd person verb phrase ending with period (*Gets the foobar.* not *Get the foobar*)
- `@since 2025.XX` on new public API. No `@author` tag
- License header: Apache 2.0 with current year and `Contributors:` section

### XML (OSGI-INF, POMs, XSD, HTML)

- **2-space indent**, no tabs, 120 char line width
- Self-closing tags: add space before `/>` (e.g., `<property name="foo" />`)

### JavaScript (Automation Scripting)

- **4-space indent**, no tabs
- Nuxeo Automation Scripting uses **ECMAScript 5** (no `let`/`const`, no arrow functions, no template literals)
- Automation operations are called as global functions: `Document.Create(input, type, name, properties)`, etc. — always pass named parameters as an object

### Markdown (README, docs)

- Use GitHub alert syntax for notes, warnings, tips, etc.:
  ```
  > [!NOTE]
  > Content here

  > [!TIP]
  > Content here

  > [!IMPORTANT]
  > Content here

  > [!WARNING]
  > Content here

  > [!CAUTION]
  > Content here
  ```
- See [GitHub alerts documentation](https://docs.github.com/en/get-started/writing-on-github/getting-started-with-writing-and-formatting-on-github/basic-writing-and-formatting-syntax#alerts)

## Release Process

> [!WARNING]
> Check the repository is clean before starting. Alert and stop if there are uncommitted changes.

> [!IMPORTANT]
> The version numbers below are examples only. Always read the actual current version from the POM before running any command, and derive the release and next snapshot versions from it (e.g. `2025.1.0-SNAPSHOT` → release `2025.1.0` → next snapshot `2025.2.0-SNAPSHOT`).

1. Remove `-SNAPSHOT` from the current version: `mvn versions:set -DnewVersion=<current-version-without-SNAPSHOT> -DgenerateBackupPoms=false`
2. Build: `mvn clean install -DskipTests`
3. Copy `nuxeo-labs-mp/target/nuxeo-labs-mp-<version>.zip` (and optionally `nuxeo-labs-operations-mp/target/nuxeo-labs-operations-mp-<version>.zip`) to `~/Downloads/`
4. Bump to next snapshot (increment minor, add `-SNAPSHOT`): `mvn versions:set -DnewVersion=<next-version>-SNAPSHOT -DgenerateBackupPoms=false`
5. Verify: `mvn clean install -DskipTests`
6. Commit and push:
   ```bash
   git add .
   git commit -m "Post <version> release"
   git push
   ```

> [!NOTE]
> No git tag or GitHub release is created. The ZIP(s) copied to `~/Downloads` are the deliverables.
