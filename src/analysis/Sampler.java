package analysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Sampler {

    public void sampler(String csv, String part1, String part2, double percent)
            throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(csv), "UTF-8"));

        List<String> data = new LinkedList<String>();
        while (br.ready())
            data.add(br.readLine());
        br.close();

        Collections.shuffle(data);
        int n1 = (int) (data.size() * percent);
        BufferedWriter bw1 = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(part1), "UTF-8"));
        BufferedWriter bw2 = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(part2), "UTF-8"));
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
        Sampler s = new Sampler();
        s.sampler("./experiment/labeled2.csv", "./experiment/lpart1.csv",
                "./experiment/rpart1.csv", 0.2);
    }
}
