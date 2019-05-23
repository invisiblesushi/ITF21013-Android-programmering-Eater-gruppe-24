package no.hiof.danielch.eater.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.ArrayList;

import no.hiof.danielch.eater.R;
import no.hiof.danielch.eater.adapter.FavoriteAdapter;
import no.hiof.danielch.eater.adapter.SearchAdapter;
import no.hiof.danielch.eater.api.Api;
import no.hiof.danielch.eater.model.Review;
import no.hiof.danielch.eater.model.User;
import no.hiof.danielch.eater.model.places.Details;
import no.hiof.danielch.eater.model.places.Result;
import no.hiof.danielch.eater.ui.detail.RestaurantDetailActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FavoriteFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private static final String TAG = "FavoriteFragment";
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;

    private ArrayList<String> mFavorites = new ArrayList<>();
    private ArrayList<Result> mResults = new ArrayList<>();
    private FavoriteAdapter mAdapter;


    public FavoriteFragment() {
        // Required empty public constructor
    }

    public static FavoriteFragment newInstance(String param1, String param2) {
        FavoriteFragment fragment = new FavoriteFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        getFirestoreData();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);

        mRecyclerView = view.findViewById(R.id.favorite_recyclerView);
        mAdapter = new FavoriteAdapter(getContext(), mResults);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }

    //Gets favorites from user
    private void getFirestoreData() {
        db.collection("users").document(firebaseAuth.getCurrentUser().getUid())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    User user = document.toObject(User.class);

                    if (user.getFavorites() != null){
                        for (int i = 0; i < user.getFavorites().size(); i++){
                            getDetails(user.getFavorites().get(i));
                        }
                    }
                }
            }
        });
    }

    //Gets details of each favorite from google places api
    private void getDetails(String PLACE_ID) {

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

                Result details = response.body().getResult();
                mResults.add(details);
                mAdapter.notifyDataSetChanged();

            }

            @Override
            public void onFailure(Call<Details> call, Throwable t) {
            }
        });
    }


}
