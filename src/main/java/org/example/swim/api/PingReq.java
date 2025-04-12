package org.example.swim.api;

public record PingReq(
    String id,
    String host,
    int port
) {

    public String address() {
        return host + ":" + port;
    }
}
