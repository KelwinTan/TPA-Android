package com.bluejack18_2.model;

import android.os.Parcel;
import android.os.Parcelable;

public class AddedPlace implements Parcelable {

    private String id;
    private String placeId;
    private String userId;
    private String roleName;
    private String placeName;
    private String imageUrl;

    public AddedPlace(){ }

    public AddedPlace(String placeId, String userId, String roleName) {
        this.placeId = placeId;
        this.userId = userId;
        this.roleName = roleName;
    }

    protected AddedPlace(Parcel in) {
        id = in.readString();
        placeId = in.readString();
        userId = in.readString();
        roleName = in.readString();
        placeName = in.readString();
        imageUrl = in.readString();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(placeId);
        parcel.writeString(userId);
        parcel.writeString(roleName);
        parcel.writeString(placeName);
        parcel.writeString(imageUrl);
    }

    public static final Creator<AddedPlace> CREATOR = new Creator<AddedPlace>() {
        @Override
        public AddedPlace createFromParcel(Parcel in) {
            return new AddedPlace(in);
        }
        @Override
        public AddedPlace[] newArray(int size) {
            return new AddedPlace[size];
        }
    };

    @Override
    public int describeContents() { return 0; }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getPlaceId() { return placeId; }

    public void setPlaceId(String placeId) { this.placeId = placeId; }

    public String getUserId() { return userId; }

    public void setUserId(String userId) { this.userId = userId; }

    public String getRoleName() { return roleName; }

    public void setRoleName(String roleName) { this.roleName = roleName; }

    public String getPlaceName() { return placeName; }

    public void setPlaceName(String placeName) { this.placeName = placeName; }

    public String getImageUrl() { return imageUrl; }

    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}


