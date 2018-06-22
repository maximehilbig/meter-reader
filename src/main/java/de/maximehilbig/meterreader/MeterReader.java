package de.maximehilbig.meterreader;

import gnu.io.NRSerialPort;
import org.openmuc.jsml.structures.*;
import org.openmuc.jsml.structures.responses.SmlGetListRes;
import org.openmuc.jsml.transport.MessageExtractor;

import javax.print.DocFlavor;
import javax.xml.bind.DatatypeConverter;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLOutput;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class MeterReader {

    public static void main(String[] args) throws IOException, SQLException {

        Datenbank datenbank = new Datenbank();
        datenbank.createConnection();
        int a;
        String x, y;
        
        Scanner eingabewert = new Scanner(System.in);
        Scanner zeitintervallmin = new Scanner(System.in);
        Scanner zeitintervallmax = new Scanner(System.in);

        MeterReader meterReader = new MeterReader();

        System.out.println("Wenn sie die Daten lesen wollen, tippen sie 1 oder wenn Sie ein Zeitintervall eingeben möchten, tippen Sie 2:  ");
        a = eingabewert.nextInt();
        if (a < 2) {

            meterReader.dataReader(datenbank);

        } else {
            System.out.println("Geben Sie ihr gewünschtes Zeitintervall ein.");
            System.out.println("Zeitintervallmin: ");
            x = zeitintervallmin.nextLine();
            long b = datenbank.conversion(x);
            System.out.println("Zeitintervallmax: ");
            y = zeitintervallmax.nextLine();
            long c = datenbank.conversion(y);
            List <Meter> meters = datenbank.timeInterval(b, c, meterReader);
            datenbank.mean(meters);
            datenbank.wastage(meters);

        }
    }


    public void dataReader(Datenbank datenbank) throws IOException, SQLException {


        InputStream stream = MeterReader.selectInputStream();

        BufferedInputStream bis = new BufferedInputStream(stream);
        DataInputStream dis = new DataInputStream(bis);
        while (true) {

            MessageExtractor messageExtractor = new MessageExtractor(dis, 3000);

            byte[] smlFile = messageExtractor.getSmlMessage();


            ByteArrayInputStream bais = new ByteArrayInputStream(smlFile);
            DataInputStream smlFileDis = new DataInputStream(bais);


            while (smlFileDis.available() > 0) {
                SmlMessage message = new SmlMessage();
                message.decode(smlFileDis);


                ASNObject obj = message.getMessageBody().getChoice();
                if (obj instanceof SmlGetListRes) {
                    SmlGetListRes content = (SmlGetListRes) obj;
//                    System.out.println(content.toString());

                    SmlList values = content.getValList();
                    SmlListEntry[] entries = values.getValListEntry();
                    String inputMeterId = null;
                    double inputEnergy = -1;
                    double inputPower = -1;
                    double inputPower1 = -1;
                    double inputPower2 = -1;
                    double inputPower3 = -1;

                    for (SmlListEntry entry : entries) {
                        String obisCode = getObisCode(entry.getObjName().getValue());
                        ASNObject valueObj = entry.getValue().getChoice();

                        if (obisCode.equals("1-0:96.1.0*255")) {
                            //System.out.println("meterId:" + valueObj.toString());
                            OctetString octetString = (OctetString) valueObj;
                            String converted = convertToString(octetString.getValue());
                            inputMeterId = convertToMeterId(converted);

                        } else if (obisCode.equals("1-0:1.8.0*255") && (valueObj instanceof Unsigned32)) {
                            Unsigned32 unsigned32 = (Unsigned32) valueObj;
                            int x = unsigned32.getVal();
                            // Multizieren der Zahl, damit ich auf Wh komme.
                            //System.out.println("energy: " + x * 0.1 + "Wh");
                            inputEnergy = x * 0.1;


                        } else if (obisCode.equals("1-0:16.7.0*255") && (valueObj instanceof Integer16)) {
                            Integer16 integer16 = (Integer16) valueObj;
                            int i = integer16.getVal();
                            //System.out.println("power:" + i * 0.01);
                            inputPower = i * 0.01;


                        } else if (obisCode.equals("1-0:36.7.0*255") && (valueObj instanceof Integer16)) {
                            Integer16 integer16 = (Integer16) valueObj;
                            int a = integer16.getVal();
                            //System.out.println("power1:" + a * 0.01);
                            inputPower1 = a * 0.01;

                        } else if (obisCode.equals("1.0:56.7.0*255") && (valueObj instanceof Integer16)) {
                            Integer16 integer16 = (Integer16) valueObj;
                            int b = integer16.getVal();
                            //System.out.println("power2:" + b * 0.01);
                            inputPower2 = b * 0.01;


                        } else if (obisCode.equals("1.0.76.7.0*255") && (valueObj instanceof Integer16)) {
                            Integer16 integer16 = (Integer16) valueObj;
                            int c = integer16.getVal();
                            //System.out.println("power3:" + c * 0.01);
                            inputPower3 = c * 0.01;
                        }


                    }
                    Meter meter = new Meter(System.currentTimeMillis(), inputMeterId, inputEnergy, inputPower, inputPower1, inputPower2, inputPower3);
                    System.out.println(meter);
                    datenbank.save(meter);

                }
            }
        }
    }


    static InputStream selectInputStream() throws IOException {
        Set<String> ports = NRSerialPort.getAvailableSerialPorts();
        InputStream inputStream;
        if (ports.size() != 0) {
            for (String port : ports) {
                System.out.println(port);
            }
            String selectedPort = "COM3";
            int baudRate = 9600;
            NRSerialPort serialPort = new NRSerialPort(selectedPort, baudRate);
            serialPort.connect();
            serialPort.notifyOnDataAvailable(true);

            inputStream = serialPort.getInputStream();
        } else {

            inputStream = Files.newInputStream(Paths.get("C:\\Users\\maxim\\IdeaProjects\\meterreader\\src\\main\\resources\\meter-data.out"));
        }
        return inputStream;
    }

    static String getObisCode(byte[] bytes) {


        int arrayLength = bytes.length;
        byte byteValue = bytes[arrayLength - 1];

        int result = Byte.toUnsignedInt(byteValue);
        String s = (bytes[0] + "-" + bytes[1] + ":" + bytes[2] + "." + bytes[3] + "." + bytes[4] + "*" + result);
        //System.out.println(s);
        return s;

    }

    static String convertToString(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            builder.append(String.format("%02x", b));
        }
        String result = builder.toString();
        return result;
    }


    static String convertToMeterId(String hexMeterId) {
        String sparte = hexMeterId.substring(3, 4);

        String hersteller = new String(DatatypeConverter.parseHexBinary(hexMeterId.substring(4, 10)));

        String number = hexMeterId.substring(11, 13);

        String id = String.format("%08d", Long.parseLong(hexMeterId.substring(12), 16));

        StringBuilder builder = new StringBuilder();
        builder.append(sparte);
        builder.append(hersteller);
        builder.append(number);
        builder.append(id);
        String i = builder.toString();
        //System.out.println(i);
        return i;

    }

}



