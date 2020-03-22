/*
 * Created on 26.09.2005
 *
 */

package de.dermoba.srcp.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import de.dermoba.srcp.common.SocketReader;
import de.dermoba.srcp.common.SocketWriter;
import de.dermoba.srcp.common.TokenizedLine;
import de.dermoba.srcp.common.exception.SRCPException;
import de.dermoba.srcp.common.exception.SRCPHostNotFoundException;
import de.dermoba.srcp.common.exception.SRCPIOException;
import de.dermoba.srcp.common.exception.SRCPUnsufficientDataException;
import de.dermoba.srcp.common.exception.SRCPWrongValueException;
import de.dermoba.srcp.devices.listener.CRCFInfoListener;
import de.dermoba.srcp.devices.listener.FBInfoListener;
import de.dermoba.srcp.devices.listener.GAInfoListener;
import de.dermoba.srcp.devices.listener.GLInfoListener;
import de.dermoba.srcp.devices.listener.GMInfoListener;
import de.dermoba.srcp.devices.listener.LOCKInfoListener;
import de.dermoba.srcp.devices.listener.POWERInfoListener;
import de.dermoba.srcp.devices.listener.SERVERInfoListener;
import de.dermoba.srcp.devices.listener.SMInfoListener;
import de.dermoba.srcp.model.locomotives.SRCPLocomotiveDirection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InfoChannel implements Runnable {
	private static final Logger LOGGER = LoggerFactory.getLogger(InfoChannel.class);

	private static final int INFO_SET = 100;

	private static final int INFO_INIT = 101;

	private static final int INFO_TERM = 102;

	private Socket socket = null;

	private SocketWriter out = null;

	private SocketReader in = null;

	private final String serverName;

	private final int serverPort;

	private int id;

	private final Collection<FBInfoListener> FBListeners = new ArrayList<FBInfoListener>();

	private final Collection<GAInfoListener> GAListeners = new ArrayList<GAInfoListener>();

	private final Collection<GLInfoListener> GLListeners = new ArrayList<GLInfoListener>();

	private final Collection<LOCKInfoListener> LOCKListeners = new ArrayList<LOCKInfoListener>();

	private final Collection<POWERInfoListener> POWERListeners = new ArrayList<POWERInfoListener>();

	private final Collection<SERVERInfoListener> SERVERListeners = new ArrayList<SERVERInfoListener>();

	private final Collection<SMInfoListener> SMListeners = new ArrayList<SMInfoListener>();

	private final Collection<GMInfoListener> GMListeners = new ArrayList<GMInfoListener>();

	private final Collection<CRCFInfoListener> CRCFListeners = new ArrayList<CRCFInfoListener>();

	// private List<DESCRIPTIONInfoListener> DESCRIPTIONListeners;
	// private List<SESSIONInfoListener> SESSIONListeners;
	private final Collection<InfoDataListener> listeners = new ArrayList<InfoDataListener>();

	private Thread infoThread;

	private final CRCFHandler CRCFHandle = new CRCFHandler();

	/**
	 * creates a new SRCP connection on the info channel to handle all info
	 * communication.
	 * 
	 * @param pServerName
	 *            server name or IP address
	 * @param pServerPort
	 *            TCP port number
	 * @throws SRCPException
	 */
	public InfoChannel(final String pServerName, final int pServerPort) {
		serverName = pServerName;
		serverPort = pServerPort;
		GMListeners.add(CRCFHandle);
	}

	public void connect() throws SRCPException {
		try {

			socket = new Socket();
			socket.connect(new InetSocketAddress(serverName, serverPort), 5000);
			out = new SocketWriter(socket);
			in = new SocketReader(socket);

			// Application protocol layer initialization
			String s = in.read(); // Ignore welcome message
			send("SET CONNECTIONMODE SRCP INFO");
			s = in.read();
			send("GO");
			s = in.read();
			try {
				final String[] sSplitted = s.split(" ");

				if (sSplitted.length >= 5) {
					id = Integer.parseInt(sSplitted[4]);
				}
			} catch (final NumberFormatException e) {
				System.err.println(s + ": cannot convert the 5. token from \""
						+ s + "\" into an integer");
			}

			// Now receive and handle messages continuously.
			infoThread = new Thread(this);
			infoThread.setDaemon(true);
			infoThread.start();
		} catch (final UnknownHostException e) {
			throw new SRCPHostNotFoundException();
		} catch (final IOException e) {
			throw new SRCPIOException();
		}
	}

	public void disconnect() throws SRCPException {
		try {
			GMListeners.remove(CRCFHandle);
			if (socket != null) {
				socket.close();
				socket = null;
			}
		} catch (final IOException e) {
			throw new SRCPIOException(e);
		}
	}

	private void send(final String s) throws IOException {
		informListenersSent(s);
		out.write(s + "\n");
	}

	public void run() {
		try {
			while (true) {
				final String s = in.read();
				if (s == null) {
					break;
				}
				informListenersReceived(s);
			}
		} catch (final SocketException e) {
			return;
		} catch (final IOException e) {
			// what to do, if IOException on info channel?
		}
	}

	public void addInfoDataListener(final InfoDataListener listener) {
		listeners.add(listener);
	}

	public void removeInfoDataListener(final InfoDataListener listener) {
		listeners.remove(listener);
	}

	private void informListenersReceived(final String s) {
		try {
			final TokenizedLine tokenLine = new TokenizedLine(s);

			if (tokenLine.hasMoreElements()) {
				final double timestamp = tokenLine.nextDoubleToken();
				final int number = tokenLine.nextIntToken();

				if (number < 200) {
					tokenLine.nextStringToken();
					final int bus = tokenLine.nextIntToken();
					final String deviceGroup = tokenLine.nextStringToken()
							.toUpperCase();
					if (deviceGroup.equals("FB")) {
						handleFB(tokenLine, timestamp, number, bus);
					} else if (deviceGroup.equals("GA")) {
						handleGA(tokenLine, timestamp, number, bus);
					} else if (deviceGroup.equals("GL")) {
						handleGL(tokenLine, timestamp, number, bus);
					} else if (deviceGroup.equals("LOCK")) {
						handleLOCK(tokenLine, timestamp, number, bus);
					} else if (deviceGroup.equals("POWER")) {
						handlePOWER(tokenLine, timestamp, number, bus);
					} else if (deviceGroup.equals("SERVER")) {
						handleSERVER(tokenLine, timestamp, number);
					} else if (deviceGroup.equals("DESCRIPTION")) {
						// TODO: parse DESCRIPTION-Info
					} else if (deviceGroup.equals("SESSION")) {
						// TODO: parse SESSION-Info
					} else if (deviceGroup.equals("GM")) {
						handleGM(tokenLine, timestamp, number, bus);
					} else if (deviceGroup.equals("SM")) {
						handleSM(tokenLine, timestamp, number, bus);
					}
				}
			}
		} catch (final SRCPUnsufficientDataException e) {
			System.err.println("cannot parse line \"" + s + "\"");
			e.printStackTrace();
		} catch (final NumberFormatException e) {
			System.err.println("cannot convert the next token from \"" + s
					+ "\" into an integer");
			e.printStackTrace();
		} catch (final SRCPWrongValueException e) {
			System.err.println("wrong value in line \"" + s + "\"");
			e.printStackTrace();
		}

		LOGGER.debug("received data: " + s.trim());
		for (final InfoDataListener listener : listeners) {
			listener.infoDataReceived(s);
		}
	}

	private void informListenersSent(final String s) {
		for (final InfoDataListener listener : listeners) {
			listener.infoDataSent(s);
		}
	}

	private void handleFB(final TokenizedLine tokenLine,
			final double timestamp, final int number, final int bus)
			throws SRCPUnsufficientDataException {

		if (number == INFO_SET) {
			final int address = tokenLine.nextIntToken();
			final int value = tokenLine.nextIntToken();
			synchronized (FBListeners) {
				for (final FBInfoListener l : FBListeners) {
					l.FBset(timestamp, bus, address, value);
				}
			}
		} else if (number == INFO_TERM) {
			synchronized (FBListeners) {
				for (final FBInfoListener l : FBListeners) {
					l.FBterm(timestamp, bus);
				}
			}
		}
	}

	private void handleGL(final TokenizedLine tokenLine,
			final double timestamp, final int number, final int bus)
			throws SRCPUnsufficientDataException {
		final int address = tokenLine.nextIntToken();

		if (number == INFO_SET) {
			final SRCPLocomotiveDirection drivemode = SRCPLocomotiveDirection
					.valueOf(Integer.parseInt(tokenLine.nextStringToken()));
			final int v = tokenLine.nextIntToken();
			final int vMax = tokenLine.nextIntToken();
			final Collection<Boolean> functions = new ArrayList<Boolean>();

			while (tokenLine.hasMoreElements()) {
				functions.add(tokenLine.nextStringToken().equals("1"));
			}

			final boolean[] f = new boolean[functions.size()];
			int i = 0;

			for (final Boolean function : functions) {
				f[i++] = function.booleanValue();
			}
			synchronized (GLListeners) {
				for (final GLInfoListener l : GLListeners) {
					l.GLset(timestamp, bus, address, drivemode, v, vMax, f);
				}
			}
		} else if (number == INFO_INIT) {
			final String protocol = tokenLine.nextStringToken();
			while (tokenLine.hasMoreElements()) {
				// TODO: get params
				tokenLine.nextStringToken();
			}
			synchronized (GLListeners) {
				for (final GLInfoListener l : GLListeners) {
					l.GLinit(timestamp, bus, address, protocol, null);
				}
			}
		} else if (number == INFO_TERM) {
			synchronized (GLListeners) {
				for (final GLInfoListener l : GLListeners) {
					l.GLterm(timestamp, bus, address);
				}
			}
		}
	}

	private void handleGA(final TokenizedLine tokenLine,
			final double timestamp, final int number, final int bus)
			throws SRCPUnsufficientDataException {
		final int address = tokenLine.nextIntToken();
		if (number == INFO_SET) {
			final int port = tokenLine.nextIntToken();
			final int value = tokenLine.nextIntToken();
			synchronized (GAListeners) {
				for (final GAInfoListener l : GAListeners) {
					l.GAset(timestamp, bus, address, port, value);
				}
			}
		} else if (number == INFO_INIT) {
			final String protocol = tokenLine.nextStringToken();

			while (tokenLine.hasMoreElements()) {
				// TODO: get params

				tokenLine.nextStringToken();
			}
			synchronized (GAListeners) {
				for (final GAInfoListener l : GAListeners) {
					l.GAinit(timestamp, bus, address, protocol, null);
				}
			}
		} else if (number == INFO_TERM) {
			synchronized (GAListeners) {
				for (final GAInfoListener l : GAListeners) {
					l.GAterm(timestamp, bus, address);
				}
			}
		}
	}

	private void handleLOCK(final TokenizedLine tokenLine,
			final double timestamp, final int number, final int bus)
			throws SRCPUnsufficientDataException {

		final String lockedDeviceGroup = tokenLine.nextStringToken();
		final int address = tokenLine.nextIntToken();
		if (number == INFO_SET) {
			final int duration = tokenLine.nextIntToken();
			final int sessionID = tokenLine.nextIntToken();
			synchronized (LOCKListeners) {
				for (final LOCKInfoListener l : LOCKListeners) {
					l.LOCKset(timestamp, bus, address, lockedDeviceGroup,
							duration, sessionID);
				}
			}
		} else if (number == INFO_TERM) {
			synchronized (LOCKListeners) {
				for (final LOCKInfoListener l : LOCKListeners) {
					l.LOCKterm(timestamp, bus, address, lockedDeviceGroup);
				}
			}
		}
	}

	private void handlePOWER(final TokenizedLine tokenLine,
			final double timestamp, final int number, final int bus)
			throws SRCPUnsufficientDataException {

		if (number == INFO_SET) {
			final boolean powerOn = tokenLine.nextStringToken().equals("ON");
			String freeText = "";
			while (tokenLine.hasMoreElements()) {
				freeText += tokenLine.nextStringToken() + " ";
			}
			synchronized (POWERListeners) {
				for (final POWERInfoListener l : POWERListeners) {
					l.POWERset(timestamp, bus, powerOn, freeText);
				}
			}
		} else if (number == INFO_TERM) {
			synchronized (POWERListeners) {
				for (final POWERInfoListener l : POWERListeners) {
					l.POWERterm(timestamp, bus);
				}
			}
		}
	}

	private void handleSERVER(final TokenizedLine tokenLine,
			final double timestamp, final int number)
			throws SRCPUnsufficientDataException {

		if (number == INFO_SET) {
			final String action = tokenLine.nextStringToken();

			synchronized (SERVERListeners) {
				for (final SERVERInfoListener l : SERVERListeners) {
					if (action.equals("RESETTING")) {
						l.SERVERreset(timestamp);
					} else if (action.equals("TERMINATING")) {
						l.SERVERterm(timestamp);
					}
				}
			}
		}
	}

	private void handleSM(final TokenizedLine tokenLine,
			final double timestamp, final int number, final int bus)
			throws SRCPUnsufficientDataException {
		if (number == INFO_INIT) {
			final String protocol = tokenLine.nextStringToken();

			synchronized (SMListeners) {
				for (final SMInfoListener l : SMListeners) {
					l.SMinit(timestamp, bus, protocol);
				}
			}
		} else if (number == INFO_SET) {
			final int address = tokenLine.nextIntToken();
			final String type = tokenLine.nextStringToken();
			final Collection<String> values = new ArrayList<String>();

			while (tokenLine.hasMoreElements()) {
				values.add(tokenLine.nextStringToken());
			}
			synchronized (SMListeners) {
				for (final SMInfoListener l : SMListeners) {
					l.SMset(timestamp, bus, address, type,
							values.toArray(new String[0]));
				}
			}
		} else if (number == INFO_TERM) {
			synchronized (SMListeners) {
				for (final SMInfoListener l : SMListeners) {
					l.SMterm(timestamp, bus);
				}
			}
		}
	}

	/**
	 * Handles GM Messages, calls all registered GMInfoListeners.
	 * 
	 * @param tokenLine
	 * @param timestamp
	 * @param number
	 * @param bus
	 * @throws SRCPUnsufficientDataException
	 * @throws NumberFormatException
	 * @throws SRCPWrongValueException
	 */
	private void handleGM(final TokenizedLine tokenLine,
			final double timestamp, final int number, final int bus)
			throws SRCPUnsufficientDataException, NumberFormatException,
			SRCPWrongValueException {

		if (number == INFO_SET) {
			final int sendTo = tokenLine.nextIntToken();
			final int replyTo = tokenLine.nextIntToken();
			final String messageType = tokenLine.nextStringToken();
			synchronized (GMListeners) {
				for (final GMInfoListener l : GMListeners) {
					l.GMset(timestamp, bus, sendTo, replyTo, messageType,
							tokenLine);
				}
			}
		}
	}

	public synchronized void addFBInfoListener(final FBInfoListener l) {
		FBListeners.add(l);
	}

	public synchronized void addGAInfoListener(final GAInfoListener l) {
		GAListeners.add(l);
	}

	public synchronized void addGLInfoListener(final GLInfoListener l) {
		GLListeners.add(l);
	}

	public synchronized void addLOCKInfoListener(final LOCKInfoListener l) {
		LOCKListeners.add(l);
	}

	public synchronized void addPOWERInfoListener(final POWERInfoListener l) {
		POWERListeners.add(l);
	}

	public synchronized void addSERVERInfoListener(final SERVERInfoListener l) {
		SERVERListeners.add(l);
	}

	public synchronized void addSMInfoListener(final SMInfoListener l) {
		SMListeners.add(l);
	}

	public synchronized void addGMInfoListener(final GMInfoListener l) {
		GMListeners.add(l);
	}

	public synchronized void addCRCFInfoListener(final CRCFInfoListener l) {
		CRCFListeners.add(l);
	}

	public synchronized void removeFBInfoListener(final FBInfoListener l) {
		FBListeners.remove(l);
	}

	public synchronized void removeGAInfoListener(final GAInfoListener l) {
		GAListeners.remove(l);
	}

	public synchronized void removeGLInfoListener(final GLInfoListener l) {
		GLListeners.remove(l);
	}

	public synchronized void removeLOCKInfoListener(final LOCKInfoListener l) {
		LOCKListeners.remove(l);
	}

	public synchronized void removePOWERInfoListener(final POWERInfoListener l) {
		POWERListeners.remove(l);
	}

	public synchronized void removeSERVERInfoListener(final SERVERInfoListener l) {
		SERVERListeners.remove(l);
	}

	public synchronized void removeSMInfoListener(final SMInfoListener l) {
		SMListeners.remove(l);
	}

	public synchronized void removeGMInfoListener(final GMInfoListener l) {
		GMListeners.remove(l);
	}

	public synchronized void removeCRCFInfoListener(final CRCFInfoListener l) {
		CRCFListeners.remove(l);
	}

	public int getID() {
		return id;
	}

	/**
	 * Handler for CRCF Messages, is registered as GMInfoListener. Calls
	 * CRCFInfoListener.
	 * 
	 * @author Michael Oppenauer 03.02.2009
	 * 
	 */
	class CRCFHandler implements GMInfoListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.dermoba.srcp.devices.GMInfoListener#GMset(double, int, int,
		 * int, java.lang.String, de.dermoba.srcp.common.TokenizedLine)
		 */
		public void GMset(final double timestamp, final int bus,
				final int sendTo, final int replyTo, final String messageType,
				final TokenizedLine tokenLine)
				throws SRCPUnsufficientDataException, NumberFormatException,
				SRCPWrongValueException {
			if (messageType.equals("CRCF")) {
				final String actor = tokenLine.nextURLStringToken();
				final UUID actor_id = UUID.fromString(tokenLine
						.nextURLStringToken());
				final String method = tokenLine.nextStringToken();
				final String attribute = tokenLine.nextURLStringToken();
				String attribute_value = "";
				if (tokenLine.hasMoreElements()) {
					attribute_value = tokenLine.nextURLStringToken();
				}
				synchronized (CRCFListeners) {
					for (final CRCFInfoListener l : CRCFListeners) {
						if (method.equals("GET")) {
							l.CRCFget(timestamp, bus, sendTo, replyTo, actor,
									actor_id, attribute);
						} else if (method.equals("SET")) {
							l.CRCFset(timestamp, bus, sendTo, replyTo, actor,
									actor_id, attribute, attribute_value);
						} else if (method.equals("INFO")) {
							l.CRCFinfo(timestamp, bus, sendTo, replyTo, actor,
									actor_id, attribute, attribute_value);
						} else if (method.equals("LIST")) {
							l.CRCFlist(timestamp, bus, sendTo, replyTo, actor,
									actor_id, attribute);
						}
					}
				}
			}
		}
	}
}
