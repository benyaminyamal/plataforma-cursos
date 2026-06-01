# Plataforma de Cursos — Sistema de Inscripción

Microservicio desarrollado con **Spring Boot 3** y **Java 17** para una plataforma
educativa virtual. Permite consultar cursos, registrar nuevos cursos e inscribir
estudiantes en uno o más cursos, con **persistencia en Oracle Cloud (Autonomous
Database)** y **despliegue continuo (CI/CD)** hacia **Docker Hub** y **AWS EC2**.

Además, genera el **resumen de inscripción como archivo PDF** y permite
**almacenarlo en un bucket de AWS S3** (subir, modificar, descargar y borrar).

> Actividades formativas:
> - Semana 1: *"Desplegando aplicaciones en la nube"* (microservicios + CI/CD).
> - Semana 2: *"Almacenando archivos en la nube"* (generación de archivo + AWS S3).

---

## Tabla de contenido
1. [Arquitectura](#arquitectura)
2. [Endpoints de la API](#endpoints-de-la-api)
3. [Ejecutar en local](#ejecutar-en-local)
4. [Base de datos en Oracle Cloud](#base-de-datos-en-oracle-cloud)
5. [Docker](#docker)
6. [CI/CD: Docker Hub + AWS EC2](#cicd-docker-hub--aws-ec2)
7. [Secrets de GitHub](#secrets-de-github-requeridos)

---

## Arquitectura

```
plataforma-cursos/
├── src/main/java/com/plataforma/cursos/
│   ├── controller/      # CursoController, InscripcionController, ResumenController
│   ├── service/         # Negocio + ResumenPdfService + S3StorageService + ResumenService
│   ├── repository/      # Spring Data JPA
│   ├── model/           # Entidades: Curso, Inscripcion, InscripcionDetalle
│   ├── dto/             # Request/Response (records)
│   ├── config/          # S3Config (cliente AWS S3)
│   └── exception/       # Manejo global de errores
├── src/main/resources/
│   ├── application.properties          # Perfil por defecto -> Oracle Cloud
│   ├── application-local.properties    # Perfil "local" -> H2 en memoria
│   └── data.sql                        # Datos de ejemplo (solo H2/local)
├── Dockerfile                          # Build multi-stage (Maven -> JRE)
└── .github/workflows/deploy.yml        # Pipeline CI/CD
```

| Capa        | Tecnología                          |
|-------------|-------------------------------------|
| Lenguaje    | Java 17                             |
| Framework   | Spring Boot 3.3 (Web, JPA, Validation, Actuator) |
| BD producción | Oracle Cloud — Autonomous Database |
| BD local    | H2 en memoria                       |
| Almacenamiento | AWS S3 (AWS SDK v2)              |
| PDF         | OpenPDF                             |
| Build       | Maven                               |
| Contenedor  | Docker                              |
| CI/CD       | GitHub Actions → Docker Hub → AWS EC2 |

---

## Endpoints de la API

Base URL local: `http://localhost:8080`

### 1. Listar cursos disponibles
```http
GET /api/cursos
```
Respuesta `200 OK`:
```json
[
  { "id": 1, "nombre": "Introduccion a Java", "instructor": "Ana Torres", "duracionHoras": 40, "costo": 120.00 }
]
```

### 2. Agregar un nuevo curso
```http
POST /api/cursos
Content-Type: application/json

{
  "nombre": "Spring Boot Avanzado",
  "instructor": "Luis Gomez",
  "duracionHoras": 60,
  "costo": 200.00
}
```
Respuesta `201 Created` con el curso creado.

### 3. Inscribir estudiante en uno o más cursos
```http
POST /api/inscripciones
Content-Type: application/json

{
  "estudianteNombre": "Juan Perez",
  "estudianteEmail": "juan@example.com",
  "cursoIds": [1, 2]
}
```
Respuesta `201 Created` — **resumen de la inscripción** con los cursos seleccionados,
el costo de cada uno y el total a pagar:
```json
{
  "inscripcionId": 1,
  "estudianteNombre": "Juan Perez",
  "estudianteEmail": "juan@example.com",
  "fechaInscripcion": "2026-06-01T10:30:00",
  "cursos": [
    { "cursoId": 1, "nombre": "Introduccion a Java", "instructor": "Ana Torres", "costo": 120.00 },
    { "cursoId": 2, "nombre": "Spring Boot Avanzado", "instructor": "Luis Gomez", "costo": 200.00 }
  ],
  "total": 320.00
}
```

### 4. Resumen de inscripción como archivo (Semana 2)

| Método | Endpoint | Descripción |
|---|---|---|
| `GET`    | `/api/inscripciones/{id}/resumen/archivo` | Genera el resumen como **PDF físico** descargable |
| `POST`   | `/api/inscripciones/{id}/resumen/s3`      | **Sube** el resumen a un bucket de S3 (carpeta = número del resumen) |
| `PUT`    | `/api/inscripciones/{id}/resumen/s3`      | **Modifica/regenera** el archivo en S3 |
| `GET`    | `/api/inscripciones/{id}/resumen/s3`      | **Descarga** el archivo desde S3 |
| `DELETE` | `/api/inscripciones/{id}/resumen/s3`      | **Borra** el archivo de S3 |

Ejemplos:
```bash
# Descargar el PDF del resumen al computador
curl -OJ http://localhost:8080/api/inscripciones/1/resumen/archivo

# Subir el resumen al bucket de S3 (se guarda en la carpeta "1/")
curl -X POST http://localhost:8080/api/inscripciones/1/resumen/s3

# Descargar desde S3 / modificar / borrar
curl -OJ http://localhost:8080/api/inscripciones/1/resumen/s3
curl -X PUT    http://localhost:8080/api/inscripciones/1/resumen/s3
curl -X DELETE http://localhost:8080/api/inscripciones/1/resumen/s3
```

En S3 el archivo queda como: `1/resumen-inscripcion-1.pdf`
(la carpeta `1/` corresponde al número del resumen).

> Health check (usado por Docker/EC2): `GET /actuator/health`

---

## Ejecutar en local

Requisitos: **JDK 17** y **Maven** (o usar el contenedor).

```bash
# Usa H2 en memoria con datos de ejemplo (no requiere Oracle)
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Probar:
```bash
curl http://localhost:8080/api/cursos
```

Ejecutar las pruebas:
```bash
mvn test
```

---

## Base de datos en Oracle Cloud

1. Crea una **Autonomous Database** en Oracle Cloud.
2. Descarga el **Wallet** (botón *DB Connection → Download Wallet*). Obtendrás un `.zip`.
3. Descomprime el wallet en una carpeta (ej. `wallet/`). Contiene `tnsnames.ora`,
   `cwallet.sso`, `sqlnet.ora`, etc. El archivo `tnsnames.ora` lista nombres de
   servicio como `midb_high`, `midb_tp`, etc.
4. La aplicación se conecta usando estas variables de entorno:

| Variable      | Ejemplo                                                        |
|---------------|----------------------------------------------------------------|
| `DB_URL`      | `jdbc:oracle:thin:@midb_high?TNS_ADMIN=/app/wallet`            |
| `DB_USERNAME` | `ADMIN`                                                        |
| `DB_PASSWORD` | `TuPasswordSeguro123`                                          |
| `TNS_ADMIN`   | `/app/wallet` (ruta del wallet dentro del contenedor)          |

> ⚠️ El wallet **no** se incluye en la imagen Docker ni en el repositorio (ver
> `.gitignore` / `.dockerignore`). Se monta en tiempo de ejecución.

Las tablas (`CURSOS`, `INSCRIPCIONES`, `INSCRIPCION_DETALLE`) se crean
automáticamente vía Hibernate (`ddl-auto=update`).

---

## Almacenamiento en AWS S3 (Semana 2)

El resumen de inscripción se genera como PDF y puede guardarse en un bucket de S3.

1. Crea un **bucket** en S3 (ej. `plataforma-cursos-resumenes`).
2. La aplicación usa estas variables de entorno:

| Variable        | Ejemplo                        |
|-----------------|--------------------------------|
| `AWS_S3_BUCKET` | `plataforma-cursos-resumenes`  |
| `AWS_REGION`    | `us-east-1`                    |

3. **Credenciales** (cadena por defecto del SDK de AWS):
   - **En local:** define `AWS_ACCESS_KEY_ID` y `AWS_SECRET_ACCESS_KEY` (o usa `aws configure`).
   - **En EC2 (recomendado):** asocia un **IAM Role** a la instancia con permisos
     sobre el bucket (`s3:PutObject`, `s3:GetObject`, `s3:DeleteObject`). Así no se
     guardan credenciales en ningún lado.

Política IAM mínima de ejemplo:
```json
{
  "Version": "2012-10-17",
  "Statement": [{
    "Effect": "Allow",
    "Action": ["s3:PutObject", "s3:GetObject", "s3:DeleteObject"],
    "Resource": "arn:aws:s3:::plataforma-cursos-resumenes/*"
  }]
}
```

Ejecución local apuntando a S3:
```bash
export AWS_ACCESS_KEY_ID=...
export AWS_SECRET_ACCESS_KEY=...
export AWS_REGION=us-east-1
export AWS_S3_BUCKET=plataforma-cursos-resumenes
mvn spring-boot:run
```

> El endpoint `GET .../resumen/archivo` (descarga del PDF) funciona **sin** AWS;
> solo los endpoints `/s3` requieren bucket y credenciales.

---

## Docker

Construir y ejecutar localmente apuntando a Oracle Cloud:

```bash
# Construir la imagen
docker build -t plataforma-cursos .

# Ejecutar (montando el wallet de Oracle Cloud)
docker run -d --name plataforma-cursos -p 8080:8080 \
  -e DB_URL='jdbc:oracle:thin:@midb_high?TNS_ADMIN=/app/wallet' \
  -e DB_USERNAME='ADMIN' \
  -e DB_PASSWORD='TuPasswordSeguro123' \
  -e TNS_ADMIN=/app/wallet \
  -v "$(pwd)/wallet:/app/wallet:ro" \
  plataforma-cursos
```

---

## CI/CD: Docker Hub + AWS EC2

El pipeline (`.github/workflows/deploy.yml`) se ejecuta **automáticamente con cada
push a `main`** y realiza:

1. **Build & test** del proyecto con Maven.
2. **Construcción de la imagen** Docker y **publicación en Docker Hub**
   (`<usuario>/plataforma-cursos:latest` y `:<sha>`).
3. **Despliegue en EC2** vía SSH: hace `docker pull` de la nueva imagen, detiene
   el contenedor anterior y arranca el nuevo exponiendo el puerto `80`.

```
push a main ──> GitHub Actions ──> Docker Hub ──> AWS EC2 (docker run)
```

### Preparación de la instancia EC2 (una sola vez)

1. Lanza una instancia EC2 (Amazon Linux 2023 o Ubuntu) y abre los puertos
   **22 (SSH)** y **80 (HTTP)** en el *Security Group*.
2. Instala Docker:
   ```bash
   # Amazon Linux 2023
   sudo dnf install -y docker && sudo systemctl enable --now docker
   sudo usermod -aG docker $USER   # reconectar la sesión SSH después
   ```
3. Copia el **wallet de Oracle Cloud** a la instancia (en el home del usuario SSH):
   ```bash
   scp -i tu-llave.pem -r ./wallet ec2-user@<IP_EC2>:/home/ec2-user/wallet
   ```
   El workflow monta `/home/<EC2_USER>/wallet` dentro del contenedor como
   `/app/wallet`.

Tras el despliegue, la app queda disponible en `http://<IP_EC2>/api/cursos`.

---

## Secrets de GitHub (requeridos)

Configúralos en **Settings → Secrets and variables → Actions**:

| Secret                | Descripción                                              |
|-----------------------|----------------------------------------------------------|
| `DOCKERHUB_USERNAME`  | Usuario de Docker Hub                                    |
| `DOCKERHUB_TOKEN`     | Access Token de Docker Hub                               |
| `EC2_HOST`            | IP pública o DNS de la instancia EC2                     |
| `EC2_USER`            | Usuario SSH (`ec2-user` o `ubuntu`)                      |
| `EC2_SSH_KEY`         | Clave privada SSH (contenido del `.pem`)                 |
| `DB_URL`              | `jdbc:oracle:thin:@midb_high?TNS_ADMIN=/app/wallet`      |
| `DB_USERNAME`         | Usuario de Oracle Cloud (ej. `ADMIN`)                    |
| `DB_PASSWORD`         | Contraseña de la base de datos                           |
| `AWS_S3_BUCKET`       | Nombre del bucket de S3 para los resúmenes               |
| `AWS_REGION`          | Región del bucket (ej. `us-east-1`)                      |

> La instancia EC2 debe tener un **IAM Role** con permisos sobre el bucket. Si
> prefieres credenciales explícitas, añade también `AWS_ACCESS_KEY_ID` y
> `AWS_SECRET_ACCESS_KEY` como secrets y pásalos al contenedor en el workflow.

---

## Licencia

Proyecto académico — uso educativo.
