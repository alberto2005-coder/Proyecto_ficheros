# 📁 Proyecto Ficheros — Sistema de gestión de archivos en Java

[![Java](https://img.shields.io/badge/Java-17%2B-orange?style=flat-square&logo=java)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?style=flat-square&logo=spring-boot)](https://spring.io/projects/spring-boot)
[![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3.x-blue?style=flat-square)](https://www.thymeleaf.org/)

[Español](#español) · [English](#english)

---

## Español

### Índice

- [Descripción](#descripción)
- [Características](#características)
- [Tecnologías](#tecnologías)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Instalación y ejecución](#instalación-y-ejecución)
- [Licencia](#licencia)

### Descripción

**Proyecto Ficheros** es una aplicación web desarrollada con **Spring Boot** para la gestión avanzada de archivos y sistemas de persistencia no relacionales (**RAF** y **XML**) en Java.

Es una herramienta educativa y funcional para explorar, editar y gestionar archivos del sistema local desde una interfaz web moderna. Su núcleo simula operaciones SQL sobre archivos de **Acceso Aleatorio (RAF)** y archivos **XML**, lo que permite tratar datos estructurados sin una base de datos tradicional.

### Características

- **Explorador de archivos**: navegación completa por el sistema de archivos local, con creación y eliminación de ficheros y directorios.
- **Editor de texto**: edición directa de archivos de texto plano desde el navegador.
- **Emulador SQL sobre RAF**: permite ejecutar `SELECT`, `UPDATE` y `DELETE` sobre archivos binarios con estructura de registro fija (ID, Nombre, Edad).
- **Gestión XML avanzada**:
  - Serialización y deserialización con **JAXB**.
  - Manipulación de nodos con **DOM**.
  - Emulación de operaciones SQL sobre archivos XML.
- **Conversor de codificación**: transforma archivos entre UTF-8, ASCII, UTF-16 e ISO-8859-1.
- **Esquemas flexibles**: versiones adaptativas para trabajar con estructuras de datos dinámicas en RAF y XML.
- **Exportación e importación**: exporta el listado de archivos o los registros RAF a formato XML.

### Tecnologías

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 17+ |
| Framework | Spring Boot 3 (Spring MVC) |
| Motor de plantillas | Thymeleaf |
| Persistencia | Java File I/O, `RandomAccessFile`, JAXB, DOM |
| Dependencias | Maven |

### Estructura del proyecto

```text
src/main/java/com/example/fileinfo/
├── Aplicacion.java         # Punto de entrada de Spring Boot
├── controller/
│   └── FileController.java # Endpoints y control de flujo
├── model/
│   ├── FileInfo.java       # POJO para metadatos de archivos
│   ├── Schema.java         # Definición de estructuras dinámicas
│   └── ...
└── util/
    ├── RAFSqlEmulator.java # Lógica SQL para archivos binarios
    ├── XMLUtil.java        # Utilidades de JAXB y DOM
    ├── ...Flexible.java    # Versiones para esquemas dinámicos
```

### Instalación y ejecución

1. **Requisitos**: JDK 17 o superior y Maven instalado.
2. **Descarga** el código fuente.
3. **Compila**:

   ```bash
   mvn clean install
   ```

4. **Ejecuta**:

   ```bash
   mvn spring-boot:run
   ```

5. **Accede** desde el navegador a [http://localhost:8080](http://localhost:8080).

> [!NOTE]
> Este proyecto se desarrolló como parte de un estudio profundo sobre el manejo de flujos de datos (*streams*) y persistencia aleatoria en Java.

### Licencia

**MIT License** © 2025 TeamProjectsDev. Consulta [LICENSE](LICENSE).

---

## English

### Table of contents

- [Overview](#overview)
- [Features](#features)
- [Tech stack](#tech-stack)
- [Project structure](#project-structure)
- [Installation and running](#installation-and-running)
- [License](#license)

### Overview

**Project Files** is a web application built with **Spring Boot** for advanced file management and non-relational persistence systems (**RAF** and **XML**) in Java.

It is an educational yet functional tool to explore, edit and manage local system files through a modern web interface. Its core simulates SQL operations over **Random Access Files (RAF)** and **XML** files, so structured data can be handled without a traditional database.

### Features

- **File explorer**: full navigation of the local file system, including creating and deleting files and directories.
- **Text editor**: edit plain text files directly from the browser.
- **SQL emulator over RAF**: run `SELECT`, `UPDATE` and `DELETE` on binary files with a fixed record structure (ID, Name, Age).
- **Advanced XML management**:
  - Serialization and deserialization with **JAXB**.
  - Node manipulation with **DOM**.
  - SQL operation emulation over XML files.
- **Encoding converter**: transform files between UTF-8, ASCII, UTF-16 and ISO-8859-1.
- **Flexible schemas**: adaptive versions for dynamic data structures in both RAF and XML.
- **Export and import**: export file listings or RAF records to XML.

### Tech stack

| Layer | Technology |
|---|---|
| Language | Java 17+ |
| Framework | Spring Boot 3 (Spring MVC) |
| Template engine | Thymeleaf |
| Persistence | Java File I/O, `RandomAccessFile`, JAXB, DOM |
| Dependencies | Maven |

### Project structure

```text
src/main/java/com/example/fileinfo/
├── Aplicacion.java         # Spring Boot entry point
├── controller/
│   └── FileController.java # Endpoints and flow control
├── model/
│   ├── FileInfo.java       # POJO for file metadata
│   ├── Schema.java         # Dynamic structure definitions
│   └── ...
└── util/
    ├── RAFSqlEmulator.java # SQL logic for binary files
    ├── XMLUtil.java        # JAXB and DOM utilities
    ├── ...Flexible.java    # Versions for dynamic schemas
```

### Installation and running

1. **Requirements**: JDK 17 or later and Maven installed.
2. **Download** the source code.
3. **Compile**:

   ```bash
   mvn clean install
   ```

4. **Run**:

   ```bash
   mvn spring-boot:run
   ```

5. **Open** [http://localhost:8080](http://localhost:8080) in your browser.

> [!NOTE]
> This project was built as part of a deep dive into data stream handling (*streams*) and random-access persistence in Java.

### License

**MIT License** © 2025 TeamProjectsDev. See [LICENSE](LICENSE).
