package wrapper;

import java.io.File;
import java.io.IOException;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

public class SvmInstanceFormatWrapper {

    // translate csv file to arff file
    public static void main(String[] args) throws IOException {
        CSVLoader loader = new CSVLoader();
        loader.setSource(new File("./experiment/abstract/train-batch-full.csv"));
        Instances data = loader.getDataSet();
        ArffSaver saver = new ArffSaver();
        saver.setInstances(data);
        saver.setFile(new File("./experiment/abstract/train-batch-full.arff"));
        // saver.setDestination(new File("./experiment/abstract/train-batch-full.arff"));
        saver.writeBatch();
    }
}
