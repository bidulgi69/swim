package org.example.swim.event;

import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

@Component
public class MyStateChangeEventDispatcher {

    private final List<Consumer<StateChangeEvent>> consumers = new LinkedList<>();

    public void registerConsumer(Consumer<StateChangeEvent> consumer) {
        consumers.add(consumer);
    }

    public void dispatch(StateChangeEvent event) {
        consumers.forEach(consumer -> consumer.accept(event));
    }
}
