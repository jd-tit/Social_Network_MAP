package com.escript.domain;

public class User implements Cloneable{
    private final String username;

    //maybe TODO: make an UserHistory class to remember what an user did over time

    public User(String username){
        this.username = username;
    }
    public String getUsername(){
        return this.username;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj.getClass() != this.getClass())
            return false;
        return ((User) obj).username.equals(this.username);
    }

    @Override
    public int hashCode() {
        return this.username.hashCode();
    }

    @Override
    public User clone() {
        try {
            return (User) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
