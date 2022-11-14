package tech.android.jobsharing.models;

/***
 * Created by HoangRyan aka LilDua on 11/13/2022.
 */
public class User {
    public String name, email, studentId, image, password;

    public User(String name, String email, String studentId, String image, String password) {
        this.name = name;
        this.email = email;
        this.studentId = studentId;
        this.image = image;
        this.password = password;
    }
}
