package ch.fork.AdHocRailway.services.impl.rest;

import com.google.gson.annotations.Expose;

/**
 * Created by fork on 4/5/14.
 */
public class AdHocServerError {
    @Expose
    private String msg;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
