package com.bluejack18_2.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bluejack18_2.R;
import com.bluejack18_2.activity.PlaceDetailActivity;
import com.bluejack18_2.model.Place;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class HomeAllPlaceAdapter extends RecyclerView.Adapter<HomeAllPlaceAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Place> places;

    public HomeAllPlaceAdapter(Context context){
        this.context = context;
        places = new ArrayList<>();
    }

    public void setPlaces(ArrayList<Place> places) {
        this.places = places;
    }

    @NonNull
    @Override
    public HomeAllPlaceAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_all_place_home, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeAllPlaceAdapter.ViewHolder viewHolder, final int position) {
        viewHolder.textPlaceName.setText(places.get(position).getPlaceName());
        Picasso.get().load(places.get(position).getImageUrl()).resize(350,175).centerCrop().into(viewHolder.imageViewPlace);
        viewHolder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PlaceDetailActivity.class);
                intent.putExtra("Place", places.get(position));
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return places.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textPlaceName;
        ImageView imageViewPlace;
        CardView cardView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textPlaceName = itemView.findViewById(R.id.text_place_name);
            imageViewPlace = itemView.findViewById(R.id.image_view_place);
            cardView = itemView.findViewById(R.id.cv_all_home_place);
        }
    }


}
