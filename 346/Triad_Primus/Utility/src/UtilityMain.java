import jdk.nashorn.internal.objects.annotations.Where;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Utilityを動かしてみる奴
 *
 * Created by nakamura on 2017/11/02.
 */
public class UtilityMain {
    public static void main(String[] args) {
        System.out.println(Utility.first(IntStream.range(0,10).boxed()));
        System.out.println(Utility.last(IntStream.range(0,100).boxed()));


        List<String> a  = Arrays.asList("1","2","3");
        List<String> b  = Arrays.asList("2","2","1");

        System.out.println(Utility.divide(a,2));
        System.out.println(Utility.divide(b,1));


        // こういうことができる
        Utility.zip(a.stream(),b.stream(),(a1, b1) -> a1.equals(b1))
                .forEach(e -> System.out.println(e));

    }
}
