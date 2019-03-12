有return的情况下try catch finally的执行顺序（最有说服力的总结）
结论：
1、不管有没有出现异常，finally块中代码都会执行；
2、当try和catch中有return时，finally仍然会执行；
3、finally是在return后面的表达式运算后执行的（此时并没有返回运算后的值，而是先把要返回的值保存起来，不管finally中的代码怎么样，返回的值都不会改变，任然是之前保存的值），所以函数返回值是在finally执行前确定的；
4、finally中最好不要包含return，否则程序会提前退出，返回值不是try或catch中保存的返回值。
举例：
情况1：try{} catch(){}finally{} return;
            显然程序按顺序执行。
情况2:try{ return; }catch(){} finally{} return;
          程序执行try块中return之前（包括return语句中的表达式运算）代码；
         再执行finally块，最后执行try中return;
         finally块之后的语句return，因为程序在try中已经return所以不再执行。
情况3:try{ } catch(){return;} finally{} return;
         程序先执行try，如果遇到异常执行catch块，
         有异常：则执行catch中return之前（包括return语句中的表达式运算）代码，再执行finally语句中全部代码，
                     最后执行catch块中return. finally之后也就是4处的代码不再执行。
         无异常：执行完try再finally再return.
情况4:try{ return; }catch(){} finally{return;}
          程序执行try块中return之前（包括return语句中的表达式运算）代码；
          再执行finally块，因为finally块中有return所以提前退出。
情况5:try{} catch(){return;}finally{return;}
          程序执行catch块中return之前（包括return语句中的表达式运算）代码；
          再执行finally块，因为finally块中有return所以提前退出。
情况6:try{ return;}catch(){return;} finally{return;}
          程序执行try块中return之前（包括return语句中的表达式运算）代码；
          有异常：执行catch块中return之前（包括return语句中的表达式运算）代码；
                       则再执行finally块，因为finally块中有return所以提前退出。
          无异常：则再执行finally块，因为finally块中有return所以提前退出。

最终结论：任何执行try 或者catch中的return语句之前，都会先执行finally语句，如果finally存在的话。
                  如果finally中有return语句，那么程序就return了，所以finally中的return是一定会被return的，
                  编译器把finally中的return实现为一个warning。

 

 

下面是个测试程序
public class FinallyTest  
{
	public static void main(String[] args) {
		 
		System.out.println(new FinallyTest().test());;
	}

	static int test()
	{
		int x = 1;
		try
		{
			x++;
			return x;
		}
		finally
		{
			++x;
		}
	}
}
结果是2。
	在try语句中，在执行return语句时，要返回的结果已经准备好了，就在此时，程序转到finally执行了。
在转去之前，try中先把要返回的结果存放到不同于x的局部变量中去，执行完finally之后，在从中取出返回结果，
因此，即使finally中对变量x进行了改变，但是不会影响返回结果。
它应该使用栈保存返回值。
