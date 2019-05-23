package no.hiof.danielch.eater.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.ArrayList;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;
import no.hiof.danielch.eater.R;
import no.hiof.danielch.eater.api.Api;
import no.hiof.danielch.eater.model.places.Details;
import no.hiof.danielch.eater.model.places.Result;
import no.hiof.danielch.eater.ui.detail.RestaurantDetailActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.support.v4.content.ContextCompat.startActivity;
import static no.hiof.danielch.eater.R.id.item_layout_favorite;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.ViewHolder>{
    private static final String TAG = "FavoriteAdapter";

    private Context mContext;
    private ArrayList<Result> mResult;



    public FavoriteAdapter(Context mContext, ArrayList<Result> mResult) {
        this.mContext = mContext;
        this.mResult = mResult;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_favorite, viewGroup, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int i) {
        viewHolder.fav_name.setText(mResult.get(i).getName());
        if (mResult.get(i).getPhotos() != null){
            String picture_URL = Api.PHOTO_URL + mResult.get(i).getPhotos().get(0).getPhotoReference() + "&key=" + Api.API_KEY;
            Glide.with(mContext).load(picture_URL).into(viewHolder.fav_img);
        }
        if (mResult.get(i).getRating() != null){
            viewHolder.fav_ratingbar.setRating(mResult.get(i).getRating().floatValue());
            viewHolder.fav_rating.setText(mResult.get(i).getRating().toString());
        }

        viewHolder.item_layout_favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FancyToast.makeText(mContext,mResult.get(i).getName(),FancyToast.LENGTH_LONG,FancyToast.DEFAULT,false).show();
            }
        });
    }




    @Override
    public int getItemCount() {
        return mResult.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView fav_img;
        TextView fav_name;
        MaterialRatingBar fav_ratingbar;
        TextView fav_rating;
        CardView item_layout_favorite;




        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            fav_img = itemView.findViewById(R.id.fav_image);
            fav_name = itemView.findViewById(R.id.fav_name);
            fav_ratingbar = itemView.findViewById(R.id.fav_ratingbar);
            fav_rating = itemView.findViewById(R.id.fav_rating);
            item_layout_favorite = itemView.findViewById(R.id.item_layout_favorite);
        }
    }

}
