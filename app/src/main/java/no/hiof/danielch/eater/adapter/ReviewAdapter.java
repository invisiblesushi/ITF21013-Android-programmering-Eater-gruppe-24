package no.hiof.danielch.eater.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;
import no.hiof.danielch.eater.R;
import no.hiof.danielch.eater.model.Review;
import no.hiof.danielch.eater.model.User;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewsViewHolder>{

    private static final String TAG = "ReviewAdapter";
    private ArrayList<Review> mReview;
    private Context mContext;






    public ReviewAdapter(Context mContext, ArrayList<Review> mReview) {
        this.mReview = mReview;
        this.mContext = mContext;
    }


    @Override
    public ReviewAdapter.ReviewsViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_review, viewGroup, false);
        ReviewAdapter.ReviewsViewHolder holder = new ReviewAdapter.ReviewsViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ReviewAdapter.ReviewsViewHolder holder, int i) {
        Review currentObj = mReview.get(i);
        holder.setData(currentObj);
    }

    @Override
    public int getItemCount() {
        return mReview.size();
    }




    //Viewholder holds each induvidual entries in memory
    class ReviewsViewHolder extends RecyclerView.ViewHolder{




        TextView reviewUser;
        TextView reviewTimestamp;
        MaterialRatingBar reviewRating;
        TextView reviewRatingNum;
        TextView reviewDesc;
        ImageView reviewUserPicture;
        ImageView reviewPicture;

        public ReviewsViewHolder(View itemView) {
            super(itemView);


            //User
            reviewUser = itemView.findViewById(R.id.per_rev_User);
            reviewUserPicture = itemView.findViewById(R.id.per_rev_UserPicture);

            //Review
            reviewRating = itemView.findViewById(R.id.per_rev_Rating);
            reviewRatingNum = itemView.findViewById(R.id.reviewRatingNum);
            reviewDesc = itemView.findViewById(R.id.per_rev_Desc);
            reviewPicture = itemView.findViewById(R.id.reviewPicture);


        }


        public void setData(Review data) {
            reviewDesc.setText(data.getDesc());

            reviewPicture.setVisibility(View.INVISIBLE);
            if (data.getPicture() != null){
                reviewPicture.setVisibility(View.VISIBLE);
                Glide.with(mContext).load(data.getPicture()).into(reviewPicture);
            }

            reviewRatingNum.setText(data.getRating());
            reviewRating.setRating(Float.parseFloat(data.getRating()));


            //Getting user data from firestore
            if (data.getUser_uid() != null){

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("users").document(data.getUser_uid())
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    User user = document.toObject(User.class);
                                    reviewUser.setText(user.getName());
                                    if (user.getPicture() != null){
                                        Glide.with(mContext).load(user.getPicture()).into(reviewUserPicture);
                                    }

                                } else {
                                    Log.w(TAG + "firestore", "Error getting documents.", task.getException());
                                }
                            }
                        });

            }







        }


    }

}