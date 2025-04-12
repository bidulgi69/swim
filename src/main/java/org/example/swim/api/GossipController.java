package org.example.swim.api;

import org.example.swim.LocalHealthIndicator;
import org.example.swim.Member;
import org.example.swim.MembershipService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Controller
public class GossipController {

    private final MembershipService membershipService;
    private final RestTemplate restTemplate;
    private final LocalHealthIndicator localHealthIndicator;
    private final Logger _logger = LoggerFactory.getLogger(getClass());

    @Autowired
    public GossipController(MembershipService membershipService,
                            RestTemplate restTemplate,
                            LocalHealthIndicator localHealthIndicator
    ) {
        this.membershipService = membershipService;
        this.restTemplate = restTemplate;
        this.localHealthIndicator = localHealthIndicator;
    }

    // receives partial membership
    @PostMapping("/ping")
    public ResponseEntity<List<Member>> ping(@RequestBody List<Member> gossip) {
        _logger.info("Ping received {}", gossip);
        membershipService.mergeMembership(gossip);
        // 자신의 membership 정보를 반환
        return ResponseEntity.ok(membershipService.getMembership()
            .stream()
            .limit(membershipService.getFanoutSize())
            .toList()
        );
    }

    // suspicious marking 전 ping 을 대신 수행
    @PostMapping("/ping-req")
    public ResponseEntity<Boolean> pingReq(@RequestBody PingReq req) {
        _logger.info("Ping-req received {}", req);
        // toxiproxy 테스트를 위해 자신의 membership 에서 조회를 수행
        String address = membershipService.getMember(req.id())
            .map(Member::address)
            .orElseGet(req::address);
        String url = "http://" + address + "/ping";
        List<Member> gossip = membershipService.getMembership()
            .stream()
            .limit(membershipService.getFanoutSize())
            .toList();

        try {
            restTemplate.postForEntity(url, gossip, List.class);
            localHealthIndicator.recordSuccess();
            return ResponseEntity.ok(true);
        } catch (RuntimeException e) {
            _logger.error("Failed to send ping request to {}", req.id());
            localHealthIndicator.recordFailure();
            return ResponseEntity.ok(false);
        }
    }
}
