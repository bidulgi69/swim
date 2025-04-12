package org.example.swim;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Component
public class SwimApplicationRunner implements ApplicationRunner {

    @Value("${swim.seeds:}")
    private List<String> seeds;

    private final MembershipService membershipService;
    private final IncarnationManager incarnationManager;

    @Autowired
    public SwimApplicationRunner(MembershipService membershipService,
                                 IncarnationManager incarnationManager
    ) {
        this.membershipService = membershipService;
        this.incarnationManager = incarnationManager;
    }

    @PostConstruct
    public void validate() {
        if (CollectionUtils.isEmpty(seeds)) {
            throw new IllegalStateException("No seeds specified");
        }
    }

    @Override
    public void run(ApplicationArguments args) {
        seeds.forEach(seed -> {
            String[] parts = seed.split("=");
            String id = parts[0];
            String[] hostAndPort = parts[1].split(":");
            long incarnation = incarnationManager.loadAndBump(id);
            Member member = new Member(id, hostAndPort[0], Integer.parseInt(hostAndPort[1]), incarnation);
            membershipService.upsertMember(member);
        });
    }
}
