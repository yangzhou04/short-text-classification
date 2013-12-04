package corpus;

import java.io.File;
import java.io.IOException;

import com.aliasi.classify.Classification;
import com.aliasi.classify.Classified;
import com.aliasi.corpus.Corpus;
import com.aliasi.corpus.ObjectHandler;
import com.aliasi.util.Files;

public class TrainLabeledCorpus extends
        Corpus<ObjectHandler<Classified<CharSequence>>> {

    private final File mCorpusFile;

    private String[] mCats;

    public TrainLabeledCorpus(String corpusPath) throws IOException {
        this(new File(corpusPath));
    }

    public TrainLabeledCorpus(File corpusFile) throws IOException {
        mCorpusFile = corpusFile;
        mCats = getCatogories();
    }

    @Override
    public void visitTrain(ObjectHandler<Classified<CharSequence>> handler)
            throws IOException {
        for (int i = 0; i < mCats.length; i++) {
            File classDir = new File(mCorpusFile, mCats[i]);
            if (!classDir.isDirectory())
                throw new IllegalArgumentException(
                        "Could not find training directory=" + classDir);

            String[] trainingFiles = classDir.list();
            for (int j = 0; j < trainingFiles.length; ++j) {
                File file = new File(classDir, trainingFiles[j]);
                String text = Files.readFromFile(file, "utf-8");
                Classification classification = new Classification(mCats[i]);
                Classified<CharSequence> classified = new Classified<CharSequence>(
                        text, classification);
                handler.handle(classified);
            }
        }
    }

    @Override
    public void visitTest(ObjectHandler<Classified<CharSequence>> handler)
            throws IOException {

        throw new UnsupportedOperationException();
        // for (int i = 0; i < mCats.length; i++) {
        // File classDir = new File(mTestDir, mCats[i]);
        // String[] testFiles = classDir.list();
        // for (int j = 0; j < testFiles.length; j++) {
        // String text = Files.readFromFile(new File(classDir,
        // testFiles[j]), "utf-8");
        // // System.out.print("Testing on " + mCats[i] + "/"
        // // +testFiles[j]+ " ");
        //
        // StringTokenizer st = new StringTokenizer(text, "\n");
        // Classification classification = new Classification(mCats[i]);
        // while (st.hasMoreTokens()) {
        // String feat = st.nextToken();
        // Classified<CharSequence> classified = new Classified<CharSequence>(
        // feat, classification);
        // handler.handle(classified);
        // }
        // }
        // }

    }

    public String[] getCatogories() throws IOException {
        if (mCats == null) {
            if (mCorpusFile.isDirectory()) {
                File trainDir = new File(mCorpusFile.getAbsolutePath());
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
