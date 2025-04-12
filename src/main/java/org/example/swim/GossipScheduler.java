package org.example.swim;

import jakarta.annotation.PostConstruct;
import org.example.swim.api.PingReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Component
public class GossipScheduler {

    @Value("${swim.gossip.init-delay-ms:3000}")
    private long initialDelay;

    @Value("${swim.gossip.interval-ms:500}")
    private long period;

    private final MembershipService membershipService;
    private final RestTemplate restTemplate;
    private final LocalHealthIndicator localHealthIndicator;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @Autowired
    public GossipScheduler(MembershipService membershipService,
                           RestTemplate restTemplate,
                           LocalHealthIndicator localHealthIndicator
    ) {
        this.membershipService = membershipService;
        this.restTemplate = restTemplate;
        this.localHealthIndicator = localHealthIndicator;
    }

    @PostConstruct
    public void init() {
        scheduler.scheduleAtFixedRate(
            gossip(),
            initialDelay,
            period,
            TimeUnit.MILLISECONDS
        );
    }

    private Runnable gossip() {
        return () -> {
            List<Member> peers = membershipService.getPeers()
                .stream()
                .filter(m -> MemberState.ALIVE == m.getState())
                .limit(membershipService.getFanoutSize())
                .toList();

            List<Member> gossip = membershipService.getMembership()
                .stream()
                .limit(membershipService.getFanoutSize())
                .toList();

            for (Member peer : peers) {
                try {
                    String url = "http://" + peer.address() + "/ping";
                    List<Member> membership = restTemplate.exchange(
                        url,
                        HttpMethod.POST,
                        new HttpEntity<>(gossip),
                        new ParameterizedTypeReference<List<Member>>() {}
                    ).getBody();

                    if (membership != null) {
                        membershipService.mergeMembership(membership);
                    }
                } catch (RuntimeException e) {
                    // ping-req
                    Stream<Member> agents = membershipService.getPeers()
                        .stream()
                        .filter(m -> MemberState.ALIVE == m.getState() && !peer.getId().equals(m.getId()))
                        .limit(3);

                    boolean anySuccess = agents.anyMatch(agent -> {
                        String url = "http://" + agent.address() + "/ping-req";
                        PingReq body = new PingReq(peer.getId(), peer.getHost(), peer.getPort());
                        try {
                            Boolean result = restTemplate.postForEntity(
                                url,
                                body,
                                Boolean.class
                            ).getBody();
                            localHealthIndicator.recordSuccess();
                            return Boolean.TRUE.equals(result);
                        } catch (RuntimeException e2) {
                            localHealthIndicator.recordFailure();
                            return false;
                        }
                    });

                    if (!anySuccess) {
                        // mark as suspicious
                        membershipService.scheduleSuspicion(peer.getId());
                    }
                }
            }
        };
    }
}
