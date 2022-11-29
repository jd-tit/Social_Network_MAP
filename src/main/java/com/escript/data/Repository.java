package com.escript.data;

import com.escript.domain.Storable;
import com.escript.exceptions.DuplicateElementException;
import com.escript.exceptions.ID_NotFoundException;

import java.util.Collection;

public interface Repository<ID_type, E extends Storable<ID_type>> {
     void add(E storable) throws DuplicateElementException;

     void addAll(Iterable<E> iterable) throws DuplicateElementException;
     Collection<E> getAll();
     E get(ID_type id) throws ID_NotFoundException;

     void remove(ID_type id) throws ID_NotFoundException;

     void removeAll();
}
