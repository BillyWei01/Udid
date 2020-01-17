package horizon.util;

public class StringUtil {
    public static String removeChar(String src, char ch){
        if(src == null || src.isEmpty()){
            return src;
        }
        if(src.indexOf(ch) < 0){
            return src;
        }

        char[] a  = src.toCharArray();
        int len = a.length;
        int p = 0;
        for (int i = 0; i < len; i++) {
            if(a[i] != ch){
                a[p++] = a[i];
            }
        }
        return new String(a, 0, p);
    }
}
