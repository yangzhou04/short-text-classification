package analysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Sampler {

    public static void sample(String csv, String test, String train, double percent)
            throws IOException {
        
        BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(csv), "UTF-8"));

        List<String> data = new LinkedList<String>();
        while (br.ready())
            data.add(br.readLine());
        br.close();

        Collections.shuffle(data);
        int n1 = (int) (data.size() * percent);
        File part1File = new File(test);
        if (!part1File.exists()) {
            part1File.getParentFile().mkdirs();
            part1File.createNewFile();
        }
        File part2File = new File(train);
        if (!part2File.exists()) {
            part2File.getParentFile().mkdirs();
            part2File.createNewFile();
        }
        
        BufferedWriter bw1 = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(test), "UTF-8"));
        BufferedWriter bw2 = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(train), "UTF-8"));
        for (int i = 0; i < n1; i++) {
            bw1.append(data.get(i));
            bw1.newLine();
        }
        bw1.close();
        for (int i = n1; i < data.size(); i++) {
            bw2.append(data.get(i));
            bw2.newLine();
        }
        bw2.close();
    }

    public static void main(String[] args) throws IOException {
        double testPercent = 0.6;
        Sampler.sample("exper2/all.csv", 
                "exper2/test.csv", "exper2/train.csv", testPercent);
    }
}
