package corpus;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.aliasi.corpus.Corpus;
import com.aliasi.corpus.ObjectHandler;

public class UnlabeledCorpus extends
        Corpus<ObjectHandler<CharSequence>> {

    private List<String> unlabeledCorpusList;

    public UnlabeledCorpus(String filename) throws IOException {
        File unlabeledCorpusFile = new File(filename);
        if (!unlabeledCorpusFile.exists())
            throw new FileNotFoundException();

        unlabeledCorpusList = new ArrayList<String>();
        BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(unlabeledCorpusFile), "UTF-8"));
        while (br.ready())
            unlabeledCorpusList.add(br.readLine());
        br.close();
    }

    public void visitTrain(ObjectHandler<CharSequence> handler)
            throws IOException {
        for (int i = 0; i < unlabeledCorpusList.size(); ++i)
            handler.handle(unlabeledCorpusList.get(i));
    }

    public void visitTest(ObjectHandler<CharSequence> handler)
            throws IOException {
        throw new UnsupportedOperationException();
    }

}
