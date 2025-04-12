package org.example.swim;

import org.example.swim.event.StateChangeEvent;
import org.example.swim.event.MyStateChangeEventDispatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class MembershipService {

    @Value("${swim.id:}")
    private String id;

    private final LocalHealthIndicator localHealthIndicator;
    private final MyStateChangeEventDispatcher dispatcher;

    private final ConcurrentMap<String, Member> membership = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final ConcurrentMap<String, ScheduledFuture<?>> tasks = new ConcurrentHashMap<>();

    @Autowired
    public MembershipService(LocalHealthIndicator localHealthIndicator,
                             MyStateChangeEventDispatcher dispatcher
    ) {
        this.localHealthIndicator = localHealthIndicator;
        this.dispatcher = dispatcher;
    }

    public void scheduleSuspicion(String id) {
        cancelSuspicion(id);
        markAsSuspicious(id);
        long delay = localHealthIndicator.getSuspicionDelay();
        ScheduledFuture<?> scheduledFuture = scheduler.schedule(
            () -> markAsDead(id),
            delay,
            TimeUnit.MILLISECONDS
        );
        tasks.put(id, scheduledFuture);
    }

    private void cancelSuspicion(String id) {
        ScheduledFuture<?> scheduledFuture = tasks.get(id);
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
        }
    }

    private void markAsSuspicious(String id) {
        membership.computeIfPresent(id, (key, member) -> {
            if (MemberState.ALIVE == member.getState()) {
                member.setState(MemberState.SUSPICIOUS);
            }
            return member;
        });
    }

    private void markAsDead(String id) {
        membership.computeIfPresent(id, (key, member) -> {
            if (MemberState.SUSPICIOUS == member.getState()) {
                member.setState(MemberState.DEAD);
                member.incrementIncarnation();
            }
            return member;
        });
    }

    public void upsertMember(Member newMember) {
        membership.compute(newMember.getId(), (key, member) -> {
            if (member == null) {
                return newMember;
            }

            if (newMember.compareTo(member) > 0) {
                // alive 로 변경된 경우 scheduledTask 취소
                if (MemberState.ALIVE == newMember.getState()) {
                    cancelSuspicion(newMember.getId());
                }

                final MemberState oldState = member.getState();
                final MemberState newState = newMember.getState();
                // toxiproxy 테스트를 위해 host, port 는 갱신되지 않도록 제한
                member.update(newMember);
                if (id.equals(member.getId()) && oldState != newState) {
                    dispatcher.dispatch(new StateChangeEvent(
                        member,
                        oldState,
                        newState
                    ));
                }
                return member;
            }

            return member;
        });
    }

    public void mergeMembership(List<Member> membership) {
        membership.forEach(this::upsertMember);
    }

    public Collection<Member> getMembership() {
        List<Member> members = new ArrayList<>(membership.values());
        // return random nodes(members)
        Collections.shuffle(members);
        return members;
    }

    public Collection<Member> getPeers() {
        List<Member> peers = membership.values().stream()
            .filter(m -> !id.equals(m.getId()))
            .collect(Collectors.toList());
        Collections.shuffle(peers);
        return peers;
    }

    public Optional<Member> getMember(String id) {
        return Optional.ofNullable(membership.getOrDefault(id, null));
    }

    // log(n)
    public int getFanoutSize() {
        return 1 + (int)Math.log(membership.size());
    }
}
