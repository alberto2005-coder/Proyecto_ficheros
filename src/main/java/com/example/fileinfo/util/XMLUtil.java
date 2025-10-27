package com.example.fileinfo.util;

import com.example.fileinfo.model.FileInfo;
import com.example.fileinfo.model.FileListWrapper; // Importación de la clase auxiliar
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Clase de utilidad para simular operaciones SQL (SELECT, UPDATE, DELETE, INSERT) sobre
 * un fichero XML con estructura <catalogo> <item> <campo>.
 * * CAMPOS: <id>, <nombre>, <edad>
 * * NOTA: Esta clase también contiene los ADAPTADORES JAXB para FileInfo.
 */
public class XMLUtil {

    private static final String ROOT_TAG = "catalogo";
    private static final String ITEM_TAG = "item";
    private static final List<String> FIELD_NAMES = List.of("id", "nombre", "edad");
    private final String filePath;

    public XMLUtil(String filePath) {
        this.filePath = filePath;
        // Asegura que el archivo exista y tenga la estructura base si está vacío
        File file = new File(filePath);
        if (!file.exists() || file.length() == 0) {
            try {
                writeXML(new ArrayList<>()); // Escribe un catálogo vacío
            } catch (Exception e) {
                System.err.println("Error creando el archivo XML base: " + e.getMessage());
            }
        }
    }

    /** Lee todo el contenido del XML y lo devuelve como una lista de mapas. */
    public List<Map<String, String>> readXML() throws Exception {
        List<Map<String, String>> records = new ArrayList<>();
        File xmlFile = new File(filePath);
        
        if (!xmlFile.exists() || xmlFile.length() == 0) {
            return records;
        }

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(xmlFile);
        doc.getDocumentElement().normalize();

        NodeList itemList = doc.getElementsByTagName(ITEM_TAG);

        for (int i = 0; i < itemList.getLength(); i++) {
            Node itemNode = itemList.item(i);

            if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
                Element itemElement = (Element) itemNode;
                Map<String, String> record = new HashMap<>();
                
                for (String field : FIELD_NAMES) {
                    NodeList fieldList = itemElement.getElementsByTagName(field);
                    if (fieldList.getLength() > 0) {
                        record.put(field, fieldList.item(0).getTextContent());
                    } else {
                        record.put(field, ""); 
                    }
                }
                records.add(record);
            }
        }
        return records;
    }

    /** Escribe una lista de mapas al archivo XML. */
    public void writeXML(List<Map<String, String>> records) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.newDocument();

        // Elemento raíz: <catalogo>
        Element rootElement = doc.createElement(ROOT_TAG);
        doc.appendChild(rootElement);

        for (Map<String, String> record : records) {
            // Elemento de registro: <item>
            Element itemElement = doc.createElement(ITEM_TAG);
            rootElement.appendChild(itemElement);

            // Campos: <campo>VALUE</campo>
            for (String field : FIELD_NAMES) {
                Element fieldElement = doc.createElement(field);
                String value = record.getOrDefault(field, "");
                fieldElement.setTextContent(value);
                itemElement.appendChild(fieldElement);
            }
        }

        // Configuración y ejecución del Transformer para escribir al archivo
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(filePath));
        
        transformer.transform(source, result);
    }
    
    // --- MÉTODOS SIMULANDO SQL ---
    
    /** 1) Método string selectCampo(int numRegistro, string nomColumna) */
    public String selectCampo(int numRegistro, String nomColumna) throws Exception {
        List<Map<String, String>> records = readXML();
        if (numRegistro < 1 || numRegistro > records.size()) {
            return null;
        }
        Map<String, String> record = records.get(numRegistro - 1);
        return record.getOrDefault(nomColumna.toLowerCase(), null);
    }
    
    /** 2) Método List selectColumna(string nomColumna) */
    public List<String> selectColumna(String nomColumna) throws Exception {
        List<Map<String, String>> records = readXML();
        return records.stream()
                .map(record -> record.getOrDefault(nomColumna.toLowerCase(), ""))
                .collect(Collectors.toList());
    }

    /** 3) Método Map selectRowMap(int numRegistro) */
    public Map<String, String> selectRowMap(int numRegistro) throws Exception {
        List<Map<String, String>> records = readXML();
        if (numRegistro < 1 || numRegistro > records.size()) {
            return new HashMap<>();
        }
        return records.get(numRegistro - 1);
    }

    /** 4) Método update(int row, Map) y 5) update(int row, string campo, string valor) */
    public void update(int row, String campo, String valor) throws Exception {
        List<Map<String, String>> records = readXML();
        if (row < 1 || row > records.size()) {
            throw new IllegalArgumentException("Registro no válido para actualizar: " + row);
        }

        Map<String, String> targetRecord = records.get(row - 1);
        String field = campo.toLowerCase();
        
        if (FIELD_NAMES.contains(field)) {
            targetRecord.put(field, valor);
            writeXML(records);
        } else {
            throw new IllegalArgumentException("El campo '" + campo + "' no existe en el esquema XML.");
        }
    }

    /** 6) Método delete(int row) - Borrado Físico en XML */
    public void delete(int row) throws Exception {
        List<Map<String, String>> records = readXML();
        if (row < 1 || row > records.size()) {
            throw new IllegalArgumentException("Registro no válido para eliminar: " + row);
        }

        // Borrado físico (eliminar de la lista)
        records.remove(row - 1);
        
        writeXML(records);
    }
    
    /** 7) Método insert(Map) - Nuevo para añadir filas al XML */
    public void insert(Map<String, String> campos) throws Exception {
        List<Map<String, String>> records = readXML();
        
        Map<String, String> newRecord = new HashMap<>();
        // 1. Establecer campos por defecto
        for (String field : FIELD_NAMES) {
            newRecord.put(field, "");
        }
        
        // 2. Aplicar los campos proporcionados
        for (Map.Entry<String, String> entry : campos.entrySet()) {
            String field = entry.getKey().toLowerCase();
            if (FIELD_NAMES.contains(field)) {
                newRecord.put(field, entry.getValue());
            }
        }
        
        // 3. Generar ID automático si no se proporciona (usando el máximo + 1)
        if (!newRecord.containsKey("id") || newRecord.get("id").isEmpty()) {
            int nextId = records.stream()
                .mapToInt(r -> {
                    try {
                        return Integer.parseInt(r.getOrDefault("id", "0"));
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .max()
                .orElse(0) + 1;
            newRecord.put("id", String.valueOf(nextId));
        }

        records.add(newRecord);
        writeXML(records);
    }
    
    // =========================================================================
    // ADAPTADORES dat2xml() y xml2dat() para FileInfo (con JAXB)
    // =========================================================================

    /**
     * FUNCIÓN ADAPTADORA: dat2xml()
     * Convierte una Lista de FileInfo (dat) a una cadena XML (String) usando JAXB.
     */
    public static String dat2xml(List<FileInfo> dataList) throws JAXBException {
        // 1. Crear el objeto contenedor (wrapper)
        FileListWrapper wrapper = new FileListWrapper(dataList);

        // 2. Inicializar el contexto JAXB con la clase raíz del XML
        JAXBContext jaxbContext = JAXBContext.newInstance(FileListWrapper.class, FileInfo.class);
        
        // 3. Crear el Marshaller (serializador)
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        
        // Configuración de la salida (formato bonito)
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        
        // 4. Escribir el objeto en un String
        StringWriter sw = new StringWriter();
        jaxbMarshaller.marshal(wrapper, sw);

        return sw.toString();
    }

    /**
     * FUNCIÓN ADAPTADORA: xml2dat()
     * Convierte una cadena XML (String) a una Lista de FileInfo (dat) usando JAXB.
     */
    public static List<FileInfo> xml2dat(String xmlString) throws JAXBException {
        // 1. Inicializar el contexto JAXB con la clase raíz del XML
        JAXBContext jaxbContext = JAXBContext.newInstance(FileListWrapper.class, FileInfo.class);
        
        // 2. Crear el Unmarshaller (deserializador)
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        
        // 3. Leer el XML desde el String
        StringReader reader = new StringReader(xmlString);
        
        // 4. Convertir el XML a objeto Java (FileListWrapper)
        FileListWrapper wrapper = (FileListWrapper) jaxbUnmarshaller.unmarshal(reader);
        
        return wrapper.getFicheros();
    }
}