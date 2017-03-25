/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.eap.LSHDB.apps;

import gr.eap.LSHDB.DataStore;
import gr.eap.LSHDB.HammingConfiguration;
import gr.eap.LSHDB.HammingKey;
import gr.eap.LSHDB.HammingLSHStore;
import gr.eap.LSHDB.Key;
import gr.eap.LSHDB.StoreInitException;
import gr.eap.LSHDB.util.FileUtil;
import gr.eap.LSHDB.util.Record;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.StringTokenizer;
import gr.eap.LSHDB.embeddables.BloomFilter;

/**
 *
 * @author dimkar
 */
public class Insert {

    public static void main(String[] args) {
        try {
            String file = "c:/voters/dblp.txt"; // Specify  the full path of dblp.txt   
            int lines = FileUtil.countLines(file);
            System.out.println("About to insert " + lines + " records.");
           
            FileReader input1 = new FileReader(file);
            BufferedReader bufRead1 = new BufferedReader(input1);
           
                String folder = "c:/LEVELDB";
                String storeName = "dblp";
                String engine = "gr.eap.LSHDB.LevelDB";
                HammingLSHStore lsh;
                int base = 0;
                if (!DataStore.exists(folder, storeName)) {
                    Key key1 = new HammingKey("author", 32, .1, 52, true, true, new BloomFilter(500, 19, 2));
                    HammingConfiguration hc = new HammingConfiguration(folder, storeName, engine, new Key[]{key1}, true);
                    hc.saveConfiguration();
                    lsh = new HammingLSHStore(folder, storeName, engine, hc, true);
                    lsh.setCacheNoLimit(5000);

                } else {
                    lsh = (HammingLSHStore) HammingLSHStore.open(storeName);  
                    lsh.getConfiguration().saveConfiguration();
                    lsh.setCacheNoLimit(5000);
                    base = 1621004;
                }

               
                //for (int i = base; i < prevBase; i++) {
                  //  String line1 = bufRead1.readLine();
                //}

                for (int i = 0; i < lines-1; i++) {
                    
                    String line1 = bufRead1.readLine();
                    StringTokenizer st1 = new StringTokenizer(line1, ",");
                   
                    String id = base+i + ""; //id
                    st1.nextToken();

                    String type = st1.nextToken().trim();
                    String author = st1.nextToken().trim();
                    String title = st1.nextToken().trim().replace(".", "");
                    String year = "";
                    if (st1.hasMoreTokens()) {
                        year = st1.nextToken().trim();
                    }
                    String journal = ""; // journal or conference
                    if (st1.hasMoreTokens()) {
                        journal = st1.nextToken().trim();
                    }
                    String pages = "";
                    if (st1.hasMoreTokens()) {
                        pages = st1.nextToken().trim();
                    }

                    Record rec = new Record();
                    rec.setId(id);

                    rec.set("author", author);
                    String[] authors = author.split(" ");

                    rec.set("year", year);

                    rec.set("title", title);
                    //String[] titles = title.split(" ");
                    //rec.set("title_tokens", titles);

                    rec.set("pages", pages);
                    if (type.equals("A")) {
                        rec.set("J", journal);
                    } else {
                        rec.set("C", journal);
                    }

                    if (authors.length > 1) { // We insert those first authors, who have at least one given name and one surname.                    
                        rec.set("author" + Key.TOKENS, new String[]{authors[authors.length - 1]});
                        lsh.insert(rec);
                    }

                    System.out.println(id);

                }    
               
                lsh.close();
                
            
        } catch (StoreInitException ex) {
            ex.getMessage();
        } catch (Exception e) {//Catch exception if any
            e.printStackTrace();
        }
    }
}
