package preprocess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Translate training file with csv format to folders based on class
 * @author Yang
 *
 */
public class CsvToFolders {

    private Map<String, List<String>> catToTexts;
    
    public CsvToFolders(String csv) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(
                new FileInputStream(csv), "UTF-8"));
        catToTexts = new HashMap<String, List<String>>();
        while (in.ready()) {
            String line = in.readLine();
            String[] splits = line.split("\t");
            
            if (splits.length == 2) {
                String text = splits[0];
                String clazz = splits[1];
                if (catToTexts.containsKey(clazz)) {
                    catToTexts.get(clazz).add(text);
                } else {
                    List<String> tmp = new LinkedList<String>();
                    tmp.add(text);
                    catToTexts.put(clazz, tmp);
                }
            } else {
                System.err.println("Warning: ignore: " + line);
            }
        }
        in.close();
    }
    
    public Map<String, List<String>> getCatToTexts() {
        return catToTexts;
    }
    
    public void save(String dst) throws IOException {
        for (Iterator<String> iter = catToTexts.keySet().iterator();
                iter.hasNext();) {
            String cat = iter.next();
            File catDir = new File(dst, cat);
            if (!catDir.exists()) catDir.mkdirs();
            else System.err.println("Warning: " + catDir + " exists, overwriting happens");
            
            List<String> texts = catToTexts.get(cat);
            for (int i = 0; i < texts.size(); i++) {
                String text = texts.get(i);
                if (text.length() == 0) continue;
                File f = new File(catDir, String.format("%03d", i));
                if (!f.exists()) f.createNewFile();
                else System.err.println("Warning: " + f + " exists, overwriting happens");
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(f), "UTF-8"));
                out.append(text);
                out.close();
            }
        }
    }
    
    public static void main(String[] args) throws IOException {
        CsvToFolders c2f = new CsvToFolders("./exper/train.csv");
        c2f.save("./exper/abstracts/train/");
        CsvToFolders c2f2 = new CsvToFolders("./exper/test.csv");
        c2f2.save("./exper/abstracts/test/");
    }
}
