package no.hiof.danielch.eater.ui.detail;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.smarteist.autoimageslider.SliderLayout;
import com.smarteist.autoimageslider.SliderView;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;
import no.hiof.danielch.eater.adapter.ReviewAdapter;
import no.hiof.danielch.eater.model.places.Details;
import no.hiof.danielch.eater.model.places.Result;
import no.hiof.danielch.eater.R;
import no.hiof.danielch.eater.api.Api;
import no.hiof.danielch.eater.model.Review;
import no.hiof.danielch.eater.ui.form.ReviewFormActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.ArrayList;

public class RestaurantDetailActivity extends Activity implements OnMapReadyCallback{
    private static final String TAG = "RestaurantDetail";
    private String PLACE_ID;

    private FirebaseFirestore db;
    private FirebaseAuth firebaseAuth;

    private Result details;
    private ArrayList<Review> reviewlist = new ArrayList<>();

    private ReviewAdapter madapter;
    private RecyclerView mRecyclerView;

    private SliderLayout sliderLayout;
    private MapView mMapView;


    @Override
    protected void onResume() {
        super.onResume();
        getFirestoreData();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_detail);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        PLACE_ID = getIntent().getStringExtra("placeid");
        
        initRecyclerView();
        getDetails();
        getFirestoreData();


        //Add to favorite
        Button fav_btn = findViewById(R.id.fav_btn);
        fav_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Add current place to favirute array
                db.collection("users").document(firebaseAuth.getCurrentUser().getUid())
                        .update("favorites", FieldValue.arrayUnion(PLACE_ID)).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        FancyToast.makeText(getBaseContext(), details.getName() + " added to favorite",FancyToast.LENGTH_LONG,FancyToast.SUCCESS,false).show();
                    }
                });
            }
        });

        //Opens ReviewFormActivity
        Button rev_btn = findViewById(R.id.rev_btn);
        rev_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openReviewFormActivity();
            }
        });
    }


    private void openReviewFormActivity(){
        Intent intent = new Intent(this, ReviewFormActivity.class);
        intent.putExtra("placeid", PLACE_ID);
        startActivity(intent);
    }

    private void initRecyclerView() {
        mRecyclerView = findViewById(R.id.restaurant_reviews_recyclerview);
        madapter = new ReviewAdapter(this, reviewlist);
        mRecyclerView.setAdapter(madapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }


    //Gets data from firestore
    //Bug: gets the same data twice.
    private void getFirestoreData() {
        reviewlist.clear();

        final CollectionReference reference = db.collection("reviews");
        Query query = reference.whereEqualTo("place_id", PLACE_ID);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Review review = document.toObject(Review.class);
                        reviewlist.add(review);
                        madapter.notifyDataSetChanged();
                    }
                } else {
                    Log.w(TAG, "Error getting documents.", task.getException());
                }
            }
        });
    }


    private void getDetails() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Api.DETAILS_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        Api api = retrofit.create(Api.class);

        Call<Details> call = api.getDetails(
                PLACE_ID,
                "name,rating,formatted_phone_number,opening_hours,photos,type,formatted_address,website,geometry",
                Api.API_KEY);

        call.enqueue(new Callback<Details>() {
            @Override
            public void onResponse(Call<Details> call, Response<Details> response) {
                details = response.body().getResult();
                setData();
            }

            @Override
            public void onFailure(Call<Details> call, Throwable t) {
                FancyToast.makeText(RestaurantDetailActivity.this, t.getMessage() ,FancyToast.LENGTH_LONG,FancyToast.ERROR,false).show();
            }
        });
    }

    private void setData() {
        sliderLayout = findViewById(R.id.imageSlider);
        TextView weekday_text = findViewById(R.id.weekday_text);
        TextView restaurant_name = findViewById(R.id.restaurant_name);
        MaterialRatingBar detail_ratingbar = findViewById(R.id.detail_ratingbar);
        TextView detail_rating = findViewById(R.id.detail_rating);
        TextView detail_phone = findViewById(R.id.detail_phone);
        TextView detail_url = findViewById(R.id.detail_url);

        if (details.getPhotos() != null){
            String ref = details.getPhotos().get(0).getPhotoReference().toString();
            sliderLayout.setIndicatorAnimation(SliderLayout.Animations.SLIDE);
            sliderLayout.setScrollTimeInSec(3600);  //Unable to disable scrolling
            setSliderViews();
        }
        else {
            sliderLayout.setVisibility(View.INVISIBLE);
        }

        if (details.getOpeningHours() != null){
            String weekday = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                weekday = String.join("\n", details.getOpeningHours().getWeekdayText());
            }
            weekday_text.setText(weekday);
        }

        restaurant_name.setText(details.getName());

        mMapView = findViewById(R.id.mapView);
        if(mMapView != null){
            mMapView.onCreate(null);
            mMapView.onResume();
            mMapView.getMapAsync(this);
        }

        if (details.getRating() != null){
            detail_rating.setText(details.getRating().toString());
            detail_ratingbar.setRating(details.getRating().floatValue());
        }

        if (details.getFormattedPhoneNumber() != null){
            detail_phone.setText(details.getFormattedPhoneNumber());
        }

        if (details.getWebsite() != null){
            detail_url.setText(details.getWebsite());
        }
    }

    //Sets pictures into image slider
    private void setSliderViews() {
        for (int i = 0; i < details.getPhotos().size() ; i++) {
            SliderView sliderView = new SliderView(this);
            sliderView.setImageUrl(Api.PHOTO_URL + details.getPhotos().get(i).getPhotoReference()+ "&key=" + Api.API_KEY);
            sliderView.setImageScaleType(ImageView.ScaleType.CENTER_CROP);
            sliderLayout.addSliderView(sliderView);
        }
    }


    //Sets marker on map
    @Override
    public void onMapReady(GoogleMap googleMap) {
        double lat = details.getGeometry().getLocation().getLat();
        double lng = details.getGeometry().getLocation().getLng();

        MapsInitializer.initialize(this);
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(lat, lng))
                .title(details.getName()));

        CameraPosition Marker = CameraPosition.builder().target(new LatLng(lat, lng)).zoom(16).build();

        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(Marker));
    }
}
