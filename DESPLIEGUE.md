# Despliegue en Render + Aiven (guía rápida)

Arquitectura: **Render** ejecuta la app (Docker con Tomcat 10.1 / JDK 17) y **Aiven** aloja MySQL.
El proyecto local (NetBeans + XAMPP) no se modifica: todo lo de la nube está en `Dockerfile`, `docker/` e `incidencias_db_nube.sql`.

## 1. Base de datos (Aiven)
1. Crear cuenta en aiven.io (sin tarjeta) -> Create service -> MySQL -> plan Free.
2. Cuando esté en "Running", copiar de la pestaña Overview: Host, Port, User (avnadmin), Password y la base (defaultdb).
3. Importar `incidencias_db_nube.sql` en `defaultdb` (cliente `mysql` o DBeaver, conexión con SSL).
   Comprobar que existen las 5 tablas y el usuario `admin`.

## 2. Código (GitHub)
Subir esta carpeta completa a un repositorio (con `Dockerfile` en la raíz y el `.jar` de `web/WEB-INF/lib`).

## 3. App (Render)
New > Web Service > repositorio > Language/Runtime: **Docker** > plan Free. Variables de entorno:

| Variable | Valor |
|---|---|
| `INCIDENCIAS_DB_URL` | `jdbc:mysql://HOST:PORT/defaultdb?sslMode=REQUIRED` |
| `INCIDENCIAS_DB_USER` | `avnadmin` |
| `INCIDENCIAS_DB_PASSWORD` | (la de Aiven) |

Deploy. La primera vez tarda unos minutos (compila dentro de Docker).

## 4. Primer ingreso
Elegir "Administrador de personal", usuario `admin`, contraseña inicial (la entrega Claude en el chat; no está en el repositorio).
El sistema obliga a cambiarla en el acto.

## Día de la presentación
- Entrar a la web y a la consola de Aiven ~10 minutos antes (el servicio gratis de Render se duerme y la base de Aiven puede apagarse si no se usa).
- Tener el plan B: demo local con XAMPP/Tomcat y el video.
