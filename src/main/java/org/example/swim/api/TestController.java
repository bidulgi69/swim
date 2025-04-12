package org.example.swim.api;

import org.example.swim.Member;
import org.example.swim.MembershipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
public class TestController {

    private final MembershipService membershipService;

    @Autowired
    public TestController(MembershipService membershipService) {
        this.membershipService = membershipService;
    }

    @GetMapping("/membership")
    public Collection<Member> membership() {
        return membershipService.getMembership();
    }

    // for testing buddy system
    @PatchMapping("/suspicion")
    public void updateState(@RequestParam String id) {
        membershipService.scheduleSuspicion(id);
    }
}
