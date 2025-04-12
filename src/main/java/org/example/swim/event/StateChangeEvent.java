package org.example.swim.event;

import org.example.swim.Member;
import org.example.swim.MemberState;

public record StateChangeEvent(
    Member member,
    MemberState oldState,
    MemberState newState
) {
}
