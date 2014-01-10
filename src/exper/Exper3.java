package exper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import classifier.EMNaiveBayesClassifier;
import preprocess.CsvToFolders;

public class Exper3 {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        // cross fold number
        final int N = 5;
        // source csv file path
        final String src = "exper4/all.seged.csv.shuffled.csv";
        
        List<String> data = new ArrayList<String>();
        BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(src), "utf-8"));
        // one line one instance
        int instanceNum;
        for(instanceNum = 0; br.ready(); instanceNum++)
            data.add(br.readLine());
        br.close();
        
        // save shuffled result
        boolean reShuffled = false;
        if (reShuffled) {
            boolean saveShuffled = true;
            Collections.shuffle(data);
            if (saveShuffled) {
                File out = new File(src + ".shuffled.csv");
                if (!out.exists()) 
                    out.createNewFile();
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(out), "utf-8"));
                for (String instance : data) {
                    bw.append(instance);
                    bw.newLine();
                }
                bw.close();
            }
        }
        int[] bounds = new int[N+1];
        int step = instanceNum / N;
        for (int i = 0; i < N+1; i++)
            bounds[i] = i * step;
        
        List<String> trainList = new LinkedList<String>(), 
                testList = new LinkedList<String>();
        // cross fold validation
        final String DIR = "exper4/";
        
        for (int i = 0; i < N; i++) {
            final String SUB_DIR = DIR + "cross" + i + "/";
            
            if (reShuffled) {
                int floor = bounds[i],
                    ceil = bounds[i+1];
                
                trainList.clear();
                testList.clear();
                trainList.addAll(data.subList(0, floor));
                trainList.addAll(data.subList(ceil, data.size()));
                testList.addAll(data.subList(floor, ceil));
                
                File subdir = new File(SUB_DIR);
                if (!subdir.exists()) subdir.mkdirs();
                
                File trainFile = new File(subdir, "train.csv");
                BufferedWriter trainBw = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(trainFile), "utf-8"));
                for (String instance : trainList) {
                    trainBw.append(instance);
                    trainBw.newLine();
                }
                trainBw.close();
            
                File testFile = new File(subdir, "test.csv");
                BufferedWriter testBw = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(testFile), "utf-8"));
                for (String instance : testList) {
                    testBw.append(instance);
                    testBw.newLine();
                }
                testBw.close();
                
                CsvToFolders trainTranslate = new CsvToFolders(trainFile.getAbsolutePath(), 
                        SUB_DIR + "train/");
                trainTranslate.translateLabeled();
                CsvToFolders testTranslate = new CsvToFolders(testFile.getAbsolutePath(), 
                        SUB_DIR + "test/");
                testTranslate.translateLabeled();
            }
            File out = new File(SUB_DIR+"semi-supervised.txt");
            if (!out.exists()) {
                out.getParentFile().mkdirs();
                out.createNewFile();
            }
            System.setOut(new PrintStream(out, "UTF-8"));
            
            EMNaiveBayesClassifier emnbc = new EMNaiveBayesClassifier(SUB_DIR, // labeled corpus
                    "exper3/unlabeled", // unlabeled corpus
                    100, // maximum iteration
                    1, // minimum token count
                    1, // minimum improvement
                    100000, // cat prior
                    1, // token prior
                    20); // length norm
            
            System.out.println(emnbc.getConfusionMatrix());
            System.out.println("Macro precision = " + emnbc.macroAvgPrecision());
            System.out.println("Macro precision without NaN = " + emnbc.macroAvgPrecisionExceptNaN());
            System.out.println("Micro accuracy = " + emnbc.microAccuracy());
            System.out.println("Total accuracy = " + emnbc.totalAccuracy());
            System.out.println("Micro precision = " + emnbc.microPrecision());
            System.out.println("Micro recall = " + emnbc.microRecall());
            System.out.println("Micro fmeasure = " + emnbc.microFmeasure());
            emnbc.printCategoriesAprioriProbability();
        }
    }

}
