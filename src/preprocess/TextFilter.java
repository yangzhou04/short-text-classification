package preprocess;

public class TextFilter {

    public static final String PARENTHESIS = "\\(.*?\\)";
    public static final String DIGIT = "\\d";
    public static final String SPACE = " *";
    public static final String EQUATION_TAG = "<.*?>";
    
    public static void main(String[] args) {
        String src = "本点火装置包括壳体(1)，它带有空气和气体燃料输送短管(2，3)，引爆管(4)，引爆管的入口与燃料空气混合物输送短管(7)相连通，接近此入口的(12)部分位于壳体(1)之外，其上装有点火的电喷嘴(13)，装置在壳体(1)的工作端(16)一侧有火苗形成装置(15)和火苗信号指示器(17)。火苗形成装置(15)作为燃料空气";
        System.out.println(src);
        System.out.println(src.replaceAll(PARENTHESIS, ""));
        
        String src2 = "差动式常闭制动器。固定座1上有制动块6，制动盘2和制动盘16由连接杆12连接，杆两端的制动弹簧分别作用于两制动盘，两制动盘之间有顶力机构，该机构中的驱动块与制动盘连接；制动盘2上有差动孔7，差动轴5位于该孔内，一端与主动传动件11连接，另一端与顶力机构的顶力块13连接。利用主动传动件带动差动轴";
        System.out.println(src2);
        System.out.println(src2.replaceAll(DIGIT, ""));
        
        String src3 = "一种阻环型同步器(18)包括锥形离合器的摩擦表面(24，48和26、50)和爪式离合器的齿(36b，28和36c，30)，它们摩擦式地同步运动并且使齿轮(14，16)与轴(12)进行确定地结合。多个刚性构件(72)可被插入到阻环(40，42)的止动件齿(44，46)和自助力斜面(70a-70d)之间，其中自助力斜面位于装于轴(12)上的轮毂(32)的外圆周上。换挡套筒(34)与轮毂(32)成可滑动的花键连接，并且换挡套筒由操作者换挡力(                                    <span class=\"highlight\">                                      F";
        System.out.println(src3);
        System.out.println(src3.replaceAll(SPACE, "").replaceAll(EQUATION_TAG, ""));
    }

}
