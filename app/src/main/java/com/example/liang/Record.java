package com.example.liang;

public class Record {
    private String date;
    private String time;
    private String location;
    private String temperature;




    public String getDate() {
        return date;
    }

    public String getTime(){
        return time;
    }

    public String getLocation() {
        return location;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
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
}
