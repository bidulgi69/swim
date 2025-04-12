package org.example.swim;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LocalHealthIndicator {

    @Value("${swim.lifeguard.suspicion-delay-ms:3000}")
    private long suspicionDelay;

    @Value("${swim.lifeguard.max-health-monitor:5}")
    private double maxHealthMonitor;

    @Value("${swim.lifeguard.min-health-monitor:0.5}")
    private double minHealthMonitor;

    private double lhm = 1.0f;

    // 실패 시 증가
    public synchronized void recordFailure() {
        lhm = Math.min(maxHealthMonitor, lhm + 0.5d);
    }

    // 성공 시 감소
    public synchronized void recordSuccess() {
        lhm = Math.max(minHealthMonitor, lhm - 0.2d);
    }

    public double getLhm() {
        return lhm;
    }

    public long getSuspicionDelay() {
        return (long)(suspicionDelay * lhm);
    }
}
