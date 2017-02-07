/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.eap.LSHDB.apps;

// Imports
import gr.eap.LSHDB.HammingLSHStore;
import gr.eap.LSHDB.NoKeyedFieldsException;
import gr.eap.LSHDB.StoreInitException;
import gr.eap.LSHDB.util.FileUtil;
import gr.eap.LSHDB.util.Record;
import gr.eap.LSHDB.util.QueryRecord;
import gr.eap.LSHDB.util.Result;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.StringTokenizer;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import static javax.swing.JFrame.EXIT_ON_CLOSE;
import javax.swing.JLayer;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.Timer;

import javax.swing.border.TitledBorder;
import net.java.dev.designgridlayout.DesignGridLayout;

class TableApp_NCVR extends JFrame {

    // Instance attributes used in this example
    private JPanel topPanel;
    private JPanel dPanel;
    private JTable table;
    private JScrollPane scrollPane;
    private JTable dTable;
    private JScrollPane dScrollPane;
    private GTableModel dModel;
    private GTableModel mModel;
    
    TitledBorder tb;
    WaitLayerUI layerUI;
    JLayer<JComponent> jlayer;
    String queryFileName = "c://voters//Q.txt";
    String folder = "c://LEVELDB";
    String storeName = "NCVR_test";
    HammingLSHStore lshStore;
    int maxQueryRows = 30;
    String[] labels;
    String[] fieldNames;
    String[] descriptions;
    char[] mnemonics;
    int[] widths;
    String timeLegend = " Time to retrieve the result set: ";
    JLabel timeMsg = new JLabel();
    String noRecsLegend = " Number of retrieved records: ";
    JLabel noRecsMsg = new JLabel();
    JCheckBox check = new JCheckBox();
    //HammingLSHDB db=new HammingLSHDB("c:/tmp","testBer",null);
    //HammingLSHDB db = new HammingLSHDB("c:", "testV", null);

    // Constructor of main frame
    public void handleResult(ArrayList<Record> r) {        
        for (int j = 0; j < r.size(); j++) {
            Record rec = r.get(j);

            String[] vals = new String[rec.getFieldNames().size() + 1];
            vals[0] = r.get(j).getId();
            for (int x = 0; x < rec.getFieldNames().size(); x++) {
                String field = rec.getFieldNames().get(x);
                String value = (String) rec.get(field);

                for (int c = 0; c < fieldNames.length; c++) {
                    String fieldName = fieldNames[c];
                    if (fieldName.equals(field)) {
                        vals[c + 1] = value; // +1 due to the Id
                        break;
                    }
                }

            }

            dModel.addRow(vals);
        }
    }

    public TableApp_NCVR(String[] labels, String[] fieldNames, char[] mnemonics, int[] widths, String[] descriptions) {
        try{           
            lshStore = new HammingLSHStore(folder, storeName, "gr.eap.LSHDB.LevelDB");
        }catch(StoreInitException ex){
            ex.printStackTrace();
        }    
        this.labels = labels;
        this.fieldNames = fieldNames;
        this.mnemonics = mnemonics;
        this.widths = widths;
        this.descriptions = descriptions;

        setTitle("Queries");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1400, 700);
        setBackground(Color.gray);

        // Create a panel to hold all other components
        layerUI = new WaitLayerUI();
        topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        //getContentPane().add(topPanel);
        dPanel = new JPanel();
        dPanel.setLayout(new BorderLayout());
        tb = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "List of query records",
                TitledBorder.CENTER,
                TitledBorder.TOP);
        dPanel.setBorder(tb);
        dPanel.setLayout(new BorderLayout());

        JSplitPane splitPane = new JSplitPane();
        splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);

        mModel = new GTableModel();
        table = new JTable(mModel);

        dModel = new GTableModel();
        dModel.addColumn("Id");
        mModel.addColumn("Id");
        for (int i = 0; i < fieldNames.length; i++) {
            dModel.addColumn(fieldNames[i]);
            mModel.addColumn(fieldNames[i]);
        }

        dTable = new JTable(dModel);
        dScrollPane = new JScrollPane(dTable);
        final Timer stopper = new Timer(400, new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                layerUI.stop();
            }
        });
        stopper.setRepeats(false);

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    layerUI.start();
                    if (!stopper.isRunning()) {
                        stopper.start();
                    }
                    JTable target = (JTable) e.getSource();
                    target.setEnabled(false);
                    int row = target.getSelectedRow();
                    String id = (String) mModel.getValueAt(row, 0);
                    //dPanel.getBorder().
                    tb.setTitle("query " + id);
                    dPanel.repaint();
                    dModel.setRowCount(0);
                    //dModel.setColumnCount(0);
                    int column = target.getSelectedColumn();
                    
                    QueryRecord query = new QueryRecord(maxQueryRows); // n denotes the max number of the returned records.                    
                    for (int i = 0; i < target.getColumnCount(); i++) {
                        String columnName = target.getColumnName(i);
                        String columnValue = (String) mModel.getValueAt(row, i);
                        query.set(columnName, columnValue);
                    }
                    query.set(1.0, check.isSelected());                    
                    Result r = null;
                    long begin = System.nanoTime();
                    try{
                     r = lshStore.query(query);                    
                    }catch(NoKeyedFieldsException ex){
                        ex.printStackTrace();
                    } 
                    if (r!=null){
                       if (r.getStatus()!=Result.STATUS_OK)
                           System.out.println(r.getMsg());
                       else {
                            r.prepare();
                            long end = System.nanoTime();
                            long elapsedTime = end - begin;
                            double seconds = elapsedTime / 1.0E09;
                            r.setTime(seconds);                
                            timeMsg.setText(r.getTime()+" secs");
                            noRecsMsg.setText(r.getRecords().size()+"");
                            handleResult(r.getRecords());
                            target.setEnabled(true);
                       }  
                    }

                }
            }
        });
        table.setRowSelectionAllowed(true);
        scrollPane = new JScrollPane(table);

        JSplitPane topPane = new JSplitPane();
        topPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        topPane.setLeftComponent(scrollPane);

       
        JPanel formPanel = new JPanel();
        DesignGridLayout designLayout = new DesignGridLayout(formPanel); 
        
        check.setSelected(true);
        designLayout.row().grid(new JLabel("Perform the comparisons")).add(check).empty();     
        designLayout.row().grid(new JLabel(noRecsLegend)).add(noRecsMsg).empty();        
        designLayout.row().grid(new JLabel(timeLegend)).add(timeMsg).empty();        
        
        topPane.setRightComponent(formPanel);
        topPane.setDividerLocation(900);

        splitPane.setTopComponent(topPane);
        splitPane.setBottomComponent(dScrollPane);
        splitPane.setDividerLocation(200);
        getContentPane().add(splitPane);
        jlayer = new JLayer<JComponent>(splitPane, layerUI);

        //topPanel.add(splitPane, BorderLayout.CENTER);        
        //getContentPane().add(topPanel);
        add(jlayer);

        //dPanel.add(splitPane, BorderLayout.CENTER);        
        //dPanel.add(dScrollPane, BorderLayout.CENTER);
        //topPanel.add(dPanel, BorderLayout.SOUTH);
    }

    // Main entry point for this example
    public static void main(String args[]) {
        
        String[] labels = {" First Name ", " Last Name ", " Address ", " Town "};
        String[] fieldNames = {"First Name", "Last Name", "Address", "Town"};
        char[] mnemonics = {'F', 'L', 'A', 'T'};
        int[] widths = {25, 25, 25, 25};
        String[] descriptions = {"First Name", "Last Name", "Address", "Town"};
        TableApp_NCVR main = new TableApp_NCVR(labels, fieldNames, mnemonics, widths, descriptions);
        
        try {
            FileReader input1 = new FileReader(main.queryFileName);
            BufferedReader bufRead1 = new BufferedReader(input1);
            int lines = FileUtil.countLines(main.queryFileName);
            String[] vals = new String[7];
            for (int i = 0; i < lines; i++) {
                String line1 = bufRead1.readLine();
                StringTokenizer st1 = new StringTokenizer(line1, ",");
                String id = st1.nextToken(); //id
                String lastName = st1.nextToken();
                String firstName = st1.nextToken();
                String address = st1.nextToken();
                String town = st1.nextToken();
                vals[0] = id;
                vals[1] = firstName;
                vals[2] = lastName;
                vals[3] = address;
                vals[4] = town;               
                main.mModel.addRow(vals);

            }

        } catch (Exception e) {//Catch exception if any
            e.printStackTrace();
        }

        // Create an instance of the test application
        main.setVisible(true);
    }
}
