package analysis;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class DistanceAnalysis {

    public static Map<String, Map<String, Double>> 
            getWordDistributionOfPerClass(String src) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(src), "UTF-8"));
        Map<String, Map<String, Integer>> wordCountOfPerClass = 
                new HashMap<String, Map<String, Integer>>();
        Set<String> vocabulary = new TreeSet<String>();
        while (br.ready()) {
            String[] splits = br.readLine().split("\t");
            String clazz = splits[1];
            String[] tokens = splits[0].split(" ");
            for (int i = 0; i < tokens.length; i++) {
                if (!vocabulary.contains(tokens[i]))
                    vocabulary.add(tokens[i]);
                
                if (!wordCountOfPerClass.containsKey(clazz))
                    wordCountOfPerClass.put(clazz, new HashMap<String, Integer>());
                Map<String, Integer> wordCount = wordCountOfPerClass.get(clazz);
                if (wordCount.containsKey(tokens[i]))
                    wordCount.put(tokens[i], wordCount.get(tokens[i])+1);
                else
                    wordCount.put(tokens[i], 1);
            }
        }
        br.close();
        
        Map<String, Map<String, Double>> wordDistributionOfPerClass = 
                new HashMap<String, Map<String, Double>>();
        
        for (Iterator<String> vIter = vocabulary.iterator(); vIter.hasNext();) {
            String v = vIter.next();
            for (Iterator<String> clazzIter = wordCountOfPerClass.keySet().iterator();
                    clazzIter.hasNext();) {
                String clazz = clazzIter.next();
                if (!wordDistributionOfPerClass.containsKey(clazz))
                    wordDistributionOfPerClass.put(clazz, new HashMap<String, Double>());
                
                Map<String, Double> wordDistrubution = wordDistributionOfPerClass.get(clazz);
                Map<String, Integer> wordCount = wordCountOfPerClass.get(clazz);
                final int B = vocabulary.size();
                int N = 0;
                for (Iterator<String> wordIter = wordCount.keySet().iterator();
                        wordIter.hasNext();)
                    N += wordCount.get(wordIter.next());
                if (wordCount.containsKey(v))
                    wordDistrubution.put(v, (wordCount.get(v)+1) / (double)(N+B));
                else
                    wordDistrubution.put(v, 1 / (double)(N+B));
            }
        }
        
        return wordDistributionOfPerClass;
    }
    
    public static void distInExper6Test() throws IOException {
        Map<String, Map<String, Double>> wordDistributionOfPerClass = 
                getWordDistributionOfPerClass("exper/exper6/exper6_test.choosed.csv");

        System.out.print("\t");
        for (String className : wordDistributionOfPerClass.keySet()) {
            System.out.print(className);
            System.out.print("\t");
        }
        System.out.println();
        
        String[] classNameArr = wordDistributionOfPerClass.keySet().toArray(new String[0]);
        
        for (String className : classNameArr) {
            Map<String, Double> class1 = wordDistributionOfPerClass.get(className);
            System.out.print(className + "\t");
            for (String className2 : classNameArr) {
                    Map<String, Double> class2 = wordDistributionOfPerClass.get(className2);
                    int i = 0;
                    assert class1.size() == class2.size();
                    double[] p = new double[class1.size()];
                    double[] q = new double[class2.size()];
                    for (String word : class1.keySet()) {
                        p[i] = class1.get(word);
                        q[i] = class2.get(word);
                        i++;
                    }
                    double dist = Distance.KullbackLeibler(p, q);
                    System.out.print(String.format("%.2f", dist));
                    System.out.print("\t");
            }
            System.out.println();
        }
    }
    
    public static void main(String[] args) throws IOException {
        
    }

}
