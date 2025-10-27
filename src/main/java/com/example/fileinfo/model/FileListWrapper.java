package com.example.fileinfo.model;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Clase contenedora (Wrapper) necesaria para serializar y deserializar
 * correctamente una List<FileInfo> como el elemento raíz del XML.
 */
@XmlRootElement(name = "ficheros") // Etiqueta raíz del documento XML (e.g., <ficheros>)
public class FileListWrapper {

    private List<FileInfo> ficheros;

    // Constructor sin argumentos: Obligatorio para JAXB
    public FileListWrapper() {
    }

    public FileListWrapper(List<FileInfo> ficheros) {
        this.ficheros = ficheros;
    }

    // Getter con @XmlElement: Indica la etiqueta para cada elemento de la lista.
    // 'file' coincide con el XmlRootElement de FileInfo.
    @XmlElement(name = "file") 
    public List<FileInfo> getFicheros() {
        return ficheros;
    }

    public void setFicheros(List<FileInfo> ficheros) {
        this.ficheros = ficheros;
    }
}