package com.example.liang;

public class Record {
    private String date;
    private String time;
    private String location;
    private int state;
    private String inOut;

    public String getDate() {
        return date;
    }

    public String getTime(){
        return time;
    }

    public String getLocation() {
        return location;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getInOut() {
        return inOut;
    }

    public void setInOut(String inOut) {
        this.inOut = inOut;
    }
}
