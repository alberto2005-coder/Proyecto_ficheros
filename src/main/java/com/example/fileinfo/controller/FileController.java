package com.example.fileinfo.controller;

import com.example.fileinfo.model.FileInfo;
import com.example.fileinfo.util.RAFSqlEmulator;
import com.example.fileinfo.util.XMLUtil; 

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.xml.bind.JAXBException;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class FileController {

    private static final List<String> ALLOWED_ENCODINGS = Arrays.asList("ASCII", "UTF-8", "UTF-16", "ISO-8859-1");

    // =========================================================================
    // MÉTODOS AUXILIARES
    // =========================================================================
    
    private List<FileInfo> getFileInfoList(String ruta) {
        File file = new File(ruta);
        File[] archivos = file.listFiles();
        return archivos != null
                ? Arrays.stream(archivos).map(FileInfo::new).collect(Collectors.toList())
                : null;
    }

    private String reloadInfo(String ruta, Model model, String mensajeExito, String mensajeError) {
        File dir = new File(ruta);
        model.addAttribute("rutaActual", ruta);
        model.addAttribute("rutaPadre", dir.getParent());
        model.addAttribute("elementos", getFileInfoList(ruta));
        if (mensajeExito != null) model.addAttribute("mensajeExito", mensajeExito);
        if (mensajeError != null) model.addAttribute("mensajeError", mensajeError);
        return "info";
    }

    // =========================================================================
    // ENDPOINTS BÁSICOS Y CRUD DE FICHEROS
    // =========================================================================

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("mensaje", "Introduce una ruta válida para explorar.");
        return "index";
    }

    @PostMapping("/info")
    public String getFileInfo(@RequestParam String ruta, Model model) {
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
    public String eliminarArchivo(@RequestParam String rutaCompleta, Model model) {
        File file = new File(rutaCompleta);
        String parent = file.getParent();
        String mensajeExito = null;
        String mensajeError = null;

        if (!file.exists()) {
            mensajeError = "❌ El archivo no existe: " + rutaCompleta;
        } else if (file.delete()) {
            mensajeExito = "✅ Archivo eliminado correctamente: " + rutaCompleta;
        } else {
            mensajeError = "❌ No se pudo eliminar el archivo: " + rutaCompleta;
        }

        return reloadInfo(parent, model, mensajeExito, mensajeError);
    }

    @PostMapping("/crearArchivo")
    public String crearArchivo(@RequestParam String ruta,
            @RequestParam String nombreArchivo,
            Model model) {
        File nuevoArchivo = new File(ruta, nombreArchivo);
        String mensajeExito = null;
        String mensajeError = null;

        if (nuevoArchivo.exists()) {
            mensajeError = "⚠️ El archivo ya existe: " + nuevoArchivo.getAbsolutePath();
        } else {
            try {
                if (nuevoArchivo.createNewFile()) {
                    mensajeExito = "✅ Archivo creado correctamente: " + nuevoArchivo.getName();
                } else {
                    mensajeError = "❌ No se pudo crear el archivo.";
                }
            } catch (IOException e) {
                mensajeError = "❌ Error al crear el archivo: " + e.getMessage();
            }
        }

        return reloadInfo(ruta, model, mensajeExito, mensajeError);
    }

    // --- NUEVO MÉTODO: Crear Archivo XML ---
    @PostMapping("/crearArchivoXML")
    public String crearArchivoXML(@RequestParam String ruta,
            @RequestParam String nombreArchivo,
            Model model) {
        
        File nuevoArchivo = new File(ruta, nombreArchivo);
        String mensajeExito = null;
        String mensajeError = null;

        if (nuevoArchivo.exists()) {
            mensajeError = "⚠️ El archivo XML ya existe: " + nuevoArchivo.getAbsolutePath();
        } else {
            // Intentamos inicializar el XML con el XMLUtil, lo que crea el archivo con la estructura base
            try {
                // Se necesita un constructor dummy de XMLUtil o asegurar que el constructor
                // que usa la ruta hace el trabajo de inicialización. Asumiendo la lógica
                // de XMLUtil, esto debería funcionar:
                XMLUtil xmlUtil = new XMLUtil(nuevoArchivo.getAbsolutePath());
                // El constructor de XMLUtil asegura la creación del archivo vacío con la estructura <catalogo>
                mensajeExito = "✅ Archivo XML (Catálogo) creado correctamente: " + nuevoArchivo.getName();
            } catch (Exception e) {
                mensajeError = "❌ Error al crear el archivo XML: " + e.getMessage();
            }
        }

        return reloadInfo(ruta, model, mensajeExito, mensajeError);
    }
    
    // ... (Métodos para editar, guardarEdicion, RandomAccessFile y convertir quedan igual)
    @GetMapping("/editar")
    public String mostrarEditor(@RequestParam String rutaCompleta, Model model) {
        // Lógica de mostrar editor de texto plano...
        File file = new File(rutaCompleta);

        if (!file.exists() || file.isDirectory()) {
            model.addAttribute("mensajeError", "❌ No se puede editar la ruta: " + rutaCompleta);
            return "info";
        }

        try {
            String contenido = new String(java.nio.file.Files.readAllBytes(file.toPath()));
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
            String parent = file.getParent();
            File dir = new File(parent != null ? parent : "/");
            return getFileInfo(dir.getAbsolutePath(), model);
        }
    }

    @PostMapping("/guardarEdicion")
    public String guardarEdicion(@RequestParam String rutaCompleta,
            @RequestParam String contenido,
            Model model) {
        File file = new File(rutaCompleta);
        String parent = file.getParent();
        String mensajeExito = null;
        String mensajeError = null;

        try {
            java.nio.file.Files.write(file.toPath(), contenido.getBytes());
            mensajeExito = "✅ Archivo guardado correctamente: " + file.getName();
        } catch (IOException e) {
            mensajeError = "❌ Error al guardar el archivo: " + e.getMessage();
        }

        return reloadInfo(parent, model, mensajeExito, mensajeError);
    }

    @PostMapping("/crearRandomAccessFile")
    public String crearRandomAccessFile(@RequestParam String ruta,
            @RequestParam String nombreArchivo,
            Model model) {
        File nuevoArchivo = new File(ruta, nombreArchivo);
        String mensajeExito = null;
        String mensajeError = null;

        if (nuevoArchivo.exists()) {
            mensajeError = "⚠️ El archivo ya existe: " + nuevoArchivo.getAbsolutePath();
        } else {
            try (RandomAccessFile raf = new RandomAccessFile(nuevoArchivo, "rw")) {
                mensajeExito = "✅ Archivo de Acceso Aleatorio creado correctamente: " + nuevoArchivo.getName();
            } catch (IOException e) {
                mensajeError = "❌ Error al crear el archivo de Acceso Aleatorio: " + e.getMessage();
            }
        }

        return reloadInfo(ruta, model, mensajeExito, mensajeError);
    }

    @PostMapping("/editarAleatorio")
    public String editarAleatorio(@RequestParam String rutaCompleta,
            @RequestParam long posicion,
            @RequestParam String contenido,
            Model model) {
        File file = new File(rutaCompleta);
        String parent = file.getParent();
        String mensajeExito = null;
        String mensajeError = null;

        if (file.isDirectory()) {
            mensajeError = "❌ No se puede editar un directorio de forma aleatoria.";
        } else {
            try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
                raf.seek(posicion);
                byte[] data = contenido.getBytes("UTF-8");
                raf.write(data);

                mensajeExito = "✅ Datos escritos correctamente en la posición " + posicion
                        + " en el archivo: " + file.getName();
            } catch (IOException e) {
                mensajeError = "❌ Error al escribir en el archivo: " + e.getMessage();
            }
        }

        return reloadInfo(parent, model, mensajeExito, mensajeError);
    }

    @PostMapping("/convertir")
    public String convertirFichero(@RequestParam String rutaDirectorioPadre,
            @RequestParam String inputPath,
            @RequestParam String inputEncoding,
            @RequestParam String outputPath,
            @RequestParam String outputEncoding,
            Model model) {

        File inputFile = new File(inputPath);
        File outputFile = new File(outputPath);

        String inputEnc = inputEncoding.toUpperCase();
        String outputEnc = outputEncoding.toUpperCase();

        String mensajeExito = null;
        String mensajeError = null;

        if (!inputFile.exists() || inputFile.isDirectory()) {
            mensajeError = "❌ La ruta de entrada no es un fichero válido: " + inputPath;
        } else if (!ALLOWED_ENCODINGS.contains(inputEnc) || !ALLOWED_ENCODINGS.contains(outputEnc)) {
            mensajeError = "❌ Una o ambas codificaciones no son válidas. Soportadas: " + ALLOWED_ENCODINGS;
        } else {
            try (
                    FileInputStream fis = new FileInputStream(inputFile);
                    InputStreamReader isr = new InputStreamReader(fis, Charset.forName(inputEnc));
                    BufferedReader reader = new BufferedReader(isr);

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
                mensajeExito = "✅ Conversión completada: '" + inputFile.getName() + "' (" + inputEnc + ") -> '" +
                        outputFile.getName() + "' (" + outputEnc + "). Líneas: " + lines;

            } catch (FileNotFoundException e) {
                mensajeError = "❌ Archivo no encontrado o no accesible: " + e.getMessage();
            } catch (UnsupportedCharsetException e) {
                mensajeError = "❌ Codificación no soportada por el sistema: " + e.getCharsetName();
            } catch (IOException e) {
                mensajeError = "❌ Error de I/O durante la conversión: " + e.getMessage();
            } catch (Exception e) {
                mensajeError = "❌ Error inesperado: " + e.getMessage();
            }
        }

        return reloadInfo(rutaDirectorioPadre, model, mensajeExito, mensajeError);
    }
    
    // ... (gestionRA y ejecutarRAFSql quedan igual)
    @GetMapping("/gestionRA")
    public String gestionRandomAccessFile(@RequestParam String rutaCompleta, Model model) {
        try {
            RAFSqlEmulator emulator = new RAFSqlEmulator(rutaCompleta);
            
            List<Map<String, String>> registros = new ArrayList<>();
            int numRegistro = 1;
            Map<String, String> row;

            do {
                row = emulator.selectRowMap(numRegistro);
                if (!row.isEmpty()) {
                    registros.add(row);
                    numRegistro++;
                } else {
                    break;
                }
            } while (true);
            
            model.addAttribute("rutaCompleta", rutaCompleta);
            model.addAttribute("registros", registros);
            model.addAttribute("nombreArchivo", new File(rutaCompleta).getName());
            
            String parentPath = new File(rutaCompleta).getParent();
            model.addAttribute("rutaPadre", parentPath != null ? parentPath : "/"); 
            
        } catch (Exception e) {
            model.addAttribute("mensajeError", "❌ Error al cargar la gestión del RAF: " + e.getMessage());
            String parentPath = new File(rutaCompleta).getParent();
            if (parentPath != null) {
                return getFileInfo(parentPath, model); 
            }
        }
        
        return "gestionRA";
    }

    @PostMapping("/ejecutarRAFSql")
    public String ejecutarRAFSql(
            @RequestParam String rutaCompleta,
            @RequestParam String accion,
            @RequestParam(required = false) Integer numRegistro,
            @RequestParam(required = false) String campo,
            @RequestParam(required = false) String valor,
            Model model) {

        try {
            RAFSqlEmulator emulator = new RAFSqlEmulator(rutaCompleta);
            String mensajeExito = "";
            String campoLower = campo != null ? campo.toLowerCase() : null;

            if ("delete".equals(accion) && numRegistro != null) {
                emulator.delete(numRegistro); 
                mensajeExito = "✅ Registro " + numRegistro + " eliminado (marcado con ID=0).";

            } else if ("update".equals(accion) && numRegistro != null && campo != null && valor != null) {
                if (("id".equals(campoLower) || "edad".equals(campoLower)) && !valor.matches("^-?\\d+$")) {
                    throw new IllegalArgumentException("El campo '" + campo + "' debe ser un número entero.");
                }
                emulator.update(numRegistro, campoLower, valor); 
                mensajeExito = "✅ Registro " + numRegistro + ", campo '" + campo + "' actualizado a '" + valor + "'.";

            } else if ("selectCampo".equals(accion) && numRegistro != null && campo != null) {
                String resultado = emulator.selectCampo(numRegistro, campoLower); 
                if (resultado == null || "ERROR".equals(resultado)) {
                    model.addAttribute("mensajeError", "Registro/campo no encontrado o índice inválido.");
                } else {
                    model.addAttribute("selectCampoResultado", "Resultado de SELECT CAMPO (" + campo + " en registro "
                            + numRegistro + "): **" + resultado + "**");
                }

            } else {
                model.addAttribute("mensajeError", "❌ Acción no válida o parámetros incompletos.");
            }

            if (!mensajeExito.isEmpty()) {
                model.addAttribute("mensajeExito", mensajeExito);
            }

        } catch (Exception e) {
            model.addAttribute("mensajeError", "❌ Error al ejecutar la acción RAF: " + e.getMessage());
        }

        return gestionRandomAccessFile(rutaCompleta, model);
    }
    
    // =========================================================================
    // ENDPOINTS PARA GESTIÓN DE XML CON ADAPTADORES
    // =========================================================================

    /**
     * Muestra la página de gestión XML (gestionXML.html), 
     * cargando la lista de registros del archivo XML.
     * @param rutaCompleta La ruta al archivo XML (no al directorio).
     */
    @GetMapping("/gestionXML")
    public String gestionXML(@RequestParam String rutaCompleta, Model model) {
        try {
            // Usar rutaCompleta aquí, que es el path al archivo XML
            XMLUtil xmlUtil = new XMLUtil(rutaCompleta); 
            
            // Cargar todos los registros del XML
            List<Map<String, String>> registros = xmlUtil.readXML(); 
            
            model.addAttribute("rutaCompleta", rutaCompleta);
            model.addAttribute("registros", registros);
            model.addAttribute("nombreArchivo", new File(rutaCompleta).getName());
            
            // Calcular y añadir la ruta del padre al modelo
            String parentPath = new File(rutaCompleta).getParent();
            model.addAttribute("rutaPadre", parentPath != null ? parentPath : "/"); 
            
        } catch (Exception e) {
            model.addAttribute("mensajeError", "❌ Error al cargar la gestión del XML: " + e.getMessage());
            // Si hay un error, redirigir al directorio padre
            String parentPath = new File(rutaCompleta).getParent();
            if (parentPath != null) {
                return reloadInfo(parentPath, model, null, "Error al cargar la gestión XML.");
            }
        }
        
        return "gestionXML"; // Nombre de la plantilla
    }
    
    /**
     * Maneja las acciones de INSERT, DELETE, UPDATE y SELECT CAMPO enviadas desde gestionXML.html
     */
    @PostMapping("/ejecutarXMLSql")
    public String ejecutarXMLSql(
            @RequestParam String rutaCompleta,
            @RequestParam String accion,
            @RequestParam Map<String, String> params, // Captura todos los parámetros del formulario
            Model model) {

        try {
            XMLUtil xmlUtil = new XMLUtil(rutaCompleta);
            String mensajeExito = "";
            
            switch (accion.toLowerCase()) {
                case "insert":
                    xmlUtil.insert(params); 
                    mensajeExito = "✅ Nuevo registro añadido correctamente al XML.";
                    break;
                    
                case "delete":
                    int deleteRow = Integer.parseInt(params.get("numRegistro"));
                    xmlUtil.delete(deleteRow); 
                    mensajeExito = "✅ Registro " + deleteRow + " eliminado permanentemente del XML.";
                    break;

                case "update":
                    int updateRow = Integer.parseInt(params.get("numRegistro"));
                    String campo = params.get("campo");
                    String valor = params.get("valor");
                    
                    if (("id".equalsIgnoreCase(campo) || "edad".equalsIgnoreCase(campo)) && !valor.matches("^-?\\d+$")) {
                        throw new IllegalArgumentException("El campo '" + campo + "' debe ser un número entero.");
                    }
                    
                    xmlUtil.update(updateRow, campo, valor);
                    mensajeExito = "✅ Registro " + updateRow + ", campo '" + campo + "' actualizado a '" + valor + "'.";
                    break;

                case "selectcampo":
                    int selectRow = Integer.parseInt(params.get("numRegistro"));
                    String selectCampo = params.get("campo");
                    String resultado = xmlUtil.selectCampo(selectRow, selectCampo); 
                    
                    if (resultado == null) {
                         model.addAttribute("mensajeError", "Registro/campo no encontrado o índice inválido.");
                    } else {
                        model.addAttribute("selectCampoResultado", "Resultado de SELECT CAMPO (" + selectCampo + " en registro "
                                + selectRow + "): **" + resultado + "**");
                    }
                    mensajeExito = "Consulta ejecutada.";
                    break;

                default:
                    model.addAttribute("mensajeError", "❌ Acción SQL no válida: " + accion);
                    break;
            }

            if (!mensajeExito.isEmpty()) {
                model.addAttribute("mensajeExito", mensajeExito);
            }

        } catch (NumberFormatException e) {
            model.addAttribute("mensajeError", "❌ Error de formato: Asegúrate de que los números de registro, ID y Edad son válidos.");
        } catch (IllegalArgumentException e) {
            model.addAttribute("mensajeError", "❌ Error de validación: " + e.getMessage());
        } catch (Exception e) {
            model.addAttribute("mensajeError", "❌ Error al ejecutar la acción XML: " + e.getMessage());
        }

        return gestionXML(rutaCompleta, model);
    }
    
    // ... (exportarXML e importarXML quedan igual)
    @GetMapping("/exportarXML")
    public ResponseEntity<byte[]> exportarXML(@RequestParam String rutaDirectorio) {
        try {
            List<FileInfo> elementos = getFileInfoList(rutaDirectorio);
            
            String xmlString = XMLUtil.dat2xml(elementos);

            byte[] xmlBytes = xmlString.getBytes("UTF-8");
            String dirName = new File(rutaDirectorio).getName().isEmpty() ? "raiz" : new File(rutaDirectorio).getName();
            String fileName = "ficheros_" + dirName.replaceAll("[^a-zA-Z0-9.-]", "_") + ".xml";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_XML);
            headers.setContentLength(xmlBytes.length);
            headers.setContentDispositionFormData("attachment", fileName);
            
            return new ResponseEntity<>(xmlBytes, headers, HttpStatus.OK);

        } catch (JAXBException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error de JAXB al serializar: " + e.getMessage()).getBytes());
        } catch (IOException e) {
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error de I/O: " + e.getMessage()).getBytes());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(("Error al exportar: " + e.getMessage()).getBytes());
        }
    }

    @PostMapping("/importarXML")
    public String importarXML(@RequestParam("xmlFile") MultipartFile file, Model model, RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensajeError", "❌ Por favor, selecciona un archivo XML para importar.");
            return "redirect:/";
        }
        
        try (InputStream inputStream = file.getInputStream()) {
            
            String xmlString = new BufferedReader(new InputStreamReader(inputStream))
                                     .lines().collect(Collectors.joining("\n"));
            
            List<FileInfo> ficherosCargados = XMLUtil.xml2dat(xmlString);
            
            model.addAttribute("rutaActual", "IMPORTACIÓN XML: " + file.getOriginalFilename());
            model.addAttribute("rutaPadre", null);
            model.addAttribute("elementos", ficherosCargados);
            model.addAttribute("mensajeExito", "✅ Importación XML completada. Mostrando " + ficherosCargados.size() + " registros.");
            
            return "info";

        } catch (JAXBException e) {
            model.addAttribute("mensajeError", "❌ Error al procesar el XML (JAXB): Asegúrate de que el formato es correcto. " + e.getMessage());
        } catch (IOException e) {
            model.addAttribute("mensajeError", "❌ Error de I/O al leer el archivo: " + e.getMessage());
        } catch (Exception e) {
            model.addAttribute("mensajeError", "❌ Error inesperado durante la importación: " + e.getMessage());
        }
        
        return "index";
    }
}