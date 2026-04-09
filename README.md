# StaffMode

Professionele Paper/Spigot staffmode plugin met MySQL opslag via HikariCP.

## Features

- `/staffmode` toggle (aan/uit)
- Creative mode bij activatie
- Survival mode bij uitschakelen
- Inventory wordt geleegd bij toggelen
- Staff is onzichtbaar voor normale spelers
- Staffmode wordt automatisch verwijderd bij logout
- Staffmode status wordt opgeslagen in MySQL

## Requirements

- Java 21
- Paper/Spigot 1.21+
- MySQL/MariaDB database

## Installatie

1. Build de plugin (zie Build).
2. Plaats de jar in `plugins/`.
3. Start de server om `config.yml` te genereren.
4. Vul database gegevens in `plugins/StaffMode/config.yml`.
5. Restart de server.

## Commands

- `/staffmode` (alias: `/sm`)

## Permissions

- `staffmode.use` (default: op)

## Config

`plugins/StaffMode/config.yml`

```yml
database:
  host: "localhost"
  port: 3306
  database: "minecraft"
  username: "root"
  password: ""
  useSSL: false
  allowPublicKeyRetrieval: true
  pool:
    maximumPoolSize: 10
    minimumIdle: 2
    connectionTimeoutMs: 10000
    idleTimeoutMs: 60000
    maxLifetimeMs: 1800000
    leakDetectionThresholdMs: 0
```

## Database

De plugin maakt automatisch een tabel aan:

- `staffmode_players`
  - `uuid` (CHAR(36), PK)
  - `enabled` (TINYINT(1))
  - `updated_at` (TIMESTAMP)

## Build

```bash
mvn -DskipTests package
```

De output jar staat in `target/`.

