package com.escript.domain;

public class User extends Storable<Long> implements Cloneable{
    private String username;

    //maybe TODO: make an UserHistory class to remember what an user did over time

    public User(String username){
        this.username = username;
    }
    public String getUsername(){
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj.getClass() != this.getClass())
            return false;
        return ((User) obj).getUsername().equals(this.username);
    }

    @Override
    public int hashCode() {
        return this.username.hashCode();
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", id=" + id +
                '}';
    }

    @Override
    public User clone() {
        try {
            return (User) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Override
    public String toCSV() {
        return String.join(",", Long.toString(getIdentifier()), username);
    }

    @Override
    public int compareID(Long id) {
        return getIdentifier().compareTo(id);
    }

    public static User fromCSV(String string) {
        var parts = string.split(",");
        long identifier = Long.parseLong(parts[0]);
        String username = parts[1];
        var result = new User(username);
        result.setIdentifier(identifier);
        return result;
    }
}
