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

import ch.fork.AdHocRailway.controllers.PowerController;
import ch.fork.AdHocRailway.controllers.RailwayDevice;
import ch.fork.AdHocRailway.model.AdHocRailwayException;
import ch.fork.AdHocRailway.persistence.xml.XMLServiceHelper;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;
import ch.fork.AdHocRailway.ui.bus.events.*;
import ch.fork.AdHocRailway.ui.context.AdHocRailwayIface;
import ch.fork.AdHocRailway.ui.context.ApplicationContext;
import ch.fork.AdHocRailway.ui.context.EditingModeEvent;
import ch.fork.AdHocRailway.ui.locomotives.LocomotiveControlPanel;
import ch.fork.AdHocRailway.ui.locomotives.configuration.LocomotiveConfigurationDialog;
import ch.fork.AdHocRailway.ui.power.PowerControlPanel;
import ch.fork.AdHocRailway.ui.routes.configuration.RoutesConfigurationDialog;
import ch.fork.AdHocRailway.ui.turnouts.configuration.TurnoutConfigurationDialog;
import ch.fork.AdHocRailway.ui.utils.GlobalKeyShortcutHelper;
import ch.fork.AdHocRailway.ui.widgets.ErrorPanel;
import ch.fork.AdHocRailway.ui.widgets.SmallToolbarButton;
import ch.fork.AdHocRailway.ui.widgets.SplashWindow;
import ch.fork.AdHocRailway.ui.widgets.TrackControlPanel;
import com.google.common.eventbus.Subscribe;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;
import de.dermoba.srcp.model.locking.SRCPLockingException;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.SystemUtils;
import org.apache.log4j.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Set;

import static ch.fork.AdHocRailway.ui.utils.ImageTools.createImageIconFromCustom;
import static ch.fork.AdHocRailway.ui.utils.ImageTools.createImageIconFromIconSet;

public class AdHocRailway extends JFrame implements AdHocRailwayIface,
        PreferencesKeys {

    public static final String TITLE = "AdHoc-Railway";
    private static final Logger LOGGER = Logger.getLogger(AdHocRailway.class);
    private TrackControlPanel trackControlPanel;

    private LocomotiveControlPanel locomotiveControlPanel;

    private JPanel statusBarPanel;

    private JLabel railwayDeviceLabelLabel;

    private JButton connectToolBarButton;

    private JButton disconnectToolBarButton;

    private JComboBox<String> commandHistory;

    private DefaultComboBoxModel<String> commandHistoryModel;

    private JMenuItem daemonConnectItem;

    private JMenuItem daemonDisconnectItem;

    private JMenuItem daemonPowerOnItem;

    private JMenuItem daemonPowerOffItem;

    private JButton toggleFullscreenButton;

    private JMenuBar menuBar;

    private boolean fullscreen = false;

    private SplashWindow splash;

    private JPanel mainPanel;
    private JPanel toolbarPanel;
    private JProgressBar progressBar;
    private PowerControlPanel powerControlPanel;

    private JMenuItem saveItem;

    private JMenuItem saveAsItem;

    private JMenuItem switchesItem;

    private JMenuItem routesItem;

    private JMenuItem locomotivesItem;

    private JCheckBoxMenuItem enableEditing;

    private JButton turnoutsToolBarButton;

    private JButton routesToolBarButton;

    private JButton locomotivesToolBarButton;
    private JButton preferencesToolBarButton;
    private JMenuItem preferencesItem;

    private ApplicationContext appContext;

    private Preferences preferences;

    private PersistenceManager persistenceManager;
    private RailwayDeviceManager railwayDeviceManager;

    public AdHocRailway(org.apache.commons.cli.CommandLine parsedCommandLine) {

        if (!SystemUtils.IS_OS_MAC_OSX) {
            setTitle(TITLE);
        }
        try {

            appContext = new ApplicationContext();
            appContext.getMainBus().register(appContext);
            appContext.getMainBus().register(this);
            appContext.setMainApp(this);
            appContext.setMainFrame(this);
            setUpLogging();

            LOGGER.info("****************************************");
            LOGGER.info("AdHoc-Railway starting up!!!");
            LOGGER.info("****************************************");


            splash = new SplashWindow(createImageIconFromCustom("splash.png"),
                    this, 500, 12);
            setIconImage(createImageIconFromCustom("2-Hot-Train-icon 128.png")
                    .getImage());

            initProceeded("Loading Persistence Layer (Preferences)");

            preferences = Preferences.getInstance();
            preferences.loadPreferences(parsedCommandLine.hasOption("c"));
            appContext.setPreferences(preferences);

            initProceeded("Creating GUI ...");

            initGUI();
            disableEnableMenuItems();
            LOGGER.info("Finished Creating GUI");
            splash.setVisible(false);

            updateGUI();
            persistenceManager = new PersistenceManager(appContext);
            persistenceManager
                    .loadLastFileOrLoadDataFromAdHocServerIfRequested();

            railwayDeviceManager = new RailwayDeviceManager(appContext);
            appContext.setRailwayDeviceManager(railwayDeviceManager);
            railwayDeviceManager.autoConnectToRailwayDeviceIfRequested();


            initProceeded("AdHoc-Railway started");
            updateCommandHistory("AdHoc-Railway started");
            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(final Thread t, final Throwable e) {
                    handleException(e);
                }
            });
            pack();
            setLocationByPlatform(true);
            setVisible(true);
        } catch (final Exception e) {
            handleException(e);
            e.printStackTrace();
        }
    }

    public static void setupGlobalExceptionHandling() {

    }

    private static void macSetup(String appName) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        if (!SystemUtils.IS_OS_MAC_OSX)
            return;

        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "WikiTeX");
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }


    private static void winSetup(String title) throws UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        if (!SystemUtils.IS_OS_WINDOWS)
            return;

        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }

    private static void linuxSetup(String title) throws UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        if (!SystemUtils.IS_OS_LINUX)
            return;
        PlasticLookAndFeel
                .setTabStyle(PlasticLookAndFeel.TAB_STYLE_DEFAULT_VALUE);
        PlasticLookAndFeel.setHighContrastFocusColorsEnabled(false);

        UIManager.setLookAndFeel(new PlasticXPLookAndFeel());
    }


    public static void main(final String[] args) throws Exception {

        Options options = new Options();
        options.addOption("c", "clean", false, "start with a clean config");
        CommandLineParser parser = new BasicParser();

        org.apache.commons.cli.CommandLine parsedCommandLine = parser.parse(options, args);

        macSetup(TITLE);
        winSetup(TITLE);
        linuxSetup(TITLE);

        AdHocRailway adHocRailway = new AdHocRailway(parsedCommandLine);
    }

    @Override
    public void addMenu(final JMenu menu) {
        menuBar.add(menu);
    }

    @Override
    public void addToolBar(final JToolBar toolbar) {
        toolbarPanel.add(toolbar);
    }

    @Override
    public void displayMessage(String message) {
        final ExceptionProcessor instance2 = ExceptionProcessor.getInstance();
        instance2.displayMessage(message);
    }

    @Override
    public void handleException(final Throwable ex) {
        handleException(null, ex);
    }

    @Override
    public void handleException(final String message, final Throwable e) {
        final ExceptionProcessor instance2 = ExceptionProcessor.getInstance();
        if (instance2 != null) {
            instance2.processException(message, e);
        }
    }

    @Override
    public void registerKey(final int keyEvent, int modifiers, Action action) {
        GlobalKeyShortcutHelper.registerKey(getRootPane(), keyEvent, modifiers, action);
    }

    @Override
    public void registerEscapeKey(final Action action) {
        registerKey(KeyEvent.VK_ESCAPE, 0, action);
    }

    @Override
    public void registerSpaceKey(final Action action) {
        registerKey(KeyEvent.VK_SPACE, 0, action);
    }

    @Subscribe
    public void editingModeChanged(final EditingModeEvent event) {
        final boolean editing = event.isEditingMode();
        switchesItem.setEnabled(editing);
        routesItem.setEnabled(editing);
        locomotivesItem.setEnabled(editing);
        turnoutsToolBarButton.setEnabled(editing);
        routesToolBarButton.setEnabled(editing);
        locomotivesToolBarButton.setEnabled(editing);

    }

    @Override
    public void updateCommandHistory(final String text) {
        final DateFormat df = new SimpleDateFormat("HH:mm:ss.SS");
        final String date = df.format(Calendar.getInstance().getTime());
        final String fullText = "[" + date + "]: " + text;
        SwingUtilities.invokeLater(new CommandHistoryUpdater(fullText));
    }

    @Override
    public void initProceeded(final String message) {
        splash.nextStep(message);
    }

    public void saveActualFile() {
        if (isFileMode()) {
            saveFile(appContext.getActualFile());
        }
    }

    private boolean isFileMode() {
        final boolean fileMode = !preferences.getBooleanValue(USE_ADHOC_SERVER);
        return fileMode;
    }

    @Subscribe
    public void connectedToRailwayDevice(final ConnectedToRailwayEvent event) {
        final boolean connected = event.isConnected();
        daemonConnectItem.setEnabled(!connected);
        daemonDisconnectItem.setEnabled(connected);
        connectToolBarButton.setEnabled(!connected);
        preferencesItem.setEnabled(!connected);
        preferencesToolBarButton.setEnabled(!connected);
        disconnectToolBarButton.setEnabled(connected);
    }

    private void setUpLogging() {
        PropertyConfigurator.configure(AdHocRailway.class.getClassLoader().getResource("log4j.properties"));

        final FileAppender appender = new FileAppender();
        appender.setName("MyFileAppender");
        appender.setLayout(new PatternLayout("%d [%t] %-5p %c{1} - %m%n"));
        String localhostname = "";
        try {
            localhostname = java.net.InetAddress.getLocalHost().getHostName();
        } catch (final UnknownHostException e) {
            e.printStackTrace();
        }
        final String userName = System.getProperty("user.name");

        appender.setFile("./logs/" + localhostname + "_" + userName + ".log");
        appender.setAppend(true);
        appender.setThreshold(Level.DEBUG);
        appender.activateOptions();
        Logger.getRootLogger().addAppender(appender);

    }

    private void initGUI() {

        setFont(new Font("Verdana", Font.PLAIN, 19));
        setLayout(new BorderLayout(0,0));
        initMenu();
        initToolbar();
        statusBarPanel = initStatusBar();
        mainPanel = new JPanel();

        mainPanel = new JPanel(new MigLayout("debug, insets 5, gap 5", "[][grow]", "[grow][]"));

        final JPanel segmentPanel = new KeyControl(appContext);

        trackControlPanel = new TrackControlPanel(appContext);
        locomotiveControlPanel = new LocomotiveControlPanel(appContext);
        powerControlPanel = new PowerControlPanel(appContext);

        mainPanel.add(segmentPanel, "grow");
        mainPanel.add(trackControlPanel, "grow, wrap");
        mainPanel.add(powerControlPanel, "grow");
        mainPanel.add(locomotiveControlPanel, "grow, wrap");

        add(mainPanel, BorderLayout.CENTER);
        add(statusBarPanel, BorderLayout.PAGE_END);

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                new ExitAction().actionPerformed(null);
            }
        });
        setRailwayDeviceLabelText();

        initShortcuts();
        appContext.getMainBus().register(this);
        appContext.getMainBus().post(
                new EditingModeEvent(appContext.isEditingMode()));

    }

    private void updateGUI() {
        disableNavigationKeys(mainPanel);
        mainPanel.requestFocus();
    }

    private void disableNavigationKeys(final Component comp) {
        comp.setFocusTraversalKeysEnabled(false);
        if (comp instanceof Container) {
            final Component[] children = ((Container) comp).getComponents();
            for (int i = 0; i < children.length; i++) {
                disableNavigationKeys(children[i]);
            }
        }
    }

    private void disableEnableMenuItems() {
        saveAsItem.setEnabled(isFileMode());
        saveItem.setEnabled(isFileMode());

    }

    private void saveFile(final File file) {
        try {
            final XMLServiceHelper xmlService = new XMLServiceHelper();
            xmlService.saveFile(appContext.getLocomotiveManager(),
                    appContext.getTurnoutManager(),
                    appContext.getRouteManager(), file);
        } catch (final IOException e) {
            handleException(e);
        }
        updateCommandHistory("AdHoc-Railway Configuration saved (" + file + ")");
    }

    private void initMenu() {
        menuBar = new JMenuBar();
        /* FILE */
        final JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        final JMenuItem newItem = new JMenuItem(new NewFileAction());
        newItem.setMnemonic(KeyEvent.VK_N);
        newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

        final JMenuItem openItem = new JMenuItem(new OpenFileAction());
        openItem.setMnemonic(KeyEvent.VK_O);
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

        final JMenuItem openDatabaseItem = new JMenuItem(
                new OpenDatabaseAction());

        saveItem = new JMenuItem(new SaveAction());
        saveItem.setMnemonic(KeyEvent.VK_S);
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

        saveAsItem = new JMenuItem(new SaveAsAction());
        saveAsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | ActionEvent.SHIFT_MASK));

        final JMenuItem importAllItem = new JMenuItem(
                new ImportAllAction());
        final JMenuItem importLocomotivesItem = new JMenuItem(
                new ImportLocomotivesAction());
        final JMenuItem exportLocomotivesItem = new JMenuItem(
                new ExportLocomotivesAction());
        final JMenuItem exportAllItem = new JMenuItem(new ExportAllAction());

        final JMenu importMenu = new JMenu("Import");
        importMenu.add(importAllItem);
        importMenu.add(importLocomotivesItem);
        final JMenu exportMenu = new JMenu("Export");
        exportMenu.add(exportLocomotivesItem);
        exportMenu.add(exportAllItem);

        final JMenuItem clearLocomotivesItem = new JMenuItem(
                new ClearLocomotivesAction());
        final JMenuItem clearTurnoutsRoutesItem = new JMenuItem(
                new ClearTurnoutsAndRoutesAction());

        final JMenu clearMenu = new JMenu("Clear");
        clearMenu.add(clearLocomotivesItem);
        clearMenu.add(clearTurnoutsRoutesItem);

        final JMenuItem exitItem = new JMenuItem(new ExitAction());
        exitItem.setMnemonic(KeyEvent.VK_X);
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.add(openDatabaseItem);
        fileMenu.add(saveItem);
        fileMenu.add(saveAsItem);
        fileMenu.add(new JSeparator());
        fileMenu.add(importMenu);
        fileMenu.add(exportMenu);
        fileMenu.add(clearMenu);
        fileMenu.add(new JSeparator());
        fileMenu.add(exitItem);

		/* EDIT */
        final JMenu editMenu = new JMenu("Edit");
        enableEditing = new JCheckBoxMenuItem(new EnableEditingAction());

        switchesItem = new JMenuItem(new TurnoutAction());
        routesItem = new JMenuItem(new RoutesAction());
        locomotivesItem = new JMenuItem(new LocomotivesAction());
        preferencesItem = new JMenuItem(new PreferencesAction());
        enableEditing.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,
                ActionEvent.ALT_MASK));

        switchesItem.setMnemonic(KeyEvent.VK_T);
        switchesItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T,
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
        editMenu.add(enableEditing);
        editMenu.add(new JSeparator());
        editMenu.add(switchesItem);
        editMenu.add(routesItem);
        editMenu.add(locomotivesItem);
        editMenu.add(new JSeparator());
        editMenu.add(preferencesItem);

		/* DAEMON */
        final JMenu daemonMenu = new JMenu("Device");
        daemonConnectItem = new JMenuItem(new ConnectAction());
        daemonConnectItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        daemonDisconnectItem = new JMenuItem(new DisconnectAction());
        daemonDisconnectItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        daemonPowerOnItem = new JMenuItem(new PowerOnAction());
        assignAccelerator(daemonPowerOnItem, "PowerOn");
        daemonPowerOnItem.setEnabled(true);
        daemonPowerOffItem = new JMenuItem(new PowerOffAction());
        assignAccelerator(daemonPowerOffItem, "PowerOff");
        daemonPowerOffItem.setEnabled(true);
        daemonDisconnectItem.setEnabled(false);
        daemonMenu.add(daemonConnectItem);
        daemonMenu.add(daemonDisconnectItem);
        daemonMenu.add(new JSeparator());
        daemonMenu.add(daemonPowerOnItem);
        daemonMenu.add(daemonPowerOffItem);

		/* VIEW */
        final JMenu viewMenu = new JMenu("View");
        //final JMenuItem refreshItem = new JMenuItem(new RefreshAction());
        final JMenuItem fullscreenItem = new JMenuItem(
                new ToggleFullscreenAction());

        //viewMenu.add(refreshItem);
        viewMenu.add(fullscreenItem);

		/* HELP */
        addMenu(fileMenu);
        addMenu(editMenu);
        addMenu(daemonMenu);
        addMenu(viewMenu);
        setJMenuBar(menuBar);
    }

    /**
     *
     */
    private void assignAccelerator(final JMenuItem item, final String actionName) {
        final Set<KeyStroke> strokes = preferences.getKeyBoardLayout().getKeys(
                actionName);
        if (strokes != null && strokes.size() > 0) {
            item.setAccelerator(strokes.iterator().next());
        }
    }

    private void initToolbar() {
        /* FILE */
        final JToolBar fileToolBar = new JToolBar();

        final JButton newFileToolBarButton = new SmallToolbarButton(
                new NewFileAction());
        final JButton openFileToolBarButton = new SmallToolbarButton(
                new OpenFileAction());
        final JButton openDatabaseToolBarButton = new SmallToolbarButton(
                new OpenDatabaseAction());
        final JButton saveToolBarButton = new SmallToolbarButton(
                new SaveAction());
        final JButton exitToolBarButton = new SmallToolbarButton(
                new ExitAction());

        fileToolBar.add(newFileToolBarButton);
        fileToolBar.add(openFileToolBarButton);
        fileToolBar.add(openDatabaseToolBarButton);
        fileToolBar.add(saveToolBarButton);
        fileToolBar.add(exitToolBarButton);

		/* DIGITAL */
        final JToolBar digitalToolBar = new JToolBar();
        turnoutsToolBarButton = new SmallToolbarButton(new TurnoutAction());
        routesToolBarButton = new SmallToolbarButton(new RoutesAction());
        locomotivesToolBarButton = new SmallToolbarButton(
                new LocomotivesAction());
        preferencesToolBarButton = new SmallToolbarButton(
                new PreferencesAction());

        digitalToolBar.add(turnoutsToolBarButton);
        digitalToolBar.add(routesToolBarButton);
        digitalToolBar.add(locomotivesToolBarButton);
        digitalToolBar.add(preferencesToolBarButton);

		/* SRCP / AdHoc-Brain */
        final JToolBar daemonToolBar = new JToolBar();
        railwayDeviceLabelLabel = new JLabel();

        setRailwayDeviceLabelText();
        connectToolBarButton = new SmallToolbarButton(new ConnectAction());
        disconnectToolBarButton = new SmallToolbarButton(new DisconnectAction());
        disconnectToolBarButton.setEnabled(false);

        daemonToolBar.add(railwayDeviceLabelLabel);
        daemonToolBar.addSeparator();
        daemonToolBar.add(connectToolBarButton);
        daemonToolBar.add(disconnectToolBarButton);

		/* VIEWS */
        final JToolBar viewToolBar = new JToolBar();
        //final JButton refreshButton = new SmallToolbarButton(
        //        new RefreshAction());
        toggleFullscreenButton = new SmallToolbarButton(
                new ToggleFullscreenAction());

        // viewToolBar.add(refreshButton);
        viewToolBar.add(toggleFullscreenButton);

		/* ERROR */
        final ErrorPanel errorPanel = new ErrorPanel();
        ExceptionProcessor.getInstance(errorPanel);

        toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        addToolBar(fileToolBar);
        addToolBar(digitalToolBar);
        addToolBar(daemonToolBar);
        addToolBar(viewToolBar);

        final JPanel toolbarErrorPanel = new JPanel(new BorderLayout(2, 2));
        toolbarErrorPanel.add(toolbarPanel, BorderLayout.WEST);
        toolbarErrorPanel.add(errorPanel, BorderLayout.EAST);

        add(toolbarErrorPanel, BorderLayout.PAGE_START);
    }

    private void initShortcuts() {
        GlobalKeyShortcutHelper.registerKey(getRootPane(), KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK, new EnableEditingAction());
    }

    private void setRailwayDeviceLabelText() {
        final RailwayDevice railwayDevice = RailwayDevice
                .fromString(preferences
                        .getStringValue(PreferencesKeys.RAILWAY_DEVICE));

        if (RailwayDevice.SRCP.equals(railwayDevice)) {
            final String hostname = preferences
                    .getStringValue(PreferencesKeys.SRCP_HOSTNAME);
            railwayDeviceLabelLabel.setText("SRCP: " + hostname);
        } else if (RailwayDevice.ADHOC_BRAIN.equals(railwayDevice)) {
            final String adhocBrainPort = preferences
                    .getStringValue(PreferencesKeys.ADHOC_BRAIN_PORT);
            railwayDeviceLabelLabel.setText("AdHoc-Brain: " + adhocBrainPort);
        } else {
            railwayDeviceLabelLabel.setText("Null-Device (nothing happens)");
        }
    }

    private JPanel initStatusBar() {
        final JPanel statusBarPanel = new JPanel();
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

    @Subscribe
    public void initProceeded(final InitProceededEvent event) {
        initProceeded(event.getMessage());
    }

    @Subscribe
    public void updateCommandLog(final CommandLogEvent event) {
        updateCommandHistory(event.getMessage());
    }

    @Subscribe
    public void updateMainTitle(final UpdateMainTitleEvent event) {
        setTitle(event.getTitle());
    }

    private class CommandHistoryUpdater implements Runnable {

        private final String text;

        public CommandHistoryUpdater(final String text) {
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
            super("New\u2026", createImageIconFromIconSet("document-new.png"));
        }

        @Override
        public void actionPerformed(final ActionEvent arg0) {
            try {
                if (isFileMode()) {

                    final int result = JOptionPane.showConfirmDialog(
                            AdHocRailway.this,
                            "Do you want to save the actual configuration?",
                            "New file...", JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            createImageIconFromIconSet("dialog-warning.png"));
                    if (result == JOptionPane.YES_OPTION) {
                        saveActualFile();
                    }
                }
                persistenceManager.createNewFile();

                disableEnableMenuItems();

                setRailwayDeviceLabelText();

                setTitle(AdHocRailway.TITLE + " []");
                appContext.setActualFile(null);
                updateGUI();
                updateCommandHistory("Empty AdHoc-Railway Configuration created");
            } catch (final IOException e) {
                handleException(e);
            }
        }
    }

    private class OpenFileAction extends AbstractAction {


        public OpenFileAction() {
            super("Open\u2026", createImageIconFromIconSet("document-open.png"));
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            final JFileChooser fileChooser = new JFileChooser(new File("."));
            final int returnVal = fileChooser.showOpenDialog(AdHocRailway.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File actualFile = fileChooser.getSelectedFile();
                openFile(actualFile);
            } else {
                updateCommandHistory("Open command cancelled by user");
            }

        }

        public void openFile(final File file) {
            final Thread t = new Thread(new Runnable() {
                @Override
                public void run() {

                    try {

                        railwayDeviceManager.disconnect();
                        progressBar.setIndeterminate(true);
                        disableEnableMenuItems();

                        persistenceManager.openFile(file);

                        progressBar.setIndeterminate(false);
                    } catch (final IOException e) {
                        handleException(e);
                    }

                }

            });
            t.start();
        }
    }

    private class OpenDatabaseAction extends AbstractAction {


        public OpenDatabaseAction() {
            super("Open Database\u2026",
                    createImageIconFromIconSet("network-server-database.png"));
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            try {

                railwayDeviceManager.disconnect();
                progressBar.setIndeterminate(true);
                disableEnableMenuItems();

                persistenceManager.openDatabase();

                updateGUI();
                progressBar.setIndeterminate(false);
            } catch (final IOException e1) {
                handleException(e1);
            }
        }
    }

    private class ClearLocomotivesAction extends AbstractAction {


        public ClearLocomotivesAction() {
            super("Clear Locomotives\u2026",
                    createImageIconFromIconSet("list-remove.png"));
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            progressBar.setIndeterminate(true);
            final int result = JOptionPane.showConfirmDialog(AdHocRailway.this,
                    "Do you REALLY want to remove all locomotives?",
                    "Remove all locmotives", JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    createImageIconFromIconSet("dialog-warning.png"));
            if (result == JOptionPane.YES_OPTION) {
                appContext.getLocomotiveManager().clearToService();
            }

            updateGUI();
            progressBar.setIndeterminate(false);
        }
    }

    private class ClearTurnoutsAndRoutesAction extends AbstractAction {


        public ClearTurnoutsAndRoutesAction() {
            super("Clear Turnouts and Routes\u2026",
                    createImageIconFromIconSet("list-remove.png"));
        }

        @Override
        public void actionPerformed(final ActionEvent e) {

            progressBar.setIndeterminate(true);
            final int result = JOptionPane.showConfirmDialog(AdHocRailway.this,
                    "Do you REALLY want to remove all turnouts and routes?",
                    "Remove all turnouts and routes",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                    createImageIconFromIconSet("dialog-warning.png"));
            if (result == JOptionPane.YES_OPTION) {
                appContext.getRouteManager().clearToService();
                appContext.getTurnoutManager().clearToService();
            }

            updateGUI();
            progressBar.setIndeterminate(false);
        }
    }

    private class SaveAction extends AbstractAction {
        public SaveAction() {
            super("Save", createImageIconFromIconSet("document-save.png"));
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            if (appContext.getActualFile() == null) {
                new SaveAsAction().actionPerformed(null);
            }
            saveActualFile();
        }
    }

    private class SaveAsAction extends AbstractAction {
        public SaveAsAction() {
            super("Save as\u2026",
                    createImageIconFromIconSet("document-save-as.png"));
        }

        @Override
        public void actionPerformed(final ActionEvent e) {

            final JFileChooser fileChooser = new JFileChooser(new File("."));
            final int returnVal = fileChooser.showSaveDialog(AdHocRailway.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                final File saveFile = fileChooser.getSelectedFile();
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
        public void actionPerformed(final ActionEvent e) {

            progressBar.setIndeterminate(true);
            final JFileChooser fileChooser = new JFileChooser(new File("."));
            final int returnVal = fileChooser.showOpenDialog(AdHocRailway.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                final int result = JOptionPane.showConfirmDialog(AdHocRailway.this,
                        "Do you REALLY want to import all locomotives? This will delete all previous data!",
                        "Import",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                        createImageIconFromIconSet("dialog-warning.png"));
                if (result == JOptionPane.YES_OPTION) {
                    new XMLServiceHelper().importLocomotivesFromFile(
                            fileChooser.getSelectedFile(),
                            appContext.getLocomotiveManager());
                }
            }

            progressBar.setIndeterminate(false);
        }
    }

    private class ImportAllAction extends AbstractAction {

        public ImportAllAction() {
            super("Import Locomotives, Tunouts and Routes");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {

            progressBar.setIndeterminate(true);
            appContext.getMainBus().post(new StartImportEvent());
            progressBar.setIndeterminate(true);
            final JFileChooser fileChooser = new JFileChooser(new File("."));
            final int returnVal = fileChooser.showOpenDialog(AdHocRailway.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                final int result = JOptionPane.showConfirmDialog(AdHocRailway.this,
                        "Do you REALLY want to import all locomotives,turnouts and routes? This will delete all previous data!",
                        "Import",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                        createImageIconFromIconSet("dialog-warning.png"));
                if (result == JOptionPane.YES_OPTION) {


                    new XMLServiceHelper().importAllFromFile(
                            fileChooser.getSelectedFile(),
                            appContext.getLocomotiveManager(), appContext.getTurnoutManager(), appContext.getRouteManager());
                }
            }

            progressBar.setIndeterminate(false);
            appContext.getMainBus().post(new EndImportEvent());
        }
    }

    private class ExportLocomotivesAction extends AbstractAction {


        public ExportLocomotivesAction() {
            super("Export Locomotives");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            final JFileChooser fileChooser = new JFileChooser(new File("."));
            final int returnVal = fileChooser.showSaveDialog(AdHocRailway.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                try {
                    new XMLServiceHelper().exportLocomotivesToFile(
                            fileChooser.getSelectedFile(),
                            appContext.getLocomotiveManager());
                } catch (final IOException e1) {
                    handleException("failed to export locomotives", e1);
                }
            }
        }
    }

    private class ExportAllAction extends AbstractAction {


        public ExportAllAction() {
            super("Export Locomotives, Turnouts, Routes");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            final JFileChooser fileChooser = new JFileChooser(new File("."));
            final int returnVal = fileChooser.showSaveDialog(AdHocRailway.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                try {
                    new XMLServiceHelper().saveFile(
                            appContext.getLocomotiveManager(),
                            appContext.getTurnoutManager(),
                            appContext.getRouteManager(),
                            fileChooser.getSelectedFile());
                } catch (final IOException e1) {
                    handleException("failed to export locomotives, turnouts and routes", e1);
                }
            }
        }
    }

    private class ExitAction extends AbstractAction {

        public ExitAction() {
            super("Exit", createImageIconFromIconSet("application-exit.png"));
        }

        @Override
        public void actionPerformed(final ActionEvent e) {

            final int activeBoosterCount = appContext.getActiveBoosterCount();
            if (activeBoosterCount > 0) {
                final int exit = JOptionPane
                        .showConfirmDialog(
                                AdHocRailway.this,
                                "There are still "
                                        + activeBoosterCount
                                        + " boosters running.\nDo you really want to exit the application?",
                                "Active Boosters",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE,
                                createImageIconFromIconSet("dialog-warning.png")
                        );
                if (exit == JOptionPane.NO_OPTION
                        || exit == JOptionPane.CANCEL_OPTION || exit == -1) {
                    return;
                }
            }

            if (appContext.getRailwayDeviceManager().isConnected()) {
                try {
                    appContext.getLockControl().releaseAllLocks();
                    appContext.getLocomotiveControl().emergencyStopActiveLocos();
                } catch (final SRCPLockingException e1) {
                    handleException(e1);
                }
            }

            if (appContext.getActualFile() != null) {
                preferences.setStringValue(PreferencesKeys.LAST_OPENED_FILE,
                        appContext.getActualFile().getAbsolutePath());
                try {
                    preferences.save();
                } catch (final IOException e1) {
                    throw new AdHocRailwayException(
                            "could not save preferences");
                }
            }
            persistenceManager.disconnectFromCurrentPersistence();
            System.exit(0);
        }
    }

    private class TurnoutAction extends AbstractAction {

        public TurnoutAction() {
            super("Turnouts\u2026", createImageIconFromCustom("switch.png"));
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            final TurnoutConfigurationDialog turnoutConfigurationDialog = new TurnoutConfigurationDialog(
                    AdHocRailway.this, appContext);
            if (turnoutConfigurationDialog.isOkPressed()) {
                updateCommandHistory("Turnout configuration changed");
            }
        }
    }

    private class RoutesAction extends AbstractAction {

        public RoutesAction() {
            super("Routes\u2026", createImageIconFromCustom("route_edit.png"));
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            new RoutesConfigurationDialog(AdHocRailway.this, appContext);
        }
    }

    private class LocomotivesAction extends AbstractAction {


        public LocomotivesAction() {
            super("Locomotives\u2026",
                    createImageIconFromCustom("locomotive.png"));
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            final LocomotiveConfigurationDialog locomotiveConfigDialog = new LocomotiveConfigurationDialog(
                    appContext, AdHocRailway.this);
            if (locomotiveConfigDialog.isOkPressed()) {
                updateCommandHistory("Locomotive configuration changed");
            }
        }
    }

    private class PreferencesAction extends AbstractAction {

        public PreferencesAction() {
            super("Preferences\u2026",
                    createImageIconFromIconSet("preferences-system.png"));
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            final PreferencesDialog p = new PreferencesDialog(
                    AdHocRailway.this, appContext);
            if (p.isOkPressed()) {
                updateGUI();
                setRailwayDeviceLabelText();
                updateCommandHistory("Preferences saved to: "
                        + preferences.getConfigFile());

                railwayDeviceManager.disconnect();
            }
        }
    }

    private class ConnectAction extends AbstractAction {

        public ConnectAction() {
            super("Connect", createImageIconFromCustom("daemonconnect.png"));
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            railwayDeviceManager.connect();
        }
    }

    private class DisconnectAction extends AbstractAction {

        public DisconnectAction() {
            super("Disconnect",
                    createImageIconFromCustom("daemondisconnect.png"));
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            railwayDeviceManager.disconnect();
        }

    }

    private class PowerOnAction extends AbstractAction {

        public PowerOnAction() {
            super("Power On");
        }

        @Override
        public void actionPerformed(final ActionEvent arg0) {
            final PowerController powerControl = appContext.getPowerControl();
            powerControl.powerOn(powerControl.getPowerSupply(1));
        }
    }

    private class PowerOffAction extends AbstractAction {

        public PowerOffAction() {
            super("Power Off");
        }

        @Override
        public void actionPerformed(final ActionEvent arg0) {
            final PowerController powerControl = appContext.getPowerControl();
            powerControl.powerOff(powerControl.getPowerSupply(1));
        }
    }

    private class ToggleFullscreenAction extends AbstractAction {

        public ToggleFullscreenAction() {
            super("ToggleFullscreen",
                    createImageIconFromIconSet("view-fullscreen.png"));
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            if (fullscreen) {
                dispose();
                menuBar.setVisible(true);
                setResizable(true);
                setUndecorated(false);
                setSize(1000, 700);
                setVisible(true);
                toggleFullscreenButton
                        .setIcon(createImageIconFromIconSet("view-fullscreen.png"));
                fullscreen = false;
            } else {
                dispose();
                menuBar.setVisible(false);
                setResizable(false);
                setUndecorated(true);
                setSize(java.awt.Toolkit.getDefaultToolkit().getScreenSize());
                setVisible(true);
                toggleFullscreenButton
                        .setIcon(createImageIconFromIconSet("view-fullscreen.png"));
                fullscreen = true;
            }
        }
    }

    private class EnableEditingAction extends AbstractAction {


        public EnableEditingAction() {
            super("Edit mode");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            final boolean editingMode = enableEditing.isSelected();
            appContext.getMainBus().post(new EditingModeEvent(editingMode));

        }
    }
}
