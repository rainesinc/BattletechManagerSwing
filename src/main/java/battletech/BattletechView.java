/*
 * BattletechView.java
 */
package battletech;

import java.awt.Component;
import java.awt.Point;

import com.rainesinc.NetbeansResourceMapReader;
import org.jdesktop.application.Action;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * The application's main frame.
 */
public class BattletechView extends FrameView {

    public BattletechView(SingleFrameApplication app) throws IOException {
        super(app);

        initComponents();

        NetbeansResourceMapReader resourceMap =
                new NetbeansResourceMapReader("BattletechView.properties");


        // status bar initialization - message timeout, idle icon and busy animation, etc

        // ResourceMap resourceMap = getResourceMap();


        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {

            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String) (evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer) (evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });
        init();
    }

    private void init() {
        this.jTabbedPane1.addChangeListener(new TabbedPaneListener());
    }

    private class TabbedPaneListener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
           JTabbedPane pane = (JTabbedPane)e.getSource();
            // Get current tab
            int sel = pane.getSelectedIndex();
            String title = pane.getTitleAt(sel);
            if(title != null && "Ammo Usage".equalsIgnoreCase(title)){
                calculateAmmoUsage();
            }
        }
    }

    public List<MechPanel> getMechPanels() {
        List panels = new ArrayList();
        List frames = getMechInternalFrames();
        for (Iterator i = frames.iterator(); i.hasNext();) {
            MechInternalFrame frame = (MechInternalFrame) i.next();
            panels.add(frame.getMechPanel());
        }
        return panels;
    }

    private List<MechInternalFrame> getMechInternalFrames() {
        List frames = new ArrayList();
        Component[] components = jDesktopPane1.getComponents();
        // the jDesktopPane seems to store the components
        // as a stack, first in, last out, so let's iterate through
        // the components[] in reverse order to create our list
        // of frames to return.
        int len = components.length;
        for (int x = len - 1; x > -1; x--) {
            // find instances of MechInternalFrame class in component list
            String componentName = components[x].getClass().getSimpleName();
            if (componentName != null && "MechInternalFrame".equalsIgnoreCase(componentName)) {
                MechInternalFrame frame = (MechInternalFrame) components[x];
                frames.add(frame);
            }
        }
        return frames;
    }

    private void calculateAmmoUsage() {
        ammoUsageTextArea.setText("");
        List panels = getMechPanels();
        for (Iterator i = panels.iterator(); i.hasNext();) {
            MechPanel panel = (MechPanel) i.next();
            String id = panel.getLance() + ":" + panel.getMech();
            Map ammo = new HashMap();
            List weaponsFired = panel.getWeaponsFired();
            if (weaponsFired.size() > 0) {
                for (Iterator j = weaponsFired.iterator(); j.hasNext();) {
                    String weapon = (String) j.next();
                    if (BattletechUtil.isBallistic(weapon)) {
                        // debug
                        // System.out.println(id + " " + weapon);
                        if (ammo.containsKey(weapon)) {
                            int num = ((Integer) ammo.get(weapon)).intValue();
                            num++;
                            ammo.remove(weapon);
                            ammo.put(weapon, new Integer(num));
                        } else {
                            ammo.put(weapon, new Integer(1));
                        }
                    }
                }
                Set keys = ammo.keySet();
                if (keys.size() > 0) {
                    ammoUsageTextArea.append("\n" + id);
                }
                for (Iterator k = keys.iterator(); k.hasNext();) {
                    String key = (String) k.next();
                    int numUsed = ((Integer) ammo.get(key)).intValue();
                    ammoUsageTextArea.append("\n\t" + key + "\t" + numUsed);
                }
            }
        }
    }

    @Action
    public void reset() throws IOException {
        List panels = getMechPanels();
        for (Iterator i = panels.iterator(); i.hasNext();) {
            MechPanel panel = (MechPanel) i.next();
            // clear shots
            panel.clearShots();
            // clear phys attacks
            panel.clearPhysAttacks();
            // reset basics
            // panel.setTerrain("Open Ground");
            panel.setMoveMode("Stationary");
            panel.setMoveDistance(0);
            panel.setStoodUp(false);
            panel.setHeadMod(0);
            // panel.setDamageMod(0);
            panel.setMovedCheckBox(false);
            panel.setFiredCheckBox(false);
            panel.setPhysCheckBox(false);
            // disable weapon attacks
            panel.disableWeaponAttacks();
            // disable physical attacks
            panel.disablePhysicalAttacks();
            // clear ammo usage
            ammoUsageTextArea.setText("");
            // select weaponPanels
            panel.selectWeaponAttacksTab();
        }
        // Alert the user to set heat modifiers
        showAlertDlg("Alert! check: HeatMod, SensorHits, EngineHits");
    }

    @Action
    public void selectPhysicalPanels() {
        List panels = getMechPanels();
        for (Iterator i = panels.iterator(); i.hasNext();) {
            MechPanel panel = (MechPanel) i.next();
            panel.selectPhysAttacksTab();
        }
    }

    @Action
    public void selectWeaponPanels() {
        List panels = getMechPanels();
        for (Iterator i = panels.iterator(); i.hasNext();) {
            MechPanel panel = (MechPanel) i.next();
            panel.selectWeaponAttacksTab();
        }
    }

    @Action
    public void newLance() throws IOException {
        // Get new lance name with dialog
        JFrame mainFrame = BattletechApp.getApplication().getMainFrame();
        lanceDlg = new LanceDlg(mainFrame, true);
        lanceDlg.setLocationRelativeTo(mainFrame);
        BattletechApp.getApplication().show(lanceDlg);
        if (lanceDlg.getReturnStatus() == LanceDlg.RET_OK) {
            // Create four new mech panels
            for (int x = 0; x < 4; x++) {
                newMechPanel(lanceDlg.getLanceName(), lanceDlg.getMech(x + 1), lanceDlg.getTons(x + 1));
            }
        }
        lanceDlg = null;
    }

    private void newMechPanel(String lance, String mech, String tons) {
        MechInternalFrame mif = new MechInternalFrame(lance, mech, tons);
        jDesktopPane1.add(mif);
        // Decide on location to place the new panel based on the last one
        if (lastNewMechPanelLocation == null) {
            lastNewMechPanelLocation = new Point(0, 0);
            mif.setLocation(lastNewMechPanelLocation.getLocation());
        } else {
            lastNewMechPanelLocation.translate(20, 20);
            mif.setLocation(lastNewMechPanelLocation.getLocation());
        }
        mif.setVisible(true);
    }

    @Action
    public void newMechPanel() {
        MechInternalFrame mif = new MechInternalFrame();
        jDesktopPane1.add(mif);
        // Decide on location to place the new panel based on the last one
        if (lastNewMechPanelLocation == null) {
            lastNewMechPanelLocation = new Point(0, 0);
            mif.setLocation(lastNewMechPanelLocation.getLocation());
        } else {
            lastNewMechPanelLocation.translate(20, 20);
            mif.setLocation(lastNewMechPanelLocation.getLocation());
        }
        mif.setVisible(true);
    }

    @Action
    public void tileMechInternalFrames() {
        // Get the list of MechInternalFrames
        List frames = getMechInternalFrames();
        // Arrage the frames in an appropriate grid
        int x = 0;
        int y = 0;
        int colCount = 0;
        for (Iterator i = frames.iterator(); i.hasNext();) {
            MechInternalFrame frame = (MechInternalFrame) i.next();
            frame.setLocation(x, y);
            x = x + frame.getWidth();
            colCount++;
            if (colCount > 3) {
                colCount = 0;
                x = 0;
                y = y + frame.getHeight();
            }
        }
    }

    @Action
    public void cascadeMechInternalFrames() {
        // Get the list of Frames
        List frames = getMechInternalFrames();
        // Cascade the frames
        int x = (frames.size() - 1) * 70;
        int y = (frames.size() - 1) * 45;
        for (Iterator i = frames.iterator(); i.hasNext();) {
            MechInternalFrame frame = (MechInternalFrame) i.next();
            frame.setLocation(x, y);
            x = x - 70;
            y = y - 45;
        }
    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = BattletechApp.getApplication().getMainFrame();
            aboutBox = new BattletechAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        BattletechApp.getApplication().show(aboutBox);
    }

    @Action
    public void showAlertDlg(String message) throws IOException {
        JFrame mainFrame = BattletechApp.getApplication().getMainFrame();
        alertDlg = new AlertDlg(mainFrame, true, message);
        alertDlg.setLocationRelativeTo(mainFrame);
        BattletechApp.getApplication().show(alertDlg);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() throws IOException {

        mainPanel = new javax.swing.JPanel();
        jTabbedPane1 = new JTabbedPane();
        jDesktopPane1 = new javax.swing.JDesktopPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        ammoUsageTextArea = new javax.swing.JTextArea();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        resetMenuItem = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();
        newMenuItem = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();

        mainPanel.setName("mainPanel"); // NOI18N

        jTabbedPane1.setName("jTabbedPane1"); // NOI18N

        jDesktopPane1.setName("jDesktopPane1"); // NOI18N

        NetbeansResourceMapReader resourceMap =
                new NetbeansResourceMapReader("BattletechView.properties");

        jTabbedPane1.addTab(resourceMap.getString("jDesktopPane1.TabConstraints.tabTitle"), jDesktopPane1); // NOI18N

        jPanel1.setName("jPanel1"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        ammoUsageTextArea.setColumns(20);
        ammoUsageTextArea.setEditable(false);
        ammoUsageTextArea.setRows(5);
        ammoUsageTextArea.setName("ammoUsageTextArea"); // NOI18N
        jScrollPane1.setViewportView(ammoUsageTextArea);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 365, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(276, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 326, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 656, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 373, Short.MAX_VALUE)
        );

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(BattletechApp.class).getContext().getActionMap(BattletechView.class, this);
        resetMenuItem.setAction(actionMap.get("reset")); // NOI18N
        resetMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
        resetMenuItem.setText(resourceMap.getString("resetMenuItem.text")); // NOI18N
        resetMenuItem.setName("resetMenuItem"); // NOI18N
        fileMenu.add(resetMenuItem);

        jMenuItem4.setAction(actionMap.get("selectPhysicalPanels")); // NOI18N
        jMenuItem4.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem4.setText(resourceMap.getString("jMenuItem4.text")); // NOI18N
        jMenuItem4.setName("jMenuItem4"); // NOI18N
        fileMenu.add(jMenuItem4);

        jMenuItem5.setAction(actionMap.get("selectWeaponPanels")); // NOI18N
        jMenuItem5.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_W, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem5.setText(resourceMap.getString("jMenuItem5.text")); // NOI18N
        jMenuItem5.setName("jMenuItem5"); // NOI18N
        fileMenu.add(jMenuItem5);

        newMenuItem.setAction(actionMap.get("newMechPanel")); // NOI18N
        newMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        newMenuItem.setText(resourceMap.getString("newMenuItem.text")); // NOI18N
        newMenuItem.setName("newMenuItem"); // NOI18N
        fileMenu.add(newMenuItem);

        jMenuItem1.setAction(actionMap.get("newLance")); // NOI18N
        jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem1.setText(resourceMap.getString("jMenuItem1.text")); // NOI18N
        jMenuItem1.setName("jMenuItem1"); // NOI18N
        fileMenu.add(jMenuItem1);

        jMenuItem2.setAction(actionMap.get("tileMechInternalFrames")); // NOI18N
        jMenuItem2.setText(resourceMap.getString("jMenuItem2.text")); // NOI18N
        jMenuItem2.setName("jMenuItem2"); // NOI18N
        fileMenu.add(jMenuItem2);

        jMenuItem3.setAction(actionMap.get("cascadeMechInternalFrames")); // NOI18N
        jMenuItem3.setText(resourceMap.getString("jMenuItem3.text")); // NOI18N
        jMenuItem3.setName("jMenuItem3"); // NOI18N
        fileMenu.add(jMenuItem3);

        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 482, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 656, Short.MAX_VALUE)
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 10, Short.MAX_VALUE)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusMessageLabel)
                    .addComponent(statusAnimationLabel)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3))
        );

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea ammoUsageTextArea;
    private javax.swing.JDesktopPane jDesktopPane1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private JTabbedPane jTabbedPane1;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem newMenuItem;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JMenuItem resetMenuItem;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    // End of variables declaration//GEN-END:variables
    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;
    private JDialog aboutBox;
    private JDialog alertDlg;
    private Point lastNewMechPanelLocation;
    private LanceDlg lanceDlg;
}
