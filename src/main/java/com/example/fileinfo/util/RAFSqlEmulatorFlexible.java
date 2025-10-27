package com.example.fileinfo.util;

import com.example.fileinfo.model.FieldDefinition;
import com.example.fileinfo.model.Schema;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RAFSqlEmulatorFlexible {

    private final String filePath;
    private final Schema schema;
    private final int recordLength;
    private final List<String> columnNames;

    public RAFSqlEmulatorFlexible(String filePath) throws IOException {
        this.filePath = filePath;

        File schemaFile = new File(filePath + ".schema.json");
        if (!schemaFile.exists()) {
            throw new IOException("Fichero de esquema no encontrado: " + schemaFile.getAbsolutePath());
        }
        ObjectMapper mapper = new ObjectMapper();
        this.schema = mapper.readValue(schemaFile, Schema.class);

        this.recordLength = schema.getFields().stream().mapToInt(FieldDefinition::getSize).sum();
        this.columnNames = schema.getFields().stream().map(FieldDefinition::getName).collect(Collectors.toList());
        
        File dataFile = new File(filePath);
        if (!dataFile.exists()) {
            dataFile.createNewFile();
        }
    }

    public List<String> getColumnNames() {
        return this.columnNames;
    }

    public Map<String, String> selectRowMap(int numRegistro) {
        Map<String, String> rowMap = new LinkedHashMap<>();
        if (numRegistro <= 0) return rowMap;
        long position = (long) (numRegistro - 1) * recordLength;

        try (RandomAccessFile raf = new RandomAccessFile(filePath, "r")) {
            if (position >= raf.length()) return rowMap;
            raf.seek(position);

            for (FieldDefinition field : schema.getFields()) {
                rowMap.put(field.getName(), readFieldFromRaf(raf, field));
            }
        } catch (IOException e) {
            System.err.println("Error al leer la fila " + numRegistro + ": " + e.getMessage());
        }
        return rowMap;
    }

    public void insert(Map<String, String> campos) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(filePath, "rw")) {
            raf.seek(raf.length());
            for (FieldDefinition field : schema.getFields()) {
                writeFieldToRaf(raf, field, campos.getOrDefault(field.getName(), getDefaultValue(field.getType())));
            }
        }
    }
    
    public void delete(int row) throws IOException {
        long position = (long) (row - 1) * recordLength;
        try (RandomAccessFile raf = new RandomAccessFile(filePath, "rw")) {
            if (position >= raf.length()) throw new IOException("El registro " + row + " no existe.");
            raf.seek(position);
            for (FieldDefinition field : schema.getFields()) {
                writeFieldToRaf(raf, field, getDefaultValue(field.getType()));
            }
        }
    }

    private String readFieldFromRaf(RandomAccessFile raf, FieldDefinition field) throws IOException {
        switch (field.getType().toLowerCase()) {
            case "int": return String.valueOf(raf.readInt());
            case "string":
                byte[] buffer = new byte[field.getSize()];
                raf.readFully(buffer);
                return new String(buffer, StandardCharsets.UTF_8).trim();
            case "double": return String.valueOf(raf.readDouble());
            case "boolean": return String.valueOf(raf.readBoolean());
            default: throw new IOException("Tipo de dato no soportado: " + field.getType());
        }
    }

    private void writeFieldToRaf(RandomAccessFile raf, FieldDefinition field, String value) throws IOException {
        switch (field.getType().toLowerCase()) {
            case "int": raf.writeInt(Integer.parseInt(value)); break;
            case "string":
                byte[] buffer = new byte[field.getSize()];
                byte[] valueBytes = value.getBytes(StandardCharsets.UTF_8);
                System.arraycopy(valueBytes, 0, buffer, 0, Math.min(valueBytes.length, buffer.length));
                raf.write(buffer);
                break;
            case "double": raf.writeDouble(Double.parseDouble(value)); break;
            case "boolean": raf.writeBoolean(Boolean.parseBoolean(value)); break;
            default: throw new IOException("Tipo de dato no soportado: " + field.getType());
        }
    }
    
    private String getDefaultValue(String type) {
        switch (type.toLowerCase()) {
            case "int": case "double": return "0";
            case "boolean": return "false";
            case "string": default: return "";
        }
    }
}