package com.escript.domain;

public abstract class Storable<ID_type>{
    private ID_type id;
    public ID_type getIdentifier() {
        return id;
    }

    public abstract String toCSV();
    public void setIdentifier(ID_type identifier) {
        this.id = identifier;
    }

    public abstract int compareID(ID_type id);
}
