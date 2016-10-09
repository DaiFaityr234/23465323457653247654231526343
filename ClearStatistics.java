package spelling;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * 
 * This class contains functions that clear the statistic files and displays a confirmation to the user
 * @author hchu167
 * @collaborator yyap601
 *
 */
public class ClearStatistics {

	// function to return information message and at the same time clear all the statistic files
	public static String clearStats(){
		String infoMsg = "All Spelling Statistics Cleared";
		clear();
		return infoMsg;
	}

	// function to clear all the statistic files
	private static void clear(){
		clearFile(new File(".spelling_aid_tried_words"));
		clearFile(new File(".spelling_aid_failed"));
		clearFile(new File(".spelling_aid_statistics"));		
		clearFile(new File(".spelling_aid_accuracy"));
	}

	// function to clear a single statistic file
	public static void clearFile(File file){
		PrintWriter writer;
		try {
			writer = new PrintWriter(file);
			writer.print("");
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}