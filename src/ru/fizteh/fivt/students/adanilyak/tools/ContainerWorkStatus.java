package ru.fizteh.fivt.students.adanilyak.tools;

/**
 * User: Alexander
 * Date: 22.11.13
 * Time: 2:23
 */
public enum ContainerWorkStatus {
    WORKING(1),
    NOT_INITIALIZED(-1),
    CLOSED(0);

    private int state;

    private ContainerWorkStatus(int givenState) throws IllegalStateException {
        if (givenState != 0 && givenState != 1 && givenState != -1) {
            throw new IllegalStateException("container work status: bad state");
        } else {
            state = givenState;
        }
    }

    public void isOkForOperations() throws IllegalStateException {
        if (state == -1) {
            throw new IllegalStateException("container work status: not initialized");
        }
        if (state == 0) {
            throw new IllegalStateException("container work status: closed");
        }
        /** state == 1, container ready for work */
    }
}
