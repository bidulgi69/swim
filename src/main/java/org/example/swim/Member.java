package org.example.swim;

import java.time.Instant;

public class Member implements Comparable<Member> {

    private final String id;
    private final String host;
    private final int port;
    private MemberState state;
    private Instant lastUpdatedAt;
    private long incarnation;

    public Member(String id, String host, int port, long incarnation) {
        this.id = id;
        this.host = host;
        this.port = port;
        this.state = MemberState.ALIVE;
        this.lastUpdatedAt = Instant.now();
        this.incarnation = incarnation;
    }

    public String getId() {
        return id;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String address() {
        return host + ":" + port;
    }

    public MemberState getState() {
        return state;
    }

    public void setState(MemberState state) {
        this.state = state;
    }

    public long getIncarnation() {
        return incarnation;
    }

    public void incrementIncarnation() {
        incarnation++;
        this.lastUpdatedAt = Instant.now();
    }

    public void update(Member newMember) {
        this.state = newMember.getState();
        this.incarnation = newMember.getIncarnation();
        this.lastUpdatedAt = Instant.now();
    }

    @Override
    public int compareTo(Member o) {
        if (incarnation == o.getIncarnation()) {
            return Integer.compare(state.getPriority(), o.getState().getPriority());
        }

        return Long.compare(incarnation, o.getIncarnation());
    }

    @Override
    public String toString() {
        return String.format("Member{id: %s, state: %s, incarnation: %d}", id, state.name(), incarnation);

//        return """
//            Member{
//            id: %s
//            state: %s
//            incarnation: %d
//            }
//            """
//            .formatted(
//                id,
//                state.name(),
//                incarnation
//            );
    }
}
