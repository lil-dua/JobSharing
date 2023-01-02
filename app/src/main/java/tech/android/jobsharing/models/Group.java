package tech.android.jobsharing.models;

import android.os.Parcel;
import android.os.Parcelable;

/***
 * Created by HoangRyan aka LilDua on 12/20/2022.
 */
public class Group implements Parcelable {
    private String groupName, groupDescription, imagePath, groupId, userId,dateCreated,member;

    public Group(String groupName, String groupDescription, String imagePath,String groupId,String userId, String member,String dateCreated) {
        this.groupName = groupName;
        this.groupDescription = groupDescription;
        this.imagePath = imagePath;
        this.groupId = groupId;
        this.userId = userId;
        this.member = member;
        this.dateCreated = dateCreated;
    }
    public Group(){

    }


    protected Group(Parcel in) {
        groupName = in.readString();
        groupDescription = in.readString();
        imagePath = in.readString();
        groupId = in.readString();
        userId = in.readString();
        dateCreated = in.readString();
        member = in.readString();
    }

    public static final Creator<Group> CREATOR = new Creator<Group>() {
        @Override
        public Group createFromParcel(Parcel in) {
            return new Group(in);
        }

        @Override
        public Group[] newArray(int size) {
            return new Group[size];
        }
    };

    @Override
    public String toString() {
        return "Group{" +
                "groupName='" + groupName + '\'' +
                ", groupDescription='" + groupDescription + '\'' +
                ", imagePath='" + imagePath + '\'' +
                ", member=" + member +
                '}';
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupDescription() {
        return groupDescription;
    }

    public void setGroupDescription(String groupDescription) {
        this.groupDescription = groupDescription;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMember() {
        return member;
    }

    public void setMember(String member) {
        this.member = member;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(groupName);
        dest.writeString(groupDescription);
        dest.writeString(imagePath);
        dest.writeString(groupId);
        dest.writeString(userId);
        dest.writeString(dateCreated);
        dest.writeString(member);
    }
}
