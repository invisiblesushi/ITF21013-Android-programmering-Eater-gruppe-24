package no.hiof.danielch.eater.ui;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import no.hiof.danielch.eater.adapter.ReviewAdapter;
import no.hiof.danielch.eater.R;
import no.hiof.danielch.eater.model.Review;


public class ReviewFragment extends Fragment {
    private static final String TAG = "ReviewFragment";
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthStateListener;
    private FirebaseFirestore db;

    RecyclerView mRecyclerView;
    ArrayList<Review> Reviewlist = new ArrayList<>();
    ReviewAdapter madapter;


    public ReviewFragment() {
        // Required empty public constructor
    }

    public static ReviewFragment newInstance(String param1, String param2) {
        ReviewFragment fragment = new ReviewFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        firebaseAuth = firebaseAuth.getInstance();
        getFirestoreData();






    }

    //Gets documents written by current user
    private void getFirestoreData() {
        db.collection("reviews").whereEqualTo("user_uid", firebaseAuth.getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Review review = document.toObject(Review.class);
                                Reviewlist.add(review);
                                madapter.notifyDataSetChanged();
                            }
                        } else {
                            Log.w(TAG + "firestore", "Error getting documents.", task.getException());
                        }
                    }
                });
    }





    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_review, container, false);

        mRecyclerView = view.findViewById(R.id.reviewRecyclerView);
        madapter = new ReviewAdapter(getContext(), Reviewlist );

        mRecyclerView.setAdapter(madapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }

}
