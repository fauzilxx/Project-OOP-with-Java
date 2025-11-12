## Repo snapshot

- This repository currently contains a minimal Java skeleton: `Login.java` at the project root.
- There are no build files (no `pom.xml`, `build.gradle`, or `package.json`) and no `src/` or `test/` folders.

## What an AI coding agent should know (high-value, repo-specific)

- Big picture: the project is a tiny, single-class Java scaffold. Any change should preserve the root-level layout unless you intentionally introduce a package structure.
- `Login.java` is an empty public class. Example path and symbol to reference: `Login.java` -> `public class Login { }`.
- No existing tests or CI workflows are present. Expect that any automated build/test steps will need files added (e.g., Maven/Gradle) to become reproducible.

## Developer workflows (how to build / run locally)

- Quick compile/run (assumes a JDK is installed on the machine). From repository root (Windows bash):

```bash
javac Login.java
java Login
```

- Note: `Login.java` currently has no `main` method, so `java Login` will fail until a `public static void main(String[] args)` is added. Use `javac` to check for compile errors after edits.

## Conventions and patterns to follow in this repo

- Keep Java source and packages consistent with directory layout. If you add package declarations (for example `package com.example;`), move files into `com/example/`.
- Prefer small, incremental changes: add a `main` method or small unit first to make the repo runnable.
- If introducing a build system, prefer Maven (`pom.xml`) or Gradle and place sources under `src/main/java` and tests under `src/test/java`.

## Integration points and external dependencies

- None are present. If you add libraries, include a build manifest (Maven/Gradle) and list dependency coordinates in the manifest.

## Tasks an agent can help with right away (concrete suggestions)

- Make the project runnable: add a basic `main` method to `Login` that prints a short message and compiles with `javac`.
- Add a minimal `pom.xml` or `build.gradle` if the user requests a standard Java build system.
- Add a brief unit test using JUnit (only after adding a build file).

## Assumptions made (documented)

- Java JDK is available on the developer machine (we assume Java 11+ by default). If you need to support a specific Java version, add a `README` or build file to pin it.

## When you are uncertain

- If a requested change could alter project layout (new packages, modules, or a build tool), ask the user before making that change.

---

If anything important is missing (intended packages, preferred Java version, or a target build tool), tell me and I will update this guidance and the repository accordingly.
