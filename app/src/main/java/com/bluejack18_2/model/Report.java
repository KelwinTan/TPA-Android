package com.bluejack18_2.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Report implements Parcelable {

    private String id;
    private String placeId;
    private String userId;
    private String imageUrl;
    private String reportTitle;
    private String reportDescription;
    private String reportStatus;

    public Report(){ }

    public Report(String placeId, String userId, String imageUrl, String reportTitle, String reportDescription, String reportStatus) {
        this.placeId = placeId;
        this.userId = userId;
        this.imageUrl = imageUrl;
        this.reportTitle = reportTitle;
        this.reportDescription = reportDescription;
        this.reportStatus = reportStatus;
    }

    protected Report(Parcel in) {
        id = in.readString();
        placeId = in.readString();
        userId = in.readString();
        imageUrl = in.readString();
        reportTitle = in.readString();
        reportDescription = in.readString();
        reportStatus = in.readString();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(placeId);
        parcel.writeString(userId);
        parcel.writeString(imageUrl);
        parcel.writeString(reportTitle);
        parcel.writeString(reportDescription);
        parcel.writeString(reportStatus);
    }

    @Override
    public int describeContents() { return 0; }

    public static final Creator<Report> CREATOR = new Creator<Report>() {
        @Override
        public Report createFromParcel(Parcel in) {
            return new Report(in);
        }
        @Override
        public Report[] newArray(int size) {
            return new Report[size];
        }
    };

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getPlaceId() { return placeId; }

    public void setPlaceId(String placeId) { this.placeId = placeId; }

    public String getUserId() { return userId; }

    public void setUserId(String userId) { this.userId = userId; }

    public String getImageUrl() { return imageUrl; }

    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getReportTitle() { return reportTitle; }

    public void setReportTitle(String reportTitle) { this.reportTitle = reportTitle; }

    public String getReportDescription() { return reportDescription; }

    public void setReportDescription(String reportDescription) { this.reportDescription = reportDescription; }

    public String getReportStatus() { return reportStatus; }

    public void setReportStatus(String reportStatus) { this.reportStatus = reportStatus; }
}
