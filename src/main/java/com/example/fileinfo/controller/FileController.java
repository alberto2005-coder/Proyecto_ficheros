package com.example.fileinfo.controller;

import com.example.fileinfo.model.FileInfo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Controller
public class FileController {

    // 1. Página de inicio (recibe el Model para los mensajes flash)
    @GetMapping("/")
    public String index(Model model) {
        // El 'mensaje' flash attribute se mueve automáticamente al Model.
        return "index";
    }

    // 2. Exploración de directorios (lista el contenido)
    @PostMapping("/info")
    public String getFileInfo(@RequestParam("ruta") String ruta, Model model) {
        File file = new File(ruta);

        if (!file.exists() || !file.isDirectory()) {
            model.addAttribute("mensajeError", "La ruta especificada no existe o no es un directorio: " + ruta);
            return "info";
        }
        
        File[] files = file.listFiles();
        List<FileInfo> elementos = new ArrayList<>();
        if (files != null) {
            for (File f : files) {
                elementos.add(new FileInfo(f));
            }
        }
        
        // CORRECCIÓN: Calcula la ruta del directorio padre en Java
        String rutaPadre = file.getParent();
        // Si el padre es nulo (ej: estamos en la raíz de un disco), usamos "/" o la propia ruta
        if (rutaPadre == null) {
            // Usamos la propia ruta, ya que no podemos ir "más arriba"
            rutaPadre = ruta; 
        }
            
        model.addAttribute("rutaActual", ruta);
        model.addAttribute("elementos", elementos);
        // Añadimos la ruta padre al modelo
        model.addAttribute("rutaPadre", rutaPadre); 
        
        return "info";
    }

    // 3. Eliminación de archivo/directorio
    @PostMapping("/eliminar")
    public String eliminarArchivo(@RequestParam("rutaCompleta") String rutaCompleta,
                                RedirectAttributes redirectAttributes) {

        File fileToDelete = new File(rutaCompleta);
        String mensaje;

        if (fileToDelete.exists()) {
            try {
                // Intenta eliminar el archivo/directorio.
                Files.delete(fileToDelete.toPath()); 
                
                if (fileToDelete.isDirectory()) {
                    mensaje = "Directorio '" + fileToDelete.getName() + "' eliminado correctamente.";
                } else {
                    mensaje = "Fichero '" + fileToDelete.getName() + "' eliminado correctamente.";
                }

            } catch (IOException e) {
                mensaje = "ERROR: No se pudo eliminar '" + fileToDelete.getName() + "'. Motivo: " + e.getMessage();
            }
        } else {
            mensaje = "ERROR: El elemento a eliminar no existe: " + rutaCompleta;
        }

        redirectAttributes.addFlashAttribute("mensaje", mensaje);
        return "redirect:/";
    }
    
    // 4. Tu método de creación de estructura (sin cambios)
    @PostMapping("/crearEstructura")
    public String crearEstructura(RedirectAttributes redirectAttributes) {
        String[] abuelos = {"Abuelo", "Abuela"};
        String[] padres = {"Padre", "Madre"};
        String[][] hijos = {
            {"Hijo1.txt", "Hija2.txt"}, {"Hijo3.txt", "Hija4.txt"},
            {"Hijo5.txt", "Hijo6.txt"}, {"Hijo7.txt", "Hija8.txt"}
        };

        int indexHijo = 0;
        StringBuilder resultado = new StringBuilder("Estructura creada/actualizada:<br>");

        for (String abuelo : abuelos) {
            for (String padre : padres) {
                File carpeta = new File("estructura" + File.separator + abuelo + File.separator + padre);
                carpeta.mkdirs();

                for (int j = 0; j < 2; j++) {
                    File archivo = new File(carpeta, hijos[indexHijo][j]);
                    try {
                        if (archivo.createNewFile()) {
                            resultado.append("Archivo creado: ").append(archivo.getPath()).append("<br>");
                        } else {
                            resultado.append("Ya existía: ").append(archivo.getPath()).append("<br>");
                        }
                    } catch (IOException e) {
                         resultado.append("Error al crear: ").append(archivo.getPath()).append("<br>");
                    }
                }
                indexHijo++;
            }
        }

        redirectAttributes.addFlashAttribute("mensaje", resultado.toString());
        return "redirect:/"; 
    }
}