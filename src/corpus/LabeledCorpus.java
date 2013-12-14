package corpus;

import java.io.File;
import java.io.IOException;

import com.aliasi.classify.Classification;
import com.aliasi.classify.Classified;
import com.aliasi.corpus.Corpus;
import com.aliasi.corpus.ObjectHandler;
import com.aliasi.util.Files;

public class LabeledCorpus extends
        Corpus<ObjectHandler<Classified<CharSequence>>> {

    private final File mTrainCorpus;
    private final File mTestCorpus;
    private String[] mCats;

    public LabeledCorpus(String trainCorpus, String testCorpus) throws IOException {
        this(new File(trainCorpus), new File(testCorpus));
    }

    public LabeledCorpus(File trainCorpus, File testCorpus) throws IOException {
        mTrainCorpus = trainCorpus;
        mTestCorpus = testCorpus;
        mCats = getCatogories();
    }

    @Override
    public void visitTrain(ObjectHandler<Classified<CharSequence>> handler)
            throws IOException {
        for (String cat : mCats) {
            File catDir = new File(mTrainCorpus, cat);
            if (!catDir.isDirectory()) {
                System.err.println("Warning: ignore " + catDir.getAbsolutePath()
                        + " bacause it is not a directory");
                continue;
            }

            String[] trainingFiles = catDir.list();
            for (int j = 0; j < trainingFiles.length; ++j) {
                File file = new File(catDir, trainingFiles[j]);
                String text = Files.readFromFile(file, "utf-8");
                Classification classification = new Classification(cat);
                Classified<CharSequence> classified = new Classified<CharSequence>(
                        text, classification);
                handler.handle(classified);
            }
        }
    }

    @Override
    public void visitTest(ObjectHandler<Classified<CharSequence>> handler)
            throws IOException {
         for (String cat : mCats) {
             File catDir = new File(mTestCorpus, cat);
             if (!catDir.isDirectory()) {
                 System.err.println("Warning: ignore " + catDir.getAbsolutePath()
                         + " bacause it is not a directory");
                 continue;
             }

             for (File file : catDir.listFiles()) {
                 String text = Files.readFromFile(file, "utf-8");
                 Classification classification = new Classification(cat);
                 Classified<CharSequence> classified = new Classified<CharSequence>(
                         text, classification);
                 handler.handle(classified);
             }
         }
    }

    public String[] getCatogories() throws IOException {
        if (mCats == null) {
            if (mTrainCorpus.isDirectory()) {
                File trainDir = new File(mTrainCorpus.getAbsolutePath());
                if (trainDir.isDirectory()) {
                    return trainDir.list();
                }
            } else {
                throw new IllegalArgumentException();
            }
        }

        return mCats;
    }

}
