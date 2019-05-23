package no.hiof.danielch.eater.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;






import com.bumptech.glide.Glide;

import java.util.ArrayList;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;
import no.hiof.danielch.eater.model.places.Result;
import no.hiof.danielch.eater.R;
import no.hiof.danielch.eater.api.Api;


public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.PlacesViewHolder>{

    private static final String TAG = "SearchAdapter";
    private ArrayList<Result> mResult;
    private Context mContext;
    private android.location.Location location;
    private RecyclerViewClickListener clickListener;




    public SearchAdapter(Context mContext, ArrayList<Result> mResult, RecyclerViewClickListener clickListener, android.location.Location location) {
        this.mResult = mResult;
        this.mContext = mContext;
        this.clickListener = clickListener;
        this.location = location;
    }


    @Override
    public PlacesViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_restaurant, viewGroup, false);
        PlacesViewHolder holder = new PlacesViewHolder(view, clickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(PlacesViewHolder holder, int i) {
        Result currentObj = mResult.get(i);
        holder.setData(currentObj);
    }

    @Override
    public int getItemCount() {
        return mResult.size();
    }




    //Viewholder holds each induvidual entries in memory
    class PlacesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        RecyclerViewClickListener onClickListener;


        TextView restaurant_name;
        ImageView restaurant_item_image;
        MaterialRatingBar restaurant_rating;
        TextView restaurant_rating_num;
        TextView restaurant_distance;
        TextView restaurant_type;

        public PlacesViewHolder(View itemView, RecyclerViewClickListener onClickListener) {
            super(itemView);

            this.onClickListener = onClickListener;

            restaurant_name = itemView.findViewById(R.id.restaurant_name);
            restaurant_item_image = itemView.findViewById(R.id.restaurant_item_image);
            restaurant_rating = itemView.findViewById(R.id.detail_ratingbar);
            restaurant_rating_num = itemView.findViewById(R.id.restaurant_rating_num);
            restaurant_distance = itemView.findViewById(R.id.restaurant_distance);

            itemView.setOnClickListener(this);
        }


        public void setData(Result data) {

            if (data.getPhotos() == null) {
                Log.d(TAG, "setData: No photo");
            }
            else {
                String picture_URL = Api.PHOTO_URL + data.getPhotos().get(0).getPhotoReference() + "&key=" + Api.API_KEY;
                Glide.with(mContext).load(picture_URL).into(restaurant_item_image);
            }

            restaurant_name.setText(data.getName());

            if (data.getRating() == null){
                Log.d(TAG, "setData: " + data.getName() + "no rating found");
            }
            else{
                restaurant_rating.setRating(data.getRating().floatValue());
                restaurant_rating_num.setText(data.getRating().toString());
            }

            //Calculates distance based on loaction
            android.location.Location loc2 = new android.location.Location("");
            loc2.setLatitude(data.getGeometry().getLocation().getLat());
            loc2.setLongitude(data.getGeometry().getLocation().getLng());

            float distanceInMeters = location.distanceTo(loc2);
            restaurant_distance.setText(String.format("%.0f", distanceInMeters) + "m");
        }

        @Override
        public void onClick(View view) {

            onClickListener.onClick(view, getAdapterPosition());
        }

    }
    public interface RecyclerViewClickListener {
        void onClick(View view, int position);
    }
}
