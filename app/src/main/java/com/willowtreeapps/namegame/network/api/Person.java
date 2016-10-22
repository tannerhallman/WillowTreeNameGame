package com.willowtreeapps.namegame.network.api;

import android.os.Parcel;
import android.os.Parcelable;

public class Person implements Parcelable {

    private final String name;
    private final String url;

    public Person(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        String n = name;
        n = n.replaceAll("2", "");
        n = n.replaceAll("3", "");
        n = n.replaceAll("-", "");
        return n;
    }

    public String getUrl() {
        //hard coded url needs to be replaced with that is parsed
        // from the website since they all vary now and dont
        // share a common url prefix string
        return url;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // need to write both name AND url now
        dest.writeString(this.name);
        dest.writeString(this.url);
    }

    protected Person(Parcel in) {
        this.name = in.readString();
        this.url = in.readString();
    }

    public static final Creator<Person> CREATOR = new Creator<Person>() {
        public Person createFromParcel(Parcel source) {
            return new Person(source);
        }

        public Person[] newArray(int size) {
            return new Person[size];
        }
    };
}
