package ru.fizteh.fivt.students.lizaignatyeva.database;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.zip.DataFormatException;

public class Table {
    final File path;
    private final int base = 16;
    public String name;
    private HashMap<String, String> data = new HashMap<String, String>();
    private HashMap<String, String> backup = new HashMap<String, String>();
    private boolean saved = true;

    @SuppressWarnings("unchecked")
    public Table(Path globalDirectory, String tableName) {
        name = tableName;
        path = globalDirectory.resolve(tableName).toFile();
        try {
            FileUtils.mkDir(path.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("Can't open table " + tableName + ": " + e.getMessage());
        }
        try {
            data = new HashMap<String, String>();
            readTable();
            backup = (HashMap<String, String>) data.clone();
            System.err.println("created successfully");
        } catch (IOException e) {
            System.err.println("Error creating table: " + e.getMessage());
            System.exit(1);
        } catch (DataFormatException e) {
            System.err.println("Error creating table: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Error creating table: " + e.getMessage());
            System.exit(1);
        }
    }

    public Table(File directory) throws IllegalArgumentException {
        path = directory;
        name = path.getName();
        try {
            readTable();
        } catch (Exception e) {
            throw new IllegalArgumentException("Directory contains invalid subdirectories/files: " + e.getMessage());
        }
    }

    public String getName() {
        return name;
    }

    public int getSize() throws Exception {
        if (data == null) {
            return 0;
        }
       return data.size();
    }

    public void delete() throws Exception {
        FileUtils.remove(path);
    }

    private int getDirNumber(String key) {
        int number = key.getBytes()[0];
        number = Math.abs(number);
        return number % 16;
    }


    private int getFileNumber(String key) {
        int number = key.getBytes()[0];
        number = Math.abs(number);
        return number / 16 % 16;
    }

    private String getDirName(String key) {
        return String.format("%d.dir", getDirNumber(key));
    }

    private String getFileName(String key) {
        return String.format("%d.dat", getFileNumber(key));
    }

    private boolean isValid(String key, String dirName, String fileName) {
        return (getDirName(key).equals(dirName) && getFileName(key).equals(fileName));
    }

    private void readEntry(ByteBuffer buffer, String dirName, String fileName) throws BufferUnderflowException,
            DataFormatException {
        int keyLength = buffer.getInt();
        if (keyLength > buffer.remaining() || keyLength < 0) {
            throw new DataFormatException("too long key buffer");
        }
        int valueLength = buffer.getInt();
        if (valueLength > buffer.remaining() || valueLength < 0) {
            throw new DataFormatException("too long value buffer");
        }
        byte[] keyBytes = new byte[keyLength];
        buffer.get(keyBytes);
        byte[] valueBytes = new byte[valueLength];
        buffer.get(valueBytes);
        String key = new String(keyBytes, StandardCharsets.UTF_8);
        if (!isValid(key, dirName, fileName)) {
            throw new DataFormatException("entry in a wrong file, key: " + key + ", file: "
                    + fileName + ", expected file: " + getFileName(key) + ", directory: " + dirName
                    + ", expected directory: " + getDirName(key));
        }
        String value = new String(valueBytes, StandardCharsets.UTF_8);
        if (data.containsKey(key)) {
            throw new DataFormatException("duplicating keys: " + key);
        }
        data.put(key, value);
    }

    private boolean isValidDirectoryName(String name) {
        for (int i = 0; i < base; ++i) {
            if (name.equals(Integer.toString(i) + ".dir")) {
                return true;
            }
        }
        return false;
    }

    private boolean isValidFileName(String name) {
        for (int i = 0; i < base; ++i) {
            if (name.equals(Integer.toString(i) + ".dat")) {
                return true;
            }
        }
        return false;
    }

    public void readTable() throws Exception {
        File[] subDirs = path.listFiles();
        if (subDirs == null) {
            return;
        }
        for (File dir : subDirs) {
            if (!dir.isDirectory() || !isValidDirectoryName(dir.getName())) {
                throw new DataFormatException("Table '" + name + "' contains strange file(s): '" + dir.getName() + "'");
            }
            readData(dir);
        }
    }

    public void readData(File directory) throws Exception {
        File[] children = directory.listFiles();
        if (children == null) {
            return;
        }
        for (File file : children) {
            if (!isValidFileName(file.getName())) {
                throw new DataFormatException("Table '" + name + "' contains strange file(s): '"
                        + file.getName() + "'");
            }
            readFromFile(file.getCanonicalPath(), directory.getName(), file.getName());
        }

    }


    public void readFromFile(String filePath, String dirName, String fileName) throws Exception {
        byte[] data = Files.readAllBytes(Paths.get(filePath));
        ByteBuffer buffer = ByteBuffer.wrap(data);
        while (buffer.hasRemaining()) {
            readEntry(buffer, dirName, fileName);
        }
    }

    private byte[] intToBytes(int value) {
        return ByteBuffer.allocate(4).putInt(value).array();
    }

    private void writeEntry(String key, String value, BufferedOutputStream outputStream) throws IOException {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] valueBytes = value.getBytes(StandardCharsets.UTF_8);
        outputStream.write(intToBytes(keyBytes.length));
        outputStream.write(intToBytes(valueBytes.length));
        outputStream.write(keyBytes);
        outputStream.write(valueBytes);
    }

    public void writeToFile() throws IOException {
        try {
            this.delete();
        } catch (Exception e) {
            System.err.println("Error while updating database files: " + e.getMessage());
            System.exit(1);
        }
        FileUtils.mkDir(path.getAbsolutePath());
        for (String key: data.keySet()) {
            String value = data.get(key);
            File directory = FileUtils.mkDir(path.getAbsolutePath()
                                        + File.separator + getDirName(key));
            File file = FileUtils.mkFile(directory, getFileName(key));
            try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file.getCanonicalPath(), true))) {
                writeEntry(key, value, outputStream);
            }
        }
    }

    public void write() {
        //it's a nice debugging tool i'd like to keep here
        System.out.println("we are off now: " + name);
        for (String str : data.keySet()) {
            System.out.println(str + " " + data.get(str));
        }

    }

    public String get(String key) throws Exception {
        return data.get(key);
    }

    public void put(String key, String value) throws Exception {
        data.put(key, value);
    }

    public boolean containsKey(String key) throws Exception {
        return data.containsKey(key);
    }

    public void remove(String key) throws Exception {
        data.remove(key);
    }

    //JUnit

    @SuppressWarnings("unchecked")
    public int commit() throws Exception {
        int diff = getChangesAmount();
        backup = (HashMap<String, String>) data.clone();
        saved = true;
        return diff;
    }

    @SuppressWarnings("unchecked")
    public int rollback() throws Exception {
        int diff = getChangesAmount();
        data = (HashMap<String, String>) backup.clone();
        saved = true;
        return diff;
    }

    public int getChangesAmount() throws Exception {
        //new key
        //removed key
        //new value with old key
        int diff = 0;
        //System.err.println("yep");
        for (String currentKey : data.keySet()) {
            if (backup.get(currentKey) == null) {
               // System.err.println("case 1");

                diff++;
            } else {
                if (!backup.get(currentKey).equals(data.get(currentKey))) {
                   // System.err.println("case 2");
                    diff++;
                }
            }
        }
        for (String oldKey : backup.keySet()) {
            if (data.get(oldKey) == null) {
               // System.err.println("case 3");
                diff++;
            }
        }
        return diff;
    }

    public boolean isSaved() {
        return saved;
    }

    public void wasSaved() {
        saved = true;
    }

    public void wasChanged() {
        saved = false;
    }

}
