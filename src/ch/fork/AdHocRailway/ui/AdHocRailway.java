/*------------------------------------------------------------------------
 * 
 * <./ui/AdHocRailway.java>  -  <desc>
 * 
 * begin     : Wed Aug 23 17:00:30 BST 2006
 * copyright : (C) by Benjamin Mueller 
 * email     : news@fork.ch
 * language  : java
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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ConnectException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

import javax.swing.AbstractAction;
import javax.swing.Box;
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
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import ch.fork.AdHocRailway.domain.exception.ControlException;
import ch.fork.AdHocRailway.domain.locking.LockControl;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveControl;
import ch.fork.AdHocRailway.domain.routes.RouteControl;
import ch.fork.AdHocRailway.domain.switches.Switch;
import ch.fork.AdHocRailway.domain.switches.SwitchControl;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;
import ch.fork.AdHocRailway.technical.configuration.XMLExporter;
import ch.fork.AdHocRailway.technical.configuration.XMLImporter;
import ch.fork.AdHocRailway.technical.configuration.exception.ConfigurationException;
import ch.fork.AdHocRailway.ui.locomotives.LocomotiveControlPanel;
import ch.fork.AdHocRailway.ui.locomotives.configuration.LocomotiveConfiguration;
import ch.fork.AdHocRailway.ui.locomotives.configuration.LocomotiveConfigurationDialog;
import ch.fork.AdHocRailway.ui.routes.configuration.RoutesConfiguration;
import ch.fork.AdHocRailway.ui.routes.configuration.RoutesConfigurationDialog;
import ch.fork.AdHocRailway.ui.switches.SwitchProgrammer;
import ch.fork.AdHocRailway.ui.switches.configuration.SwitchConfiguration;
import ch.fork.AdHocRailway.ui.switches.configuration.SwitchConfigurationDialog;
import de.dermoba.srcp.client.CommandDataListener;
import de.dermoba.srcp.client.InfoDataListener;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.common.exception.SRCPException;

public class AdHocRailway extends JFrame implements CommandDataListener,
        InfoDataListener, PreferencesKeys {

    private static final long      serialVersionUID  = 1L;
    private static final String    NAME              = "AdHoc-Railway";
    private SRCPSession            session;
    private SwitchControl          switchControl     = SwitchControl
                                                             .getInstance();
    private LocomotiveControl      locomotiveControl = LocomotiveControl
                                                             .getInstance();
    private LockControl            lockControl       = LockControl
                                                             .getInstance();
    private Preferences            preferences       = Preferences
                                                             .getInstance();

    // GUI-Components
    private TrackControlPanel      trackControlPanel;
    private LocomotiveControlPanel locomotiveControlPanel;
    private JPanel                 statusBarPanel;
    private JLabel                 hostnameLabel;
    private JButton                connectToolBarButton;
    private JButton                disconnectToolBarButton;
    private JComboBox              commandHistory;
    private DefaultComboBoxModel   commandHistoryModel;
    private JMenuItem              daemonConnectItem;
    private JMenuItem              daemonDisconnectItem;
    private JMenuItem              daemonResetItem;
    private JMenu                  recentFilesMenu;
    private File                   actualFile;
    private JButton                toggleFullscreenButton;
    private JMenuBar               menuBar;

    boolean                        fullscreen        = false;
    private SplashWindow           splash;
    private JTabbedPane            trackControl;

    public AdHocRailway() {
        this(null);
    }

    public AdHocRailway(String file) {
        super(NAME);
        // Plastic3DLookAndFeel.setPlasticTheme(new DesertBlue());
        // try {
        // UIManager.setLookAndFeel(new Plastic3DLookAndFeel());
        // } catch (Exception e) {}
        splash = new SplashWindow(createImageIcon("splash.png"), this, 500, 4);
        splash.nextStep("Starting AdHoc-Railway ...");
        setIconImage(createImageIcon("RailControl.png").getImage());

        loadConfiguration(file);

        splash.nextStep("Creating GUI ...");
        initGUI();

        trackControlPanel.update();
        locomotiveControlPanel.update(locomotiveControl.getLocomotiveGroups());

        initKeyboardActions();

        //setSize(1000, 700);

        setSize(java.awt.Toolkit.getDefaultToolkit().getScreenSize());

        splash.nextStep("RailControl started");
        updateCommandHistory("RailControl started");
        if (preferences.getBooleanValue(AUTOCONNECT))
            new ConnectAction().actionPerformed(null);
        //pack();
        setVisible(true);
    }

    private void loadConfiguration(String file) {
        if (file != null) {
            File standardFile = new File(file);
            if (standardFile.exists()) {
                splash.nextStep("Loading configuration from " + file + " ...");
                OpenAction oa = new OpenAction(null);
                oa.openFile(standardFile);
            } else {
                splash.nextStep("");
            }
        } else {
            splash.nextStep("");
        }
    }

    private void initGUI() {

        setFont(new Font("Verdana", Font.PLAIN, 19));
        setLayout(new BorderLayout());
        
        
        trackControlPanel = new TrackControlPanel(this); 
        locomotiveControlPanel = initLocomotiveControl();
        statusBarPanel = initStatusBar();
        
        initMenu();
        initToolbar();
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.add(trackControlPanel, BorderLayout.CENTER);
        mainPanel.add(locomotiveControlPanel, BorderLayout.SOUTH);
        add(mainPanel, BorderLayout.CENTER);
        add(statusBarPanel, BorderLayout.SOUTH);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                new ExitAction().actionPerformed(null);
            }
        });
    }

    private LocomotiveControlPanel initLocomotiveControl() {
        LocomotiveControlPanel locomotiveControlPanel = new LocomotiveControlPanel(
                this);
        
        return locomotiveControlPanel;
    }

    private void initKeyboardActions() {
        if (trackControl != null) {
            trackControl.registerKeyboardAction(new AbstractAction() {

                public void actionPerformed(ActionEvent e) {
                    if (trackControl.getSelectedIndex() == 0)
                        trackControl.setSelectedIndex(1);
                    else
                        trackControl.setSelectedIndex(0);
                }

            }, "", KeyStroke.getKeyStroke(KeyEvent.VK_BACK_QUOTE, 0),
                    JComponent.WHEN_IN_FOCUSED_WINDOW);
        }
    }

    private void updateGUI() {
    	hostnameLabel.setText(preferences.getStringValue("Hostname"));
    	updateSwitches();
    	updateLocomotives();
    }

    private void updateLocomotives() {
        locomotiveControlPanel.update(locomotiveControl.getLocomotiveGroups());
    }

    private void updateSwitches() {
        trackControlPanel.update();
    }

    public void commandDataSent(String commandData) {
        if (preferences.getBooleanValue(LOGGING)) {
            updateCommandHistory("To Server: " + commandData);
        }
    }

    public void infoDataReceived(String infoData) {
        if (preferences.getBooleanValue(LOGGING)) {
            updateCommandHistory("From Server: " + infoData);
        }
    }

    public void infoDataReceived(double timestamp, int bus, String deviceGroup,
            String data) {

    }

    public void updateCommandHistory(String text) {
        DateFormat df = new SimpleDateFormat("HH:mm:ss.SS");
        String date = df.format(GregorianCalendar.getInstance().getTime());
        String fullText = "[" + date + "]: " + text;
        SwingUtilities.invokeLater(new CommandHistoryUpdater(fullText));

    }

    private class CommandHistoryUpdater implements Runnable {

        private String text;

        public CommandHistoryUpdater(String text) {
            this.text = text;
        }

        public void run() {
            if (commandHistoryModel.getSize() > 100) {
                commandHistoryModel.removeElementAt(100);
            }
            commandHistoryModel.insertElementAt(text, 0);
            commandHistory.setSelectedIndex(0);
        }
    }

    private class NewAction extends AbstractAction {

        public NewAction() {
            super("New file", createImageIcon("filenew.png"));
        }

        public void actionPerformed(ActionEvent e) {
            actualFile = null;
            switchControl.clear();
            locomotiveControl.clear();
            RouteControl.getInstance().clear();
            updateGUI();
            updateCommandHistory("Created new configuration");
            setTitle(AdHocRailway.NAME + " : [ ]");
        }
    }

    private class OpenAction extends AbstractAction {

        private File file;

        public OpenAction(File file) {
            super("Open file...", createImageIcon("fileopen.png"));
        }

        public void actionPerformed(ActionEvent e) {
            if (file == null) {
                JFileChooser fileChooser = new JFileChooser(new File("."));
                int returnVal = fileChooser.showOpenDialog(AdHocRailway.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    file = fileChooser.getSelectedFile();
                    openFile(file);
                    updateGUI();
                } else {
                    updateCommandHistory("Open command cancelled by user");
                }
            } else {
                // openFile(file);
            }
        }

        private void openFile(File file) {

            try {
                new XMLImporter(file.getAbsolutePath());

                /*
                 * if (recentFilesMenu.getComponentCount() > 1) {
                 * recentFilesMenu.remove(10); } JMenuItem recentItem = new
                 * JMenuItem(file.getPath()); recentItem.addActionListener(new
                 * OpenAction(file)); recentFilesMenu.add(recentItem, 0);
                 * recentFilesMenu.repaint(); recentFilesMenu.revalidate();
                 */
                actualFile = file;
                // updateCommandHistory("Opened configuration: " +
                // file.getName());
                setTitle(AdHocRailway.NAME + " : [ "
                        + actualFile.getAbsolutePath() + " ]");
            } catch (ConfigurationException e) {
                ExceptionProcessor.getInstance().processException(
                        "Error loading file", e);
            }

        }
    }

    private class SaveAsAction extends AbstractAction {

        public SaveAsAction() {
            super("Save as...", createImageIcon("filesaveas.png"));
        }

        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser(new File("."));
            int returnVal = fileChooser.showSaveDialog(AdHocRailway.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                // This is where a real application would open
                // the file.
                FileWriter fileWriter = null;
                try {
                    String xmlConfig = XMLExporter
                            .export(Constants.ACTUAL_CONFIG_VERSION);
                    fileWriter = new FileWriter(file);
                    fileWriter.write(xmlConfig);
                    updateCommandHistory("Configuration saved: "
                            + file.getName());
                } catch (IOException e1) {
                    ExceptionProcessor.getInstance().processException(
                            "Error writing file", e1);
                } catch (ConfigurationException e1) {
                    ExceptionProcessor.getInstance().processException(
                            "Error writing file", e1);
                } finally {
                    try {
                        fileWriter.close();
                    } catch (IOException e1) {
                        ExceptionProcessor.getInstance().processException(
                                "Error writing file", e1);
                    }
                }
                actualFile = file;
            } else {
                updateCommandHistory("Save cancelled by user");
            }
        }
    }
    private class SaveAction extends AbstractAction {

        public SaveAction() {
            super("Save", createImageIcon("filesave.png"));
        }

        public void actionPerformed(ActionEvent e) {
            if (actualFile != null) {

                FileWriter fileWriter = null;
                try {
                    String xmlConfig = XMLExporter
                            .export(Constants.ACTUAL_CONFIG_VERSION);
                    fileWriter = new FileWriter(actualFile);
                    fileWriter.write(xmlConfig);
                    updateCommandHistory("Configuration saved: "
                            + actualFile.getName());
                } catch (ConfigurationException e1) {
                    ExceptionProcessor.getInstance().processException(
                            "Error writing file", e1);

                } catch (IOException e1) {
                    ExceptionProcessor.getInstance().processException(
                            "Error writing file", e1);
                } finally {
                    try {
                        fileWriter.close();
                    } catch (IOException e1) {
                        ExceptionProcessor.getInstance().processException(
                                "Error writing file", e1);
                    }
                }
            } else {
                SaveAsAction sa = new SaveAsAction();
                sa.actionPerformed(e);
            }
        }
    }
    private class ExitAction extends AbstractAction {

        public ExitAction() {
            super("Exit", createImageIcon("exit.png"));
        }

        public void actionPerformed(ActionEvent e) {
            int result = JOptionPane.showConfirmDialog(AdHocRailway.this,
                    "Really exit ?", "Exit", JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,createImageIcon("messagebox_warning.png"));
            if (result == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        }
    }
    private class SwitchesAction extends AbstractAction {

        public SwitchesAction() {
            super("Switches", createImageIcon("switch.png"));
        }

        public void actionPerformed(ActionEvent e) {
            SwitchConfigurationDialog switchConfigDialog = new SwitchConfigurationDialog(
                    AdHocRailway.this);
            if (switchConfigDialog.isOkPressed()) {

                SwitchConfiguration switchConf = (SwitchConfiguration) switchConfigDialog
                        .getTempConfiguration();
                switchControl.clear();
                switchControl
                        .registerSwitchGroups(switchConf.getSwitchGroups());
                switchControl.registerSwitches(switchConf
                        .getSwitchNumberToSwitch().values());
                updateSwitches();
                updateCommandHistory("Switch configuration changed");
            }
        }
    }

    private class RoutesAction extends AbstractAction {

        public RoutesAction() {
            super("Routes", createImageIcon("route_edit.png"));
        }

        public void actionPerformed(ActionEvent e) {
            RoutesConfigurationDialog routesConfig = new RoutesConfigurationDialog(
                    AdHocRailway.this);
            if (routesConfig.isOkPressed()) {
                RouteControl rc = RouteControl.getInstance();
                RoutesConfiguration routesConfiguration = routesConfig
                        .getTempConfiguration();
                rc.clear();
                rc.registerRoutes(routesConfiguration.getRoutes());
                trackControlPanel.update();
                updateCommandHistory("Routes configuration changed");
            }
        }
    }
    private class LocomotivesAction extends AbstractAction {

        public LocomotivesAction() {
            super("Locomotives", createImageIcon("locomotive.png"));
        }

        public void actionPerformed(ActionEvent e) {
            LocomotiveConfigurationDialog locomotiveConfigDialog = new LocomotiveConfigurationDialog(
                    AdHocRailway.this);
            if (locomotiveConfigDialog.isOkPressed()) {
                LocomotiveConfiguration locomotiveConfiguration = locomotiveConfigDialog
                        .getTempConfiguration();
                locomotiveControl.clear();
                System.out.println(locomotiveConfiguration
                        .getLocomotives());
                locomotiveControl.registerLocomotives(locomotiveConfiguration
                        .getLocomotives());
                locomotiveControl
                        .registerLocomotiveGroups(locomotiveConfiguration
                                .getLocomotiveGroups());
                updateLocomotives();
                updateCommandHistory("Locomotive configuration changed");
            }
        }
    }
    private class PreferencesAction extends AbstractAction {

        public PreferencesAction() {
            super("Preferences", createImageIcon("package_settings.png"));
        }

        public void actionPerformed(ActionEvent e) {
            PreferencesDialog p = new PreferencesDialog(AdHocRailway.this);
            if (p.isOkPressed()) {
                updateGUI();
                hostnameLabel.setText(preferences.getStringValue("Hostname"));
                updateCommandHistory("Preferences changed");
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
            super("Connect", createImageIcon("daemonconnect.png"));
        }

        public void actionPerformed(ActionEvent e) {
            try {
                String host = preferences.getStringValue(HOSTNAME);
                int port = preferences.getIntValue(PORT);
                session = new SRCPSession(host, port, false);
                session.getCommandChannel().addCommandDataListener(
                        AdHocRailway.this);
                session.getInfoChannel().addInfoDataListener(AdHocRailway.this);
                switchControl.setSession(session);
                locomotiveControl.setSession(session);
                lockControl.setSession(session);
                session.connect();
                daemonConnectItem.setEnabled(false);
                daemonDisconnectItem.setEnabled(true);
                daemonResetItem.setEnabled(true);
                connectToolBarButton.setEnabled(false);
                disconnectToolBarButton.setEnabled(true);
                //updateGUI();
                updateCommandHistory("Connected to server " + host
                        + " on port " + port);
            } catch (SRCPException e1) {
            	System.out.println("here");
                if (e1.getCause() instanceof ConnectException) {
                	System.out.println("here");
                    ExceptionProcessor.getInstance().processException(
                            "Server not running", e1);
                }
            }
        }
    }
    private class DisconnectAction extends AbstractAction {

        public DisconnectAction() {
            super("Disconnect", createImageIcon("daemondisconnect.png"));
        }

        public void actionPerformed(ActionEvent e) {
            try {
                String host = preferences.getStringValue(HOSTNAME);
                int port = preferences.getIntValue(PORT);
                session.disconnect();
                session = null;
                switchControl.setSession(null);
                locomotiveControl.setSession(null);
                lockControl.setSession(null);
                daemonConnectItem.setEnabled(true);
                daemonDisconnectItem.setEnabled(false);
                daemonResetItem.setEnabled(false);
                connectToolBarButton.setEnabled(true);
                disconnectToolBarButton.setEnabled(false);
                updateCommandHistory("Disconnected from server " + host
                        + " on port " + port);
            } catch (SRCPException e1) {
                ExceptionProcessor.getInstance().processException(e1);
            }
        }
    }
    private class ResetAction extends AbstractAction {

        public ResetAction() {
            super("Reset", createImageIcon("daemonreset.png"));
        }

        public void actionPerformed(ActionEvent e) {
        }
    }
    private class RefreshAction extends AbstractAction {

        public RefreshAction() {
            super("Refresh", createImageIcon("reload.png"));
        }

        public void actionPerformed(ActionEvent e) {
            updateGUI();
        }
    }
    private class SwitchProgrammerAction extends AbstractAction {

        public SwitchProgrammerAction() {
            super("SwitchProgrammer", createImageIcon("switch_programmer.png"));
        }

        public void actionPerformed(ActionEvent e) {
            SwitchProgrammer sp = new SwitchProgrammer(AdHocRailway.this,
                    session);
        }
    }
    private class ToggleFullscreenAction extends AbstractAction {

        public ToggleFullscreenAction() {
            super("ToggleFullscreen",createImageIcon("window_fullscreen.png"));
        }

        public void actionPerformed(ActionEvent e) {
            if (fullscreen) {
                dispose();
                menuBar.setVisible(true);
                setResizable(true);
                setUndecorated(false);
                setSize(1000, 700);
                setVisible(true);
                toggleFullscreenButton.setIcon(createImageIcon("window_fullscreen.png"));
                fullscreen = false;
            } else {
                dispose();
                menuBar.setVisible(false);
                setResizable(false);
                setUndecorated(true);
                setSize(java.awt.Toolkit.getDefaultToolkit().getScreenSize());
                setVisible(true);
                toggleFullscreenButton.setIcon(createImageIcon("window_nofullscreen.png"));
                fullscreen = true;
            }
        }
    }
    private class SwitchesStraightAction extends AbstractAction {

        public SwitchesStraightAction() {
            super("Set all switches straight", createImageIcon("switch.png"));
        }

        public void actionPerformed(ActionEvent e) {
            SwitchStraighter s = new SwitchStraighter();
            s.start();
        }

        private class SwitchStraighter extends Thread {

            public void run() {
                try {

                    for (Switch s : switchControl.getNumberToSwitch().values()) {
                        switchControl.setDefaultState(s);
                        Thread.sleep(10);
                    }
                } catch (ControlException e1) {
                    ExceptionProcessor.getInstance().processException(e1);
                    return;
                } catch (InterruptedException e2) {
                    e2.printStackTrace();
                }
            }
        }
    }

    private void initMenu() {
        menuBar = new JMenuBar();
        /* FILE */
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        JMenuItem newItem = new JMenuItem(new NewAction());
        newItem.setMnemonic(KeyEvent.VK_N);
        newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
                ActionEvent.CTRL_MASK));

        JMenuItem openItem = new JMenuItem(new OpenAction(null));
        openItem.setMnemonic(KeyEvent.VK_O);
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
                ActionEvent.CTRL_MASK));

        JMenuItem saveAsItem = new JMenuItem(new SaveAsAction());

        JMenuItem saveItem = new JMenuItem(new SaveAction());
        saveItem.setMnemonic(KeyEvent.VK_S);
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                ActionEvent.CTRL_MASK));

        recentFilesMenu = new JMenu("Recent files...");

        JMenuItem exitItem = new JMenuItem(new ExitAction());
        exitItem.setMnemonic(KeyEvent.VK_X);
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
                ActionEvent.CTRL_MASK));

        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.add(saveAsItem);
        fileMenu.add(recentFilesMenu);
        fileMenu.add(new JSeparator());
        fileMenu.add(exitItem);

        /* EDIT */
        JMenu editMenu = new JMenu("Edit");
        JMenuItem switchesItem = new JMenuItem(new SwitchesAction());
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
        daemonResetItem = new JMenuItem(new ResetAction());
        daemonDisconnectItem.setEnabled(false);
        daemonResetItem.setEnabled(false);
        daemonMenu.add(daemonConnectItem);
        daemonMenu.add(daemonDisconnectItem);
        daemonMenu.add(new JSeparator());
        daemonMenu.add(daemonResetItem);

        /* VIEW */
        JMenu viewMenu = new JMenu("View");
        JMenuItem refreshItem = new JMenuItem(new RefreshAction());
        JMenuItem fullscreenItem = new JMenuItem(new ToggleFullscreenAction());

        viewMenu.add(refreshItem);
        viewMenu.add(fullscreenItem);

        /* TOOLS */
        JMenu toolsMenu = new JMenu("Tools");
        JMenuItem switchesStraightItem = new JMenuItem(
                new SwitchesStraightAction());
        JMenuItem switchProgrammerItem = new JMenuItem(
                new SwitchProgrammerAction());

        toolsMenu.add(switchesStraightItem);
        toolsMenu.add(switchProgrammerItem);

        /* HELP */
        JMenu helpMenu = new JMenu("Help");

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(viewMenu);
        menuBar.add(daemonMenu);
        menuBar.add(toolsMenu);
        menuBar.add(Box.createGlue());
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);
    }

    private void initToolbar() {
        /* FILE */
        JToolBar fileTooBar = new JToolBar();
        JButton newToolBarButton = new SmallToolbarButton(new NewAction());
        JButton openToolBarButton = new SmallToolbarButton(new OpenAction(null));
        JButton saveToolBarButton = new SmallToolbarButton(new SaveAction());
        // JButton saveAsToolBarButton = new SmallToolbarButton(new
        // SaveAsAction());
        JButton exitToolBarButton = new SmallToolbarButton(new ExitAction());

        fileTooBar.add(newToolBarButton);
        fileTooBar.add(openToolBarButton);
        fileTooBar.add(saveToolBarButton);
        fileTooBar.add(exitToolBarButton);

        /* DIGITAL */
        JToolBar digitalToolBar = new JToolBar();
        JButton switchesToolBarButton = new SmallToolbarButton(
                new SwitchesAction());
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

        /* TOOLS */
        JToolBar toolsToolBar = new JToolBar();
        JButton setAllSwitchesStraightButton = new SmallToolbarButton(
                new SwitchesStraightAction());
        JButton switchProgrammerButton = new SmallToolbarButton(
                new SwitchProgrammerAction());

        toolsToolBar.add(setAllSwitchesStraightButton);
        toolsToolBar.add(switchProgrammerButton);

        /* VIEWS */
        JToolBar viewToolBar = new JToolBar();
        JButton refreshButton = new SmallToolbarButton(new RefreshAction());
        toggleFullscreenButton = new SmallToolbarButton(
                new ToggleFullscreenAction());

        viewToolBar.add(refreshButton);
        viewToolBar.add(toggleFullscreenButton);

        /* ERROR */
        ErrorPanel errorPanel = new ErrorPanel();
        ExceptionProcessor.getInstance(errorPanel);
        
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0,
                0));
        toolbarPanel.add(fileTooBar);
        toolbarPanel.add(digitalToolBar);
        toolbarPanel.add(daemonToolBar);
        toolbarPanel.add(toolsToolBar);
        toolbarPanel.add(viewToolBar);
        //toolbarPanel.add(errorPanel);
        
        JPanel toolbarErrorPanel = new JPanel(new BorderLayout(10,10));
        toolbarErrorPanel.add(toolbarPanel, BorderLayout.WEST);
        toolbarErrorPanel.add(errorPanel, BorderLayout.EAST);
        
        add(toolbarErrorPanel, BorderLayout.PAGE_START);
    }

    private JPanel initStatusBar() {
        JPanel statusBarPanel = new JPanel();
        commandHistoryModel = new DefaultComboBoxModel();
        commandHistory = new JComboBox(commandHistoryModel);
        commandHistory.setEditable(false);
        commandHistory.setFocusable(false);
        statusBarPanel.setLayout(new BorderLayout());
        statusBarPanel.add(commandHistory, BorderLayout.SOUTH);
        return statusBarPanel;
    }

    public static void main(String[] args) {

        if (args.length == 1) {
            new AdHocRailway(args[0]);
        } else {
            new AdHocRailway();
        }
    }
}
