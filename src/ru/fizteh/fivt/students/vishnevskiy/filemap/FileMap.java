package ru.fizteh.fivt.students.vishnevskiy.filemap;

import ru.fizteh.fivt.students.vishnevskiy.shell.Shell;
import ru.fizteh.fivt.students.vishnevskiy.shell.Command;
import ru.fizteh.fivt.students.vishnevskiy.filemap.commands.*;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;

public class FileMap {

    private List<Command> commands() {
        List<Command> list = new ArrayList<Command>();
        Command get = new Get();
        list.add(get);
        Command put = new Put();
        list.add(put);
        Command remove = new Remove();
        list.add(remove);
        Command exit = new Exit();
        list.add(exit);
        return list;
    }

    public void run(String[] args) {
        try {
            String dirString = System.getProperty("fizteh.db.dir");
            if (dirString == null) {
                throw new IOException("Path to datebase directory expected");
            }
            File dir = new File(dirString);
            if (!dir.isDirectory()) {
                throw new IOException("Wrong path to datebase directory");
            }
            File datebase = new File(dirString, "db.dat");
            Shell shell = new Shell(commands(), new SingleFileMap(datebase));
            shell.run(args);
       } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

}
