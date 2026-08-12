<!--
    Copyright 1996-2026 Traction Software, Inc.
    Licensed under the Apache License, Version 2.0.
-->

# commons — Claude context

## Project

Maven project: `com.tractionsoftware.commons:tractionsoftware-commons:4.0.0-SNAPSHOT`

## Build

```
mvn test               # run tests
mvn site               # regenerate Cobertura HTML coverage report
```

## Coverage report

**Location:** `htmlReport/` in the project root (Cobertura HTML format).

- `htmlReport/index.html` — top-level summary by package
- `htmlReport/ns-*/index.html` — per-package class list
- `htmlReport/ns-*/sources/source-*.html` — annotated source (fc=covered, nc=not covered, pc=partial)

Package ↔ `ns-*` directory mapping (as of last regeneration; may change after `mvn site`):

| ns-dir | package         |
|--------|-----------------|
| ns-1   | codec           |
| ns-2   | codegen.java    |
| ns-3   | config          |
| ns-4   | html            |
| ns-5   | image           |
| ns-6   | io              |
| ns-7   | lang            |
| ns-8   | mail            |
| ns-9   | net             |
| ns-a   | net.http.server |
| ns-b   | processor       |
| ns-c   | properties      |
| ns-d   | text            |
| ns-e   | util            |
| ns-f   | util.function   |
| ns-10  | xml             |

All packages are under `com.tractionsoftware.commons.*`.

## Test conventions

- JUnit 5 (Jupiter): `@Test`, static imports from `org.junit.jupiter.api.Assertions.*`
- TDD/characterization style: tests document actual behavior; bugs flagged in prose comments, not code
- No Mockito available; concrete inner classes for interfaces that have multiple abstract methods
- `mvn` is **not available in the Claude sandbox** — test correctness must be verified by code review
- `TextTransformer` has **two** abstract methods — not a functional interface, use an inner class
- `PropertyCache.createInstance()` — factory method (no public constructor)
- `SimplePropertyNameMapper.DEFAULT_SEPARATOR = '_'` — default separator for namespace/prefix
- slf4j-simple runs at WARN level in tests — `logger.isDebugEnabled()` returns false
- Blank `MapPropertyStore` for tests: `MapPropertyStore.createDefaultInstance()` or one of the other factory
  constructors as necessary.
- `PropStore` for tests via `SimpleProperties.asPropStore(new HashMap<>())`
- `TempFileResource.AbstractFactory` requires implementing both `createImpl` **and** `loadExistingImpl`

## Key property API semantics

- `getNamespace("ns")` → caller uses short key `"bar"`, underlying store key is `"ns_bar"`
- `getPrefix("ns")` → **inverse**: caller uses full key `"ns_bar"`, underlying store key is `"bar"`
- `GetPutProperty.toWriteOnly()` → returns `ForwardingPutProperty`, a pure `PutProperty` (not `GetPutProperty`); useful
  to reach `PutProperty` interface default methods
