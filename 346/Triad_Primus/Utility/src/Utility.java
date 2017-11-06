import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Java8のUtility 最終兵器
 * メソッドチェーンにしたい
 * Created by nakamura on 2017/11/02.
 */
public final class Utility {

    /**
     * Listを好きな数で分割
     * 余りは切り捨て(変更したかったら変えて)
     *
     * @param data
     * @param value
     * @param <T>
     * @return
     */
    public static <T> List<List<T>> divide(List<T> data, int value) {
        // 0から　元のリストの数を分割数で割った数を四捨五入(切り上げ) - 1 のintStreamを作る
        return IntStream.range(0, (int) Math.ceil((double) data.size() / (double) value))
                // プリミティブStreamからStreamへの変換
                .boxed()
                // IntStreamの数だけ回して　SkipとTakeですっ飛ばしたListにする
                .map(i -> data.stream().skip(i * value)
                        .limit(value)
                        // 新しいListを生み出すよ
                        .collect(Collectors.toList()))
                        // でもサイズがvalue未満なら切り捨てようかな？
                        .filter(ts -> ts.size() == value)
                        // でもサイズがvalue未満のデータって何よ？
                        // .filter(ts -> ts.size() < value)
                // 新しいListを生み出すよ
                .collect(Collectors.toList());
    }

    /**
     * Stream<T>の最初の要素を返す
     * Javaの奴はたいていそうだけど.findFirst()からgetしないといけないのがアレだった
     *
     * @param stream
     * @param <T>
     * @return
     */
    public static <T> T first(Stream<T> stream) {
        return stream.findFirst().get();
    }

    /**
     * Stream<T>の最後の要素を返す
     * stream.findLast()すらないのでreduceでゴリゴリ
     *
     * @param stream
     * @param <T>
     * @return
     */
    public static <T> T last(Stream<T> stream) {
        return stream.reduce((first, second) -> second).get();
    }

//    /**
//     * Java8に導入予定だったZIPのソースコード
//     *
//     * @param a
//     * @param b
//     * @param zipper
//     * @param <A>
//     * @param <B>
//     * @param <C>
//     * @return
//     */
//    public static<A, B, C> Stream<C> zip(Stream<? extends A> a,
//                                         Stream<? extends B> b,
//                                         BiFunction<? super A, ? super B, ? extends C> zipper) {
//        Objects.requireNonNull(zipper);
//        Spliterator<? extends A> aSpliterator = Objects.requireNonNull(a).spliterator();
//        Spliterator<? extends B> bSpliterator = Objects.requireNonNull(b).spliterator();
//
//        // Zipping looses DISTINCT and SORTED characteristics
//        int characteristics = aSpliterator.characteristics() & bSpliterator.characteristics() &
//                ~(Spliterator.DISTINCT | Spliterator.SORTED);
//
//        long zipSize = ((characteristics & Spliterator.SIZED) != 0)
//                ? Math.min(aSpliterator.getExactSizeIfKnown(), bSpliterator.getExactSizeIfKnown())
//                : -1;
//
//        Iterator<A> aIterator = Spliterators.iterator(aSpliterator);
//        Iterator<B> bIterator = Spliterators.iterator(bSpliterator);
//        Iterator<C> cIterator = new Iterator<C>() {
//            @Override
//            public boolean hasNext() {
//                return aIterator.hasNext() && bIterator.hasNext();
//            }
//
//            @Override
//            public C next() {
//                return zipper.apply(aIterator.next(), bIterator.next());
//            }
//        };
//
//        Spliterator<C> split = Spliterators.spliterator(cIterator, zipSize, characteristics);
//        return (a.isParallel() || b.isParallel())
//                ? StreamSupport.stream(split, true)
//                : StreamSupport.stream(split, false);
//    }

    /**
     * 上のZIPを簡単にした
     * first と second から交互に要素を取り出し、
     * どちらかのストリームの要素がなくなったら停止する
     *
     * @param first
     * @param second
     * @param zipper
     * @param <A>
     * @param <B>
     * @param <C>
     * @return
     */
    public static <A, B, C> Stream<C> zip(Stream<A> first,
                                          Stream<B> second,
                                          // A,Bの2つの引数を受け取って結果のCをを生成する関数を表します
                                          BiFunction<A, B, C> zipper) {
        // ストリームをイテレータに変換
        final Iterator<A> left = first.iterator();
        final Iterator<B> right = second.iterator();

        // 2つのiteratorから1つのiteratorを生成
        final Iterator<C> iterator = new Iterator<C>() {
            /**
             * 次の要素が両方にある場合はTrue
             *
             * @return
             */
            public boolean hasNext() {
                return left.hasNext() && right.hasNext();
            }

            /**
             * 次の要素
             *
             * @return
             */
            public C next() {
                // 引数の関数に次の値を投げる
                return zipper.apply(left.next(), right.next());
            }
        };
        // 片方でも並列実行される場合はTrue
        final boolean parallel = first.isParallel() || second.isParallel();
        // 下に投げた結果を返す
        return iteratorToFiniteStream(iterator, parallel);
    }

    /**
     * SpliteratorからStreamを生成、Streamを返す
     *
     * @param iterator
     * @param parallel
     * @param <T>
     * @return
     */
    public static <T> Stream<T> iteratorToFiniteStream(Iterator<T> iterator, boolean parallel) {
        final Iterable<T> iterable = () -> iterator;
        // iterableをSpliteratorにSpliteratorからStreamを生成
        return StreamSupport.stream(iterable.spliterator(), parallel);
    }

}
