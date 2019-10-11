package com.bluejack18_2.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class Place implements Parcelable {

    private String id;
    private String imageUrl;
    private String placeName;
    private String placeDescription;
    private String guestCode;
    private String adminCode;

    public Place(){ }

    public Place(String imageUrl, String placeName, String placeDescription, String guestCode, String adminCode) {
        this.imageUrl = imageUrl;
        this.placeName = placeName;
        this.placeDescription = placeDescription;
        this.guestCode = guestCode;
        this.adminCode = adminCode;
    }

    protected Place(Parcel in) {
        id = in.readString();
        imageUrl = in.readString();
        placeName = in.readString();
        placeDescription = in.readString();
        guestCode = in.readString();
        adminCode = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(imageUrl);
        dest.writeString(placeName);
        dest.writeString(placeDescription);
        dest.writeString(guestCode);
        dest.writeString(adminCode);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Place> CREATOR = new Creator<Place>() {
        @Override
        public Place createFromParcel(Parcel in) {
            return new Place(in);
        }

        @Override
        public Place[] newArray(int size) {
            return new Place[size];
        }
    };

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getImageUrl() { return imageUrl; }

    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getPlaceName() { return placeName; }

    public void setPlaceName(String placeName) { this.placeName = placeName; }

    public String getPlaceDescription() { return placeDescription; }

    public void setPlaceDescription(String placeDescription) { this.placeDescription = placeDescription; }

    public String getGuestCode() { return guestCode; }

    public void setGuestCode(String guestCode) { this.guestCode = guestCode; }

    public String getAdminCode() { return adminCode; }

    public void setAdminCode(String adminCode) { this.adminCode = adminCode; }
}
