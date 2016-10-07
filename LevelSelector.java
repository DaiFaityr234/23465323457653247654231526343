package spelling;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * 
 * This class creators a level selector that allows the user to select a starting level
 * when pressing the 'New Spelling Quiz' button to start a quiz
 * @authors yyap601 hchu167
 *
 */
public class LevelSelector implements ActionListener{
	//Creating buttons
	final JLabel levelPrompt = new JLabel("Select a level:");
	final JButton _levelone = new JButton("Level 1");
	final JButton _leveltwo = new JButton("Level 2");
	final JButton _levelthree = new JButton("Level 3");
	final JButton _levelfour = new JButton("Level 4");
	final JButton _levelfive = new JButton("Level 5");
	final JButton _levelsix = new JButton("Level 6");
	final JButton _levelseven = new JButton("Level 7");
	final JButton _leveleight = new JButton("Level 8");
	final JButton _levelnine = new JButton("Level 9");
	final JButton _levelten = new JButton("Level 10");
	final JButton _leveleleven = new JButton("Level 11");
	private int startLevel = 0;

	// Constructor for level selector
	public LevelSelector(){
		JOptionPane.showOptionDialog(null,
				makePanel(),
				"VOXSPELL LEVEL SELECTOR",
				JOptionPane.YES_OPTION, JOptionPane.PLAIN_MESSAGE, null, new String[]{} , null);
	}

	// Method to make the main panel of the level selector
	private JPanel makePanel() {
		
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		Color qColor = new Color(255,113,126); //spelling (q)uiz functionality color
		Color hColor = new Color(151, 195, 10); //spelling (h)elper color
		Color wColor = new Color(248,248,242); //(w)hite color - better version for eyes
		Color BqColor = qColor.brighter();
		Color DqColor = qColor.darker();
		//Set colors
		levelPrompt.setForeground(hColor);
		_levelone.setForeground(BqColor);
		_leveltwo.setForeground(BqColor);
		_levelthree.setForeground(BqColor);
		_levelfour.setForeground(BqColor);
		_levelfive.setForeground(qColor);
		_levelsix.setForeground(qColor);
		_levelseven.setForeground(qColor);
		_leveleight.setForeground(DqColor);
		_levelnine.setForeground(DqColor);
		_levelten.setForeground(DqColor);
		_leveleleven.setForeground(DqColor);
		panel.setBackground(wColor);
		//Centre all buttons
		levelPrompt.setAlignmentX(Component.CENTER_ALIGNMENT);
		_levelone.setAlignmentX(Component.CENTER_ALIGNMENT);
		_leveltwo.setAlignmentX(Component.CENTER_ALIGNMENT);
		_levelthree.setAlignmentX(Component.CENTER_ALIGNMENT);
		_levelfour.setAlignmentX(Component.CENTER_ALIGNMENT);
		_levelfive.setAlignmentX(Component.CENTER_ALIGNMENT);
		_levelsix.setAlignmentX(Component.CENTER_ALIGNMENT);
		_levelseven.setAlignmentX(Component.CENTER_ALIGNMENT);
		_leveleight.setAlignmentX(Component.CENTER_ALIGNMENT);
		_levelnine.setAlignmentX(Component.CENTER_ALIGNMENT);
		_levelten.setAlignmentX(Component.CENTER_ALIGNMENT);
		_leveleleven.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		//Add action listeners
		_levelone.addActionListener(this);
		_leveltwo.addActionListener(this);
		_levelthree.addActionListener(this);
		_levelfour.addActionListener(this);
		_levelfive.addActionListener(this);
		_levelsix.addActionListener(this);
		_levelseven.addActionListener(this);
		_leveleight.addActionListener(this);
		_levelnine.addActionListener(this);
		_levelten.addActionListener(this);
		_leveleleven.addActionListener(this);
		
		//Add all buttons
		panel.add(levelPrompt);
		panel.add(_levelone);
		panel.add(_leveltwo);
		panel.add(_levelthree);
		panel.add(_levelfour);
		panel.add(_levelfive);
		panel.add(_levelsix);
		panel.add(_levelseven);
		panel.add(_leveleight);
		panel.add(_levelnine);
		panel.add(_levelten);
		panel.add(_leveleleven);

		return panel;
	}
	//Set operations for different buttons
	public void actionPerformed(ActionEvent ae) {
		//Setting internal representation for each option chosen
		if (ae.getSource() == _levelone) {
			startLevel = 1;
			JOptionPane.getRootFrame().dispose();  
		}
		else if (ae.getSource() == _leveltwo) {
			startLevel = 2;
			JOptionPane.getRootFrame().dispose();  
		}
		else if (ae.getSource() == _levelthree) {
			startLevel = 3;
			JOptionPane.getRootFrame().dispose();  
		}
		else if (ae.getSource() == _levelfour) {
			startLevel = 4;
			JOptionPane.getRootFrame().dispose();  
		}
		else if (ae.getSource() == _levelfive) {
			startLevel = 5;
			JOptionPane.getRootFrame().dispose();  
		}
		else if (ae.getSource() == _levelsix) {
			startLevel = 6;
			JOptionPane.getRootFrame().dispose();  
		}
		else if (ae.getSource() == _levelseven) {
			startLevel = 7;
			JOptionPane.getRootFrame().dispose();  
		}
		else if (ae.getSource() == _leveleight) {
			startLevel = 8;
			JOptionPane.getRootFrame().dispose();  
		}
		else if (ae.getSource() == _levelnine) {
			startLevel = 9;
			JOptionPane.getRootFrame().dispose();  
		}
		else if (ae.getSource() == _levelten) {
			startLevel = 10;
			JOptionPane.getRootFrame().dispose();  
		}
		else if (ae.getSource() == _leveleleven) {
			startLevel = 11;
			JOptionPane.getRootFrame().dispose();  
		}
	}
	
	public int getLevel(){
		return startLevel;
	}
}