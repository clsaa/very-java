# Arrays

## 1.sort函数

### 1.1.Java7实现

Java Arrays中提供了对所有类型的排序。其中主要分为Primitive(8种基本类型)和Object两大类。

* 基本类型：采用调优的快速排序；
* 对象类型：采用改进的归并排序。

也就是说，优化的归并排序既快速（nlog(n)）又稳定。

对于对象的排序，稳定性很重要。比如成绩单，一开始可能是按人员的学号顺序排好了的，现在让我们用成绩排，那么你应该保证，本来张三在李四前面，即使他们成绩相同，张三不能跑到李四的后面去。

而快速排序是不稳定的，而且最坏情况下的时间复杂度是O(n^2)。

另外，对象数组中保存的只是对象的引用，这样多次移位并不会造成额外的开销，但是，对象数组对比较次数一般比较敏感，有可能对象的比较比单纯数的比较开销大很多。归并排序在这方面比快速排序做得更好，这也是选择它作为对象排序的一个重要原因之一。

排序优化：实现中快排和归并都采用递归方式，而在递归的底层，也就是待排序的数组长度小于7时，直接使用冒泡排序，而不再递归下去。

分析：长度为6的数组冒泡排序总比较次数最多也就1+2+3+4+5+6=21次，最好情况下只有6次比较。而快排或归并涉及到递归调用等的开销，其时间效率在n较小时劣势就凸显了，因此这里采用了冒泡排序，这也是对快速排序极重要的优化。

### 1.2.Java8实现

#### 1.2.1.基本类型排序

Arrays的sort方法直接调用了DualQivotQuicksort的sort方法，类名的英语意思是双轴快速排序。可见双轴快速排序是关键。

<https://www.jianshu.com/p/d7ba7d919b80>
<https://mp.weixin.qq.com/s/t0dsJeN397wO41pwBWPeTg>

点进sort方法：

```java
// Use Quicksort on small arrays
if (right - left < QUICKSORT_THRESHOLD) {//QUICKSORT_THRESHOLD = 286
  sort(a, left, right, true);
  return;
}
```

数组一进来，会碰到第一个阀值QUICKSORT_THRESHOLD（286），注解上说，小过这个阀值的进入Quicksort （快速排序），其实并不全是，点进去sort(a, left, right, true);方法：

```java
// Use insertion sort on tiny arrays
if (length < INSERTION_SORT_THRESHOLD) {
    if (leftmost) {
    ......
```

判断数组长度是否小于INSERTION_SORT_THRESHOLD，如果是的话，则使用插入排序，并且在插入排序使用leftmost控制使用传统的插入排序还是改进的插入排序。改进的插入排序被称为成对插入排序（pair insertion sort）,其基本思想是一次取出两个未排序数组的元素，并且确保（a1>a2）,那么在a1找到位置后，a2的位置肯定在a1之前，继续查找即可。

点进去后我们看到第二个阀值INSERTION_SORT_THRESHOLD（47），如果元素少于47这个阀值，就用插入排序，往下看确实如此：

```java
/*
  * Traditional (without sentinel) insertion sort,
  * optimized for server VM, is used in case of
  * the leftmost part.
  */
for (int i = left, j = i; i < right; j = ++i) {
    int ai = a[i + 1];
    while (ai < a[j]) {
        a[j + 1] = a[j];
        if (j-- == left) {
            break;
        }
    }
a[j + 1] = ai;
```

![image](https://clsaa-markdown-imgbed-1252032169.cos.ap-shanghai.myqcloud.com/very-java/2019-03-23-9175374-fec991caa60aa10d.gif)

这里有必要解释一下双轴快速排序，双轴快速排序的思想是相对于单轴快速排序，经典的单轴快速排序，每次选择一个元素作为轴值pivot,然后使用双指针将小于pivot移到pivot的左边，大于pivot的值移到右边，使得pivot处于最终位置，这个过程称为一次排序，对两边递归的调用一次排序可以得到最终的排序结果。
而双轴快速排序的基本思想是一次可以将两个元素放到最终位置上，假设这两个轴值为pivot1,pivot2,那么一次排序后，最终数组被这两个元素划分成三块：<pivot1, >=pivot1并且<=pivot2, >pivot2。为了达到这种划分，我们需要三个指针来进行操作i,k,j,i左边的是小于pivot1的，j右边的是大于pivot1的，k用来扫描，当k和j相聚的时候，则扫描结束。关于双轴快速排序的具体实现可以参考这篇文章<https://blog.csdn.net/holmofy/article/details/71168530#t7>，写的很好。
相比单轴快速排序，双轴快速排序能够获取更快的排序效率，我们来看一看源码中的关键实现，从源码上来看，当5分位点所有值都不相同的时候，选取第二个点和第四个点作为双轴进行双轴快速排序。

至于大过INSERTION_SORT_THRESHOLD（47）的，就使用第三个点进行单轴快速排序：

1. 从数列中挑出五个元素，称为 “基准”（pivot）；
2. 重新排序数列，所有元素比基准值小的摆放在基准前面，所有元素比基准值大的摆在基准的后面（相同的数可以到任一边）。在这个分区退出之后，该基准就处于数列的中间位置。这个称为分区（partition）操作；
3. 递归地（recursive）把小于基准值元素的子数列和大于基准值元素的子数列排序。

![image](https://clsaa-markdown-imgbed-1252032169.cos.ap-shanghai.myqcloud.com/very-java/2019-03-23-9175374-a047775f1c22e6ee.gif)

在对原生类型的排序支持上，JDK使用的是一种叫做 双轴快速排序的排序方法：DualPivotQuicksort.sort，相信很多人和我一样，是第一次看到这个名词，我们结合Arrays.sort对int数组的排序好好了解下这个排序：

这是少于阀值QUICKSORT_THRESHOLD（286）的两种情况，至于大于286的，它会进入归并排序（Merge Sort），但在此之前，它有个小动作, 其目的也很明确，就是检查原来数组是不是基本有序，方法是找出有序的数组片段，用count进行统计，如果count一旦等于MAX_RUN_COUNT，则认为基本无序，使用我们上面提到的方法进行快速排序：

```java
    // Check if the array is nearly sorted
    for (int k = left; k < right; run[count] = k) {
        if (a[k] < a[k + 1]) { // ascending
            while (++k <= right && a[k - 1] <= a[k]);
        } else if (a[k] > a[k + 1]) { // descending
            while (++k <= right && a[k - 1] >= a[k]);
            for (int lo = run[count] - 1, hi = k; ++lo < --hi; ) {
                int t = a[lo]; a[lo] = a[hi]; a[hi] = t;
            }
        } else { // equal
            for (int m = MAX_RUN_LENGTH; ++k <= right && a[k - 1] == a[k]; ) {
                if (--m == 0) {
                    sort(a, left, right, true);
                    return;
                }
            }
        }

        /*
         * The array is not highly structured,
         * use Quicksort instead of merge sort.
         */
         //基本无序状态检查
        if (++count == MAX_RUN_COUNT) {
            sort(a, left, right, true);
            return;
        }
    }
```

这里主要作用是看他数组具不具备结构：实际逻辑是分组排序，每降序为一个组，像1,9,8,7,6,8。9到6是降序，为一个组，然后把降序的一组排成升序：1,6,7,8,9,8。然后最后的8后面继续往后面找。。。

在上面统计count的过程中，同样使用run数组记录所有的基本有序的数组的最后一个元素的下标，如果判定结果是基本有序，则使用的归并排序进行最终的排序，方法是通过run方法记录的下标每次合并两个有序序列，最终使得数组有序，这里就不贴出很长的归并代码。

每遇到这样一个降序组，++count，当count大于MAX_RUN_COUNT（67），被判断为这个数组不具备结构（也就是这数据时而升时而降），然后送给之前的sort(里面的快速排序)的方法（The array is not highly structured,use Quicksort instead of merge sort.）。
如果count少于MAX_RUN_COUNT（67）的，说明这个数组还有点结构，就继续往下走下面的归并排序：


```java
   // Determine alternation base for merge
    byte odd = 0;
    for (int n = 1; (n <<= 1) < count; odd ^= 1);
```

从这里开始，正式进入归并排序（Merge Sort）！

```java
  // Merging
    for (int last; count > 1; count = last) {
        for (int k = (last = 0) + 2; k <= count; k += 2) {
            int hi = run[k], mi = run[k - 1];
            for (int i = run[k - 2], p = i, q = mi; i < hi; ++i) {
                if (q >= hi || p < mi && a[p + ao] <= a[q + ao]) {
                    b[i + bo] = a[p++ + ao];
                } else {
                    b[i + bo] = a[q++ + ao];
                }
            }
            run[++last] = hi;
        }
        if ((count & 1) != 0) {
            for (int i = right, lo = run[count - 1]; --i >= lo;
                b[i + bo] = a[i + ao]
            );
            run[++last] = right;
        }
        int[] t = a; a = b; b = t;
        int o = ao; ao = bo; bo = o;
    }
```

![image](https://clsaa-markdown-imgbed-1252032169.cos.ap-shanghai.myqcloud.com/very-java/2019-03-23-9175374-7e2639f6dd669c86.gif)

总结：
从上面分析，Arrays.sort并不是单一的排序，而是插入排序，快速排序，归并排序三种排序的组合，为此我画了个流程图：

![image](https://clsaa-markdown-imgbed-1252032169.cos.ap-shanghai.myqcloud.com/very-java/2019-03-23-172027.png)

#### 1.2.2.Object类型排序

Arrays.sort方法的Object数组要求实现Comparable接口，因为在其实现过程中有
Object类型的排序中legacyMergeSort是经典的归并排序，不过它即将被废弃了，这里只是用来兼容老的排序方法，现在默认使用的是ComparableSort里面的sort方法，我们的分析着重在这个方法。

```java
    public static <T> void sort(T[] a, Comparator<? super T> c) {
        if (c == null) {
            sort(a);
        } else {
            if (LegacyMergeSort.userRequested)
                legacyMergeSort(a, c);
            else
                TimSort.sort(a, 0, a.length, c, null, 0, 0);
        }
    }
```

首先，如果数组的长度小于MIN_MERGE(32),那么就会调用二分插入排序binarySort方法进行排序，所谓二分排序，是指在插入的过程中，使用二分查找的方法查找待插入的位置，这种查找方法会比线性查找快一点。

```java
// If array is small, do a "mini-TimSort" with no merges
if (nRemaining < MIN_MERGE) {
    int initRunLen = countRunAndMakeAscending(a, lo, hi, c);
    binarySort(a, lo, hi, lo + initRunLen, c);
    return;
}
```
如果不答应MIN_MERGE，则使用归并排序，归并的方法是将数组换分成等块的小数组(除最后一块)，在小数组内进行二分插入排序，排序后将当前数组的初始位置，长度使用pushRun方法压栈，mergeCollapse方法每次会从数组中取出两个小数组进行归并排序，最终循环结束后，数组也已经排序完成。


```java
/**
 * March over the array once, left to right, finding natural runs,
 * extending short natural runs to minRun elements, and merging runs
 * to maintain stack invariant.
 */
TimSort<T> ts = new TimSort<>(a, c, work, workBase, workLen);
int minRun = minRunLength(nRemaining);
do {
      // Identify next run
    int runLen = countRunAndMakeAscending(a, lo, hi, c);

    // If run is short, extend to min(minRun, nRemaining)
        if (runLen < minRun) {
              int force = nRemaining <= minRun ? nRemaining : minRun;
            binarySort(a, lo, lo + force, lo + runLen, c);
            runLen = force;
        }

    // Push run onto pending-run stack, and maybe merge
        ts.pushRun(lo, runLen);
        ts.mergeCollapse();

    // Advance to find next run
        lo += runLen;
        nRemaining -= runLen;
    } while (nRemaining != 0);
```