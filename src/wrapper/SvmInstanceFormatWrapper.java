package wrapper;

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
import java.util.Set;
import java.util.TreeSet;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

public class SvmInstanceFormatWrapper {

    // translate csv file to arff file
    public static void main(String[] args) throws IOException {
        
        // 转换sparse的csv为vector的形式
        BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream("./experiment/abstract/2class_sparse.csv"), "UTF-8"));
        Set<String> vocabulary = new TreeSet<String>();
        while (br.ready()) {
            String line = br.readLine();
            int idx = line.lastIndexOf(',');
            if (idx == -1) continue;
            String[] terms = line.substring(0, idx).split(",");
            vocabulary.addAll(Arrays.asList(terms));
        }
        br.close();
        
        br = new BufferedReader(new InputStreamReader(
                new FileInputStream("./experiment/abstract/2class_sparse.csv"), "UTF-8"));
        
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream("./experiment/abstract/2class_vector.csv"), "UTF-8"));
        for (Iterator<String> iter = vocabulary.iterator(); iter.hasNext();) {
            bw.append("'");
            bw.append(iter.next());
            bw.append("'");
            bw.append(",");
        }
        bw.append("class");
        bw.newLine();
        
        while (br.ready()) {
            String line = br.readLine();
            int idx = line.lastIndexOf(',');
            if (idx == -1) continue;
            String[] terms = line.substring(0, idx).split(",");
            String clazz = line.substring(idx+1);
            Set<String> termSet = new TreeSet<String>(Arrays.asList(terms));
            for (Iterator<String> iter = vocabulary.iterator(); iter.hasNext();) {
                if (termSet.contains(iter.next()))
                    bw.append("1");
                else
                    bw.append("0");
                bw.append(",");
            }
            bw.append(clazz);
            bw.newLine();
        }
        bw.close();
        
        //
        CSVLoader loader = new CSVLoader();
        loader.setSource(new File("./experiment/abstract/2class_vector.csv"));
        Instances data = loader.getDataSet();
        ArffSaver saver = new ArffSaver();
        saver.setInstances(data);
        saver.setFile(new File("./experiment/abstract/2class_vector.arff"));
        saver.writeBatch();
    }
}
