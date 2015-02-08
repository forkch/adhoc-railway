package ch.fork.AdHocRailway.railway.brain.brain;

import jssc.*;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class BrainController {

    private static final Logger LOGGER = Logger
            .getLogger(BrainController.class);
    private static final BrainController INSTANCE = new BrainController();
    private final List<BrainListener> listeners = new ArrayList<BrainListener>();


    private boolean connected = false;
    private SerialPort serialPort;

    private BrainController() {
        super();
    }

    public static BrainController getInstance() {
        return INSTANCE;
    }

    public static void main(final String[] args) throws IOException {
        final BrainController instance2 = BrainController.getInstance();

        List<String> availableSerialPortsAsString = instance2.getAvailableSerialPortsAsString();
        instance2.connect(availableSerialPortsAsString.get(0));
        System.in.read();
        instance2.write("XSTOP");
        System.in.read();
        instance2.write("XGO");
        System.in.read();
        instance2.disconnect();
    }

    public void connect(final String portName) {
            serialPort = new SerialPort(portName);
        try {
            serialPort.openPort();//Open serial port
        serialPort.setParams(230400,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);


            serialPort.addEventListener(new SerialReader());
            connected = true;
        } catch (SerialPortException e) {
            throw new BrainException(e.getExceptionType() + ": "
                    + portName, e);
        }

    }

    public void disconnect() {

        if (!connected) {
            return;
        }
        try {
            serialPort.closePort();
        } catch (final Exception e) {
            throw new BrainException(
                    "error while closing the connection to the brain");
        } finally {
            connected = false;
        }
    }

    public void write(final String str) throws BrainException {

        LOGGER.info(str);
        try {
            final byte[] bytes = str.getBytes(Charset.forName("US-ASCII"));
            serialPort.writeBytes(bytes);
            serialPort.writeInt(0x0d);
            for (final BrainListener listener : listeners) {
                listener.sentMessage(str);
            }
        } catch (SerialPortException e) {
            throw new BrainException("error writing " + str + " to the brain", e);
        }
    }

    public List<String> getAvailableSerialPortsAsString() {

        final List<String> ports = new ArrayList<String>();

        String[] portNames = SerialPortList.getPortNames();
        for (int i = 0; i < portNames.length; i++) {
            ports.add(portNames[i]);
        }

        LOGGER.info("serialport names: " + ports);
        return ports;
    }

    public void addBrainListener(final BrainListener listener) {
        listeners.add(listener);
    }

    public void removeBrainListener(final BrainListener listener) {
        listeners.remove(listener);
    }

    /**
     * Handles the input coming from the serial port. A new line character is
     * treated as the end of a block in this example.
     */
    public class SerialReader implements SerialPortEventListener {
        private StringBuilder receivedString = new StringBuilder();

        @Override
        public void serialEvent(SerialPortEvent serialPortEvent) {
            int data;
            if(serialPortEvent.isRXCHAR()){//If data is available
                int bytesToRead = serialPortEvent.getEventValue();
                if(bytesToRead > 0){//Check bytes count in the input buffer
                    try {
                        byte buffer[] = serialPort.readBytes(serialPortEvent.getEventValue());

                        receivedString.append(new String(buffer, "US-ASCII"));

                        if (buffer[buffer.length-1] == 0x0d) {

                            final String completeString =receivedString.toString();
                            receivedString = new StringBuilder();

                            for (final BrainListener listener : listeners) {
                                listener.receivedMessage(completeString);
                            }
                            LOGGER.debug(completeString);
                        }

                    }
                    catch (SerialPortException ex) {
                        LOGGER.error("error receiving data from serialport", ex);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }

}