package ru.fizteh.fivt.students.krivchansky.multifilemap;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import ru.fizteh.fivt.students.krivchansky.shell.*;
import ru.fizteh.fivt.students.krivchansky.filemap.*;
import ru.fizteh.fivt.students.krivchansky.filemap.ExitCommand;


public class MultifileMapMain {
	public static void main(String args[]) {
		MultiFileMapShellState state = new MultiFileMapShellState();
		HashSet<Commands<FileMapShellState>> com =  new HashSet<Commands<FileMapShellState>>() {{ add(new ExitCommand()); add(new RollbackCommand()); add(new CommitCommand()); 
        add(new PutCommand()); add(new GetCommand()); add(new RemoveKeyCommand());}};
        HashSet<Commands<MultiFileMapShellState>> com1 =  new HashSet<Commands<MultiFileMapShellState>>() {{add(new DropCommand()); add(new UseCommand()); 
         add(new CreateCommand());}};
        ArrayList<Commands> res = new ArrayList<Commands>();
        res.addAll(com);
        res.addAll(com1);
        HashSet<Commands> actualResult = new HashSet<Commands>(res);
        Shell<MultiFileMapShellState> shell = new Shell<MultiFileMapShellState>(actualResult);
        String dbDirectory = System.getProperty("fizteh.db.dir");
        if (dbDirectory != null && new File(dbDirectory).isDirectory()) {
         	DatabaseFactory factory = new DatabaseFactory();
            state.tableProvider = factory.create(dbDirectory);
            shell.setShellState(state);
            shell.run(args, shell);
            System.exit(0);
        }
        File db = new File (dbDirectory, "fizteh");
        if (!db.isDirectory()) {
        	System.out.println("Error occured. No path to database.");
        	System.exit(-1);
        	/*File oneMore = new File(db.getAbsolutePath(), "db");
        	oneMore.mkdir();
        	File andLast = new File(oneMore.getAbsolutePath(), "dir");
        	andLast.mkdir();
        	dbDirectory = andLast.getAbsolutePath();*/
        } else {
        	File temp = new File(db.getAbsolutePath(), "db");
        	if (!temp.isDirectory()) {
        		System.err.println("Error occured. Wrong path to database.");
            	System.exit(-2);
        	}
        	File andLast = new File(temp.getAbsolutePath(), "dir");
        	if (!andLast.isDirectory()) {
        		System.err.println("Error occured. Bad path to database.");
            	System.exit(-3);
        	}
        	dbDirectory = andLast.getAbsolutePath();
        }
        DatabaseFactory factory = new DatabaseFactory();
        state.tableProvider = factory.create(dbDirectory);
        shell.setShellState(state);
        shell.run(args, shell);
	    }
}
