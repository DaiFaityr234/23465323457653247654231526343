package spelling.Functionality;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import spelling.ContentPlayers.AudioPlayer;
import spelling.HelperClasses.ClearStatistics;
import spelling.HelperClasses.Tools;

/**
 * 
 * This class controls the logic to ask questions 
 * and generate festival commands during the spelling quiz.
 * Getter functions are also used so GUI output and statistics
 * can be linked in a proper manner.
 * @author hchu167
 * @collaborator yyap601
 */
public class SpellingList {
	// initialising variables to use during quiz TO KEEP TRACK OF QUESTIONS AND ATTEMPT COUNTS

	// Progress Bar Completion
	int quizProgress;
	// There are two spelling types: new and review
	String spellType;
	// Question Number
	int questionNo; 	
	// Current Level
	int currentLevel;
	// True if question has been attempted (according to current question)
	private boolean attempt = false; 
	private boolean endOfQuestion = false;
	public static boolean extraLevels = false;
	public boolean duringq; //keep track of whether the user is still in a question
	public boolean faulted; //keep track of when word is faulted
	public static boolean playingTrack1;
	public static boolean playingTrack2;
	public static boolean playingTrack7;
	// Current word to spell
	private String wordToSpell; 	 
	// Current example provided
	private String exampleToGive;
	// There are three types of spelling status: ASKING, ANSWERING and ANSWERED
	String status;
	// User's answer is stored here
	private String userAnswer = "0";

	// This is the SPELLING AID APP
	private SpellingAid spellingAidApp = null;

	// Number of correct answers
	private int correctAnsCount = 0;

	// List to store level labels
	public static ArrayList<String> levelNames = new ArrayList<String>();
	// List to store special levels
	public static ArrayList<String> specialNames = new ArrayList<String>();
	// List to ask questions from 
	ArrayList<String> currentQuizList ;
	// List of examples to words
	ArrayList<String> currentExamples;
	// List to record stats for the current level
	ArrayList<String> currentFailedList ;
	ArrayList<String> currentTriedList ;
	ArrayList<String> listOfWordsToChooseFrom;
	ArrayList<String> listOfExamplesToChooseFrom;
	// Files that contains the word lists and statistics
	File wordList;
	File customList;
	File spelling_aid_tried_words;
	File spelling_aid_failed;
	File spelling_aid_statistics;
	File spelling_aid_accuracy;
	File personal_best;
	File check;
	File examples;
	// ArrayLists for storing file contents for easier processing later according to levels
	HashMap<Integer, ArrayList<String>> mapOfWords;
	public HashMap<Integer, ArrayList<String>> mapOfWordExamples;
	HashMap<Integer, ArrayList<String>> mapOfFailedWords;
	HashMap<Integer, ArrayList<String>> mapOfTriedWords;

	// Special ArrayLists for extra levels
	HashMap<Integer, ArrayList<String>> mapOfExtraWords;
	HashMap<Integer, ArrayList<String>> mapOfExtraTriedWords;
	HashMap<Integer, ArrayList<String>> mapOfExtraFailedWords;
	// Hashmaps to store accuracy related values for every level
	HashMap<Integer,Integer> totalAsked;
	HashMap<Integer,Integer> totalCorrect;

	// Special HashMaps for accuracy values
	HashMap<Integer,Integer> totalExtraAsked;
	HashMap<Integer,Integer> totalExtraCorrect;
	// Constructor of spellinglist model for current session
	public SpellingList(){
		check = new File("wordList");
		examples = new File("NZCER-examples.txt");
		// Files that contains the word list and statistics
		wordList = new File("NZCER-spelling-lists.txt");
		if (check.exists()){
			extraLevels = true;
			customList = new File("wordList");
		} else {
			extraLevels = false;
		}
		spelling_aid_tried_words = new File(".spelling_aid_tried_words");
		spelling_aid_failed = new File(".spelling_aid_failed");
		spelling_aid_statistics = new File(".spelling_aid_statistics");
		spelling_aid_accuracy = new File(".spelling_aid_accuracy");
		personal_best = new File(".personal_best");
		// INITIALISING LISTS TO STORE VALUES
		initialiseListsToStoreValuesFromWordAndStatsList();
	}

	// List of getter methods to access state stored during spelling quiz at particular time

	// get number of questions
	public int getNoOfQuestions(){
		return currentQuizList.size();
	}

	// get number of questions
	public int getCurrentLevel(){
		return currentLevel;
	}

	//get current word 
	public String getCurrentWord(){
		return wordToSpell;
	}

	//get current example
	public String getCurrentExample(){
		return exampleToGive;
	}
	//get correct answer count
	public int getCorrectAns(){
		return correctAnsCount;
	}

	// Creates a list of words to test according to level and mode
	public void createLevelList(int level, String spellingType, SpellingAid spellAidApp){

		// For every level these following variables start as follows
		questionNo = 0;
		quizProgress = 0;
		correctAnsCount = 0;
		currentLevel = level;
		spellType=spellingType;
		spellingAidApp = spellAidApp;
		status = "ASKING";

		int questionListSize = 10;

		// choose list to read from according to mode
		HashMap<Integer, ArrayList<String>> wordMap;
		if(spellingType.equals("new") && !extraLevels){
			wordMap = mapOfWords;
		} else if (extraLevels){
			wordMap = mapOfExtraWords;
		} else{
			if (extraLevels){
				wordMap = mapOfExtraFailedWords; 
			} else{
				wordMap = mapOfFailedWords; 
			}	
		}
		// if level has not been attempted, create a list for that level since it won't exist
		if(!extraLevels){
			if(mapOfFailedWords.get(currentLevel)==null){
				mapOfFailedWords.put(currentLevel, new ArrayList<String>());
			}
			if(mapOfTriedWords.get(currentLevel)==null){
				mapOfTriedWords.put(currentLevel, new ArrayList<String>());
			}
			if(totalAsked.get(currentLevel)==null){
				totalAsked.put(currentLevel, 0);
				totalCorrect.put(currentLevel, 0);
			}
		} else {
			/*
			if(mapOfExtraFailedWords.get(currentLevel)==null){
				mapOfExtraFailedWords.put(currentLevel, new ArrayList<String>());
			}
			if(mapOfExtraTriedWords.get(currentLevel)==null){
				mapOfExtraTriedWords.put(currentLevel, new ArrayList<String>());
			}
			if(totalExtraAsked.get(currentLevel)==null){
				totalExtraAsked.put(currentLevel, 0);
				totalExtraCorrect.put(currentLevel, 0);
			}
			*/
		}


		// produce 10 random words from the correct list of words
		if (extraLevels){
			listOfWordsToChooseFrom = wordMap.get(level);
			listOfExamplesToChooseFrom = mapOfWordExamples.get(level);
		} else {
			listOfWordsToChooseFrom = wordMap.get(level);
			listOfExamplesToChooseFrom = mapOfWordExamples.get(level);
		}
		ArrayList<String> listOfWordsToTest = new ArrayList<String>();
		ArrayList<String> listOfExamples = new ArrayList<String>();
		HashMap<String,Integer> uniqueWordsToTest = new HashMap<String,Integer>();

		// if the mode is review, the list size should be the size of the list is the size is less than 10
		if(spellingType.equals("review")){
			if(listOfWordsToChooseFrom.size()<10){
				questionListSize = listOfWordsToChooseFrom.size();
			}
		}
		while(uniqueWordsToTest.keySet().size() != questionListSize){
			int positionToChoose = (int) Math.floor(Math.random() * listOfWordsToChooseFrom.size());
			
			if(uniqueWordsToTest.get(listOfWordsToChooseFrom.get(positionToChoose)) == null){
				uniqueWordsToTest.put(listOfWordsToChooseFrom.get(positionToChoose),1);
				listOfExamples.add(listOfExamplesToChooseFrom.get(positionToChoose));
				listOfWordsToTest.add(listOfWordsToChooseFrom.get(positionToChoose));
			}

		}
		// initialise lists to quiz and also change statistics
		currentExamples = listOfExamples;
		currentQuizList = listOfWordsToTest;
		if(!extraLevels){
			currentFailedList = mapOfFailedWords.get(currentLevel);
			currentTriedList = mapOfTriedWords.get(currentLevel);
		} else {
			/*
			currentFailedList = mapOfExtraFailedWords.get(currentLevel);
			currentTriedList = mapOfExtraTriedWords.get(currentLevel);
			*/
		}
		
	}

	// QuestionAsker is a swing worker class which asks the next question on the list.
	// The swing worker terminates when the whole list is covered
	class QuestionAsker extends SwingWorker<Void, Void>{

		protected Void doInBackground() throws Exception {
			if(getNoOfQuestions()!=0){
				askNextQuestion();
			} else {
				// if noOfQuestions = 0
				spellingAidApp.window.setText("");
				spellingAidApp.window.append(spellingAidApp.pColor,"                   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+\n",18);
				spellingAidApp.window.append(spellingAidApp.pColor,"                                             Review Spelling Quiz ( Level "+ spellingAidApp.levelSelect.getLevel() +" )\n",18);
				spellingAidApp.window.append(spellingAidApp.pColor,"                   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+\n\n",18);
				spellingAidApp.window.append(spellingAidApp.qColor," There are no words to review in this level.\n\n",15);
			}
			return null;
		}		
		protected void done(){
			levelNames.clear();
			if(getNoOfQuestions()==0){
				spellingAidApp.revertToOriginal();
			}
			// stop the quiz and record progress when the whole quiz list has been covered
			if(questionNo == 11){
				AudioPlayer.stopSound();		
			}
			if(questionNo > getNoOfQuestions()){
				recordFailedAndTriedWordsFromLevel();
				if (correctAnsCount == 10){
					AudioPlayer.playLoopSound(".ON/Track2.wav",-2.5f);
					playingTrack1 = false;
					playingTrack2 = true;
					playingTrack7 = false;
				} else if (correctAnsCount >= 7){
					AudioPlayer.playLoopSound(".ON/Track1.wav",-12.5f);
					playingTrack1 = true;
					playingTrack2 = false;
					playingTrack7 = false;
				} else {
					AudioPlayer.playLoopSound(".ON/Track3.wav",-0.0f);
					playingTrack7 = true;
				}
				if(spellType.equals("new")){
					// new spelling quiz has a next level option
					if (correctAnsCount == 10){
						spellingAidApp.window.append(spellingAidApp.gColor,"\n❂ You got "+ correctAnsCount +" out of "+ getNoOfQuestions() + " words correct on the first attempt❂ \n\n\n",15 );
						spellingAidApp.window.append(spellingAidApp.gColor,"CONGRATULATIONS! YOU REALLY DID WELL IN THIS LEVEL "+ currentLevel+" QUIZ! \n\n",15 );
					} else {
						spellingAidApp.window.append(spellingAidApp.pColor,"\n You got "+ correctAnsCount +" out of "+ getNoOfQuestions() + " words correct on the first attempt.\n\n",15 );
					}

					spellingAidApp.changeToNextState();
				} else if (spellType.equals("review")){
					spellingAidApp.window.append(spellingAidApp.pColor,"\n You got "+ correctAnsCount +" out of "+ getNoOfQuestions() + " words correct on the first attempt.\n\n",15 );
					spellingAidApp.revertToOriginal();
				}
			}
		}

	}

	// Method to return the QuestionAsker swing worker object
	public QuestionAsker getQuestionAsker(){
		return new QuestionAsker();
	}

	// AnswerChecker is a swing worker class which checks the user's answer against the expected answer and acts accordingly
	class AnswerChecker extends SwingWorker<Void, Void>{
		protected Void doInBackground() throws Exception {
			checkAnswer();
			return null;
		}

		protected void done(){
			// quit button is clicked
			if (status.equals("ASKING")){
				// when a question is over and it is time to ask the next question
				spellingAidApp.goOnToNextQuestion();
				duringq = false;
			}
		}
	}

	// Method to return the AnswerChecker swing worker object
	public AnswerChecker getAnswerChecker(){
		return new AnswerChecker();
	}

	// Start asking the new question
	private void askNextQuestion() throws InterruptedException{
		spellingAidApp.enter.setEnabled(false);
		spellingAidApp.wordListen.setEnabled(false);
		spellingAidApp.stopQuiz.setEnabled(false);
		spellingAidApp.sentenceListen.setEnabled(false);
		// make sure user input field is cleared everytime a question is asked
		spellingAidApp.userInput.setText("");
		if (extraLevels){
			spellingAidApp.accuracyIndicator.setText("");
		}
		// < NoOfQuestion because questionNo is used to access the current quiz list's question which starts at 0
		if(questionNo < getNoOfQuestions()){

			// focus the answering area
			spellingAidApp.userInput.requestFocus();
			// attempt is true only when the question has been attempted, so it starts as false
			attempt = false;
			// endOfQuestion is true when it is time to move on to the next question
			endOfQuestion = false;

			// faulted boolean is false
			faulted = false;
			// starts at 0
			wordToSpell = currentQuizList.get(questionNo);
			exampleToGive = currentExamples.get(questionNo);
			// then increment the question no to represent the real question number
			questionNo++;


			if (questionNo != 1){
				Thread.sleep(500);
			}


			// after ASKING, it is time for ANSWERING
			
			spellingAidApp.window.append(spellingAidApp.pColor," Spell word " + questionNo + " of " + currentQuizList.size() + ": ",15);
			spellingAidApp.voiceGen.sayText("Please spell word " + questionNo + " of " + currentQuizList.size() + ": " + ",",wordToSpell+",");
			status = "ANSWERING";
			spellingAidApp.enter.setEnabled(true);
			spellingAidApp.wordListen.setEnabled(true);
			spellingAidApp.stopQuiz.setEnabled(true);
			if (!spellingAidApp.reviewMode){
				spellingAidApp.sentenceListen.setEnabled(true);
			} else {
				spellingAidApp.sentenceListen.setEnabled(false);
				spellingAidApp.sentenceListen.setToolTipText("Example sentences are disabled in review mode.");
			}
		} else {
			questionNo++;
		}

	}

	// This method checks if the answer is right and act accordingly
	private void checkAnswer(){
		// ensure that the answer is valid
		if (!validInput(userAnswer)){
			// warning dialog for invalid user input
			JOptionPane.showMessageDialog(spellingAidApp, "Please enter in ALPHABETICAL LETTERS and use appropriate symbols.", "Input Warning",JOptionPane.WARNING_MESSAGE);
			// go back to ANSWERING since current answer is invalid
			status = "ANSWERING";

			return;
		} 


		// if it is valid, start the checking

		// turn to lower case for BOTH and then compare
		if(userAnswer.toLowerCase().equals(wordToSpell.toLowerCase())){
			// Correct echoed if correct
			if (duringq){
				spellingAidApp.window.append(spellingAidApp.tColor,userAnswer,15);
				spellingAidApp.window.append(spellingAidApp.tColor,"  ✔",15);
				faulted = true;
				spellingAidApp.window.append(spellingAidApp.tColor,"\n\n",15);
			}
			else {
				spellingAidApp.window.append(spellingAidApp.hColor,userAnswer,15);
				spellingAidApp.window.append(spellingAidApp.hColor,"  ✔",15);
				faulted = false;
				spellingAidApp.window.append(spellingAidApp.hColor,"\n\n",15);
			}
			spellingAidApp.voiceGen.sayText("Correct","");
			if (questionNo != 10 && !spellingAidApp.reviewMode){
				if (faulted){
					AudioPlayer.playSound(".ON/Bit1.wav");
				} else {
					AudioPlayer.playSound(".ON/Bit3.wav");
				}
			}
			if (!extraLevels){
				//processStarter("echo Correct | festival --tts"); 
				if(!attempt){
					Tools.record(spelling_aid_statistics,wordToSpell+" Mastered"); // store as mastered
					spellingAidApp.score = spellingAidApp.score + 100;
					if (spellingAidApp.score > spellingAidApp.highScore){
						spellingAidApp.highScore = spellingAidApp.score;
						Tools.overwrite(personal_best, new Double(spellingAidApp.highScore).toString());
						System.out.println(new Double(spellingAidApp.highScore).toString());
					}
					spellingAidApp.scoreLabel.setText("Score: "+spellingAidApp.score);
					spellingAidApp.personalBest.setText("Personal Best: "+spellingAidApp.highScore);
					correctAnsCount++; //question answered correctly
				} else {
					Tools.record(spelling_aid_statistics,wordToSpell+" Faulted"); // store as faulted
					spellingAidApp.score = spellingAidApp.score + 50;
					if (spellingAidApp.score > spellingAidApp.highScore){
						spellingAidApp.highScore = spellingAidApp.score;
					}
					spellingAidApp.scoreLabel.setText("Score: "+spellingAidApp.score);
					spellingAidApp.personalBest.setText("Personal Best: "+spellingAidApp.highScore);
				}

				// increment the counter which stores the total number of correct answers in the current level
				int totalNumberOfCorrectsInLevel = totalCorrect.get(currentLevel)+1;
				totalCorrect.put(currentLevel, totalNumberOfCorrectsInLevel);

				if(currentFailedList.contains(wordToSpell)){ // remove from failed list if exists
					currentFailedList.remove(wordToSpell);
				}
				attempt = true; // question has been attempted
				endOfQuestion = true;
				// answer is correct and so proceed to ASKING the next question
				status = "ASKING";
			} else {
				attempt = true; // question has been attempted
				endOfQuestion = true;
				status = "ASKING";
				spellingAidApp.specialScore = spellingAidApp.specialScore + 500;
				if (spellingAidApp.specialScore > spellingAidApp.highScore){
					spellingAidApp.highScore = spellingAidApp.specialScore;
				}
				spellingAidApp.scoreLabel.setText("Special Score: "+spellingAidApp.specialScore);
				spellingAidApp.personalBest.setText("Personal Best: "+spellingAidApp.highScore);
				spellingAidApp.progressBar.setValue(quizProgress);
			}

		} else {
			if(!attempt){
				spellingAidApp.window.append(spellingAidApp.pColor,userAnswer,15);
				spellingAidApp.window.append(spellingAidApp.pColor,"\n       Incorrect, try once more: ",15);
				spellingAidApp.voiceGen.sayText("Incorrect, try once more: "+"...",wordToSpell);
				spellingAidApp.voiceGen.sayText("",wordToSpell+",");
				//processStarter("echo Incorrect, try once more: "+wordToSpell+" . "+wordToSpell+" . " + "| festival --tts");
				// answer is wrong and a second chance is given and so back to ANSWERING
				status = "ANSWERING";
				duringq = true;
			} else {
				spellingAidApp.window.append(spellingAidApp.qColor,userAnswer,15);
				spellingAidApp.window.append(spellingAidApp.qColor,"  ✘",15);
				spellingAidApp.voiceGen.sayText("Incorrect.",",");
				AudioPlayer.playSound(".ON/Bit5.wav");
				spellingAidApp.window.append(spellingAidApp.qColor,"\n\n",15);
				//processStarter("echo Incorrect | festival --tts");
				Tools.record(spelling_aid_statistics,wordToSpell+" Failed"); // store as failed
				spellingAidApp.score = spellingAidApp.score -50;
				spellingAidApp.scoreLabel.setText("Score: "+spellingAidApp.score);
				if(!currentFailedList.contains(wordToSpell)){ //add to failed list if it doesn't exist
					currentFailedList.add(wordToSpell);
				}
				// answer is wrong on second attempt and so back to ASKING
				status = "ASKING";
				endOfQuestion = true;
			}
			attempt = true; // question has been attempted
		}

		// increment the counter which stores the total number of questions asked in the current level
		if(endOfQuestion){
			int totalNumberOfQuestionsAskedInLevel = totalAsked.get(currentLevel)+ 1;
			totalAsked.put(currentLevel, totalNumberOfQuestionsAskedInLevel);
			quizProgress= quizProgress+10;
			spellingAidApp.progressBar.setValue(quizProgress);
			// store as an attempted word after checking to make sure that there are no duplicates in the tried_words list
			if(!currentTriedList.contains(wordToSpell)){
				currentTriedList.add(wordToSpell);
			}
		}

	}

	/// This method records everything related to the current level to the file
	public void recordFailedAndTriedWordsFromLevel(){		

		Object[] failedKeys = mapOfFailedWords.keySet().toArray();
		Object[] triedKeys = mapOfFailedWords.keySet().toArray();
		Object[] accuracyKeys = totalAsked.keySet().toArray();

		ClearStatistics.clearFile(spelling_aid_failed);
		ClearStatistics.clearFile(spelling_aid_tried_words);
		ClearStatistics.clearFile(spelling_aid_accuracy);

		Arrays.sort(failedKeys);
		Arrays.sort(triedKeys);

		for(Object key : failedKeys){
			int dKey = (Integer)key;
			Tools.record(spelling_aid_failed,"%Level "+dKey);
			for(String wordToRecord : mapOfFailedWords.get(dKey)){
				Tools.record(spelling_aid_failed,wordToRecord);
			}
		}	
		for(Object key : triedKeys){
			int dKey = (Integer)key;
			Tools.record(spelling_aid_tried_words,"%Level "+dKey);
			for(String wordToRecord : mapOfTriedWords.get(dKey)){
				Tools.record(spelling_aid_tried_words,wordToRecord);
			}
		}	
		for(Object key : accuracyKeys){
			int dKey = (Integer)key;
			Tools.record(spelling_aid_accuracy,dKey+" "+totalAsked.get(dKey)+" "+totalCorrect.get(dKey));
		}

	}


	// function to ensure that the answer the user inputted is valid (in the format that can be accepted)
	private boolean validInput(String answer) {
		char[] chars = answer.toCharArray();
		// blank = unacceptable
		if(answer.equals("")){
			return false;
		}
		// first letter symbol = unacceptable
		if(!Character.isLetter(chars[0])){
			return false;
		}
		// accept any space or ' after first letter
		for (char c : chars) {
			if(!Character.isLetter(c) && (c != '\'') && (c != ' ')) {
				return false;
			}
		}
		return true;	
	}

	// calculate the accuracy for the current level
	public double getLvlAccuracy(){
		double noOfQuestionsAnsweredCorrectly = totalCorrect.get(currentLevel);
		double totalQuestionsAsked = totalAsked.get(currentLevel);
		if(totalQuestionsAsked==0.0){
			return 0;
		}
		double accuracy = (noOfQuestionsAnsweredCorrectly/totalQuestionsAsked)*100.0;
		return Math.round(accuracy*10.0)/10.0;
	}

	// Go through all the statistic files and also the file containing the word list 
	private void initialiseListsToStoreValuesFromWordAndStatsList(){
		mapOfWords = new HashMap<Integer, ArrayList<String>>();
		mapOfWordExamples = new HashMap<Integer, ArrayList<String>>();
		mapOfFailedWords = new HashMap<Integer, ArrayList<String>>();
		mapOfTriedWords = new HashMap<Integer, ArrayList<String>>();
		totalAsked = new HashMap<Integer,Integer>();
		totalCorrect = new HashMap<Integer,Integer>();

		mapOfExtraWords = new HashMap<Integer, ArrayList<String>>();
		try {
			// start adding file contents to the appropriate array list

			// WORDLIST
			BufferedReader readWordList = new BufferedReader(new FileReader(wordList));
			String word = readWordList.readLine();
			// array to store words in a level
			ArrayList<String> wordsInALevel = new ArrayList<String>();
			// level at which the word storage is happening
			int newSpellingLevel = 1;
			while(word != null){
				// % = level and so do appropriate things
				if(word.charAt(0) == '%'){
					levelNames.add(word.substring(1));
					String[] levelNo = word.split(" ");
					newSpellingLevel = Integer.parseInt(levelNo[1]);
					wordsInALevel = new ArrayList<String>();
					mapOfWords.put(newSpellingLevel,wordsInALevel);
				} else {
					wordsInALevel.add(word);
				}
				word = readWordList.readLine();
			}

			readWordList.close();
			
			// WORDEXAMPLES
			BufferedReader readExamples = new BufferedReader(new FileReader(examples));
			String ex = readExamples.readLine();
			ArrayList<String> examplesInALevel = new ArrayList<String>();
			int newExLevel = 1;
			while (ex != null) {
				if(ex.charAt(0) == '%'){
					String[] lvlNo = ex.split(" ");
					newExLevel = Integer.parseInt(lvlNo[1]);
					examplesInALevel = new ArrayList<String>();
					mapOfWordExamples.put(newExLevel, examplesInALevel);
				} else {
					examplesInALevel.add(ex);
				}
				ex = readExamples.readLine();
			}
			readExamples.close();
			// CUSTOMLIST
			if (extraLevels){
				BufferedReader readCustomList = new BufferedReader(new FileReader(customList));
				String w = readCustomList.readLine();
				ArrayList<String> wordsInLevel = new ArrayList<String>();
				// level at which the word storage is happening
				int spellingLevel = 1;
				while(w != null){
					// % = level and so do appropriate things
					if(w.charAt(0) == '%'){
						specialNames.add(w.substring(1));
						wordsInLevel = new ArrayList<String>();
						for (int i = 0; i < SpellingList.specialNames.size(); i++){
							if (w.substring(1).equals(SpellingList.specialNames.get(i))){
								spellingLevel = i+1;
							}
						}
						mapOfExtraWords.put(spellingLevel,wordsInLevel);
					} else {
						wordsInLevel.add(w);
					}
					w = readCustomList.readLine();
				} 
				readCustomList.close();
			}
			// TRIED WORDS
			BufferedReader readTriedList = new BufferedReader(new FileReader(spelling_aid_tried_words));
			String triedWord = readTriedList.readLine();
			// array to store words in a level
			ArrayList<String> triedWordsInALevel = new ArrayList<String>();
			// level at which the word storage is happening
			int triedLevel = 1;
			while(triedWord != null){
				// % = level and so do appropriate things
				if(triedWord.charAt(0) == '%'){
					String[] levelNo = triedWord.split(" ");
					triedLevel = Integer.parseInt(levelNo[1]);
					triedWordsInALevel = new ArrayList<String>();
					mapOfTriedWords.put(triedLevel,triedWordsInALevel);
				} else {
					triedWordsInALevel.add(triedWord);
				}
				triedWord = readTriedList.readLine();
			}
			readTriedList.close();

			// FAILED WORDS
			BufferedReader readFailList = new BufferedReader(new FileReader(spelling_aid_failed));
			String failedWord = readFailList.readLine();
			// array to store words to review in a level
			ArrayList<String> wordsToReviewInALevel = new ArrayList<String>();
			// level at which the word storage is happening
			int reviewLevel = 1;
			while(failedWord != null){
				// % = level and so do appropriate things
				if(failedWord.charAt(0) == '%'){
					String[] levelNo = failedWord.split(" ");
					reviewLevel = Integer.parseInt(levelNo[1]);
					wordsToReviewInALevel = new ArrayList<String>();
					mapOfFailedWords.put(reviewLevel,wordsToReviewInALevel);
				} else {
					wordsToReviewInALevel.add(failedWord);
				}
				failedWord = readFailList.readLine();
			}
			readFailList.close();

			// LEVEL ACCURACY
			BufferedReader readAccuracyList = new BufferedReader(new FileReader(spelling_aid_accuracy));
			String accuracyLine = readAccuracyList.readLine();
			while(accuracyLine != null){
				String[] accuracyLog = accuracyLine.split(" ");
				totalAsked.put(Integer.parseInt(accuracyLog[0]), Integer.parseInt(accuracyLog[1]));
				totalCorrect.put(Integer.parseInt(accuracyLog[0]), Integer.parseInt(accuracyLog[2]));
				accuracyLine = readAccuracyList.readLine();
			}
			readAccuracyList.close();

		} catch (IOException e){
			e.printStackTrace();
		}
	}

	// for the GUI to set the answer
	public void setAnswer(String theUserAnswer){
		userAnswer=theUserAnswer;
	}

}