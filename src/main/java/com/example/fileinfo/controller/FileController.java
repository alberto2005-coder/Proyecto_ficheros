package com.example.fileinfo.controller;

import com.example.fileinfo.model.FileInfo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class FileController {

    private static final List<String> ALLOWED_ENCODINGS = Arrays.asList("ASCII", "UTF-8", "UTF-16", "ISO-8859-1");

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

            // CORRECCIÓN: Usar getParent() para obtener la ruta del directorio padre de
            // forma segura.
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
            // Intentamos crear el archivo utilizando RandomAccessFile en modo "rw"
            // (lectura/escritura)
            try (RandomAccessFile raf = new RandomAccessFile(nuevoArchivo, "rw")) {
                model.addAttribute("mensajeExito",
                        "✅ Archivo de Acceso Aleatorio creado correctamente: " + nuevoArchivo.getName());
            } catch (IOException e) {
                model.addAttribute("mensajeError",
                        "❌ Error al crear el archivo de Acceso Aleatorio: " + e.getMessage());
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

                model.addAttribute("mensajeExito", "✅ Datos escritos correctamente en la posición " + posicion
                        + " en el archivo: " + file.getName());
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

    @PostMapping("/crearEstructura")
    public String crearEstructura(Model model) {

        final String BASE_DIR_NAME = "estructura";
        File baseDir = new File(BASE_DIR_NAME);

        String[] abuelos = { "Abuelo", "Abuela" };
        String[] padres = { "Padre", "Madre" };

        String mensaje;
        int archivosCreados = 0;

        try {

            if (!baseDir.mkdir()) {
                throw new IOException("No se pudo crear el directorio base: " + baseDir.getAbsolutePath());
            }

            int contadorHijos = 1;
            for (String abuelo : abuelos) {
                for (String padre : padres) {

                    File dirPadre = new File(baseDir, abuelo + File.separator + padre);
                    if (!dirPadre.mkdirs()) {
                        throw new IOException("No se pudo crear el directorio: " + dirPadre.getAbsolutePath());
                    }

                    File hijo = new File(dirPadre, "Hijo" + contadorHijos + ".txt");
                    if (hijo.createNewFile()) {
                        archivosCreados++;
                    }

                    File hija = new File(dirPadre, "Hija" + (contadorHijos + 1) + ".txt");
                    if (hija.createNewFile()) {
                        archivosCreados++;
                    }

                    contadorHijos += 2;
                }
            }

            mensaje = "✅ Estructura genealógica creada con éxito en **" + baseDir.getAbsolutePath() + "**."
                    + " (" + archivosCreados + " archivos y 8 directorios creados).";

        } catch (IOException e) {
            mensaje = "❌ Error al crear la estructura genealógica: " + e.getMessage();
        }

        model.addAttribute("mensaje", mensaje);
        return "index";
    }

    @PostMapping("/convertir")
    public String convertirFichero(@RequestParam("rutaDirectorioPadre") String rutaDirectorioPadre,
            @RequestParam("inputPath") String inputPath,
            @RequestParam("inputEncoding") String inputEncoding,
            @RequestParam("outputPath") String outputPath,
            @RequestParam("outputEncoding") String outputEncoding,
            Model model) {

        File inputFile = new File(inputPath);
        File outputFile = new File(outputPath);

        String inputEnc = inputEncoding.toUpperCase();
        String outputEnc = outputEncoding.toUpperCase();

        // 1. Validar existencia del archivo de entrada y codificaciones
        if (!inputFile.exists() || inputFile.isDirectory()) {
            model.addAttribute("mensajeError", "❌ La ruta de entrada no es un fichero válido: " + inputPath);
        } else if (!ALLOWED_ENCODINGS.contains(inputEnc) || !ALLOWED_ENCODINGS.contains(outputEnc)) {
            model.addAttribute("mensajeError",
                    "❌ Una o ambas codificaciones no son válidas. Soportadas: " + ALLOWED_ENCODINGS);
        } else {
            // 2. Ejecutar la conversión
            try (
                    // Lector: Lee bytes y los decodifica usando inputEncoding
                    FileInputStream fis = new FileInputStream(inputFile);
                    InputStreamReader isr = new InputStreamReader(fis, Charset.forName(inputEnc));
                    BufferedReader reader = new BufferedReader(isr);

                    // Escritor: Codifica caracteres usando outputEncoding y escribe los bytes
                    FileOutputStream fos = new FileOutputStream(outputFile);
                    OutputStreamWriter osw = new OutputStreamWriter(fos, Charset.forName(outputEnc));
                    BufferedWriter writer = new BufferedWriter(osw)) {
                String line;
                long lines = 0;
                while ((line = reader.readLine()) != null) {
                    writer.write(line);
                    writer.newLine();
                    lines++;
                }

                writer.flush();
                model.addAttribute("mensajeExito",
                        "✅ Conversión completada: '" + inputFile.getName() + "' (" + inputEnc + ") -> '" +
                                outputFile.getName() + "' (" + outputEnc + "). Líneas: " + lines);

            } catch (FileNotFoundException e) {
                // Captura si el fichero de entrada no existe, o si no se puede crear el de
                // salida
                model.addAttribute("mensajeError", "❌ Archivo no encontrado o no accesible: " + e.getMessage());
            } catch (UnsupportedCharsetException e) {
                // Aunque se validan antes, esta excepción captura fallos de Charset.forName
                model.addAttribute("mensajeError", "❌ Codificación no soportada por el sistema: " + e.getCharsetName());
            } catch (IOException e) {
                // Captura errores de lectura/escritura
                model.addAttribute("mensajeError", "❌ Error de I/O durante la conversión: " + e.getMessage());
            } catch (Exception e) {
                // Captura cualquier otra excepción
                model.addAttribute("mensajeError", "❌ Error inesperado: " + e.getMessage());
            }
        }

        // 3. Recargar la vista info del directorio actual o padre
        File dir = new File(rutaDirectorioPadre);
        File[] archivos = dir.listFiles();
        model.addAttribute("rutaActual", rutaDirectorioPadre);
        model.addAttribute("rutaPadre", dir.getParent());
        model.addAttribute("elementos", archivos != null
                ? Arrays.stream(archivos).map(FileInfo::new).collect(Collectors.toList())
                : null);

        return "info";
    }
}