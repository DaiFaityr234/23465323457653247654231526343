package spelling.HelperClasses;

/**
 * 
 * This class creates a custom JTextPane that easily appends
 * colored text and gifs to the main text area with text manipulation
 * @author hchu167
 *
 */
import java.awt.Color;

import javax.swing.ImageIcon;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

@SuppressWarnings("serial")
public class ColorPane extends JTextPane {

	public void append(Color c, String s, int size) { 

		StyledDocument doc = this.getStyledDocument();
		Style style = this.addStyle("adsf", null);
		StyleConstants.setFontSize(style, size);
		StyleConstants.setForeground(style, c);
		try { doc.insertString(doc.getLength(), s,style); }
		catch (BadLocationException e){}

	}
	public void addGIF(String pic){
		StyledDocument doc = (StyledDocument) this.getDocument();
		Style style = doc.addStyle("StyleName", null); //Allows adding of image icon 
		StyleConstants.setIcon(style, new ImageIcon(pic));
		try {
			doc.insertString(doc.getLength(), "ignored text", style);
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		}
	}
}