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
}
