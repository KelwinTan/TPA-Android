package com.bluejack18_2.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Comment implements Parcelable {

    private String id;
    private String reportId;
    private String userId;
    private String content;
    private String username;

    protected Comment(Parcel in) {
        id = in.readString();
        reportId = in.readString();
        userId = in.readString();
        content = in.readString();
        username = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(reportId);
        dest.writeString(userId);
        dest.writeString(content);
        dest.writeString(username);

    }

    public Comment(String id, String reportId, String userId, String content,String username) {
        this.id = id;
        this.reportId = reportId;
        this.userId = userId;
        this.content = content;
        this.username = username;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Comment> CREATOR = new Creator<Comment>() {
        @Override
        public Comment createFromParcel(Parcel in) {
            return new Comment(in);
        }

        @Override
        public Comment[] newArray(int size) {
            return new Comment[size];
        }
    };

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getReportId() {
        return reportId;
    }

    public String getUserId() {
        return userId;
    }

    public String getContent() {
        return content;
    }

    public String getUsername() { return username; }

    public static Creator<Comment> getCREATOR() {
        return CREATOR;
    }
}
