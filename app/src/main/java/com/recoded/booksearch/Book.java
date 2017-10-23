package com.recoded.booksearch;

import android.os.Parcel;
import android.os.Parcelable;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class Book implements Parcelable{

    private String title;
    private ArrayList<String> authors;
    private URL imgUrl;
    private String desc;

    public Book(String title){
        this.title = title;
        authors = new ArrayList<>();
    }

    public void addAuthor(String author){
        this.authors.add(author);
    }

    public ArrayList<String> getAuthors(){
        return authors;
    }

    public String getAuthorsString(){
        String retString = "By";
        for (String author : authors){
            retString += ", " + author;
        }
        return retString;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc){
        this.desc = desc;
    }

    public String getTitle() {
        return title;
    }

     
    public URL getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        try {
            this.imgUrl = new URL(imgUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.title);
        dest.writeStringList(this.authors);
        dest.writeSerializable(this.imgUrl);
        dest.writeString(this.desc);
    }

    protected Book(Parcel in) {
        this.title = in.readString();
        this.authors = in.createStringArrayList();
        this.imgUrl = (URL) in.readSerializable();
        this.desc = in.readString();
    }

    public static final Creator<Book> CREATOR = new Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel source) {
            return new Book(source);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };
}
