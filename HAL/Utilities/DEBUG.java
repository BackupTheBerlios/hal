package Utilities;

public class DEBUG {

	private static final boolean flag = true;
	private static final boolean flag2 = false;
	
	public static void print(String in){
		if(flag)
			System.out.println(in);
	}
	
	public static void printAnoying(String in){
		if(flag2)
			System.out.println(in);
	}
	
}
