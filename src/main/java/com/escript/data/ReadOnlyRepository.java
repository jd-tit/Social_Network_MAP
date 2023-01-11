package com.escript.data;

import com.escript.domain.Storable;
import com.escript.exceptions.ID_NotFoundException;

import java.util.Collection;

public interface ReadOnlyRepository<ID_type, E extends Storable<ID_type>> {
    Collection<E> getAll();
    E get(ID_type id) throws ID_NotFoundException;
}
