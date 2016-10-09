package spelling;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * 
 * This class creators a level selector that allows the user to select a starting level
 * when pressing the 'New Spelling Quiz' button to start a quiz
 * @author hchu167
 * @collaborator yyap601
 *
 */
public class LevelSelector implements ActionListener{
	//Creating buttons
	final JLabel levelPrompt = new JLabel("Select a level:");
	public Color qColor = new Color(255,113,126); //spelling (q)uiz functionality color
	public Color hColor = new Color(151, 195, 10); //spelling (h)elper color
	public Color wColor = new Color(248,248,242); //(w)hite color - better version for eyes
	public Color DqColor = qColor.darker();
	public int newSpellingLevel = -1;
	// Constructor for level selector
	public LevelSelector(){
		int dialog = JOptionPane.showOptionDialog(null,
				makePanel(),
				"VOXSPELL LEVEL SELECTOR",
				JOptionPane.YES_OPTION, JOptionPane.PLAIN_MESSAGE, null, new String[]{} , null);
		if (dialog == JOptionPane.CLOSED_OPTION){
			SpellingList.levelNames.clear();
		}
	}

	// Method to make the main panel of the level selector
	private JPanel makePanel() {

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		levelPrompt.setForeground(hColor);
		levelPrompt.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.setBackground(wColor);
		panel.add(levelPrompt);
		makeButtons(SpellingList.levelNames, panel);
		return panel;

	}
	//Set operations for different buttons
	public void actionPerformed(ActionEvent ae) {
		//Setting internal representation for each option chosen
		Object o = ae.getSource();
		JButton b = null;
		String bLabel = "";
		if(o instanceof JButton){
			b = (JButton)o;
		}
		if(b != null) {
			bLabel = b.getText();
			String[] splitLabel = bLabel.split(" ");
			newSpellingLevel = Integer.parseInt(splitLabel[1]);
		}
		JOptionPane.getRootFrame().dispose();  
	}

	public void makeButtons(ArrayList <String> list, JPanel p) {
		for (int i = 0; i < list.size(); i++){
			JButton test= new JButton("Level "+ (i+1));
			test.setAlignmentX(Component.CENTER_ALIGNMENT);
			test.addActionListener(this);
			test.setForeground(DqColor);
			p.add(test);
		}
	}
	public int getLevel(){
		return newSpellingLevel;
	}
}