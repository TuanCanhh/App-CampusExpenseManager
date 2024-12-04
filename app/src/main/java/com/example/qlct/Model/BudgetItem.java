package com.example.qlct.Model;

import android.os.Parcel;
import android.os.Parcelable;

public class BudgetItem implements Parcelable {

    private String id;
    private String group;
    private String amount;
    private String date;
    private String type;  // Thêm trường type

    // Constructor không tham số (để Firebase có thể deserialize)
    public BudgetItem() {
        // Constructor mặc định yêu cầu Firestore
    }

    // Constructor với các tham số
    public BudgetItem(String id, String group, String amount, String date, String type) {
        this.id = id;
        this.group = group;
        this.amount = amount;
        this.date = date;
        this.type = type;  // Khởi tạo type
    }

    // Implement Parcelable interface
    protected BudgetItem(Parcel in) {
        id = in.readString();
        group = in.readString();
        amount = in.readString();
        date = in.readString();
        type = in.readString();  // Đọc type từ Parcel
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(group);
        dest.writeString(amount);
        dest.writeString(date);
        dest.writeString(type);  // Ghi type vào Parcel
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<BudgetItem> CREATOR = new Creator<BudgetItem>() {
        @Override
        public BudgetItem createFromParcel(Parcel in) {
            return new BudgetItem(in);
        }

        @Override
        public BudgetItem[] newArray(int size) {
            return new BudgetItem[size];
        }
    };

    // Getters and setters
    public String getId() {
        return id;
    }

    public String getGroup() {
        return group;
    }

    public String getAmount() {
        return amount;
    }

    public String getDate() {
        return date;
    }

    public String getType() {
        return type;  // Getter cho type
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setType(String type) {
        this.type = type;  // Setter cho type
    }
}
