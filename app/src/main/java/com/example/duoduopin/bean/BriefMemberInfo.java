package com.example.duoduopin.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class BriefMemberInfo implements Parcelable {
    public static final Creator<BriefMemberInfo> CREATOR = new Creator<BriefMemberInfo>() {
        @Override
        public BriefMemberInfo createFromParcel(Parcel in) {
            return new BriefMemberInfo(in);
        }

        @Override
        public BriefMemberInfo[] newArray(int size) {
            return new BriefMemberInfo[size];
        }
    };
    private final String nickname;
    private final String credit;
    private final String userId;
    private final String headpath;

    public BriefMemberInfo(String nickname, String credit, String userId, String headpath) {
        this.nickname = nickname;
        this.credit = credit;
        this.userId = userId;
        this.headpath = headpath;
    }

    protected BriefMemberInfo(Parcel in) {
        nickname = in.readString();
        credit = in.readString();
        userId = in.readString();
        headpath = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(nickname);
        dest.writeString(credit);
        dest.writeString(userId);
        dest.writeString(headpath);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getNickname() {
        return nickname;
    }

    public String getCredit() {
        return credit;
    }

    public String getUserId() {
        return userId;
    }

    public String getHeadpath() {
        return headpath;
    }
}
