package com.example.fileinfo.model;

import java.io.File;
import java.util.Date;

public class FileInfo {
    private String name;
    private String path;
    private boolean exists;
    private boolean isFile;
    private boolean isDirectory;
    private boolean isHidden;
    private long length;
    private long lastModified;
    private String parent;
    private String absolutePath;
    private boolean canRead;
    private boolean canWrite;
    private boolean canExecute;
    private String[] list; // solo si es directorio

    public FileInfo(File file) {
        this.name = file.getName();
        this.path = file.getPath();
        this.exists = file.exists();
        this.isFile = file.isFile();
        this.isDirectory = file.isDirectory();
        this.isHidden = file.isHidden();
        this.length = file.length();
        this.lastModified = file.lastModified();
        this.parent = file.getParent();
        this.absolutePath = file.getAbsolutePath();
        this.canRead = file.canRead();
        this.canWrite = file.canWrite();
        this.canExecute = file.canExecute();
        if (file.isDirectory()) {
            this.list = file.list();
        }
    }

    // Getters
    public String getName() { return name; }
    public String getPath() { return path; }
    public boolean isExists() { return exists; }
    public boolean isFile() { return isFile; }
    public boolean isDirectory() { return isDirectory; }
    public boolean isHidden() { return isHidden; }
    public long getLength() { return length; }
    public Date getLastModifiedDate() { return new Date(lastModified); }
    public String getParent() { return parent; }
    public String getAbsolutePath() { return absolutePath; }
    public boolean isCanRead() { return canRead; }
    public boolean isCanWrite() { return canWrite; }
    public boolean isCanExecute() { return canExecute; }
    public String[] getList() { return list; }
}
