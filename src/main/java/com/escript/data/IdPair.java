package com.escript.data;

import com.escript.exceptions.DuplicateElementException;

public class IdPair {
    private final Long element1;
    private final Long element2;
    public IdPair(Long value1, Long value2) throws DuplicateElementException {
        if(value1.equals(value2))
            throw new DuplicateElementException("Same value provided to IdPair");
        if(value1 < value2) {
            element1 = value1;
            element2 = value2;
        } else {
            element1 = value2;
            element2 = value1;
        }
    }

    public Long getFirst() {
        return element1;
    }

    public Long getSecond() {
        return element2;
    }
}
