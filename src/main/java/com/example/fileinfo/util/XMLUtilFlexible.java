package com.example.fileinfo.util;

import com.example.fileinfo.model.Schema;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class XMLUtilFlexible {

    private final String filePath;
    private final Schema schema;
    private final List<String> fieldNames;
    private final Map<String, Integer> calculatedSizes = new LinkedHashMap<>();

    public XMLUtilFlexible(String filePath) throws IOException {
        this.filePath = filePath;
        
        File schemaFile = new File(filePath + ".schema.json");
        if (!schemaFile.exists()) {
            throw new IOException("Fichero de esquema no encontrado: " + schemaFile.getAbsolutePath());
        }
        ObjectMapper mapper = new ObjectMapper();
        this.schema = mapper.readValue(schemaFile, Schema.class);
        this.fieldNames = schema.getFields().stream().map(f -> f.getName().toLowerCase()).collect(Collectors.toList());

        this.fieldNames.forEach(name -> calculatedSizes.put(name, 0));
    }
    
    public List<String> getColumnNames() {
        return this.fieldNames;
    }
    
    public Map<String, Integer> getCalculatedSizes() {
        return this.calculatedSizes;
    }

    public List<Map<String, String>> readXMLAndCalculateSizes() throws Exception {
        List<Map<String, String>> records = new ArrayList<>();
        File xmlFile = new File(filePath);
        
        if (!xmlFile.exists() || xmlFile.length() == 0) {
            writeXML(new ArrayList<>()); // Asegura que el fichero exista con la raíz
            return records;
        }

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(xmlFile);
        doc.getDocumentElement().normalize();

        NodeList itemList = doc.getElementsByTagName(schema.getRecordRoot());

        for (int i = 0; i < itemList.getLength(); i++) {
            Node itemNode = itemList.item(i);
            if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
                Element itemElement = (Element) itemNode;
                Map<String, String> record = new HashMap<>();
                
                for (String field : fieldNames) {
                    NodeList fieldList = itemElement.getElementsByTagName(field);
                    String value = (fieldList.getLength() > 0) ? fieldList.item(0).getTextContent() : "";
                    record.put(field, value);

                    if (value.length() > calculatedSizes.get(field)) {
                        calculatedSizes.put(field, value.length());
                    }
                }
                records.add(record);
            }
        }
        return records;
    }
    
    public void writeXML(List<Map<String, String>> records) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.newDocument();

        Element rootElement = doc.createElement("root");
        doc.appendChild(rootElement);

        for (Map<String, String> record : records) {
            Element itemElement = doc.createElement(schema.getRecordRoot());
            rootElement.appendChild(itemElement);

            for (String field : fieldNames) {
                Element fieldElement = doc.createElement(field);
                fieldElement.setTextContent(record.getOrDefault(field, ""));
                itemElement.appendChild(fieldElement);
            }
        }

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(filePath));
        transformer.transform(source, result);
    }

    public void insert(Map<String, String> campos) throws Exception {
        List<Map<String, String>> records = readXMLAndCalculateSizes(); // Lee los datos actuales
        
        Map<String, String> newRecord = new HashMap<>();
        for (String field : fieldNames) {
            newRecord.put(field, campos.getOrDefault(field, ""));
        }
        
        records.add(newRecord);
        writeXML(records);
    }
    
    public void delete(int row) throws Exception {
        List<Map<String, String>> records = readXMLAndCalculateSizes();
        if (row < 1 || row > records.size()) {
            throw new IllegalArgumentException("Registro no válido para eliminar: " + row);
        }
        records.remove(row - 1);
        writeXML(records);
    }
}