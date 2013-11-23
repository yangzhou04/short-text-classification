package feature;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class InformationGain {

    private int docNum = 0;
    private Map<String, Integer> tMap; // document frequency that term occurs
    private Map<String, Map<String, Integer>> ctMap; // class, term frequency
    private Map<String, Integer> cMap; // class frequency
    private Map<String, Double> igMap; // ig of each term

    public InformationGain() {
        tMap = new HashMap<String, Integer>();
        cMap = new HashMap<String, Integer>();
        ctMap = new HashMap<String, Map<String, Integer>>();
        igMap = new HashMap<String, Double>();
    }

    public InformationGain(String dir) throws IOException {
        this();

        File fdir = new File(dir);
        if (!fdir.isDirectory()) {
            System.err.println("need Lingpipe train directory architecture");
            return;
        }

        for (File cf : fdir.listFiles()) {
            String c = cf.getName();
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    new FileInputStream(new File(cf, "labeledCorpus.txt")),
                    "UTF-8"));

            // term frequency of class c
            Map<String, Integer> cttMap;
            if (ctMap.containsKey(c))
                cttMap = ctMap.get(c);
            else {
                cttMap = new HashMap<String, Integer>();
                ctMap.put(c, cttMap);
            }

            while (br.ready()) {
                docNum++;
                // class frequency statistics
                if (cMap.containsKey(c))
                    cMap.put(c, cMap.get(c) + 1);
                else
                    cMap.put(c, 1);

                String line = br.readLine();
                String[] terms = line.split(" ");
                // term frequency statistics
                Set<String> occured = new TreeSet<String>();
                for (String t : terms) {
                    if (!occured.contains(t)) {
                        if (tMap.containsKey(t))
                            tMap.put(t, tMap.get(t) + 1);
                        else
                            tMap.put(t, 1);

                        if (cttMap.containsKey(t))
                            cttMap.put(t, cttMap.get(t) + 1);
                        else
                            cttMap.put(t, 1);

                        occured.add(t);
                    }
                }
            }
            br.close();
        }
        calcInformationGain();
    }

    public Map<String, Double> get() {
        return igMap;
    }

    private double entropy(List<Double> X) {
        double e = 0;
        double s = 0;
        for (double x : X) {
            if (Double.compare(x, 0) != 0) {
                e += x * Math.log(x);
                s += x;
            }
        }
        System.out.println(s);
        assert s == 1;
        return -e;
    }

    private void calcInformationGain() {
        // calc P(C)
        List<Double> pc = new LinkedList<Double>();
        for (Iterator<String> iter = cMap.keySet().iterator(); iter.hasNext();) {
            String c = iter.next();
            pc.add(cMap.get(c) / (double) docNum);
        }

        double hc = entropy(pc);

        Set<String> terms = tMap.keySet();
        Set<String> classes = cMap.keySet();
        for (Iterator<String> term = terms.iterator(); term.hasNext();) {
            String t = term.next();
            double ig = hc;

            // 出现t的文档数
            int docNumContainT = tMap.get(t);
            // 没出现t的文档数
            int docNumNotContainT = docNum - docNumContainT;
            double pt = docNumContainT / (double) docNum;
            double pnt = 1 - pt;

            List<Double> ciGivenTList = new LinkedList<Double>();
            List<Double> ciGivenNTList = new LinkedList<Double>();
            for (Iterator<String> clazz = classes.iterator(); clazz.hasNext();) {
                String c = clazz.next();
                System.out.println("Processing ..." + c + " " + t);
                // ci中出现t的文档数
                assert ctMap.containsKey(c);
                if (ctMap.get(c).containsKey(t)) {
                    int docNumInCContainT = ctMap.get(c).get(t);
                    // P(c_i | t)
                    double ciGivent = docNumInCContainT / (double) docNumContainT;
                    ciGivenTList.add(ciGivent);
                    // ci中的文档数
                    int docNumInC = cMap.get(c);
                    // ci中没出现t的文档数
                    int docNumInCNotContainT = docNumInC - docNumInCContainT;

                    // P(c_i | not t)
                    double ciGivenNT = docNumInCNotContainT / (double) docNumNotContainT;
                    ciGivenNTList.add(ciGivenNT);
                } else {
                    ciGivenTList.add(0d);
                    ciGivenNTList.add(1d);
                    System.out.println("class = " + c +" <-> term =  "+ t);
                }
            }
            ig -= pt * entropy(ciGivenTList);
            ig -= pnt * entropy(ciGivenNTList);

            igMap.put(t, ig);
        }
    }

    public static void main(String[] args) throws IOException {
        InformationGain ig = new InformationGain("./experiment/abstract/try/");
        Map<String, Double> igMap = ig.get();
        System.out.println(igMap);
    }
}
