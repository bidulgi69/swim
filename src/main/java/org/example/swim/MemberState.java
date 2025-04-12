package org.example.swim;

public enum MemberState {

    ALIVE(0),
    SUSPICIOUS(1),
    DEAD(2),
    ;

    private final int priority;

    MemberState(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}
