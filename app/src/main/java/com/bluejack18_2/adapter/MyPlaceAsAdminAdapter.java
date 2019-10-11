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
import com.bluejack18_2.activity.PlaceInformationActivity;
import com.bluejack18_2.model.AddedPlace;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MyPlaceAsAdminAdapter extends RecyclerView.Adapter<MyPlaceAsAdminAdapter.ViewHolder> {

    private Context context;
    private ArrayList<AddedPlace> addedPlaces;

    public MyPlaceAsAdminAdapter(Context context) {
        this.context = context;
        addedPlaces = new ArrayList<>();
    }

    public void setAddedPlaces(ArrayList<AddedPlace> addedPlaces) { this.addedPlaces = addedPlaces; }

    @NonNull
    @Override
    public MyPlaceAsAdminAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_my_place, viewGroup, false);
        return new MyPlaceAsAdminAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyPlaceAsAdminAdapter.ViewHolder viewHolder, final int position) {
        viewHolder.textPlaceName.setText(addedPlaces.get(position).getPlaceName());
        Picasso.get().load(addedPlaces.get(position).getImageUrl()).resize(350,175).centerCrop().into(viewHolder.imageViewPlace);
        viewHolder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PlaceInformationActivity.class);
                intent.putExtra("My_Place", addedPlaces.get(position));
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() { return addedPlaces.size(); }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textPlaceName;
        ImageView imageViewPlace;
        CardView cardView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textPlaceName = itemView.findViewById(R.id.text_place_name);
            imageViewPlace = itemView.findViewById(R.id.image_view_place);
            cardView = itemView.findViewById(R.id.cv_my_place);
        }
    }
}
