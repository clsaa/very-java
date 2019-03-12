# LinedList

## 1.总结

### 1.1.类继承关系

![image](https://clsaa-markdown-imgbed-1252032169.cos.ap-shanghai.myqcloud.com/very-java/2019-03-12-104739.png)

1. 继承了AbstractSequentialList抽象类：在遍历LinkedList的时候，官方更推荐使用迭代器。（因为LinkedList底层是通过一个链表来实现的）（虽然LinkedList也提供了get（int index）方法，但是底层的实现是：每次调用get（int index）方法的时候，都需要从链表的头部或者尾部进行遍历，每一的遍历时间复杂度是O(index)，而相对比ArrayList的底层实现，每次遍历的时间复杂度都是O(1)。所以不推荐通过get（int index）遍历LinkedList。至于上面的说从链表的头部后尾部进行遍历：官方源码对遍历进行了优化：通过判断索引index更靠近链表的头部还是尾部来选择遍历的方向）（所以这里遍历LinkedList推荐使用迭代器）。
2. 与ArrayList对比发现，LinkedList并没有实现RandomAccess，而实现RandomAccess表明其支持快速（通常是固定时间）随机访问。此接口的主要目的是允许一般的算法更改其行为，从而在将其应用到随机或连续访问列表时能提供良好的性能。
3. 实现了List接口。（提供List接口中所有方法的实现）
4. 实现了Cloneable接口，它支持克隆（浅克隆），底层实现：LinkedList节点并没有被克隆，只是通过Object的clone（）方法得到的Object对象强制转化为了LinkedList,然后把它内部的实例域都置空，然后把被拷贝的LinkedList节点中的每一个值都拷贝到clone中。（后面有源码解析）
5. 实现了Deque接口。实现了Deque所有的可选的操作。
6. 实现了Serializable接口。表明它支持序列化。（和ArrayList一样，底层都提供了两个方法：readObject（ObjectInputStream o）、writeObject（ObjectOutputStream o），用于实现序列化，底层只序列化节点的个数和节点的值）。
7. implements Deque<E>：Deque，Double ended queue，双端队列。LinkedList可用作队列或双端队列就是因为实现了它。

### 1.2.主要特性

* LinkedList底层是一个双链表。是一个直线型的链表结构。 
* LinkedList内部实现了6种主要的辅助方法：void linkFirst(E e)、void linkLast(E e)、linkBefore(E e, Node<E> succ)、E unlinkFirst(Node<E> f)、E unlinkLast(Node<E> l)、E unlink(Node<E> x)。它们都是private修饰的方法或者没有修饰符，表明这里都只是为LinkedList的其他方法提供服务，或者同一个包中的类提供服务。在LinkedList内部，绝大部分方法的实现都是依靠上面的6种辅助方法实现的，所以，只要把这6个辅助方法自己实现了（或者理解了（最好还是自己实现一次）），LinkedList的基本操作也就掌握了。 

## 2.属性

### 2.1.实例属性

```java
    transient int size = 0;

    /**
     * Pointer to first node.
     * Invariant: (first == null && last == null) ||
     *            (first.prev == null && first.item != null)
     */
    transient Node<E> first;

    /**
     * Pointer to last node.
     * Invariant: (first == null && last == null) ||
     *            (last.next == null && last.item != null)
     */
    transient Node<E> last;

    private static class Node<E> {
      E item;
      Node<E> next;
      Node<E> prev;

      Node(Node<E> prev, E element, Node<E> next) {
          this.item = element;
          this.next = next;
          this.prev = prev;
      }
    }
```

* 被声明为transient的属性不会被序列化，这就是transient关键字的作用
* size：用来记录LinkedList的大小 
* Node first：用来表示LinkedList的头节点。 
* Node last：用来表示LinkedList的尾节点。

## 3.方法

### 3.1.构造方法

接下来，看LinkedList提供的构造方法。ArrayList提供了两种构造方法。 

* 构造空LinkedList。

```java
/**
 * 构造一个空链表.
 */
public LinkedList() {
}
```

* 构造空LinkedList

```java
/**
 * 根据指定集合c构造linkedList。先构造一个空linkedlist，在把指定集合c中的所有元素都添加到linkedList中。
 *
 * @param  c 指定集合
 * @throws NullPointerException 如果特定指定集合c为null
 */
public LinkedList(Collection<? extends E> c) {
    this();
    addAll(c);
}
```

### 3.2.addAll方法

```java
// 首先调用一下空的构造器。
//然后调用addAll(c)方法。
  public LinkedList(Collection<? extends E> c) {
        this();
        addAll(c);
    }
//通过调用addAll(int index, Collection<? extends E> c) 完成集合的添加。
  public boolean addAll(Collection<? extends E> c) {
        return addAll(size, c);
    }
//重点来了。（还是建议大家自己手动写一次源码实现。） 
//几乎所有的涉及到在指定位置添加或者删除或修改操作都需要判断传进来的参数是否合法。
// checkPositionIndex(index)方法就起这个作用。该方法后面会介绍。  
public boolean addAll(int index, Collection<? extends E> c) {
  checkPositionIndex(index);
  //先把集合转化为数组，然后为该数组添加一个新的引用（Objext[] a）。
  Object[] a = c.toArray();
  //新建一个变量存储数组的长度。
  int numNew = a.length;
  //如果待添加的集合为空，直接返回，无需进行后面的步骤。后面都是用来把集合中的元素添加到
  //LinkedList中。
  if (numNew == 0)
      return false;
//这里定义了两个节点：（读这里的程序的时候，强烈建议自己画一个图，辅助你理解这个过程）。
//Node<E> succ：指代待添加节点的位置。
//Node<E> pred：指代待添加节点的前一个节点。
//下面的代码是依据新添加的元素的位置分为两个分支：
//①新添加的元素的位置位于LinkedList最后一个元素的后面。
//新添加的元素的位置位于LinkedList中。
//如果index==size;说明此时需要添加LinkedList中的集合中的每一个元素都是在LinkedList
//最后面。所以把succ设置为空，pred指向尾节点。
//否则的话succ指向插入待插入位置的节点。这里用到了node（int index）方法，这个方法
//后面会详细分析，这里只需要知道该方法返回对应索引位置上的Node（节点）。pred指向succ节点的前一个节点。
//上面分析的这几步如果看不懂的话，可以自己画个图，清晰明了^_^。
  Node<E> pred, succ;
  if (index == size) {
      succ = null;
      pred = last;
  } else {
      succ = node(index);
      pred = succ.prev;
  }
//接着遍历数组中的每个元素。在每次遍历的时候，都新建一个节点，该节点的值存储数组a中遍历
//的值，该节点的prev用来存储pred节点，next设置为空。接着判断一下该节点的前一个节点是否为
//空，如果为空的话，则把当前节点设置为头节点。否则的话就把当前节点的前一个节点的next值
//设置为当前节点。最后把pred指向当前节点，以便后续新节点的添加。
  for (Object o : a) {
      @SuppressWarnings("unchecked") E e = (E) o;
      Node<E> newNode = new Node<>(pred, e, null);
      if (pred == null)
          first = newNode;
      else
          pred.next = newNode;
      pred = newNode;
  }
//这里仍然和上面一样，分两种情况对待：
//①当succ==null（也就是新添加的节点位于LinkedList集合的最后一个元素的后面），
//通过遍历上面的a的所有元素，此时pred指向的是LinkedList中的最后一个元素，所以把
//last指向pred指向的节点。
//当不为空的时候，表明在LinkedList集合中添加的元素，需要把pred的next指向succ上，
//succ的prev指向pred。
//最后把集合的大小设置为新的大小。
//modCount（修改的次数）自增。
    if (succ == null) {
        last = pred;
    } else {
        pred.next = succ;
        succ.prev = pred;
    }

    size += numNew;
    modCount++;
    return true;
} 

public Object[] toArray() {
    Object[] result = new Object[size];
    int i = 0;
    for (Node<E> x = first; x != null; x = x.next)
        result[i++] = x.item;
    return result;
}

```

可以看出，传入集合的构造函数，是将集合转成数组，再将数组转成链表，经过了2次O(N)复杂度的循环。

### 3.3.一些辅助方法

接下是一些辅助方法（这些方法大多数是private或者是protected修饰的），LinkedList中大多数通过他们来完成List、Deque中类似的操作。

#### 3.3.1.把参数中的元素作为链表的第一个元素

```java
//因为我们需要把该元素设置为头节点，所以需要新建一个变量把头节点存储起来。
//然后新建一个节点，把next指向f，然后自身设置为头结点。
//再判断一下f是否为空，如果为空的话，说明原来的LinkedList为空，所以同时也需要把新节点设
//置为尾节点。否则就把f的prev设置为newNode。
//size和modCount自增。
private void linkFirst(E e) {
    final Node<E> f = first;
    final Node<E> newNode = new Node<>(null, e, f);
    first = newNode;
    if (f == null)
        last = newNode;
    else
        f.prev = newNode;
    size++;
    modCount++;
}
```

#### 3.3.2.把参数中的元素作为链表的最后一个元素

```java
//因为我们需要把该元素设置为尾节点，所以需要新建一个变量把尾节点存储起来。
//然后新建一个节点，把last指向l，然后自身设置为尾结点。
//再判断一下l是否为空，如果为空的话，说明原来的LinkedList为空，所以同时也需要把新节点设
//置为头节点。否则就把l的next设置为newNode。
//size和modCount自增。
    void linkLast(E e) {
        final Node<E> l = last;
        final Node<E> newNode = new Node<>(l, e, null);
        last = newNode;
        if (l == null)
            first = newNode;
        else
            l.next = newNode;
        size++;
        modCount++;
    }
```

#### 3.3.3.在非空节点succ之前插入元素e

```java

void linkBefore(E e, Node<E> succ) {
        // assert succ != null;
//首先我们需要新建一个变量指向succ节点的前一个节点，因为我们要在succ前面插入一个节点。
//接着新建一个节点，它的prev设置为我们刚才新建的变量，后置节点设置为succ。
//然后修改succ的prev为新节点。
//接着判断一个succ的前一个节点是否为空，如果为空的话，需要把新节点设置为为头结点。
//如果不为空，则把succ的前一个节点的next设置为新节点。
        final Node<E> pred = succ.prev;
        final Node<E> newNode = new Node<>(pred, e, succ);
        succ.prev = newNode;
        if (pred == null)
            first = newNode;
        else
            pred.next = newNode;
        size++;
        modCount++;
    }

```

#### 3.3.4.删除LinkedList中第一个节点

（该节点不为空）（并且返回删除的节点的值） 

官方文档的代码中也给出了注释：使用该方法的前提是参数f是头节点，而且f不能为空。 不过，它是私有方法，我们也没有权限使用

```java
  private E unlinkFirst(Node<E> f) {
        // assert f == first && f != null;
  //定义一个变量element指向待删除节点的值，接着定义一个变量next指向待删除节点的
  //下一个节点。（因为我们需要设置f节点的下一个节点为头结点，而且需要把f节点的值设置为空）
  //接着把f的值和它的next设置为空，把它的下一个节点设置为头节点。
  //接着判断一个它的下一个节点是否为空，如果为空的话，则需要把last设置为空。否则
  //的话，需要把next的prev设置为空，因为next现在指代头节点。
        final E element = f.item;
        final Node<E> next = f.next;
        f.item = null;
        f.next = null; // help GC
        first = next;
        if (next == null)
            last = null;
        else
            next.prev = null;
        size--;
        modCount++;
        return element;
    }
```

#### 3.3.5.删除LinkedList的最后一个节点

（该节点不为空）（并且返回删除节点对应的值）和unlinkFirst（）方法思路差不多。

```java
 private E unlinkLast(Node<E> l) {
        // assert l == last && l != null;
        final E element = l.item;
        final Node<E> prev = l.prev;
        l.item = null;
        l.prev = null; // help GC
        last = prev;
        if (prev == null)
            first = null;
        else
            prev.next = null;
        size--;
        modCount++;
        return element;
    }
```

#### 3.3.6.删除一个节点（该节点不为空） 

试想一下，我们删除LinkedList中的一个节点，都需要把该节点的前、后节点重新链接起来，总不能让链表断开吧(⊙o⊙)…。 
所以呢，这就需要我们新建几个变量来保存他们的状态。 
所以，下面新建了三个变量，第一个变量用来存储当前被删除节点的值，因为我们最后需要把这个返回回去。第二个变量用来存储待删除节点的前一个节点，第三个变量用来存储待删除节点的后一个节点。 
接下来判断一下，待删除节点的前一个节点是否为空，如果为空的话，表明待删除的节点是我们的头结点。则需要把待删除节点的后一个节点设置为头结点。如果不为空，就需要把待删除的节点的前、后节点链接起来（你不能删个节点，就把人家LinkedList弄断了吧(⊙o⊙)…），此刻只需要链接一部分，通过pre.next=next；x.prev= null设置（这里也是JDK设计的技巧的部分，一般我们都会直接在这里把链接全部设置上），接着判断一下待删除的节点是否为空，如果为空的话，则表明待删除节点是尾节点，所以需要我们把待删除节点的前一个节点设置为尾节点。如果不为空的，则把next.prev=prev，x.next=null（看到了吧，JDK直接把链接分到了两次判断中分别设置）。最后把待删除节点的值设置为空。切记不要忘了把size和modCount自减，最后返回被删除节点的值（机制的JDK一开始就把要返回的值保存起来了。）

```java
E unlink(Node<E> x) {
    // assert x != null;
    final E element = x.item;
    final Node<E> next = x.next;
    final Node<E> prev = x.prev;

    if (prev == null) {
        first = next;
    } else {
        prev.next = next;
        x.prev = null;
    }

    if (next == null) {
        last = prev;
    } else {
        next.prev = prev;
        x.next = null;
    }

    x.item = null;
    size--;
    modCount++;
    return element;
}
```

#### 3.3.7.计算指定索引上的节点

LinkedList还对整个做了优化，不是盲目地直接从头进行遍历，而是先比较一下index更靠近链表（LinkedList）的头节点还是尾节点。然后进行遍历，获取相应的节点。

```
Node<E> node(int index) {
  // assert isElementIndex(index);

  if (index < (size >> 1)) {
      Node<E> x = first;
      for (int i = 0; i < index; i++)
          x = x.next;
      return x;
  } else {
      Node<E> x = last;
      for (int i = size - 1; i > index; i--)
          x = x.prev;
      return x;
  }
}
```

到目前大部分的私有方法全部讲完了，接下来的方法大多引用了上面的工具方法。

### 3.4.add方法

```java
    /**
     * Appends the specified element to the end of this list.
     *
     * <p>This method is equivalent to {@link #addLast}.
     *
     * @param e element to be appended to this list
     * @return {@code true} (as specified by {@link Collection#add})
     */
    public boolean add(E e) {
        linkLast(e);
        return true;
    }
 
    /**
     * Links e as last element.
     */
    void linkLast(E e) {
        final Node<E> l = last;
        final Node<E> newNode = new Node<>(l, e, null);
        last = newNode;
        if (l == null)
            first = newNode;
        else
            l.next = newNode;
        size++;
        modCount++;
    }

```

可以看出，add就是next引用指向的拨动，末尾追加Node节点，追加的Node成为末尾元素。这种结构反应在堆内存上，可以充分节省空间。

### 3.5.get方法

```java
/**
  * Returns the element at the specified position in this list.
  *
  * @param index index of the element to return
  * @return the element at the specified position in this list
  * @throws IndexOutOfBoundsException {@inheritDoc}
  */
public E get(int index) {
    checkElementIndex(index);
    return node(index).item;
}

/**
  * Returns the (non-null) Node at the specified element index.
  */
Node<E> node(int index) {
    // assert isElementIndex(index);

    if (index < (size >> 1)) {
        Node<E> x = first;
        for (int i = 0; i < index; i++)
            x = x.next;
        return x;
    } else {
        Node<E> x = last;
        for (int i = size - 1; i > index; i--)
            x = x.prev;
        return x;
    }
}

```

折半 size >> 1，遍历取值，比起数组结构效率就低很多了。

### 3.6.remove方法

```java
    /**
     * Removes the first occurrence of the specified element from this list,
     * if it is present.  If this list does not contain the element, it is
     * unchanged.  More formally, removes the element with the lowest index
     * {@code i} such that
     * <tt>(o==null ? get(i)==null : o.equals(get(i)))</tt>
     * (if such an element exists).  Returns {@code true} if this list
     * contained the specified element (or equivalently, if this list
     * changed as a result of the call).
     *
     * @param o element to be removed from this list, if present
     * @return {@code true} if this list contained the specified element
     */
    public boolean remove(Object o) {
        if (o == null) {
            for (Node<E> x = first; x != null; x = x.next) {
                if (x.item == null) {
                    unlink(x);
                    return true;
                }
            }
        } else {
            for (Node<E> x = first; x != null; x = x.next) {
                if (o.equals(x.item)) {
                    unlink(x);
                    return true;
                }
            }
        }
        return false;
    }
 
    /**
     * Unlinks non-null node x.
     */
    E unlink(Node<E> x) {
        // assert x != null;
        final E element = x.item;
        final Node<E> next = x.next;
        final Node<E> prev = x.prev;
 
        if (prev == null) {
            first = next;
        } else {
            prev.next = next;
            x.prev = null;
        }
 
        if (next == null) {
            last = prev;
        } else {
            next.prev = prev;
            x.next = null;
        }
 
        x.item = null;
        size--;
        modCount++;
        return element;
    }

```

可以看出删除节点就是将当前节点的前一个节点的next，指向当前节点的next, 将当前节点的后一个节点的prev，指向当前节点的prev。

### 3.7.迭代器

```java

    public ListIterator<E> listIterator(int index) {
        checkPositionIndex(index);
        return new ListItr(index);
    }
 
    private class ListItr implements ListIterator<E> {
        private Node<E> lastReturned;
        private Node<E> next;
        private int nextIndex;
        private int expectedModCount = modCount;
 
        ListItr(int index) {
            // assert isPositionIndex(index);
            next = (index == size) ? null : node(index);
            nextIndex = index;
        }
 
        public boolean hasNext() {
            return nextIndex < size;
        }
 
        public E next() {
            checkForComodification();
            if (!hasNext())
                throw new NoSuchElementException();
 
            lastReturned = next;
            next = next.next;
            nextIndex++;
            return lastReturned.item;
        }
 
        public boolean hasPrevious() {
            return nextIndex > 0;
        }
 
        public E previous() {
            checkForComodification();
            if (!hasPrevious())
                throw new NoSuchElementException();
 
            lastReturned = next = (next == null) ? last : next.prev;
            nextIndex--;
            return lastReturned.item;
        }
 
        public int nextIndex() {
            return nextIndex;
        }
 
        public int previousIndex() {
            return nextIndex - 1;
        }
 
        public void remove() {
            checkForComodification();
            if (lastReturned == null)
                throw new IllegalStateException();
 
            Node<E> lastNext = lastReturned.next;
            unlink(lastReturned);
            if (next == lastReturned)
                next = lastNext;
            else
                nextIndex--;
            lastReturned = null;
            expectedModCount++;
        }
 
        public void set(E e) {
            if (lastReturned == null)
                throw new IllegalStateException();
            checkForComodification();
            lastReturned.item = e;
        }
 
        public void add(E e) {
            checkForComodification();
            lastReturned = null;
            if (next == null)
                linkLast(e);
            else
                linkBefore(e, next);
            nextIndex++;
            expectedModCount++;
        }
 
        public void forEachRemaining(Consumer<? super E> action) {
            Objects.requireNonNull(action);
            while (modCount == expectedModCount && nextIndex < size) {
                action.accept(next.item);
                lastReturned = next;
                next = next.next;
                nextIndex++;
            }
            checkForComodification();
        }
 
        final void checkForComodification() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
    }

```

默认迭代器，index参数为0，链表逐一迭代。

从内部类ListItr看到，LinkedList的迭代器可以双向进行迭代的，迭代过程中只能使用迭代器的add()、set()、remove()对链表进行修改，为什么重点标出只能，因为很多新手很容易写出这样的代码：

```java
//虽然没有显式地使用迭代器，但其实底层实现也是使用迭代器进行迭代
for (Object o : linkedList) {
    if(o.equals(something)){
        linkedList.remove(o);
    }
}
```

这样是会报错的，通过注释，我们可以看到这是在迭代过程中是不允许直接修改链表的结构的，fail-fast机制，可以看到，在迭代器的代码中，有个非public方法checkForComodification(),迭代器中几乎每个操作都会调用一下该方法，而该方法的方法体内仅仅做的一件事就是检查链表的modCount是否等于迭代器expectedModCount，不相等将抛出ConcurrentModificationException，从而实现不允许在迭代过程中直接修改链表结构，至于为什么要这样做则自行研究上述迭代的代码看如果在迭代过程中修改了链表结构会有什么错误发生。因此，正确的使用迭代器删除元素应该是像下面这样：

```java
while (iterator.hasNext()){
    Object o = iterator.next();
    if(o.equals(something)){
        iterator.remove();
    }
}
```

从LinkedList的迭代器代码中可以看到一个方法forEachRemaining(),我们查看Iterator接口的描述：

```java
/**
    * Performs the given action for each remaining element until all elements
    * have been processed or the action throws an exception.  Actions are
    * performed in the order of iteration, if that order is specified.
    * Exceptions thrown by the action are relayed to the caller.
    *
    * @implSpec
    * <p>The default implementation behaves as if:
    * <pre>{@code
    *     while (hasNext())
    *         action.accept(next());
    * }</pre>
    *
    * @param action The action to be performed for each element
    * @throws NullPointerException if the specified action is null
    * @since 1.8
    */
default void forEachRemaining(Consumer<? super E> action) {
    Objects.requireNonNull(action);
    while (hasNext())
        action.accept(next());
}
```

看到该方法是在jdk1.8版本后才加入的，用于实现对集合每个元素做特定操作，传入参数为Consumer接口，Consumer接口是一个只有一个方法需要实现的接口，所以不难看出其实该方法是用于配合lambda来使用的，以此更加简化java的表达，举例：

```
iterator.forEachRemaining(System.out::println);

iterator.forEachRemaining(item -> {
    System.out.println(item);
});
```



### 4.分片迭代器

继续看Linked的源代码，我们看到一个Spliterator，这是jdk1.8才加入的迭代器，该迭代器其实就是可分割的迭代器，可分割，意味着可以将迭代过程分给不同的线程去完成，从而提高效率。为了方便，源码分析将在下述代码以注释方式进行：

```java
    @Override
    public Spliterator<E> spliterator() {
        return new LLSpliterator<E>(this, -1, 0);
    }

    /** A customized variant of Spliterators.IteratorSpliterator */
    //变种的IteratorSpliterator，区别：IteratorSpliterator使用interator进行迭代，LLSpliterator直接使用Node的next指针迭代，原则上迭代速度更快
    static final class LLSpliterator<E> implements Spliterator<E> {
        static final int BATCH_UNIT = 1 << 10;  // batch array size increment;分割的长度增加单位，每分割一次下次分割长度增加1024
        static final int MAX_BATCH = 1 << 25;  // max batch array size;最大分割长度，大于2^25分割长度将不再增加
        final LinkedList<E> list; // null OK unless traversed
        Node<E> current;      // current node; null until initialized
        int est;              // size estimate; -1 until first needed
        int expectedModCount; // initialized when est set
        int batch;            // batch size for splits;当前分割长度

        LLSpliterator(LinkedList<E> list, int est, int expectedModCount) {
            this.list = list;
            this.est = est;
            this.expectedModCount = expectedModCount;
        }

        final int getEst() {
            int s; // force initialization
            final LinkedList<E> lst;
            if ((s = est) < 0) {
                if ((lst = list) == null)
                    s = est = 0;
                else {
                    expectedModCount = lst.modCount;
                    current = lst.first;
                    s = est = lst.size;
                }
            }
            return s;
        }

        public long estimateSize() { return (long) getEst(); }

        //分割出batch长度的Spliterator
        public Spliterator<E> trySplit() { 
            Node<E> p;
            int s = getEst();
            if (s > 1 && (p = current) != null) {
                //每次分割长度增加BATCH_UNIT，达到MAX_BATCH便不再增加
                int n = batch + BATCH_UNIT;
                if (n > s)
                    n = s;
                if (n > MAX_BATCH)
                    n = MAX_BATCH;
                //将需要分割的元素生成数组
                Object[] a = new Object[n];
                int j = 0;
                do { a[j++] = p.item; } while ((p = p.next) != null && j < n);
                current = p;
                batch = j;
                est = s - j;
                //返回新的Spliterator，注意：新的Spliterator为ArraySpliterator类型，实现上有所区别，ArraySpliterator每次分割成一半一半，而IteratorSpliterator算术递增
                return Spliterators.spliterator(a, 0, j, Spliterator.ORDERED);
            }
            return null;
        }
        
        //遍历当前迭代器中所有元素并对获取元素值进行action操作（操作所有元素）
        public void forEachRemaining(Consumer<? super E> action) {
            Node<E> p; int n;
            if (action == null) throw new NullPointerException();
            if ((n = getEst()) > 0 && (p = current) != null) {
                current = null;
                est = 0;
                do {
                    E e = p.item;
                    p = p.next;
                    action.accept(e);
                } while (p != null && --n > 0);
            }
            if (list.modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
        
        //对当前迭代元素的元素值进行action操作（只操作一个元素）
        public boolean tryAdvance(Consumer<? super E> action) {
            Node<E> p;
            if (action == null) throw new NullPointerException();
            if (getEst() > 0 && (p = current) != null) {
                --est;
                E e = p.item;
                current = p.next;
                action.accept(e);
                if (list.modCount != expectedModCount)
                    throw new ConcurrentModificationException();
                return true;
            }
            return false;
        }

        public int characteristics() {
            return Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED;
        }
    }
```