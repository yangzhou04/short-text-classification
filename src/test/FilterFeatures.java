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
import java.util.List;

import com.aliasi.util.Files;

import feature.Features;

public class FilterFeatures {
    public static List<String> choosedFeatureList;
    
    public static void filter(String src, String dst) throws IOException {
        BufferedReader trainReader = new BufferedReader(new InputStreamReader(
                new FileInputStream(src), "utf-8"));
        BufferedWriter trainWriter = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(dst), "utf-8"));
        while (trainReader.ready()) {
            String line = trainReader.readLine();
            String[] splits = line.split("\t");
            
            if (splits.length == 2) {
                String text = splits[0];
                String[] words = text.split(" ");
                String clazz = splits[1];
                for (String word : words) {
                    if (choosedFeatureList.contains(word)) {
                        trainWriter.append(word);
                        trainWriter.append(" ");
                    }
                }
                trainWriter.append("\t");
                trainWriter.append(clazz);
                trainWriter.newLine();
            } else 
                System.err.println("ignore line : " + line);
        }
        trainReader.close();trainWriter.close();
    }

    public static void main(String[] args) throws IOException {
        String igpath = "exper/exper6/feature-ig.csv";
        String content = Files.readFromFile(new File(igpath), "utf-8");
        String[] features= content.split("\n");
        List<String> featureList = Arrays.asList(features);
        choosedFeatureList = Features.chooseTop(featureList, 0.25);
//        filter("exper/exper6/exper6_train.csv", "exper/exper6/exper6_train.choosed.csv");
//        filter("exper/exper6/exper6_test.csv", "exper/exper6/exper6_test.choosed.csv");
        filter("exper/exper7/exper7_test.csv", "exper/exper7/exper7_test.choosed.csv");
//        filter("exper/exper6/exper6_train.csv", "exper/exper6/exper6_train.choosed.csv");
    }

}
