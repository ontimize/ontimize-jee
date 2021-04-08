package com.ontimize.jee.desktopclient.test.reportstore;

import javax.swing.JFrame;

public class UtilReportStore extends JFrame {

    // protected JFileChooser file = new JFileChooser();
    // protected JLabel label = null;
    // protected JTextField text = null;
    // protected JButton bFile = null;
    // protected JButton bRefresh = null;
    // protected JTable reportTable = null;
    //
    // protected class ReportModel extends AbstractTableModel {
    //
    // protected Hashtable data = new Hashtable();
    // protected Vector keys = new Vector();
    //
    // public ReportModel(Hashtable h) {
    // this.setData(h);
    // }
    //
    // @Override
    // public int getColumnCount() {
    // return 7;
    // }
    //
    // @Override
    // public int getRowCount() {
    // if (this.data != null) {
    // return this.data.size();
    // }
    // return 0;
    // }
    //
    // @Override
    // public Object getValueAt(int rowIndex, int columnIndex) {
    // if (columnIndex == 0) {
    // return this.keys.get(rowIndex);
    // }
    //
    // if (columnIndex == 1) {
    // Object k = this.keys.get(rowIndex);
    // IReportDefinition def = (IReportDefinition) this.data.get(k);
    // return FileReportStore.generateDirectoryName(def.getName());
    // }
    //
    // if (columnIndex == 2) {
    // Object k = this.keys.get(rowIndex);
    // IReportDefinition def = (IReportDefinition) this.data.get(k);
    // return def.getName();
    // }
    //
    // if (columnIndex == 3) {
    // Object k = this.keys.get(rowIndex);
    // IReportDefinition def = (IReportDefinition) this.data.get(k);
    // return def.getDescription();
    // }
    //
    // if (columnIndex == 4) {
    // Object k = this.keys.get(rowIndex);
    // IReportDefinition def = (IReportDefinition) this.data.get(k);
    // return def.getEntity();
    // }
    //
    // if (columnIndex == 5) {
    // Object k = this.keys.get(rowIndex);
    // IReportDefinition def = (IReportDefinition) this.data.get(k);
    // return def.getQueryExpression();
    // }
    // return null;
    // }
    //
    // @Override
    // public String getColumnName(int column) {
    // if (column == 0) {
    // return "Directory";
    // }
    // if (column == 1) {
    // return "Name";
    // }
    // if (column == 2) {
    // return "Report name";
    // }
    // if (column == 3) {
    // return "Description";
    // }
    // if (column == 4) {
    // return "Entity";
    // }
    // if (column == 5) {
    // return "Query";
    // }
    // if (column == 6) {
    // return "Type";
    // }
    // return super.getColumnName(column);
    // }
    //
    // public void setData(Hashtable h) {
    // this.data = h;
    // Enumeration enu = h.keys();
    // this.keys.clear();
    // while (enu.hasMoreElements()) {
    // this.keys.add(enu.nextElement());
    // }
    // this.fireTableDataChanged();
    // }
    //
    // }
    //
    // protected File root = null;
    //
    // public UtilReportStore() {
    // this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    // this.jInit();
    // }
    //
    // protected void jInit() {
    // JPanel control = new JPanel(new GridBagLayout());
    // this.label = new JLabel("Directorio raiz");
    // this.text = new JTextField();
    // this.bFile = new JButton("file");
    // this.bRefresh = new JButton("refresh");
    // control.add(this.label, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST,
    // GridBagConstraints.HORIZONTAL, new Insets(0, 0,
    // 0, 0), 0, 0));
    // control.add(this.text, new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.CENTER,
    // GridBagConstraints.HORIZONTAL, new Insets(0,
    // 0, 0, 0), 0, 0));
    // control.add(this.bFile, new GridBagConstraints(2, 0, 1, 1, 0, 0, GridBagConstraints.EAST,
    // GridBagConstraints.HORIZONTAL, new Insets(0, 0,
    // 0, 0), 0, 0));
    // control.add(this.bRefresh, new GridBagConstraints(0, 3, 3, 1, 1, 0, GridBagConstraints.CENTER,
    // GridBagConstraints.NONE, new Insets(0, 0,
    // 0, 0), 0, 0));
    //
    // this.file = new JFileChooser();
    // this.file.setMultiSelectionEnabled(false);
    // this.file.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    // this.bFile.addActionListener(new ActionListener() {
    // @Override
    // public void actionPerformed(ActionEvent e) {
    // int op = UtilReportStore.this.file.showOpenDialog(UtilReportStore.this);
    // if (op == JFileChooser.APPROVE_OPTION) {
    // UtilReportStore.this.root = UtilReportStore.this.file.getSelectedFile();
    // UtilReportStore.this.text.setText(UtilReportStore.this.root.getAbsolutePath());
    // }
    // }
    // });
    //
    // this.bRefresh.addActionListener(new ActionListener() {
    // @Override
    // public void actionPerformed(ActionEvent e) {
    // if ((UtilReportStore.this.root != null) && UtilReportStore.this.root.isDirectory()) {
    // UtilReportStore.this.refreshTable();
    // }
    // }
    // });
    // this.reportTable = new JTable();
    //
    // JScrollPane jscTablePanel = new JScrollPane(this.reportTable);
    //
    // this.getContentPane().setLayout(new BorderLayout());
    // this.getContentPane().add(control, BorderLayout.NORTH);
    // this.getContentPane().add(jscTablePanel);
    // this.pack();
    // ApplicationManager.center(this);
    // }
    //
    // public void refreshTable() {
    // File[] files = this.root.listFiles();
    // Hashtable hList = new Hashtable();
    // if (files == null) {
    // return;
    // }
    // for (int i = 0; i < files.length; i++) {
    // String name = files[i].getName();
    // if (name.startsWith(FileReportStore.PREFIX) && files[i].isDirectory()) {
    // // Properties
    // String p = FileReportStore.getPropertiesFileName(name);
    // File fProp = new File(files[i], p);
    // if (fProp.exists() == false) {
    // FileReportStore.logger.warn("Found directory {} but NOT found report properties: {}", files[i],
    // fProp);
    // continue;
    // }
    // FileInputStream fInProp = null;
    // try {
    // fInProp = new FileInputStream(fProp);
    // Properties prop = new Properties();
    // prop.load(fInProp);
    // BasicReportDefinition bd = new
    // BasicReportDefinition(prop.getProperty(FileReportStore.PROPERTY_NAME), prop.getProperty(
    // FileReportStore.PROPERTY_DESCRIPTION, ""), prop.getProperty("entity"), null,
    // prop.getProperty("query"),
    // prop.getProperty(FileReportStore.PROPERTY_TYPE), null, null);
    // hList.put(p, bd);
    // } catch (IOException e) {
    // FileReportStore.logger.error(e.getMessage(), e);
    // } finally {
    // try {
    // if (fInProp != null) {
    // fInProp.close();
    // }
    // } catch (Exception ex) {
    // }
    // }
    // }
    // }
    //
    // ReportModel model = new ReportModel(hList);
    // this.reportTable.setModel(model);
    //
    // }
    //
    // public static void main(String[] args) {
    // try {
    // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    // } catch (Exception e) {
    //
    // }
    // UtilReportStore util = new UtilReportStore();
    // util.setVisible(true);
    // }

}
