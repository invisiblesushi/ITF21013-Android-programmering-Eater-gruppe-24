package no.hiof.danielch.eater;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.util.Log;
import android.view.MenuItem;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.Fragment;
import android.support.annotation.NonNull;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import no.hiof.danielch.eater.model.Review;
import no.hiof.danielch.eater.model.User;
import no.hiof.danielch.eater.ui.FavoriteFragment;
import no.hiof.danielch.eater.ui.ProfileFragment;
import no.hiof.danielch.eater.ui.ReviewFragment;
import no.hiof.danielch.eater.ui.SearchFragment;
import no.hiof.danielch.eater.utils.AvatarGenerator;
import no.hiof.danielch.eater.utils.BottomNavigationBehavior;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.shashank.sony.fancytoastlib.FancyToast;

import static android.support.v4.app.FragmentManager.POP_BACK_STACK_INCLUSIVE;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int RC_SIGN_IN = 1;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthStateListener;
    private FirebaseFirestore db;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);

        setContentView(R.layout.activity_main);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        //Show/hide nav onScroll
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) navigation.getLayoutParams();
        layoutParams.setBehavior(new BottomNavigationBehavior());

        //Default fragment
        loadFragment(new SearchFragment());
        initFirebaseAuth();
        initFirestore();
        createAuthenticationListener();
    }

    private void initFirestore() {
        db = FirebaseFirestore.getInstance();
    }

    private void initFirebaseAuth() {
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (firebaseAuthStateListener != null)
            firebaseAuth.addAuthStateListener(firebaseAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (firebaseAuthStateListener != null)
            firebaseAuth.removeAuthStateListener(firebaseAuthStateListener);
    }

    //Auth listener
    private void createAuthenticationListener() {
        firebaseAuthStateListener = new FirebaseAuth.AuthStateListener() {

            @Override
            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser == null) {
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(
                                            Arrays.asList(
                                                    new AuthUI.IdpConfig.EmailBuilder().build(),
                                                    new AuthUI.IdpConfig.GoogleBuilder().build()
                                            ))
                                    .setLogo(R.drawable.web_hi_res_round_512)
                                    .build(),
                            RC_SIGN_IN);
                    userCheck();
                }
                else {
                    userCheck();
                }
            }
        };
    }

    //Checks if user exist in db
    private void userCheck(){
        if (firebaseAuth.getCurrentUser() != null){
            DocumentReference docRef = db.collection("users").document(firebaseAuth.getCurrentUser().getUid());
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists() == false) {
                            Log.d(TAG, "new user");
                            addNewUser();
                        }
                    } else {
                        Log.d(TAG, "firebaseAuth.getCurrentUser: ", task.getException());
                    }
                }
            });
        }
    }

    //Creates new document in firestore if new user
    private void addNewUser() {
        CollectionReference users = db.collection("users");

        //Generates an profilepicture url, based on name
        AvatarGenerator avatarGenerator = new AvatarGenerator();
        String avatar = avatarGenerator.generateAvatar(firebaseAuth.getCurrentUser().getDisplayName().toString());

        User user = new User(firebaseAuth.getCurrentUser().getDisplayName().toString(),
                avatar,
                firebaseAuth.getCurrentUser().getUid(),
                null);

        users.document(firebaseAuth.getCurrentUser().getUid()).set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "onSuccess: New user added to db");
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                FancyToast.makeText(this,"successfully logged in as " + user.getDisplayName(),FancyToast.LENGTH_LONG,FancyToast.SUCCESS,false).show();
            } else if (resultCode == RESULT_CANCELED) {
                FancyToast.makeText(this,"Sign in canceled!",FancyToast.LENGTH_LONG,FancyToast.ERROR,false).show();
                finish();
            }
        }
    }


    //https://www.androidhive.info/2017/12/android-working-with-bottom-navigation/
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener;
    {
        mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment;
                switch (item.getItemId()) {
                    case R.id.navigation_search:
                        fragment = new SearchFragment();
                        loadFragment(fragment);
                        return true;

                    case R.id.navigation_favorite:
                        fragment = new FavoriteFragment();
                        loadFragment(fragment);
                        return true;

                    case R.id.navigation_review:
                        fragment = new ReviewFragment();
                        loadFragment(fragment);
                        return true;

                    case R.id.navigation_profile:
                        fragment = new ProfileFragment();
                        loadFragment(fragment);
                        return true;
                }
                return false;
            }


        };

    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }



}
