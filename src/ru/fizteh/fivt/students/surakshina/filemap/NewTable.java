package ru.fizteh.fivt.students.surakshina.filemap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import ru.fizteh.fivt.storage.structured.ColumnFormatException;
import ru.fizteh.fivt.storage.structured.Storeable;
import ru.fizteh.fivt.storage.structured.Table;

public class NewTable implements Table {
    private String name;
    private HashMap<String, ValueState<Storeable>> dataMap = new HashMap<>();
    private NewTableProvider provider = null;
    private ArrayList<Class<?>> types;

    public NewTable(String newName, NewTableProvider newProvider) throws IOException {
        name = newName;
        provider = newProvider;
        types = readSignature();
    }

    private ArrayList<Class<?>> readSignature() throws IOException {
        ArrayList<Class<?>> list = new ArrayList<Class<?>>();
        try {
            FileInputStream stream = new FileInputStream(new File(provider.getCurrentDirectory() + File.separator
                    + this.name + File.separator + "signature.tsv"));
            Scanner scanner = new Scanner(stream);
            int i = 0;
            while (scanner.hasNext()) {
                list.add(provider.getNameClass(scanner.next()));
                if (list.get(i) == null) {
                    scanner.close();
                    stream.close();
                    throw new IOException("Bad signature");
                }
                ++i;
            }
            scanner.close();
            stream.close();
        } catch (FileNotFoundException e) {
            throw new IOException(e.getMessage(), e);
        }
        if (list.isEmpty()) {
            throw new IOException("Signature is empty");
        }
        return list;
    }

    @Override
    public String getName() {
        return name;
    }

    private void checkName(String name) {
        if (name == null || name.trim().isEmpty() || name.contains(" ") || name.contains("\t")
                || name.contains(System.lineSeparator()) || name.contains("[") || name.contains("]")) {
            throw new IllegalArgumentException("wrong type (incorrect key)");
        }
    }

    public void loadCommitedValues(HashMap<String, Storeable> load) {
        for (String key : load.keySet()) {
            ValueState<Storeable> value = new ValueState<Storeable>(load.get(key), load.get(key));
            dataMap.put(key, value);
        }
    }

    public HashMap<String, String> returnMap() {
        HashMap<String, String> map = new HashMap<>();
        for (String key : dataMap.keySet()) {
            map.put(key, JSONSerializer.serialize(provider.getTable(name), dataMap.get(key).getCommitedValue()));
        }
        return map;
    }

    @Override
    public int size() {
        int count = 0;
        for (ValueState<Storeable> value : dataMap.values()) {
            if (value.getValue() != null) {
                ++count;
            }
        }
        return count;
    }

    public int unsavedChanges() {
        int count = 0;
        for (ValueState<Storeable> value : dataMap.values()) {
            if (value.needToCommit()) {
                ++count;
            }
        }
        return count;
    }

    @Override
    public int commit() throws IOException {
        int count = 0;
        for (ValueState<Storeable> value : dataMap.values()) {
            if (value.commitValue()) {
                ++count;
            }
        }
        if (count != 0) {
            try {
                if (provider.getCurrentTableFile() != null) {
                    provider.saveChanges(provider.getCurrentTableFile());
                }
            } catch (IOException e) {
                throw new IOException(e.getMessage(), e);
            }
        } else {
            return 0;
        }
        return count;
    }

    @Override
    public int rollback() {
        int count = 0;
        for (ValueState<Storeable> value : dataMap.values()) {
            if (value.rollbackValue()) {
                ++count;
            }
        }
        return count;
    }

    @Override
    public Storeable put(String key, Storeable value) throws ColumnFormatException {
        checkName(key);
        if (value == null) {
            throw new IllegalArgumentException("wrong type (value is null)");
        }
        checkStoreable(value);
        Storeable result;
        if (dataMap.containsKey(key)) {
            result = dataMap.get(key).getValue();
            dataMap.get(key).setValue(value);
        } else {
            dataMap.put(key, new ValueState<Storeable>(null, value));
            result = null;
        }
        return result;
    }

    private void checkStoreable(Storeable value) {
        int i = 0;
        try {
            for (i = 0; i < types.size(); ++i) {
                if (!value.getColumnAt(i).equals(types.get(i))) {
                    throw new ColumnFormatException("Storeable invalid");
                }
            }
        } catch (IndexOutOfBoundsException e) {
            throw new ColumnFormatException("Storeable invalid");
        }
        try {
            value.getColumnAt(i);
        } catch (IndexOutOfBoundsException e) {
            return;
        }
        throw new ColumnFormatException("Storeable invalid");

    }

    @Override
    public int getColumnsCount() {
        return types.size();
    }

    @Override
    public Class<?> getColumnType(int columnIndex) throws IndexOutOfBoundsException {
        checkIndex(columnIndex);
        return types.get(columnIndex);
    }

    private void checkIndex(int columnIndex) {
        if (columnIndex < 0 || columnIndex > types.size() - 1) {
            throw new IndexOutOfBoundsException("Incorrect column index");
        }
    }

    @Override
    public Storeable get(String key) {
        checkName(key);
        if (!dataMap.containsKey(key)) {
            return null;
        }
        return dataMap.get(key).getValue();
    }

    @Override
    public Storeable remove(String key) {
        checkName(key);
        if (dataMap.containsKey(key)) {
            Storeable oldVal = dataMap.get(key).getValue();
            dataMap.get(key).setValue(null);
            return oldVal;
        } else {
            return null;
        }
    }

}
