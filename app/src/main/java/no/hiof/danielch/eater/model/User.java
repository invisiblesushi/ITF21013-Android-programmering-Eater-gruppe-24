package no.hiof.danielch.eater.model;

import java.util.List;

public class User {

    private String name;
    private String picture;
    private String uid;
    private List<String> favorites;

    public User(String name, String picture, String uid, List<String> favorites) {
        this.name = name;
        this.picture = picture;
        this.uid = uid;
        this.favorites = favorites;
    }

    public User() {
    }

    public String getName() {
        return name;
    }

    public String getPicture() {
        return picture;
    }

    public String getUid() {
        return uid;
    }

    public List<String> getFavorites() {
        return favorites;
    }
}
