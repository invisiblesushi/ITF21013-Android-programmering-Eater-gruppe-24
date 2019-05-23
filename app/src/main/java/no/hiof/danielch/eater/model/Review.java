package no.hiof.danielch.eater.model;

public class Review {

    /*
    TextView reviewUser;
    TextView reviewTimestamp;
    TextView reviewRating;
    TextView reviewRatingNum;
    TextView reviewDesc;
    ImageView reviewUserPicture;
    ImageView reviewPicture;*/

    private String place_id;
    private String user_uid;
    private String rating;
    private String desc;
    private String picture;

    public Review(String place_id, String user_uid, String rating, String desc, String picture) {
        this.place_id = place_id;
        this.user_uid = user_uid;
        this.rating = rating;
        this.desc = desc;
        this.picture = picture;
    }


    public Review() {
    }

    public String getPlace_id() {
        return place_id;
    }

    public String getUser_uid() {
        return user_uid;
    }

    public String getRating() {
        return rating;
    }

    public String getPicture() {
        return picture;
    }

    public String getDesc() {
        return desc;
    }

}
