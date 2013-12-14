package preprocess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class TextFilter {

    public static final String PARENTHESIS = "(\\(|\\[|\\（).*?(\\)|\\]|\\）)";
    public static final String DIGIT = "\\d";
    public static final String ALPHA = "[a-zA-Z]";
    public static final String SPACE = " *";
    public static final String EQUATION_TAG = "<.*?>";

    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(
                new FileInputStream("./exper/ulabeled.csv"), "UTF-8"));
        File f = new File("./exper/ulabeled.clean.csv");
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(f), "UTF-8"));
        while (in.ready()) {
            String text = in.readLine();
            text = text.replaceAll(TextFilter.PARENTHESIS, "")
                .replaceAll(TextFilter.SPACE, "")
                .replaceAll(TextFilter.EQUATION_TAG, "");
            out.append(text);
            out.newLine();
        }
        out.close();
        in.close();
    }

}
