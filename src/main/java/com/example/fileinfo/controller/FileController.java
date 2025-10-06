package com.example.fileinfo.controller;

import com.example.fileinfo.model.FileInfo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
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
            
            // 💡 CORRECCIÓN: Usar getParent() para obtener la ruta del directorio padre de forma segura.
            String parentPath = file.getParent();
            if (parentPath == null) {
                // Si getParent() es null (estamos en la raíz), usamos la ruta de vuelta '/' o la ruta completa
                // para que el formulario de retorno funcione y la vista 'info' maneje la raíz.
                parentPath = "/"; 
            }

            model.addAttribute("rutaCompleta", rutaCompleta);
            model.addAttribute("nombreArchivo", file.getName());
            model.addAttribute("contenido", contenido);
            model.addAttribute("rutaDirectorioPadre", parentPath); // Nuevo atributo para usar en la plantilla
            
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
}