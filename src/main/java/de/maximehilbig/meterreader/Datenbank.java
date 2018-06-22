package de.maximehilbig.meterreader;

import org.postgresql.ds.PGSimpleDataSource;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Datenbank {
    public Datenbank() {
    }


    private Connection connection;


    public void createConnection() throws SQLException {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setDatabaseName("meterreader");
        dataSource.setServerName("localhost");
        dataSource.setPortNumber(5432);
        dataSource.setUser("postgres");
        this.connection = dataSource.getConnection();


    }

    public boolean save(Meter meter) throws SQLException {
        PreparedStatement ps = this.connection.prepareStatement("INSERT INTO meter_data (meter_id, \"current_time\", energy, \"power\") VALUES(?,?,?,?)");
        ps.setString(1, meter.getMeterId());
        ps.setLong(2, meter.getCurrentTime());
        ps.setDouble(3, meter.getEnergy());
        ps.setDouble(4, meter.getPower());

        int rowsModified = ps.executeUpdate();
        ps.close();
        return true;

    }

    public long conversion(String x) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        LocalDateTime x1 = LocalDateTime.parse(x, formatter);

        String dateTime1 = x1.format(formatter);
        ZoneOffset offset = ZoneOffset.ofHours(+2);
        Instant instant1 = x1.toInstant(offset);
        long currentMilliseconds1 = instant1.toEpochMilli();
        return currentMilliseconds1;


    }

    public List<Meter> timeInterval(long b, long c) throws SQLException {
        PreparedStatement ps = this.connection.prepareStatement("SELECT * FROM meter_data WHERE \"current_time\" BETWEEN ? AND ? ORDER BY \"current_time\" asc");
        ps.setLong(1, b);
        ps.setLong(2, c);

        ResultSet resultSet = ps.executeQuery();
        List<Meter> meters = new ArrayList<>();
        while (resultSet.next()) {
            String inputMeterId = resultSet.getString(1);
            long currentTime = resultSet.getLong(2);
            Double inputEnergy = resultSet.getDouble(3);
            Double inputPower = resultSet.getDouble(4);
            Double inputPower1 = -1.0;
            Double inputPower2 = -1.0;
            Double inputPower3 = -1.0;
            Meter meter1 = new Meter(currentTime, inputMeterId, inputEnergy, inputPower, inputPower1, inputPower2, inputPower3);
            meters.add(meter1);
            System.out.println(meter1);

        }

        resultSet.close();
        ps.close();
        return meters;


    }

    public double mean(List<Meter> meters) {
        double total = 0;
        for (Meter meter : meters) {
            Double i = meter.getPower();
            total += i;
        }
        double mean = total / meters.size();
        System.out.println("Das arithmetische Mittel von P: " + mean);
        return mean;
    }

    public double wastage(List<Meter> meters) {
        // Hier brauche ich das erste und das letzte Elemente der Liste.
        int listLength= meters.size();
        Meter meter1 = meters.get(listLength-1);
        Meter meter2 = meters.get(0);
        double a = meter1.getEnergy();
        double b = meter2.getEnergy();
        double result = a-b;

//        System.out.println("Es wurden " + result + " Wattstunden während diesem Zeitintervall verbraucht." );
         String s = String.format("Es wurden %.3f Wattstunden während diesem Zeitintervall verbraucht.", result);
        System.out.println(s);
         return result;


    }
}