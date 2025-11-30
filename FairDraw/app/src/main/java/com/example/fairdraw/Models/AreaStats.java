package com.example.fairdraw.Models;

public class AreaStats {
    private double lat;
    private double lng;
    private int count;

    public AreaStats(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
        this.count = 0;
    }

    public void increment() {
        count++;
    }

    public double getLat() { return lat; }
    public double getLng() { return lng; }
    public int getCount() { return count; }
}
