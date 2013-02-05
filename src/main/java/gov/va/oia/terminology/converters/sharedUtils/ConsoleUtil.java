package gov.va.oia.terminology.converters.sharedUtils;

public class ConsoleUtil
{
	private static int lastStatus;
	private static boolean progressLine = false;
	private static int printsSinceReturn = 0;
	private static boolean progressLineUsed = false;
	public static boolean disableFancy = (System.console() == null);
	
	public static void showProgress()
	{
		char c;
		switch (lastStatus) {
		case 0:
			c = '/';
			break;
		case 1:
			c = '-';
			break;
		case 2:
			c = '\\';
			break;
		case 3:
			c = '|';
			break;

		default:  //shouldn't be used
			c = '-';
			break;
		}
		lastStatus++;
		if (lastStatus > 3)
		{
			lastStatus = 0;
		}
		
		if (!progressLine)
		{
			System.out.println();
			printsSinceReturn = 0;
		}
		if (disableFancy)
		{
			System.out.print(".");
			printsSinceReturn++;
			if (printsSinceReturn >= 75)
			{
			    System.out.println();
			    printsSinceReturn = 0;
			}
		}
		else
		{
			System.out.print("\r" + c);
		}
		progressLine = true;
		progressLineUsed = true;
	}
	
	public static void print(String string)
	{
		if (progressLine)
		{
			if (disableFancy)
			{
				if (progressLineUsed)
				{
					System.out.println();
					printsSinceReturn = 0;
				}
			}
			else
			{
				System.out.print("\r \r");
			}
			progressLine = false;
		}
		System.out.print(string);
	}
	
	public static void println(String string)
	{
		if (progressLine)
		{
			if (disableFancy)
			{
				if (progressLineUsed)
				{
					System.out.println();
					printsSinceReturn = 0;
				}
			}
			else
			{
				System.out.print("\r \r");
			}
		}
		System.out.println(string);
		progressLine = true;
		progressLineUsed = false;
	}
	
	public static void printErrorln(String string)
	{
		if (progressLine)
		{
			if (disableFancy)
			{
				if (progressLineUsed)
				{
					System.out.println();
					printsSinceReturn = 0;
				}
			}
			else
			{
				System.out.print("\r \r");
			}
			progressLine = false;
		}
		System.err.println(string);
		printsSinceReturn = 0;
		progressLine = true;
		progressLineUsed = false;
	}
}