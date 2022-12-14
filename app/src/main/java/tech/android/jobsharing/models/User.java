package tech.android.jobsharing.models;

import android.os.Parcel;
import android.os.Parcelable;

/***
 * Created by HoangRyan aka LilDua on 11/13/2022.
 */
public class User implements Parcelable {
    private String name, email, studentId,phone,date,
            description,link, image, password,userId,fcmToken;

    public User(){

    }
    public User(String name, String email, String studentId,String phone,String date,String description,
                String link, String image, String password,String userId,String fcmToken){
        this.name = name;
        this.email = email;
        this.studentId = studentId;
        this.phone = phone;
        this.date = date;
        this.description = description;
        this.link = link;
        this.image = image;
        this.password = password;
        this.userId = userId;
        this.fcmToken = fcmToken;
    }

    protected User(Parcel in) {
        name = in.readString();
        email = in.readString();
        studentId = in.readString();
        image = in.readString();
        password = in.readString();
        userId = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(email);
        dest.writeString(studentId);
        dest.writeString(image);
        dest.writeString(password);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getPhone(){return phone;}
    public void setPhone(String phone){this.phone = phone;}

    public String getDate(){return date;}
    public void setDate(String date){this.date = date;}

    public String getDescription(){return description;}
    public void setDescription(String description){this.description = description;}

    public String getLink(){return link;}
    public void setLink(String link){this.link = link;}

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFcmToken(){return fcmToken;}
    public void setFcmToken(String fcmToken){this.fcmToken = fcmToken;}
}
