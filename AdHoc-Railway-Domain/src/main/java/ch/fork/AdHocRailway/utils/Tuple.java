package ch.fork.AdHocRailway.utils;

public class Tuple<A, B> {

    private final A first;
    private final B second;

    public Tuple(final A first, final B second) {
        this.first = first;
        this.second = second;
    }

    public A getFirst() {
        return first;
    }

    public B getSecond() {
        return second;
    }

}
