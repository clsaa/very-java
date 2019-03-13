# Hashtable

## 1.概述

## 1.1.介绍与类结构

Hashtable 存储的内容是键值对(key-value)映射，其底层实现是一个Entry数组+链表。

Hashtable是线程安全的它的key、value都不可以为null。此外，Hashtable中的映射不是有序的。

![image](https://clsaa-markdown-imgbed-1252032169.cos.ap-shanghai.myqcloud.com/very-java/2019-03-13-055622.png)

* Hashtable继承于Dictionary类，实现了Map接口。Map是"key-value键值对"接口
* Dictionary是声明了操作"键值对"函数接口的抽象类。 


### 1.2.数据节点Entry

```java
private static class Entry<K,V> implements Map.Entry<K,V> {
    // 哈希值
    int hash;
    K key;
    V value;
    // 指向的下一个Entry，即链表的下一个节点
    Entry<K,V> next;

    // 构造函数
    protected Entry(int hash, K key, V value, Entry<K,V> next) {
        this.hash = hash;
        this.key = key;
        this.value = value;
        this.next = next;
    }

    protected Object clone() {
        return new Entry<K,V>(hash, key, value,
              (next==null ? null : (Entry<K,V>) next.clone()));
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    // 设置value。若value是null，则抛出异常。
    public V setValue(V value) {
        if (value == null)
            throw new NullPointerException();

        V oldValue = this.value;
        this.value = value;
        return oldValue;
    }

    // 覆盖equals()方法，判断两个Entry是否相等。
    // 若两个Entry的key和value都相等，则认为它们相等。
    public boolean equals(Object o) {
        if (!(o instanceof Map.Entry))
            return false;
        Map.Entry e = (Map.Entry)o;

        return (key==null ? e.getKey()==null : key.equals(e.getKey())) &&
           (value==null ? e.getValue()==null : value.equals(e.getValue()));
    }

    public int hashCode() {
        return hash ^ (value==null ? 0 : value.hashCode());
    }

    public String toString() {
        return key.toString()+"="+value.toString();
    }
}
```

## 2.变量

```java
private transient Entry<?,?>[] table; //数组+链表实现
private transient int count; //Entry的总数
private int threshold; //count>=此值时，扩容rehash
private float loadFactor; //负载因子
private transient int modCount = 0;  //用来帮助实现fail-fast机制
```

加载因子loadFactor是Hashtable扩容前可以达到多满的一个尺度。这个参数是可以设置的，默认是0.75。加载因子过高虽然减少了空间开销，但同时也增加了查找某个条目的时间（在大多数 Hashtable 操作中，包括 get 和 put 操作，都反映了这一点）。

* Hashtable是通过"拉链法"实现的哈希表。它包括几个重要的成员变量：table, count, threshold, loadFactor, modCount。
* table是一个Entry[]数组类型，而Entry实际上就是一个单向链表。哈希表的"key-value键值对"都是存储在Entry数组中的。 
* count是Hashtable的大小，它是Hashtable保存的键值对的数量。 
* threshold是Hashtable的阈值，用于判断是否需要调整Hashtable的容量。threshold的值="容量*加载因子"。
* loadFactor就是加载因子。 
* modCount是用来实现fail-fast机制的

## 3.方法

### 3.1.构造函数

* Hashtable(int initialCapacity)

```java
// 指定“容量大小”的构造函数
public Hashtable(int initialCapacity) {
    this(initialCapacity, 0.75f);
}
```


*  Hashtable()

```java
// 默认构造函数。
public Hashtable() {
    // 默认构造函数，指定的容量大小是11；加载因子是0.75
    this(11, 0.75f);
}
```

*  Hashtable(Map<? extends K, ? extends V> t)

```java
// 包含“子Map”的构造函数
public Hashtable(Map<? extends K, ? extends V> t) {
    this(Math.max(2*t.size(), 11), 0.75f);
    // 将“子Map”的全部元素都添加到Hashtable中
    putAll(t);
}
```

* Hashtable(int initialCapacity, float loadFactor)

```java

public Hashtable(int initialCapacity, float loadFactor) {
  if (initialCapacity < 0)
      throw new IllegalArgumentException("Illegal Capacity: "+
                                          initialCapacity);
  if (loadFactor <= 0 || Float.isNaN(loadFactor))
      throw new IllegalArgumentException("Illegal Load: "+loadFactor);
  if (initialCapacity==0)
      initialCapacity = 1;
  this.loadFactor = loadFactor;
  table = new Entry<?,?>[initialCapacity];
  threshold = (int)Math.min(initialCapacity * loadFactor, MAX_ARRAY_SIZE + 1);
}
```

* 其实上面的前三个构造方法，最终都调用了第四个构造方法。如果在初始化Hashtable时，不指定加载因子loadFactor，那么加载因子会被设置为0.75f。
* 注意: 如果设置initialCapacity为0,则实际初始化容量为1

### 3.2.常用方法

#### 3.2.1.clear

clear() 的作用是清空Hashtable。它是将Hashtable的table数组的值全部设为null

```java
public synchronized void clear() {
    Entry tab[] = table;
    modCount++;
    for (int index = tab.length; --index >= 0; )
        tab[index] = null;
    count = 0;
}
```

#### 3.2.2.contains() 和 containsValue()

contains() 和 containsValue() 的作用都是判断Hashtable是否包含“值(value)”

```java
public boolean containsValue(Object value) {
    return contains(value);
}

public synchronized boolean contains(Object value) {
    // Hashtable中“键值对”的value不能是null，
    // 若是null的话，抛出异常!
    if (value == null) {
        throw new NullPointerException();
    }

    // 从后向前遍历table数组中的元素(Entry)
    // 对于每个Entry(单向链表)，逐个遍历，判断节点的值是否等于value
    Entry tab[] = table;
    for (int i = tab.length ; i-- > 0 ;) {
        for (Entry<K,V> e = tab[i] ; e != null ; e = e.next) {
            if (e.value.equals(value)) {
                return true;
            }
        }
    }
    return false;
}
```

#### 3.2.3.containsKey()

```java
public synchronized boolean containsKey(Object key) {
    Entry tab[] = table;
    int hash = key.hashCode();
    // 计算索引值，
    // % tab.length 的目的是防止数据越界
    int index = (hash & 0x7FFFFFFF) % tab.length;
    // 找到“key对应的Entry(链表)”，然后在链表中找出“哈希值”和“键值”与key都相等的元素
    for (Entry<K,V> e = tab[index] ; e != null ; e = e.next) {
        if ((e.hash == hash) && e.key.equals(key)) {
            return true;
        }
    }
    return false;
}
```

#### 3.2.4.elements()

```java
public synchronized Enumeration<V> elements() {
    return this.<V>getEnumeration(VALUES);
}

// 获取Hashtable的枚举类对象
private <T> Enumeration<T> getEnumeration(int type) {
    if (count == 0) {
        return (Enumeration<T>)emptyEnumerator;
    } else {
        return new Enumerator<T>(type, false);
    }
}
```

从中，我们可以看出：

1. 若Hashtable的实际大小为0,则返回“空枚举类”对象emptyEnumerator；
2. 否则，返回正常的Enumerator的对象。(Enumerator实现了迭代器和枚举两个接口)

我们先看看emptyEnumerator对象是如何实现的

```java
private static Enumeration emptyEnumerator = new EmptyEnumerator();

// 空枚举类
// 当Hashtable的实际大小为0；此时，又要通过Enumeration遍历Hashtable时，返回的是“空枚举类”的对象。
private static class EmptyEnumerator implements Enumeration<Object> {

    EmptyEnumerator() {
    }

    // 空枚举类的hasMoreElements() 始终返回false
    public boolean hasMoreElements() {
        return false;
    }

    // 空枚举类的nextElement() 抛出异常
    public Object nextElement() {
        throw new NoSuchElementException("Hashtable Enumerator");
    }
}
```

我们在来看看Enumeration类

Enumerator的作用是提供了“通过elements()遍历Hashtable的接口” 和 “通过entrySet()遍历Hashtable的接口”。因为，它同时实现了 “Enumerator接口”和“Iterator接口”。

```java
private class Enumerator<T> implements Enumeration<T>, Iterator<T> {
    // 指向Hashtable的table
    Entry[] table = Hashtable.this.table;
    // Hashtable的总的大小
    int index = table.length;
    Entry<K,V> entry = null;
    Entry<K,V> lastReturned = null;
    int type;

    // Enumerator是 “迭代器(Iterator)” 还是 “枚举类(Enumeration)”的标志
    // iterator为true，表示它是迭代器；否则，是枚举类。
    boolean iterator;

    // 在将Enumerator当作迭代器使用时会用到，用来实现fail-fast机制。
    protected int expectedModCount = modCount;

    Enumerator(int type, boolean iterator) {
        this.type = type;
        this.iterator = iterator;
    }

    // 从遍历table的数组的末尾向前查找，直到找到不为null的Entry。
    public boolean hasMoreElements() {
        Entry<K,V> e = entry;
        int i = index;
        Entry[] t = table;
        /* Use locals for faster loop iteration */
        while (e == null && i > 0) {
            e = t[--i];
        }
        entry = e;
        index = i;
        return e != null;
    }

    // 获取下一个元素
    // 注意：从hasMoreElements() 和nextElement() 可以看出“Hashtable的elements()遍历方式”
    // 首先，从后向前的遍历table数组。table数组的每个节点都是一个单向链表(Entry)。
    // 然后，依次向后遍历单向链表Entry。
    public T nextElement() {
        Entry<K,V> et = entry;
        int i = index;
        Entry[] t = table;
        /* Use locals for faster loop iteration */
        while (et == null && i > 0) {
            et = t[--i];
        }
        entry = et;
        index = i;
        if (et != null) {
            Entry<K,V> e = lastReturned = entry;
            entry = e.next;
            return type == KEYS ? (T)e.key : (type == VALUES ? (T)e.value : (T)e);
        }
        throw new NoSuchElementException("Hashtable Enumerator");
    }

    // 迭代器Iterator的判断是否存在下一个元素
    // 实际上，它是调用的hasMoreElements()
    public boolean hasNext() {
        return hasMoreElements();
    }

    // 迭代器获取下一个元素
    // 实际上，它是调用的nextElement()
    public T next() {
        if (modCount != expectedModCount)
            throw new ConcurrentModificationException();
        return nextElement();
    }

    // 迭代器的remove()接口。
    // 首先，它在table数组中找出要删除元素所在的Entry，
    // 然后，删除单向链表Entry中的元素。
    public void remove() {
        if (!iterator)
            throw new UnsupportedOperationException();
        if (lastReturned == null)
            throw new IllegalStateException("Hashtable Enumerator");
        if (modCount != expectedModCount)
            throw new ConcurrentModificationException();

        synchronized(Hashtable.this) {
            Entry[] tab = Hashtable.this.table;
            int index = (lastReturned.hash & 0x7FFFFFFF) % tab.length;

            for (Entry<K,V> e = tab[index], prev = null; e != null;
                 prev = e, e = e.next) {
                if (e == lastReturned) {
                    modCount++;
                    expectedModCount++;
                    if (prev == null)
                        tab[index] = e.next;
                    else
                        prev.next = e.next;
                    count--;
                    lastReturned = null;
                    return;
                }
            }
            throw new ConcurrentModificationException();
        }
    }
}
```
entrySet(), keySet(), keys(), values()的实现方法和elements()差不多，而且源码中已经明确的给出了注释。这里就不再做过多说明了。

#### 3.2.5.get()

get() 的作用就是获取key对应的value，没有的话返回null

```java
public synchronized V get(Object key) {
    Entry tab[] = table;
    int hash = key.hashCode();
    // 计算索引值，
    int index = (hash & 0x7FFFFFFF) % tab.length;
    // 找到“key对应的Entry(链表)”，然后在链表中找出“哈希值”和“键值”与key都相等的元素
    for (Entry<K,V> e = tab[index] ; e != null ; e = e.next) {
        if ((e.hash == hash) && e.key.equals(key)) {
            return e.value;
        }
    }
    return null;
}
```

#### 3.2.6.put()

put() 的作用是对外提供接口，让Hashtable对象可以通过put()将“key-value”添加到Hashtable中。

```java
public synchronized V put(K key, V value) {
    // Hashtable中不能插入value为null的元素！！！
    if (value == null) {
        throw new NullPointerException();
    }

    // 若“Hashtable中已存在键为key的键值对”，
    // 则用“新的value”替换“旧的value”
    Entry tab[] = table;
    int hash = key.hashCode();
    int index = (hash & 0x7FFFFFFF) % tab.length;
    for (Entry<K,V> e = tab[index] ; e != null ; e = e.next) {
        if ((e.hash == hash) && e.key.equals(key)) {
            V old = e.value;
            e.value = value;
            return old;
            }
    }

    // 若“Hashtable中不存在键为key的键值对”，
    // (01) 将“修改统计数”+1
    modCount++;
    // (02) 若“Hashtable实际容量” > “阈值”(阈值=总的容量 * 加载因子)
    //  则调整Hashtable的大小
    if (count >= threshold) {
        // Rehash the table if the threshold is exceeded
        rehash();

        tab = table;
        index = (hash & 0x7FFFFFFF) % tab.length;
    }

    // (03) 将“Hashtable中index”位置的Entry(链表)保存到e中
    Entry<K,V> e = tab[index];
    // (04) 创建“新的Entry节点”，并将“新的Entry”插入“Hashtable的index位置”，并设置e为“新的Entry”的下一个元素(即“新Entry”为链表表头)。        
    tab[index] = new Entry<K,V>(hash, key, value, e);
    // (05) 将“Hashtable的实际容量”+1
    count++;
    return null;
}
```

* rehash

```java
// 调整Hashtable的长度，将长度变成原来的(2倍+1)
// (01) 将“旧的Entry数组”赋值给一个临时变量。
// (02) 创建一个“新的Entry数组”，并赋值给“旧的Entry数组”
// (03) 将“Hashtable”中的全部元素依次添加到“新的Entry数组”中
protected void rehash() {
    int oldCapacity = table.length;
    Entry[] oldMap = table;

    int newCapacity = oldCapacity * 2 + 1;
    Entry[] newMap = new Entry[newCapacity];

    modCount++;
    threshold = (int)(newCapacity * loadFactor);
    table = newMap;

    for (int i = oldCapacity ; i-- > 0 ;) {
        for (Entry<K,V> old = oldMap[i] ; old != null ; ) {
            Entry<K,V> e = old;
            old = old.next;

            int index = (e.hash & 0x7FFFFFFF) % newCapacity;
            e.next = newMap[index];
            newMap[index] = e;
        }
    }
}
```

#### 3.2.7.putAll()

putAll() 的作用是将“Map(t)”的中全部元素逐一添加到Hashtable中

```java
public synchronized void putAll(Map<? extends K, ? extends V> t){
     for (Map.Entry<? extends K, ? extends V> e : t.entrySet())
        put(e.getKey(), e.getValue());
}
```

#### 3.2.8.remove()

```java
public synchronized V remove(Object key) {
    Entry tab[] = table;
    int hash = key.hashCode();
    int index = (hash & 0x7FFFFFFF) % tab.length;
    // 找到“key对应的Entry(链表)”
    // 然后在链表中找出要删除的节点，并删除该节点。
    for (Entry<K,V> e = tab[index], prev = null ; e != null ; prev = e, e = e.next) {
        if ((e.hash == hash) && e.key.equals(key)) {
            modCount++;
            if (prev != null) {
                prev.next = e.next;
            } else {
                tab[index] = e.next;
            }
            count--;
            V oldValue = e.value;
            e.value = null;
            return oldValue;
        }
    }
    return null;
}
```

### 3.3.Hashtable实现的Cloneable接口

Hashtable实现了Cloneable接口，即实现了clone()方法。clone()方法的作用很简单，就是克隆一个Hashtable对象并返回

```java
// 克隆一个Hashtable，并以Object的形式返回。
public synchronized Object clone() {
    try {
        Hashtable<K,V> t = (Hashtable<K,V>) super.clone();
        t.table = new Entry[table.length];
        for (int i = table.length ; i-- > 0 ; ) {
            t.table[i] = (table[i] != null)
            ? (Entry<K,V>) table[i].clone() : null;
        }
        t.keySet = null;
        t.entrySet = null;
        t.values = null;
        t.modCount = 0;
        return t;
    } catch (CloneNotSupportedException e) {
        // this shouldn't happen, since we are Cloneable
        throw new InternalError();
    }
}
```

### 3.4.Hashtable实现的Serializable接口

```java
private synchronized void writeObject(java.io.ObjectOutputStream s)
    throws IOException
{
    // Write out the length, threshold, loadfactor
    s.defaultWriteObject();

    // Write out length, count of elements and then the key/value objects
    s.writeInt(table.length);
    s.writeInt(count);
    for (int index = table.length-1; index >= 0; index--) {
        Entry entry = table[index];

        while (entry != null) {
        s.writeObject(entry.key);
        s.writeObject(entry.value);
        entry = entry.next;
        }
    }
}

private void readObject(java.io.ObjectInputStream s)
     throws IOException, ClassNotFoundException
{
    // Read in the length, threshold, and loadfactor
    s.defaultReadObject();

    // Read the original length of the array and number of elements
    int origlength = s.readInt();
    int elements = s.readInt();

    // Compute new size with a bit of room 5% to grow but
    // no larger than the original size.  Make the length
    // odd if it's large enough, this helps distribute the entries.
    // Guard against the length ending up zero, that's not valid.
    int length = (int)(elements * loadFactor) + (elements / 20) + 3;
    if (length > elements && (length & 1) == 0)
        length--;
    if (origlength > 0 && length > origlength)
        length = origlength;

    Entry[] table = new Entry[length];
    count = 0;

    // Read the number of elements and then all the key/value objects
    for (; elements > 0; elements--) {
        K key = (K)s.readObject();
        V value = (V)s.readObject();
            // synch could be eliminated for performance
            reconstitutionPut(table, key, value);
    }
    this.table = table;
}
```

### 3.5.Hashtable遍历方式

#### 3.5.1.遍历Hashtable的键值对

1. 根据entrySet()获取Hashtable的“键值对”的Set集合。
2. 通过Iterator迭代器遍历“第一步”得到的集合。

```java
// 假设table是Hashtable对象
// table中的key是String类型，value是Integer类型
Integer integ = null;
Iterator iter = table.entrySet().iterator();
while(iter.hasNext()) {
    Map.Entry entry = (Map.Entry)iter.next();
    // 获取key
    key = (String)entry.getKey();
        // 获取value
    integ = (Integer)entry.getValue();
}
```

#### 3.5.2.通过Iterator遍历Hashtable的键

1. 根据keySet()获取Hashtable的“键”的Set集合。
2. 通过Iterator迭代器遍历“第一步”得到的集合。

```java
// 假设table是Hashtable对象
// table中的key是String类型，value是Integer类型
String key = null;
Integer integ = null;
Iterator iter = table.keySet().iterator();
while (iter.hasNext()) {
        // 获取key
    key = (String)iter.next();
        // 根据key，获取value
    integ = (Integer)table.get(key);
}
```

#### 3.5.3.通过Iterator遍历Hashtable的值

1. 根据value()获取Hashtable的“值”的集合。
2. 通过Iterator迭代器遍历“第一步”得到的集合。

```java
// 假设table是Hashtable对象
// table中的key是String类型，value是Integer类型
Integer value = null;
Collection c = table.values();
Iterator iter= c.iterator();
while (iter.hasNext()) {
    value = (Integer)iter.next();
}
```

#### 3.5.4.通过Enumeration遍历Hashtable的键

1. 根据keys()获取Hashtable的集合。
2. 通过Enumeration遍历“第一步”得到的集合。

```java
Enumeration enu = table.keys();
while(enu.hasMoreElements()) {
    System.out.println(enu.nextElement());
}   
```

#### 3.5.5.通过Enumeration遍历Hashtable的值

1. 根据elements()获取Hashtable的集合。
2. 通过Enumeration遍历“第一步”得到的集合。

```java
Enumeration enu = table.elements();
while(enu.hasMoreElements()) {
    System.out.println(enu.nextElement());
}
```