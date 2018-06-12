package de.maximehilbig.meterreader;

public class Meter {

    private String meterId;
    private double energy;
    private double achievement;
    private double achievement1;
    private double achievement2;
    private double achievement3;


    Meter(String inputMeterId, double inputEnergy, double inputAchievement, double inputAchievement1, double inputAchievement2, double inputAchievement3){
        this.meterId = inputMeterId;
        this.energy = inputEnergy;
        this.achievement = inputAchievement;
        this.achievement1 = inputAchievement1;
        this.achievement2 = inputAchievement2;
        this.achievement3 = inputAchievement3;

    }
}
