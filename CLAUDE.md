# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project state

This is a freshly generated Spring Boot (Spring Initializr) scaffold — `com.example.projeto_padrao`. It currently contains no business logic: just the default `ProjetoPadraoApplication` entry point and a stub test. Treat any architectural conventions as not-yet-established; when adding the first real feature, set the package structure and layering intentionally rather than assuming one exists.

- Java version: 25
- Spring Boot version: 4.0.5 (via `spring-boot-starter-parent`)
- Web stack: `spring-boot-starter-webmvc` (Spring MVC)

## Commands

Use the Maven wrapper (`mvnw` / `mvnw.cmd`) rather than a system-installed Maven, since the wrapper pins the correct Maven version for this project.

```
./mvnw clean install        # build
./mvnw spring-boot:run       # run the app locally
./mvnw test                  # run all tests
./mvnw test -Dtest=ProjetoPadraoApplicationTests   # run a single test class
```

On Windows, use `mvnw.cmd` in place of `./mvnw`.
