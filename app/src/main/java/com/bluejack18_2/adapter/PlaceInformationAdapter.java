package com.bluejack18_2.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bluejack18_2.R;
import com.bluejack18_2.activity.ReportDetailActivity;
import com.bluejack18_2.model.Report;

import java.util.ArrayList;

public class PlaceInformationAdapter extends RecyclerView.Adapter<PlaceInformationAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Report> reports;
    private SharedPreferences sharedPreferences;
    private String userLoggedInId;

    public PlaceInformationAdapter(Context context) {
        this.context = context;
        reports = new ArrayList<>();
    }

    public void setReports(ArrayList<Report> reports) { this.reports = reports; }

    @NonNull
    @Override
    public PlaceInformationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_place_information_report, viewGroup, false);
        return new PlaceInformationAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceInformationAdapter.ViewHolder viewHolder, final int position) {
        sharedPreferences = context.getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        userLoggedInId = sharedPreferences.getString("Id", null);

        viewHolder.textReportTitle.setText(reports.get(position).getReportTitle());
        String desc = "Description: " + reports.get(position).getReportDescription();
        viewHolder.textReportDescription.setText(desc);
        String status = "Status: " + reports.get(position).getReportStatus();
        viewHolder.textReportStatus.setText(status);
        if(!userLoggedInId.equals(reports.get(position).getUserId())) {
            textYouReportedThis.setVisibility(View.GONE);
        }
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

    private TextView textYouReportedThis;
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textReportTitle, textReportDescription, textReportStatus;
        CardView cardView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textReportTitle = itemView.findViewById(R.id.text_report_title);
            textReportDescription = itemView.findViewById(R.id.text_report_description);
            textReportStatus = itemView.findViewById(R.id.text_report_status);
            textYouReportedThis = itemView.findViewById(R.id.text_you_reported_this);
            cardView = itemView.findViewById(R.id.cv_place_information_report);
        }
    }
}
