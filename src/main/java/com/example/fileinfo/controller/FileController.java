package com.example.fileinfo.controller;

import com.example.fileinfo.model.FileInfo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile; // Importación clave para acceso aleatorio
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class FileController {

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("mensaje", "Introduce una ruta válida para explorar.");
        return "index";
    }

    @PostMapping("/info")
    public String getFileInfo(@RequestParam("ruta") String ruta, Model model) {
        File file = new File(ruta);

        if (!file.exists()) {
            model.addAttribute("mensajeError", "❌ La ruta no existe: " + ruta);
            return "info";
        }

        if (file.isFile()) {
            model.addAttribute("mensajeError", "📄 La ruta es un archivo, no un directorio.");
            return "info";
        }

        File[] archivos = file.listFiles();
        List<FileInfo> elementos = archivos != null
                ? Arrays.stream(archivos).map(FileInfo::new).collect(Collectors.toList())
                : null;

        model.addAttribute("rutaActual", file.getAbsolutePath());
        model.addAttribute("rutaPadre", file.getParent());
        model.addAttribute("elementos", elementos);

        return "info";
    }

    @PostMapping("/eliminar")
    public String eliminarArchivo(@RequestParam("rutaCompleta") String rutaCompleta, Model model) {
        File file = new File(rutaCompleta);
        String parent = file.getParent();

        if (!file.exists()) {
            model.addAttribute("mensajeError", "❌ El archivo no existe: " + rutaCompleta);
        } else if (file.delete()) {
            model.addAttribute("mensajeExito", "✅ Archivo eliminado correctamente: " + rutaCompleta);
        } else {
            model.addAttribute("mensajeError", "❌ No se pudo eliminar el archivo: " + rutaCompleta);
        }

        // Recargar el directorio padre
        File dir = new File(parent);
        File[] archivos = dir.listFiles();
        model.addAttribute("rutaActual", parent);
        model.addAttribute("rutaPadre", dir.getParent());
        model.addAttribute("elementos", archivos != null
                ? Arrays.stream(archivos).map(FileInfo::new).collect(Collectors.toList())
                : null);

        return "info";
    }

    @PostMapping("/crearArchivo")
    public String crearArchivo(@RequestParam("ruta") String ruta,
                               @RequestParam("nombreArchivo") String nombreArchivo,
                               Model model) {
        File nuevoArchivo = new File(ruta, nombreArchivo);

        if (nuevoArchivo.exists()) {
            model.addAttribute("mensajeError", "⚠️ El archivo ya existe: " + nuevoArchivo.getAbsolutePath());
        } else {
            try {
                if (nuevoArchivo.createNewFile()) {
                    model.addAttribute("mensajeExito", "✅ Archivo creado correctamente: " + nuevoArchivo.getName());
                } else {
                    model.addAttribute("mensajeError", "❌ No se pudo crear el archivo.");
                }
            } catch (IOException e) {
                model.addAttribute("mensajeError", "❌ Error al crear el archivo: " + e.getMessage());
            }
        }

        // Recargar información del directorio actual
        File dir = new File(ruta);
        File[] archivos = dir.listFiles();
        model.addAttribute("rutaActual", ruta);
        model.addAttribute("rutaPadre", dir.getParent());
        model.addAttribute("elementos", archivos != null
                ? Arrays.stream(archivos).map(FileInfo::new).collect(Collectors.toList())
                : null);

        return "info";
    }
    
    @GetMapping("/editar")
    public String mostrarEditor(@RequestParam("rutaCompleta") String rutaCompleta, Model model) {
        File file = new File(rutaCompleta);

        if (!file.exists() || file.isDirectory()) {
            model.addAttribute("mensajeError", "❌ No se puede editar la ruta: " + rutaCompleta);
            return "info"; 
        }

        try {
            // Leer el contenido del archivo
            String contenido = new String(java.nio.file.Files.readAllBytes(file.toPath()));
            
            // CORRECCIÓN: Usar getParent() para obtener la ruta del directorio padre de forma segura.
            String parentPath = file.getParent();
            if (parentPath == null) {
                parentPath = "/"; 
            }

            model.addAttribute("rutaCompleta", rutaCompleta);
            model.addAttribute("nombreArchivo", file.getName());
            model.addAttribute("contenido", contenido);
            model.addAttribute("rutaDirectorioPadre", parentPath); 
            
            return "editar";
        } catch (IOException e) {
            model.addAttribute("mensajeError", "❌ Error al leer el archivo: " + e.getMessage());
            // Recargar el directorio padre antes de volver a info
            String parent = file.getParent();
            File dir = new File(parent != null ? parent : "/");
            File[] archivos = dir.listFiles();
            model.addAttribute("rutaActual", dir.getAbsolutePath());
            model.addAttribute("rutaPadre", dir.getParent());
            model.addAttribute("elementos", archivos != null
                    ? Arrays.stream(archivos).map(FileInfo::new).collect(Collectors.toList())
                    : null);
            return "info";
        }
    }

    @PostMapping("/guardarEdicion")
    public String guardarEdicion(@RequestParam("rutaCompleta") String rutaCompleta,
                               @RequestParam("contenido") String contenido,
                               Model model) {
        File file = new File(rutaCompleta);
        String parent = file.getParent();

        try {
            java.nio.file.Files.write(file.toPath(), contenido.getBytes());
            model.addAttribute("mensajeExito", "✅ Archivo guardado correctamente: " + file.getName());
        } catch (IOException e) {
            model.addAttribute("mensajeError", "❌ Error al guardar el archivo: " + e.getMessage());
        }

        // Recargar el directorio padre (la vista info)
        File dir = new File(parent);
        File[] archivos = dir.listFiles();
        model.addAttribute("rutaActual", parent);
        model.addAttribute("rutaPadre", dir.getParent());
        model.addAttribute("elementos", archivos != null
                ? Arrays.stream(archivos).map(FileInfo::new).collect(Collectors.toList())
                : null);

        return "info";
    }

    // --- NUEVOS MÉTODOS PARA ACCESO ALEATORIO ---

    @PostMapping("/crearRandomAccessFile")
    public String crearRandomAccessFile(@RequestParam("ruta") String ruta,
                                       @RequestParam("nombreArchivo") String nombreArchivo,
                                       Model model) {
        File nuevoArchivo = new File(ruta, nombreArchivo);

        if (nuevoArchivo.exists()) {
            model.addAttribute("mensajeError", "⚠️ El archivo ya existe: " + nuevoArchivo.getAbsolutePath());
        } else {
            // Intentamos crear el archivo utilizando RandomAccessFile en modo "rw" (lectura/escritura)
            try (RandomAccessFile raf = new RandomAccessFile(nuevoArchivo, "rw")) {
                model.addAttribute("mensajeExito", "✅ Archivo de Acceso Aleatorio creado correctamente: " + nuevoArchivo.getName());
            } catch (IOException e) {
                model.addAttribute("mensajeError", "❌ Error al crear el archivo de Acceso Aleatorio: " + e.getMessage());
            }
        }

        // Recargar información del directorio actual
        File dir = new File(ruta);
        File[] archivos = dir.listFiles();
        model.addAttribute("rutaActual", ruta);
        model.addAttribute("rutaPadre", dir.getParent());
        model.addAttribute("elementos", archivos != null
                ? Arrays.stream(archivos).map(FileInfo::new).collect(Collectors.toList())
                : null);

        return "info";
    }


    @PostMapping("/editarAleatorio")
    public String editarAleatorio(@RequestParam("rutaCompleta") String rutaCompleta,
                                  @RequestParam("posicion") long posicion,
                                  @RequestParam("contenido") String contenido,
                                  Model model) {
        File file = new File(rutaCompleta);
        String parent = file.getParent();

        if (file.isDirectory()) {
            model.addAttribute("mensajeError", "❌ No se puede editar un directorio de forma aleatoria.");
        } else {
            try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
                // 1. Mover el puntero a la posición deseada
                raf.seek(posicion);

                // 2. Obtener los bytes del contenido a escribir
                byte[] data = contenido.getBytes("UTF-8"); 

                // 3. Escribir los bytes
                raf.write(data);

                model.addAttribute("mensajeExito", "✅ Datos escritos correctamente en la posición " + posicion + " en el archivo: " + file.getName());
            } catch (IOException e) {
                model.addAttribute("mensajeError", "❌ Error al escribir en el archivo: " + e.getMessage());
            }
        }

        // Recargar el directorio padre (la vista info)
        File dir = new File(parent);
        File[] archivos = dir.listFiles();
        model.addAttribute("rutaActual", parent);
        model.addAttribute("rutaPadre", dir.getParent());
        model.addAttribute("elementos", archivos != null
                ? Arrays.stream(archivos).map(FileInfo::new).collect(Collectors.toList())
                : null);

        return "info";
    }
    @PostMapping("/convertir")
public String convertirArchivo(
        @RequestParam("entradaPath") String entradaPath,
        @RequestParam("encodingEntrada") String encodingEntrada,
        @RequestParam("salidaPath") String salidaPath,
        @RequestParam("encodingSalida") String encodingSalida,
        Model model) {

    StringBuilder mensaje = new StringBuilder();

    try (BufferedReader br = new BufferedReader(
            new java.io.InputStreamReader(new java.io.FileInputStream(entradaPath), encodingEntrada));
         BufferedWriter bw = new BufferedWriter(
            new java.io.OutputStreamWriter(new java.io.FileOutputStream(salidaPath), encodingSalida))) {

        String linea;
        while ((linea = br.readLine()) != null) {
            bw.write(linea);
            bw.newLine();
        }

        mensaje.append("✅ Conversión completada correctamente.<br>");
        mensaje.append("Fichero de salida: ").append(salidaPath).append("<br>");
        mensaje.append("Encoding de entrada: ").append(encodingEntrada).append("<br>");
        mensaje.append("Encoding de salida: ").append(encodingSalida);

    } catch (java.io.FileNotFoundException e) {
        mensaje.append("❌ Error: Fichero no encontrado - ").append(e.getMessage());
    } catch (java.io.IOException e) {
        mensaje.append("❌ Error de lectura/escritura - ").append(e.getMessage());
    } catch (Exception e) {
        mensaje.append("❌ Error general - ").append(e.getMessage());
    }

    model.addAttribute("mensaje", mensaje.toString());
    return "info";
}

}