package test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import feature.InformationGain;

public class FeatureExtractionForSVM {
    
    
    
    
    public static void main(String[] args) throws IOException {
        
        
        
        
        InformationGain ig = new InformationGain("./experiment/abstract/train/");
        Map<String, Double> igMap = ig.get();
        Set<String> vocabulary = igMap.keySet();
        
//        for (Iterator<String> term = igMap.keySet().iterator(); term
//                .hasNext();) {
//            String t = term.next();
//            System.out.println(t + ": " + igMap.get(t));
//        }
        
        BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream("./experiment/abstract/train-batch.csv"),
                "UTF-8"));
        
        
        File csvFile = new File("./experiment/abstract/train-batch-full.csv");
        if (csvFile.exists()) csvFile.delete();
        csvFile.getParentFile().mkdirs(); csvFile.createNewFile();
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(csvFile), "UTF-8"));
        int i = 1;
        for (Iterator<String> iterms = vocabulary.iterator(); iterms.hasNext();) {
            bw.append("'");
//             bw.append(iterms.next());
            
            bw.append("att" + i++); iterms.next();
            bw.append("'");
            bw.append(",");
        }
        bw.append("'class'");
        bw.newLine();
        while (br.ready()) {
            String line = br.readLine();
            int idx = line.lastIndexOf(',');
            if (idx == -1) continue;
            String[] terms = line.substring(0, idx).split(",");
            String clazz = line.substring(idx+1);
            
            Set<String> features = new TreeSet<String>(Arrays.asList(terms));
            for (Iterator<String> iterms = vocabulary.iterator(); iterms.hasNext();) {
                String t = iterms.next(); 
                
                if (features.contains(t)) {
                    bw.append(String.valueOf(1));
//                    bw.append(String.valueOf(igMap.get(t)));
                    bw.append(",");
                }
                else {
                    bw.append("0");
                    bw.append(",");
                }
            }
            bw.append("'");
            
//            for (byte b : clazz.getBytes("UTF-8"))
                bw.append(clazz);
            bw.append("'");
            bw.newLine();
        }
        bw.close();
        br.close();
    }
}
