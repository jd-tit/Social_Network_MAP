package com.escript.data;

public class LongID_Generator implements ID_Generator<Long> {
    private long current;

    /**
     * Construct a generator of Long IDs
     * @param start The number at which to start generating IDs successively
     */
    public LongID_Generator(long start) {
        this.current = start;
    }

    public void setCurrent(long current) {
        this.current = current;
    }

    @Override
    public Long nextID() {
        return this.current++;
    }
}
