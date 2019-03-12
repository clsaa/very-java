# Spliterator

Spliterator是一个可分割迭代器(splitable iterator)，可以和iterator顺序遍历迭代器一起看。jdk1.8发布后，对于并行处理的能力大大增强，Spliterator就是为了并行遍历元素而设计的一个迭代器，jdk1.8中的集合框架中的数据结构都默认实现了spliterator，后面我们也会结合ArrayList中的spliterator()一起解析。

```java
package java.util;
 
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;
 
/**
 * Spliterator是一个可分割迭代器(splitable iterator)，可以和iterator顺序遍历迭代器一起看。
 * jdk1.8发布后，对于并行处理的能力大大增强，Spliterator就是为了并行遍历元素而设计的一个迭代器，
 * jdk1.8中的集合框架中的数据结构都默认实现了spliterator，后面我们也会结合ArrayList中的spliterator()一起解析。
 * 
 * @see Collection
 * @since 1.8
 */
public interface Spliterator<T> {
	
    /**
     *对单个元素执行给定的动作，并且返回true，否则返回false
     * @throws NullPointerException 如果具体操作为空
     */
    boolean tryAdvance(Consumer<? super T> action);
 
    /**
     * 对当前线程中的每个剩余元素执行给定操作，直到处理完所有元素或操作抛出异常
     * @throws NullPointerException 如果具体操作为空
     */
    default void forEachRemaining(Consumer<? super T> action) {
        do { } while (tryAdvance(action));
    }
 
    /**
     * 如果这个分割器可以被分割，返回一个新的Spliterator，如果不能分割，则返回null
     *（分割后，被分割的迭代器只有原来的一半（一般都是一半），新的迭代器为另一半）
     * 除非这个Spliterator覆盖无数的元素，否则重复调用trySplit()最终必须返回null。
     * 非空返回时：
     *      1.旧的迭代器的estimateSize()返回的值，一定大于等于，
     *        被分割而产生的两个迭代器的estimateSize()之和; 
     *      2.如果这个Spliterator是SUBSIZED，那么旧的迭代器的estimateSize()返回的值，
     *        一定等于，被分割而产生的两个迭代器的estimateSize()之和
     */
    Spliterator<T> trySplit();
 
    /**
     * 返回forEachRemaining(java.util.function.Consumer<? super T>)遍历可能遇到的
     * 元素数量的估计值，如果是无限的，未知的或者计算成本太高的，就返回Long.MAX_VALUE
     */
    long estimateSize();
 
    /**
     * 当迭代器拥有SIZED特征时，返回estimateSize()的值；否则返回-1
     */
    default long getExactSizeIfKnown() {
        return (characteristics() & SIZED) == 0 ? -1L : estimateSize();
    }
 
    /**
     * 返回此Spliterator及其元素的一组特征，具体的实现类通过这个方法设置自己实现的
     * Spliterator的特征值，多个特征值之间用|隔开
     */
    int characteristics();
 
    /**
     * 判断该Spliterator是否包含指定的特征值
     */
    default boolean hasCharacteristics(int characteristics) {
        return (characteristics() & characteristics) == characteristics;
    }
 
    /**
     *  如果Spliterator的list是通过Comparator排序的，则返回Comparator
     *  如果Spliterator的list是自然排序的 ，则返回null
     *  其他情况下抛错
     */
    default Comparator<? super T> getComparator() {
        throw new IllegalStateException();
    }
 
    /**
     * 特征值表示该Spliterator有哪些特性，可以更好控制和优化Spliterator的使用。
     * 比如ArrayList的ArrayListSpliterator就通过characteristics()方法表示
     * 了自己的三个特征值：ORDERED，SIZED，SUBSIZED。的确，ArrayList是有序的，
     * 并且是大小确定的，符合这三个特征值。
     */
    public static final int ORDERED    = 0x00000010;  //表示元素是有序的
 
    public static final int DISTINCT   = 0x00000001;  //表示任意元素都不相同
 
    public static final int SORTED     = 0x00000004;  //表示遇到的顺序遵循一个定义好的排序。（一个Spliterator 具有SORTED特征值时，肯定具有ORDERED特征值）。这个特征值表示getComparator()必定能获得一个比较器。顺序是通过这个Comparator来排序的
 
    public static final int SIZED      = 0x00000040;  //表示在遍历或分割之前， estimateSize()的返回值表示一个有限的大小，在没有结构源修改的情况下，它代表了一个完整遍历所遇到的元素数量的精确计数。
 
    public static final int NONNULL    = 0x00000100;  //表示任何元素都不为null
 
    public static final int IMMUTABLE  = 0x00000400;  //表示元素源不能被结构修改;也就是说，元素不能被添加、替换或移除，因此在遍历过程中不会发生这样的更改。
 
    public static final int CONCURRENT = 0x00001000;  //表示可以通过多个线程安全同时修改元素源（允许添加，替换和/或删除），而无需外部同步。
 
    public static final int SUBSIZED = 0x00004000;    //表示被trySplit()方法分割获得的spliterator，都将会有SIZED或者SUBSIZED特征
 
    /**
     * 可以看到，这个接口基本没有变动，这是多增加两个泛型声明而已，本质上和Spliterator没有太大的区别，
     * 只不过，它限制tryAdvance的参数action类型T_CONS和trySplit的返回参数T_SPLITR必须在实现接口时先声明类型。 
     * 基于OfPrimitive接口，又衍生出了OfInt、OfLong、OfDouble等专门用来处理int、Long、double等分割迭代器接口
     * （在Spliterators有具体的实现）。
     * @see Spliterator.OfInt
     * @see Spliterator.OfLong
     * @see Spliterator.OfDouble
     * @since 1.8
     */
    public interface OfPrimitive<T, T_CONS, T_SPLITR extends Spliterator.OfPrimitive<T, T_CONS, T_SPLITR>>
            extends Spliterator<T> {
        @Override
        T_SPLITR trySplit();
 
       
        @SuppressWarnings("overloads")
        boolean tryAdvance(T_CONS action);
 
        
        @SuppressWarnings("overloads")
        default void forEachRemaining(T_CONS action) {
            do { } while (tryAdvance(action));
        }
    }
 
    //专门用来处理int分割迭代器接口
    public interface OfInt extends OfPrimitive<Integer, IntConsumer, OfInt> {
 
        @Override
        OfInt trySplit();
 
        @Override
        boolean tryAdvance(IntConsumer action);
 
        @Override
        default void forEachRemaining(IntConsumer action) {
            do { } while (tryAdvance(action));
        }
 
       
        @Override
        default boolean tryAdvance(Consumer<? super Integer> action) {
            if (action instanceof IntConsumer) {
                return tryAdvance((IntConsumer) action);
            }
            else {
                if (Tripwire.ENABLED)
                    Tripwire.trip(getClass(),
                                  "{0} calling Spliterator.OfInt.tryAdvance((IntConsumer) action::accept)");
                return tryAdvance((IntConsumer) action::accept);
            }
        }
 
        
        @Override
        default void forEachRemaining(Consumer<? super Integer> action) {
            if (action instanceof IntConsumer) {
                forEachRemaining((IntConsumer) action);
            }
            else {
                if (Tripwire.ENABLED)
                    Tripwire.trip(getClass(),
                                  "{0} calling Spliterator.OfInt.forEachRemaining((IntConsumer) action::accept)");
                forEachRemaining((IntConsumer) action::accept);
            }
        }
    }
 
    
    //专门用来处理Long分割迭代器接口
    public interface OfLong extends OfPrimitive<Long, LongConsumer, OfLong> {
 
        @Override
        OfLong trySplit();
 
        @Override
        boolean tryAdvance(LongConsumer action);
 
        @Override
        default void forEachRemaining(LongConsumer action) {
            do { } while (tryAdvance(action));
        }
 
        
        @Override
        default boolean tryAdvance(Consumer<? super Long> action) {
            if (action instanceof LongConsumer) {
                return tryAdvance((LongConsumer) action);
            }
            else {
                if (Tripwire.ENABLED)
                    Tripwire.trip(getClass(),
                                  "{0} calling Spliterator.OfLong.tryAdvance((LongConsumer) action::accept)");
                return tryAdvance((LongConsumer) action::accept);
            }
        }
 
        
        @Override
        default void forEachRemaining(Consumer<? super Long> action) {
            if (action instanceof LongConsumer) {
                forEachRemaining((LongConsumer) action);
            }
            else {
                if (Tripwire.ENABLED)
                    Tripwire.trip(getClass(),
                                  "{0} calling Spliterator.OfLong.forEachRemaining((LongConsumer) action::accept)");
                forEachRemaining((LongConsumer) action::accept);
            }
        }
    }
 
    //专门用来处理double分割迭代器接口
    public interface OfDouble extends OfPrimitive<Double, DoubleConsumer, OfDouble> {
 
        @Override
        OfDouble trySplit();
 
        @Override
        boolean tryAdvance(DoubleConsumer action);
 
        @Override
        default void forEachRemaining(DoubleConsumer action) {
            do { } while (tryAdvance(action));
        }
 
       
        @Override
        default boolean tryAdvance(Consumer<? super Double> action) {
            if (action instanceof DoubleConsumer) {
                return tryAdvance((DoubleConsumer) action);
            }
            else {
                if (Tripwire.ENABLED)
                    Tripwire.trip(getClass(),
                                  "{0} calling Spliterator.OfDouble.tryAdvance((DoubleConsumer) action::accept)");
                return tryAdvance((DoubleConsumer) action::accept);
            }
        }
 
       
        @Override
        default void forEachRemaining(Consumer<? super Double> action) {
            if (action instanceof DoubleConsumer) {
                forEachRemaining((DoubleConsumer) action);
            }
            else {
                if (Tripwire.ENABLED)
                    Tripwire.trip(getClass(),
                                  "{0} calling Spliterator.OfDouble.forEachRemaining((DoubleConsumer) action::accept)");
                forEachRemaining((DoubleConsumer) action::accept);
            }
        }
    }
}

```