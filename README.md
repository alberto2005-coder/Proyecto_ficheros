# 📁 Proyecto Ficheros - Java File Management System

[![Java](https://img.shields.io/badge/Java-17%2B-orange?style=flat-square&logo=java)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?style=flat-square&logo=spring-boot)](https://spring.io/projects/spring-boot)
[![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3.x-blue?style=flat-square)](https://www.thymeleaf.org/)

Este proyecto es una aplicación web robusta desarrollada con **Spring Boot** para la gestión avanzada de archivos y sistemas de persistencia no relacionales (RAF y XML) en Java.

---

## 🇪🇸 Índice (Español)
1. [Descripción General](#descripción-general)
2. [Características Principales](#características-principales)
3. [Tecnologías Utilizadas](#tecnologías-utilizadas)
4. [Estructura del Proyecto](#estructura-del-proyecto)
5. [Instalación y Ejecución](#instalación-y-ejecución)

---

## 🇺🇸 Index (English)
1. [General Description](#general-description)
2. [Key Features](#key-features)
3. [Technologies Used](#technologies-used)
4. [Project Structure](#project-structure)
5. [Installation and Running](#installation-and-running)

---

## 🇪🇸 Descripción General
**Proyecto Ficheros** es una herramienta educativa y funcional diseñada para explorar, editar y gestionar archivos en el sistema local a través de una interfaz web moderna. Su núcleo se centra en la simulación de operaciones SQL sobre archivos de **Acceso Aleatorio (RAF)** y archivos **XML**, permitiendo tratar datos estructurados sin necesidad de una base de datos tradicional.

## 🇺🇸 General Description
**Project Files** is an educational and functional tool designed to explore, edit, and manage local system files through a modern web interface. Its core focuses on simulating SQL operations over **Random Access Files (RAF)** and **XML** files, allowing structured data handling without the need for a traditional database.

---

## 🇪🇸 Características Principales
- **Explorador de Archivos**: Navegación completa por el sistema de archivos local, creación y eliminación de ficheros y directorios.
- **Editor de Texto**: Edición directa de archivos de texto plano desde el navegador.
- **Emulador SQL sobre RAF**: Permite realizar `SELECT`, `UPDATE` y `DELETE` sobre archivos binarios con estructura de registro fija (ID, Nombre, Edad).
- **Gestión XML Avanzada**:
    - Serialización y deserialización con **JAXB**.
    - Manipulación de nodos con **DOM**.
    - Emulación de operaciones SQL sobre archivos XML.
- **Conversor de Codificación**: Herramienta para transformar archivos entre diferentes encodings (UTF-8, ASCII, UTF-16, ISO-8859-1).
- **Soporte de Esquemas Flexibles**: Versiones adaptativas que permiten trabajar con estructuras de datos dinámicas en RAF y XML.
- **Exportación/Importación**: Funcionalidad para exportar el listado de archivos o registros RAF a formato XML.

## 🇺🇸 Key Features
- **File Explorer**: Full navigation through the local file system, including creation and deletion of files and directories.
- **Text Editor**: Direct editing of plain text files from the browser.
- **SQL Emulator over RAF**: Perform `SELECT`, `UPDATE`, and `DELETE` operations on binary files with a fixed record structure (ID, Name, Age).
- **Advanced XML Management**:
    - Serialization and deserialization using **JAXB**.
    - Node manipulation with **DOM**.
    - SQL operation emulation over XML files.
- **Encoding Converter**: Tool to transform files between different encodings (UTF-8, ASCII, UTF-16, ISO-8859-1).
- **Flexible Schema Support**: Adaptive versions that allow working with dynamic data structures in both RAF and XML.
- **Export/Import**: Functionality to export file listings or RAF records to XML format.

---

## 🇪🇸 Tecnologías Utilizadas
- **Lenguaje**: Java 17+
- **Framework**: Spring Boot 3 (Spring MVC)
- **Motor de Plantillas**: Thymeleaf
- **Persistencia**: Java File I/O, RandomAccessFile, JAXB, DOM (XML)
- **Gestión de Dependencias**: Maven

## 🇺🇸 Technologies Used
- **Language**: Java 17+
- **Framework**: Spring Boot 3 (Spring MVC)
- **Template Engine**: Thymeleaf
- **Persistence**: Java File I/O, RandomAccessFile, JAXB, DOM (XML)
- **Dependency Management**: Maven

---

## 🇪🇸 Estructura del Proyecto
```text
src/main/java/com/example/fileinfo/
├── Aplicacion.java         # Punto de entrada de Spring Boot
├── controller/
│   └── FileController.java # Lógica de endpoints y control de flujo
├── model/
│   ├── FileInfo.java       # POJO para metadatos de archivos
│   ├── Schema.java         # Definición de estructuras dinámicas
│   └── ...
└── util/
    ├── RAFSqlEmulator.java # Lógica SQL para archivos binarios
    ├── XMLUtil.java        # Utilidades de JAXB y DOM
    ├── ...Flexible.java    # Versiones para esquemas dinámicos
```

## 🇺🇸 Project Structure
```text
src/main/java/com/example/fileinfo/
├── Aplicacion.java         # Spring Boot entry point
├── controller/
│   └── FileController.java # Endpoint logic and flow control
├── model/
│   ├── FileInfo.java       # POJO for file metadata
│   ├── Schema.java         # Definition for dynamic structures
│   └── ...
└── util/
    ├── RAFSqlEmulator.java # SQL logic for binary files
    ├── XMLUtil.java        # JAXB and DOM utilities
    ├── ...Flexible.java    # Versions for dynamic schemas
```

---

## 🇪🇸 Instalación y Ejecución
1. **Requisitos**: JDK 17 o superior y Maven instalado.
2. **Clonar/Descargar**: Descarga el código fuente.
3. **Compilar**: `mvn clean install`
4. **Ejecutar**: `mvn spring-boot:run`
5. **Acceso**: Abre un navegador y ve a `http://localhost:8080`

## 🇺🇸 Installation and Running
1. **Requirements**: JDK 17 or higher and Maven installed.
2. **Clone/Download**: Download the source code.
3. **Compile**: `mvn clean install`
4. **Run**: `mvn spring-boot:run`
5. **Access**: Open a browser and navigate to `http://localhost:8080`

---

> [!NOTE]
> Este proyecto fue desarrollado como parte de un estudio profundo sobre el manejo de flujos de datos (Streams) y persistencia aleatoria en Java.
