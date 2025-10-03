package com.example.fileinfo.controller;

import com.example.fileinfo.model.FileInfo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;

@Controller
public class FileController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/info")
    public String getFileInfo(@RequestParam("ruta") String ruta, Model model) {
        File file = new File(ruta);
        FileInfo info = new FileInfo(file);

        model.addAttribute("fileInfo", info);
        return "info";
    }
    @PostMapping("/crearEstructura")
public String crearEstructura(Model model) {
    String[] abuelos = {"Abuelo", "Abuela"};
    String[] padres = {"Padre", "Madre"};
    String[][] hijos = {
        {"Hijo1.txt", "Hija2.txt"},
        {"Hijo3.txt", "Hija4.txt"},
        {"Hijo5.txt", "Hijo6.txt"},
        {"Hijo7.txt", "Hija8.txt"}
    };

    int indexHijo = 0;
    StringBuilder resultado = new StringBuilder();

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

    model.addAttribute("mensaje", resultado.toString());
    return "resultado";
}

@PostMapping("/eliminar")
public String eliminarArchivo(@RequestParam("nombre") String nombre, Model model) {
    File raiz = new File("estructura");
    boolean eliminado = buscarYEliminar(raiz, nombre);

    if (eliminado) {
        model.addAttribute("mensaje", "Fichero '" + nombre + "' eliminado correctamente.");
    } else {
        model.addAttribute("mensaje", "No se encontró el fichero '" + nombre + "'.");
    }
    return "resultado";
}
private boolean buscarYEliminar(File directorio, String nombreFichero) {
        File[] archivos = directorio.listFiles();
        if (archivos != null) {
            for (File f : archivos) {
                if (f.isDirectory()) {
                    if (buscarYEliminar(f, nombreFichero)) {
                        return true;
                    }
                } else {
                    if (f.getName().equals(nombreFichero)) {
                        return f.delete();
                    }
                }
            }
        }
        return false;
    }

}
