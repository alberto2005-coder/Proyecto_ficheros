package com.example.fileinfo.util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Clase de utilidad para simular operaciones SQL (SELECT, UPDATE, DELETE) sobre
 * un fichero de Acceso Aleatorio (RandomAccessFile) con estructura de registro fija.
 * * ESTRUCTURA DE REGISTRO FIJA (TOTAL: 28 bytes):
 * 1. ID: int (4 bytes)
 * 2. NOMBRE: String de 10 caracteres (20 bytes)
 * 3. EDAD: int (4 bytes)
 */
public class RAFSqlEmulator {

    private final String filePath;
    private static final int ID_SIZE = 4; // int
    private static final int NOMBRE_CHAR_LENGTH = 10; 
    private static final int NOMBRE_BYTE_SIZE = NOMBRE_CHAR_LENGTH * 2; // Cada char es 2 bytes (writeChars)
    private static final int EDAD_SIZE = 4; // int
    private static final int RECORD_LENGTH = ID_SIZE + NOMBRE_BYTE_SIZE + EDAD_SIZE; // 28 bytes

    private static final List<String> COLUMN_NAMES = List.of("id", "nombre", "edad");

    public RAFSqlEmulator(String filePath) {
        this.filePath = filePath;
        // Opcional: Crear el archivo si no existe.
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                boolean created = file.createNewFile();
                if (created) {
                    System.out.println("Archivo creado: " + filePath);
                }
            } catch (IOException e) {
                System.err.println("No se pudo crear el archivo: " + e.getMessage());
            }
        }
    }

    // --- MÉTODOS DE UTILIDAD INTERNOS ---

    /**
     * Calcula la posición de inicio en bytes para un registro dado.
     * @param numRegistro El número de registro (>= 1).
     * @return La posición del puntero.
     */
    private long getRecordPosition(int numRegistro) {
        if (numRegistro <= 0) {
            throw new IllegalArgumentException("El número de registro debe ser mayor a 0.");
        }
        // Registros numerados desde 1.
        return (long) (numRegistro - 1) * RECORD_LENGTH;
    }

    /**
     * Escribe una String de tamaño fijo, rellenando o truncando. Usa writeChars.
     */
    private void writeFixedString(RandomAccessFile raf, String value, int charLength) throws IOException {
        StringBuilder sb = new StringBuilder(value);
        if (sb.length() > charLength) {
            sb.setLength(charLength); // Truncar
        } else {
            // Rellenar con espacios para asegurar la longitud fija
            while (sb.length() < charLength) {
                sb.append(' ');
            }
        }
        raf.writeChars(sb.toString()); 
    }
    
    /**
     * Lee una String de tamaño fijo y elimina espacios de relleno.
     */
    private String readFixedString(RandomAccessFile raf, int charLength) throws IOException {
        char[] chars = new char[charLength];
        for (int i = 0; i < charLength; i++) {
            chars[i] = raf.readChar();
        }
        return new String(chars).trim(); 
    }
    
    /**
     * Lee el valor de un campo específico para un registro. Útil para 'selectCampo'.
     */
    private String readField(int numRegistro, String nomColumna) throws IOException {
        long position = getRecordPosition(numRegistro);
        int colIndex = COLUMN_NAMES.indexOf(nomColumna.toLowerCase());
        if (colIndex == -1) {
            throw new IllegalArgumentException("Columna no válida: " + nomColumna);
        }

        try (RandomAccessFile raf = new RandomAccessFile(filePath, "r")) {
            // Si la posición de inicio del registro está fuera del archivo, no existe
            if (position >= raf.length()) return null; 
            
            raf.seek(position);

            // Si no hay suficientes bytes para un registro completo
            if (raf.getFilePointer() + RECORD_LENGTH > raf.length()) return null;

            // Ajustar el puntero dentro del registro antes de leer el campo.
            // Para poder usar raf.skipBytes, necesitamos posicionar el puntero al inicio
            // y luego avanzar hasta el campo deseado.
            
            switch (colIndex) {
                case 0: // id: int (4 bytes)
                    return String.valueOf(raf.readInt());
                case 1: // nombre: String (20 bytes)
                    raf.skipBytes(ID_SIZE); // Saltar ID
                    return readFixedString(raf, NOMBRE_CHAR_LENGTH);
                case 2: // edad: int (4 bytes)
                    raf.skipBytes(ID_SIZE + NOMBRE_BYTE_SIZE); // Saltar ID y NOMBRE
                    return String.valueOf(raf.readInt());
                default:
                    throw new IllegalStateException("Columna no gestionada: " + nomColumna);
            }
        }
    }

    // --- IMPLEMENTACIÓN DE MÉTODOS SOLICITADOS ---

    /** 1) Método string selectCampo( int numRegistro, string nomColumna) */
    public String selectCampo(int numRegistro, String nomColumna) {
        try {
            return readField(numRegistro, nomColumna);
        } catch (Exception e) {
            // Devolver un error en lugar de la excepción cruda
            return "ERROR al leer el campo: " + e.getMessage();
        }
    }

    /** 2) Método List selectColumna( string nomColumna ) */
    public List<String> selectColumna(String nomColumna) {
        List<String> columnValues = new ArrayList<>();
        int numRegistro = 1;
        String value;

        // Iterar hasta que readField devuelva null (registro vacío/parcial/fin de archivo)
        while (true) {
            try {
                value = readField(numRegistro, nomColumna);
                if (value == null) {
                    break; 
                }
                columnValues.add(value);
                numRegistro++;
            } catch (Exception e) {
                // Si hay un error de I/O a mitad, terminamos y reportamos.
                System.err.println("Error de I/O al leer la columna: " + e.getMessage());
                break;
            }
        }
        return columnValues;
    }

    /** 3) Método List selectRowList( in numRegistro ) */
    public List<String> selectRowList(int numRegistro) {
        List<String> rowData = new ArrayList<>();
        try (RandomAccessFile raf = new RandomAccessFile(filePath, "r")) {
            long position = getRecordPosition(numRegistro);
            raf.seek(position);

            if (raf.getFilePointer() + RECORD_LENGTH > raf.length()) {
                 return rowData; // Registro no existe/incompleto
            }

            // ID
            rowData.add(String.valueOf(raf.readInt()));
            // NOMBRE
            rowData.add(readFixedString(raf, NOMBRE_CHAR_LENGTH));
            // EDAD
            rowData.add(String.valueOf(raf.readInt()));

        } catch (Exception e) {
            System.err.println("ERROR de I/O al leer la fila: " + e.getMessage());
        }
        return rowData;
    }

    /** 4) Método Map selectRowMap( in numRegistro ) */
    public Map<String, String> selectRowMap(int numRegistro) {
        List<String> rowData = selectRowList(numRegistro);
        Map<String, String> rowMap = new HashMap<>();

        if (rowData.size() != COLUMN_NAMES.size()) {
            return rowMap; // Devuelve Map vacío si el registro no existe o está incompleto.
        }

        // Asocia los valores con los nombres de columna fijos
        for (int i = 0; i < COLUMN_NAMES.size(); i++) {
            rowMap.put(COLUMN_NAMES.get(i), rowData.get(i));
        }

        return rowMap;
    }

    /** 5.1) Método update( int row, Map ) */
    public void update(int row, Map<String, String> campos) throws IOException {
        long position = getRecordPosition(row);

        try (RandomAccessFile raf = new RandomAccessFile(filePath, "rw")) {
            
            // 1. Obtener los datos actuales para campos no modificados
            Map<String, String> currentData = selectRowMap(row);
            
            // Si el registro no existe (posición > longitud del archivo), lanzamos excepción para el usuario
            if (position > raf.length()) {
                throw new IOException("El registro " + row + " está fuera de los límites del archivo.");
            }

            // 2. Si el registro existe pero selectRowMap lo devolvió vacío (registro incompleto o borrado, pero existe el espacio), 
            // inicializar con valores por defecto para evitar NullPointer
            if (currentData.isEmpty() && position < raf.length()) {
                 currentData.put("id", "0");
                 currentData.put("nombre", "");
                 currentData.put("edad", "0");
            }
            
            // Si el registro está al final, es un nuevo registro que se va a insertar (aunque esto no es técnicamente un update)
            // Asegurarse de que al menos se inicialice un ID si no se proporciona.
            if(currentData.isEmpty()) {
                 // Inicializar con valores por defecto y permitir que el Map de entrada los sobrescriba
                 currentData.put("id", String.valueOf(row)); // Asignar un ID basado en la posición
                 currentData.put("nombre", "");
                 currentData.put("edad", "0");
            }

            // 3. Sobrescribir los datos actuales con los nuevos
            currentData.putAll(campos);

            // 4. Escribir el registro completo
            raf.seek(position);

            // ID
            raf.writeInt(Integer.parseInt(currentData.getOrDefault("id", "0")));
            // NOMBRE
            writeFixedString(raf, currentData.getOrDefault("nombre", ""), NOMBRE_CHAR_LENGTH);
            // EDAD
            raf.writeInt(Integer.parseInt(currentData.getOrDefault("edad", "0")));
        }
    }

    /** 5.2) Método update( int row, string campo, string valor ) */
    public void update(int row, String campo, String valor) throws IOException {
        Map<String, String> updateMap = new HashMap<>();
        updateMap.put(campo.toLowerCase(), valor);
        update(row, updateMap);
    }

    /** 6) Método delete( int row ) */
    public void delete(int row) throws IOException {
        long position = getRecordPosition(row);

        try (RandomAccessFile raf = new RandomAccessFile(filePath, "rw")) {
            // Comprobar si el registro existe
            if (position >= raf.length()) {
                 throw new IOException("El registro " + row + " no existe.");
            }
            
            raf.seek(position);

            // Escribir valores por defecto (borrado lógico: ID=0, NOMBRE="", EDAD=0)
            raf.writeInt(0); 
            writeFixedString(raf, "", NOMBRE_CHAR_LENGTH); 
            raf.writeInt(0); 
        }
    }
}