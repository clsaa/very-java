# HashMap

## 1.HashMap介绍

[image](https://clsaa-markdown-imgbed-1252032169.cos.ap-shanghai.myqcloud.com/very-java/2019-03-07-103237.png)

HashMap 是应用更加广泛的哈希表实现，行为上大致上与 HashTable 一致，主要区别在于 HashMap 不是同步的，支持 null 键和值, 而Hashtable则不能(原因就是equlas()方法需要对象，因为HashMap是后出的API经过处理才可以)。

通常情况下，HashMap 进行 put 或者 get 操作，可以达到常数时间的性能，所以它是绝大部分利用键值对存取场景的首选

HashMap默认容量为16，且要求容量一定为2的整数次幂，HashMap扩容将容量变为原来的2倍。

一段话描述HashMap: HashMap基于哈希思想，实现对数据的读写。当我们将键值对传递给put()方法时，它调用键对象的hashCode()方法来计算hashcode，让后找到bucket位置来储存值对象。当获取对象时，通过键对象的equals()方法找到正确的键值对，然后返回值对象。HashMap使用链表来解决碰撞问题，当发生碰撞了，对象将会储存在链表的下一个节点中。 HashMap在每个链表节点中储存键值对对象。当两个不同的键对象的hashcode相同时，它们会储存在同一个bucket位置的链表中，可通过键对象的equals()方法用来找到键值对。如果链表大小超过阈值（TREEIFY_THRESHOLD, 8），链表就会被改造为树形结构; 如果链表大小法旨小于6, 树就会被改造为链表结构.

```java
public class HashMap<K,V> extends AbstractMap<K,V>
    implements Map<K,V>, Cloneable, Serializable {}
```

## 2.HashMap实现原理

### 2.1.HashMap的主干

HashMap的主干是一个Entry数组。Entry是HashMap的基本组成单元，每一个Entry包含一个key-value键值对。

```java
//HashMap的主干数组，可以看到就是一个Entry数组，初始值为空数组{}，主干数组的长度一定是2的次幂，至于为什么这么做，后面会有详细分析。
transient Entry<K,V>[] table = (Entry<K,V>[]) EMPTY_TABLE;
```

相比较Java7中的链表组合存储，Java8中的HashMap有了大量改进，最为明显的就是Java8中采用数组+链表+红黑树的方式对元素进行存储，这样安全和功能性完备的情况下让其速度更快，同时减少了哈希冲突的情况。

在Java8时内部实现改为了

```java
transient Node<K,V>[] table;

static class Node<K,V> implements Map.Entry<K,V> {
    final int hash;
    final K key;
    V value;
    Node<K,V> next;
    public final boolean equals(Object o) {
        if (o == this)
            return true;
        if (o instanceof Map.Entry) {
            Map.Entry<?,?> e = (Map.Entry<?,?>)o;
            if (Objects.equals(key, e.getKey()) &&
                Objects.equals(value, e.getValue()))
                return true;
        }
        return false;
    }
}
static final class TreeNode<K,V> extends LinkedHashMap.Entry<K,V> {
        TreeNode<K,V> parent;  // red-black tree links
        TreeNode<K,V> left;
        TreeNode<K,V> right;
        TreeNode<K,V> prev;    // needed to unlink next upon deletion
        boolean red;        //是否为红色节点
        TreeNode(int hash, K key, V val, Node<K,V> next) {
            super(hash, key, val, next);
        }
}
```

## 2.2.HashMap的重要属性


静态属性

```java
  // 默认初始容量 16
　static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; 
  // 最大容量
  static final int MAXIMUM_CAPACITY = 1 << 30;
  // 默认负载因子
  static final float DEFAULT_LOAD_FACTOR = 0.75f;
  // 树化阈值
  static final int TREEIFY_THRESHOLD = 8;
  // 链表化阀值
  static final int UNTREEIFY_THRESHOLD = 6;
  // 最小树化容量
  static final int MIN_TREEIFY_CAPACITY = 64;
```

对象属性

```java
　
transient Node<K,V>[] table;//存储元素的实体数组

transient Set<Map.Entry<K,V>> entrySet;

transient int size;//实际存放元素的个数

int threshold; //阈值，当table == {}时，该值为初始容量（初始容量默认为16）；当table被填充了，也就是为table分配内存空间后，threshold一般为 capacity*loadFactory。HashMap在进行扩容时需要参考threshold，后面会详细谈到

final float loadFactor; //加载因子，代表了table的填充度有多少，默认是0.75

transient int modCount;//被修改的次数,用于快速失败，由于HashMap非线程安全，在对HashMap进行迭代时，如果期间其他线程的参与导致HashMap的结构发生变化了（比如put，remove等操作），需要抛出异常ConcurrentModificationException
```

- 加载因子越大,填满的元素越多,好处是空间利用率高了,但冲突的机会加大了,链表长度会越来越长,查找效率降低。反之,加载因子越小,填满的元素越少,好处是冲突的机会减小了,但空间浪费多了,表中的数据将过于稀疏,提高扩容(复制)频率（很多空间还没用，就开始扩容了）
- 冲突的机会越大,则查找的成本越高.
- 因此,必须在 "冲突的机会"与"空间利用率"之间寻找一种平衡与折衷. 这种平衡与折衷本质上是数据结构中有名的"时-空"矛盾的平衡与折衷.如果机器内存足够，并且想要提高查询速度的话可以将加载因子设置小一点；相反如果机器内存紧张，并且对查询速度没有什么要求的话可以将加载因子设置大一点。不过一般我们都不用去设置它，让它取默认值0.75就好了。

## 2.3.构造方法

```java
public HashMap(int initialCapacity, float loadFactor) {
    if (initialCapacity < 0)
        throw new IllegalArgumentException("Illegal initial capacity: " +
                                            initialCapacity);
    if (initialCapacity > MAXIMUM_CAPACITY)
        initialCapacity = MAXIMUM_CAPACITY;
    if (loadFactor <= 0 || Float.isNaN(loadFactor))
        throw new IllegalArgumentException("Illegal load factor: " +
                                            loadFactor);
    this.loadFactor = loadFactor;
    this.threshold = tableSizeFor(initialCapacity);
}

public HashMap(int initialCapacity) {
    this(initialCapacity, DEFAULT_LOAD_FACTOR);
}

public HashMap() {
    this.loadFactor = DEFAULT_LOAD_FACTOR; // all other fields defaulted
}

public HashMap(Map<? extends K, ? extends V> m) {
    this.loadFactor = DEFAULT_LOAD_FACTOR;
    putMapEntries(m, false);
}
```

# 3.HashMap要解决的一些问题

### 3.1.解决Hash冲突的方法

#### 开放定址法（再散列法）

* 在开法定址法中，哈希表中的空闲单元（记为d）不仅允许哈希地址为d的同义词关键字使用，而且也允许发生冲突的其他关键字使用。开法定址法的名字就是来自于此方法的哈希表空闲单元既向同义词开放，也向发生冲突的非同义词关键字开放。谁先找到这个单元谁先占用，这和哈希表的元素排列次序有关。开放定址法以发生冲突的地址d作为自变量来得到一个新的空闲单元，下面介绍常用的几种。（d加下标i记为d[i]，小i打不出来==）特别对于开放定址法的删除操作，不能简单的进行物理删除，因为对于同义词来说，这个地址可能在其查找路径上，若物理删除的话，会中断查找路径，故只能设置删除标志。
  * 线性探查法:发生冲突时，线性遍历后续单元直到找到空闲单元。即d[i] = (d[i-1] + 1) mod m线性探查容易产生堆积的问题。因为若是出现了若干个同意词会堆积在第一个同义词的地址单元附近。
  * 平方探查法:发生冲突时，用平方探查法的探查序列为d[i] + 1²，d[i] + 2², d[i] + 3²...直到找到空闲单元。平方探查法是一种比较好的处理冲突的方法，可以避免堆积问题。它的缺点是不能探查到哈希表上的所有单元，不过至少也能探查到一半单元。

![image](https://clsaa-markdown-imgbed-1252032169.cos.ap-shanghai.myqcloud.com/very-java/2019-03-08-013248.png)

#### 链地址法（拉链法）

链地址法的思想是将哈希表的每个单元作为链表的头结点，所有哈希地址为i的元素构成一个同义词链表。即发生冲突时就把该关键字链在以该单元为头结点的链表的尾部。（图得靠自己脑补）链地址法适用于经常插入删除的情况，其中查找、插入和删除操作主要在同义词链中进行。

#### 再哈希法

在构造函数时同时构造多个不同的哈希函数。当哈希地址发生冲突用其他的函数计算另一个哈希函数地址，直到冲突不再产生为止。这种方法不易产生聚集，但增加了计算时间。

#### 建立公共溢出区

建立公共溢出区的基本思想是将哈希表分为基本表和溢出表2部分，发生冲突的元素都放入溢出表中。


## 3.2.HashMap如何解决hash冲突

- HashMap 采用一种所谓的“Hash 算法”来决定每个元素的存储位置。
- 当程序执行 map.put(String,Obect)方法 时，系统将调用String的 hashCode() 方法得到其 hashCode 值. 每个 Java 对象都有 hashCode() 方法，都可通过该方法获得它的 hashCode 值。得到这个对象的 hashCode 值之后，系统会根据该 hashCode 值来决定该元素的存储位置。
- 扰动函数与下标计算

```
static final int hash(Object key) {
    int h;
    return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
}
```

- 下标计算

```
i = (n - 1) & hash
```

仔细观察哈希值的源头，我们会发现，它并不是 key 本身的 hashCode，而是来自于 HashMap 内部的另外一个 hash 方法。注意，为什么这里需要将高位数据移位到低位进行异或运算呢？这是因为有些数据计算出的哈希值差异主要在高位，而 HashMap 里的哈希寻址是忽略容量以上的高位的，那么这种处理就可以有效避免类似情况下的哈希碰撞。

理论上散列值是一个int型，如果直接拿散列值作为下标访问HashMap主数组的话，考虑到2进制32位带符号的int表值范围从-2147483648到2147483648。前后加起来大概40亿的映射空间。只要哈希函数映射得比较均匀松散，一般应用是很难出现碰撞的。

但问题是一个40亿长度的数组，内存是放不下的。你想，HashMap扩容之前的数组初始大小才16。所以这个散列值是不能直接拿来用的。用之前还要先做对数组的长度取模运算，得到的余数才能用来访问数组下标。源码中模运算是在这个indexFor( )函数里完成的。

顺便说一下，这也正好解释了为什么HashMap的数组长度要取2的整数幂。因为这样（数组长度-1）正好相当于一个“低位掩码”。“与”操作的结果就是散列值的高位全部归零，只保留低位值，用来做数组下标访问。以初始长度16为例，16-1=15。2进制表示是00000000 00000000 00001111。和某散列值做“与”操作如下，结果就是截取了最低的四位值。

时候“扰动函数”的价值就体现出来了，说到这里大家应该猜出来了。看下面这个图

![IMAGE](https://clsaa-markdown-imgbed-1252032169.cos.ap-shanghai.myqcloud.com/very-java/2019-03-07-103239.png)


### 3.2.1.Java8put方法

```java

public V put(K key, V value) {
    return putVal(hash(key), key, value, false, true);
}

static final int hash(Object key) {
    int h;
    return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
}

final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
                boolean evict) {
    Node<K,V>[] tab; Node<K,V> p; int n, i;
    // tab为空则调用resize()初始化创建
    if ((tab = table) == null || (n = tab.length) == 0)
        n = (tab = resize()).length;
    // 计算index, 并对null做处理, 当不存在hash冲突时直接赋值
    if ((p = tab[i = (n - 1) & hash]) == null)
        tab[i] = newNode(hash, key, value, null);
    else {
        Node<K,V> e; K k;
        // 节点key存在, 直接覆盖value, 保证key的唯一性
        if (p.hash == hash &&
            ((k = p.key) == key || (key != null && key.equals(k))))
            e = p;
        // 判断是否为为红黑树
        else if (p instanceof TreeNode)
            e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
        else {//是链表
            // index 相同的情况下
            for (int binCount = 0; ; ++binCount) {
                if ((e = p.next) == null) {
                    p.next = newNode(hash, key, value, null);//如果e后的Node为空，将value赋予下一个Node
                    if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                        treeifyBin(tab, hash);//链表长度达到8，改变链表结构为红黑树
                    break;
                }
                // key相同则跳出循环
                if (e.hash == hash &&
                    ((k = e.key) == key || (key != null && key.equals(k))))
                    break;
                //就是移动指针方便继续取 p.next
                p = e;
            }
        }
        if (e != null) { // existing mapping for key
            V oldValue = e.value;
            //根据规则选择是否覆盖value
            if (!onlyIfAbsent || oldValue == null)
                e.value = value;
            afterNodeAccess(e);
            return oldValue;
        }
    }
    ++modCount;
    // 扩容检测
    if (++size > threshold)
    // size大于加载因子,扩容
        resize();
    afterNodeInsertion(evict);
    return null;
}
```

只会往表中插入 key-value, 若key对应的value之前存在，不会覆盖。（jdk8增加的方法）

```
    @Override
    public V putIfAbsent(K key, V value) {
        return putVal(hash(key), key, value, true, true);
    }
```

1. 对Key求Hash值，然后再计算下标
2. 如果没有碰撞，直接放入桶中（碰撞的意思是计算得到的Hash值相同，需要放到同一个bucket中）
3. 如果碰撞了，以链表的方式链接到后面
4. 如果链表长度超过阀值( TREEIFY THRESHOLD==8)，就把链表转成红黑树，链表长度低于6，就把红黑树转回链表
5. 如果节点已经存在就替换旧值
6. 如果桶满了(容量16*加载因子0.75)，就需要 resize（扩容2倍后重排）

### 3.2.2.Java7put方法


```java
public V put(K key, V value) {
     // 若“key为null”，则将该键值对添加到table[0]中。
         if (key == null) 
            return putForNullKey(value);
     // 若“key不为null”，则计算该key的哈希值，然后将其添加到该哈希值对应的链表中。
         int hash = hash(key.hashCode());
     //搜索指定hash值在对应table中的索引
         int i = indexFor(hash, table.length);
     // 循环遍历Entry数组,若“该key”对应的键值对已经存在，则用新的value取代旧的value。然后退出！
         for (Entry<K,V> e = table[i]; e != null; e = e.next) { 
             Object k;
              if (e.hash == hash && ((k = e.key) == key || key.equals(k))) { //如果key相同则覆盖并返回旧值
                  V oldValue = e.value;
                 e.value = value;
                 e.recordAccess(this);
                 return oldValue;
              }
         }
     //修改次数+1
         modCount++;
     //将key-value添加到table[i]处
     addEntry(hash, key, value, i);
     return null;
}
```

- 上面程序中用到了一个重要的内部接口：Map.Entry，每个 Map.Entry 其实就是一个 key-value 对。从上面程序中可以看出：当系统决定存储 HashMap 中的 key-value 对时，完全没有考虑 Entry 中的 value，仅仅只是根据 key 来计算并决定每个 Entry 的存储位置。这也说明了前面的结论：我们完全可以把 Map 集合中的 value 当成 key 的附属，当系统决定了 key 的存储位置之后，value 随之保存在那里即可。

- 我们慢慢的来分析这个函数，第2和3行的作用就是处理key值为null的情况，我们看看putForNullKey(value)方法：

```java
private V putForNullKey(V value) {
        for (Entry<K,V> e = table[0]; e != null; e = e.next) {
            if (e.key == null) {   //如果有key为null的对象存在，则覆盖掉
                V oldValue = e.value;
                e.value = value;
                e.recordAccess(this);
                return oldValue;
           }
       }
        modCount++;
        addEntry(0, null, value, 0); //如果键为null的话，则hash值为0
        return null;
    }
```

- 注意：如果key为null的话，hash值为0，对象存储在数组中索引为0的位置。即table[0]

- 我们再回去看看put方法中第4行，它是通过key的hashCode值计算hash码，下面是计算hash码的函数：

```java
//计算hash值的方法 通过键的hashCode来计算
    static int hash(int h) {
        // This function ensures that hashCodes that differ only by
        // constant multiples at each bit position have a bounded
        // number of collisions (approximately 8 at default load factor).
        h ^= (h >>> 20) ^ (h >>> 12);
        return h ^ (h >>> 7) ^ (h >>> 4);
    }
```

- 得到hash码之后就会通过hash码去计算出应该存储在数组中的索引，计算索引的函数如下：

```java
 static int indexFor(int h, int length) {       //根据hash值和数组长度算出索引值
     return h & (length-1);  //这里不能随便算取，用hash&(length-1)是有原因的，这样可以确保算出来的索引是在数组大小范围内，不会超出
}
```

- 这个我们要重点说下，我们一般对哈希表的散列很自然地会想到用hash值对length取模（即除法散列法），Hashtable中也是这样实现的，这种方法基本能保证元素在哈希表中散列的比较均匀，但取模会用到除法运算，效率很低，HashMap中则通过h&(length-1)的方法来代替取模，同样实现了均匀的散列，但效率要高很多，这也是HashMap对Hashtable的一个改进。

## 3.3.解决扩容问题

### Java8扩容

先看一下扩容函数： 这是一个重点！重点！重点！ 
**初始化或加倍哈希桶大小。如果是当前哈希桶是null,分配符合当前阈值的初始容量目标。 否则，因为我们扩容成以前的两倍。 在扩容时，要注意区分以前在哈希桶相同index的节点，现在是在以前的index里，还是index+oldlength 里**

```java
final Node<K,V>[] resize() {
  //oldTab 为当前表的哈希桶
  Node<K,V>[] oldTab = table;
  //当前哈希桶的容量 length
  int oldCap = (oldTab == null) ? 0 : oldTab.length;
  //当前的阈值
  int oldThr = threshold;
  //初始化新的容量和阈值为0
  int newCap, newThr = 0;
  //如果当前容量大于0
  if (oldCap > 0) {
      //如果当前容量已经到达上限
      if (oldCap >= MAXIMUM_CAPACITY) {
          //则设置阈值是2的31次方-1
          threshold = Integer.MAX_VALUE;
          //同时返回当前的哈希桶，不再扩容
          return oldTab;
      }//否则新的容量为旧的容量的两倍。 
      else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                oldCap >= DEFAULT_INITIAL_CAPACITY)//如果旧的容量大于等于默认初始容量16
          //那么新的阈值也等于旧的阈值的两倍
          newThr = oldThr << 1; // double threshold
  }//如果当前表是空的，但是有阈值。代表是初始化时指定了容量、阈值的情况
  else if (oldThr > 0) // initial capacity was placed in threshold
      newCap = oldThr;//那么新表的容量就等于旧的阈值
  else {}//如果当前表是空的，而且也没有阈值。代表是初始化时没有任何容量/阈值参数的情况               // zero initial threshold signifies using defaults
      newCap = DEFAULT_INITIAL_CAPACITY;//此时新表的容量为默认的容量 16
      newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);//新的阈值为默认容量16 * 默认加载因子0.75f = 12
  }
  if (newThr == 0) {//如果新的阈值是0，对应的是  当前表是空的，但是有阈值的情况
      float ft = (float)newCap * loadFactor;//根据新表容量 和 加载因子 求出新的阈值
      //进行越界修复
      newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                (int)ft : Integer.MAX_VALUE);
  }
  //更新阈值 
  threshold = newThr;
  @SuppressWarnings({"rawtypes","unchecked"})
  //根据新的容量 构建新的哈希桶
      Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
  //更新哈希桶引用
  table = newTab;
  //如果以前的哈希桶中有元素
  //下面开始将当前哈希桶中的所有节点转移到新的哈希桶中
  if (oldTab != null) {
      //遍历老的哈希桶
      for (int j = 0; j < oldCap; ++j) {
          //取出当前的节点 e
          Node<K,V> e;
          //如果当前桶中有元素,则将链表赋值给e
          if ((e = oldTab[j]) != null) {
              //将原哈希桶置空以便GC
              oldTab[j] = null;
              //如果当前链表中就一个元素，（没有发生哈希碰撞）
              if (e.next == null)
                  //直接将这个元素放置在新的哈希桶里。
                  //注意这里取下标 是用 哈希值 与 桶的长度-1 。 由于桶的长度是2的n次方，这么做其实是等于 一个模运算。但是效率更高
                  newTab[e.hash & (newCap - 1)] = e;
                  //如果发生过哈希碰撞 ,而且是节点数超过8个，转化成了红黑树（暂且不谈 避免过于复杂， 后续专门研究一下红黑树）
              else if (e instanceof TreeNode)
                  ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
              //如果发生过哈希碰撞，节点数小于8个。则要根据链表上每个节点的哈希值，依次放入新哈希桶对应下标位置。
              else { // preserve order
                  //因为扩容是容量翻倍，所以原链表上的每个节点，现在可能存放在原来的下标，即low位， 或者扩容后的下标，即high位。 high位=  low位+原哈希桶容量
                  //低位链表的头结点、尾节点
                  Node<K,V> loHead = null, loTail = null;
                  //高位链表的头节点、尾节点
                  Node<K,V> hiHead = null, hiTail = null;
                  Node<K,V> next;//临时节点 存放e的下一个节点
                  do {
                      next = e.next;
                      //这里又是一个利用位运算 代替常规运算的高效点： 利用哈希值 与 旧的容量，可以得到哈希值去模后，是大于等于oldCap还是小于oldCap，等于0代表小于oldCap，应该存放在低位，否则存放在高位
                      if ((e.hash & oldCap) == 0) {
                          //给头尾节点指针赋值
                          if (loTail == null)
                              loHead = e;
                          else
                              loTail.next = e;
                          loTail = e;
                      }//高位也是相同的逻辑
                      else {
                          if (hiTail == null)
                              hiHead = e;
                          else
                              hiTail.next = e;
                          hiTail = e;
                      }//循环直到链表结束
                  } while ((e = next) != null);
                  //将低位链表存放在原index处，
                  if (loTail != null) {
                      loTail.next = null;
                      newTab[j] = loHead;
                  }
                  //将高位链表存放在新index处
                  if (hiTail != null) {
                      hiTail.next = null;
                      newTab[j + oldCap] = hiHead;
                  }
              }
          }
      }
  }
  return newTab;
  }
```

依据 resize 源码，不考虑极端情况（容量理论最大极限由 MAXIMUM_CAPACITY 指定，数值为 1 <<30, 也就是 2 的 30 次方），我们可以归纳为：

* 门限值等于（负载因子）x（容量），如果构建 HashMap 的时候没有指定它们，那么就是依据相应的默认常量值。
* 门限通常是以倍数进行调整（newThr = oldThr  <<1)，我前面提到，根据 putVal 中的逻辑，当元素个数超过门】限大小时，则调整 Map 大小。
* 扩容后，需要将老的数组中的元素重新放置到新的数组，这是扩容的一一个主要开销来源。

### Java7扩容

 调整大小

```java
void resize(int newCapacity) {
        Entry[] oldTable = table;
        int oldCapacity = oldTable.length;
        if (oldCapacity == MAXIMUM_CAPACITY) {
            threshold = Integer.MAX_VALUE;
            return;
       }

        Entry[] newTable = new Entry[newCapacity];
        transfer(newTable);//用来将原先table的元素全部移到newTable里面
        table = newTable;  //再将newTable赋值给table
        threshold = (int)(newCapacity * loadFactor);//重新计算临界值
    }
```

- 新建了一个HashMap的底层数组，上面代码中第10行为调用transfer方法，将HashMap的全部元素添加到新的HashMap中,并重新计算元素在新的数组中的索引位置
- 当HashMap中的元素越来越多的时候，hash冲突的几率也就越来越高，因为数组的长度是固定的。所以为了提高查询的效率，就要对HashMap的数组进行扩容，数组扩容这个操作也会出现在ArrayList中，这是一个常用的操作，而在HashMap数组扩容之后，最消耗性能的点就出现了：原数组中的数据必须重新计算其在新数组中的位置，并放进去，这就是resize。
- 那么HashMap什么时候进行扩容呢？当HashMap中的元素个数超过数组大小*loadFactor时，就会进行数组扩容，loadFactor的默认值为0.75，这是一个折中的取值。也就是说，默认情况下，数组大小为16，那么当HashMap中元素个数超过16*0.75=12的时候，就把数组的大小扩展为 2*16=32，即扩大一倍，然后重新计算每个元素在数组中的位置，扩容是需要进行数组复制的，复制数组是非常消耗性能的操作，所以如果我们已经预知HashMap中元素的个数，那么预设元素的个数能够有效的提高HashMap的性能。

```java
private void addEntry(int hash, K key, V value, int index) {
    modCount++;
    Entry<?,?> tab[] = table;
    if (count >= threshold) {
        // Rehash the table if the threshold is exceeded
        rehash();
        tab = table;
        hash = key.hashCode();
        index = (hash & 0x7FFFFFFF) % tab.length;
    }
    // Creates the new entry.
    @SuppressWarnings("unchecked")
    Entry<K,V> e = (Entry<K,V>) tab[index];
    //将新增节点放在链表的头部，原来的链表头e，作为新节点的next节点
    tab[index] = new Entry<>(hash, key, value, e);
    count++;
}
```

## 3.4.HashMap键值为空的问题

```java
public class Test {
    public static void main(String[] args) {
        HashMap<Integer, String> map = new HashMap<>();
        map.put(0, "0");
        map.put(1, null);
        map.put(null, "2");
        map.put(3, "3");
        map.put(null, "4");
        System.out.println("map size : " + map.size());
        System.out.println("map null-key : " + map.get(null));
        for (Integer key : map.keySet()) {
            System.out.println("Key-->" + key + " Value-->" + map.get(key));
        }
    }
}


map size : 4
map null-key : 4
Key-->0 Value-->0
Key-->null Value-->4
Key-->1 Value-->null
Key-->3 Value-->3

```

- 由此看来，对于我们平时使用较多的HashMap来说，键和值是可以为null的，map.put(null, “4”)还会覆盖map.put(null, “2”)这个操作。而HashTable则不可以。
- 另外要注意hashmap在get操作时能获取以null为key的value，而在size或遍历操作时无法得到null为key的entity


## 3.5.为什么哈希表的容量一定要是2的整数次幂

- 接下来，我们分析下为什么哈希表的容量一定要是2的整数次幂。首先，length为2的整数次幂的话，h&(length-1)就相当于对length取模，这样便保证了散列的均匀，同时也提升了效率；其次，length为2的整数次幂的话，为偶数，这样length-1为奇数，奇数的最后一位是1，这样便保证了h&(length-1)的最后一位可能为0，也可能为1（这取决于h的值），即与后的结果可能为偶数，也可能为奇数，这样便可以保证散列的均匀性，而如果length为奇数的话，很明显length-1为偶数，它的最后一位是0，这样h&(length-1)的最后一位肯定为0，即只能为偶数，这样任何hash值都只会被散列到数组的偶数下标位置上，这便浪费了近一半的空间，因此，length取2的整数次幂，是为了使不同hash值发生碰撞的概率较小，这样就能使元素在哈希表中均匀地散列。

```java
 h & (table.length-1)      hash                     table.length-1
& (15-1)：                  0100           &              1110           =     0100
& (15-1)：                  0101           &              1110           =     0100
       -----------------------------------------------------------------------------
& (16-1)：                  0100           &              1111           =     0100
& (16-1)：                  0101           &              1111           =     0101
```

- 上面的例子中可以看出：当它们和15-1（1110）“与”的时候，产生了相同的结果，也就是说它们会定位到数组中的同一个位置上去，这就产生了碰撞，8和9会被放到数组中的同一个位置上形成链表，那么查询的时候就需要遍历这个链 表，得到8或者9，这样就降低了查询的效率。同时，我们也可以发现，当数组长度为15的时候，hash值会与15-1（1110）进行“与”，那么 最后一位永远是0，而0001，0011，0101，1001，1011，0111，1101这几个位置永远都不能存放元素了，空间浪费相当大，更糟的是这种情况中，数组可以使用的位置比数组长度小了很多，这意味着进一步增加了碰撞的几率，减慢了查询的效率！而当数组长度为16时，即为2的n次方时，2n-1得到的二进制数的每个位上的值都为1，这使得在低位上&时，得到的和原hash的低位相同，加之hash(int h)方法对key的hashCode的进一步优化，加入了高位计算，就使得只有相同的hash值的两个值才会被放到数组中的同一个位置上形成链表。
- 所以说，当数组长度为2的n次幂的时候，不同的key算得得index相同的几率较小，那么数据在数组上分布就比较均匀，也就是说碰撞的几率小，相对的，查询的时候就不用遍历某个位置上的链表，这样查询效率也就较高了。
- 根据上面 put 方法的源代码可以看出，当程序试图将一个key-value对放入HashMap中时，程序首先根据该 key 的 hashCode() 返回值决定该 Entry 的存储位置：如果两个 Entry 的 key 的 hashCode() 返回值相同，那它们的存储位置相同。如果这两个 Entry 的 key 通过 equals 比较返回 true，新添加 Entry 的 value 将覆盖集合中原有 Entry 的 value，但key不会覆盖。如果这两个 Entry 的 key 通过 equals 比较返回 false，新添加的 Entry 将与集合中原有 Entry 形成 Entry 链，而且新添加的 Entry 位于 Entry 链的头部——具体说明继续看 addEntry() 方法的说明。

```java
void addEntry(int hash, K key, V value, int bucketIndex) {
        Entry<K,V> e = table[bucketIndex]; //如果要加入的位置有值，将该位置原先的值设置为新entry的next,也就是新entry链表的下一个节点
        table[bucketIndex] = new Entry<>(hash, key, value, e);
        if (size++ >= threshold) //如果大于临界值就扩容
            resize(2 * table.length); //以2的倍数扩容
    }
```

- 参数bucketIndex就是indexFor函数计算出来的索引值，第2行代码是取得数组中索引为bucketIndex的Entry对象，第3行就是用hash、key、value构建一个新的Entry对象放到索引为bucketIndex的位置，并且将该位置原先的对象设置为新对象的next构成链表。
- 第4行和第5行就是判断put后size是否达到了临界值threshold，如果达到了临界值就要进行扩容，HashMap扩容是扩为原来的两倍。

## 3.6.Java8中EntrySet是如何维护的

那么平时我们遍历Map的时候获取EntrySet是怎么来的呢？ 
我们可以看看EntrySet方法的源码：

```java
public Set<Map.Entry<K,V>> entrySet() {
        Set<Map.Entry<K,V>> es;
        return (es = entrySet) == null ? (entrySet = new EntrySet()) : es;
}
```

可以看到，就直接返回了一个entrySet，那么这个entrySet是什么呢？

```
transient Set<Map.Entry<K,V>> entrySet;
```

我们不难看到，整个过程中，就直接返回了entrySet，我们在put 的时候也看不到jdk将put的元素加入到entrySet中，那我们遍历的时候元素哪里来？

我们可以看到，虽然是返回的entrySet，但是事实上你在操作的时候都是通过迭代器进行的而非直接操作一个set中的数据(一种懒加载的形式), 比如说在输出（System.out.println）entrySet的时候，是会调用toString()的，我们看看toString方法的源码：

* java.util.AbstractCollection
* java.util.AbstractSet


```java
    public String toString() {
        Iterator<E> it = iterator();
        if (! it.hasNext())
            return "[]";

        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (;;) {
            E e = it.next();
            sb.append(e == this ? "(this Collection)" : e);
            if (! it.hasNext())
                return sb.append(']').toString();
            sb.append(',').append(' ');
        }
    }
```

## 3.7.Get方法实现

### 3.7.1.Java7

```java
public V get(Object key) {
    if (key == null)
        return getForNullKey();
    int hash = hash(key.hashCode());
    for (Entry<K,V> e = table[indexFor(hash, table.length)];
        e != null;
        e = e.next) {
        Object k;
        if (e.hash == hash && ((k = e.key) == key || key.equals(k)))
            return e.value;
    }
    return null;
}
```

- 有了上面存储时的hash算法作为基础，理解起来这段代码就很容易了。从上面的源代码中可以看出：从HashMap中get元素时，首先计算key的hashCode，找到数组中对应位置的某一元素，然后通过key的equals方法在对应位置的链表中找到需要的元素。

### 3.7.2.Java8

```java
final Node<K,V> getNode(int hash, Object key) {
    Node<K,V>[] tab; Node<K,V> first, e; int n; K k;
    if ((tab = table) != null && (n = tab.length) > 0 &&
        (first = tab[(n - 1) & hash]) != null) {
        if (first.hash == hash && // always check first node
            ((k = first.key) == key || (key != null && key.equals(k))))
            return first;
        if ((e = first.next) != null) {
            if (first instanceof TreeNode)
                return ((TreeNode<K,V>)first).getTreeNode(hash, key);
            do {
                if (e.hash == hash &&
                    ((k = e.key) == key || (key != null && key.equals(k))))
                    return e;
            } while ((e = e.next) != null); //do-while遍历链表
        }
    }
    return null;
}
```

逻辑就比较简单了，首先判断第一个节点是否为要找的key值，然后如果是链表形态，则do-while结构遍历链表；如果是红黑树形态，则调用getTreeNode查找并返回对应的TreeNode。getTreeNode的方法调用了TreeNode的find方法，其定义如下:

```java
final TreeNode<K,V> find(int h, Object k, Class<?> kc) {
    TreeNode<K,V> p = this;
    do {
        int ph, dir; K pk;
        TreeNode<K,V> pl = p.left, pr = p.right, q;
        if ((ph = p.hash) > h)
            p = pl;
        else if (ph < h)
            p = pr;
        else if ((pk = p.key) == k || (k != null && k.equals(pk)))
            return p;  //上面三个判断时通过>, <, = 比较符号进行比较的
        else if (pl == null)
            p = pr;
        else if (pr == null)
            p = pl;    //上面两个判断左右子节点是否为空
        else if ((kc != null ||
                  (kc = comparableClassFor(k)) != null) &&
                 (dir = compareComparables(kc, k, pk)) != 0)
            p = (dir < 0) ? pl : pr; //通过Comparable的compareTo方法判断，-1遍历左子树，1遍历右子树
        else if ((q = pr.find(h, k, kc)) != null) //
            return q;
        else
            p = pl; //上面两个是递归调用find的两种形式，一个为递归find右子树，另一个为循环遍历左子树
    } while (p != null);
    return null;
}
```

find的逻辑与putTreeVal的查找逻辑十分类似，也是通过比较符、compareTo方法，递归调用find来查找，不同的是，这里不使用System.identityHashCode来比较了。

当我们调用get()方法，HashMap会使用键对象的hashcode找到bucket位置，找到bucket位置之后，会调用keys.equals()方法去找到链表中正确的节点，最终找到要找的值对象。

![](https://clsaa-markdown-imgbed-1252032169.cos.ap-shanghai.myqcloud.com/very-java/2019-03-09-053527.png)

## 3.8.遍历分析

HashMap的遍历方式与Map的一样，有三种遍历方式，分别为:key的遍历，value的遍历，Entry(K/V对)的遍历。三者的实现一致，下面以key的遍历为例进行分析。

keySet()方法会返回一个Set对象，它的实现类是一个内部类KeySet，继承了AbstractSet，通过KeySet的iterator方法，可以返回一个KeyIterator对象，KeyIterator也是一个内部类，继承了HashIterator，ValueIterator（value的遍历对象），EntryIterator（Entry的遍历对象）也继承了该类，因此，在此分析HashIterator即可。

HashIterator在初始化的时候会将next指向从table的0开始数，第一个存了Node的位置，next指向的为一个Node。构造函数定义如下：

nextNode方法返回next当前指向的Node，并将next指向下一个Node。

```java 
public final Iterator<Map.Entry<K,V>> iterator() {
      return new EntryIterator();
  }
final class EntryIterator extends HashIterator
    implements Iterator<Map.Entry<K,V>> {
    public final Map.Entry<K,V> next() { return nextNode(); }
}

final class EntryIterator extends HashIterator
    implements Iterator<Map.Entry<K,V>> {
    public final Map.Entry<K,V> next() { return nextNode(); }
}

    abstract class HashIterator {
        Node<K,V> next;        // next entry to return
        Node<K,V> current;     // current entry
        int expectedModCount;  // for fast-fail
        int index;             // current slot

        HashIterator() {
            expectedModCount = modCount;
            Node<K,V>[] t = table;
            current = next = null;
            index = 0;
            if (t != null && size > 0) { // advance to first entry
                do {} while (index < t.length && (next = t[index++]) == null);
            }
        }

        public final boolean hasNext() {
            return next != null;
        }

        final Node<K,V> nextNode() {
            Node<K,V>[] t;
            Node<K,V> e = next;
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            if (e == null)
                throw new NoSuchElementException();
            //将next指向下一个不为空的Node
            if ((next = (current = e).next) == null && (t = table) != null) {
                do {} while (index < t.length && (next = t[index++]) == null);
            }
            return e;
        }

        public final void remove() {
            Node<K,V> p = current;
            if (p == null)
                throw new IllegalStateException();
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            current = null;
            K key = p.key;
            removeNode(hash(key), key, null, false, false);
            expectedModCount = modCount;
        }
    }
```

## 3.9.有什么方法可以减少碰撞

* 扰动函数可以减少碰撞，原理是如果两个不相等的对象返回不同的hashcode的话，那么碰撞的几率就会小些，这就意味着存链表结构减小，这样取值的话就不会频繁调用equal方法，这样就能提高HashMap的性能。（扰动即Hash方法内部的算法实现，目的是让不同对象返回不同hashcode。）

* 使用不可变的、声明作final的对象，并且采用合适的equals()和hashCode()方法的话，将会减少碰撞的发生。不可变性使得能够缓存不同键的hashcode，这将提高整个获取对象的速度，使用String，Interger这样的wrapper类作为键是非常好的选择。为什么String, Interger这样的wrapper类适合作为键？因为String是final的，而且已经重写了equals()和hashCode()方法了。不可变性是必要的，因为为了要计算hashCode()，就要防止键值改变，如果键值在放入时和获取时返回不同的hashcode的话，那么就不能从HashMap中找到你想要的对象。

## 3.10.拉链法导致的链表过深问题为什么不用二叉查找树代替，而选择红黑树？为什么不一直使用红黑树？

之所以选择红黑树是为了解决二叉查找树的缺陷，二叉查找树在特殊情况下会变成一条线性结构（这就跟原来使用链表结构一样了，造成很深的问题），遍历查找会非常慢。而红黑树在插入新数据后可能需要通过左旋，右旋、变色这些操作来保持平衡，引入红黑树就是为了查找数据快，解决链表查询深度的问题，我们知道红黑树属于平衡二叉树，但是为了保持“平衡”是需要付出代价的，但是该代价所损耗的资源要比遍历线性链表要少，所以当长度大于8的时候，会使用红黑树，如果链表长度很短的话，根本不需要引入红黑树，引入反而会慢。

## 3.11.说说你对红黑树的见解

![image](https://clsaa-markdown-imgbed-1252032169.cos.ap-shanghai.myqcloud.com/very-java/2019-03-09-053918.png)

1. 每个节点非红即黑
2. 根节点总是黑色的
3. 如果节点是红色的，则它的子节点必须是黑色的（反之不一定）
4. 每个叶子节点都是黑色的空节点（NIL节点）
5. 从根节点到叶节点或空子节点的每条路径，必须包含相同数目的黑色节点（即相同的黑色高度）


## 3.12.重新调整HashMap大小存在什么问题吗

* 当重新调整HashMap大小的时候，确实存在条件竞争，因为如果两个线程都发现HashMap需要重新调整大小了，它们会同时试着调整大小。在调整大小的过程中，存储在链表中的元素的次序会反过来，因为移动到新的bucket位置的时候，HashMap并不会将元素放在链表的尾部，而是放在头部，这是为了避免尾部遍历(tail traversing)。如果条件竞争发生了，那么就死循环了。(多线程的环境下不使用HashMap）
* 为什么多线程会导致死循环，它是怎么发生的？

HashMap的容量是有限的。当经过多次元素插入，使得HashMap达到一定饱和度时，Key映射位置发生冲突的几率会逐渐提高。这时候，HashMap需要扩展它的长度，也就是进行Resize。1.扩容：创建一个新的Entry空数组，长度是原数组的2倍。2.ReHash：遍历原Entry数组，把所有的Entry重新Hash到新数组。

## 3.13.对比HashTable

* 默认容量不同
  * Hashtabl:11
  * HashMap:16
* 扩容不同
  * hashtable: newCapacity = oldCapacity * 2 + 1
  * hashmap: newCap = oldCap << 1
* 处理Hash冲突
  * Hashtable:拉链
  * HashMap:拉链+树化
  * hashtable:put冲突时添加到链表头部
  * hashmap:put冲突时添加到链表尾部
* 内部数据结构不同
  * Hashtable:Entry
  * HashMap:Node->TreeNode
* 线程安全性
  * HashTable:线程安全
  * HashMap:线程不安全
* 效率不同 HashTable 要慢因为加锁
* Hash计算方法不一样
  * hashtable:key.hashCode() & 0x7FFFFFFF
  * hashmap:
    * java8:(key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16)
    * java7:h ^= k.hashCode(); h ^= (h >>> 20) ^ (h >>> 12);return h ^ (h >>> 7) ^ (h >>> 4);
* 索引计算方式不同
  * Hashtable: hash % table.length
  * HashMap: hash & (length-1). 因为length为2^n，所以length-1换算成二进制，其全部位数均为1。按位与计算的原则是两位同时为“1”，结果才为“1”，否则为“0”。所以对于计算表达式h & （length-1）来说，等同于返回h的低（length-1）位，与h%length相同，但是快很多。