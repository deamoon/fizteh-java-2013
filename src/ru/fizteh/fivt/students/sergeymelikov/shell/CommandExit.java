package ru.fizteh.fivt.students.sergeymelikov.shell;

import java.io.IOException;

import ru.fizteh.fivt.students.sergeymelikov.utils.ExitRuntimeException;

public class CommandExit extends AbstractCommand {
    public CommandExit(StateInterface st) {
        super(0);
    }
    
    public String getName() {
        return "exit";
    }
    
    public void execute(String[] args) throws IOException {
        throw new ExitRuntimeException();
    }
}
