/*
 * MechPanel.java
 *
 * Created on February 10, 2008, 11:24 AM
 */
package battletech;

import com.rainesinc.NetbeansResourceMapReader;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.ColorModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JFrame;

import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author  ryan
 */
public class MechPanel extends javax.swing.JPanel {

    /** Creates new form MechPanel */
    public MechPanel() {
        initComponents();
        init();
    }

    private void init() {
        // Add a shotsTable listener to affect weaponsHeat and ToHit on changes
        shotsTable.getModel().addTableModelListener(new ShotsTableModelListener());
        shotsTable.getColumnModel().getColumn(6).setCellRenderer(new RedTableCellRenderer());
        physTable.getModel().addTableModelListener(new PhysTableModelListener());
        moveComboBox.addItemListener(new MoveComboBoxListener());
        distanceSpinner.addChangeListener(new DistanceSpinnerListener());
        standUpCheckBox.addChangeListener(new StandUpCheckBoxListener());
        heatModSpinner.addChangeListener(new HeatModSpinnerListener());
        movedCheckBox.addChangeListener(new MovedCheckBoxListener());
        firedCheckBox.addChangeListener(new FiredCheckBoxListener());
        physCheckBox.addChangeListener(new PhysCheckBoxListener());
        engineHitCheckBox1.addChangeListener(new EngineHitListener());
        engineHitCheckBox2.addChangeListener(new EngineHitListener());
        sensorHitCheckBox1.addChangeListener(new SensorHitListener());
        sensorHitCheckBox2.addChangeListener(new SensorHitListener());
    }

    private class RedTableCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            int val = ((Integer) value).intValue();
            if (val > 50) {
                if (isSelected) {
                    this.setBackground(Color.RED);
                    this.setForeground(new Color(0, 0, 0));
                } else {
                    this.setBackground(Color.PINK);
                    this.setForeground(new Color(0, 0, 0));
                }
            } else {
                if (isSelected) {
                    this.setBackground(new Color(10, 36, 106));
                    this.setForeground(new Color(255, 255, 255));
                } else {
                    this.setBackground(new Color(255, 255, 255));
                    this.setForeground(new Color(0, 0, 0));
                }
            }
            setText(String.valueOf(value));
            this.setFont(Font.getFont("shotsTable.font"));
            return this;
        }
    }

    private class EngineHitListener implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            affectEngineHitHeat();
        }
    }

    private class SensorHitListener implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            affectShots();
        }
    }

    private class MovedCheckBoxListener implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            if (movedCheckBox.isSelected()) {
                movedCheckBox.setForeground(Color.BLACK);
            } else {
                movedCheckBox.setForeground(Color.BLUE);
            }
            // check to see if everyone has moved...
            // if so enable weapons firing
            // get handle on panels
            BattletechView battletechView = (BattletechView) BattletechApp.getApplication().getMainView();
            List panels = battletechView.getMechPanels();
            boolean allMechsMoved = true;
            for (Iterator i = panels.iterator(); i.hasNext();) {
                MechPanel panel = (MechPanel) i.next();
                if (!panel.isMoved()) {
                    allMechsMoved = false;
                    break;
                }
            }
            if (allMechsMoved) {
                enableWeaponAttacksGlobal();
            }
        }
    }

    private class FiredCheckBoxListener implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            if (firedCheckBox.isSelected()) {
                firedCheckBox.setForeground(Color.BLACK);
            } else {
                firedCheckBox.setForeground(Color.RED);
            }
            // check to see if everyone has fired...
            // if so enable phys attacks
            // get handle on panels
            BattletechView battletechView = (BattletechView) BattletechApp.getApplication().getMainView();
            List panels = battletechView.getMechPanels();
            boolean allMechsFired = true;
            for (Iterator i = panels.iterator(); i.hasNext();) {
                MechPanel panel = (MechPanel) i.next();
                if (!panel.isFired()) {
                    allMechsFired = false;
                    break;
                }
            }
            if (allMechsFired) {
                enablePhysicalAttacksGlobal();
            }
        }
    }

    private class PhysCheckBoxListener implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            if (physCheckBox.isSelected()) {
                physCheckBox.setForeground(Color.BLACK);
            } else {
                physCheckBox.setForeground(Color.RED);
            }
        }
    }

    private class ShotsTableModelListener implements TableModelListener {

        public void tableChanged(TableModelEvent e) {
            // Affect weapon heat.
            affectWeaponHeat();
            // If the change is not the shotToHit itself, then Affect shots
            int type = e.getType();
            if (type == TableModelEvent.UPDATE || type == TableModelEvent.INSERT) {
                int column = e.getColumn();
                if (column != 6) {
                    affectShots();
                }
            }
            flagFired();
        }
    }

    private class PhysTableModelListener implements TableModelListener {

        public void tableChanged(TableModelEvent e) {
            // If the change is not the physAttackToHit itself, then Affect attacks
            int type = e.getType();
            if (type == TableModelEvent.UPDATE || type == TableModelEvent.INSERT) {
                int column = e.getColumn();
                if (column != 3) {
                    affectPhysAttacks();
                }
            }
            flagPhys();
        }
    }

    private class MoveComboBoxListener implements ItemListener {

        public void itemStateChanged(ItemEvent e) {
            // Movement mode changed, affect move heat
            affectMoveHeat();
            // If changed to stationary, make distance zero, disable distance spinner
            String moveMode = (String) moveComboBox.getSelectedItem();
            if ("stationary".equalsIgnoreCase(moveMode)) {
                // zero out distance spinner and disable it
                distanceSpinner.setValue(new Integer(0));
                distanceSpinner.setEnabled(false);
            }
            // If changed FROM stationary, make distance spiller enabled
            // and set it's text color to red
            if (!"stationary".equalsIgnoreCase(moveMode)) {
                distanceSpinner.setEnabled(true);
                JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) distanceSpinner.getEditor();
                editor.getTextField().setForeground(Color.RED);
            }
            affectShotsGlobal();
            affectPhysAttacksGlobal();
            flagMovement();
        }
    }

    private class DistanceSpinnerListener implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            // Distance status changed,affect move heat
            affectMoveHeat();
            affectShotsGlobal();
            affectPhysAttacksGlobal();
            flagMovement();
            // change text color to black
            JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) distanceSpinner.getEditor();
            editor.getTextField().setForeground(Color.BLACK);
        }
    }

    private class StandUpCheckBoxListener implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            // Stand Up status changed, 
            // change position to upright if standup
            if (standUpCheckBox.isSelected()) {
                positionComboBox.setSelectedItem("Upright");
                standUpSpinner.setEnabled(true);
                standUpSpinner.getModel().setValue(new Integer(1));
            } else {
                standUpSpinner.setEnabled(false);
                standUpSpinner.setValue(new Integer(1));
            }
            affectMoveHeat();
            flagMovement();
        }
    }

    private class HeatModSpinnerListener implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            affectShots();
        }
    }

    public void selectWeaponAttacksTab() {
        jTabbedPane1.setSelectedIndex(0);
    }

    public void selectPhysAttacksTab() {
        jTabbedPane1.setSelectedIndex(1);
    }

    public boolean isMoved() {
        return this.movedCheckBox.isSelected();
    }
    
    public boolean isFired() {
        return this.firedCheckBox.isSelected();
    }

    public void enableWeaponAttacks() {
        addShotButton.setEnabled(true);
        deleteShotButton.setEnabled(true);
        firedCheckBox.setEnabled(true);
    }

    public void disableWeaponAttacks() {
        addShotButton.setEnabled(false);
        deleteShotButton.setEnabled(false);
        firedCheckBox.setEnabled(false);
    }

    private void enableWeaponAttacksGlobal() {
        // get handle on panels
        BattletechView battletechView = (BattletechView) BattletechApp.getApplication().getMainView();
        List panels = battletechView.getMechPanels();
        for (Iterator i = panels.iterator(); i.hasNext();) {
            MechPanel panel = (MechPanel) i.next();
            panel.enableWeaponAttacks();
        }
    }

    public void enablePhysicalAttacks() {
        addPhysButton.setEnabled(true);
        deletePhysButton.setEnabled(true);
        physCheckBox.setEnabled(true);
    }

    public void disablePhysicalAttacks() {
        addPhysButton.setEnabled(false);
        deletePhysButton.setEnabled(false);
        physCheckBox.setEnabled(false);
    }

    private void enablePhysicalAttacksGlobal() {
        // get handle on panels
        BattletechView battletechView = (BattletechView) BattletechApp.getApplication().getMainView();
        List panels = battletechView.getMechPanels();
        for (Iterator i = panels.iterator(); i.hasNext();) {
            MechPanel panel = (MechPanel) i.next();
            panel.enablePhysicalAttacks();
        }
    }

    private void flagMovement() {
        this.movedCheckBox.setSelected(true);
    }

    private void flagFired() {
        this.firedCheckBox.setSelected(true);
    }
    
    private void flagPhys() {
        this.physCheckBox.setSelected(true);
    }

    public String getLance() {
        return lanceText.getText();
    }

    public void setLance(String lance) {
        lanceText.setText(lance);
    }

    public String getMech() {
        return mechText.getText();
    }

    public void setMech(String mechName) {
        mechText.setText(mechName);
    }

    public void setTons(String tons) {
        tonnageText.setText(tons);
    }

    public int getGunskill() {
        Integer gsk = (Integer) gunskillSpinner.getValue();
        return gsk.intValue();
    }

    public String getPosition() {
        return (String) positionComboBox.getSelectedItem();
    }

    public int getHeatMod() {
        Integer mod = (Integer) heatModSpinner.getValue();
        return mod.intValue();
    }

    public void setHeadMod(int mod) {
        heatModSpinner.setValue(new Integer(mod));
    }

    public String getMoveMode() {
        return (String) moveComboBox.getSelectedItem();
    }

    public void setMoveMode(String mode) {
        moveComboBox.setSelectedItem(mode);
    }

    public int getMoveDistance() {
        Integer dist = (Integer) distanceSpinner.getValue();
        return dist.intValue();
    }

    public void setMoveDistance(int distance) {
        distanceSpinner.setValue(new Integer(distance));
    }

    public boolean getStoodUp() {
        return this.standUpCheckBox.isSelected();
    }

    public void setStoodUp(boolean stoodUp) {
        if (stoodUp) {
            standUpCheckBox.setSelected(true);
        } else {
            standUpCheckBox.setSelected(false);
        }

    }

    public int getStandAttempts() {
        Integer attempts = (Integer) standUpSpinner.getValue();
        return attempts.intValue();
    }

    public void setFiredCheckBox(boolean checked) {
        if (checked) {
            firedCheckBox.setSelected(true);
        } else {
            firedCheckBox.setSelected(false);
        }

    }

    public void setPhysCheckBox(boolean checked) {
        if (checked) {
            physCheckBox.setEnabled(true);
        } else {
            physCheckBox.setSelected(false);
        }
    }

    public void setMovedCheckBox(boolean checked) {
        if (checked) {
            movedCheckBox.setSelected(true);
        } else {
            movedCheckBox.setSelected(false);
        }

    }

    private void affectEngineHitHeat() {
        int hits = 0;
        if (engineHitCheckBox1.isSelected()) {
            hits++;
        }

        if (engineHitCheckBox2.isSelected()) {
            hits++;
        }

        engineHitHeat = hits * 5;
        int totalHeat = movementHeat + weaponHeat + engineHitHeat;
        heatText.setText(String.valueOf(totalHeat));
    }

    private void affectMoveHeat() {
        movementHeat = BattletechUtil.movementHeat(getMoveMode(), getMoveDistance(), getStoodUp(), getStandAttempts());
        int totalHeat = movementHeat + weaponHeat + engineHitHeat;
        heatText.setText(String.valueOf(totalHeat));
    }

    private void affectWeaponHeat() {
        // parse the shotsTable and add up heat for all weapons fired
        int temp = 0;
        DefaultTableModel model = (DefaultTableModel) shotsTable.getModel();
        for (int x = 0; x <
                model.getRowCount(); x++) {
            String weapon = (String) model.getValueAt(x, 1);
            temp =
                    temp + BattletechUtil.weaponHeat(weapon);
        }

        weaponHeat = temp;
        int totalHeat = movementHeat + weaponHeat + engineHitHeat;
        heatText.setText(String.valueOf(totalHeat));
    }

    private int getSensorHits() {
        int hits = 0;
        if (sensorHitCheckBox1.isSelected()) {
            hits++;
        }

        if (sensorHitCheckBox2.isSelected()) {
            hits++;
        }

        return hits;
    }

    public List<String> getWeaponsFired() {
        List weapons = new ArrayList();
        DefaultTableModel model = (DefaultTableModel) shotsTable.getModel();
        for (int x = 0; x <
                model.getRowCount(); x++) {
            String weapon = (String) model.getValueAt(x, 1);
            weapons.add(weapon);
        }

        return weapons;
    }

    public void affectShots() {
        DefaultTableModel model = (DefaultTableModel) shotsTable.getModel();
        for (int x = 0; x < model.getRowCount(); x++) {
            // gather shot info
            String target = (String) model.getValueAt(x, 0);
            String weapon = (String) model.getValueAt(x, 1);
            int range = ((Integer) model.getValueAt(x, 2)).intValue();
            int losTrnMod = ((Integer) model.getValueAt(x, 3)).intValue();
            boolean secondary = ((Boolean) model.getValueAt(x, 4)).booleanValue();
            boolean partialCvr = ((Boolean) model.getValueAt(x, 5)).booleanValue();
            // gather target info
            MechPanel tgtPanel = getPanelByTarget(target);
            if (tgtPanel != null) {
                String tgtPosition = tgtPanel.getPosition();
                String tgtMoveMode = tgtPanel.getMoveMode();
                int tgtMoveDist = tgtPanel.getMoveDistance();
                // find mods
                int attackerMod = BattletechUtil.attackerMod(this.getMoveMode(),
                        this.getPosition(), this.getHeatMod(), this.getSensorHits());
                int weaponMod = BattletechUtil.weaponMod(weapon, range);
                int targetMod = BattletechUtil.targetMod(secondary,
                        partialCvr, tgtPosition, tgtMoveMode, tgtMoveDist, range);
                // calculate shot mod
                int shotToHit = this.getGunskill() + attackerMod + weaponMod + targetMod + losTrnMod;
                // update shot table
                model.setValueAt(new Integer(shotToHit), x, 6);
            }

        }
    }

    /**
     * Affect the shots tables of ALL
     * existing MechPanel's
     */
    private void affectShotsGlobal() {
        // get handle on panels
        BattletechView battletechView = (BattletechView) BattletechApp.getApplication().getMainView();
        List panels = battletechView.getMechPanels();
        for (Iterator i = panels.iterator(); i.hasNext();) {
            MechPanel panel = (MechPanel) i.next();
            panel.affectShots();
        }

    }

    /**
     * Clear all shots from shots table
     */
    public void clearShots() {
        DefaultTableModel model = (DefaultTableModel) shotsTable.getModel();
        int rowCount = model.getRowCount();
        for (int x = 0; x <
                rowCount; x++) {
            model.removeRow(0);
        }
    }

    public void affectPhysAttacks() {
        DefaultTableModel model = (DefaultTableModel) physTable.getModel();
        for (int x = 0; x < model.getRowCount(); x++) {
            // gather attack info
            String target = (String) model.getValueAt(x, 0);
            String attack = (String) model.getValueAt(x, 1);
            int mod = ((Integer) model.getValueAt(x, 2)).intValue();
            // gather target info
            MechPanel tgtPanel = getPanelByTarget(target);
            if (tgtPanel != null) {
                String tgtPosition = tgtPanel.getPosition();
                String tgtMoveMode = tgtPanel.getMoveMode();
                int tgtMoveDist = tgtPanel.getMoveDistance();
                // find mods
                int attackerMod = BattletechUtil.physicalAttackerMod(this.getMoveMode(), this.getPosition());
                int physAttackMod = BattletechUtil.physicalAttackMod(attack);
                int targetMod = BattletechUtil.physicalTargetMod(tgtMoveMode, tgtMoveDist, tgtPosition);
                // calculate toHit
                int attackToHit = attackerMod + physAttackMod + targetMod + mod;
                // update phys table
                model.setValueAt(new Integer(attackToHit), x, 3);
            }

        }
    }

    /**
     * Affect the phys tables of ALL
     * existing MechPanel's
     */
    private void affectPhysAttacksGlobal() {
        // get handle on panels
        BattletechView battletechView = (BattletechView) BattletechApp.getApplication().getMainView();
        List panels = battletechView.getMechPanels();
        for (Iterator i = panels.iterator(); i.hasNext();) {
            MechPanel panel = (MechPanel) i.next();
            panel.affectPhysAttacks();
        }

    }

    /**
     * Clear all attacks from physical attacks table
     */
    public void clearPhysAttacks() {
        DefaultTableModel model = (DefaultTableModel) physTable.getModel();
        int rowCount = model.getRowCount();
        for (int x = 0; x <
                rowCount; x++) {
            model.removeRow(0);
        }
    }

    /**
     * target is of format "lance:mech"
     * @param target
     */
    private MechPanel getPanelByTarget(String target) {
        MechPanel panel = null;
        String lance = "";
        String mech = "";
        // find lance and mech from target
        if (target != null && !"".equalsIgnoreCase(target)) {
            target = target.trim();
            String[] line = target.split(":");
            if (line.length > 1) {
                lance = line[0];
                mech =
                        line[1];
            }
// get handle on panels
            BattletechView battletechView = (BattletechView) BattletechApp.getApplication().getMainView();
            List panels = battletechView.getMechPanels();
            // find the corresponding panel
            for (Iterator i = panels.iterator(); i.hasNext();) {
                MechPanel tempPanel = (MechPanel) i.next();
                if (lance.equalsIgnoreCase(tempPanel.getLance()) && mech.equalsIgnoreCase(tempPanel.getMech())) {
                    panel = tempPanel;
                    break;
                }

            }
        }
        return panel;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        statusGroup = new javax.swing.ButtonGroup();
        movementGroup = new javax.swing.ButtonGroup();
        jPanel6 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        lanceText = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        mechText = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        tonnageText = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        gunskillSpinner = new JSpinner();
        pilotskillSpinner = new JSpinner();
        jPanel11 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        moveComboBox = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        standUpCheckBox = new javax.swing.JCheckBox();
        positionComboBox = new javax.swing.JComboBox();
        standUpSpinner = new JSpinner();
        distanceSpinner = new JSpinner();
        sensorHitCheckBox2 = new javax.swing.JCheckBox();
        sensorHitCheckBox1 = new javax.swing.JCheckBox();
        jLabel10 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        engineHitCheckBox1 = new javax.swing.JCheckBox();
        engineHitCheckBox2 = new javax.swing.JCheckBox();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        weaponPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        shotsTable = new JTable();
        deleteShotButton = new javax.swing.JButton();
        addShotButton = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        heatModSpinner = new JSpinner();
        physicalPanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        physTable = new JTable();
        addPhysButton = new javax.swing.JButton();
        deletePhysButton = new javax.swing.JButton();
        movedCheckBox = new javax.swing.JCheckBox();
        firedCheckBox = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        heatText = new javax.swing.JLabel();
        physCheckBox = new javax.swing.JCheckBox();

        setMinimumSize(new java.awt.Dimension(310, 310));
        setName("Form"); // NOI18N

        jPanel6.setName("jPanel6"); // NOI18N

        NetbeansResourceMapReader resourceMap = null;
        try {
            resourceMap = new NetbeansResourceMapReader("MechPanel.properties");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        lanceText.setFont(resourceMap.getFont("lanceText.font")); // NOI18N
        lanceText.setName("lanceText"); // NOI18N

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        mechText.setFont(resourceMap.getFont("mechText.font")); // NOI18N
        mechText.setName("mechText"); // NOI18N

        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        tonnageText.setText(resourceMap.getString("tonnageText.text")); // NOI18N
        tonnageText.setName("tonnageText"); // NOI18N

        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        gunskillSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(4), null, null, Integer.valueOf(1)));
        gunskillSpinner.setName("gunskillSpinner"); // NOI18N
        gunskillSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
                gunskillSpinnerStateChanged(evt);
            }
        });

        pilotskillSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(5), null, null, Integer.valueOf(1)));
        pilotskillSpinner.setName("pilotskillSpinner"); // NOI18N

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lanceText, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(mechText, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(gunskillSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pilotskillSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tonnageText, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(32, Short.MAX_VALUE))
        );

        jPanel6Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new Component[] {gunskillSpinner, pilotskillSpinner});

        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(mechText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(tonnageText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lanceText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(gunskillSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(pilotskillSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jPanel11.setName("jPanel11"); // NOI18N

        jLabel13.setText(resourceMap.getString("jLabel13.text")); // NOI18N
        jLabel13.setName("jLabel13"); // NOI18N

        moveComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Stationary", "Walk", "Run", "Jump", "Pounce", "Charge" }));
        moveComboBox.setName("moveComboBox"); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        standUpCheckBox.setText(resourceMap.getString("standUpCheckBox.text")); // NOI18N
        standUpCheckBox.setName("standUpCheckBox"); // NOI18N

        positionComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Upright", "Prone" }));
        positionComboBox.setName("positionComboBox"); // NOI18N
        positionComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                positionComboBoxActionPerformed(evt);
            }
        });

        standUpSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), null, Integer.valueOf(1)));
        standUpSpinner.setEnabled(false);
        standUpSpinner.setName("standUpSpinner"); // NOI18N
        standUpSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
                standUpSpinnerStateChanged(evt);
            }
        });

        distanceSpinner.setBackground(resourceMap.getColor("distanceSpinner.background")); // NOI18N
        distanceSpinner.setFont(resourceMap.getFont("distanceSpinner.font")); // NOI18N
        distanceSpinner.setForeground(resourceMap.getColor("distanceSpinner.foreground")); // NOI18N
        distanceSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(1)));
        distanceSpinner.setEnabled(false);
        distanceSpinner.setName("distanceSpinner"); // NOI18N

        sensorHitCheckBox2.setText(resourceMap.getString("sensorHitCheckBox2.text")); // NOI18N
        sensorHitCheckBox2.setName("sensorHitCheckBox2"); // NOI18N

        sensorHitCheckBox1.setText(resourceMap.getString("sensorHitCheckBox1.text")); // NOI18N
        sensorHitCheckBox1.setName("sensorHitCheckBox1"); // NOI18N

        jLabel10.setText(resourceMap.getString("jLabel10.text")); // NOI18N
        jLabel10.setName("jLabel10"); // NOI18N

        jLabel9.setText(resourceMap.getString("jLabel9.text")); // NOI18N
        jLabel9.setName("jLabel9"); // NOI18N

        engineHitCheckBox1.setText(resourceMap.getString("engineHitCheckBox1.text")); // NOI18N
        engineHitCheckBox1.setName("engineHitCheckBox1"); // NOI18N

        engineHitCheckBox2.setText(resourceMap.getString("engineHitCheckBox2.text")); // NOI18N
        engineHitCheckBox2.setName("engineHitCheckBox2"); // NOI18N

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addComponent(jLabel13)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(moveComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(distanceSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sensorHitCheckBox1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sensorHitCheckBox2))
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addComponent(standUpCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(standUpSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(positionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(engineHitCheckBox1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(engineHitCheckBox2)))
                .addGap(24, 24, 24))
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER, false)
                    .addComponent(jLabel13)
                    .addComponent(jLabel1)
                    .addComponent(distanceSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10)
                    .addComponent(sensorHitCheckBox1)
                    .addComponent(sensorHitCheckBox2)
                    .addComponent(moveComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8, 8, 8)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(standUpCheckBox)
                        .addComponent(standUpSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(positionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                        .addComponent(engineHitCheckBox1)
                        .addComponent(engineHitCheckBox2)
                        .addComponent(jLabel9))))
        );

        jTabbedPane1.setName("jTabbedPane1"); // NOI18N

        weaponPanel.setName("weaponPanel"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        shotsTable.setBackground(resourceMap.getColor("shotsTable.background")); // NOI18N
        shotsTable.setFont(resourceMap.getFont("shotsTable.font")); // NOI18N
        shotsTable.setForeground(resourceMap.getColor("shotsTable.foreground")); // NOI18N
        shotsTable.setModel(new DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Tgt", "Weapon", "Rng", "MOD", "Scndry", "Prtl Cvr", "To Hit"
            }
        ) {
            Class[] types = new Class [] {
                String.class, String.class, Integer.class, Integer.class, Boolean.class, Boolean.class, Integer.class
            };
            boolean[] canEdit = new boolean [] {
                true, true, true, true, true, true, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        shotsTable.setName("shotsTable"); // NOI18N
        shotsTable.setSelectionBackground(resourceMap.getColor("shotsTable.selectionBackground")); // NOI18N
        shotsTable.setSelectionForeground(resourceMap.getColor("shotsTable.selectionForeground")); // NOI18N
        jScrollPane1.setViewportView(shotsTable);

        deleteShotButton.setText(resourceMap.getString("deleteShotButton.text")); // NOI18N
        deleteShotButton.setEnabled(false);
        deleteShotButton.setName("deleteShotButton"); // NOI18N
        deleteShotButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteShotButtonActionPerformed(evt);
            }
        });

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(BattletechApp.class).getContext().getActionMap(MechPanel.class, this);
        addShotButton.setAction(actionMap.get("showShotDlg")); // NOI18N
        addShotButton.setText(resourceMap.getString("addShotButton.text")); // NOI18N
        addShotButton.setEnabled(false);
        addShotButton.setName("addShotButton"); // NOI18N
        addShotButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addShotButtonActionPerformed(evt);
            }
        });

        jLabel7.setText(resourceMap.getString("jLabel7.text")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N

        heatModSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(1)));
        heatModSpinner.setName("heatModSpinner"); // NOI18N

        javax.swing.GroupLayout weaponPanelLayout = new javax.swing.GroupLayout(weaponPanel);
        weaponPanel.setLayout(weaponPanelLayout);
        weaponPanelLayout.setHorizontalGroup(
            weaponPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(weaponPanelLayout.createSequentialGroup()
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(heatModSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(addShotButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(deleteShotButton)
                .addContainerGap())
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 305, Short.MAX_VALUE)
        );
        weaponPanelLayout.setVerticalGroup(
            weaponPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, weaponPanelLayout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 116, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(weaponPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel7)
                    .addComponent(heatModSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addShotButton, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(deleteShotButton, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jTabbedPane1.addTab(resourceMap.getString("weaponPanel.TabConstraints.tabTitle"), weaponPanel); // NOI18N

        physicalPanel.setName("physicalPanel"); // NOI18N

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        physTable.setFont(resourceMap.getFont("physTable.font")); // NOI18N
        physTable.setModel(new DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Tgt", "Attack", "MOD", "To Hit"
            }
        ) {
            Class[] types = new Class [] {
                String.class, String.class, Integer.class, Integer.class
            };
            boolean[] canEdit = new boolean [] {
                true, true, true, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        physTable.setName("physTable"); // NOI18N
        jScrollPane2.setViewportView(physTable);

        addPhysButton.setAction(actionMap.get("showShotDlg")); // NOI18N
        addPhysButton.setText(resourceMap.getString("addPhysButton.text")); // NOI18N
        addPhysButton.setEnabled(false);
        addPhysButton.setName("addPhysButton"); // NOI18N
        addPhysButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addPhysButtonActionPerformed(evt);
            }
        });

        deletePhysButton.setText(resourceMap.getString("deletePhysButton.text")); // NOI18N
        deletePhysButton.setEnabled(false);
        deletePhysButton.setName("deletePhysButton"); // NOI18N
        deletePhysButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deletePhysButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout physicalPanelLayout = new javax.swing.GroupLayout(physicalPanel);
        physicalPanel.setLayout(physicalPanelLayout);
        physicalPanelLayout.setHorizontalGroup(
            physicalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(physicalPanelLayout.createSequentialGroup()
                .addGap(107, 107, 107)
                .addComponent(addPhysButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(deletePhysButton)
                .addContainerGap(110, Short.MAX_VALUE))
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 305, Short.MAX_VALUE)
        );
        physicalPanelLayout.setVerticalGroup(
            physicalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, physicalPanelLayout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 103, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(physicalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(addPhysButton, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(deletePhysButton, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jTabbedPane1.addTab(resourceMap.getString("physicalPanel.TabConstraints.tabTitle"), physicalPanel); // NOI18N

        movedCheckBox.setFont(resourceMap.getFont("jCheckBox4.font")); // NOI18N
        movedCheckBox.setForeground(Color.BLUE);
        movedCheckBox.setText(resourceMap.getString("movedCheckBox.text")); // NOI18N
        movedCheckBox.setName("movedCheckBox"); // NOI18N

        firedCheckBox.setFont(resourceMap.getFont("firedCheckBox.font")); // NOI18N
        firedCheckBox.setForeground(Color.RED);
        firedCheckBox.setText(resourceMap.getString("firedCheckBox.text")); // NOI18N
        firedCheckBox.setEnabled(false);
        firedCheckBox.setName("firedCheckBox"); // NOI18N

        jPanel1.setName("jPanel1"); // NOI18N

        jLabel11.setFont(resourceMap.getFont("jLabel11.font")); // NOI18N
        jLabel11.setText(resourceMap.getString("jLabel11.text")); // NOI18N
        jLabel11.setName("jLabel11"); // NOI18N

        heatText.setFont(resourceMap.getFont("heatText.font")); // NOI18N
        heatText.setText(resourceMap.getString("heatText.text")); // NOI18N
        heatText.setName("heatText"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(heatText, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(heatText)
                .addComponent(jLabel11))
        );

        physCheckBox.setFont(resourceMap.getFont("physCheckBox.font")); // NOI18N
        physCheckBox.setForeground(Color.RED);
        physCheckBox.setText(resourceMap.getString("physCheckBox.text")); // NOI18N
        physCheckBox.setEnabled(false);
        physCheckBox.setName("physCheckBox"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(movedCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(firedCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(physCheckBox)
                .addGap(45, 45, 45)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(23, 23, 23))
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 310, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel11, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 165, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(movedCheckBox)
                    .addComponent(firedCheckBox)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(physCheckBox)))
        );
    }// </editor-fold>//GEN-END:initComponents
    private void addShotButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addShotButtonActionPerformed
        // Add Shots
        JFrame mainFrame = BattletechApp.getApplication().getMainFrame();
        shotDlg = new ShotDlg(mainFrame, true, this);
        shotDlg.setLocationRelativeTo(mainFrame);
        BattletechApp.getApplication().show(shotDlg);
        if (shotDlg.getReturnStatus() == ShotDlg.RET_OK) {
            // record new shots
            // TABLE MODEL: Tgt Weapons Rng MOD Sec PrtlCvr ToHit
            Object[] weapons = shotDlg.getShotsList();
            int weaponCount = weapons.length;
            if (weaponCount > 0) {
                String target = shotDlg.getTarget();
                Integer range = shotDlg.getRange();
                Integer mod = shotDlg.getMod();
                Boolean secondary = shotDlg.getSecondary();
                Boolean partialCover = shotDlg.getPartialCover();
                // add shots
                DefaultTableModel shotsModel = (DefaultTableModel) shotsTable.getModel();
                for (int x = 0; x < weaponCount; x++) {
                    Object[] row = {target, (String) weapons[x], range, mod, secondary, partialCover, new Integer(0)};
                    shotsModel.addRow(row);
                }
            }
        }
}//GEN-LAST:event_addShotButtonActionPerformed

    private void deleteShotButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteShotButtonActionPerformed
        // Delete Shots
        int min = shotsTable.getSelectionModel().getMinSelectionIndex();
        int count = shotsTable.getSelectedRowCount();
        if (min != -1) {
            DefaultTableModel model = (DefaultTableModel) shotsTable.getModel();
            for (int x = 0; x < count; x++) {
                model.removeRow(min);
            }
        }
}//GEN-LAST:event_deleteShotButtonActionPerformed

    private void standUpSpinnerStateChanged(ChangeEvent evt) {//GEN-FIRST:event_standUpSpinnerStateChanged
        // Stand attempts changed, affect move heat
        affectMoveHeat();
    }//GEN-LAST:event_standUpSpinnerStateChanged

    private void gunskillSpinnerStateChanged(ChangeEvent evt) {//GEN-FIRST:event_gunskillSpinnerStateChanged
        // gunskill changed
        affectShots();
    }//GEN-LAST:event_gunskillSpinnerStateChanged

    private void positionComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_positionComboBoxActionPerformed
        // position changed
        affectShotsGlobal();
        affectPhysAttacksGlobal();
    }//GEN-LAST:event_positionComboBoxActionPerformed

    private void addPhysButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addPhysButtonActionPerformed
        // Add physical attacks
        JFrame mainFrame = BattletechApp.getApplication().getMainFrame();
        physDlg = new PhysDlg(mainFrame, true, this);
        physDlg.setLocationRelativeTo(mainFrame);
        BattletechApp.getApplication().show(physDlg);
        if (physDlg.getReturnStatus() == PhysDlg.RET_OK) {
            // record new phys attacks
            // TABLE MODEL: Tgt Attack MOD ToHit
            Object[] physAttacks = physDlg.getPhysAttacksList();
            int attackCount = physAttacks.length;
            if (attackCount > 0) {
                String target = physDlg.getTarget();
                Integer mod = physDlg.getMod();
                // add phys attacks
                DefaultTableModel physAttacksModel = (DefaultTableModel) physTable.getModel();
                for (int x = 0; x < attackCount; x++) {
                    Object[] row = {target, (String) physAttacks[x], mod, new Integer(0)};
                    physAttacksModel.addRow(row);
                }
            }
        }
        
}//GEN-LAST:event_addPhysButtonActionPerformed

    private void deletePhysButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deletePhysButtonActionPerformed
        // Delete Physical Attacks
        int min = physTable.getSelectionModel().getMinSelectionIndex();
        int count = physTable.getSelectedRowCount();
        if (min != -1) {
            DefaultTableModel model = (DefaultTableModel) physTable.getModel();
            for (int x = 0; x < count; x++) {
                model.removeRow(min);
            }
        }
        
}//GEN-LAST:event_deletePhysButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addPhysButton;
    private javax.swing.JButton addShotButton;
    private javax.swing.JButton deletePhysButton;
    private javax.swing.JButton deleteShotButton;
    private JSpinner distanceSpinner;
    private javax.swing.JCheckBox engineHitCheckBox1;
    private javax.swing.JCheckBox engineHitCheckBox2;
    private javax.swing.JCheckBox firedCheckBox;
    private JSpinner gunskillSpinner;
    private JSpinner heatModSpinner;
    private javax.swing.JLabel heatText;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField lanceText;
    private javax.swing.JTextField mechText;
    private javax.swing.JComboBox moveComboBox;
    private javax.swing.JCheckBox movedCheckBox;
    private javax.swing.ButtonGroup movementGroup;
    private javax.swing.JCheckBox physCheckBox;
    private JTable physTable;
    private javax.swing.JPanel physicalPanel;
    private JSpinner pilotskillSpinner;
    private javax.swing.JComboBox positionComboBox;
    private javax.swing.JCheckBox sensorHitCheckBox1;
    private javax.swing.JCheckBox sensorHitCheckBox2;
    private JTable shotsTable;
    private javax.swing.JCheckBox standUpCheckBox;
    private JSpinner standUpSpinner;
    private javax.swing.ButtonGroup statusGroup;
    private javax.swing.JTextField tonnageText;
    private javax.swing.JPanel weaponPanel;
    // End of variables declaration//GEN-END:variables
    private ShotDlg shotDlg;
    private PhysDlg physDlg;
    private int movementHeat = 0;
    private int weaponHeat = 0;
    private int engineHitHeat = 0;
    }
