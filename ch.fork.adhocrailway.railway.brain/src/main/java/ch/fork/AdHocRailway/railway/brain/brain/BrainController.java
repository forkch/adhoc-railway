package ch.fork.AdHocRailway.railway.brain.brain;

import com.google.common.collect.Lists;
import jssc.*;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
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

    public static void main(final String[] args) {
        final BrainController instance2 = BrainController.getInstance();

        List<String> availableSerialPortsAsString = instance2.getAvailableSerialPortsAsString();
        System.out.println(availableSerialPortsAsString);
        instance2.connect(availableSerialPortsAsString.get(0));
        instance2.addBrainListener(new BrainListener() {
            @Override
            public void sentMessage(String sentMessage) {
            }

            @Override
            public void receivedMessage(String receivedMessage) {

            }

            @Override
            public void brainReset(String receivedMessage) {

            }
        });

        for (int i = 0; i < 1000; i++) {
            instance2.write("XSTOP");
            instance2.write("XGO");
        }
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
            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN | SerialPort.FLOWCONTROL_RTSCTS_OUT);

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

        final List<String> ports = new ArrayList<>();

        String[] portNames = SerialPortList.getPortNames();
        ports.addAll(Arrays.asList(portNames));

        LOGGER.info("serialport names: " + ports);
        return ports;
    }

    public void addBrainListener(final BrainListener listener) {
        listeners.add(listener);
    }

    public void removeBrainListener(final BrainListener listener) {
        listeners.remove(listener);
    }

    private void checkForReset(String receivedMessage) {
        if (StringUtils.startsWith(receivedMessage, "XRS")) {
            processBrainResetMessage(receivedMessage);
        }
    }

    private void processBrainResetMessage(String receivedMessage) {
        for (final BrainListener listener : listeners) {
            listener.brainReset(receivedMessage);
        }
    }

    /**
     * Handles the input coming from the serial port. A new line character is
     * treated as the end of a block in this example.
     */
    public class SerialReader implements SerialPortEventListener {
        List<Byte> inputBuffer = Lists.newArrayList();

        @Override
        public void serialEvent(SerialPortEvent serialPortEvent) {
            if (serialPortEvent.isRXCHAR()) {//If data is available
                int bytesToRead = serialPortEvent.getEventValue();
                if (bytesToRead > 0) {//Check bytes count in the input buffer
                    try {
                        byte buffer[] = serialPort.readBytes(bytesToRead);

                        processBuffer(buffer);
                    } catch (SerialPortException ex) {
                        LOGGER.error("error receiving data from serialport", ex);
                    }
                }
            }
        }

        private void processBuffer(byte[] buffer) {

            for (byte b : buffer) {
                inputBuffer.add(b);
            }
            int start = 0;
            for (int i = 0; i < inputBuffer.size(); i++) {
                if (inputBuffer.get(i) == 0x0d) {

                    final List<Byte> fullCommand = inputBuffer.subList(start, i + 1);
                    inputBuffer = inputBuffer.subList(i + 1, inputBuffer.size());

                    Byte[] bytes = fullCommand.toArray(new Byte[fullCommand.size()]);

                    String completeString = new String(ArrayUtils.toPrimitive(bytes), Charset.forName("US-ASCII"));
                    String[] completeStringLines = completeString.split("\\r+\\n?");
                    for (String completeStringLine : completeStringLines) {

                        checkForReset(completeStringLine.trim());
                        for (final BrainListener listener : listeners) {
                            listener.receivedMessage(completeStringLine.trim());
                        }
                    }
                    LOGGER.debug(completeString);
                    start = i + 1;
                }
            }
        }
    }

}