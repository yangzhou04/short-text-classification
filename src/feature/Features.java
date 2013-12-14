package feature;

import java.util.List;

public class Features {

    public static List<String> chooseTop(List<String> src, double d) {
        return src.subList(0, (int)(src.size() * d));
    }
    
    public static List<String> chooseTopK(List<String> src, int k) {
        return src.subList(0,  k);
    }

}
