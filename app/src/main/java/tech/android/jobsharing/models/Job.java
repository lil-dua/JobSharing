package tech.android.jobsharing.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Job implements Parcelable {
    private String position, company,imageCompany, workplaceType, location, jobType,numberEmployee,experience, description, jobId, userId, dateCreated;


    protected Job(Parcel in) {
        position = in.readString();
        company = in.readString();
        imageCompany = in.readString();
        workplaceType = in.readString();
        location = in.readString();
        jobType = in.readString();
        numberEmployee = in.readString();
        experience = in.readString();
        description = in.readString();
        jobId = in.readString();
        userId = in.readString();
        dateCreated = in.readString();
    }

    public static final Creator<Job> CREATOR = new Creator<Job>() {
        @Override
        public Job createFromParcel(Parcel in) {
            return new Job(in);
        }

        @Override
        public Job[] newArray(int size) {
            return new Job[size];
        }
    };

    public Job(String position, String company, String imageCompany, String workplaceType, String location, String jobType,String numberEmployee,String experience, String description, String jobId, String userId, String dateCreated) {
        this.position = position;
        this.company = company;
        this.imageCompany = imageCompany;
        this.workplaceType = workplaceType;
        this.location = location;
        this.jobType = jobType;
        this.numberEmployee =numberEmployee;
        this.experience = experience;
        this.description = description;
        this.jobId = jobId;
        this.userId = userId;
        this.dateCreated = dateCreated;
    }

    public Job() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(position);
        dest.writeString(company);
        dest.writeString(imageCompany);
        dest.writeString(workplaceType);
        dest.writeString(location);
        dest.writeString(jobType);
        dest.writeString(numberEmployee);
        dest.writeString(experience);
        dest.writeString(description);
        dest.writeString(jobId);
        dest.writeString(userId);
        dest.writeString(dateCreated);
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getImageCompany() {
        return imageCompany;
    }

    public void setImageCompany(String imageCompany) {
        this.imageCompany = imageCompany;
    }

    public String getWorkplaceType() {
        return workplaceType;
    }

    public void setWorkplaceType(String workplaceType) {
        this.workplaceType = workplaceType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public String getNumberEmployee() {
        return numberEmployee;
    }

    public void setNumberEmployee(String numberEmployee) {
        this.numberEmployee = numberEmployee;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }
}
