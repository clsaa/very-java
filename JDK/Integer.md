# Integer

## 1.概述

### 1.1.基本特性

Integer 类在对象中包装了一个基本类型 int 的值。Integer 类型的对象包含一个 int 类型的字段。

此外，该类提供了多个方法，能在 int 类型和 String 类型之间互相转换，还提供了处理 int 类型时非常有用的其他一些常量和方法。

### 1.2.类结构

```java
public final class Integer extends Number implements Comparable<Integer>
```

![image](https://clsaa-markdown-imgbed-1252032169.cos.ap-shanghai.myqcloud.com/very-java/2019-03-13-074837.png)

1. Integer类不能被继承
2. Integer类实现了Comparable接口，所以可以用compareTo进行比较并且Integer对象只能和Integer类型的对象进行比较，不能和其他类型比较（至少调用compareTo方法无法比较）。
3. Integer继承了Number类，所以该类可以调用longValue、floatValue、doubleValue等系列方法返回对应的类型的值。

## 2.属性

### 2.1.私有属性

```java
private final int value;
private static final long serialVersionUID = 1360826667806852920L;
```

* serialVersionUID和序列化有关。
* 还有一个私有属性——value属性就是Integer对象中真正保存int值的。当我们使用new Integer(10)创建一个Integer对象的时候，就会用以下形式给value赋值。还有其他的构造函数在后面会讲。

```java
public Integer(int value) {
    this.value = value;
}
```

* 这里我们讨论一下Interger对象的可变性。从value的定义形式中可以看出value被定义成final类型。也就说明，一旦一个Integer对象被初始化之后，就无法再改变value的值。那么这里就深入讨论一下以下代码的逻辑：

```java

public class IntegerTest {
    public static void main(String[] args) {
        Integer i = new Integer(10);
        i = 5;
    }
}
```

在以上代码中，首先调用构造函数new一个Integer对象，给私有属性value赋值，这时value=10,接下来使用i=5的形式试图改变i的值。有一点开发经验的同学都知道，这个时候如果使用变量i，那么它的值一定是5，那么i=5这个赋值操作到底做了什么呢？到底是如何改变i的值的呢？是改变了原有对象i中value的值还是重新创建了一个新的Integer对象呢？我们将上面的代码进行反编译，反编译之后的代码如下：

```java
public class IntegerTest
{
    public static void main(String args[])
    {
        Integer i = new Integer(10);
        i = Integer.valueOf(5);
    }
}
```

通过看反编译之后的代码我们发现，编译器会把i=5转成i = Integer.valueOf(5);这里先直接给出结论，i=5操作并没有改变使用Integer i = new Integer(10);创建出来的i中的value属性的值。要么是直接返回一个已有对象，要么新建一个对象。这里的具体实现细节在后面讲解valueOf方法的时候给出。

### 2.2.类属性

```java
//值为 （－（2的31次方）） 的常量，它表示 int 类型能够表示的最小值。
public static final int   MIN_VALUE = 0x80000000;
//值为 （（2的31次方）－1） 的常量，它表示 int 类型能够表示的最大值。
public static final int   MAX_VALUE = 0x7fffffff;   
//表示基本类型 int 的 Class 实例。
public static final Class<Integer>  TYPE = (Class<Integer>) Class.getPrimitiveClass("int");
//用来以二进制补码形式表示 int 值的比特位数。
public static final int SIZE = 32;
//用来以二进制补码形式表示 int 值的字节数。1.8以后才有
public static final int BYTES = SIZE / Byte.SIZE;

```

以上属性可直接使用，因为他们已经定义成publis static fianl能用的时候尽量使用他们，这样不仅能使代码有很好的可读性，也能提高性能节省资源。

### 2.3.静态内部类

```java
  /**
     * Cache to support the object identity semantics of autoboxing for values between
     * -128 and 127 (inclusive) as required by JLS.
     *
     * The cache is initialized on first usage.  The size of the cache
     * may be controlled by the {@code -XX:AutoBoxCacheMax=<size>} option.
     * During VM initialization, java.lang.Integer.IntegerCache.high property
     * may be set and saved in the private system properties in the
     * sun.misc.VM class.
     */

    private static class IntegerCache {
        static final int low = -128;
        static final int high;
        static final Integer cache[];

        static {
            // high value may be configured by property
            int h = 127;
            String integerCacheHighPropValue =
                sun.misc.VM.getSavedProperty("java.lang.Integer.IntegerCache.high");
            if (integerCacheHighPropValue != null) {
                try {
                    int i = parseInt(integerCacheHighPropValue);
                    i = Math.max(i, 127);
                    // Maximum array size is Integer.MAX_VALUE
                    h = Math.min(i, Integer.MAX_VALUE - (-low) -1);
                } catch( NumberFormatException nfe) {
                    // If the property cannot be parsed into an int, ignore it.
                }
            }
            high = h;

            cache = new Integer[(high - low) + 1];
            int j = low;
            for(int k = 0; k < cache.length; k++)
                cache[k] = new Integer(j++);

            // range [-128, 127] must be interned (JLS7 5.1.7)
            assert IntegerCache.high >= 127;
        }

        private IntegerCache() {}
    }
```

也就是说，当Integer被加载时，就新建了-128到127的所有数字并存放在Integer数组cache中。

## 3.方法

### 3.1.构造方法

```java
//构造一个新分配的 Integer 对象，它表示指定的 int 值。
public Integer(int value) {
    this.value = value;
}

//构造一个新分配的 Integer 对象，它表示 String 参数所指示的 int 值。
public Integer(String s) throws NumberFormatException {
    this.value = parseInt(s, 10);
}
```

从构造方法中我们可以知道，初始化一个Integer对象的时候只能创建一个十进制的整数。

### 3.2.valueOf(int i)

前面说到Integer中私有属性value的时候提到

```java
Integer i = new Integer(10);
i = 5;
```

其中i=5操作时，编译器会转成i = Integer.valueOf(5);执行。那么这里就解释一下valueOf(int i)方法是如何给变量赋值的。

```java
public static Integer valueOf(int i) {
    if (i >= IntegerCache.low && i <= IntegerCache.high)
        return IntegerCache.cache[i + (-IntegerCache.low)];
    return new Integer(i);
}
```

以上是valueOf方法的实现细节。通常情况下，IntegerCache.low=-128，IntegerCache.high=127（除非显示声明java.lang.Integer.IntegerCache.high的值），Integer中有一段动态代码块，该部分内容会在Integer类被加载的时候就执行。

可以得出结论。当调用valueOf方法（包括后面会提到的重载的参数类型包含String的valueOf方法）时，如果参数的值在-127到128之间，则直接从缓存中返回一个已经存在的对象。如果参数的值不在这个范围内，则new一个Integer对象返回。

所以，当把一个int变量转成Integer的时候（或者新建一个Integer的时候），建议使用valueOf方法来代替构造函数。或者直接使用Integer i = 100;编译器会转成Integer s = Integer.valueOf(10000);

### 3.3.String转Integer/int的方法

```java
Integer getInteger(String nm)
Integer getInteger(String nm, int val)
Integer getInteger(String nm, Integer val)
Integer decode(String nm)
Integer valueOf(String s)
Integer valueOf(String s, int radix)
int parseUnsignedInt(String s)
int parseUnsignedInt(String s, int radix)
int parseInt(String s)
int parseInt(String s, int radix)
```

以上所有方法都能实现将String类型的值转成Integer(int)类型（如果 String 不包含可解析整数将抛出NumberFormatException）

可以说，所有将String转成Integer的方法都是基于parseInt方法实现的。简单看一下以上部分方法的调用栈。

```java
getInteger(String nm) ---> getInteger(nm, null);--->Integer.decode()--->Integer.valueOf()--->parseInt()
```

### 3.3.1.getInteger

确定具有指定名称的系统属性的整数值。 第一个参数被视为系统属性的名称。通过System.getProperty(java.lang.String) 方法可以访问系统属性。然后，将该属性的字符串值解释为一个整数值，并返回表示该值的 Integer 对象。使用 getProperty 的定义可以找到可能出现的数字格式的详细信息。其中参数nm应该在System的props中可以找到。这个方法在日常编码中很好是用到。在代码中可以用以下形式使用该方法：

```java
Properties props = System.getProperties();
props.put("hollis.integer.test.key","10000");
Integer i = Integer.getInteger("hollis.integer.test.key");
System.out.println(i);
//输出 10000
```

另外两个方法

```java
getInteger(String nm,int val)
getInteger(String nm, Integer val)
```

第二个参数是默认值。如果未具有指定名称的属性，或者属性的数字格式不正确，或者指定名称为空或 null，则返回默认值。getInteger的具体实现细节如下：

```java
public static Integer getInteger(String nm, Integer val) {
  String v = null;
  try {
      v = System.getProperty(nm);
  } catch (IllegalArgumentException | NullPointerException e) {
  }
  if (v != null) {
      try {
          return Integer.decode(v);
      } catch (NumberFormatException e) {
      }
  }
  return val;
}
```

先按照nm作为key从系统配置中取出值，然后调用Integer.decode方法将其转换成整数并返回。

### 3.3.2.decode()

```java
public static Integer decode(String nm) throws NumberFormatException
```

该方法的作用是将 String 解码为 Integer。接受十进制、十六进制和八进制数字。

根据要解码的 String（mn)的形式转成不同进制的数字。 mn由三部分组成：符号、基数说明符和字符序列。 —0X123中-是符号位，0X是基数说明符（0表示八进制，0x,0X，#表示十六进制，什么都不写则表示十进制），123是数字字符序列。

使用例子举例如下：

```java
Integer DecimalI = Integer.decode("+10");
Integer OctI = Integer.decode("-010");
Integer HexI = Integer.decode("-0x10");
Integer HexI1 = Integer.decode("#10");
System.out.println(DecimalI);
System.out.println(OctI);
System.out.println(HexI);
System.out.println(HexI1);
//10 -8 -16 16
```

decode方法的具体实现也比较简单，首先就是判断String类型的参数mn是否以(+/—)符号开头。然后再依次判断是否以”0x”、“#”、“0”开头，确定基数说明符的值。然后将字符串mn进行截取，只保留其中纯数字部分。在用截取后的纯数字和基数调用valueOf(String s, int radix)方法并返回其值。

### 3.3.3.valueOf()

```java
public static Integer valueOf(String s) throws NumberFormatException 
public static int parseInt(String s, int radix) throws NumberFormatException
```

返回一个 Integer 对象。如果指定第二个参数radix，将第一个参数解释为用第二个参数指定的基数表示的有符号整数。如果没指定则按照十进制进行处理。

该方法实现非常简单：

```java
public static Integer valueOf(String s) throws NumberFormatException {
     return Integer.valueOf(parseInt(s, 10));
}

public static Integer valueOf(String s, int radix) throws NumberFormatException {
    return Integer.valueOf(parseInt(s,radix));
}

```

主要用到了两个方法，parseInt(String s, int radix)和valueOf(int i)方法。前面已经讲过valueOf方法会检查参数内容是否在-127到128之间，如果是则直接返回。否则才会新建一个对象。

#### 3.3.4.parseInt()

```java
public static int parseInt(String s) throws NumberFormatException {
    return parseInt(s,10);
}

public static int parseInt(String s, int radix) throws NumberFormatException
```

使用第二个参数指定的基数(如果没指定，则按照十进制处理），将字符串参数解析为有符号的整数。除了第一个字符可以是用来表示负值的 ASCII 减号 ‘-‘ (‘\u002D’)外，字符串中的字符必须都是指定基数的数字（通过 Character.digit(char, int) 是否返回一个负值确定）。返回得到的整数值。

如果发生以下任意一种情况，则抛出一个 NumberFormatException 类型的异常：

* 第一个参数为 null 或一个长度为零的字符串。
* 基数小于 Character.MIN_RADIX 或者大于 Character.MAX_RADIX。
* 假如字符串的长度超过 1，那么除了第一个字符可以是减号 ‘-‘ (‘u002D’) 外，字符串中存在任意不是由指定基数的数字表示的字符.
* 字符串表示的值不是 int 类型的值。

```java
parseInt("0", 10) 返回 0
parseInt("473", 10) 返回 473
parseInt("-0", 10) 返回 0
parseInt("-FF", 16) 返回 -255
parseInt("1100110", 2) 返回 102
parseInt("2147483647", 10) 返回 2147483647
parseInt("-2147483648", 10) 返回 -2147483648
parseInt("2147483648", 10) 抛出 NumberFormatException
parseInt("99", 8) 抛出 NumberFormatException
parseInt("Hollis", 10) 抛出 NumberFormatException
parseInt("Hollis", 27) 返回 411787
```

该方法的具体实现方式也比较简单，主要逻辑代码（省略部分参数校验）如下：

```java
while (i < len) {
  // Accumulating negatively avoids surprises near MAX_VALUE
  digit = Character.digit(s.charAt(i++),radix);
  if (digit < 0) {
      throw NumberFormatException.forInputString(s);
  }
  if (result < multmin) {
      throw NumberFormatException.forInputString(s);
  }
  result *= radix;
  if (result < limit + digit) {
  throw NumberFormatException.forInputString(s);
  }
  result -= digit;
}
```

主要思想其实也很好理解。

“12345”按照十进制转成12345的方法其实就是以下方式： （（1*10）+2）*10）+3）*10+4）*10+5 具体的如何依次取出“12345”中的每一个字符并将起转成不同进制int类型则是Character.digit方法实现的，这里就不深入讲解了。

#### 3.3.5.总结

1. parseInt方法返回的是基本类型int
2. 其他的方法返回的是Integer
3. valueOf（String）方法会调用valueOf(int)方法。

如果只需要返回一个基本类型，而不需要一个对象，可以直接使用Integert.parseInt("123");

如果需要一个对象，那么建议使用valueOf(),因为该方法可以借助缓存带来的好处。

如果和进制有关，那么就是用decode方法。

如果是从系统配置中取值，那么就是用getInteger

### 3.4.int转String的方法

```java
String  toString()
static String   toString(int i)
static String   toString(int i, int radix)
static String   toBinaryString(int i)
static String   toHexString(int i)
static String   toOctalString(int i)
static String   toUnsignedString(int i)
static String   toUnsignedString(int i, int radix)
```

#### 3.4.1.toString

直接看toString方法，toString方法的定义比较简单，就是把一个int类型的数字转换成字符串类型，但是这个方法的实现调用了一系列方法，通过阅读这个方法，你就会对sun公司的程序员产生油然的敬佩。

```java
public static String toString(int i) {
    if (i == Integer.MIN_VALUE)
        return "-2147483648";
    int size = (i < 0) ? stringSize(-i) + 1 : stringSize(i);
    char[] buf = new char[size];
    getChars(i, size, buf);
    return new String(buf, true);
}
```

* 片段一

```java
if (i == Integer.MIN_VALUE)
        return "-2147483648";
```

这里先对i的值做检验，如果等于Int能表示的最小值，则直接返回最小值的字符串形式。那么为什么-2147483648要特殊处理呢？请看代码片段二的分析。

* 片段二

```java
int size = (i < 0) ? stringSize(-i) + 1 : stringSize(i);
char[] buf = new char[size];
```
这段代码的主要目的是体取出整数i的位数，并创建一个字符数组。 其中提取I的位数使用stringSize方法，这个方法实现如下：

```java
final static int [] sizeTable = { 9, 99, 999, 9999, 99999, 999999, 9999999, 99999999, 999999999, Integer.MAX_VALUE };

// Requires positive x
static int stringSize(int x) {
    for (int i=0; ; i++)
        if (x <= sizeTable[i])
            return i+1;
}
```

该方法要求传入一个正整数，如果传入的数字x的值是10000，那么因为他大于9，99，999，9999,小于99999.所以他会返回99999在整型数组sizeTable中的下标”4″+1 = 5。我们看10000这个数字的位数也确实是5。所以，就实现了返回一个正整数的位数。

设置size时，当i<0的时候返回的size数组在stringSize方法的基础上+1的目的是这一位用来存储负号。

由于stringSize方法要求传入一个正整数，所以代码片段二在调用该方法时需要将负数转成正数传入。代码片段一中，将-2147483648的值直接返回的原因就是整数最大只能表示2147483647，无法将stringSize(-i)中的i赋值成-2147483648。

getSize使用了的体系结构知识：

1. 局部性原理之空间局部性:sizeTable为数组，存储在相邻的位置，cpu一次加载一个块数据数据到cache中(多个数组数据)，此后访问sizeTable 不需要访问内存。
2. 基于范围的查找，是很实用的设计技术

* 片段三

```java
getChars(i, size, buf);
```

那么接下来就深入理解一下getChars方法。这部分我把关于这段代码的分析直接写到注释中，便于结合代码理解。

```java
static void getChars(int i, int index, char[] buf) {
    int q, r;
    int charPos = index;
    char sign = 0;

    if (i < 0) {
        sign = '-';
        i = -i;
    }

     // 每次循环过后，都会将i中的走后两位保存到字符数组buf中的最后两位中，读者可以将数字i设置为12345678测试一下， 
     //第一次循环结束之后，buf[7] = 8,buf[6]=7。第二次循环结束之后，buf[5] = 6,buf[4] = 5。
    while (i >= 65536) {
        q = i / 100;
    // really: r = i - (q * 100);
        r = i - ((q << 6) + (q << 5) + (q << 2));
        i = q;
        //取DigitOnes[r]的目的其实取数字r%10的结果
        buf [--charPos] = DigitOnes[r];
        //取DigitTens[r]的目的其实是取数字r/10的结果
        buf [--charPos] = DigitTens[r];
    }

    // Fall thru to fast mode for smaller numbers
    // assert(i <= 65536, i);
    //循环将其他数字存入字符数组中空余位置
    for (;;) {
          //这里其实就是除以10。取数52429和16+3的原因在后文分析。
        q = (i * 52429) >>> (16+3);
        // r = i-(q*10) ...
        r = i - ((q << 3) + (q << 1));   
        //将数字i的最后一位存入字符数组，
        //还是12345678那个例子，这个for循环第一次结束后，buf[3]=4。
        buf [--charPos] = digits [r];
        i = q;
        //for循环结束后，buf内容为“12345678”；
        if (i == 0) break;
    }
    if (sign != 0) {
        buf [--charPos] = sign;
    }}//其中用到的几个数组//100以内的数字除以10的结果（取整），//比如取DigitTens[78]，返回的是数字7//只要是70-79的数字，返回的都是7，依次类推，所以总结出规律，其实就是返回的对应数字除10取整的结果。finalstaticchar[]DigitTens={'0','0','0','0','0','0','0','0','0','0','1','1','1','1','1','1','1','1','1','1','2','2','2','2','2','2','2','2','2','2','3','3','3','3','3','3','3','3','3','3','4','4','4','4','4','4','4','4','4','4','5','5','5','5','5','5','5','5','5','5','6','6','6','6','6','6','6','6','6','6','7','7','7','7','7','7','7','7','7','7','8','8','8','8','8','8','8','8','8','8','9','9','9','9','9','9','9','9','9','9',};//100以内的数字对10取模的结果，//比如取DigitTens[78]，返回的8finalstaticchar[]DigitOnes={'0','1','2','3','4','5','6','7','8','9','0','1','2','3','4','5','6','7','8','9','0','1','2','3','4','5','6','7','8','9','0','1','2','3','4','5','6','7','8','9','0','1','2','3','4','5','6','7','8','9','0','1','2','3','4','5','6','7','8','9','0','1','2','3','4','5','6','7','8','9','0','1','2','3','4','5','6','7','8','9','0','1','2','3','4','5','6','7','8','9','0','1','2','3','4','5','6','7','8','9',};finalstaticchar[] digits ={'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'};
```

* 为什么在getChars方法中，将整型数字写入到字符数组的过程中为什么按照数字65536分成了两部分呢？这个65535是怎么来的？

* 部分一

```java
while (i >= num1) {
        q = i / 100;
    // really: r = i - (q * 100);
        r = i - ((q << 6) + (q << 5) + (q << 2));
        i = q;
        buf [--charPos] = DigitOnes[r];
        buf [--charPos] = DigitTens[r];
    }
```

* 部分二

```java
// Fall thru to fast mode for smaller numbers
    // assert(i <= 65536, i);
    for (;;) {
        q = (i * num2) >>> (num3);
        r = i - ((q << 3) + (q << 1));  // r = i-(q*10) ...
        buf [--charPos] = digits [r];
        i = q;
        if (i == 0) break;
    }
```

使用num1,num2,num3三个变量代替源代码中的数字，便于后面分析使用。

问题二、在上面两段代码的部分二中，在对i进行除十操作的过程中为什么选择先乘以52429在向右移位19位。其中52429和19是怎么来的？

* 回答上面两个问题之前，首先要明确两点：
  * 移位的效率比直接乘除的效率要高
  * 乘法的效率比除法的效率要高

先理解以下代码：

```java
r = i - ((q << 6) + (q << 5) + (q << 2));表示的其实是r = i - (q * 100);，i-q*2^6 - q*2^5 - q*2^2 = i-64q-32q-4q = i-100q。

q = (i * num2) >>> (num3);中，>>>表示无符号向右移位。代表的意义就是除以2^num3。 所以q = (i * 52429) >>> (16+3); 可以理解为：q = (i * 52429) / 524288;,那么就相当于 q= i * 0.1 也就是q=i/10，这样通过乘法和向右以为的组合的形式代替了除法，能提高效率。
```

再来回答上面两个问题中，部分一和部分二中最大的区别就是部分一代码使用了除法，第二部分只使用了乘法和移位。因为乘法和移位的效率都要比除法高，所以第二部分单独使用了乘法加移位的方式来提高效率。那么为什么不都使用乘法加移位的形式呢？为什么大于num1(65536)的数字要使用除法呢？原因是int型变量最大不能超过(2^31-1)。如果使用一个太大的数字进行乘法加移位运算很容易导致溢出。那么为什么是65536这个数字呢？第二阶段用到的乘法的数字和移位的位数又是怎么来的呢？

我们再回答第二个问题。

既然我们要使用q = (i * num2) >>> (num3);的形式使用乘法和移位代替除法，那么n和m就要有这样的关系

```
num2= (2^num3 /10 +1)
```

只有这样才能保证(i * num2) >>> (num3)结果接近于0.1。

那么52429这个数是怎么来的呢?来看以下数据：

```
2^10=1024, 103/1024=0.1005859375
2^11=2048, 205/2048=0.10009765625
2^12=4096, 410/4096=0.10009765625
2^13=8192, 820/8192=0.10009765625
2^14=16384, 1639/16384=0.10003662109375
2^15=32768, 3277/32768=0.100006103515625
2^16=65536, 6554/65536=0.100006103515625
2^17=131072, 13108/131072=0.100006103515625
2^18=262144, 26215/262144=0.10000228881835938
2^19=524288, 52429/524288=0.10000038146972656
2^20=1048576, 104858/1048576=0.1000003815
2^21=2097152, 209716/2097152 = 0.1000003815
2^22= 4194304, 419431/4194304= 0.1000001431
```

超过22的数字我就不列举了，因为如果num3越大，就会要求i比较小，因为必须保证(i * num2) >>> (num3)的过程不会因为溢出而导致数据不准确。那么是怎么敲定num1=65536,num2= 524288, num3=19的呢？ 这三个数字之间是有这样一个操作的：

```
（num1* num2）>>> num3
```

因为要保证该操作不能因为溢出导致数据不准确，所以num1和num2就相互约束。两个数的乘积是有一定范围的，不成超过这个范围，所以，num1增大，num2就要随之减小。

我觉得有以下几个原因：

1. 52429/524288=0.10000038146972656精度足够高。
2. 下一个精度较高的num2和num3的组合是419431和22。2^31/2^22 = 2^9 = 512。512这个数字实在是太小了。65536正好是2^16，一个整数占4个字节。65536正好占了2个字节，选定这样一个数字有利于CPU访问数据。


不知道有没有人发现，其实65536* 52429是超过了int的最大值的，一旦超过就要溢出，那么为什么还能保证（num1* num2）>>> num3能得到正确的结果呢？

这和>>>有关，因为>>>表示无符号右移，他会在忽略符号位，空位都以0补齐。

一个有符号的整数能表示的范围是-2147483648至2147483647，但是无符号的整数能表示的范围就是0-4,294,967,296（2^32），所以，只要保证num2*num3的值不超过2^32次方就可以了。65536是2^16,52429正好小于2^16,所以，他们的乘积在无符号向右移位就能保证数字的准确性。

getChars使用了的体系结构知识：

1. 乘法比除法高效：q = ( i * 52429) >>> (16+3); => 约等于q0.1,但i52429是整数乘法器，结合位移避免除法。
2. 重复利用计算结果:在获取r(i%100)时，充分利用了除法的结果，结合位移避免重复计算。
3. 位移比乘法高效:r = i – (( q << 6) + ( q << 5) + ( q << 2)); = >等价于r = i – (q * 100);
4. 局部性原理之空间局部性

```java
buf[–charPos] =DigitOnes[r];buf[–charPos] =DigitTens[r];通过查找数组，实现快速访问,避免除法计算
buf [–charPos ] = digits [ r];
```

* 片段四

```
return new String(buf, true);
```

这里用到了一个String中提供的保护类型构造函数，关于此函数请查看String源码分析,该函数比使用其他的构造函数有更好的性能。

#### 3.4.2.总结

所以，一个Integer对象有很多方法能够将值转成String类型。除了上面提到的一系列方法外，一般在要使用String的时候，很多人愿意使用如下形式：

```java
Integer s = new Integer(199);
System.out.println(s + "");
```

老规矩，反编译看看怎么实现的：

```java
Integer s = new Integer(199);
System.out.println((new StringBuilder()).append(s).append("").toString());
```

### 3.5.compareTo方法

在看是介绍Interger的类定义的时候介绍过，Integer类实现了Comparable<Integer>接口，所以Integer对象可以和另外一个Integer对象进行比较。

```java
public int compareTo(Integer anotherInteger) {
    return compare(this.value, anotherInteger.value);
}

public static int compare(int x, int y) {
    return (x < y) ? -1 : ((x == y) ? 0 : 1);
}
```

代码实现比较简单，就是拿出其中的int类型的value进行比较。


### 3.6.Number的方法

int intValue();
long longValue();
float floatValue();
double doubleValue();
byte byteValue();
short shortValue();

```java
public long longValue() {
    return (long)value;
}

public float floatValue() {
    return (float)value;
}

public double doubleValue() {
    return (double)value;
}
```