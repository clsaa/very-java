# AtomicInteger

## 1.概述

CAS

CAS(Compare-And-Swap，比较并交换)操作是CPU中术语，它保证了操作的原子性。CAS指令需要三个操作数，分别是：

Ｖ：内存位置（也就是本次操作变量的内存地址）；

Ａ：旧的预期值；

Ｂ： 操作完成后的新值。

CAS指令执行时，当且仅当V符合旧预期值A时，处理器用新值B更新V的值，否则它就不执行更新，无论是否更新，都会返回V的旧值，整个CAS操作是一个原子操作。在JDK1.5之后，Java程序中才可以使用CAS操作，该操作由sun.misc.Unsafe类里的compareAndSwapXXX()方法包装提供，虚拟机在内部对这些方法做了特殊处理。在JDK1.5中提供了原子变量，如AtomicInteger,AtomicLong等，由并发大师Doug Lea操刀，提供了在单个变量上面不需要锁的线程安全性。现在就让我们走进AtomicInteger的世界中，探究它是如何实现的，并领略大师的风采。

### 1.1.类结构

![image](https://clsaa-markdown-imgbed-1252032169.cos.ap-shanghai.myqcloud.com/very-java/2019-03-13-120702.png)


##  2.属性

### 2.1.类属性

```java
private static final Unsafe unsafe = Unsafe.getUnsafe();//调用指针类Unsafe
private static final long valueOffset;//变量value的内存偏移量
```

* unsafe字段，AtomicInteger包含了一个Unsafe类的实例，unsafe就是用来实现CAS的；
* valueOfset，通过字面意思就可以看出来valueOfset是value在内存中的偏移量，也就是在内存中的地址，通过Unsafe.objectFieldOffset(Field f)获取。前面在讲CAS时，我们提到需要操作内存的位置，valueOfset就是这个位置。
* Unsafe类是一个可以执行不安全、容易犯错的操作的一个特殊类。虽然Unsafe类中所有方法都是public的，但是这个类只能在一些被信任的代码中使用。

```java
    static {
      try {
        //初始化valueOffset，通过unsafe.objectFieldOffset方法获取成员属性value内存地址相对于对象内存地址的偏移量
        valueOffset = unsafe.objectFieldOffset(AtomicInteger.class.getDeclaredField("value"));
      } catch (Exception ex) { throw new Error(ex); }
    }
```

* Unsafe类可以执行以下几种操作：
  * 分配内存，释放内存：在方法allocateMemory，reallocateMemory，freeMemory中，有点类似c中的malloc，free方法
  * 可以定位对象的属性在内存中的位置，可以修改对象的属性值。使用objectFieldOffset方法
  * 挂起和恢复线程，被封装在LockSupport类中供使用
  * CAS操作(CompareAndSwap，比较并交换，是一个原子操作)

### 2.2.实例属性

```java
private volatile int value;//volatile修饰的int变量value
```

* value字段，表示当前对象代码的基本类型的值，AtomicInteger是int型的线程安全包装类，value就代码了AtomicInteger的值。注意，这个字段是volatile的。

## 3.方法

### 3.1.构造函数

```java
public AtomicInteger(int initialValue) {//带参数的构造函数
    value = initialValue;
}

public AtomicInteger() {//不带参数的构造函数
}
```

### 3.2.get

```
public final int get() {//获取当前最新值
    return value;
}
```

* 直接将value返回,因为volatile能保证可见性,而且get是一个一步操作,不存在线程安全问题


### 3.3.set

```
public final void set(int newValue) {//设置当前值
    value = newValue;
}
```

* 直接将value设置为新值,因为volatile能保证可见性,而且set是一个一步操作,不存在线程安全问题

### 3.4.CAS方法

* 这些操作都是传统上的两步操作有线程安全问题需要特殊指令保证线程安全

```java
public final void lazySet(int newValue) {//最终把值设置为newValue，使用该方法后，其他线程在一段时间内还会获取到旧值
    unsafe.putOrderedInt(this, valueOffset, newValue);
}


public final int getAndSet(int newValue) {//设置新值并返回旧值
    return unsafe.getAndSetInt(this, valueOffset, newValue);
}

public final boolean compareAndSet(int expect, int update) {//如果当前值为expect，则设置为update
    return unsafe.compareAndSwapInt(this, valueOffset, expect, update);
}

public final int getAndIncrement() {//当前值加1返回旧值
    return unsafe.getAndAddInt(this, valueOffset, 1);
}


public final int getAndDecrement() {//当前值减1返回旧值
    return unsafe.getAndAddInt(this, valueOffset, -1);
}


public final int getAndAdd(int delta) {//当前值增加delta，返回旧值
    return unsafe.getAndAddInt(this, valueOffset, delta);
}


public final int incrementAndGet() {//当前值增加1返回新值
    return unsafe.getAndAddInt(this, valueOffset, 1) + 1;
}


public final int decrementAndGet() {//当前值减1，返回新值
    return unsafe.getAndAddInt(this, valueOffset, -1) - 1;
}
```

* Java7中getAndIncrement实现

```
/** 
  * 原子操作，实现自增操作，通过CAS实现，返回自增之前的值 
  * 实现原理是这样的： 
  * 1.首先获得当前值，保存到current中，next=current + 1 
  * 2.如果CAS成功，则返回current 
  *   如果CAS不成功，这说明刚刚有其他的线程修改了当前值，current已经失效了，next也已经失效了 
  *   只能重新获取当值，并继续CAS，直到成功为止 
  */  
public final int getAndIncrement() {  
    for (;;) {  
        int current = get();  
        int next = current + 1;  
        if (compareAndSet(current, next))  
            return current;  
    }  
}  
```

* Java8之后添加了一些函数式编程的接口

```java
    /**
     * Atomically updates the current value with the results of
     * applying the given function, returning the previous value. The
     * function should be side-effect-free, since it may be re-applied
     * when attempted updates fail due to contention among threads.
     *
     * @param updateFunction a side-effect-free function
     * @return the previous value
     * @since 1.8
     */
    public final int getAndUpdate(IntUnaryOperator updateFunction) {
        int prev, next;
        do {
            prev = get();
            next = updateFunction.applyAsInt(prev);
        } while (!compareAndSet(prev, next));
        return prev;
    }
      /**
     * Atomically updates the current value with the results of
     * applying the given function, returning the updated value. The
     * function should be side-effect-free, since it may be re-applied
     * when attempted updates fail due to contention among threads.
     *
     * @param updateFunction a side-effect-free function
     * @return the updated value
     * @since 1.8
     */
    public final int updateAndGet(IntUnaryOperator updateFunction) {
        int prev, next;
        do {
            prev = get();
            next = updateFunction.applyAsInt(prev);
        } while (!compareAndSet(prev, next));
        return next;
    }
```

## 3.缺点

1. ABA问题，如果V的初始值是A，在准备赋值的时候检查到它仍然是A，那么能说它没有改变过吗？也许V经历了这样一个过程：它先变成了B，又变成了A，使用CAS检查时以为它没变，其实却变里；
2. 循环时间长，开销大，通过自旋CAS一直在消耗CPU
3. 只能保证一个共享变量的原子操作，当对多个共享变量操作时就无法保证原子性了。

## 4.Java7实现

Java7中代码看起来冗余一点,大概是unsafe没提供那么强的操作能力

```java
package com.java.source;  
  
import sun.misc.Unsafe;  
  
public class AtomicInteger extends Number implements java.io.Serializable {  
    private static final long serialVersionUID = 6214790243416807050L;  
  
    //使用Unsafe.compareAndSwapInt来执行修改操作，CAS是通过Unsafe.compareAndSwapXXX()方法实现的  
    private static final Unsafe unsafe = Unsafe.getUnsafe();  
    //value在内存中的地址偏移量  
    private static final long valueOffset;  
  
    static {  
      try {  
        //获得value的内存地址偏移量  
        valueOffset = unsafe.objectFieldOffset  
            (AtomicInteger.class.getDeclaredField("value"));  
      } catch (Exception ex) { throw new Error(ex); }  
    }  
    //当前对象代表的值，注意是volatile  
    private volatile int value;  
  
    /*使用给定的值创建对象，也就是把给定的值包装起来*/  
    public AtomicInteger(int initialValue) {  
        value = initialValue;  
    }  
  
    /*默认初始化为0*/  
    public AtomicInteger() {  
    }  
      
    /*getter/setter*/  
    public final int get() {  
        return value;  
    }  
    public final void set(int newValue) {  
        value = newValue;  
    }  
  
    /*最后设置指定的值*/  
    public final void lazySet(int newValue) {  
        unsafe.putOrderedInt(this, valueOffset, newValue);  
    }  
  
   /*原子操作：设定新值，返回旧值，通过CAS完成*/  
    public final int getAndSet(int newValue) {  
        for (;;) {  
            int current = get();  
            if (compareAndSet(current, newValue))  
                return current;  
        }  
    }  
  
    /** 
     * 原子操作 
     * CAS:Compare-and-Swap 
     * 如果当前值==expect，则设置新值 
     */  
    public final boolean compareAndSet(int expect, int update) {  
        return unsafe.compareAndSwapInt(this, valueOffset, expect, update);  
    }  
  
    /** 
     * 原子操作，功能与compareAndSet一样 
     * 有可能意外失败，且不保证排序，但是调用的代码是完全一样的，JVM又在内部做了手脚？ 
     * 在极少情况下用来替代compareAndSet 
     */  
    public final boolean weakCompareAndSet(int expect, int update) {  
        return unsafe.compareAndSwapInt(this, valueOffset, expect, update);  
    }  
  
    /** 
     * 原子操作，实现自增操作，通过CAS实现，返回自增之前的值 
     * 实现原理是这样的： 
     * 1.首先获得当前值，保存到current中，next=current + 1 
     * 2.如果CAS成功，则返回current 
     *   如果CAS不成功，这说明刚刚有其他的线程修改了当前值，current已经失效了，next也已经失效了 
     *   只能重新获取当值，并继续CAS，直到成功为止 
     */  
    public final int getAndIncrement() {  
        for (;;) {  
            int current = get();  
            int next = current + 1;  
            if (compareAndSet(current, next))  
                return current;  
        }  
    }  
  
    /** 
     * 原子操作，实现自减，通过CAS实现，返回当前值 
     * 实现方法同getAndIncrement()相同 
     */  
    public final int getAndDecrement() {  
        for (;;) {  
            int current = get();  
            int next = current - 1;  
            if (compareAndSet(current, next))  
                return current;  
        }  
    }  
  
    /** 
     * 原子操作，将当前值增加delta，并返回当前值 
     * 实现原理同getAndIncrement()相同，只不过一个是增1，一个是增delta 
     */  
    public final int getAndAdd(int delta) {  
        for (;;) {  
            int current = get();  
            int next = current + delta;  
            if (compareAndSet(current, next))  
                return current;  
        }  
    }  
  
    /*原子操作，自增一，并返回增加后的值*/  
    public final int incrementAndGet() {  
        for (;;) {  
            int current = get();  
            int next = current + 1;  
            if (compareAndSet(current, next))  
                return next;  
        }  
    }  
  
   /*原子操作，自减，并返回减小后的值*/  
    public final int decrementAndGet() {  
        for (;;) {  
            int current = get();  
            int next = current - 1;  
            if (compareAndSet(current, next))  
                return next;  
        }  
    }  
  
    /*原子操作，增加delta，并返回增加后的操作*/  
    public final int addAndGet(int delta) {  
        for (;;) {  
            int current = get();  
            int next = current + delta;  
            if (compareAndSet(current, next))  
                return next;  
        }  
    }  
  
   /** 
    * 一些常规方法 
    */  
    public String toString() {  
        return Integer.toString(get());AtomicLong  
    }  
  
      
    public int intValue() {  
        return get();  
    }  
  
    public long longValue() {  
        return (long)get();  
    }  
  
    public float floatValue() {  
        return (float)get();  
    }  
  
    public double doubleValue() {  
        return (double)get();  
    }  
  
}  
```