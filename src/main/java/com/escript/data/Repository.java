package com.escript.data;

import com.escript.domain.Storable;
import com.escript.exceptions.DuplicateElementException;
import com.escript.exceptions.ID_NotFoundException;

public interface Repository<ID_type, E extends Storable<ID_type>> extends ReadOnlyRepository<ID_type, E>{
     void add(E storable) throws DuplicateElementException;

     void addAll(Iterable<E> iterable) throws DuplicateElementException;

     void remove(ID_type id) throws ID_NotFoundException;

     void removeAll();
}
