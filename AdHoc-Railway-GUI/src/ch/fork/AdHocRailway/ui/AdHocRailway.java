/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id$
 * 
 *----------------------------------------------------------------------*/

/*------------------------------------------------------------------------
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 *----------------------------------------------------------------------*/

package ch.fork.AdHocRailway.ui;

import static ch.fork.AdHocRailway.ui.ImageTools.createImageIcon;
import static ch.fork.AdHocRailway.ui.ImageTools.createImageIconFromIconSet;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Set;

import javax.persistence.PersistenceException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRootPane;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import ch.fork.AdHocRailway.domain.HibernatePersistence;
import ch.fork.AdHocRailway.domain.locking.LockingException;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveControlface;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveManager;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveManagerImpl;
import ch.fork.AdHocRailway.domain.locomotives.SRCPLocomotiveControlAdapter;
import ch.fork.AdHocRailway.domain.routes.RouteControlIface;
import ch.fork.AdHocRailway.domain.routes.RouteManager;
import ch.fork.AdHocRailway.domain.routes.RouteMangerImpl;
import ch.fork.AdHocRailway.domain.routes.SRCPRouteControlAdapter;
import ch.fork.AdHocRailway.domain.turnouts.SRCPTurnoutControlAdapter;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutControlIface;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutManagerImpl;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutManger;
import ch.fork.AdHocRailway.domain.turnouts.XMLTurnoutService;
import ch.fork.AdHocRailway.services.locomotives.HibernateLocomotiveService;
import ch.fork.AdHocRailway.services.locomotives.XMLLocomotiveService;
import ch.fork.AdHocRailway.services.routes.HibernateRouteService;
import ch.fork.AdHocRailway.services.routes.XMLRouteService;
import ch.fork.AdHocRailway.services.turnouts.HibernateTurnoutService;
import ch.fork.AdHocRailway.technical.configuration.ConfigurationException;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;
import ch.fork.AdHocRailway.ui.locomotives.LocomotiveControlPanel;
import ch.fork.AdHocRailway.ui.locomotives.configuration.LocomotiveConfigurationDialog;
import ch.fork.AdHocRailway.ui.power.PowerControlPanel;
import ch.fork.AdHocRailway.ui.routes.configuration.RoutesConfigurationDialog;
import ch.fork.AdHocRailway.ui.turnouts.configuration.TurnoutConfigurationDialog;

import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;

import de.dermoba.srcp.client.CommandDataListener;
import de.dermoba.srcp.client.InfoDataListener;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.common.exception.SRCPException;
import de.dermoba.srcp.devices.SERVER;
import de.dermoba.srcp.model.SRCPModelException;
import de.dermoba.srcp.model.locking.SRCPLockControl;
import de.dermoba.srcp.model.power.SRCPPowerControl;
import de.dermoba.srcp.model.power.SRCPPowerState;
import de.dermoba.srcp.model.power.SRCPPowerSupplyException;

public class AdHocRailway extends JFrame implements CommandDataListener,
		InfoDataListener, PreferencesKeys {
	public static Logger logger = Logger.getLogger(AdHocRailway.class);
	private static final long serialVersionUID = 1L;
	private static AdHocRailway instance;

	private static final String TITLE = "AdHoc-Railway";

	private SRCPSession session;

	private TurnoutControlIface turnoutControl;

	private TurnoutManger turnoutPersistence;

	private LocomotiveControlface locomotiveControl;

	private LocomotiveManager locomotivePersistence;

	private SRCPPowerControl powerControl;

	private SRCPLockControl lockControl;

	private RouteControlIface routeControl;

	private Preferences preferences;

	private TrackControlPanel trackControlPanel;

	private LocomotiveControlPanel locomotiveControlPanel;

	private JPanel statusBarPanel;

	private JLabel hostnameLabel;

	private JButton connectToolBarButton;

	private JButton disconnectToolBarButton;

	private JComboBox<String> commandHistory;

	private DefaultComboBoxModel<String> commandHistoryModel;

	private JMenuItem daemonConnectItem;

	private JMenuItem daemonDisconnectItem;

	private JMenuItem daemonResetItem;

	private JMenuItem daemonPowerOnItem;

	private JMenuItem daemonPowerOffItem;

	private JButton toggleFullscreenButton;

	private JMenuBar menuBar;

	boolean fullscreen = false;

	private SplashWindow splash;

	private JPanel mainPanel;
	private JPanel toolbarPanel;
	public File actualFile;
	private RouteManager routePersistence;
	private JProgressBar progressBar;
	public boolean fileMode;
	private PowerControlPanel powerControlPanel;

	public AdHocRailway() {
		this(null);
	}

	public AdHocRailway(String file) {
		super(TITLE);
		try {
			PatternLayout layout = new PatternLayout(
					"%r [%t] %-5p %c{1} - %m%n");
			logger.addAppender(new FileAppender(layout, System
					.getProperty("user.home")
					+ File.separator
					+ "adhocrailway.log"));
			// PlasticLookAndFeel.setMyCurrentTheme(settings.getSelectedTheme());
			PlasticLookAndFeel
					.setTabStyle(PlasticLookAndFeel.TAB_STYLE_DEFAULT_VALUE);
			PlasticLookAndFeel.setHighContrastFocusColorsEnabled(false);

			UIManager.setLookAndFeel(new PlasticXPLookAndFeel());

			instance = this;
			splash = new SplashWindow(createImageIconFromIconSet("splash.png"),
					this, 500, 11);
			setIconImage(createImageIconFromIconSet("RailControl.png")
					.getImage());

			initProceeded("Loading Persistence Layer (Preferences)");

			preferences = Preferences.getInstance();

			boolean useDatabase = loadPersistenceLayer();

			loadControlLayer();

			initProceeded("Creating GUI ...");
			initGUI();
			logger.info("Finished Creating GUI");
			splash.setVisible(false);

			if (preferences.getBooleanValue(OPEN_LAST_FILE)) {
				String lastFile = preferences.getStringValue(LAST_OPENED_FILE);
				if (lastFile != null && !lastFile.equals("") && !useDatabase) {

					new OpenFileAction().openFile(new File(preferences
							.getStringValue(LAST_OPENED_FILE)));
				} else if (useDatabase) {
					updateGUI();
				}
			} else {
				updateGUI();
			}
			if (preferences.getBooleanValue(AUTOCONNECT))
				new ConnectAction().actionPerformed(null);

			// EnablerDisabler.setEnable(false, trackControlPanel);
			// EnablerDisabler.setEnable(false, locomotiveControlPanel);

			setSize(1200, 1024);
			// pack();
			toFront();
			// TutorialUtils.locateOnOpticalScreenCenter(this);

			initProceeded("RailControl started");
			updateCommandHistory("RailControl started");
			setVisible(true);
		} catch (UnsupportedLookAndFeelException e) {
			logger.error(e.getMessage(), e);
			ExceptionProcessor.getInstance().processException(e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			ExceptionProcessor.getInstance().processException(e);
		}
	}

	public static AdHocRailway getInstance() {
		return instance;
	}

	private void loadControlLayer() {
		powerControl = SRCPPowerControl.getInstance();

		initProceeded("Loading Control Layer (Locomotives)");
		locomotiveControl = SRCPLocomotiveControlAdapter.getInstance();
		locomotiveControl.setLocomotivePersistence(locomotivePersistence);
		locomotiveControl.update();

		initProceeded("Loading Control Layer (Turnouts)");
		turnoutControl = SRCPTurnoutControlAdapter.getInstance();
		turnoutControl.setPersistence(turnoutPersistence);
		turnoutControl.update();

		initProceeded("Loading Control Layer (Routes)");
		routeControl = SRCPRouteControlAdapter.getInstance();
		routeControl.setRoutePersistence(routePersistence);
		routeControl.update();

		initProceeded("Loading Control Layer (Locks)");
		lockControl = SRCPLockControl.getInstance();
	}

	private boolean loadPersistenceLayer() {
		boolean useDatabase = preferences
				.getBooleanValue(PreferencesKeys.USE_DATABASE);

		if (useDatabase) {
			initProceeded("Connecting to database");
			try {
				HibernatePersistence.setup();
				System.out.println("setup()");
				HibernatePersistence.connect();
				System.out.println("connect()");
				fileMode = false;
			} catch (Exception e) {
				e.printStackTrace();
				splash.setVisible(false);
				fileMode = true;
				JOptionPane
						.showMessageDialog(
								splash,
								"Failed to connect to database\nStarting AdHoc-Railway in File-Mode",
								"Error", JOptionPane.ERROR_MESSAGE);
				useDatabase = false;
			}
		} else {
			fileMode = true;
			initProceeded("");
		}
		initProceeded("Loading Persistence Layer (Locomotives)");
		// FIXME
		// if (useDatabase)
		locomotivePersistence = LocomotiveManagerImpl.getInstance();
		// else
		// locomotivePersistence = FileLocomotivePersistence.getInstance();

		initProceeded("Loading Persistence Layer (Turnouts)");
		// if (useDatabase)
		turnoutPersistence = TurnoutManagerImpl.getInstance();
		// else
		// turnoutPersistence = XMLTurnoutService.getInstance();

		initProceeded("Loading Persistence Layer (Routes)");
		// if (useDatabase)
		routePersistence = RouteMangerImpl.getInstance();
		// else
		// routePersistence = XMLRouteService.getInstance();

		if (useDatabase) {
			String host = preferences
					.getStringValue(PreferencesKeys.DATABASE_HOST);
			String database = preferences
					.getStringValue(PreferencesKeys.DATABASE_NAME);
			String url = "jdbc:mysql://" + host + "/" + database;
			setTitle(AdHocRailway.TITLE + " [" + url + "]");
		}
		return useDatabase;
	}

	private void initGUI() {

		setFont(new Font("Verdana", Font.PLAIN, 19));
		setLayout(new BorderLayout());

		initMenu();
		initToolbar();
		statusBarPanel = initStatusBar();
		add(statusBarPanel, BorderLayout.SOUTH);
		mainPanel = new JPanel();

		mainPanel = new JPanel(new BorderLayout());

		trackControlPanel = new TrackControlPanel();
		locomotiveControlPanel = new LocomotiveControlPanel();
		powerControlPanel = new PowerControlPanel();

		JPanel southPanel = new JPanel(new BorderLayout());
		southPanel.add(locomotiveControlPanel, BorderLayout.WEST);
		southPanel.add(powerControlPanel, BorderLayout.CENTER);
		powerControlPanel.setConnected(false);
		mainPanel.add(trackControlPanel, BorderLayout.CENTER);
		mainPanel.add(southPanel, BorderLayout.SOUTH);
		add(mainPanel, BorderLayout.CENTER);

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				new ExitAction().actionPerformed(null);
			}
		});
		hostnameLabel.setText(preferences.getStringValue("Hostname"));
	}

	public void updateGUI() {

		updatePower();
		updateTurnouts();
		updateLocomotives();
		disableNavigationKeys(mainPanel);
		mainPanel.requestFocus();
	}

	private void disableNavigationKeys(Component comp) {
		comp.setFocusTraversalKeysEnabled(false);
		if (comp instanceof Container) {
			Component[] children = ((Container) comp).getComponents();
			for (int i = 0; i < children.length; i++) {
				disableNavigationKeys(children[i]);
			}
		}
	}

	private void updatePower() {
		powerControlPanel.update();
	}

	private void updateLocomotives() {
		// locomotivePersistence.reload();
		locomotiveControl.update();
		locomotiveControlPanel.update();
	}

	private void updateTurnouts() {
		// turnoutPersistence.reload();
		// routePersistence.reload();
		turnoutControl.update();
		routeControl.update();
		trackControlPanel.update();
	}

	@Override
	public void commandDataSent(String commandData) {
		if (preferences.getBooleanValue(LOGGING)) {
			// updateCommandHistory("To Server: " + commandData);
		}
		logger.info("To Server: " + commandData.trim());
	}

	@Override
	public void infoDataReceived(String infoData) {
		if (preferences.getBooleanValue(LOGGING)) {
			// updateCommandHistory("From Server: " + infoData);
		}
		logger.info("From Server: " + infoData.trim());
	}

	public void updateCommandHistory(String text) {
		DateFormat df = new SimpleDateFormat("HH:mm:ss.SS");
		String date = df.format(Calendar.getInstance().getTime());
		String fullText = "[" + date + "]: " + text;
		SwingUtilities.invokeLater(new CommandHistoryUpdater(fullText));

	}

	private void initProceeded(String message) {
		logger.info(message);
		splash.nextStep(message);
	}

	private class CommandHistoryUpdater implements Runnable {

		private final String text;

		public CommandHistoryUpdater(String text) {
			this.text = text;
		}

		@Override
		public void run() {
			if (commandHistoryModel.getSize() > 100) {
				commandHistoryModel.removeElementAt(100);
			}
			commandHistoryModel.insertElementAt(text, 0);
			commandHistory.setSelectedIndex(0);
		}
	}

	private class NewFileAction extends AbstractAction {

		public NewFileAction() {
			super("New\u2026", createImageIconFromIconSet("filenew.png"));
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {

			if (fileMode) {

				final int result = JOptionPane.showConfirmDialog(
						AdHocRailway.this,
						"Do you want to save the actual configuration?",
						"Export to database", JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						createImageIconFromIconSet("messagebox_info.png"));
				if (result == JOptionPane.YES_OPTION) {
					new SaveAction().actionPerformed(null);
				}

			} else {
				HibernatePersistence.disconnect();
			}
			XMLTurnoutService.getInstance().clear();
			XMLLocomotiveService.getInstance().clear();
			XMLRouteService.getInstance().clear();

			hostnameLabel.setText(Preferences.getInstance().getStringValue(
					PreferencesKeys.HOSTNAME));
			// turnoutPersistence = XMLTurnoutService.getInstance();
			turnoutControl.setPersistence(turnoutPersistence);
			// locomotivePersistence = FileLocomotivePersistence.getInstance();
			locomotiveControl.setLocomotivePersistence(locomotivePersistence);
			// routePersistence = XMLRouteService.getInstance();
			routeControl.setRoutePersistence(routePersistence);
			setTitle(AdHocRailway.TITLE + " []");
			actualFile = null;
			fileMode = true;
			updateGUI();
			updateCommandHistory("Empty AdHoc-Railway Configuration created");
		}

	}

	private class OpenFileAction extends AbstractAction {

		public OpenFileAction() {
			super("Open\u2026", createImageIconFromIconSet("fileopen.png"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser fileChooser = new JFileChooser(new File("."));
			int returnVal = fileChooser.showOpenDialog(AdHocRailway.this);
			if (!fileMode) {
				HibernatePersistence.disconnect();
			}
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				actualFile = fileChooser.getSelectedFile();
				openFile(actualFile);
			} else {
				updateCommandHistory("Open command cancelled by user");
			}

		}

		public void openFile(final File file) {
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						progressBar.setIndeterminate(true);
						XMLTurnoutService.getInstance().clear();
						XMLLocomotiveService.getInstance().clear();
						XMLRouteService.getInstance().clear();

						// new XMLImporter(file.getAbsolutePath());

						hostnameLabel.setText(Preferences.getInstance()
								.getStringValue(PreferencesKeys.HOSTNAME));
						// turnoutPersistence = FileTurnoutPersistence
						// .getInstance();
						turnoutControl.setPersistence(turnoutPersistence);
						// locomotivePersistence = FileLocomotivePersistence
						// .getInstance();
						locomotiveControl
								.setLocomotivePersistence(locomotivePersistence);
						// routePersistence = XMLRouteService.getInstance();
						routeControl.setRoutePersistence(routePersistence);
						setTitle(AdHocRailway.TITLE + " ["
								+ file.getAbsolutePath() + "]");
						AdHocRailway.this.actualFile = file;
						fileMode = true;
						updateGUI();
						updateCommandHistory("AdHoc-Railway Configuration loaded ("
								+ file + ")");
					} catch (ConfigurationException e) {
						ExceptionProcessor.getInstance().processException(e);
					}
					progressBar.setIndeterminate(false);

				}
			});
			t.start();
		}
	}

	private class OpenDatabaseAction extends AbstractAction {

		public OpenDatabaseAction() {
			super("Open Database\u2026",
					createImageIconFromIconSet("database.png"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {

			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						progressBar.setIndeterminate(true);
						if (!fileMode) {
							HibernatePersistence.disconnect();
						}
						HibernatePersistence.setup();
						HibernatePersistence.connect();
						hostnameLabel.setText(Preferences.getInstance()
								.getStringValue(PreferencesKeys.HOSTNAME));
						turnoutControl.setPersistence(turnoutPersistence);

						locomotiveControl
								.setLocomotivePersistence(locomotivePersistence);
						routeControl.setRoutePersistence(routePersistence);
						String host = preferences
								.getStringValue(PreferencesKeys.DATABASE_HOST);
						String database = preferences
								.getStringValue(PreferencesKeys.DATABASE_NAME);
						String url = "jdbc:mysql://" + host + "/" + database;
						setTitle(AdHocRailway.TITLE + " [" + url + "]");
						AdHocRailway.this.actualFile = null;
						fileMode = false;
						updateCommandHistory("Successfully connected to database: "
								+ url);
					} catch (PersistenceException ex) {
						ExceptionProcessor.getInstance().processException(
								"Failed to connect to database", ex);
					}
					updateGUI();
					progressBar.setIndeterminate(false);
				}
			});
			t.start();
		}
	}

	private class SaveAction extends AbstractAction {
		public SaveAction() {
			super("Save", createImageIconFromIconSet("filesave.png"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (actualFile == null) {
				new SaveAsAction().actionPerformed(null);
			}
			saveActualFile();
		}

	}

	public void saveActualFile() {
		if (fileMode)
			saveFile(AdHocRailway.this.actualFile);
	}

	private void saveFile(File file) {
		// XMLExporter_1_0 exporter = new
		// XMLExporter_1_0(turnoutPersistence,
		// locomotivePersistence, routePersistence);
		// String xml = exporter.export();
		// OutputStreamWriter out = new OutputStreamWriter(
		// new FileOutputStream(file), "UTF-8");
		// out.write(xml);
		// out.close();
		// actualFile = file;
		updateCommandHistory("AdHoc-Railway Configuration saved (" + file + ")");
	}

	private class SaveAsAction extends AbstractAction {
		public SaveAsAction() {
			super("Save as\u2026", createImageIconFromIconSet("filesave.png"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {

			JFileChooser fileChooser = new JFileChooser(new File("."));
			int returnVal = fileChooser.showSaveDialog(AdHocRailway.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File saveFile = fileChooser.getSelectedFile();
				saveFile(saveFile);
			} else {
				updateCommandHistory("Save command cancelled by user");
			}

		}
	}

	private class ImportLocomotivesAction extends AbstractAction {

		public ImportLocomotivesAction() {
			super("Import Locomotives");
		}

		@Override
		public void actionPerformed(ActionEvent e) {

		}
	}

	private class ExportLocomotivesAction extends AbstractAction {

		public ExportLocomotivesAction() {
			super("Export Locomotives");
		}

		@Override
		public void actionPerformed(ActionEvent e) {

		}
	}

	private class ExportToDatabaseAction extends AbstractAction {

		public ExportToDatabaseAction() {
			super("Export to Database");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			final int result = JOptionPane.showConfirmDialog(AdHocRailway.this,
					"All data in the database will be deleted prior "
							+ "to the export.\n"
							+ "The application will afterwards "
							+ "switch to database-mode.\n"
							+ "Do you really want to proceed ?",
					"Export to database", JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE,
					createImageIconFromIconSet("messagebox_warning.png"));
			if (result == JOptionPane.YES_OPTION) {
				Thread t = new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							progressBar.setIndeterminate(true);
							HibernatePersistence.disconnect();

							HibernatePersistence.setup();
							HibernatePersistence.connect();

							// FIXME reimplement
							// new XMLImporter(actualFile.getAbsolutePath(),
							// HibernateTurnoutPersistence.getInstance(),
							// HibernateLocomotiveService.getInstance(),
							// HibernateRoutePersistence.getInstance());

							int result2 = JOptionPane
									.showConfirmDialog(
											AdHocRailway.this,
											"The configuration has been exported to the database\n"
													+ "Do you want to switch to the database mode ?",
											"Export to database",
											JOptionPane.YES_NO_OPTION,
											JOptionPane.QUESTION_MESSAGE,
											createImageIcon("messagebox_info.png"));
							String host = preferences
									.getStringValue(PreferencesKeys.DATABASE_HOST);
							String database = preferences
									.getStringValue(PreferencesKeys.DATABASE_NAME);
							String url = "jdbc:mysql://" + host + "/"
									+ database;
							String f = actualFile.toString();
							if (result2 == JOptionPane.YES_OPTION) {
								hostnameLabel.setText(Preferences.getInstance()
										.getStringValue(
												PreferencesKeys.HOSTNAME));
								turnoutControl
										.setPersistence(turnoutPersistence);
								locomotiveControl
										.setLocomotivePersistence(locomotivePersistence);
								routeControl
										.setRoutePersistence(routePersistence);

								setTitle(AdHocRailway.TITLE + " [" + url + "]");
								actualFile = null;
								fileMode = true;
							}
							updateCommandHistory("AdHoc-Railway Configuration loaded ("
									+ f + ") and imported into database " + url);
						} catch (PersistenceException ex) {
							ExceptionProcessor.getInstance().processException(
									"Failed to connect to database", ex);
						} catch (ConfigurationException e1) {
							ExceptionProcessor.getInstance().processException(
									"Failed to connect to database", e1);
						}
						updateGUI();
						progressBar.setIndeterminate(false);
					}
				});
				t.start();
			}
		}
	}

	private class ClearDatabaseAction extends AbstractAction {

		public ClearDatabaseAction() {
			super("Clear Database");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			int result = JOptionPane.showConfirmDialog(AdHocRailway.this,
					"All data in the database will be deleted \n"
							+ "Do you really want to proceed ?",
					"Export to database", JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE,
					createImageIconFromIconSet("messagebox_warning.png"));
			if (result == JOptionPane.YES_OPTION) {

				try {
					HibernatePersistence.disconnect();
					HibernatePersistence.setup();
					HibernatePersistence.connect();

					HibernateRouteService.getInstance().clear();
					HibernateTurnoutService.getInstance().clear();
					HibernateLocomotiveService.getInstance().clear();
				} catch (Exception x) {
					ExceptionProcessor.getInstance().processException(x);
				}
				updateGUI();
			}
		}
	}

	private class ExitAction extends AbstractAction {

		public ExitAction() {
			super("Exit", createImageIconFromIconSet("exit.png"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			// int result = JOptionPane.showConfirmDialog(AdHocRailway.this,
			// "Really exit ?", "Exit", JOptionPane.YES_NO_OPTION,
			// JOptionPane.QUESTION_MESSAGE,
			// createImageIcon("messagebox_warning.png"));
			int result = JOptionPane.YES_OPTION;
			if (result == JOptionPane.YES_OPTION) {

				try {
					// SRCPLockControl.getInstance().releaseAllLocks();
				} catch (LockingException e1) {
					e1.printStackTrace();
				}

				if (actualFile != null) {
					preferences.setStringValue(
							PreferencesKeys.LAST_OPENED_FILE,
							actualFile.getAbsolutePath());
					try {
						preferences.save();
					} catch (FileNotFoundException e1) {
						ExceptionProcessor.getInstance().processException(e1);
					} catch (IOException e1) {
						ExceptionProcessor.getInstance().processException(e1);
					}
				}
				if (!fileMode) {
					HibernatePersistence.disconnect();
				}
				System.exit(0);
			}
		}
	}

	private class TurnoutAction extends AbstractAction {

		public TurnoutAction() {
			super("Turnouts\u2026", createImageIconFromIconSet("switch.png"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			TurnoutConfigurationDialog switchConfigDialog = new TurnoutConfigurationDialog(
					AdHocRailway.this);
			if (switchConfigDialog.isOkPressed()) {
				updateTurnouts();
				// updateCommandHistory("Turnout configuration changed");
			}
		}
	}

	private class RoutesAction extends AbstractAction {

		public RoutesAction() {
			super("Routes\u2026", createImageIconFromIconSet("route_edit.png"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			RoutesConfigurationDialog routesConfig = new RoutesConfigurationDialog(
					AdHocRailway.this);
			if (routesConfig.isOkPressed()) {
				updateTurnouts();
				// updateCommandHistory("Routes configuration changed");
			}
		}
	}

	private class LocomotivesAction extends AbstractAction {

		public LocomotivesAction() {
			super("Locomotives\u2026",
					createImageIconFromIconSet("locomotive.png"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			LocomotiveConfigurationDialog locomotiveConfigDialog = new LocomotiveConfigurationDialog(
					AdHocRailway.this);
			if (locomotiveConfigDialog.isOkPressed()) {
				updateLocomotives();
				// updateCommandHistory("Locomotive configuration changed");
			}
		}
	}

	private class PreferencesAction extends AbstractAction {

		public PreferencesAction() {
			super("Preferences\u2026",
					createImageIconFromIconSet("package_settings.png"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			PreferencesDialog p = new PreferencesDialog(AdHocRailway.this);
			if (p.isOkPressed()) {
				updateGUI();
				hostnameLabel.setText(preferences.getStringValue("Hostname"));
				updateCommandHistory("Preferences saved to: "
						+ preferences.getConfigFile());
			}
		}
	}

	/**
	 * Handels the start of a connection with the srcpd-server.
	 * 
	 * @author fork
	 */
	private class ConnectAction extends AbstractAction {

		public ConnectAction() {
			super("Connect", createImageIconFromIconSet("daemonconnect.png"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				String host = preferences.getStringValue(HOSTNAME);
				int port = preferences.getIntValue(PORT);

				session = new SRCPSession(host, port, false);
				session.getCommandChannel().addCommandDataListener(
						AdHocRailway.this);
				session.getInfoChannel().addInfoDataListener(AdHocRailway.this);
				powerControl.setSession(session);
				((SRCPTurnoutControlAdapter) turnoutControl)
						.setSession(session);
				((SRCPLocomotiveControlAdapter) locomotiveControl)
						.setSession(session);
				((SRCPRouteControlAdapter) routeControl).setSession(session);
				// ((SRCPRouteControl) routeControl).setSession(session);
				lockControl.setSession(session);
				session.connect();
				daemonConnectItem.setEnabled(false);
				daemonDisconnectItem.setEnabled(true);
				daemonResetItem.setEnabled(true);
				connectToolBarButton.setEnabled(false);
				disconnectToolBarButton.setEnabled(true);

				powerControlPanel.setConnected(true);

				// EnablerDisabler.setEnable(true, trackControlPanel);
				// EnablerDisabler.setEnable(true, locomotiveControlPanel);
				// updateGUI();
				updateCommandHistory("Connected to server " + host
						+ " on port " + port);
			} catch (SRCPException e1) {

				ExceptionProcessor.getInstance().processException(
						"Server not running", e1);

			}
		}
	}

	private class DisconnectAction extends AbstractAction {

		public DisconnectAction() {
			super("Disconnect",
					createImageIconFromIconSet("daemondisconnect.png"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				String host = preferences.getStringValue(HOSTNAME);
				int port = preferences.getIntValue(PORT);
				session.disconnect();
				session = null;
				((SRCPTurnoutControlAdapter) turnoutControl).setSession(null);
				((SRCPLocomotiveControlAdapter) locomotiveControl)
						.setSession(null);
				((SRCPRouteControlAdapter) routeControl).setSession(null);
				lockControl.setSession(null);
				daemonConnectItem.setEnabled(true);
				daemonDisconnectItem.setEnabled(false);
				daemonResetItem.setEnabled(false);
				connectToolBarButton.setEnabled(true);
				disconnectToolBarButton.setEnabled(false);
				powerControlPanel.setConnected(false);
				updateCommandHistory("Disconnected from server " + host
						+ " on port " + port);
			} catch (SRCPException e1) {
				ExceptionProcessor.getInstance().processException(e1);
			}
		}
	}

	private class PowerOnAction extends AbstractAction {
		public PowerOnAction() {
			super("Power On");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.ActionListener
		 * #actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent arg0) {
			try {
				powerControl.setAllStates(SRCPPowerState.ON);
			} catch (SRCPPowerSupplyException e) {
				ExceptionProcessor.getInstance().processException(e);
			} catch (SRCPModelException e) {
				ExceptionProcessor.getInstance().processException(e);
			}
		}
	}

	private class PowerOffAction extends AbstractAction {
		public PowerOffAction() {
			super("Power Off");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.ActionListener
		 * #actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent arg0) {
			try {
				powerControl.setAllStates(SRCPPowerState.OFF);
			} catch (SRCPPowerSupplyException e) {
				ExceptionProcessor.getInstance().processException(e);
			} catch (SRCPModelException e) {
				ExceptionProcessor.getInstance().processException(e);
			}
		}
	}

	private class ResetAction extends AbstractAction {

		public ResetAction() {
			super("Reset", createImageIconFromIconSet("daemonreset.png"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			SERVER serverDevice = new SERVER(session);
			try {
				serverDevice.reset();
			} catch (SRCPException e1) {
				ExceptionProcessor.getInstance().processException(e1);
			}
		}
	}

	private class RefreshAction extends AbstractAction {

		public RefreshAction() {
			super("Refresh", createImageIconFromIconSet("reload.png"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			updateGUI();
		}
	}

	private class ToggleFullscreenAction extends AbstractAction {

		public ToggleFullscreenAction() {
			super("ToggleFullscreen",
					createImageIconFromIconSet("window_fullscreen.png"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (fullscreen) {
				dispose();
				menuBar.setVisible(true);
				setResizable(true);
				setUndecorated(false);
				setSize(1000, 700);
				setVisible(true);
				toggleFullscreenButton
						.setIcon(createImageIconFromIconSet("window_fullscreen.png"));
				fullscreen = false;
			} else {
				dispose();
				menuBar.setVisible(false);
				setResizable(false);
				setUndecorated(true);
				setSize(java.awt.Toolkit.getDefaultToolkit().getScreenSize());
				setVisible(true);
				toggleFullscreenButton
						.setIcon(createImageIconFromIconSet("window_nofullscreen.png"));
				fullscreen = true;
			}
		}
	}

	private void initMenu() {
		menuBar = new JMenuBar();
		/* FILE */
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);

		JMenuItem newItem = new JMenuItem(new NewFileAction());
		newItem.setMnemonic(KeyEvent.VK_N);
		newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
				ActionEvent.CTRL_MASK));

		JMenuItem openItem = new JMenuItem(new OpenFileAction());
		openItem.setMnemonic(KeyEvent.VK_O);
		openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
				ActionEvent.CTRL_MASK));

		JMenuItem saveItem = new JMenuItem(new SaveAction());
		saveItem.setMnemonic(KeyEvent.VK_S);
		saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				ActionEvent.CTRL_MASK));

		JMenuItem saveAsItem = new JMenuItem(new SaveAsAction());
		saveAsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));

		JMenuItem importLocomotivesItem = new JMenuItem(
				new ImportLocomotivesAction());
		JMenuItem exportLocomotivesItem = new JMenuItem(
				new ExportLocomotivesAction());

		JMenuItem exportToDatabaseItem = new JMenuItem(
				new ExportToDatabaseAction());

		JMenuItem openDatabaseItem = new JMenuItem(new OpenDatabaseAction());
		JMenuItem clearDatabaseItem = new JMenuItem(new ClearDatabaseAction());

		JMenuItem exitItem = new JMenuItem(new ExitAction());
		exitItem.setMnemonic(KeyEvent.VK_X);
		exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
				ActionEvent.CTRL_MASK));

		JMenu importMenu = new JMenu("Import");
		JMenu exportMenu = new JMenu("Export");
		importMenu.add(importLocomotivesItem);
		exportMenu.add(exportLocomotivesItem);
		exportMenu.add(exportToDatabaseItem);

		fileMenu.add(newItem);
		fileMenu.add(openItem);
		fileMenu.add(openDatabaseItem);
		fileMenu.add(saveItem);
		fileMenu.add(saveAsItem);
		fileMenu.add(new JSeparator());
		fileMenu.add(openDatabaseItem);
		fileMenu.add(clearDatabaseItem);
		fileMenu.add(new JSeparator());
		fileMenu.add(importMenu);
		fileMenu.add(exportMenu);
		fileMenu.add(new JSeparator());
		fileMenu.add(exitItem);

		/* EDIT */
		JMenu editMenu = new JMenu("Edit");
		JMenuItem switchesItem = new JMenuItem(new TurnoutAction());
		JMenuItem routesItem = new JMenuItem(new RoutesAction());
		JMenuItem locomotivesItem = new JMenuItem(new LocomotivesAction());
		JMenuItem preferencesItem = new JMenuItem(new PreferencesAction());
		editMenu.setMnemonic(KeyEvent.VK_E);
		switchesItem.setMnemonic(KeyEvent.VK_S);
		switchesItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				ActionEvent.ALT_MASK));
		routesItem.setMnemonic(KeyEvent.VK_R);
		routesItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
				ActionEvent.ALT_MASK));
		locomotivesItem.setMnemonic(KeyEvent.VK_L);
		locomotivesItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L,
				ActionEvent.ALT_MASK));
		preferencesItem.setMnemonic(KeyEvent.VK_P);
		preferencesItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,
				ActionEvent.ALT_MASK));
		editMenu.add(switchesItem);
		editMenu.add(routesItem);
		editMenu.add(locomotivesItem);
		editMenu.add(new JSeparator());
		editMenu.add(preferencesItem);

		/* DAEMON */
		JMenu daemonMenu = new JMenu("Daemon");
		daemonConnectItem = new JMenuItem(new ConnectAction());
		daemonConnectItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
				ActionEvent.CTRL_MASK));
		daemonDisconnectItem = new JMenuItem(new DisconnectAction());
		daemonPowerOnItem = new JMenuItem(new PowerOnAction());
		assignAccelerator(daemonPowerOnItem, "PowerOn");
		daemonPowerOnItem.setEnabled(true);
		daemonPowerOffItem = new JMenuItem(new PowerOffAction());
		assignAccelerator(daemonPowerOffItem, "PowerOff");
		daemonPowerOffItem.setEnabled(true);
		daemonResetItem = new JMenuItem(new ResetAction());
		daemonDisconnectItem.setEnabled(false);
		daemonResetItem.setEnabled(false);
		daemonMenu.add(daemonConnectItem);
		daemonMenu.add(daemonDisconnectItem);
		daemonMenu.add(new JSeparator());
		daemonMenu.add(daemonPowerOnItem);
		daemonMenu.add(daemonPowerOffItem);
		daemonMenu.add(new JSeparator());
		daemonMenu.add(daemonResetItem);

		/* VIEW */
		JMenu viewMenu = new JMenu("View");
		JMenuItem refreshItem = new JMenuItem(new RefreshAction());
		JMenuItem fullscreenItem = new JMenuItem(new ToggleFullscreenAction());

		viewMenu.add(refreshItem);
		viewMenu.add(fullscreenItem);

		/* HELP */
		// JMenu helpMenu = new JMenu("Help");
		addMenu(fileMenu);
		addMenu(editMenu);
		addMenu(daemonMenu);
		addMenu(viewMenu);
		// addMenu(helpMenu);
		setJMenuBar(menuBar);
	}

	/**
     * 
     */
	private void assignAccelerator(JMenuItem item, String actionName) {
		Set<KeyStroke> strokes = preferences.getKeyBoardLayout().getKeys(
				actionName);
		if (strokes != null && strokes.size() > 0) {
			item.setAccelerator(strokes.iterator().next());
		}
	}

	public void addMenu(JMenu menu) {
		menuBar.add(menu);
	}

	private void initToolbar() {
		/* FILE */
		JToolBar fileToolBar = new JToolBar();

		JButton newFileToolBarButton = new SmallToolbarButton(
				new NewFileAction());
		JButton openFileToolBarButton = new SmallToolbarButton(
				new OpenFileAction());
		JButton openDatabaseToolBarButton = new SmallToolbarButton(
				new OpenDatabaseAction());
		JButton saveToolBarButton = new SmallToolbarButton(new SaveAction());
		JButton exitToolBarButton = new SmallToolbarButton(new ExitAction());

		fileToolBar.add(newFileToolBarButton);
		fileToolBar.add(openFileToolBarButton);
		fileToolBar.add(openDatabaseToolBarButton);
		fileToolBar.add(saveToolBarButton);
		fileToolBar.add(exitToolBarButton);

		/* DIGITAL */
		JToolBar digitalToolBar = new JToolBar();
		JButton switchesToolBarButton = new SmallToolbarButton(
				new TurnoutAction());
		JButton routesToolBarButton = new SmallToolbarButton(new RoutesAction());
		JButton locomotivesToolBarButton = new SmallToolbarButton(
				new LocomotivesAction());
		JButton preferencesToolBarButton = new SmallToolbarButton(
				new PreferencesAction());

		digitalToolBar.add(switchesToolBarButton);
		digitalToolBar.add(routesToolBarButton);
		digitalToolBar.add(locomotivesToolBarButton);
		digitalToolBar.add(preferencesToolBarButton);

		/* DAEMON */
		JToolBar daemonToolBar = new JToolBar();
		hostnameLabel = new JLabel();
		hostnameLabel.setText(preferences.getStringValue("Hostname"));
		connectToolBarButton = new SmallToolbarButton(new ConnectAction());
		disconnectToolBarButton = new SmallToolbarButton(new DisconnectAction());
		disconnectToolBarButton.setEnabled(false);

		daemonToolBar.add(hostnameLabel);
		daemonToolBar.addSeparator();
		daemonToolBar.add(connectToolBarButton);
		daemonToolBar.add(disconnectToolBarButton);

		/* VIEWS */
		JToolBar viewToolBar = new JToolBar();
		JButton refreshButton = new SmallToolbarButton(new RefreshAction());
		// toggleFullscreenButton = new SmallToolbarButton(
		// new ToggleFullscreenAction());

		viewToolBar.add(refreshButton);
		// viewToolBar.add(toggleFullscreenButton);

		/* ERROR */
		ErrorPanel errorPanel = new ErrorPanel();
		ExceptionProcessor.getInstance(errorPanel);

		toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
		addToolBar(fileToolBar);
		addToolBar(digitalToolBar);
		addToolBar(daemonToolBar);
		addToolBar(viewToolBar);
		// toolbarPanel.add(errorPanel);

		JPanel toolbarErrorPanel = new JPanel(new BorderLayout(10, 10));
		toolbarErrorPanel.add(toolbarPanel, BorderLayout.WEST);
		toolbarErrorPanel.add(errorPanel, BorderLayout.EAST);

		add(toolbarErrorPanel, BorderLayout.PAGE_START);
	}

	public void addToolBar(JToolBar toolbar) {
		toolbarPanel.add(toolbar);
	}

	private JPanel initStatusBar() {
		JPanel statusBarPanel = new JPanel();
		commandHistoryModel = new DefaultComboBoxModel<String>();
		commandHistory = new JComboBox<String>(commandHistoryModel);
		commandHistory.setEditable(false);
		commandHistory.setFocusable(false);

		progressBar = new JProgressBar(SwingConstants.HORIZONTAL);

		statusBarPanel.setLayout(new BorderLayout(5, 0));
		statusBarPanel.add(progressBar, BorderLayout.WEST);
		statusBarPanel.add(commandHistory, BorderLayout.CENTER);
		return statusBarPanel;
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new AdHocRailway(args[0]);
		} else {
			new AdHocRailway();
		}
	}

	public LocomotiveControlface getLocomotiveControl() {
		return locomotiveControl;
	}

	public void setLocomotiveControl(LocomotiveControlface locomotiveControl) {
		this.locomotiveControl = locomotiveControl;
	}

	public TurnoutControlIface getTurnoutControl() {
		return turnoutControl;
	}

	public TurnoutManger getTurnoutPersistence() {
		return turnoutPersistence;
	}

	public LocomotiveManager getLocomotivePersistence() {
		return locomotivePersistence;
	}

	public RouteControlIface getRouteControl() {
		return routeControl;
	}

	public Preferences getPreferences() {
		return preferences;
	}

	public SRCPSession getSession() {
		return session;
	}

	public RouteManager getRoutePersistence() {
		return routePersistence;
	}

	@Override
	public void commandDataReceived(String response) {
		// TODO Auto-generated method stub

	}

	@Override
	public void infoDataSent(String infoData) {
		// TODO Auto-generated method stub

	}

	public void registerEscapeKey(Action action) {
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		JRootPane rootPane = getRootPane();
		rootPane.registerKeyboardAction(action, stroke,
				JComponent.WHEN_IN_FOCUSED_WINDOW);
	}
}
