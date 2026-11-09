# Proyecto 2 – Sistema de Reserva de Recursos (EIF206)

Autora: Fiorella Arce Valverde

## Requisitos
- Java 21
- Maven
- MySQL 8

## Base de datos
1. Abrir MySQL Workbench.
2. Ejecutar el script `database/reservas_recursos.sql`.
3. Usuario inicial: `admin` / `admin`.

## Variables de entorno
| Variable | Uso |
|---|---|
| DB_USER | Usuario de MySQL (obligatoria, Backend) |
| DB_PASSWORD | Clave de MySQL (obligatoria, Backend) |
| DB_URL | URL JDBC (opcional; por defecto localhost:3306/reservas_recursos) |
| GEMINI_API_KEY | Clave de Gemini (opcional, reserva con IA) |

## Cómo ejecutar
1. Iniciar el Backend: clase `backend.server.BackendServer` (puerto 5000).
2. Iniciar uno o varios Frontend: clase `controller.MainLogin`.

## Pruebas
- `mvn test` ejecuta las pruebas unitarias.
- `mvn verify` ejecuta también las de integración (requiere la base `reservas_recursos_test`).