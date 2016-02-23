import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;


public class Formatter {

		public static void main(String args[]) throws IOException {
			String file = args[0];
			Scanner in = new Scanner(new File("dump/" + file));
			PrintStream out = new PrintStream(new File("results/" + file));
			
			while (in.hasNextLine()) {
				out.println(in.nextLine().replaceAll(",", "\\."));
			}
			
			in.close();
			out.close();
		}
}
