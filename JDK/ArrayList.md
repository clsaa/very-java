# ArrayList

## 1.总结

![image](https://clsaa-markdown-imgbed-1252032169.cos.ap-shanghai.myqcloud.com/very-java/2019-03-12-104944.png)

1. ArrayList随机元素时间复杂度O(1)，插入删除操作需大量移动元素，效率较低
2. 为了节约内存，当新建容器为空时，会共享Object[] EMPTY_ELEMENTDATA = {}和 Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {}空数组
3. 容器底层采用数组存储，每次扩容为1.5倍
4. ArrayList的实现中大量地调用了Arrays.copyof()和System.arraycopy()方法，其实Arrays.copyof()内部也是调用System.arraycopy()。System.arraycopy()为Native方法
5. 两个ToArray方法
   1. Object[] toArray()方法。该方法有可能会抛出java.lang.ClassCastException异常
   2. <T> T[] toArray(T[] a)方法。该方法可以直接将ArrayList转换得到的Array进行整体向下转型
6. ArrayList可以存储null值
7. ArrayList每次修改（增加、删除）容器时，都是修改自身的modCount；在生成迭代器时，迭代器会保存该modCount值，迭代器每次获取元素时，会比较自身的modCount与ArrayList的modCount是否相等，来判断容器是否已经被修改，如果被修改了则抛出异常（fast-fail机制）。

## 2.变量

### 2.1.静态变量

```java
  /**
    * Default initial capacity. 用于ArrayList空实例的共享空数组实例
    */
  private static final int DEFAULT_CAPACITY = 10;

  /**
    * Shared empty array instance used for empty instances.
    */
  private static final Object[] EMPTY_ELEMENTDATA = {};

  /**
    * Shared empty array instance used for default sized empty instances. We
    * distinguish this from EMPTY_ELEMENTDATA to know how much to inflate when
    * first element is added.用于默认大小空实例的共享空数组实例。我们将和EMPTY_ELEMENTDATA区别开来，以便在添加第一个元素时知道要膨胀多少。在一个个元素加入进来的时候，会自动扩展成为DEFAULTCAPACITY
    */
  private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};
```

两个类常量EE和DEE都是表示空数组，只是名字不一样而已。其中无参构造器创建的实例al的elementData是DEE，有参构造函数创建的空实例al1和al2的elementData是EE。即：

```java
// elementData = DEE
ArrayList<String> al = new ArrayList<String>();

// elementData = EE
ArrayList<String> al1 = new ArrayList<String>(0);
ArrarList<String> al2 = new ArrayList<String>(al1)
```

其他add方法如：add(int index, E element)、addAll(Collection<? extends E> c)、addAll(int index, Collection<? extends E> c)中都有ensureCapacityInternal(int minCapacity)方法，确保无参构成函数创建的实例al在添加第一个元素时，最小的容量是默认大小10。那有参构造函数创建的空实例al1、al2在通过add(E e)添加元素的时候是怎么样的呢？al1、al2容量增长是这样子的：0->1->2->3->4->6->9->13...，这样的增长是很慢的。

### 2.2.实例变量

```java
    /**
     * Default initial capacity.
     */
    private static final int DEFAULT_CAPACITY = 10;

    /**
     * Shared empty array instance used for empty instances.
     */
    private static final Object[] EMPTY_ELEMENTDATA = {};

    /**
     * Shared empty array instance used for default sized empty instances. We
     * distinguish this from EMPTY_ELEMENTDATA to know how much to inflate when
     * first element is added.
     */
    private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};
    /**
     * The number of times this list has been <i>structurally modified</i>.
     * Structural modifications are those that change the size of the
     * list, or otherwise perturb it in such a fashion that iterations in
     * progress may yield incorrect results.
     */
    protected transient int modCount = 0;
```

## 3.函数

### 3.1.构造函数

#### 3.1.1.Java8

```java

    //带有初始化容量的构造函数
    public ArrayList(int initialCapacity) {
        if (initialCapacity > 0) {
            //初始化一个initialCapacity大小的Object数组
            this.elementData = new Object[initialCapacity];
        } else if (initialCapacity == 0) {
            //若初始化为0,则使用默认的空数组赋值
            this.elementData = EMPTY_ELEMENTDATA;
        } else {
            //若为负数，抛出非法参数异常
            throw new IllegalArgumentException("Illegal Capacity: "+
                                               initialCapacity);
        }
    }

    //最基本的构造函数
    public ArrayList() {
        //注意这里被赋值为DEFAULTCAPACITY_EMPTY_ELEMENTDATA
        //而不是EMPTY_ELEMENTDATA，表示的意思就是当add的时候
        //会默认扩容为DEFAULT_CAPACITY
        this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
    }

    public ArrayList(Collection<? extends E> c) {
        elementData = c.toArray();
        if ((size = elementData.length) != 0) {
            // c.toArray might (incorrectly) not return Object[] (see 6260652)
            if (elementData.getClass() != Object[].class)
                elementData = Arrays.copyOf(elementData, size, Object[].class);
        } else {
            // replace with empty array.
            this.elementData = EMPTY_ELEMENTDATA;
        }
    }
```

#### 3.1.2.Java7

```java
public ArrayList(int initialCapacity) {
   super();
   if (initialCapacity < 0)
       throw new IllegalArgumentException("Illegal Capacity: "+
                                          initialCapacity);
   this.elementData = new Object[initialCapacity];
}

public ArrayList() {
   super();
   this.elementData = EMPTY_ELEMENTDATA;
}

public ArrayList(Collection<? extends E> c) {
   elementData = c.toArray();
   size = elementData.length;
   // c.toArray might (incorrectly) not return Object[] (see 6260652)
   if (elementData.getClass() != Object[].class)
       elementData = Arrays.copyOf(elementData, size, Object[].class);
}
```

### 3.2.add函数

```java
public boolean add(E e) {
    //添加元素的时候，调用内部确保容量的方法
    //为什么是内部呢？因为还有一个共有的ensureCapacity方法
    ensureCapacityInternal(size + 1);  // Increments modCount!!
    //因为本身是一个数组，所以在下一个数组索引位置添加上元素
    elementData[size++] = e;
    return true;
}

//扩容
private void ensureCapacityInternal(int minCapacity) {
    ensureExplicitCapacity(calculateCapacity(elementData, minCapacity));
}

//计算容量
private static int calculateCapacity(Object[] elementData, int minCapacity) {
    // 当第一次调用add(E e)方法的时候，判读是不是无参构造函数创建的对象，如果是，
    // 将DEFAULT_CAPACITY即10作为ArrayList的容量，此时minCapacity = 1
    if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
        //取DEFAULT_CAPACITY=10和minCapacity(可以在初始化函数中初始化)的最大值
        //也就是说若调用默认构造函数，第一次会起码扩展为10的大小
        return Math.max(DEFAULT_CAPACITY, minCapacity);
    }
    return minCapacity;
}


private void ensureExplicitCapacity(int minCapacity) {
    //用于迭代器的fail fast机制, 此变量由抽象类继承获得
    modCount++;

    // overflow-conscious code
    //如果最小容量大于数组的长度,那么扩容。
    //否则，则不扩容。
    if (minCapacity - elementData.length > 0)
        grow(minCapacity);
}


private void grow(int minCapacity) {
    // overflow-conscious code
    int oldCapacity = elementData.length;
    //新的容量为旧的容量的1.5倍
    int newCapacity = oldCapacity + (oldCapacity >> 1);
    //如果扩了1.5倍之后,还是小，那么以最小的容量进行扩容
    if (newCapacity - minCapacity < 0)
        newCapacity = minCapacity;
    if (newCapacity - MAX_ARRAY_SIZE > 0)
        newCapacity = hugeCapacity(minCapacity);
    // minCapacity is usually close to size, so this is a win:
    //将elementData扩展为newCapacity大小，使用复制数组的方式
    elementData = Arrays.copyOf(elementData, newCapacity);
}

private static int hugeCapacity(int minCapacity) {
    //如果minCapacity小于0(而minCapacity是由某数+1得到)
    //其实也刚开始想错了,minCapacity也有可能是addAll()调用导致的
    //反正minCapacity是一个需要保证的最小的容量值，不需要去理解
    //其他函数是如何调用的
    //所以minCapacity是由于Integer溢出的
    if (minCapacity < 0) // overflow
        throw new OutOfMemoryError();
    //这里表示将最大容量扩展为Integer.MAX_VALUE,那么MAX_ARRAY_SIZE还有什么意义呢?
    //这里minCapacity没有溢出说明是小于MAX_ARRAY_SIZE，但是分为两种情况，如果小于MAX_ARRAY_SIZE
    //那么就直接扩容为MAX_ARRAY_SIZE
    //否则比如说是MAX_VALUE-2,那么最多扩容为MAX_VALUE
    return (minCapacity > MAX_ARRAY_SIZE) ?
        Integer.MAX_VALUE :
        MAX_ARRAY_SIZE;
}
```

### 3.3.get操作

```java
/**
  * Returns the element at the specified position in this list.
  *
  * @param  index index of the element to return
  * @return the element at the specified position in this list
  * @throws IndexOutOfBoundsException {@inheritDoc}
  */
public E get(int index) {
    //判断是否越界
    rangeCheck(index);
    //返回数组的值
    return elementData(index);
}


/**
  * Checks if the given index is in range.  If not, throws an appropriate
  * runtime exception.  This method does *not* check if the index is
  * negative: It is always used immediately prior to an array access,
  * which throws an ArrayIndexOutOfBoundsException if index is negative.
  */
private void rangeCheck(int index) {
    if (index >= size)
        //如果下标大于元素的数量，那么抛出异常
        throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
}
```

### 3.4.remove操作

```java
/**
  * Removes the element at the specified position in this list.
  * Shifts any subsequent elements to the left (subtracts one from their
  * indices).
  *
  * @param index the index of the element to be removed
  * @return the element that was removed from the list
  * @throws IndexOutOfBoundsException {@inheritDoc}
  */
public E remove(int index) {
    rangeCheck(index);
    //add和remove都需要让modCount加一
    modCount++;
    E oldValue = elementData(index);

    int numMoved = size - index - 1;
    if (numMoved > 0)
        System.arraycopy(elementData, index+1, elementData, index,
                          numMoved);
    //最大位的引用置为null,让GC回收
    elementData[--size] = null; // clear to let GC do its work
    //返回删除的值
    return oldValue;
}
```

## 4.Java8的变化

### 4.1.新增接口

```java
public interface Collection<E> extends Iterable<E> {
    .....
    //1.8新增方法：提供了接口默认实现，返回分片迭代器
    @Override
    default Spliterator<E> spliterator() {
        return Spliterators.spliterator(this, 0);
    }
    //1.8新增方法：提供了接口默认实现，返回串行流对象
    default Stream<E> stream() {
        return StreamSupport.stream(spliterator(), false);
    }
    //1.8新增方法：提供了接口默认实现，返回并行流对象
    default Stream<E> parallelStream() {
        return StreamSupport.stream(spliterator(), true);
    }
    /**
     * Removes all of the elements of this collection that satisfy the given
     * predicate.  Errors or runtime exceptions thrown during iteration or by
     * the predicate are relayed to the caller.
     * 1.8新增方法：提供了接口默认实现，移除集合内所有匹配规则的元素，支持Lambda表达式
     */
    default boolean removeIf(Predicate<? super E> filter) {
        //空指针校验
        Objects.requireNonNull(filter);
        //注意：JDK官方推荐的遍历方式还是Iterator，虽然forEach是直接用for循环
        boolean removed = false;
        final Iterator<E> each = iterator();
        while (each.hasNext()) {
            if (filter.test(each.next())) {
                each.remove();//移除元素必须选用Iterator.remove()方法
                removed = true;//一旦有一个移除成功，就返回true
            }
        }
        //这里补充一下：由于一旦出现移除失败将抛出异常，因此返回false指的仅仅是没有匹配到任何元素而不是运行异常
        return removed;
    }
}
public interface Iterable<T>{
    .....
    //1.8新增方法：提供了接口默认实现，用于遍历集合
    default void forEach(Consumer<? super T> action) {
        Objects.requireNonNull(action);
        for (T t : this) {
            action.accept(t);
        }
    }
    //1.8新增方法：提供了接口默认实现，返回分片迭代器
    default Spliterator<T> spliterator() {
        return Spliterators.spliteratorUnknownSize(iterator(), 0);
    }
}
public interface List<E> extends Collection<E> {
    //1.8新增方法：提供了接口默认实现，用于对集合进行排序，主要是方便Lambda表达式
    default void sort(Comparator<? super E> c) {
        Object[] a = this.toArray();
        Arrays.sort(a, (Comparator) c);
        ListIterator<E> i = this.listIterator();
        for (Object e : a) {
            i.next();
            i.set((E) e);
        }
    }
    //1.8新增方法：提供了接口默认实现，支持批量删除，主要是方便Lambda表达式
    default void replaceAll(UnaryOperator<E> operator) {
        Objects.requireNonNull(operator);
        final ListIterator<E> li = this.listIterator();
        while (li.hasNext()) {
            li.set(operator.apply(li.next()));
        }
    }
    /**
      * 1.8新增方法：返回ListIterator实例对象 
      * 1.8专门为List提供了专门的ListIterator，相比于Iterator主要有以下增强：
      *     1.ListIterator新增hasPrevious()和previous()方法，从而可以实现逆向遍历
      *     2.ListIterator新增nextIndex()和previousIndex()方法，增强了其索引定位的能力
      *     3.ListIterator新增set()方法，可以实现对象的修改
      *     4.ListIterator新增add()方法，可以向List中添加对象
      */
    ListIterator<E> listIterator();
}
```


### 4.2.全局变量的变更

```java
/**
  * The array buffer into which the elements of the ArrayList are stored.
  * The capacity of the ArrayList is the length of this array buffer. 
  * Any empty ArrayList with elementData == EMPTY_ELEMENTDATA will be expanded to
  * DEFAULT_CAPACITY when the first element is added.
  * 数组缓存，跟1.7版本相比，主要有两个变化：
  *     1.去掉private属性，使用默认的friendly作用域，开放给同包类使用
  *     2.一个空数组的elementData将设置为EMPTY_ELEMENTDATA直到第一个元素新增时
  *       使用DEFAULT_CAPACITY（10）完成有容量的初始化 -- 优化：这里选择将内存分配后置，而从尽可能节省空间
  */
transient Object[] elementData; // non-private to simplify nested class access
/**
  * Shared empty array instance used for empty instances.
  * 当时用空构造时，给予数组(elementData)默认值
  */
private static final Object[] EMPTY_ELEMENTDATA = {};
```


### 4.3.构造器的变更

```java
/**
  * Constructs an empty list with an initial capacity of ten.
  * 1.8版的默认构造器，只会初始化一个空数组
  */
public ArrayList() {
    super();
    this.elementData = EMPTY_ELEMENTDATA;//初始化一个空数组
}
/**
  * Constructs an empty list with an initial capacity of ten.
  * 1.7版的默认构造器，会直接初始化一个10容量的数组
  */
public ArrayList() {
    this(10);
}
```

### 4.4.方法变更


* trimToSize方法变更

```java
/**
  * 1.8版的trimToSize，跟1.7版相比：
  *     可以明显的看到去掉了oldCapacity这一临时变量
  *     笔者认为这进一步强调了HashMap是非线程安全的，因此直接用length即可
  */
public void trimToSize() {
    modCount++;
    if (size < elementData.length) {
        elementData = Arrays.copyOf(elementData, size);
    }
}
/**
  * 1.7版的trimToSize
  */
public void trimToSize() {
    modCount++;
    int oldCapacity = elementData.length;
    if (size < oldCapacity) {
        elementData = Arrays.copyOf(elementData, size);
    }
}
```

### 4.5.新增方法

#### 4.5.1.forEach方法

```java
/**
  * Performs the given action for each element of the {@code Iterable}
  * until all elements have been processed or the action throws an
  * exception.  Unless otherwise specified by the implementing class,
  * actions are performed in the order of iteration (if an iteration order
  * is specified).  Exceptions thrown by the action are relayed to the
  * caller.
  * 1.8新增方法，重写Iterable接口的forEach方法
  * 提供对数组的遍历操作，由于支持Consumer因此在遍历时将执行传入的方法
  */
@Override
public void forEach(Consumer<? super E> action) {
    Objects.requireNonNull(action);
    final int expectedModCount = modCount;
    @SuppressWarnings("unchecked")
    final E[] elementData = (E[]) this.elementData;
    final int size = this.size;
    for (int i=0; modCount == expectedModCount && i < size; i++) {
        action.accept(elementData[i]);//执行传入的自定义方法
    }
    if (modCount != expectedModCount) {
        throw new ConcurrentModificationException();
    }
}
--------------
List<String> list = new ArrayList<>();
list.add("有村架纯");
list.add("桥本环奈");
list.add("斋藤飞鸟");
list.forEach(s -> System.out.print(s + "!!")); //输出：有村架纯!!桥本环奈!!斋藤飞鸟!!
```

#### 4.5.2.removeIf方法

```java
/**
  * Removes all of the elements of this collection that satisfy the given
  * predicate.  Errors or runtime exceptions thrown during iteration or by
  * the predicate are relayed to the caller.
  * 1.8新增方法，重写Collection接口的removeIf方法
  * 移除集合内所有复合匹配条件的元素，迭代时报错会抛出异常 或 把断言传递给调用者（即断言中断）
  * 该方法主要干了两件事情：
  *     1.根据匹配规则找到所有符合要求的元素
  *     2.移除元素并转移剩余元素位置
  * 补充：为了安全和快速，removeIf分成两步走，而不是直接找到就执行删除和转移操作，写法值得借鉴
  */
@Override
public boolean removeIf(Predicate<? super E> filter) {
    Objects.requireNonNull(filter);
    // figure out which elements are to be removed any exception thrown from
    // the filter predicate at this stage will leave the collection unmodified
    int removeCount = 0;
    //BitSet用于按位存储，这里用作存储待移除元素（即符合匹配规则的元素）
    //BitSet能够通过位图算法大幅减少数据占用存储空间和内存，尤其适合在海量数据方面，这里是个很明显的优化
    //有机会会在基础番中解析一下BitSet的奇妙之处
    final BitSet removeSet = new BitSet(size);
    final int expectedModCount = modCount;
    final int size = this.size;
    //每次循环都要判断modCount == expectedModCount！
    for (int i=0; modCount == expectedModCount && i < size; i++) {
        @SuppressWarnings("unchecked")
        final E element = (E) elementData[i];
        if (filter.test(element)) {
            removeSet.set(i);
            removeCount++;
        }
    }
    if (modCount != expectedModCount) {
        throw new ConcurrentModificationException();
    }
    // shift surviving elements left over the spaces left by removed elements
    // 当有元素被移除，需要对剩余元素进行位移
    final boolean anyToRemove = removeCount > 0;
    if (anyToRemove) {
        final int newSize = size - removeCount;
        for (int i=0, j=0; (i < size) && (j < newSize); i++, j++) {
            i = removeSet.nextClearBit(i);
            elementData[j] = elementData[i];
        }
        for (int k=newSize; k < size; k++) {
            elementData[k] = null;  // Let gc do its work
        }
        this.size = newSize;
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
        modCount++;
    }
    //正常情况下，一旦匹配到元素，应该删除成功，否则将抛出异常，当没有匹配到任何元素时，返回false
    return anyToRemove;
}
--------------
List<String> list = new ArrayList<>();
list.add("有村架纯");
list.add("桥本环奈");
list.add("斋藤飞鸟");
list.forEach(s -> System.out.print(s + "!!")); //输出：有村架纯!!桥本环奈!!斋藤飞鸟!!
System.out.println(list.removeIf(s -> s.startsWith("斋藤")));//输出：true
list.forEach(s -> System.out.print(s + ",")); //输出：有村架纯!!桥本环奈!!
--------------
//这里补充一点，使用Arrays.asList()生成的ArrayList是Arrays自己的私有静态内部类
//强行使用removeIf的话会抛出java.lang.UnsupportedOperationException的异常（因为它没实现这个方法）
```

#### 4.5.3.replaceAll方法

```java

/**
  * Replaces each element of this list with the result of applying the operator to that element.
  * Errors or runtime exceptions thrown by the operator are relayed to the caller.
  * 1.8新增方法，重写List接口的replaceAll方法
  * 提供支持一元操作的批量替换功能
  */
@Override
@SuppressWarnings("unchecked")
public void replaceAll(UnaryOperator<E> operator) {
    Objects.requireNonNull(operator);
    final int expectedModCount = modCount;
    final int size = this.size;
    for (int i=0; modCount == expectedModCount && i < size; i++) {
        elementData[i] = operator.apply((E) elementData[i]);
    }
    if (modCount != expectedModCount) {
        throw new ConcurrentModificationException();
    }
    modCount++;
}
--------------
List<String> list = new ArrayList<>();
list.add("有村架纯");
list.add("桥本环奈");
list.add("斋藤飞鸟");
list.forEach(s -> System.out.print(s + "!!")); //输出：有村架纯!!桥本环奈!!斋藤飞鸟!!
list.replaceAll(t -> {
    if(t.equals("桥本环奈")) t = "逢泽莉娜";//这里我们将"桥本环奈"替换成"逢泽莉娜"
    return t;//注意如果是语句块的话一定要返回
});
list.forEach(s -> System.out.print(s + "!!")); //输出：有村架纯!!逢泽莉娜!!斋藤飞鸟!!
```

#### 4.5.4.sort方法

与Iterator不同的是,Spliterator可以拆分成多份去遍历,有点像二分法,每次把某个Spliterator平均分成两份,但是改的只是下标.

```java
/**
  * Sorts this list according to the order induced by the specified
  * 1.8新增方法，重写List接口的sort方法
  * 支持对数组进行排序，主要方便于Lambda表达式
  */
@Override
@SuppressWarnings("unchecked")
public void sort(Comparator<? super E> c) {
    final int expectedModCount = modCount;
    //Arrays.sort底层是结合归并排序和插入排序的混合排序算法，有不错的性能
    //有机会在基础番对Timsort（1.8版）和ComparableTimSort（1.7版）进行解析
    Arrays.sort((E[]) elementData, 0, size, c);
    if (modCount != expectedModCount) {
        throw new ConcurrentModificationException();
    }
    modCount++;
}
--------------
List<String> list = new ArrayList<>();
list.add("有村架纯");
list.add("桥本环奈");
list.add("斋藤飞鸟");
list.forEach(s -> System.out.print(s + "!!")); //输出：有村架纯!!桥本环奈!!斋藤飞鸟!!
list.sort((prev, next) -> prev.compareTo(next));//这里我们选用自然排序
list.forEach(s -> System.out.print(s + "!!"));//输出：斋藤飞鸟!!有村架纯!!桥本环奈!!
```

## 5.分片迭代器

```java
default Stream<E> parallelStream() {//并行流
    return StreamSupport.stream(spliterator(), true);//true表示使用并行处理
}
static final class ArrayListSpliterator<E> implements Spliterator<E> {
    private final ArrayList<E> list;
    //起始位置（包含），advance/split操作时会修改
    private int index; // current index, modified on advance/split
    //结束位置（不包含），-1 表示到最后一个元素
    private int fence; // -1 until used; then one past last index
    private int expectedModCount; // initialized when fence set
    /** Create new spliterator covering the given  range */
    ArrayListSpliterator(ArrayList<E> list, int origin, int fence,
                    int expectedModCount) {
        this.list = list; // OK if null unless traversed
        this.index = origin;
        this.fence = fence;
        this.expectedModCount = expectedModCount;
    }
    /**
      * 获取结束位置，主要用于第一次使用时对fence的初始化赋值
      */
    private int getFence() { // initialize fence to size on first use
        int hi; // (a specialized variant appears in method forEach)
        ArrayList<E> lst;
        if ((hi = fence) < 0) {
            //当list为空，fence=0
            if ((lst = list) == null)
                hi = fence = 0;
            else {
            //否则，fence = list的长度
                expectedModCount = lst.modCount;
                hi = fence = lst.size;
            }
        }
        return hi;
    }
    /**
      * 对任务(list)分割，返回一个新的Spliterator迭代器
      */
    public ArrayListSpliterator<E> trySplit() {
        //二分法
        int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
        return (lo >= mid) ? null : // divide range in half unless too small 分成两部分，除非不够分
            new ArrayListSpliterator<E>(list, lo, index = mid,expectedModCount);
    }
    /**
      * 对单个元素执行给定的执行方法
      * 若没有元素需要执行，返回false；若可能还有元素尚未执行，返回true
      */
    public boolean tryAdvance(Consumer<? super E> action) {
        if (action == null)
            throw new NullPointerException();
        int hi = getFence(), i = index;
        if (i < hi) {//起始位置 < 终止位置 -> 说明还有元素尚未执行
            index = i + 1; //起始位置后移一位
            @SuppressWarnings("unchecked") E e = (E)list.elementData[i];
            action.accept(e);//执行给定的方法
            if (list.modCount != expectedModCount)
                throw new ConcurrentModificationException();
            return true;
        }
        return false;
    }
    /**
      * 对每个元素执行给定的方法，依次处理，直到所有元素已被处理或被异常终止
      * 默认方法调用tryAdvance方法
      */
    public void forEachRemaining(Consumer<? super E> action) {
        int i, hi, mc; // hoist accesses and checks from loop
        ArrayList<E> lst; Object[] a;
        if (action == null)
            throw new NullPointerException();
        if ((lst = list) != null && (a = lst.elementData) != null) {
            if ((hi = fence) < 0) {
                mc = lst.modCount;
                hi = lst.size;
            }
            else
                mc = expectedModCount;
            if ((i = index) >= 0 && (index = hi) <= a.length) {
                for (; i < hi; ++i) {
                    @SuppressWarnings("unchecked") E e = (E) a[i];
                    action.accept(e);
                }
                if (lst.modCount == mc)
                    return;
            }
        }
        throw new ConcurrentModificationException();
    }
    /**
      * 计算尚未执行的任务个数
      */
    public long estimateSize() {
        return (long) (getFence() - index);
    }
    /**
      * 返回当前对象的特征量
      */
    public int characteristics() {
        return Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED;
    }
}
--------------
List<String> list = new ArrayList<>();
list.add("有村架纯");
list.add("桥本环奈");
list.add("斋藤飞鸟");
Spliterator<String> spliterator =  list.spliterator();
spliterator.forEachRemaining(s -> System.out.print(s += "妹子!!"));
//输出：有村架纯妹子!!桥本环奈妹子!!斋藤飞鸟妹子!!
//因为这个类是提供给Stream使用的，因此可以直接用Stream，下面的代码作用等同上面，但进行了并发优化
Stream<String> parallelStream = list.parallelStream();
parallelStream.forEach(s -> System.out.print(s += "妹子!!"));
//输出：桥本环奈妹子!!有村架纯妹子!!斋藤飞鸟妹子!!  --> 因为引入并发，所有执行顺序会有些不同
```

需要实现Spliterator<E>接口, 接口定义大家可以自己简单去查看一下,就是要实现以下的一些方法.

```java
    static final class ArrayListSpliterator<E> implements Spliterator<E> {
        
        //用于存放实体变量的list
        private final ArrayList<E> list;
        //遍历的当前位置
        private int index; 
        //结束位置(不包括) 意思是当前可用的元素是[index, fence) = [index, fence-1] 
        private int fence; // -1 until used; then one past last index

        // 构造方法
        ArrayListSpliterator(ArrayList<E> list, int origin, int fence) {
            this.list = list; 
            this.index = origin;
            this.fence = fence;
        }
        
        //第一次使用的时候初始化fence 返回结束位置
        private int getFence() { // initialize fence to size on first use
            int hi; // (a specialized variant appears in method forEach)
            ArrayList<E> lst;
            if ((hi = fence) < 0) {
                if ((lst = list) == null)
                    hi = fence = 0;
                else {
                    hi = fence = lst.size();
                }
            }
            return hi;
        }
        
        /**
         * 根据当前的Spliterator拆分出一个新的Spliterator
         * 相当于二分,
         * Note:共享同一个list,改变的只是下标
         */
        public ArrayListSpliterator<E> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid) ? null : // divide range in half unless too small
                new ArrayListSpliterator<E>(list, lo, index = mid);
        }
        
        //单次遍历  下标index只加1
        public boolean tryAdvance(Consumer<? super E> action) {
            if (action == null)
                throw new NullPointerException();
            int hi = getFence(), i = index;
            if (i < hi) {
                index = i + 1;
                @SuppressWarnings("unchecked") E e = (E)list.get(i);
                action.accept(e);
                return true;
            }
            return false;
        }
        
        //整体遍历 
        public void forEachRemaining(Consumer<? super E> action) {
            int i, hi, mc; // hoist accesses and checks from loop
            ArrayList<E> lst; Object[] a;
            if (action == null)
                throw new NullPointerException();
            if ((lst = list) != null && (a = lst.toArray()) != null) {
                if ((hi = fence) < 0) {
                    hi = lst.size();
                }
                if ((i = index) >= 0 && (index = hi) <= a.length) {
                    for (; i < hi; ++i) {
                        @SuppressWarnings("unchecked") E e = (E) a[i];
                        action.accept(e);
                    }
                }
            }
        }
        
        //剩下还有多少元素
        public long estimateSize() {
            return (long) (getFence() - index);
        }
        
        public int characteristics() {
            return Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED;
        }
        
        public String toString() {
                return "[" + this.index + "," + getFence() + "]";
        }
    }
```

测试1: 测试trySplit()方法,观察Spliterator是如何进行拆分的.

```java
public class TestSpliterator {
    public static void main(String[] args) {
        test_trySplit();
    }

    public static void test_trySplit() {
        ArrayList<Integer> al = new ArrayList<>();
        for (int i = 0; i <= 10; i++) al.add(i);
        ArrayListSpliterator als_1 = new ArrayListSpliterator(al, 0, -1);
        System.out.println("als_1:" + als_1);    // [0,11]
        
System.out.println("---------split-----------");
        ArrayListSpliterator als_2 = als_1.trySplit();
        System.out.println("als_1:" + als_1);    // [5,11]
        System.out.println("als_2:" + als_2);    // [0,5]
        
        // [0,11](als_1) ---> [0,5](als_2) + [5,11](als_1)
        
System.out.println("---------split-----------");
        ArrayListSpliterator als_3 = als_1.trySplit();
        ArrayListSpliterator als_4 = als_2.trySplit();
        System.out.println("als_1:" + als_1);
        System.out.println("als_2:" + als_2);
        System.out.println("als_3:" + als_3);
        System.out.println("als_4:" + als_4);
        
        /**
         * [0,5](als_2)  --> [0,2](als_4)  + [2,5](als_2)
         * [5,11](als_1) --> [8,11](als_1) + [5,8](als_3)
         */
        
System.out.println("---------test the address---------");
        System.out.println("(als_1.list == als_2.list) = " + (als_1.list == als_2.list));
        System.out.println("(als_2.list == als_3.list) = " + (als_2.list == als_3.list));
        System.out.println("(als_3.list == als_4.list) = " + (als_3.list == als_4.list));
    }
}
```

输出1: 对照源码和测试代码结果就可以看出
1. 所有Spliterator都共享一个list,因为拥有的是同一个list的地址.
2. 是按下标进行二分拆分.

```
als_1:[0,11]
---------split-----------
als_1:[5,11]
als_2:[0,5]
---------split-----------
als_1:[8,11]
als_2:[2,5]
als_3:[5,8]
als_4:[0,2]
---------test the address---------
(als_1.list == als_2.list) = true
(als_2.list == als_3.list) = true
(als_3.list == als_4.list) = true
```

测试2:测试forEachRemaining方法,观察index和estimateSize()的变化

```java
public class TestSpliterator {
    public static void main(String[] args) {
        test_forEachRemaining();
        System.out.println("---------------------");
        test_tryAdvance();
    }
    
    public static void test_tryAdvance() {
        ArrayList<Integer> al = new ArrayList<>();
        for (int i = 0; i <= 10; i++) al.add(i);
        ArrayListSpliterator als_1 = new ArrayListSpliterator(al, 0, -1);
        als_1.tryAdvance(new Consumer<Integer>(){
            @Override
            public void accept(Integer t) {
                System.out.print(t + " ");
            }
        });
        System.out.println("\nals_1:" + als_1);
        System.out.println("left size:" + als_1.estimateSize());
    }
    
    public static void test_forEachRemaining() {
        ArrayList<Integer> al = new ArrayList<>();
        for (int i = 0; i <= 10; i++) al.add(i);
        ArrayListSpliterator als_1 = new ArrayListSpliterator(al, 0, -1);
        als_1.forEachRemaining(new Consumer<Integer>(){
            @Override
            public void accept(Integer t) {
                System.out.print(t + " ");
            }
        });
        System.out.println("\nals_1:" + als_1);
        System.out.println("left size:" + als_1.estimateSize());
    }
}
```

输出2:
1. forEachRemaining中index已经和getFence()相等了,并且剩下的size已经没有了,表示已经消费完了.
2. tryAdvance中只是消费了一个,所以index只是增加了1,并且剩下的size只是减少了1.

```
0 1 2 3 4 5 6 7 8 9 10 
als_1:[11,11]
left size:0
---------------------
0 
als_1:[1,11]
left size:10
```

int characteristics()方法

a representation of characteristics这个是定义了该Spliterator的属性. 建议大家自己去看一下英文的注释解释得比较清楚.

```java
public static final int CONCURRENT = 0x00001000;  //表示线程安全的
public static final int DISTINCT   = 0x00000001;        // 元素是独一无二的
public static final int IMMUTABLE  = 0x00000400;    //元素不可变  在遍历过程中不能删除修改增加
public static final int NONNULL    = 0x00000100;     //元素不能为null
public static final int ORDERED    = 0x00000010;    //迭代器按照原始的顺序迭代
public static final int SIZED      = 0x00000040;        //元素可以计数
public static final int SORTED     = 0x00000004;     //元素是有序的
public static final int SUBSIZED   = 0x00004000;
```

## 6.ArrayList和LinkedList的区别

* ArrayList是实现了基于数组的数据结构，LinkedList基于双向链表的数据结构。 
* 对于随机访问get和set，ArrayList觉得优于LinkedList，因为LinkedList要移动指针。
* 对于新增和删除操作add和remove(要根据实际的操作分析)，LinedList比较占优势，因为ArrayList要移动数据, 实际remove中,LinkedList也要从前往后开始查找;LinkedList的remove(int)和remove(Object)的方法的时间复杂度都是O(n),不是O(1).因为会有一个查找的过程。LinkedList的remove(int)要优于remove(Object)，因为remove(int)在查找的时候，会从链表的中间查找，如果int比中间小，找前半部分，否则找后半部分（类似二分查找）
* ArrayList需要扩容, 需要连续内存, 开销在于copy array, LinkedList不需要
* ArrayList实现RamdomAccess接口,可以随机访问
* LinkedList实现了Deque, 具有双端队列的功能
* ArrayList的增删比LinkedList的开销更大，因为除了有查找的时间复杂度外，还有增删的移动过程。

