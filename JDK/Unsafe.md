# Unsafe

## 1.Java源码

```java
/**
 * This class should provide access to low-level operations and its
 * use should be limited to trusted code.  Fields can be accessed using
 * memory addresses, with undefined behaviour occurring if invalid memory
 * addresses are given.
 * 这个类提供了一个更底层的操作并且应该在受信任的代码中使用。可以通过内存地址
 * 存取fields,如果给出的内存地址是无效的那么会有一个不确定的运行表现。
 *
 * @author Tom Tromey (tromey@redhat.com)
 * @author Andrew John Hughes (gnu_andrew@member.fsf.org)
 */
public class Unsafe {
    // Singleton class.
    private static Unsafe unsafe = new Unsafe();
 
    /**
     * Private default constructor to prevent creation of an arbitrary
     * number of instances.
     * 使用私有默认构造器防止创建多个实例
     */
    private Unsafe() {
    }
 
    /**
     * Retrieve the singleton instance of <code>Unsafe</code>.  The calling
     * method should guard this instance from untrusted code, as it provides
     * access to low-level operations such as direct memory access.
     * 获取<code>Unsafe</code>的单例,这个方法调用应该防止在不可信的代码中实例，
     * 因为unsafe类提供了一个低级别的操作，例如直接内存存取。
     *
     * @throws SecurityException if a security manager exists and prevents
     *                           access to the system properties.
     *                           如果安全管理器不存在或者禁止访问系统属性
     */
    public static Unsafe getUnsafe() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null)
            sm.checkPropertiesAccess();
        return unsafe;
    }
 
    /**
     * Returns the memory address offset of the given static field.
     * The offset is merely used as a means to access a particular field
     * in the other methods of this class.  The value is unique to the given
     * field and the same value should be returned on each subsequent call.
     * 返回指定静态field的内存地址偏移量,在这个类的其他方法中这个值只是被用作一个访问
     * 特定field的一个方式。这个值对于 给定的field是唯一的，并且后续对该方法的调用都应该
     * 返回相同的值。
     *
     * @param field the field whose offset should be returned.
     *              需要返回偏移量的field
     * @return the offset of the given field.
     *         指定field的偏移量
     */
    public native long objectFieldOffset(Field field);
 
    /**
     * Compares the value of the integer field at the specified offset
     * in the supplied object with the given expected value, and updates
     * it if they match.  The operation of this method should be atomic,
     * thus providing an uninterruptible way of updating an integer field.
     * 在obj的offset位置比较integer field和期望的值，如果相同则更新。这个方法
     * 的操作应该是原子的，因此提供了一种不可中断的方式更新integer field。
     *
     * @param obj    the object containing the field to modify.
     *               包含要修改field的对象
     * @param offset the offset of the integer field within <code>obj</code>.
     *               <code>obj</code>中整型field的偏移量
     * @param expect the expected value of the field.
     *               希望field中存在的值
     * @param update the new value of the field if it equals <code>expect</code>.
     *               如果期望值expect与field的当前值相同，设置filed的值为这个新值
     * @return true if the field was changed.
     *         如果field的值被更改
     */
    public native boolean compareAndSwapInt(Object obj, long offset,
                                            int expect, int update);
 
    /**
     * Compares the value of the long field at the specified offset
     * in the supplied object with the given expected value, and updates
     * it if they match.  The operation of this method should be atomic,
     * thus providing an uninterruptible way of updating a long field.
     * 在obj的offset位置比较long field和期望的值，如果相同则更新。这个方法
     * 的操作应该是原子的，因此提供了一种不可中断的方式更新long field。
     *
     * @param obj    the object containing the field to modify.
     *               包含要修改field的对象
     * @param offset the offset of the long field within <code>obj</code>.
     *               <code>obj</code>中long型field的偏移量
     * @param expect the expected value of the field.
     *               希望field中存在的值
     * @param update the new value of the field if it equals <code>expect</code>.
     *               如果期望值expect与field的当前值相同，设置filed的值为这个新值
     * @return true if the field was changed.
     *         如果field的值被更改
     */
    public native boolean compareAndSwapLong(Object obj, long offset,
                                             long expect, long update);
 
    /**
     * Compares the value of the object field at the specified offset
     * in the supplied object with the given expected value, and updates
     * it if they match.  The operation of this method should be atomic,
     * thus providing an uninterruptible way of updating an object field.
     * 在obj的offset位置比较object field和期望的值，如果相同则更新。这个方法
     * 的操作应该是原子的，因此提供了一种不可中断的方式更新object field。
     *
     * @param obj    the object containing the field to modify.
     *               包含要修改field的对象
     * @param offset the offset of the object field within <code>obj</code>.
     *               <code>obj</code>中object型field的偏移量
     * @param expect the expected value of the field.
     *               希望field中存在的值
     * @param update the new value of the field if it equals <code>expect</code>.
     *               如果期望值expect与field的当前值相同，设置filed的值为这个新值
     * @return true if the field was changed.
     *         如果field的值被更改
     */
    public native boolean compareAndSwapObject(Object obj, long offset,
                                               Object expect, Object update);
 
    /**
     * Sets the value of the integer field at the specified offset in the
     * supplied object to the given value.  This is an ordered or lazy
     * version of <code>putIntVolatile(Object,long,int)</code>, which
     * doesn't guarantee the immediate visibility of the change to other
     * threads.  It is only really useful where the integer field is
     * <code>volatile</code>, and is thus expected to change unexpectedly.
     * 设置obj对象中offset偏移地址对应的整型field的值为指定值。这是一个有序或者
     * 有延迟的<code>putIntVolatile</cdoe>方法，并且不保证值的改变被其他线程立
     * 即看到。只有在field被<code>volatile</code>修饰并且期望被意外修改的时候
     * 使用才有用。
     *
     * @param obj    the object containing the field to modify.
     *               包含需要修改field的对象
     * @param offset the offset of the integer field within <code>obj</code>.
     *               <code>obj</code>中整型field的偏移量
     * @param value  the new value of the field.
     *               field将被设置的新值
     * @see #putIntVolatile(Object, long, int)
     */
    public native void putOrderedInt(Object obj, long offset, int value);
 
    /**
     * Sets the value of the long field at the specified offset in the
     * supplied object to the given value.  This is an ordered or lazy
     * version of <code>putLongVolatile(Object,long,long)</code>, which
     * doesn't guarantee the immediate visibility of the change to other
     * threads.  It is only really useful where the long field is
     * <code>volatile</code>, and is thus expected to change unexpectedly.
     * 设置obj对象中offset偏移地址对应的long型field的值为指定值。这是一个有序或者
     * 有延迟的<code>putLongVolatile</cdoe>方法，并且不保证值的改变被其他线程立
     * 即看到。只有在field被<code>volatile</code>修饰并且期望被意外修改的时候
     * 使用才有用。
     *
     * @param obj    the object containing the field to modify.
     *               包含需要修改field的对象
     * @param offset the offset of the long field within <code>obj</code>.
     *               <code>obj</code>中long型field的偏移量
     * @param value  the new value of the field.
     *               field将被设置的新值
     * @see #putLongVolatile(Object, long, long)
     */
    public native void putOrderedLong(Object obj, long offset, long value);
 
    /**
     * Sets the value of the object field at the specified offset in the
     * supplied object to the given value.  This is an ordered or lazy
     * version of <code>putObjectVolatile(Object,long,Object)</code>, which
     * doesn't guarantee the immediate visibility of the change to other
     * threads.  It is only really useful where the object field is
     * <code>volatile</code>, and is thus expected to change unexpectedly.
     * 设置obj对象中offset偏移地址对应的object型field的值为指定值。这是一个有序或者
     * 有延迟的<code>putObjectVolatile</cdoe>方法，并且不保证值的改变被其他线程立
     * 即看到。只有在field被<code>volatile</code>修饰并且期望被意外修改的时候
     * 使用才有用。
     *
     * @param obj    the object containing the field to modify.
     *               包含需要修改field的对象
     * @param offset the offset of the object field within <code>obj</code>.
     *               <code>obj</code>中long型field的偏移量
     * @param value  the new value of the field.
     *               field将被设置的新值
     */
    public native void putOrderedObject(Object obj, long offset, Object value);
 
    /**
     * Sets the value of the integer field at the specified offset in the
     * supplied object to the given value, with volatile store semantics.
     * 设置obj对象中offset偏移地址对应的整型field的值为指定值。支持volatile store语义
     *
     * @param obj    the object containing the field to modify.
     *               包含需要修改field的对象
     * @param offset the offset of the integer field within <code>obj</code>.
     *               <code>obj</code>中整型field的偏移量
     * @param value  the new value of the field.
     *               field将被设置的新值
     */
    public native void putIntVolatile(Object obj, long offset, int value);
 
    /**
     * Retrieves the value of the integer field at the specified offset in the
     * supplied object with volatile load semantics.
     * 获取obj对象中offset偏移地址对应的整型field的值,支持volatile load语义。
     *
     * @param obj    the object containing the field to read.
     *               包含需要去读取的field的对象
     * @param offset the offset of the integer field within <code>obj</code>.
     *               <code>obj</code>中整型field的偏移量
     */
    public native int getIntVolatile(Object obj, long offset);
 
    /**
     * Sets the value of the long field at the specified offset in the
     * supplied object to the given value, with volatile store semantics.
     * 设置obj对象中offset偏移地址对应的long型field的值为指定值。支持volatile store语义
     *
     * @param obj    the object containing the field to modify.
     *               包含需要修改field的对象
     * @param offset the offset of the long field within <code>obj</code>.
     *               <code>obj</code>中long型field的偏移量
     * @param value  the new value of the field.
     *               field将被设置的新值
     * @see #putLong(Object, long, long)
     */
    public native void putLongVolatile(Object obj, long offset, long value);
 
    /**
     * Sets the value of the long field at the specified offset in the
     * supplied object to the given value.
     * 设置obj对象中offset偏移地址对应的long型field的值为指定值。
     *
     * @param obj    the object containing the field to modify.
     *               包含需要修改field的对象
     * @param offset the offset of the long field within <code>obj</code>.
     *               <code>obj</code>中long型field的偏移量
     * @param value  the new value of the field.
     *               field将被设置的新值
     * @see #putLongVolatile(Object, long, long)
     */
    public native void putLong(Object obj, long offset, long value);
 
    /**
     * Retrieves the value of the long field at the specified offset in the
     * supplied object with volatile load semantics.
     * 获取obj对象中offset偏移地址对应的long型field的值,支持volatile load语义。
     *
     * @param obj    the object containing the field to read.
     *               包含需要去读取的field的对象
     * @param offset the offset of the long field within <code>obj</code>.
     *               <code>obj</code>中long型field的偏移量
     * @see #getLong(Object, long)
     */
    public native long getLongVolatile(Object obj, long offset);
 
    /**
     * Retrieves the value of the long field at the specified offset in the
     * supplied object.
     * 获取obj对象中offset偏移地址对应的long型field的值
     *
     * @param obj    the object containing the field to read.
     *               包含需要去读取的field的对象
     * @param offset the offset of the long field within <code>obj</code>.
     *               <code>obj</code>中long型field的偏移量
     * @see #getLongVolatile(Object, long)
     */
    public native long getLong(Object obj, long offset);
 
    /**
     * Sets the value of the object field at the specified offset in the
     * supplied object to the given value, with volatile store semantics.
     * 设置obj对象中offset偏移地址对应的object型field的值为指定值。支持volatile store语义
     *
     * @param obj    the object containing the field to modify.
     *               包含需要修改field的对象
     * @param offset the offset of the object field within <code>obj</code>.
     *               <code>obj</code>中object型field的偏移量
     * @param value  the new value of the field.
     *               field将被设置的新值
     * @see #putObject(Object, long, Object)
     */
    public native void putObjectVolatile(Object obj, long offset, Object value);
 
    /**
     * Sets the value of the object field at the specified offset in the
     * supplied object to the given value.
     * 设置obj对象中offset偏移地址对应的object型field的值为指定值。
     *
     * @param obj    the object containing the field to modify.
     *               包含需要修改field的对象
     * @param offset the offset of the object field within <code>obj</code>.
     *               <code>obj</code>中object型field的偏移量
     * @param value  the new value of the field.
     *               field将被设置的新值
     * @see #putObjectVolatile(Object, long, Object)
     */
    public native void putObject(Object obj, long offset, Object value);
 
    /**
     * Retrieves the value of the object field at the specified offset in the
     * supplied object with volatile load semantics.
     * 获取obj对象中offset偏移地址对应的object型field的值,支持volatile load语义。
     *
     * @param obj    the object containing the field to read.
     *               包含需要去读取的field的对象
     * @param offset the offset of the object field within <code>obj</code>.
     *               <code>obj</code>中object型field的偏移量
     */
    public native Object getObjectVolatile(Object obj, long offset);
 
    /**
     * Returns the offset of the first element for a given array class.
     * To access elements of the array class, this value may be used along with
     * with that returned by
     * <a href="#arrayIndexScale"><code>arrayIndexScale</code></a>,
     * if non-zero.
     * 获取给定数组中第一个元素的偏移地址。
     * 为了存取数组中的元素，这个偏移地址与<a href="#arrayIndexScale"><code>arrayIndexScale
     * </code></a>方法的非0返回值一起被使用。
     *
     * @param arrayClass the class for which the first element's address should
     *                   be obtained.
     *                   第一个元素地址被获取的class
     * @return the offset of the first element of the array class.
     *         数组第一个元素 的偏移地址
     * @see arrayIndexScale(Class)
     */
    public native int arrayBaseOffset(Class arrayClass);
 
    /**
     * Returns the scale factor used for addressing elements of the supplied
     * array class.  Where a suitable scale factor can not be returned (e.g.
     * for primitive types), zero should be returned.  The returned value
     * can be used with
     * <a href="#arrayBaseOffset"><code>arrayBaseOffset</code></a>
     * to access elements of the class.
     * 获取用户给定数组寻址的换算因子.一个合适的换算因子不能返回的时候(例如：基本类型),
     * 返回0.这个返回值能够与<a href="#arrayBaseOffset"><code>arrayBaseOffset</code>
     * </a>一起使用去存取这个数组class中的元素
     *
     * @param arrayClass the class whose scale factor should be returned.
     * @return the scale factor, or zero if not supported for this array class.
     */
    public native int arrayIndexScale(Class arrayClass);
 
    /**
     * Releases the block on a thread created by
     * <a href="#park"><code>park</code></a>.  This method can also be used
     * to terminate a blockage caused by a prior call to <code>park</code>.
     * This operation is unsafe, as the thread must be guaranteed to be
     * live.  This is true of Java, but not native code.
     * 释放被<a href="#park"><code>park</code></a>创建的在一个线程上的阻塞.这个
     * 方法也可以被使用来终止一个先前调用<code>park</code>导致的阻塞.
     * 这个操作操作时不安全的,因此线程必须保证是活的.这是java代码不是native代码。
     *
     * @param thread the thread to unblock.
     *               要解除阻塞的线程
     */
    public native void unpark(Thread thread);
 
    /**
     * Blocks the thread until a matching
     * <a href="#unpark"><code>unpark</code></a> occurs, the thread is
     * interrupted or the optional timeout expires.  If an <code>unpark</code>
     * call has already occurred, this also counts.  A timeout value of zero
     * is defined as no timeout.  When <code>isAbsolute</code> is
     * <code>true</code>, the timeout is in milliseconds relative to the
     * epoch.  Otherwise, the value is the number of nanoseconds which must
     * occur before timeout.  This call may also return spuriously (i.e.
     * for no apparent reason).
     * 阻塞一个线程直到<a href="#unpark"><code>unpark</code></a>出现、线程
     * 被中断或者timeout时间到期。如果一个<code>unpark</code>调用已经出现了，
     * 这里只计数。timeout为0表示永不过期.当<code>isAbsolute</code>为true时，
     * timeout是相对于新纪元之后的毫秒。否则这个值就是超时前的纳秒数。这个方法执行时
     * 也可能不合理地返回(没有具体原因)
     *
     * @param isAbsolute true if the timeout is specified in milliseconds from
     *                   the epoch.
     *                   如果为true timeout的值是一个相对于新纪元之后的毫秒数
     * @param time       either the number of nanoseconds to wait, or a time in
     *                   milliseconds from the epoch to wait for.
     *                   可以是一个要等待的纳秒数，或者是一个相对于新纪元之后的毫秒数直到
     *                   到达这个时间点
     */
    public native void park(boolean isAbsolute, long time);
}

```

## 2.C++源码

```C++
#include <gcj/cni.h>
#include <gcj/field.h>
#include <gcj/javaprims.h>
#include <jvm.h>
#include <sun/misc/Unsafe.h>
#include <java/lang/System.h>
#include <java/lang/InterruptedException.h>
 
#include <java/lang/Thread.h>
#include <java/lang/Long.h>
 
#include "sysdep/locks.h"
 
// Use a spinlock for multi-word accesses
class spinlock
{
  static volatile obj_addr_t lock;
 
public:
 
spinlock ()
  {
    while (! compare_and_swap (&lock, 0, 1))
      _Jv_ThreadYield ();
  }
  ~spinlock ()
  {
    release_set (&lock, 0);
  }
};
 
// This is a single lock that is used for all synchronized accesses if
// the compiler can't generate inline compare-and-swap operations.  In
// most cases it'll never be used, but the i386 needs it for 64-bit
// locked accesses and so does PPC32.  It's worth building libgcj with
// target=i486 (or above) to get the inlines.
volatile obj_addr_t spinlock::lock;
 
 
static inline bool
compareAndSwap (volatile jint *addr, jint old, jint new_val)
{
  jboolean result = false;
  spinlock lock;
  if ((result = (*addr == old)))
    *addr = new_val;
  return result;
}
 
static inline bool
compareAndSwap (volatile jlong *addr, jlong old, jlong new_val)
{
  jboolean result = false;
  spinlock lock;
  if ((result = (*addr == old)))
    *addr = new_val;
  return result;
}
 
static inline bool
compareAndSwap (volatile jobject *addr, jobject old, jobject new_val)
{
  jboolean result = false;
  spinlock lock;
  if ((result = (*addr == old)))
    *addr = new_val;
  return result;
}
 
 
jlong
sun::misc::Unsafe::objectFieldOffset (::java::lang::reflect::Field *field)
{
  _Jv_Field *fld = _Jv_FromReflectedField (field);
  // FIXME: what if it is not an instance field?
  return fld->getOffset();
}
 
jint
sun::misc::Unsafe::arrayBaseOffset (jclass arrayClass)
{
  // FIXME: assert that arrayClass is array.
  jclass eltClass = arrayClass->getComponentType();
  return (jint)(jlong) _Jv_GetArrayElementFromElementType (NULL, eltClass);
}
 
jint
sun::misc::Unsafe::arrayIndexScale (jclass arrayClass)
{
  // FIXME: assert that arrayClass is array.
  jclass eltClass = arrayClass->getComponentType();
  if (eltClass->isPrimitive())
    return eltClass->size();
  return sizeof (void *);
}
 
// These methods are used when the compiler fails to generate inline
// versions of the compare-and-swap primitives.
 
jboolean
sun::misc::Unsafe::compareAndSwapInt (jobject obj, jlong offset,
                                           jint expect, jint update)
{
  jint *addr = (jint *)((char *)obj + offset);
  return compareAndSwap (addr, expect, update);
}
 
jboolean
sun::misc::Unsafe::compareAndSwapLong (jobject obj, jlong offset,
                                            jlong expect, jlong update)
{
  volatile jlong *addr = (jlong*)((char *) obj + offset);
  return compareAndSwap (addr, expect, update);
}
 
jboolean
sun::misc::Unsafe::compareAndSwapObject (jobject obj, jlong offset,
                                                jobject expect, jobject update)
{
  jobject *addr = (jobject*)((char *) obj + offset);
  return compareAndSwap (addr, expect, update);
}
 
void
sun::misc::Unsafe::putOrderedInt (jobject obj, jlong offset, jint value)
{
  volatile jint *addr = (jint *) ((char *) obj + offset);
  *addr = value;
}
 
void
sun::misc::Unsafe::putOrderedLong (jobject obj, jlong offset, jlong value)
{
  volatile jlong *addr = (jlong *) ((char *) obj + offset);
  spinlock lock;
  *addr = value;
}
 
void
sun::misc::Unsafe::putOrderedObject (jobject obj, jlong offset, jobject value)
{
  volatile jobject *addr = (jobject *) ((char *) obj + offset);
  *addr = value;
}
 
void
sun::misc::Unsafe::putIntVolatile (jobject obj, jlong offset, jint value)
{
  write_barrier ();
  volatile jint *addr = (jint *) ((char *) obj + offset);
  *addr = value;
}
 
void
sun::misc::Unsafe::putLongVolatile (jobject obj, jlong offset, jlong value)
{
  volatile jlong *addr = (jlong *) ((char *) obj + offset);
  spinlock lock;
  *addr = value;
}
 
void
sun::misc::Unsafe::putObjectVolatile (jobject obj, jlong offset, jobject value)
{
  write_barrier ();
  volatile jobject *addr = (jobject *) ((char *) obj + offset);
  *addr = value;
}
 
#if 0  // FIXME
void
sun::misc::Unsafe::putInt (jobject obj, jlong offset, jint value)
{
  jint *addr = (jint *) ((char *) obj + offset);
  *addr = value;
}
#endif
 
void
sun::misc::Unsafe::putLong (jobject obj, jlong offset, jlong value)
{
  jlong *addr = (jlong *) ((char *) obj + offset);
  spinlock lock;
  *addr = value;
}
 
void
sun::misc::Unsafe::putObject (jobject obj, jlong offset, jobject value)
{
  jobject *addr = (jobject *) ((char *) obj + offset);
  *addr = value;
}
 
jint
sun::misc::Unsafe::getIntVolatile (jobject obj, jlong offset)
{
  volatile jint *addr = (jint *) ((char *) obj + offset);
  jint result = *addr;
  read_barrier ();
  return result;
}
 
jobject
sun::misc::Unsafe::getObjectVolatile (jobject obj, jlong offset)
{
  volatile jobject *addr = (jobject *) ((char *) obj + offset);
  jobject result = *addr;
  read_barrier ();
  return result;
}
 
jlong
sun::misc::Unsafe::getLong (jobject obj, jlong offset)
{
  jlong *addr = (jlong *) ((char *) obj + offset);
  spinlock lock;
  return *addr;
}
 
jlong
sun::misc::Unsafe::getLongVolatile (jobject obj, jlong offset)
{
  volatile jlong *addr = (jlong *) ((char *) obj + offset);
  spinlock lock;
  return *addr;
}
 
void
sun::misc::Unsafe::unpark (::java::lang::Thread *thread)
{
  natThread *nt = (natThread *) thread->data;
  nt->park_helper.unpark ();
}
 
void
sun::misc::Unsafe::park (jboolean isAbsolute, jlong time)
{
  using namespace ::java::lang;
  Thread *thread = Thread::currentThread();
  natThread *nt = (natThread *) thread->data;
  nt->park_helper.park (isAbsolute, time);
}

```