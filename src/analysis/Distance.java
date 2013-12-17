package analysis;

public class Distance {

    public static double KullbackLeibler(double[] p, double[] q) {
        if (p.length != q.length)
            throw new IllegalArgumentException();
        
        double dist = 0;
        for (int x = 0; x < p.length; x++) {
            if (p[x] < 0 || q[x] < 0)
                throw new IllegalArgumentException();
            
            if (p[x] > 0 && q[x] > 0)
                dist += p[x] * Math.log(p[x]/q[x]);
            else if (q[x] == 0)
                return Double.POSITIVE_INFINITY;
        }
        return dist;
    }

}
