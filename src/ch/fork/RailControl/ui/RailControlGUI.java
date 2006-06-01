package ch.fork.RailControl.ui;

import static ch.fork.RailControl.ui.ImageTools.createImageIcon;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ConnectException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;

import ch.fork.RailControl.domain.configuration.Preferences;
import ch.fork.RailControl.domain.configuration.XMLImporter;
import ch.fork.RailControl.domain.locomotives.DeltaLocomotive;
import ch.fork.RailControl.domain.locomotives.DigitalLocomotive;
import ch.fork.RailControl.domain.locomotives.Locomotive;
import ch.fork.RailControl.domain.locomotives.LocomotiveControl;
import ch.fork.RailControl.domain.locomotives.exception.LocomotiveException;
import ch.fork.RailControl.domain.switches.Address;
import ch.fork.RailControl.domain.switches.DefaultSwitch;
import ch.fork.RailControl.domain.switches.DoubleCrossSwitch;
import ch.fork.RailControl.domain.switches.Switch;
import ch.fork.RailControl.domain.switches.SwitchControl;
import ch.fork.RailControl.domain.switches.SwitchGroup;
import ch.fork.RailControl.domain.switches.ThreeWaySwitch;
import ch.fork.RailControl.domain.switches.exception.SwitchException;
import ch.fork.RailControl.ui.locomotives.LocomotiveControlPanel;
import ch.fork.RailControl.ui.locomotives.configuration.LocomotiveConfigurationDialog;
import ch.fork.RailControl.ui.switches.DefaultSwitchCanvas;
import ch.fork.RailControl.ui.switches.DoubleCrossSwitchCanvas;
import ch.fork.RailControl.ui.switches.Segment7;
import ch.fork.RailControl.ui.switches.SwitchCanvas;
import ch.fork.RailControl.ui.switches.SwitchGroupPane;
import ch.fork.RailControl.ui.switches.ThreeWaySwitchCanvas;
import ch.fork.RailControl.ui.switches.configuration.SwitchConfigurationDialog;
import de.dermoba.srcp.client.CommandDataListener;
import de.dermoba.srcp.client.InfoDataListener;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.common.exception.SRCPException;

public class RailControlGUI extends JFrame implements CommandDataListener,
    InfoDataListener {

    private static final long serialVersionUID = 1L;

    private static final String NAME = "RailControl";

    private SRCPSession session;

    private String typedChars = "";

    // GUI-Components
    private SwitchGroupPane switchGroupPane;

    private LocomotiveControlPanel locomotiveControlPanel;

    private JPanel statusBarPanel;

    private JComboBox commandHistory;

    private DefaultComboBoxModel commandHistoryModel;

    // Datastructures
    private List<SwitchGroup> switchGroups;

    private JMenuItem daemonConnectItem;

    private JMenuItem daemonDisconnectItem;

    private JMenuItem daemonResetItem;

    private StringBuffer enteredNumberKeys;

    private List<Locomotive> locomotives;

    private Map<Integer, Switch> switchNumberToSwitch;

    private Segment7 seg1;

    private Segment7 seg2;

    private Segment7 seg3;

    private JPanel selectedSwitchDetails;

    private JButton connectToolBarButton;

    private JButton disconnectToolBarButton;

    private JMenu recentFilesMenu;

    private File actualFile;

    public RailControlGUI() {
        super(NAME);
        initGUI();
        initKeyboardActions();
        initDatastructures();

        File standardFile = new File("etc/standard.conf");
        if (standardFile.exists()) {
            OpenAction oa = new OpenAction(null);
            oa.openFile(standardFile);
        }
    }

    private void initDatastructures() {
        locomotives = new ArrayList<Locomotive>();
        switchNumberToSwitch = new HashMap<Integer, Switch>();
        enteredNumberKeys = new StringBuffer();
        switchGroups = new ArrayList<SwitchGroup>();
        // createDefaultData();

        LocomotiveControl.getInstance().registerLocomotives(locomotives);

        switchGroupPane.update(switchGroups);
        locomotiveControlPanel.update(locomotives);
        locomotiveControlPanel.revalidate();
        locomotiveControlPanel.repaint();
    }

    private void createDefaultData() {
        SwitchGroup main = new SwitchGroup("Main Line");
        SwitchGroup mountain = new SwitchGroup("Mountain Line");
        switchGroups.add(main);
        switchGroups.add(mountain);

        Switch switch1 = new DefaultSwitch(1, "HB 1", 1, new Address(1));
        Switch switch2 = new DoubleCrossSwitch(2, "Berg1", 1, new Address(
            2));
        Switch switch3 = new ThreeWaySwitch(3, "HB 2", 1,
            new Address(3, 4));

        main.addSwitch(switch1);
        main.addSwitch(switch2);
        main.addSwitch(switch3);

        switchNumberToSwitch.put(switch1.getNumber(), switch1);
        switchNumberToSwitch.put(switch2.getNumber(), switch2);
        switchNumberToSwitch.put(switch3.getNumber(), switch3);

        SwitchControl.getInstance().registerSwitches(
            switchNumberToSwitch.values());

        Locomotive ascom = new DeltaLocomotive(session, "Ascom", 1, 24,
            "RE460 'Ascom'");
        Locomotive bigBoy = new DigitalLocomotive(session, "Big Boy", 1,
            25, "UP Klasse 4000 'Big Boy'");
        locomotives.add(ascom);
        locomotives.add(bigBoy);
    }

    private void initGUI() {
        setFont(new Font("Verdana", Font.PLAIN, 19));
        setLayout(new BorderLayout());
        ExceptionProcessor.getInstance(this);
        JPanel switchPanel = initSwitchPanel();
        locomotiveControlPanel = initLocomotiveControl();
        JScrollPane locomotiveControlPane = new JScrollPane(
            locomotiveControlPanel);
        initMenu();
        initToolbar();
        initStatusBar();

        JPanel switchesAndLocomotivesPanel = new JPanel(new BorderLayout(
            5, 5));
        switchesAndLocomotivesPanel.add(switchPanel, BorderLayout.CENTER);

        switchesAndLocomotivesPanel.add(
            locomotiveControlPane, BorderLayout.SOUTH);
        add(switchesAndLocomotivesPanel, BorderLayout.CENTER);
        add(statusBarPanel, BorderLayout.SOUTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 700);
        setVisible(true);
        updateCommandHistory("RailControl started");
    }

    private JPanel initSwitchPanel() {
        JPanel switchPanel = new JPanel(new BorderLayout());
        switchPanel.setBorder(new EtchedBorder());
        switchGroupPane = new SwitchGroupPane(switchGroups);

        JPanel segmentPanelNorth = new JPanel(new FlowLayout(
            FlowLayout.TRAILING, 5, 0));
        segmentPanelNorth.setBackground(new Color(0, 0, 0));
        seg1 = new Segment7();
        seg2 = new Segment7();
        seg3 = new Segment7();
        segmentPanelNorth.add(seg3);
        segmentPanelNorth.add(seg2);
        segmentPanelNorth.add(seg1);

        selectedSwitchDetails = new JPanel();

        JPanel segmentPanel = new JPanel(new BorderLayout());
        segmentPanel.add(segmentPanelNorth, BorderLayout.NORTH);
        segmentPanel.add(selectedSwitchDetails, BorderLayout.CENTER);

        switchPanel.add(segmentPanel, BorderLayout.EAST);

        switchPanel.add(switchGroupPane, BorderLayout.CENTER);
        return switchPanel;
    }

    private LocomotiveControlPanel initLocomotiveControl() {
        LocomotiveControlPanel locomotiveControlPanel = new LocomotiveControlPanel();
        locomotiveControlPanel.setBorder(new EtchedBorder());
        return locomotiveControlPanel;
    }

    private void initToolbar() {
        JToolBar toolBar = new JToolBar();

        JButton openToolBarButton = new JButton(new OpenAction(null));
        JButton saveToolBarButton = new JButton(new SaveAction());
        JButton saveAsToolBarButton = new JButton(new SaveAsAction());
        JButton exitToolBarButton = new JButton(new ExitAction());
        JButton switchesToolBarButton = new JButton(new SwitchesAction());
        JButton locomotivesToolBarButton = new JButton(
            new LocomotivesAction());
        JButton preferencesToolBarButton = new JButton(
            new PreferencesAction());

        final JComboBox hostnamesComboBox = new JComboBox();
        for (String host : Preferences.getInstance().getHostnames()) {
            hostnamesComboBox.addItem(host);
        }
        hostnamesComboBox.setSelectedItem(Preferences.getInstance()
            .getStringValue("Hostname"));
        hostnamesComboBox.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Preferences.getInstance().setStringValue(
                    "Hostname",
                    hostnamesComboBox.getSelectedItem().toString());
            }

        });
        connectToolBarButton = new JButton(new ConnectAction());
        disconnectToolBarButton = new JButton(new DisconnectAction());
        disconnectToolBarButton.setEnabled(false);

        JButton setAllSwitchesStraightButton = new JButton(
            new SwitchesStraightAction());

        openToolBarButton.setText("");
        saveToolBarButton.setText("");
        saveAsToolBarButton.setText("");
        exitToolBarButton.setText("");
        switchesToolBarButton.setText("");
        locomotivesToolBarButton.setText("");
        preferencesToolBarButton.setText("");
        connectToolBarButton.setText("");
        disconnectToolBarButton.setText("");

        toolBar.add(openToolBarButton);
        toolBar.add(saveToolBarButton);
        toolBar.add(exitToolBarButton);
        toolBar.addSeparator();
        toolBar.add(switchesToolBarButton);
        toolBar.add(locomotivesToolBarButton);
        toolBar.add(preferencesToolBarButton);
        toolBar.addSeparator();
        toolBar.add(hostnamesComboBox);
        toolBar.add(connectToolBarButton);
        toolBar.add(disconnectToolBarButton);
        toolBar.addSeparator();
        toolBar.add(setAllSwitchesStraightButton);

        JPanel toolbarPanel = new JPanel(new BorderLayout());
        toolbarPanel.add(toolBar, BorderLayout.WEST);
        add(toolbarPanel, BorderLayout.PAGE_START);
    }

    private void initMenu() {
        JMenuBar menuBar = new JMenuBar();

        /* FILE */
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        JMenuItem openItem = new JMenuItem(new OpenAction(null));
        openItem.setMnemonic(KeyEvent.VK_O);
        openItem.setAccelerator(KeyStroke.getKeyStroke(
            KeyEvent.VK_O, ActionEvent.CTRL_MASK));

        JMenuItem saveAsItem = new JMenuItem(new SaveAsAction());

        JMenuItem saveItem = new JMenuItem(new SaveAction());
        saveItem.setMnemonic(KeyEvent.VK_S);
        saveItem.setAccelerator(KeyStroke.getKeyStroke(
            KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        recentFilesMenu = new JMenu("Recent files...");
        JMenuItem exitItem = new JMenuItem(new ExitAction());
        exitItem.setMnemonic(KeyEvent.VK_X);
        exitItem.setAccelerator(KeyStroke.getKeyStroke(
            KeyEvent.VK_X, ActionEvent.CTRL_MASK));

        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.add(saveAsItem);
        fileMenu.add(recentFilesMenu);
        fileMenu.add(new JSeparator());
        fileMenu.add(exitItem);

        /* EDIT */
        JMenu edit = new JMenu("Edit");
        JMenuItem switchesItem = new JMenuItem(new SwitchesAction());
        JMenuItem locomotivesItem = new JMenuItem(new LocomotivesAction());
        JMenuItem preferencesItem = new JMenuItem(new PreferencesAction());

        edit.setMnemonic(KeyEvent.VK_E);
        switchesItem.setMnemonic(KeyEvent.VK_S);
        switchesItem.setAccelerator(KeyStroke.getKeyStroke(
            KeyEvent.VK_S, ActionEvent.ALT_MASK));
        locomotivesItem.setMnemonic(KeyEvent.VK_L);
        locomotivesItem.setAccelerator(KeyStroke.getKeyStroke(
            KeyEvent.VK_L, ActionEvent.ALT_MASK));
        preferencesItem.setMnemonic(KeyEvent.VK_P);
        preferencesItem.setAccelerator(KeyStroke.getKeyStroke(
            KeyEvent.VK_P, ActionEvent.ALT_MASK));

        edit.add(switchesItem);
        edit.add(locomotivesItem);
        edit.add(new JSeparator());
        edit.add(preferencesItem);

        /* DAEMON */
        JMenu daemonMenu = new JMenu("Daemon");
        daemonConnectItem = new JMenuItem(new ConnectAction());
        daemonDisconnectItem = new JMenuItem(new DisconnectAction());
        daemonResetItem = new JMenuItem(new ResetAction());

        daemonDisconnectItem.setEnabled(false);
        daemonResetItem.setEnabled(false);
        daemonMenu.add(daemonConnectItem);
        daemonMenu.add(daemonDisconnectItem);
        daemonMenu.add(new JSeparator());
        daemonMenu.add(daemonResetItem);

        /* HELP */
        JMenu helpMenu = new JMenu("Help");

        menuBar.add(fileMenu);
        menuBar.add(edit);
        menuBar.add(daemonMenu);
        menuBar.add(Box.createGlue());
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);
    }

    private void initStatusBar() {
        statusBarPanel = new JPanel();
        commandHistoryModel = new DefaultComboBoxModel();
        commandHistory = new JComboBox(commandHistoryModel);
        commandHistory.setEditable(false);
        statusBarPanel.setLayout(new BorderLayout());
        statusBarPanel.add(commandHistory, BorderLayout.SOUTH);

    }

    private void initKeyboardActions() {
        for (int i = 0; i <= 10; i++) {
            switchGroupPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(
                    KeyStroke.getKeyStroke(Integer.toString(i)),
                    "numberKey");

            switchGroupPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("NUMPAD"
                    + Integer.toString(i)), "numberKey");
        }
        switchGroupPane.getActionMap().put(
            "numberKey", new NumberEnteredAction());

        switchGroupPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("ENTER"), "switchingAction");
        switchGroupPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ADD, 0),
                "switchingAction");
        switchGroupPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(
                KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0),
                "switchingAction");
        switchGroupPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(
                KeyStroke.getKeyStroke(KeyEvent.VK_DIVIDE, 0),
                "switchingAction");
        switchGroupPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(
                KeyStroke.getKeyStroke(KeyEvent.VK_MULTIPLY, 0),
                "switchingAction");
        switchGroupPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(
                KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, 0),
                "switchingAction");

        switchGroupPane.getActionMap().put(
            "switchingAction", new SwitchingAction());

        for (int i = 1; i <= 12; i++) {
            switchGroupPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("F"
                    + Integer.toString(i)), "f"
                    + Integer.toString(i));

            switchGroupPane.getActionMap().put("f"
                + Integer.toString(i), new SwitchGroupChangeAction(i - 1));
        }
    }

    public String toXML() {
        StringBuffer sb = new StringBuffer();
        sb.append("<RailControl xmlns=\"http://www.fork.ch/RailControl\" "
            + "ExporterVersion=\"0.1\">\n");
        sb.append("<SwitchConfiguration>\n");
        for (SwitchGroup sg : switchGroups) {
            sb.append(sg.toXML());
        }
        sb.append("</SwitchConfiguration>\n");
        sb.append("<LocomotiveConfiguration>\n");
        for (Locomotive l : locomotives) {
            sb.append(l.toXML());
        }
        sb.append("</LocomotiveConfiguration>\n");
        sb.append(Preferences.getInstance().toXML());
        sb.append("</RailControl>");
        return sb.toString();
    }

    public void processException(Exception e) {
        JOptionPane.showMessageDialog(
            this, e.getMessage(), "Error occured",
            JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }

    public static void main(String[] args) {
        new RailControlGUI();
    }

    public void commandDataSent(String commandData) {
        updateCommandHistory("To Server: "
            + commandData);
    }

    public void infoDataReceived(String infoData) {
        updateCommandHistory("From Server: "
            + infoData);
    }

    public void updateCommandHistory(String text) {
        DateFormat df = new SimpleDateFormat("HH:mm:ss.SS");
        String date = df.format(GregorianCalendar.getInstance().getTime());
        String fullText = "["
            + date + "]: " + text;
        SwingUtilities.invokeLater(new CommandHistoryUpdater(fullText));
    }

    private void resetSelectedSwitchDisplay() {
        enteredNumberKeys = new StringBuffer();
        seg1.setValue(0);
        seg2.setValue(0);
        seg3.setValue(0);
        seg1.repaint();
        seg2.repaint();
        seg3.repaint();
        selectedSwitchDetails.removeAll();
        selectedSwitchDetails.revalidate();
        selectedSwitchDetails.repaint();
    }

    private class CommandHistoryUpdater implements Runnable {

        private String text;

        public CommandHistoryUpdater(String text) {
            this.text = text;
        }

        public void run() {
            commandHistoryModel.insertElementAt(text, 0);
            commandHistory.setSelectedIndex(0);
        }
    }

    private class OpenAction extends AbstractAction {

        private File file;

        public OpenAction(File file) {
            super("Open file...", createImageIcon(
                "icons/fileopen.png", "File open...", RailControlGUI.this));
        }

        public void actionPerformed(ActionEvent e) {
            if (file == null) {
                JFileChooser fileChooser = new JFileChooser(new File("."));
                int returnVal = fileChooser
                    .showOpenDialog(RailControlGUI.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    file = fileChooser.getSelectedFile();
                    openFile(file);
                } else {
                    updateCommandHistory("Open command cancelled by user");
                }
            } else {
                // openFile(file);
            }
        }

        private void openFile(File file) {
            // This is where a real application would open the file.
            XMLImporter importer = new XMLImporter(Preferences
                .getInstance(), switchNumberToSwitch, switchGroups,
                locomotives, file.getAbsolutePath());
            switchGroupPane.update(switchGroups);
            SwitchControl sc = SwitchControl.getInstance();
            sc.unregisterAllSwitches();
            sc.registerSwitches(switchNumberToSwitch.values());
            sc.setSessionOnSwitches(session);

            try {
                LocomotiveControl lc = LocomotiveControl.getInstance();
                lc.unregisterAllLocomotives();
                lc.registerLocomotives(locomotives);
                lc.setSessionOnLocomotives(session);
                locomotiveControlPanel.update(locomotives);
            } catch (LocomotiveException e1) {
                ExceptionProcessor.getInstance().processException(e1);
            }

            if (recentFilesMenu.getComponentCount() > 1) {
                recentFilesMenu.remove(10);
            }
            JMenuItem recentItem = new JMenuItem(file.getPath());
            recentItem.addActionListener(new OpenAction(file));
            recentFilesMenu.add(recentItem, 0);
            recentFilesMenu.repaint();
            recentFilesMenu.revalidate();
            actualFile = file;
            updateCommandHistory("Opened configuration: "
                + file.getName());
            setTitle(RailControlGUI.NAME
                + " : [ " + actualFile.getAbsolutePath() + " ]");
        }

    }

    private class SaveAsAction extends AbstractAction {
        public SaveAsAction() {
            super("Save as...", createImageIcon(
                "icons/filesaveas.png", "Save as...", RailControlGUI.this));
        }

        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser(new File("."));
            int returnVal = fileChooser
                .showSaveDialog(RailControlGUI.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                // This is where a real application would open the file.

                String xmlConfig = RailControlGUI.this.toXML();
                FileWriter fileWriter = null;
                try {
                    fileWriter = new FileWriter(file);

                    fileWriter.write(xmlConfig);
                    updateCommandHistory("Configuration saved: "
                        + file.getName());
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
                updateCommandHistory("Save cancelled by user");
            }
        }

    }

    private class SaveAction extends AbstractAction {
        public SaveAction() {
            super("Save", createImageIcon(
                "icons/filesave.png", "Save", RailControlGUI.this));
        }

        public void actionPerformed(ActionEvent e) {

            if (actualFile != null) {

                String xmlConfig = RailControlGUI.this.toXML();
                FileWriter fileWriter = null;
                try {
                    fileWriter = new FileWriter(actualFile);

                    fileWriter.write(xmlConfig);
                    updateCommandHistory("Configuration saved: "
                        + actualFile.getName());
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
            super("Exit", createImageIcon(
                "icons/exit.png", "Exit", RailControlGUI.this));
        }

        public void actionPerformed(ActionEvent e) {
            int result = JOptionPane.showConfirmDialog(
                RailControlGUI.this, "Really exit ?", "Exit",
                JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                System.exit(0);
            }
        }

    }

    private class SwitchesAction extends AbstractAction {
        public SwitchesAction() {
            super("Switches", createImageIcon(
                "icons/switch.png", "Switches", RailControlGUI.this));
        }

        public void actionPerformed(ActionEvent e) {
            SwitchConfigurationDialog switchConfig = new SwitchConfigurationDialog(
                RailControlGUI.this, Preferences.getInstance(),
                switchNumberToSwitch, switchGroups);
            if (switchConfig.isOkPressed()) {
                SwitchControl sc = SwitchControl.getInstance();
                switchNumberToSwitch = switchConfig
                    .getSwitchNumberToSwitch();
                switchGroups = switchConfig.getSwitchGroups();
                sc.unregisterAllSwitches();
                sc.registerSwitches(switchNumberToSwitch.values());
                sc.setSessionOnSwitches(session);

                switchGroupPane.update(switchGroups);
                updateCommandHistory("Switch configuration changed");
            }
        }
    }

    private class LocomotivesAction extends AbstractAction {
        public LocomotivesAction() {
            super("Locomotives",
                createImageIcon(
                    "icons/locomotive.png", "Locomotives",
                    RailControlGUI.this));
        }

        public void actionPerformed(ActionEvent e) {
            LocomotiveConfigurationDialog locomotiveConfig = new LocomotiveConfigurationDialog(
                RailControlGUI.this, Preferences.getInstance(),
                locomotives);
            if (locomotiveConfig.isOkPressed()) {
                locomotiveControlPanel.update(locomotives);
                updateCommandHistory("Locomotive configuration changed");
            }
        }
    }

    private class PreferencesAction extends AbstractAction {
        public PreferencesAction() {
            super("Preferences", createImageIcon(
                "icons/package_settings.png", "Preferences",
                RailControlGUI.this));
        }

        public void actionPerformed(ActionEvent e) {

            PreferencesDialog p = new PreferencesDialog(
                RailControlGUI.this);
            p.editPreferences(Preferences.getInstance());
            if (p.isOkPressed()) {
                locomotiveControlPanel.update(locomotives);
                updateCommandHistory("Preferences changed");
            }
        }
    }

    private class ConnectAction extends AbstractAction {
        public ConnectAction() {
            super("Connect", createImageIcon(
                "icons/daemonconnect.png", "Connect", RailControlGUI.this));
        }

        public void actionPerformed(ActionEvent e) {
            try {
                String host = Preferences.getInstance().getStringValue(
                    "Hostname");
                int port = Preferences.getInstance().getIntValue(
                    "Portnumber");
                session = new SRCPSession(host, port, false);
                SwitchControl.getInstance().setSession(session);
                LocomotiveControl.getInstance().setSession(session);
                session.getCommandChannel().addCommandDataListener(
                    RailControlGUI.this);
                session.getInfoChannel().addInfoDataListener(
                    RailControlGUI.this);
                session.connect();
                SwitchControl.getInstance().setSessionOnSwitches(session);
                LocomotiveControl.getInstance().setSessionOnLocomotives(
                    session);

                daemonConnectItem.setEnabled(false);
                daemonDisconnectItem.setEnabled(true);
                daemonResetItem.setEnabled(true);

                connectToolBarButton.setEnabled(false);
                disconnectToolBarButton.setEnabled(true);
                updateCommandHistory("Connected to server "
                    + host + " on port " + port);
            } catch (SRCPException e1) {
                if (e1.getCause() instanceof ConnectException) {
                    ExceptionProcessor.getInstance().processException(
                        "Server not running", e1);
                }
            }
        }
    }

    private class DisconnectAction extends AbstractAction {
        public DisconnectAction() {
            super("Disconnect", createImageIcon(
                "icons/daemondisconnect.png", "Disconnect",
                RailControlGUI.this));
        }

        public void actionPerformed(ActionEvent e) {
            try {
                String host = Preferences.getInstance().getStringValue(
                    "Hostname");
                int port = Preferences.getInstance().getIntValue(
                    "Portnumber");
                session.disconnect();

                daemonConnectItem.setEnabled(true);
                daemonDisconnectItem.setEnabled(false);
                daemonResetItem.setEnabled(false);

                connectToolBarButton.setEnabled(true);
                disconnectToolBarButton.setEnabled(false);
                updateCommandHistory("Disconnected from server "
                    + host + " on port " + port);
            } catch (SRCPException e1) {
                processException(e1);
            }
        }

    }

    private class ResetAction extends AbstractAction {
        public ResetAction() {
            super("Reset", createImageIcon(
                "icons/daemonreset.png", "Reset", RailControlGUI.this));
        }

        public void actionPerformed(ActionEvent e) {

        }
    }

    private class SwitchesStraightAction extends AbstractAction {
        public SwitchesStraightAction() {
            super("", createImageIcon(
                "icons/switch.png", "Set all switches straight",
                RailControlGUI.this));
        }

        public void actionPerformed(ActionEvent e) {
            SwitchControl sc = SwitchControl.getInstance();
            try {
                for (Switch s : switchNumberToSwitch.values()) {
                    sc.setStraight(s);
                }
            } catch (SwitchException e1) {
                ExceptionProcessor.getInstance().processException(e1);
            }
        }
    }

    private class NumberEnteredAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            enteredNumberKeys.append(e.getActionCommand());
            String switchNumberAsString = enteredNumberKeys.toString();
            int switchNumber = Integer.parseInt(switchNumberAsString);
            int origNumber = switchNumber;
            if (switchNumber > 999) {
                resetSelectedSwitchDisplay();
                return;
            }
            int seg1Value = switchNumber % 10;
            seg1.setValue(seg1Value);
            seg1.repaint();
            switchNumber = switchNumber
                - seg1Value;

            int seg2Value = (switchNumber % 100) / 10;
            seg2.setValue(seg2Value);
            seg2.repaint();
            switchNumber = switchNumber
                - seg2Value * 10;

            int seg3Value = (switchNumber % 1000) / 100;
            seg3.setValue(seg3Value);
            seg3.repaint();
            switchNumber = switchNumber
                - seg3Value * 100;

            Switch searchedSwitch = null;

            searchedSwitch = switchNumberToSwitch.get(origNumber);
            if (searchedSwitch == null) {
                // resetSelectedSwitchDisplay();
                selectedSwitchDetails.removeAll();
                selectedSwitchDetails.revalidate();
                selectedSwitchDetails.repaint();
                return;
            }
            selectedSwitchDetails.removeAll();
            GridBagLayout layout = new GridBagLayout();
            GridBagConstraints gbc = new GridBagConstraints();
            selectedSwitchDetails.setLayout(layout);

            gbc.insets = new Insets(5, 5, 5, 5);

            gbc.gridx = 0;
            JLabel numberLabel = new JLabel(Integer
                .toString(searchedSwitch.getNumber()));
            numberLabel.setFont(new Font("Dialog", Font.BOLD, 16));
            layout.setConstraints(numberLabel, gbc);
            selectedSwitchDetails.add(numberLabel);

            gbc.gridx = 1;
            JLabel descLabel = new JLabel(searchedSwitch.getDesc());
            layout.setConstraints(descLabel, gbc);
            selectedSwitchDetails.add(descLabel);

            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.gridwidth = 2;
            SwitchCanvas sc = null;
            if (searchedSwitch instanceof DoubleCrossSwitch) {
                sc = new DoubleCrossSwitchCanvas(searchedSwitch);
            } else if (searchedSwitch instanceof DefaultSwitch) {
                sc = new DefaultSwitchCanvas(searchedSwitch);
            } else if (searchedSwitch instanceof ThreeWaySwitch) {
                sc = new ThreeWaySwitchCanvas(searchedSwitch);
            }

            layout.setConstraints(sc, gbc);
            selectedSwitchDetails.add(sc);
            sc.repaint();
            selectedSwitchDetails.revalidate();
            selectedSwitchDetails.repaint();
        }
    }

    private class SwitchingAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            if (enteredNumberKeys.toString().equals("")) {
                return;
            }
            String switchNumberAsString = enteredNumberKeys.toString();
            int switchNumber = Integer.parseInt(switchNumberAsString);
            Switch searchedSwitch = null;

            searchedSwitch = switchNumberToSwitch.get(switchNumber);
            if (searchedSwitch == null) {

                resetSelectedSwitchDisplay();
                return;
            }
            try {

                if (e.getActionCommand().equals("/")) {
                    handleDivide(searchedSwitch);
                } else if (e.getActionCommand().equals("*")) {
                    handleMultiply(searchedSwitch);
                } else if (e.getActionCommand().equals("-")) {
                    handleMinus(searchedSwitch);
                } else if (e.getActionCommand().equals("+")) {
                    if (!(searchedSwitch instanceof ThreeWaySwitch)) {
                        handlePlus(searchedSwitch);
                    }
                } else if (e.getActionCommand().equals("")) {
                    if (!(searchedSwitch instanceof ThreeWaySwitch)) {
                        handlePlus(searchedSwitch);
                    }
                } else if (e.getActionCommand().equals("\n")) {
                    handleEnter(searchedSwitch);
                }

                resetSelectedSwitchDisplay();
            } catch (SwitchException e1) {
                resetSelectedSwitchDisplay();
                ExceptionProcessor.getInstance().processException(e1);
            }
        }

        private void handleDivide(Switch aSwitch) throws SwitchException {
            SwitchControl.getInstance().setCurvedLeft(aSwitch);
        }

        private void handleMultiply(Switch aSwitch) throws SwitchException {
            SwitchControl.getInstance().setStraight(aSwitch);
        }

        private void handleMinus(Switch aSwitch) throws SwitchException {
            SwitchControl.getInstance().setCurvedRight(aSwitch);
        }

        private void handlePlus(Switch aSwitch) throws SwitchException {
            SwitchControl.getInstance().setCurvedLeft(aSwitch);
        }

        private void handleEnter(Switch aSwitch) throws SwitchException {
            SwitchControl.getInstance().setStraight(aSwitch);
        }
    }

    private class SwitchGroupChangeAction extends AbstractAction {

        private int selectedSwitchGroup;

        public SwitchGroupChangeAction(int selectedSwitchGroup) {
            this.selectedSwitchGroup = selectedSwitchGroup;
        }

        public void actionPerformed(ActionEvent e) {
            if (selectedSwitchGroup < switchGroups.size()) {
                switchGroupPane.setSelectedIndex(selectedSwitchGroup);
            }
        }

    }
}
