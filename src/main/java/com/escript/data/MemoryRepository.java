package com.escript.data;

import com.escript.domain.Storable;
import com.escript.exceptions.DuplicateElementException;
import com.escript.exceptions.ID_NotFoundException;

import java.util.*;
import java.util.stream.Stream;

/**
 * An implementation of the Repository interface with no data persistence
 * @param <ID_type> The type of IDs used by the elements to be stored
 * @param <E> The type of elements to be stored; has to be a kind of Storable
 */
public class MemoryRepository<ID_type, E extends Storable<ID_type>> implements Repository<ID_type, E> {
    HashMap<ID_type, E> contents;
    HashSet<E> contentSet;
    ID_Generator<ID_type> id_generator;

    /**
     * Constructor for this kind of repository
     * @param id_generator An object that will generate a unique ID for every new element
     */
    public MemoryRepository(ID_Generator<ID_type> id_generator){
        this.contents = new HashMap<>();
        this.contentSet = new HashSet<>();
        this.id_generator = id_generator;
    }

    public MemoryRepository(){
        this.contents = new HashMap<>();
        this.contentSet = new HashSet<>();
    }

    @Override
    public void add(E e) throws DuplicateElementException {
        if(contentSet.contains(e)) {
            throw new DuplicateElementException(
                "An item equal to \"%s\" is already present."
                .formatted(e.toString()));
        }

        if(e.getIdentifier() == null )
            e.setIdentifier(id_generator.nextID());
        contents.put(e.getIdentifier(), e);
        contentSet.add(e);
    }

    @Override
    public void addAll(Iterable<E> iterable) throws DuplicateElementException {
        for (var element : iterable) {
            this.add(element);
        }
    }

    @Override
    public Collection<E> getAll() {
        return this.contents.values();
    }

    @Override
    public E get(ID_type id) {
        return this.contents.get(id);
    }

    @Override
    public void remove(ID_type id) throws ID_NotFoundException {
        if(!contents.containsKey(id)) {
            throw new ID_NotFoundException("ID \"%s\" could not be found.".formatted(id.toString()));
        }
        E element = contents.remove(id);
        contentSet.remove(element);
    }

    @Override
    public void removeAll() {
        contents.clear();
        contentSet.clear();
    }

    public Stream<E> stream(){
        return this.contents.values().stream();
    }
}
