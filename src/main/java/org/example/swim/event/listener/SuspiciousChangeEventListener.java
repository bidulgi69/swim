package org.example.swim.event.listener;

import org.example.swim.LocalHealthIndicator;
import org.example.swim.Member;
import org.example.swim.MemberState;
import org.example.swim.MembershipService;
import org.example.swim.event.MyStateChangeEventDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

// buddy system
@Component
public class SuspiciousChangeEventListener {

    private final Logger _logger = LoggerFactory.getLogger(getClass());

    @Autowired
    public SuspiciousChangeEventListener(MyStateChangeEventDispatcher dispatcher,
                                         MembershipService membershipService,
                                         RestTemplate restTemplate,
                                         LocalHealthIndicator localHealthIndicator
    ) {
        dispatcher.registerConsumer(event -> {
            if (MemberState.SUSPICIOUS == event.newState() && MemberState.SUSPICIOUS != event.oldState()) {
                _logger.info("Notify buddy nodes that I'm alive");
                List<Member> buddies = membershipService.getPeers()
                    .stream()
                    .filter(m -> MemberState.ALIVE == m.getState())
                    .limit(3)
                    .toList();

                Member me = event.member();
                me.setState(MemberState.ALIVE);
                me.incrementIncarnation();
                membershipService.upsertMember(me);

                List<Member> gossip = List.of(me);
                buddies.forEach(buddy -> {
                    String url = "http://" + buddy.address() + "/ping";
                    try {
                        restTemplate.postForEntity(
                            url,
                            gossip,
                            Void.class
                        );
                        localHealthIndicator.recordSuccess();
                    } catch (RuntimeException e) {
                        localHealthIndicator.recordFailure();
                    }
                });
            }
        });
    }
}
