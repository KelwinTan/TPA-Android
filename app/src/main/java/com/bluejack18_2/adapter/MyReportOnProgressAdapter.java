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
import com.bluejack18_2.activity.ReportDetailActivity;
import com.bluejack18_2.model.Report;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MyReportOnProgressAdapter extends RecyclerView.Adapter<MyReportOnProgressAdapter.ViewHolder>{

    private Context context;
    private ArrayList<Report> reports;

    public MyReportOnProgressAdapter(Context context) {
        this.context = context;
        reports = new ArrayList<>();
    }

    public void setReports(ArrayList<Report> reports) { this.reports = reports; }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_my_report, viewGroup, false);
        return new MyReportOnProgressAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position) {
        Picasso.get().load(reports.get(position).getImageUrl()).resize(350,175).centerCrop().into(viewHolder.imageViewReport);
        viewHolder.textReportTitle.setText(reports.get(position).getReportTitle());
        viewHolder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ReportDetailActivity.class);
                intent.putExtra("My_Report", reports.get(position));
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() { return reports.size(); }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textReportTitle;
        ImageView imageViewReport;
        CardView cardView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cv_my_report);
            textReportTitle = itemView.findViewById(R.id.text_report_title);
            imageViewReport = itemView.findViewById(R.id.image_view_report);
        }
    }

}
