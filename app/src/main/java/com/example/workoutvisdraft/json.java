package com.example.workoutvisdraft;

public class json {
    int cycles[][];
    float predictions[];

    public json(int[][] cycles, float[] predictions) {
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
