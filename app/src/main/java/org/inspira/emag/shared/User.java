package org.inspira.emag.shared;

import android.content.Context;

/**
 * Created by jcapiz on 29/02/16.
 */
public class User implements Shareable {

    private String email;
    private String nickname;
    private String pass;
    private long dateOfBirth;

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public long getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(long dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    @Override
    public void commitEntry(Context ctx) {

    }
}
