package ru.fizteh.fivt.students.valentinbarishev.filemap;

public class ClassState {
    static final int CLOSED = 1;
    static final int OPEND = 0;

    private int state = OPEND;
    private Object parent = null;

    public ClassState(Object newParent, int newState) {
        state = newState;
        parent = newParent;
    }

    public ClassState(Object newParent) {
        parent = newParent;
    }

    public ClassState() {
    }

    public void check() {
        if (state == CLOSED) {
            throw new IllegalStateException("Some object is closed already!");
        }
    }

    public void close() {
        state = CLOSED;
    }

    public boolean isClosed() {
        return (state == CLOSED);
    }
}
