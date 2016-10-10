package spelling;

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
import javax.swing.text.DefaultCaret;

import spelling.SpellingList.AnswerChecker;
import spelling.SpellingList.QuestionAsker;
import spelling.VoiceGenerator.Voice;

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
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public JComboBox languageSelect = new JComboBox(new String[]{"English","Chinese","Japanese"});
	public JButton addList = new JButton("Import extra spelling levels");
	public boolean foreign = false;
	public JScrollPane scrollBar = new JScrollPane(window);
	public LevelSelector levelSelect;
	//Colours

	public Color tColor = new Color(31,190,214); //spelling (t)ab color
	public Color qColor = new Color(255,113,126); //spelling (q)uiz functionality color
	public Color hColor = new Color(151, 195, 10); //spelling (h)elper color
	public Color bColor = new Color(192, 188, 182); // (b)ackground button color
	public Color pColor = new Color(85, 85, 85); //(p)rogress bar text color
	public Color wColor = new Color(248,248,242); //(w)hite color - better version for eyes
	public Color gColor = new Color(255,223,0); //(g)old color - for 10/10
	//Layout for main GUI
	FlowLayout options = new FlowLayout();

	//To determine whether to clear out welcome text, if true = don't clear
	boolean notFirstTime; 
	double currentAcc;
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
				if (currentAcc>= 80.0){
					accuracyIndicator.setForeground(hColor);
				} else if (currentAcc>= 60.0){
					accuracyIndicator.setForeground(tColor);
				} else {
					accuracyIndicator.setForeground(qColor);
				}
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
		newQuiz.setPreferredSize(new Dimension(150, 30));
		newQuiz.setOpaque(true);
		newQuiz.setForeground(qColor);
		newQuiz.setBackground(bColor);
		tabs.add(newQuiz);
		reviewMistakes.setPreferredSize(new Dimension(150, 30));
		reviewMistakes.setOpaque(true);
		reviewMistakes.setForeground(qColor);
		reviewMistakes.setBackground(bColor);
		tabs.add(reviewMistakes);
		viewStats.setPreferredSize(new Dimension(150, 30));
		tabs.add(viewStats);
		viewStats.setOpaque(true);
		viewStats.setForeground(hColor);
		viewStats.setBackground(bColor);
		clearStats.setPreferredSize(new Dimension(150, 30));
		clearStats.setOpaque(true);
		clearStats.setForeground(hColor);
		clearStats.setBackground(bColor);
		tabs.add(clearStats);

		//Setting sizes of nextState buttons
		_replayLevel.setPreferredSize(new Dimension(120, 30));
		_replayLevel.setOpaque(true);
		_replayLevel.setForeground(qColor);
		_replayLevel.setBackground(bColor);
		nextState.add(_replayLevel);

		_nextLevel.setPreferredSize(new Dimension(120, 30));
		_nextLevel.setOpaque(true);
		_nextLevel.setForeground(qColor);
		_nextLevel.setBackground(bColor);
		nextState.add(_nextLevel);

		_videoReward.setPreferredSize(new Dimension(120, 30));
		_videoReward.setOpaque(true);
		_videoReward.setForeground(hColor);
		_videoReward.setBackground(bColor);
		nextState.add(_videoReward);
		_specialVideoReward.setOpaque(true);
		_specialVideoReward.setForeground(hColor);
		_specialVideoReward.setBackground(bColor);
		_specialVideoReward.setPreferredSize(new Dimension(120, 30));
		nextState.add(_specialVideoReward);
		_done.setOpaque(true);
		_done.setForeground(pColor);
		_done.setBackground(bColor);
		_done.setPreferredSize(new Dimension(120, 30));
		nextState.add(_done);

		//Spacer to format components on right hand side of GUI
		controller.add(Box.createRigidArea(new Dimension(40,10)));

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

		progressBar.setStringPainted(true);
		progressBar.setForeground(pColor);

		//Spacer to format components on right hand side of GUI
		controller.add(Box.createRigidArea(new Dimension(40,83)));

		//Setting size for "Listen to the word again" button
		wordListen.setPreferredSize(new Dimension(150, 40));
		wordListen.setAlignmentX(Component.CENTER_ALIGNMENT);
		wordListen.setForeground(qColor);
		controller.add(wordListen);

		//Spacer to format components on right hand side of GUI
		controller.add(Box.createRigidArea(new Dimension(40,100)));

		//Setting size for "Stop Quiz" button
		stopQuiz.setPreferredSize(new Dimension(200, 40));
		stopQuiz.setAlignmentX(Component.CENTER_ALIGNMENT);
		stopQuiz.setForeground(hColor);
		controller.add(stopQuiz);

		//Spacer to format components on right hand side of GUI
		controller.add(Box.createRigidArea(new Dimension(40,100)));

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
		enter.addActionListener(this);
		stopQuiz.addActionListener(this);
		voxSelect.addActionListener(this);
		languageSelect.addActionListener(this);
		addList.addActionListener(this);
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
		window.append(pColor,"\n\n                                     Please select from one of the options above:",15);


		languageSelect.setSize( languageSelect.getPreferredSize() );
		languageSelect.setLocation(365, 83);
		window.add( languageSelect );
		addList.setSize(addList.getPreferredSize());
		addList.setLocation(210, 120);
		window.add(addList);
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
				SpellingList.extraLevels = false;
				languageSelect.setVisible(false);
				addList.setVisible(false);
				AudioPlayer.stopSound();
				frame.getContentPane().remove(tabs);
				progressBar.setVisible(true);
				frame.getContentPane().add(progressBar, BorderLayout.NORTH);
				progressBar.setValue(0);
				controller.setVisible(true);
				if(!notFirstTime){
					// clear the window
					window.setText("");
					notFirstTime = true;
				}
				// clear the window
				window.setText("");
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
				if (currentAcc>= 80.0){
					accuracyIndicator.setForeground(hColor);
				} else if (currentAcc>= 60.0){
					accuracyIndicator.setForeground(tColor);
				} else {
					accuracyIndicator.setForeground(qColor);
				}
				questionAsker = spellList.getQuestionAsker();
				questionAsker.execute();
				//spellingLvl=spellList.getQuestion(); // initiate swing worker
				//spellingLvl.execute(); // execute quiz
			} else if (levelSelect.getLevel()==-1 && SpellingList.extraLevels) {
				if (CustomSelector.getExtra().equals("NULL")){
					SpellingList.specialNames.clear();
				}
				if (!CustomSelector.getExtra().equals("NULL")){
					languageSelect.setVisible(false);
					addList.setVisible(false);
					AudioPlayer.stopSound();
					frame.getContentPane().remove(tabs);
					progressBar.setVisible(true);
					frame.getContentPane().add(progressBar, BorderLayout.NORTH);
					progressBar.setValue(0);
					controller.setVisible(true);
					if(!notFirstTime){
						// clear the window
						window.setText("");
						notFirstTime = true;
					}
					// clear the window
					window.setText("");
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
				languageSelect.setVisible(false);
				addList.setVisible(false);
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
				//Display new spelling message to GUI
				window.append(pColor,"+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+\n",18);
				window.append(pColor,"                  Review Spelling Quiz ( Level "+ levelSelect.getLevel() +" )\n",18);
				window.append(pColor,"+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+\n\n",18);

				spellList.createLevelList(levelSelect.getLevel(), "review",this);
				accuracyIndicator.setText("Level "+ spellList.getCurrentLevel());
				questionAsker = spellList.getQuestionAsker();
				questionAsker.execute();
				//spellingLvl=spellList.getQuestion(); // initiate swing worker
				//spellingLvl.execute(); // execute quiz

			}
		}
		else if (ae.getSource() == viewStats) {
			languageSelect.setVisible(false);
			addList.setVisible(false);
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
			languageSelect.setVisible(false);
			addList.setVisible(false);
			// Scroll bar set to an arbitrary value
			window.setCaretPosition(1);
			// Scroll bar set to the top
			window.setCaretPosition(0);
			window.setText("");
			window.append(pColor,"                   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+\n",18);
			window.append(pColor,"                                              All Spelling Statistics Cleared \n",18);
			window.append(pColor,"                   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+\n",18);
			//CLEAR STATS info dialog
			JOptionPane.showMessageDialog(this, ClearStatistics.clearStats(), "VOXSPELL CLEAR STATS", JOptionPane.INFORMATION_MESSAGE);
		}
		else if (ae.getSource() == enter) {
			takeInUserInput();
		}
		else if (ae.getSource() == wordListen) {
			// this button only works when the voice generator is not generating any voice
			if(!spellList.status.equals("ASKING")&&respellGen.isDone()){
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
				wordListen.setText("Listen to the word again");
				voxPrompt.setText("Voice Toggle");
				stopQuiz.setText("Stop Quiz");
			} else if (languageSelect.getSelectedItem().toString().equals("Chinese")){				
				foreign = true;
				newQuiz.setText("新拼字測驗");
				reviewMistakes.setText("糾正錯字");
				viewStats.setText("查看統計");
				clearStats.setText("清除統計");
				addList.setText("添加自定的拼字級");
				_replayLevel.setText("再試拼字級");
				_nextLevel.setText("試下拼字級");
				_videoReward.setText("視頻廣播");
				_specialVideoReward.setText("特殊視頻廣播");
				_done.setText("結束");
				spellPrompt.setText("請下面拼寫:");
				enter.setText("確認");
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
		}
		else if (ae.getSource() == stopQuiz) {
			languageSelect.setVisible(false);
			addList.setVisible(false);
			if(spellList.status.equals("ANSWERING")){
				quizInterrupted = true;
				window.append(pColor,"\n\n Quiz has been cancelled. \n\n" ,18);
				revertToOriginal();	
				progressBar.setVisible(false);
				spellList.recordFailedAndTriedWordsFromLevel();
			}
		}
		else if (ae.getSource() == _replayLevel) {
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
			//Display new spelling message to GUI

			window.append(pColor,"+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+\n",18);
			window.append(pColor,"                    New Spelling Quiz ( Level "+ spellList.getCurrentLevel() +" )\n",18);
			window.append(pColor,"+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+\n\n",18);

			//Start asking questions
			spellList.createLevelList(spellList.getCurrentLevel(), "new",this);
			accuracyIndicator.setText("Level "+ spellList.getCurrentLevel()+" Accuracy: "+spellList.getLvlAccuracy()+"%");
			currentAcc = spellList.getLvlAccuracy();
			if (currentAcc>= 80.0){
				accuracyIndicator.setForeground(hColor);
			} else if (currentAcc>= 60.0){
				accuracyIndicator.setForeground(tColor);
			} else {
				accuracyIndicator.setForeground(qColor);
			}
			questionAsker = spellList.getQuestionAsker();
			questionAsker.execute();
		}
		else if (ae.getSource() == _nextLevel) {
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
			int nextLevel = spellList.getCurrentLevel()+1;
			// clear the window
			window.setText("");
			//Display new spelling message to GUI

			window.append(pColor,"+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+\n",18);
			window.append(pColor,"                    New Spelling Quiz ( Level "+ nextLevel +" )\n",18);
			window.append(pColor,"+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+\n\n",18);

			//Start asking questions
			spellList.createLevelList(nextLevel, "new",this);
			accuracyIndicator.setText("Level "+ spellList.getCurrentLevel()+" Accuracy: "+spellList.getLvlAccuracy()+"%");
			currentAcc = spellList.getLvlAccuracy();
			if (currentAcc>= 80.0){
				accuracyIndicator.setForeground(hColor);
			} else if (currentAcc>= 60.0){
				accuracyIndicator.setForeground(tColor);
			} else {
				accuracyIndicator.setForeground(qColor);
			}
			questionAsker = spellList.getQuestionAsker();
			questionAsker.execute();
		}
		else if (ae.getSource() == _videoReward) {
			AudioPlayer.stopSound();
			new MediaPlayer(1);
		}
		else if (ae.getSource() == _specialVideoReward) {
			AudioPlayer.stopSound();
			new MediaPlayer(2);
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
		if (quizInterrupted && !SpellingList.playingTrack7){
			AudioPlayer.playLoopSound(".ON/Track3.wav",-0.0f);
		}
		frame.getContentPane().add(tabs, BorderLayout.NORTH);
		controller.setVisible(false);
		nextState.setVisible(false);
	}

	// Method that only sets end of quiz options at the bottom of the GUI to be visible
	public void changeToNextState() {
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

}