package spelling.Functionality;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import spelling.ContentPlayers.AudioPlayer;
import spelling.ContentPlayers.SoundPlayer;
import spelling.ContentPlayers.VideoCreator;
import spelling.ContentPlayers.VideoPlayer;
import spelling.ContentPlayers.VoiceGenerator;
import spelling.ContentPlayers.VoiceGenerator.Voice;
import spelling.Functionality.SpellingList.AnswerChecker;
import spelling.Functionality.SpellingList.QuestionAsker;
import spelling.HelperClasses.ClearStatistics;
import spelling.HelperClasses.ColorPane;
import spelling.HelperClasses.CustomSelector;
import spelling.HelperClasses.LevelSelector;
import spelling.HelperClasses.SpellingAidStatistics;

/**
 * 
 * This class controls the creation of the spelling aid GUI and manages when
 * different panels are visible during the spelling tests.
 * @author hchu167
 * @collaborator yyap601
 *
 */
@SuppressWarnings("serial")
public class SpellingAid extends JFrame implements ActionListener{

	//To determine whether to clear out welcome text, if true = don't clear
	boolean notFirstTime; 
	//Variables to store accuracy, current score, special score, and high score
	double currentAcc;
	public double score = 0;
	public double specialScore = 0;
	public double highScore;

	JFrame frame = new JFrame("Spelling Aid ~ VOXSPELL"); //Main spelling frame
	final JPanel tabs = new JPanel(); //Main spelling option buttons
	final JPanel controller = new JPanel(); //Main spelling logic functions
	final JPanel nextState = new JPanel(); //Main spelling logic functions after quiz

	//The Spelling List so that all buttons can access it, will be set in New/Review button
	private SpellingList spellList = null;
	private QuestionAsker questionAsker = null;
	private AnswerChecker ansChecker = null;

	//The voice generator for Spelling Aid
	public VoiceGenerator voiceGen = null;
	public VoiceGenerator respellGen = null;
	public Voice theVoice = Voice.DEFAULT;
	public double theVoiceStretch;
	public double theVoicePitch;
	public double theVoiceRange;

	//Creating buttons for tab menu
	public JButton newQuiz = new JButton("New Spelling Quiz");
	public JButton reviewMistakes = new JButton("Review Mistakes");
	public JButton viewStats = new JButton("View Statistics");
	public JButton clearStats = new JButton("Clear Statistics");
	public int i; //Displays warning window

	//Creating buttons for nextState components
	public JButton _replayLevel = new JButton("Replay level");
	public JButton _nextLevel = new JButton("Next level");
	public JButton _videoReward = new JButton("Play video");
	public JButton _specialVideoReward = new JButton("Play fast video");
	public JButton _done = new JButton("Done");

	//Creating buttons for controller components
	public JProgressBar progressBar = new JProgressBar(0, 100);
	public JLabel spellPrompt = new JLabel("Please spell here:");
	public JTextField userInput = new JTextField();
	public JButton enter = new JButton("Enter");
	public JLabel scoreLabel = new JLabel ("Score: 0");
	public JLabel personalBest = new JLabel ("Personal Best: "+highScore);
	public JButton sentenceListen = new JButton("Listen to a sentence");
	public JButton wordListen = new JButton("Listen to the word again");
	public JLabel voxPrompt = new JLabel("Voice Toggle");
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public JComboBox voxSelect = new JComboBox(new String[]{"Default","Auckland"});
	public JLabel accuracyIndicator = new JLabel();
	public JButton stopQuiz = new JButton("Stop Quiz");

	//Boolean to check whether quiz is interrupted
	public boolean quizInterrupted;
	//Boolean to check whether GUI is in review mode
	public boolean reviewMode;
	//Creating main GUI output area
	public ColorPane window = new ColorPane();
	public JScrollPane scrollBar = new JScrollPane(window); //Adds scrolling pane to window
	public StyledDocument doc = (StyledDocument) window.getDocument();
	public Style style = doc.addStyle("StyleName", null); //Allows adding of image icon 

	//Extra buttons for selecting a language, adding a list, and help 
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public JComboBox languageSelect = new JComboBox(new String[]{"English","Chinese","Japanese"});
	public JButton addList = new JButton("Import extra spelling levels");
	public JButton help = new JButton("Help");
	public boolean foreign = false;
	public LevelSelector levelSelect;

	//Colors
	public Color tColor = new Color(31,190,214); //spelling (t)ab color
	public Color qColor = new Color(255,113,126); //spelling (q)uiz functionality color
	public Color hColor = new Color(151, 195, 10); //spelling (h)elper color
	public Color bColor = new Color(192, 188, 182); // (b)ackground button color
	public Color pColor = new Color(85, 85, 85); //(p)rogress bar text color
	public Color wColor = new Color(248,248,242); //(w)hite color - better version for eyes
	public Color gColor = new Color(255,223,0); //(g)old color - for 10/10
	//Layout for main GUI
	FlowLayout options = new FlowLayout();

	//This Action object is created to be added as a listener for userInput
	// so that when enter is pressed, it accepts input
	Action enterAction = new AbstractAction()
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			takeInUserInput();
		}
	};

	// Method to call to accept user input
	private void takeInUserInput(){
		// only take in input when it is in the ANSWERING phase
		if(spellList.status.equals("ANSWERING")){
			spellList.setAnswer(clearTxtBox());
			spellList.status = "ANSWERED";
			ansChecker=spellList.getAnswerChecker();
			ansChecker.execute();
		}	
	}

	// Method to ask next question in the quiz
	public void goOnToNextQuestion(){
		if(spellList.spellType.equals("new")){
			if (!SpellingList.extraLevels){
				accuracyIndicator.setText("Level "+ spellList.getCurrentLevel()+" Accuracy: "+spellList.getLvlAccuracy()+"%");
				currentAcc = spellList.getLvlAccuracy();
				setLabelColors(currentAcc,score,spellList);
			}
		}
		if(spellList.status.equals("ASKING")){
			questionAsker=spellList.getQuestionAsker();
			questionAsker.execute();
		}
	}

	//Method to add buttons to main GUI frame
	public void addComponentsToGUI(Container pane) {        

		//Set button alignment layout for main GUI
		tabs.setLayout(options);
		tabs.setBackground(tColor);
		options.setAlignment(FlowLayout.TRAILING);
		controller.setLayout(new BoxLayout(controller, BoxLayout.Y_AXIS));
		controller.setBackground(wColor);
		nextState.setBackground(tColor);
		window.setBackground(wColor);
		//Setting sizes of tab buttons
		setButtonParameters(newQuiz, new Dimension(150,30), qColor, bColor);
		tabs.add(newQuiz);
		setButtonParameters(reviewMistakes, new Dimension(150,30), qColor, bColor);
		tabs.add(reviewMistakes);
		setButtonParameters(viewStats, new Dimension(150,30), hColor, bColor);
		tabs.add(viewStats);
		setButtonParameters(clearStats, new Dimension(150,30), hColor, bColor);
		tabs.add(clearStats);

		//Setting sizes of nextState buttons
		setButtonParameters(_replayLevel, new Dimension(120,30), qColor, bColor);
		nextState.add(_replayLevel);
		setButtonParameters(_nextLevel, new Dimension(120,30), qColor, bColor);
		nextState.add(_nextLevel);
		setButtonParameters(_videoReward, new Dimension(120,30), hColor, bColor);
		nextState.add(_videoReward);
		setButtonParameters(_specialVideoReward, new Dimension(120,30), hColor, bColor);
		nextState.add(_specialVideoReward);
		setButtonParameters(_done, new Dimension(120,30), pColor, bColor);
		nextState.add(_done);

		//Spacer to format components on right hand side of GUI
		controller.add(Box.createRigidArea(new Dimension(40,5)));

		//Setting sizes of spelling components
		spellPrompt.setPreferredSize(new Dimension(150, 30));
		spellPrompt.setForeground(hColor);
		spellPrompt.setAlignmentX(Component.CENTER_ALIGNMENT);
		controller.add(spellPrompt);

		userInput.setSize(new Dimension(50, 15));
		userInput.setAlignmentX(Component.CENTER_ALIGNMENT);
		controller.add(userInput);



		enter.setPreferredSize(new Dimension(150, 30));
		enter.setAlignmentX(Component.CENTER_ALIGNMENT);
		enter.setForeground(qColor);
		controller.add(enter);
		controller.add(Box.createRigidArea(new Dimension(40,25)));
		scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		scoreLabel.setForeground(tColor);
		controller.add(scoreLabel);
		controller.add(Box.createRigidArea(new Dimension(40,25)));
		personalBest.setAlignmentX(Component.CENTER_ALIGNMENT);
		personalBest.setForeground(tColor);
		controller.add(personalBest);
		progressBar.setStringPainted(true);
		progressBar.setForeground(pColor);
		//Spacer to format components on right hand side of GUI
		controller.add(Box.createRigidArea(new Dimension(40,50)));

		sentenceListen.setPreferredSize(new Dimension(150,40));
		sentenceListen.setAlignmentX(Component.CENTER_ALIGNMENT);
		sentenceListen.setForeground(qColor);
		controller.add(sentenceListen);
		controller.add(Box.createRigidArea(new Dimension(40,20)));
		//Setting size for "Listen to the word again" button
		wordListen.setPreferredSize(new Dimension(150, 40));
		wordListen.setAlignmentX(Component.CENTER_ALIGNMENT);
		wordListen.setForeground(qColor);
		controller.add(wordListen);

		//Spacer to format components on r50ight hand side of GUI
		controller.add(Box.createRigidArea(new Dimension(40,50)));

		//Setting size for "Stop Quiz" button
		stopQuiz.setPreferredSize(new Dimension(200, 40));
		stopQuiz.setAlignmentX(Component.CENTER_ALIGNMENT);
		stopQuiz.setForeground(hColor);
		controller.add(stopQuiz);

		//Spacer to format components on right hand side of GUI
		controller.add(Box.createRigidArea(new Dimension(40,50)));

		//Setting size for voice selecting combo box
		voxPrompt.setPreferredSize(new Dimension(150, 30));
		voxPrompt.setAlignmentX(Component.CENTER_ALIGNMENT);
		voxPrompt.setForeground(hColor);
		controller.add(voxPrompt);
		voxSelect.setPreferredSize(new Dimension(150, 30));
		voxSelect.setAlignmentX(Component.CENTER_ALIGNMENT);
		controller.add(voxSelect);

		//Spacer to format components on right hand side of GUI
		controller.add(Box.createRigidArea(new Dimension(40,80)));

		//Setting size for level indicator at the bottom of the GUI
		accuracyIndicator.setPreferredSize(new Dimension(40, 30));
		accuracyIndicator.setAlignmentX(Component.CENTER_ALIGNMENT);


		controller.add(accuracyIndicator);

		//Arranging tabs only when GUI is opened for the first time
		pane.add(tabs, BorderLayout.NORTH);
		pane.add(controller, BorderLayout.EAST);
		pane.add(nextState, BorderLayout.SOUTH);
		//Set main text display in centre of GUI
		//Scroll bar allows user to check previous words attempted during current session
		pane.add(scrollBar, BorderLayout.CENTER);
	}
	
	//Helper method to set parameters for JButtons
	public void setButtonParameters(JButton button, Dimension d, Color fore, Color back){
		button.setPreferredSize(d);
		button.setOpaque(true);
		button.setForeground(fore);
		button.setBackground(back);
	}
	// Constructor for spelling aid object
	public SpellingAid() {
		notFirstTime = false; 

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(scrollBar);

		// Adding action listeners that perform operations when button is pressed
		newQuiz.addActionListener(this);
		reviewMistakes.addActionListener(this);
		viewStats.addActionListener(this);
		clearStats.addActionListener(this);
		wordListen.addActionListener(this);
		sentenceListen.addActionListener(this);
		enter.addActionListener(this);
		stopQuiz.addActionListener(this);
		voxSelect.addActionListener(this);
		languageSelect.addActionListener(this);
		addList.addActionListener(this);
		help.addActionListener(this);
		_replayLevel.addActionListener(this);
		_nextLevel.addActionListener(this);
		_videoReward.addActionListener(this);
		_specialVideoReward.addActionListener(this);
		_done.addActionListener(this);

		// Add all separate components to GUI
		addComponentsToGUI(frame.getContentPane());
		frame.pack();
		frame.setLocationRelativeTo(null);  //set GUI in centre of screen
		frame.setSize(628, 630);
		frame.setResizable(false); //disable user from resizing GUI
		frame.setVisible(true);
		controller.setVisible(false); //hide controller until spelling quiz starts
		nextState.setVisible(false); //hide nextState until spelling quiz ends
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				File check = new File("wordList");
				if (check.exists()){
					check.delete(); // destroys user chosen wordList after GUI closes
				}

			}
		});
		// clear the window
		window.setText("");
		//Display welcome message to GUI
		window.append(pColor,"                   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+\n",18);
		window.append(pColor,"                                              Welcome to the Spelling Aid\n",18);
		window.append(pColor,"                   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+\n\n",18);
		window.append(pColor,"                                       Please select your language:\n\n",15);
		window.append(pColor,"\n\n                                     Please select from one of the options above:\n\n\n",15);
		window.append(tColor, "                                                    ", 18);

		StyleConstants.setIcon(style, new ImageIcon("400w.gif"));
		try {
			doc.insertString(doc.getLength(), "ignored text", style);
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		}

		languageSelect.setSize( languageSelect.getPreferredSize() );
		languageSelect.setLocation(365, 83);
		window.add( languageSelect );
		addList.setSize(addList.getPreferredSize());
		addList.setLocation(210, 120);
		window.add(addList);
		help.setSize(addList.getPreferredSize());
		help.setLocation(210,180);
		window.add(help);
		//Disable any editing from user
		window.setEditable(false);

		//JTextArea doesn't automatically scroll itself 
		DefaultCaret scroller = (DefaultCaret)window.getCaret();
		scroller.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);

		// stretch spelling on word
		theVoiceStretch = 1.2;
		theVoicePitch = 95;
		theVoiceRange = 15;
		//initialise voice generator for the app
		voiceGen = new VoiceGenerator(theVoice,theVoiceStretch,theVoicePitch,theVoiceRange);

		//initialise voice generator for the respell button
		respellGen = new VoiceGenerator(theVoice,theVoiceStretch,theVoicePitch,theVoiceRange);
		respellGen.cancel(true); // immediately cancel it to allow the respell button to work on the first try

		makeSureAllNecessaryFilesArePresent();// check for the presence of the hidden files

		// JTextField tracks ENTER button
		userInput.addActionListener(enterAction);

		stopQuiz.setToolTipText("You can only use this button during the answering phase in a quiz.");
		wordListen.setToolTipText("You can only use this button during the answering phase in a quiz.");
		enter.setToolTipText("You can only use this button during the answering phase in a quiz.");
		sentenceListen.setToolTipText("You can only use this button during the answering phase in a quiz.");
	}

	public static void main(String[] args) {
		try {

			// Preferred look and feel
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
			AudioPlayer.playLoopSound(".ON/Track1.wav",-12.5f);
		} catch (UnsupportedLookAndFeelException ex) {
			ex.printStackTrace();
		} catch (IllegalAccessException ex) {
			ex.printStackTrace();
		} catch (InstantiationException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// Make main GUI
				new SpellingAid();
			}
		});
	}

	//Set operations for different buttons
	public void actionPerformed(ActionEvent ae) {
		//Setting internal representation for each option chosen
		if (ae.getSource() == newQuiz) {

			reviewMode = false;
			quizInterrupted = false;
			if (!foreign){
				stopQuiz.setText("Stop Quiz");
			}
			// Scroll bar set to an arbitrary value
			window.setCaretPosition(1);
			// Scroll bar set to the top
			window.setCaretPosition(0);
			spellList = new SpellingList(); //Create new list of 10 words
			LevelSelector levelSelect = new LevelSelector(); //Create new joptionpane to select level
			if(levelSelect.getLevel()!=0 && levelSelect.getLevel()!=-1){ // only when a level is selected, that u start changing the window's content
				File file = new File(".personal_best");
				BufferedReader test;
				try{
					test = new BufferedReader(new FileReader(file));
					String text = test.readLine();
					if (text == null){
						highScore = 0.0;
					} else {
						highScore = Double.parseDouble(text);
						personalBest.setText("Personal Best: "+highScore);
					}
				} catch (IOException ex) {
					ex.printStackTrace();
				}
				SpellingList.extraLevels = false;
				changeModes();
				progressBar.setVisible(true);
				frame.getContentPane().add(progressBar, BorderLayout.NORTH);
				progressBar.setValue(0);
				//Display new spelling message to GUI
				window.append(pColor,"+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+\n",18);
				window.append(pColor,"                    New Spelling Quiz ( Level "+ levelSelect.getLevel() +" )\n",18);
				window.append(pColor,"+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+\n\n",18);

				//Start asking questions
				if (SpellingList.playingTrack7){
					AudioPlayer.playLoopSound(".ON/Track7.wav",-12.5f);
				}
				SpellingList.playingTrack7 = false;
				spellList.createLevelList(levelSelect.getLevel(), "new",this);
				accuracyIndicator.setText("Level "+ spellList.getCurrentLevel()+" Accuracy: "+spellList.getLvlAccuracy()+"%");
				currentAcc = spellList.getLvlAccuracy();
				setLabelColors(currentAcc,score,spellList);
				questionAsker = spellList.getQuestionAsker();
				questionAsker.execute();
			} else if (levelSelect.getLevel()==-1 && SpellingList.extraLevels) {
				if (CustomSelector.getExtra().equals("NULL")){
					SpellingList.specialNames.clear();
				}
				if (!CustomSelector.getExtra().equals("NULL")){

					changeModes();
					progressBar.setVisible(true);
					frame.getContentPane().add(progressBar, BorderLayout.NORTH);
					progressBar.setValue(0);
					//Display new spelling message to GUI
					window.append(pColor,"+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+\n",18);
					window.append(pColor,"                     New Spelling Quiz ( Level "+CustomSelector.getExtra() +" )\n",18);
					window.append(pColor,"+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+\n\n",18);
					spellList.createLevelList(CustomSelector.getLevel(), "new",this);
					questionAsker = spellList.getQuestionAsker();
					questionAsker.execute();
				}

			} else {
				SpellingList.specialNames.clear();
			}
		}
		else if (ae.getSource() == reviewMistakes) {

			reviewMode = true;
			quizInterrupted = false;
			if (!foreign){
				stopQuiz.setText("Stop Review");
			}
			// Scroll bar set to an arbitrary value
			window.setCaretPosition(1);
			// Scroll bar set to the top
			window.setCaretPosition(0);
			spellList = new SpellingList(); //Create new list of 10 words
			levelSelect = new LevelSelector(); //Create new joptionpane to select level
			if(levelSelect.getLevel()!=0){ // only when a level is selected, that u start changing the window's content
				changeModes();
				//Display new spelling message to GUI
				window.append(pColor,"+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+\n",18);
				window.append(pColor,"                  Review Spelling Quiz ( Level "+ levelSelect.getLevel() +" )\n",18);
				window.append(pColor,"+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+\n\n",18);

				spellList.createLevelList(levelSelect.getLevel(), "review",this);
				accuracyIndicator.setText("Level "+ spellList.getCurrentLevel());
				questionAsker = spellList.getQuestionAsker();
				questionAsker.execute();
			}
		}
		else if (ae.getSource() == viewStats) {
			languageSelect.setVisible(false);
			addList.setVisible(false);
			help.setVisible(false);
			// Scroll bar set to an arbitrary value
			window.setCaretPosition(1);
			// Scroll bar set to the top
			window.setCaretPosition(0);
			// clear the window
			window.setText("");
			//Display new spelling message to GUI
			window.append(pColor,"                   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+\n",18);
			window.append(pColor,"                                                      Spelling Aid Statistics \n",18);
			window.append(pColor,"                   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+\n",18);

			notFirstTime = false; // to clear the stats

			// instantiate the statistics obj and execute it
			SpellingAidStatistics statsWin = new SpellingAidStatistics(this);
			statsWin.execute();
		}
		else if (ae.getSource() == clearStats) {
			if (languageSelect.getSelectedItem().toString().equals("English")){
				i = JOptionPane.showConfirmDialog(this, "All spelling progress will be lost. Continue?");
			} else if (languageSelect.getSelectedItem().toString().equals("Chinese")){				
				i = JOptionPane.showConfirmDialog(this, "所有統計數據都將丟失。確認？");
			} else if (languageSelect.getSelectedItem().toString().equals("Japanese")){	
				i = JOptionPane.showConfirmDialog(this, "すべてのスペル進行状況が失われてしまいます。 続行しますか？");
			}
			if (i == JOptionPane.YES_OPTION){
				score = 0.0;
				highScore = 0.0;
				specialScore = 0.0;
				scoreLabel.setText("Score: "+score);
				personalBest.setText("Personal Best: "+highScore);
				//CLEAR STATS info dialog
				if (languageSelect.getSelectedItem().toString().equals("English")){
					JOptionPane.showMessageDialog(this, ClearStatistics.clearStats("All Spelling Statistics Cleared"), "VOXSPELL CLEAR STATS", JOptionPane.INFORMATION_MESSAGE);
				} else if (languageSelect.getSelectedItem().toString().equals("Chinese")){				
					JOptionPane.showMessageDialog(this, ClearStatistics.clearStats("所有拼寫統計已清除"), "VOXSPELL CLEAR STATS", JOptionPane.INFORMATION_MESSAGE);
				} else if (languageSelect.getSelectedItem().toString().equals("Japanese")){	
					JOptionPane.showMessageDialog(this, ClearStatistics.clearStats("すべての統計情報をクリア"), "VOXSPELL CLEAR STATS", JOptionPane.INFORMATION_MESSAGE);
				}
			}

		}
		else if (ae.getSource() == enter) {
			if(spellList.status.equals("ANSWERING")){
				takeInUserInput();
			}
		}
		else if (ae.getSource() == sentenceListen){
			// this button only works when the voice generator is not generating any voice
			if(!spellList.status.equals("ASKING")&&respellGen.isDone()&&spellList.status.equals("ANSWERING")){
				respellGen = new VoiceGenerator(theVoice,theVoiceStretch,theVoicePitch,theVoiceRange);
				respellGen.setTextForSwingWorker("", spellList.getCurrentExample());
				respellGen.execute();
				userInput.requestFocus();
			}
		}
		else if (ae.getSource() == wordListen) {
			// this button only works when the voice generator is not generating any voice
			if(!spellList.status.equals("ASKING")&&respellGen.isDone()&&spellList.status.equals("ANSWERING")){
				respellGen = new VoiceGenerator(theVoice,theVoiceStretch,theVoicePitch,theVoiceRange);
				respellGen.setTextForSwingWorker("", spellList.getCurrentWord());
				respellGen.execute();
				userInput.requestFocus();
			}
		}
		else if (ae.getSource() == voxSelect) {

			// sets the chosen voice
			if (voxSelect.getSelectedItem().toString().equals("Default")){
				theVoice = Voice.DEFAULT;

			} else if (voxSelect.getSelectedItem().toString().equals("Auckland")){				
				theVoice = Voice.AUCKLAND;
			}
			voiceGen.setVoice(theVoice);
		}
		else if (ae.getSource() == languageSelect) {
			// sets the button language
			if (languageSelect.getSelectedItem().toString().equals("English")){
				foreign = false;
				newQuiz.setText("New Spelling Quiz");
				reviewMistakes.setText("Review Mistakes");
				viewStats.setText("View Statistics");
				clearStats.setText("Clear Statistics");
				addList.setText("Import extra spelling levels");
				_replayLevel.setText("Replay level");
				_nextLevel.setText("Next level");
				_videoReward.setText("Play video");
				_specialVideoReward.setText("Play fast video");
				_done.setText("Done");
				spellPrompt.setText("Please spell here:");
				enter.setText("Enter");
				help.setText("Help");
				sentenceListen.setText("Listen to a sentence");
				wordListen.setText("Listen to the word again");
				voxPrompt.setText("Voice Toggle");
				stopQuiz.setText("Stop Quiz");
			} else if (languageSelect.getSelectedItem().toString().equals("Chinese")){				
				foreign = true;
				newQuiz.setText("新拼字測驗");
				reviewMistakes.setText("糾正錯字");
				viewStats.setText("查看統計");
				clearStats.setText("清除統計");
				addList.setText("添加自定的詞字表");
				_replayLevel.setText("再試拼字級");
				_nextLevel.setText("試下拼字級");
				_videoReward.setText("視頻廣播");
				_specialVideoReward.setText("特殊視頻廣播");
				_done.setText("結束");
				spellPrompt.setText("請下面拼寫:");
				enter.setText("確認");
				help.setText("說明");
				sentenceListen.setText("聽聽一個例子");
				wordListen.setText("再次聽聽字");
				voxPrompt.setText("語音切換");
				stopQuiz.setText("停止測驗");
			} else if (languageSelect.getSelectedItem().toString().equals("Japanese")){	
				foreign = true;
				newQuiz.setText("新しいスペルクイズ");
				reviewMistakes.setText("ミスを直す");
				viewStats.setText("統計を分析");
				clearStats.setText("統計を捨てる");
				addList.setText("カスタムレベルを追加");
				_replayLevel.setText("レベルリプレイ");
				_nextLevel.setText("次のレベル");
				_videoReward.setText("ビデオ放送");
				_specialVideoReward.setText("特別放送");
				_done.setText("終了");
				spellPrompt.setText("以下スペルください:");
				enter.setText("確認");
				help.setText("説明");
				sentenceListen.setText("例を聞く");
				wordListen.setText("再び言葉を聞く");
				voxPrompt.setText("語調変更");
				stopQuiz.setText("ストップ");
			}
		} else if (ae.getSource() == addList){
			JFileChooser wordList = new JFileChooser();
			wordList.setDialogTitle("Choose wordlist");
			wordList.setAcceptAllFileFilterUsed(false);
			FileFilter filter = new FileNameExtensionFilter("Only text files", "txt");
			wordList.setFileFilter(filter);
			int val = wordList.showSaveDialog(null);
			if (val == JFileChooser.APPROVE_OPTION){
				File file = wordList.getSelectedFile();
				BufferedReader test;
				try {
					test = new BufferedReader(new FileReader(file));
					String text = test.readLine();
					if (text == null){
						JOptionPane.showMessageDialog(frame,
								"Please select a non-empty text file.",
								"Textfile Error",
								JOptionPane.ERROR_MESSAGE);
						val = wordList.showSaveDialog(null);
						file = wordList.getSelectedFile();
					} else if (!text.contains("%")){
						JOptionPane.showMessageDialog(frame,
								"Please select a text file with % labels.",
								"Textfile Error",
								JOptionPane.ERROR_MESSAGE);
						val = wordList.showSaveDialog(null);
						file = wordList.getSelectedFile();
					} 
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				InputStream input = null;
				OutputStream output = null;
				try{
					File fileCopy =new File("wordList");
					input = new FileInputStream(file);
					output = new FileOutputStream(fileCopy);
					byte[] buffer = new byte[1024];
					int length;
					//copy the file content in bytes
					while ((length = input.read(buffer)) > 0){
						output.write(buffer, 0, length);
					}
					input.close();
					output.close();
				}catch(IOException e){
					e.printStackTrace();
				}
			}
			if (val == JFileChooser.CANCEL_OPTION){
			}
		} else if (ae.getSource() == help){
			if (languageSelect.getSelectedItem().toString().equals("English")){
				JOptionPane.showMessageDialog(null, "Press \"New Spelling Quiz\" to start a new quiz.\n"
						+ "Press \"Review Mistakes\" to review failed words in New Spelling Quiz.\n"
						+ "Press \"View Statistics\" to view your statistics for a particular word list.\n"
						+ "Press \"Clear Statistics\" to clear all current statistics on attempted words.\n"
						+ "Press \"Import extra spelling levels\" to try spelling your own words.\n"
						+ "You can change the language of certain buttons.\n"
						+ "Press \"Help\" to find specific guidance on using this spelling aid in your native language.", "Help", JOptionPane.INFORMATION_MESSAGE);
			} else if (languageSelect.getSelectedItem().toString().equals("Chinese")){				
				JOptionPane.showMessageDialog(null, "請按 \"新拼字測驗\" 去開始拼寫英語單詞。\n"
						+ "請按 \"糾正錯字\" 去復習拼錯的英語單詞。\n"
						+ "請按 \"查看統計\" 去檢查拼寫英語單詞的進度。\n"
						+ "請按 \"清除統計\" 去清除所有的統計數據。\n"
						+ "請按 \"添加自定的詞字表\" 去拼寫你自己的英語單詞。\n"
						+ "你可以改某些按鈕的標籤。\n"
						+ "請按 \"說明\" 去看如何使用這個拼寫工具的指導。", "說明", JOptionPane.INFORMATION_MESSAGE);
			} else if (languageSelect.getSelectedItem().toString().equals("Japanese")){	
				JOptionPane.showMessageDialog(null, "新しいクイズを起動するために、\"新しいスペルクイズ\" を押してください。\n"
						+ "間違った単語を改正するために、 \"ミスを直す\" を押してください。\n"
						+ "英単語のスペルの進行状況を表示するために、 \"統計を分析\" を押してください。\n"
						+ "すべてのデータを削除するために、\"統計を捨てる\" を押してください。\n"
						+ "自分の言葉をスペル試してみるために、\"カスタムレベルを追加\" を押してください。\n"
						+ "いくつかのボタンのラベルを変更することができます。\n"
						+ "このアプリを使用する方法を見つけるために、\"説明\" を押してください。", "説明", JOptionPane.INFORMATION_MESSAGE);
			}
		}
		else if (ae.getSource() == stopQuiz) {
			languageSelect.setVisible(false);
			addList.setVisible(false);
			if(spellList.status.equals("ANSWERING")){
				stopQuiz.setEnabled(true);
				quizInterrupted = true;
				window.append(pColor,"\n\n Quiz has been cancelled. \n\n" ,18);
				revertToOriginal();	
				progressBar.setVisible(false);
				spellList.recordFailedAndTriedWordsFromLevel();
			} else {
				stopQuiz.setEnabled(false);
			}
		}
		else if (ae.getSource() == _replayLevel) {
			continueQuiz();
			//Display new spelling message to GUI
			window.append(pColor,"+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+\n",18);
			window.append(pColor,"                    New Spelling Quiz ( Level "+ spellList.getCurrentLevel() +" )\n",18);
			window.append(pColor,"+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+\n\n",18);

			//Start asking questions
			spellList.createLevelList(spellList.getCurrentLevel(), "new",this);
			accuracyIndicator.setText("Level "+ spellList.getCurrentLevel()+" Accuracy: "+spellList.getLvlAccuracy()+"%");
			currentAcc = spellList.getLvlAccuracy();
			setLabelColors(currentAcc,score,spellList);
			questionAsker = spellList.getQuestionAsker();
			questionAsker.execute();
		}
		else if (ae.getSource() == _nextLevel) {
			continueQuiz();
			int nextLevel = spellList.getCurrentLevel()+1;
			//Display new spelling message to GUI

			window.append(pColor,"+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+\n",18);
			window.append(pColor,"                    New Spelling Quiz ( Level "+ nextLevel +" )\n",18);
			window.append(pColor,"+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+\n\n",18);

			//Start asking questions
			spellList.createLevelList(nextLevel, "new",this);
			accuracyIndicator.setText("Level "+ spellList.getCurrentLevel()+" Accuracy: "+spellList.getLvlAccuracy()+"%");
			currentAcc = spellList.getLvlAccuracy();
			setLabelColors(currentAcc,score,spellList);
			questionAsker = spellList.getQuestionAsker();
			questionAsker.execute();
		}
		else if (ae.getSource() == _videoReward) {
			if (_videoReward.getText().equals("Audio Reward")&&score >= 5000.0){
				AudioPlayer.stopSound();
				new SoundPlayer();
			} else {
				AudioPlayer.stopSound();
				new VideoPlayer(1);
			}

		}
		else if (ae.getSource() == _specialVideoReward) {
			AudioPlayer.stopSound();
			new VideoPlayer(2);
		}
		else if (ae.getSource() == _done) {
			revertToOriginal(); //Display main GUI again
			// Scroll bar set to the top
			window.setCaretPosition(1);
			// Scroll bar set to the top
			window.setCaretPosition(0);
		}
	}
	// get the text from the text box then clears it
	private String clearTxtBox(){
		String theReturn = userInput.getText();
		userInput.setText("");
		return theReturn;
	}

	// checks that all the files that are storing the statistics are present and create any files that do not exist
	private void makeSureAllNecessaryFilesArePresent() {
		File spelling_aid_failed = new File(".spelling_aid_failed");
		File spelling_aid_statistics = new File(".spelling_aid_statistics");
		File spelling_aid_tried_words = new File(".spelling_aid_tried_words");
		File spelling_aid_accuracy = new File(".spelling_aid_accuracy");
		try{
			if(! spelling_aid_failed.exists()){
				spelling_aid_failed.createNewFile();
			}
			if(! spelling_aid_statistics.exists()){
				spelling_aid_statistics.createNewFile();
			}
			if(! spelling_aid_tried_words.exists()){
				spelling_aid_tried_words.createNewFile();
			}
			if(! spelling_aid_accuracy.exists()){
				spelling_aid_accuracy.createNewFile();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		// create special video with a background swingworker thread when the app starts
		VideoCreator createSpecialVideo = new VideoCreator();
		createSpecialVideo.execute();
	}

	// Method that only sets tabs at the top of the GUI to be visible
	public void revertToOriginal() {
		if (!SpellingList.playingTrack7 && !SpellingList.playingTrack1 && !SpellingList.playingTrack2){
			AudioPlayer.playLoopSound(".ON/Track3.wav", -15.0f);
		}
		frame.getContentPane().add(tabs, BorderLayout.NORTH);
		controller.setVisible(false);
		nextState.setVisible(false);
		// clear the window
		window.setText("");
		//Display welcome message to GUI
		window.append(pColor,"                   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+\n",18);
		window.append(pColor,"                                              Welcome to the Spelling Aid\n",18);
		window.append(pColor,"                   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+\n\n",18);
		window.append(pColor,"                                       Please select your language:\n\n",15);
		window.append(pColor,"\n\n                                     Please select from one of the options above:\n\n\n",15);
		window.append(tColor, "                                                      ", 16);
		StyleConstants.setIcon(style, new ImageIcon("200.gif"));

		languageSelect.setVisible(true);
		addList.setVisible(true);
		help.setVisible(true);

	}

	// Method that only sets end of quiz options at the bottom of the GUI to be visible
	public void changeToNextState() {
		if (spellList.getCorrectAns() == 10){
			AudioPlayer.playLoopSound(".ON/Track3.wav", -20.0f);
		}
		controller.setVisible(false);
		frame.getContentPane().remove(progressBar);
		if(spellList.getCorrectAns() < 9){
			_nextLevel.setToolTipText("This button is only enabled if you get at least 9/10 right on the first attempt.");
			_videoReward.setToolTipText("This button is only enabled if you get at least 9/10 right on the first attempt.");
			_specialVideoReward.setToolTipText("This button is only enabled if you get at least 9/10 right on the first attempt.");
			_nextLevel.setEnabled(false);
			_videoReward.setEnabled(false);
			_specialVideoReward.setEnabled(false);
		} else {
			_nextLevel.setEnabled(true);
			_videoReward.setEnabled(true);
			_specialVideoReward.setEnabled(true);
		}
		// disable next level as the thing to do before setting the panel to be visible to make the next level button is disabled at lvl 11
		if (spellList.getCurrentLevel() == 11){
			_nextLevel.setToolTipText("This button cannot be used now. Level 11 is the highest level.");
			_nextLevel.setEnabled(false);
		} 
		nextState.setVisible(true);
	}

	// Method to set colors of score and accuracy labels
	// Green color for score means special audio reward can be played
	public void setLabelColors(double acc, double sc, SpellingList sl) {
		if (acc>= 80.0){
			accuracyIndicator.setForeground(hColor);
		} else if (acc>= 60.0){
			accuracyIndicator.setForeground(tColor);
		} else {
			accuracyIndicator.setForeground(qColor);
		}
		if (sl.getCorrectAns() == 10 && sc>= 5000.0){
			scoreLabel.setForeground(hColor);
			_videoReward.setText("Audio Reward");
		} else if (sc>= 5000.0){
			scoreLabel.setForeground(hColor);
			_videoReward.setText("Play video");
		}else if (sc > 0.0){
			scoreLabel.setForeground(tColor);
			_videoReward.setText("Play video");
		} else {
			scoreLabel.setForeground(qColor);
			_videoReward.setText("Play video");
		}
	}

	// Method to change spelling modes
	public void changeModes(){
		languageSelect.setVisible(false);
		addList.setVisible(false);
		help.setVisible(false);
		AudioPlayer.stopSound();
		frame.getContentPane().remove(tabs);
		controller.setVisible(true);
		if(!notFirstTime){
			// clear the window
			window.setText("");
			notFirstTime = true;
		}
		// clear the window
		window.setText("");

	}

	// Method to continue with spelling quiz at the end of a previous quiz
	public void continueQuiz(){
		reviewMode = false;
		quizInterrupted = false;
		stopQuiz.setText("Stop Quiz");
		// Scroll bar set to an arbitrary value
		window.setCaretPosition(1);
		// Scroll bar set to the top
		window.setCaretPosition(0);
		AudioPlayer.stopSound();
		progressBar.setValue(0);
		progressBar.setVisible(true);
		frame.getContentPane().add(progressBar, BorderLayout.NORTH);
		controller.setVisible(true);
		nextState.setVisible(false);
		// clear the window
		window.setText("");
	}
}