package com.example.workoutvisdraft;

public class APIRes {
    int cycles[][];
    float predictions[];
    json json;

    public void setJson(com.example.workoutvisdraft.json json) {
        this.json = json;
    }

    public com.example.workoutvisdraft.json getJson() {
        return json;
    }

    public APIRes(int[][] cycles, float[] predictions) {
        this.cycles = cycles;
        this.predictions = predictions;
    }

    public void setCycles(int[][] cycles) {
        this.cycles = cycles;
    }

    public void setPredictions(float[] predictions) {
        this.predictions = predictions;
    }

    public int[][] getCycles() {
        return cycles;
    }

    public float[] getPredictions() {
        return predictions;
    }
}
