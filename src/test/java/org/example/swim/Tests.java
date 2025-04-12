package org.example.swim;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class Tests {

    @Test
    public void test() {

        Map<String, Member> membership = new HashMap<>();
        Member m1 = new Member("node1", "node1", 8080, 0);
        Member m2 = new Member("node1", "node1", 8080, 0);

        m2.setState(MemberState.SUSPICIOUS);
        membership.put("node1", m1);

        membership.compute("node1", (key, member) -> {
            if (member == null) {
                return m2;
            }

            if (m2.compareTo(member) > 0) {
                return m2;
            } else {
                return m1;
            }
        });

        Assertions.assertEquals(MemberState.SUSPICIOUS, membership.get("node1").getState());
    }
}
