package no.hiof.danielch.eater.api;

import no.hiof.danielch.eater.model.places.Details;
import no.hiof.danielch.eater.model.places.Places;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface Api {

    //Base url
    String PLACES_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/";
    String DETAILS_URL = "https://maps.googleapis.com/maps/api/place/details/";
    String PHOTO_URL = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=1000&photoreference=";

    //Google API key
    String API_KEY = "";

    //Note
    //Cannot sort in api(Cause radius), must sort in program


    //Places
    @GET("json")
    Call<Places> getPlaces(
            @Query("location") String location,
            @Query("radius") String radius,
            @Query("type") String type,
            @Query("key") String key
            );

    @GET("json")
    Call<Places> getPlacesKeyword(
            @Query("location") String location,
            @Query("radius") String radius,
            @Query("type") String type,
            @Query("keyword") String keyword,
            @Query("key") String key
    );

    @GET("json")
    Call<Places> getPlacesNext(
            @Query("key") String key,
            @Query("pagetoken") String token
    );

    //Details
    @GET("json")
    Call<Details> getDetails(
            @Query("placeid") String placeid,
            @Query("fields") String fields,
            @Query("key") String key
    );

}
