package de.maximehilbig.meterreader;

public class Meter {

    private long currentTime;
    private String meterId;
    private double energy;
    private double power;
    private double power1;
    private double power2;
    private double power3;


    Meter(long currentTime, String inputMeterId, double inputEnergy, double inputPower, double inputPower1, double inputPower2, double inputPower3){
        this.currentTime = currentTime;
        this.meterId = inputMeterId;
        this.energy = inputEnergy;
        this.power = inputPower;
        this.power1 = inputPower1;
        this.power2 = inputPower2;
        this.power3 = inputPower3;

    }

    @Override
    public String toString() {
//      String end= ("Current Time in milliseconds = "+System.currentTimeMillis()+"  Id:"+meterId+", E:"+ energy+", P:"+power+", P1:"+power1+", P2:"+ power2+", P3:"+ power3  );

        return String.format("Current Time in milliseconds: %d Id: %s E: %.3f P: %.3f P1: %.1f P2: %.1f P3: %.1fg", currentTime,meterId, energy,power,power1, power2, power3);

    }
    String getMeterId() {
        return meterId;
    }

    long getCurrentTime() {
        return currentTime;
    }

    double getEnergy() {
        return energy;
    }

    double getPower() {
        return power;
    }
}
