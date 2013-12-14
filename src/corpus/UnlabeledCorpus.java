package corpus;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.aliasi.corpus.Corpus;
import com.aliasi.corpus.ObjectHandler;
import com.aliasi.util.Files;

public class UnlabeledCorpus extends
        Corpus<ObjectHandler<CharSequence>> {

    private List<String> mUnlabeledCorpus;

    public UnlabeledCorpus(String unlabeledCorpus) throws IOException {
        File unlabeledCorpusFile = new File(unlabeledCorpus);
        if (!unlabeledCorpusFile.isDirectory())
            throw new IllegalArgumentException();
        
        mUnlabeledCorpus = new ArrayList<String>();
        for(File file : unlabeledCorpusFile.listFiles()) {
            String text = Files.readFromFile(file, "utf-8");
            mUnlabeledCorpus.add(text);
        }
    }

    public void visitTrain(ObjectHandler<CharSequence> handler)
            throws IOException {
        for (int i = 0; i < mUnlabeledCorpus.size(); ++i)
            handler.handle(mUnlabeledCorpus.get(i));
    }

    public void visitTest(ObjectHandler<CharSequence> handler)
            throws IOException {
        throw new UnsupportedOperationException();
    }

}
