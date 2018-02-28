package model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by Lenovo on 2017/12/29.
 */

public class Poetry implements Serializable, Parcelable {
    private String id;
    private String title;
    private String author;
    private String notes;//诗词注释
    private String[] content;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String[] getContent() {
        return content;
    }

    public void setContent(String[] content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Poetry{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", notes='" + notes + '\'' +
                ", content=" + Arrays.toString(content) +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Poetry> CREATOR = new Creator<Poetry>() {

        @Override
        public Poetry createFromParcel(Parcel source) {
            // TODO Auto-generated method stub

            Poetry p = new Poetry();
            p.id = source.readString();
            p.title = source.readString();
            p.author = source.readString();
            p.notes = source.readString();
            source.readStringArray(p.content);
            return p;

        }

        @Override
        public Poetry[] newArray(int size) {
            // TODO Auto-generated method stub
            return new Poetry[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(author);
        dest.writeString(notes);
        dest.writeStringArray(content);
    }
}
