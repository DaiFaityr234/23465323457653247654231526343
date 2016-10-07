package spelling;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 
 * This class is a collection of helper functions 
 * that are used in multiple classes
 * @authors yyap601 hchu167
 *
 */
public class Tools {
	// record a word to a file
	public static void record(File file,String word){
		try{
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file,true)));
			writer.println(word);
			writer.close();
		} catch(IOException e){
			System.out.println("An I/O Error Occurred");
			System.exit(0);
		}
	}

	// to run BASH commands
	public static void processStarter(String command){
		// process builder to run bash commands
		ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", command);
		Process process;
		try {
			process = builder.start();
			process.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}