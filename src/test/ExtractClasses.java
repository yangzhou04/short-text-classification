package test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Set;
import java.util.TreeSet;

public class ExtractClasses {
    public static void main(String[] args) throws IOException {
        // extract classes
        Set<String> classes = new TreeSet<String>();
        classes.add("调控"); classes.add("输出");
        
        BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream("./experiment/labeled.csv"), "UTF-8"));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream("./experiment/2class.csv"), "UTF-8"));
        while (br.ready()) {
            String line = br.readLine();
            String[] splits = line.split("\t");
            String clazz = splits[1];
            if (classes.contains(clazz)) {
                bw.append(line);
                bw.newLine();
            }
        }
        br.close();bw.close();
        
        // 
    }
}
