# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

Spring Boot app for tracking the cost of producing "malhas" (knit fabric): each `Malha` (fabric spec) goes through a series of `Servico` (services/processing steps) performed by a `Fornecedor` (supplier), each logged as a `CustoServico` (cost entry: unit value x quantity). The app computes the total cost of a `Malha` by summing its `CustoServico` entries. Cost entries can be created manually or imported from an external ERP database via a configurable read-only query (see ERP sync below).

- Java version: 25
- Spring Boot version: 4.0.5 (via `spring-boot-starter-parent`)
- Web stack: Spring MVC (`spring-boot-starter-webmvc`) + Thymeleaf server-rendered views + a parallel `/api/**` REST layer
- Persistence: Spring Data JPA over an H2 file database (`./data/malhas`), `ddl-auto=update`
- Base package: `com.example.projeto_padrao`

## Commands

Use the Maven wrapper (`mvnw` / `mvnw.cmd`) rather than a system-installed Maven, since the wrapper pins the correct Maven version for this project.

```
./mvnw clean install        # build
./mvnw spring-boot:run       # run the app locally
./mvnw test                  # run all tests
./mvnw test -Dtest=ProjetoPadraoApplicationTests   # run a single test class
```

On Windows, use `mvnw.cmd` in place of `./mvnw`.

## Architecture

Standard layered structure under `src/main/java/com/example/projeto_padrao/`:

- `domain/` — JPA entities: `Malha`, `Servico`, `Fornecedor`, `CustoServico`, `OrigemCusto` (enum: `MANUAL` or `ERP_SYNC`), and `ErpConsultaConfig`.
- `repository/` — Spring Data JPA repositories, one per entity.
- `service/` — business logic: `CustoCalculoService` (sums a malha's costs into a `CustoTotalMalha`) and `ErpSyncService` (runs an ERP sync).
- `web/` — two controllers per resource:
  - `*Controller` (`@RestController`, under `/api/**`) — JSON CRUD.
  - `*WebController` (`@Controller`) — Thymeleaf-rendered pages under the resource's plain path (e.g. `/malhas`), reusing the same repositories/services as the REST controllers.
- `config/ErpDataSourceConfig.java` — a second, read-only `DataSource`/`JdbcTemplate` (qualifier `erpJdbcTemplate`) pointed at an external ERP database, activated only via `@ConditionalOnProperty` when `erp.datasource.url` is set.

Templates live in `src/main/resources/templates/<resource>/{lista,form}.html`, with shared markup in `templates/fragments/layout.html`.

### ERP sync

`ErpSyncService.sincronizar(ErpConsultaConfig)` runs the arbitrary SQL stored in `ErpConsultaConfig.sqlConsulta` against the ERP datasource, then maps each result row to a `CustoServico` using the column-name mappings also stored on `ErpConsultaConfig` (e.g. `colunaMalhaCodigo`, `colunaValorUnitario`). Referenced `Malha`/`Servico`/`Fornecedor` rows are looked up by name/code or created on the fly. Upserts are keyed on `(malha, servico, fornecedor, dataReferencia)`; imported rows are tagged `OrigemCusto.ERP_SYNC`. The ERP datasource bean only exists when `erp.datasource.url` is configured — `ErpSyncService` injects it as `@Autowired(required = false)` and throws `IllegalStateException` if a sync is attempted while it's unconfigured. `ErpConsultaConfig` is itself editable via the app's own DB/UI so the source query and column mapping can change without a redeploy.

### Local vs ERP datasource

The app's own data (all entities in `domain/`) lives in the primary H2 datasource configured via standard `spring.datasource.*` properties. The ERP connection is a separate, optional, read-only datasource configured via `erp.datasource.*` (see `application.properties` — commented out by default, since the ERP's connection details aren't finalized). `pom.xml` currently bundles MSSQL, PostgreSQL, and MySQL JDBC drivers as runtime deps since the ERP's database engine isn't decided; prune the unused ones once it is.
