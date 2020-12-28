/** Ben F Rayfield offers this software opensource MIT license */
package immutable.lazycl.spec;
import static mutable.util.Lg.*;
import mutable.util.ui.ScreenUtil;

public strictfp class TestLazyCL{
	
	/** throws if fail */
	public static void runTests(Lazycl lz){
		//works but dont download too often... testDownload(lz); //FIXME dont do this every time. Dont want to download too many times and get local address blocked. TODO robots.txt
		testOpenclMatmul(lz);
		testOpenclRecurrentNeuralnet10CyclesDeep(lz);
		testAcylicFlow(lz);
	}
	
	public static void test(String testName, boolean z){
		if(!z) throw new RuntimeException("TEST FAIL: "+testName);
		lg("Test pass: "+testName);
	}
	
	public static void testEqq(String testName, Object x, Object y){
		if(x != y) throw new RuntimeException("TEST FAIL: "+testName+" cuz "+x+" != "+y);
		lg("Test pass: "+testName);
	}
	
	public static void testEq(String testName, Object x, Object y){
		if(((x==null) != (y==null)) || !x.equals(y)) throw new RuntimeException("TEST FAIL: "+testName+" cuz "+x+" not .equals "+y);
		lg("Test pass: "+testName);
	}
	
	/*TODO choose javassist andOr beanshell, cuz beanshell claims to have a compiled mode. Also, can it put debug breakpoints?
	Also, do i want the code that finds jdk or openjdk etc and uses it for compiling with debug info so can breakpoint in runtime generated code?
	*/
	
	public static void testDownload(Lazycl lz){
		LazyBlob bytes = lz.lazycl("Code", "download:https://upload.wikimedia.org/wikipedia/commons/a/a3/Ice_water_vapor.jpg");
		lg("testDownload got "+bytes.bize()+" bits. DisplayImage...");
		ScreenUtil.displayImage(bytes.arr(byte[].class));
	}
	
	/** an optimization useful for music tools or simple loop bodies */
	public static void testAcylicFlow(Lazycl lz){
		/*String acyclicFlow = "java:...TODO copy that static func that takes int[] and double[] params from the acyclicflow dir in earlier project, which already works...";
		int[] opcodes = TODO; //FIXME
		double[] in = TODO; //FIXME
		LazyBlob outBlob = lz.lazycl(
			//is default "IsTemp", false,
			"Code", acyclicFlow,
			"opcodes", opcodes,
			"in", in
		); //TODO it also needs to know inSize, tempSize, outSize, andOr totalSize? 
		double[] outArray = null; //FIXME get from outblob.d(int);
		*/
		throw new RuntimeException("TODO");
	}
	
	/** test wrapping of IntToDoubleFunction, for example. TODO other primitive lambdas. */
	public static void testJavaPrimitiveLambdas(Lazycl lz){
		test("i*i*i-7*i*i+3 i*i*i-7*i*i+3",
			lz.wrap(float[].class, 100, (int i)->i*i*i-7*i*i+3).arr(float[].class)[5] == 5f*5*5-7*5*5+3);
	}
	
	public static void testOpenclMatmul(Lazycl lz){
		String matmulCode1dAs2d = //todo generate kernel void hashNameBasedOnKernelCodeString
			"opencl1.2:(global float* bdOut, int const bSize, int const cSize, int const dSize, global const float* bc, global const float* cd){\n"+
			"	int bd = get_global_id(0);\n"+
			"	const int b = bd/dSize;\n"+
			"	const int d = bd%dSize;\n"+
			"	float sum = 0;\n"+
			"	for(int c=0; c<cSize; c++){\n"+
			"		sum += bc[b*cSize+c]*cd[c*dSize+d];\n"+
			"	}\n"+
			"	bdOut[bd] = sum;\n"+
			"}";
		int bSize = 20;
		int cSize = 30;
		int dSize = 50;
		
		/*how does LazyclPrototype.vm_evalOneTheSlowWayNotInGroups know how big bdOut should be? It knows its float[something],
			but its allowed foir get_global_id not to match 1-to-1 with the output array, such as if each writes a continuous
			5 indexs in the output array.
			Put something like "OutSize" or "Size(bdOut)" or "bdOut.size" or "bdOut.length" or "OutBize". in params. TODO choose.
			I choose "Bize" which is in units of bits.
		*/
		
		/*TODO include IntToDoubleFunction but used as floats, in lz.wrap,
		but still need to include int size and which primitive type,
		and for it to be that or IntToLongFunction,
		or maybe another wrap func that has size and primitive type.
		LazyBlob bc = lz.wrap(b*c, float.class, (int i)->i*i*i-7*i*i+3);
		/*LazyBlob bc = lz.lazycl(
			"Code",
			"java8WithJava4Syntax:float[] ret = new float["+(b*c)+"];"
			+" for(int i=0; i<ret.length; i++) ret[i] = i*i*i-7*i*i+3;"
			+" return TODO_HOW_TO_GET_THE_LAZYCL_INSTANCE.wrap(ret);"
		); //size b*c
		*/
		//LazyBlob bc = floats(b*c, (int i)->i*i*i-7*i*i+3); //size b*c
		//LazyBlob cd = floats(c*d, (int i)->i^(i*i-23)); //size c*d
		
		//FIXME are those IntToLongFunction instead of IntToDoubleFunction? does it matter in these 2 cases?
		LazyBlob bc = lz.wrap(float.class, bSize*cSize, (int i)->i*i*i-7*i*i+3); //size bSize*cSize
		LazyBlob cd = lz.wrap(float.class, cSize*dSize, (int i)->i^(i*i-23)); //size cSize*dSize
		
		long bize = 32L*bSize*dSize; //32==sizeof(float)
		LazyBlob bd = lz.lazycl(
			//is default "IsTemp", false,
			"Code", matmulCode1dAs2d,
			"Bize", bize,
			//could be 1 2 or 3 ints as opencl supports get_global_id in 3d in some GPUs but not older GPUs
			//which is why some of the code uses 1d and does / and % (slower) to turn it into more dims.
			"GlobalSize", lz.wrapc(bSize*dSize),
			//"LocalSize", lz.wrapc(32),
			//TODO local id for multiple threads sharing local memory together
			//todo will get default in this? unsure about that. "TODO local id, see how i did that 32x32x32 matmul cache optimization, and give url i copied it from before slightly modifying it", ints(1) or leave this as a default? but how many id dims (max 3 in cl),
			"bSize", bSize,
			"cSize", cSize,
			"dSize", dSize,
			"bc", bc,
			"cd", cd
		); //size b*d. multiplied (or waits until you observe the floats in bd)
		int testB = 7, testD = 15;
		float correctSum = 0;
		for(int c=0; c<cSize; c++){
			correctSum += bc.f(testB*cSize+c)*cd.f(c*dSize+testD);
		}
		float observedSum = bd.f(testB*dSize+testD);
		//strictfp and strict opencl compiler params and opencl1.2 supports strict IEEE754 floats so == is ok.
		testEq("matmul bc cd, testB="+testB+" testD="+testD, correctSum, observedSum);
	}
	
	public static void testOpenclRecurrentNeuralnet10CyclesDeep(Lazycl lz){
		throw new RuntimeException("TODO");
	}

}