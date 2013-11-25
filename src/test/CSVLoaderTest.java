package test;

import java.io.File;
import java.io.IOException;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

public class CSVLoaderTest {
    public static void main(String[] args) throws IOException {
        CSVLoader loader = new CSVLoader();
        loader.setSource(new File("./experiment/csvtest.csv"));
        Instances data = loader.getDataSet();
        ArffSaver saver = new ArffSaver();
        saver.setInstances(data);
        saver.setFile(new File("./experiment/abstract/csvtest.arff"));
        // saver.setDestination(new File(args[1]));
        saver.writeBatch();
    }
}
