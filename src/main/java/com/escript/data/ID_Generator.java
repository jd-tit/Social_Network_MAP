package com.escript.data;

/**
 * Interface to be implemented by all ID generators used in repositories
 * @param <ID_type> The ID datatype
 */
public interface ID_Generator <ID_type> {
    ID_type nextID();
}
