package no.hiof.danielch.eater.ui;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.shashank.sony.fancytoastlib.FancyToast;

import no.hiof.danielch.eater.MainActivity;
import no.hiof.danielch.eater.model.places.Places;
import no.hiof.danielch.eater.model.places.Result;
import no.hiof.danielch.eater.ui.detail.RestaurantDetailActivity;
import no.hiof.danielch.eater.ui.dialog.FilterDialogFragment;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import no.hiof.danielch.eater.api.Api;
import no.hiof.danielch.eater.R;
import no.hiof.danielch.eater.adapter.SearchAdapter;
import ru.alexbykov.nopaginate.callback.OnLoadMoreListener;
import ru.alexbykov.nopaginate.paginate.NoPaginate;


public class SearchFragment extends Fragment implements FilterDialogFragment.OnInputSelected, EasyPermissions.PermissionCallbacks {

    private static final String TAG = "SearchFragment";
    private RecyclerView mRecyclerView;
    private SearchAdapter madapter;

    private NoPaginate noPaginate;
    private View view;
    private android.location.Location location;

    private ArrayList<Result> resultsList = new ArrayList<>();
    private String nextToken = null;


    public SearchFragment() {
        // Required empty public constructor
    }

    public static SearchFragment newInstance(String param1, String param2) {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        location = getCurrentLocation();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_search, container, false);

        mRecyclerView = view.findViewById(R.id.search_recyclerView);
        madapter = new SearchAdapter(getContext(), resultsList, new SearchAdapter.RecyclerViewClickListener() {
            @Override
            public void onClick(View view, int position) {
                Intent intent = new Intent(getContext(), RestaurantDetailActivity.class);
                intent.putExtra("placeid", resultsList.get(position).getPlaceId());
                startActivity(intent);
            }
        }, location);

        mRecyclerView.setAdapter(madapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        initNoPaginate();
        initFilterDialog();
        initExplore();
        initExploreOnClick();

        return view;
    }

    //Sets onclick listner on predefined search query
    private void initExploreOnClick() {
        CardView explore_restaurant = view.findViewById(R.id.explore_restaurant);
        CardView explore_fastfood = view.findViewById(R.id.explore_fastfood);
        CardView explore_chinese = view.findViewById(R.id.explore_chinese);
        CardView explore_cafe = view.findViewById(R.id.explore_cafe);
        CardView explore_sushi = view.findViewById(R.id.explore_sushi);

        final TextView text_search = view.findViewById(R.id.text_search);
        final TextView text_search_location = view.findViewById(R.id.text_search_location);

        explore_restaurant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPlaces("restaurant", location.getLatitude() + "," + location.getLongitude(), "2000");
                text_search.setText("Restaurants");
                text_search_location.setText(locationGeocoder(location.getLatitude(), location.getLongitude()));
            }
        });

        explore_fastfood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPlaces("fastfood", location.getLatitude() + "," + location.getLongitude(), "2000");
                text_search.setText("Fast food");
                text_search_location.setText(locationGeocoder(location.getLatitude(), location.getLongitude()));
            }
        });

        explore_chinese.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPlaces("chinese", location.getLatitude() + "," + location.getLongitude(), "2000");
                text_search.setText("Chinese");
                text_search_location.setText(locationGeocoder(location.getLatitude(), location.getLongitude()));
            }
        });

        explore_cafe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPlaces("cafe", location.getLatitude() + "," + location.getLongitude(), "2000");
                text_search.setText("Cafe");
                text_search_location.setText(locationGeocoder(location.getLatitude(), location.getLongitude()));
            }
        });

        explore_sushi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPlaces("sushi", location.getLatitude() + "," + location.getLongitude(), "2000");
                text_search.setText("Sushi");
                text_search_location.setText(locationGeocoder(location.getLatitude(), location.getLongitude()));
            }
        });
    }

    //Init explore screen, show if there are no search results
    private void initExplore() {
        TextView explore_textview = view.findViewById(R.id.explore_textview);
        FrameLayout frameExplore = view.findViewById(R.id.frameExplore);

        if (resultsList.size() < 1){
            mRecyclerView.setVisibility(View.INVISIBLE);
            explore_textview.setVisibility(View.VISIBLE);
            frameExplore.setVisibility(View.VISIBLE);
        }
        else {
            mRecyclerView.setVisibility(View.VISIBLE);
            explore_textview.setVisibility(View.INVISIBLE);
            frameExplore.setVisibility(View.INVISIBLE);
        }
    }

    //Filter dialog, Search query
    private void initFilterDialog() {
        //Opens dialog fragment
        FrameLayout frameLayout = view.findViewById(R.id.filter_bar);
        frameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FilterDialogFragment filterDialogFragment = new FilterDialogFragment();
                filterDialogFragment.setTargetFragment(SearchFragment.this, 0);

                //passes current location to filter dialog
                Bundle args = new Bundle();
                args.putDouble("lat", location.getLatitude());
                args.putDouble("lng", location.getLongitude());
                filterDialogFragment.setArguments(args);

                filterDialogFragment.show(getFragmentManager(), "FilterDialogFragment");
            }
        });
    }

    //NoPaginate, Trigger getNext when at bottom of recyclerView
    private void initNoPaginate() {
        noPaginate = NoPaginate.with(mRecyclerView).setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                if (nextToken != null){
                    getNext(nextToken);
                }
            }
        }).build();

        noPaginate.setNoMoreItems(true);
    }


    @Override
    public void onResume() {
        Log.e("DEBUG", "onResume of LoginFragment");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.e("DEBUG", "OnPause of loginFragment");
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        noPaginate.unbind();
    }

    //Gets places using RetroFit
    private void getPlaces(String keyword, String location, String radius) {
        resultsList.clear();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Api.PLACES_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        Api api = retrofit.create(Api.class);
        Call<Places> call;

        //Ignore keyword if keyword is restaurant
        if (keyword == "restaurant"){
            call = api.getPlaces(
                    location,
                    radius,
                    "restaurant",
                    Api.API_KEY);
        }
        else {
            call = api.getPlacesKeyword(
                    location,
                    radius,
                    "restaurant",
                    keyword,
                    Api.API_KEY);
        }

        call.enqueue(new Callback<Places>() {
            @Override
            public void onResponse(Call<Places> call, Response<Places> response) {
                resultsList.addAll(response.body().getResults());
                if (response.body().getNextPageToken() == null ){
                    noPaginate.setNoMoreItems(true);
                }
                else {
                    nextToken = response.body().getNextPageToken();
                    noPaginate.setNoMoreItems(false);
                }
                madapter.notifyDataSetChanged();
                initExplore();
            }

            @Override
            public void onFailure(Call<Places> call, Throwable t) {
                FancyToast.makeText(getContext(), t.getMessage() ,FancyToast.LENGTH_LONG,FancyToast.ERROR,false).show();
                noPaginate.showError(true);
            }
        });
    }

    //Get next 20 results
    private void getNext(String token) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Api.PLACES_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        Api api = retrofit.create(Api.class);
        Call<Places> callNext;
        callNext = api.getPlacesNext(
                Api.API_KEY,
                token);
        
        callNext.enqueue(new Callback<Places>() {
            @Override
            public void onResponse(Call<Places> callNext, Response<Places> response) {
                resultsList.addAll(response.body().getResults());

                if (response.body().getNextPageToken() != null) {
                    nextToken = response.body().getNextPageToken();
                }
                else {
                    nextToken = null;
                    noPaginate.setNoMoreItems(true);
                }

                madapter.notifyDataSetChanged();
                initExplore();
            }

            @Override
            public void onFailure(Call<Places> call, Throwable t) {
                FancyToast.makeText(getContext(), t.getMessage() ,FancyToast.LENGTH_LONG,FancyToast.ERROR,false).show();
                noPaginate.showError(true);
            }
        });
    }

    //Recives query input from dialog fragment
    @Override
    public void sendInput(String keyword, String loc, String radius) {
        getPlaces(keyword, location.getLatitude() + "," + location.getLongitude(), radius);

        //Set search bar
        TextView text_search = view.findViewById(R.id.text_search);
        TextView text_search_location = view.findViewById(R.id.text_search_location);

        text_search.setText(keyword + " " + radius + "m");
        text_search_location.setText(locationGeocoder(location.getLatitude(), location.getLongitude()));
    }

    //Returns place name based on location
    private String locationGeocoder(Double lat, Double lng){
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        List<Address> adress = null;
        try {
            adress = geocoder.getFromLocation(lat, lng, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] adr = adress.get(0).getAddressLine(0).split(",");
        return adr[0];
    }


    //Permission for location
    @SuppressLint("MissingPermission")
    @AfterPermissionGranted(0)
    public Location getCurrentLocation(){
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        if (EasyPermissions.hasPermissions(getContext(), perms)){

            LocationManager locationManager = (LocationManager) getContext().getSystemService(getContext().LOCATION_SERVICE);

            Criteria criteria = new Criteria();
            String provider = locationManager.getBestProvider(criteria, true);
            location = locationManager.getLastKnownLocation(provider);
            if (location == null){
                Log.d(TAG, "getCurrentLocation: location == null");
            }
            return location;
        }
        else {
            EasyPermissions.requestPermissions(this, "This app needs permissions to access location", 0, perms);
        }
        return null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //Forwards result to easypermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)){
            //Sends user to settings
            new AppSettingsDialog.Builder(this).build().show();
        }
    }



}
