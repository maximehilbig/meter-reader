package de.maximehilbig.meterreader;

import gnu.io.NRSerialPort;
import org.openmuc.jsml.structures.*;
import org.openmuc.jsml.structures.responses.SmlGetListRes;
import org.openmuc.jsml.transport.MessageExtractor;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLOutput;
import java.util.Set;

public class MeterReader {

    public static void main(String[] args) throws IOException {
//        Set<String> ports = NRSerialPort.getAvailableSerialPorts();
//        for (String port : ports) {
//            System.out.println(port);
//        }
//        String selectedPort = "COM3";
//        int baudRate = 9600;
//        NRSerialPort serialPort = new NRSerialPort(selectedPort, baudRate);
//        serialPort.connect();
//        serialPort.notifyOnDataAvailable(true);
//
//        InputStream inputStream = serialPort.getInputStream();

        InputStream inputStream = Files.newInputStream(Paths.get("C:\\Users\\maxim\\IdeaProjects\\meterreader\\src\\main\\resources\\meter-data.out"));

        BufferedInputStream bis = new BufferedInputStream(inputStream);
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
                    System.out.println(content.toString());

                    SmlList values = content.getValList();
                    SmlListEntry[] entries = values.getValListEntry();
                    for (SmlListEntry entry : entries) {
                        String obisCode = getObisCode(entry.getObjName().getValue());
                        ASNObject valueObj = entry.getValue().getChoice();
                    }
                }
            }
        }
    }

    static String getObisCode(byte[] bytes) {


        int arrayLength = bytes.length;
        byte byteValue = bytes[arrayLength-1];

        int result = Byte.toUnsignedInt(byteValue);
        //System.out.println(result);
        String s = (bytes[0]+"-"+bytes[1]+":" + bytes[2]+ "." +bytes[3]+"." + bytes [4]+  "*" + result);
        //System.out.println(s);
        System.out.println(bytes[0]+"-"+bytes[1]+":" + bytes[2]+ "." +bytes[3]+"." + bytes [4]+  "*" + result);
        return s;

    }

    public boolean compare(List[]) {
        for
    }
}
