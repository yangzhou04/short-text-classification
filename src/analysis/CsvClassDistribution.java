package analysis;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CsvClassDistribution {
    
    private Map<String, Integer> classCounter;
    private Map<String, Double> classDistributioner;
    private int totalCount = 0;
    
    public CsvClassDistribution(String src) throws IOException {
        classCounter = new HashMap<String, Integer>();
        classDistributioner = new HashMap<String, Double>();
        doStatistics(src);
    }
    
    private void doStatistics(String src) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(
                new FileInputStream(src), "UTF-8"));
        while (in.ready()) {
            String line = in.readLine();
            String[] splits = line.split("\t");
            
            if (splits.length > 0) {
                String clazz = splits[splits.length-1];
                if (classCounter.containsKey(clazz)) {
                    classCounter.put(clazz, classCounter.get(clazz)+1);
                } else {
                    classCounter.put(clazz, 1);
                }
                totalCount++;
            } else {
                System.err.println("Warning: ignore: " + line);
            }
        }
        in.close();
        for (Iterator<String> iter = classCounter.keySet().iterator();
                iter.hasNext();){
            String key = iter.next();
            Integer val = classCounter.get(key);
            classDistributioner.put(key, val/(double)totalCount);
        }
    }
    
    public Map<String, Integer> getCount() {
        return classCounter;
    }
    
    public Map<String, Double> getDistribution() {
        return classDistributioner;
    }
    
    public static void main(String[] args) throws IOException {
        CsvClassDistribution dist1 = new CsvClassDistribution("exper/exper7/exper7_test.csv");
        System.out.println(dist1.getCount());
        System.out.println(dist1.getDistribution());
    }
}
