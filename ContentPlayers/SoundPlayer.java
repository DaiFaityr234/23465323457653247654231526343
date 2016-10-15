package spelling.ContentPlayers;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;

@SuppressWarnings("serial")
public class SoundPlayer extends JFrame{
	JPanel contentPane;
	AudioFormat audioFormat;
	AudioInputStream audioInputStream;
	SourceDataLine sourceDataLine;
	File soundFile;
	long fileLength;
	int frameSize;
	int songLength;
	float frameRate;
	float duration;
	long videoTime;
	int value = 0;
	boolean stopPlayback = false;
	public JLabel label;
	final JButton stopBtn = new JButton("STOP");
	final JButton playBtn = new JButton("PLAY");
	final JSlider bar = new JSlider(0,songLength,0);
	JTextPane textPane;
	
	Timer timer;
	private void playAudio() {
		try{
			soundFile =new File(".ON/TrackX.wav");
			videoTime = soundFile.length();
			audioInputStream = AudioSystem.getAudioInputStream(soundFile);
			audioFormat = audioInputStream.getFormat();
			fileLength = soundFile.length();
			frameSize = audioFormat.getFrameSize();
			frameRate = audioFormat.getFrameRate();
			duration = (fileLength / (frameSize * frameRate));
			DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class,audioFormat);
			sourceDataLine = (SourceDataLine)AudioSystem.getLine(dataLineInfo);
			songLength = (int)(sourceDataLine.getMicrosecondPosition()/1000);
			new PlayThread().start();
		}catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	} 
	public SoundPlayer(){
		textPane = new JTextPane();
	    StyledDocument doc = (StyledDocument) textPane.getDocument();

	    Style style = doc.addStyle("StyleName", null);
	    StyleConstants.setIcon(style, new ImageIcon("200w.gif"));

	    try {
			doc.insertString(doc.getLength(), "ignored text", style);
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		}
		contentPane = (JPanel) getContentPane();
        contentPane.setLayout(new BorderLayout());
        setSize(new Dimension(50, 50));
		stopBtn.setEnabled(false);
		playBtn.setEnabled(true);
		playBtn.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent e){
						stopBtn.setEnabled(true);
						playBtn.setEnabled(false);
						playAudio();
					}
				}
				);
		stopBtn.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent e){
						stopPlayback = true;
					}
				}
				);

		contentPane.add(playBtn,"West");
		contentPane.add(stopBtn,"East");
		contentPane.add(textPane,"North");
		//contentPane.add(bar,"South");
		textPane.setEditable(false);
		setTitle("Sound Player");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(208,260);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setLocationRelativeTo(null);
		setResizable(false);
		setVisible(true);
		this.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
			stopPlayback = true;
			}
		});
	}
	public String getImagePath(BufferedImage bi) {
	    try {
	        File temp = File.createTempFile("image", ".gif");
	        ImageIO.write(bi, "GIF", new FileOutputStream(temp));
	        return temp.getAbsolutePath();
	    } catch (IOException ex) {
	        ex.printStackTrace();
	    }
	    return null;
	}

	class PlayThread extends Thread{
		byte tempBuffer[] = new byte[10000];

		public void run(){
			try{
				sourceDataLine.open(audioFormat);
				sourceDataLine.start();
				int counter;
				while((counter = audioInputStream.read(tempBuffer,0,tempBuffer.length)) != -1 && stopPlayback == false){
					if(counter > 0){
						sourceDataLine.write(tempBuffer, 0, counter);
					}
				} 
				sourceDataLine.drain();
				sourceDataLine.close();

				stopBtn.setEnabled(false);
				playBtn.setEnabled(true);
				stopPlayback = false;
			}catch (Exception e) {
				e.printStackTrace();
				System.exit(0);
			}
		}
	}


}
