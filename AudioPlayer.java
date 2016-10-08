package spelling;

import java.io.File;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

/**
 * This class enables music effects and background music to be played with the spelling application
 * @author hchu167
 */

public class AudioPlayer {
	static double durationInSeconds;
	static Clip clip;
	// This method plays a looped music clip to ensure GUI is functioning properly
	public static void playLoopSound(String file, float volume) {

		try {
			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(file).getAbsoluteFile());
			clip = AudioSystem.getClip();
			clip.open(audioInputStream);

			FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);

			gainControl.setValue(volume); 


			clip.loop(Clip.LOOP_CONTINUOUSLY);
			clip.start();
		} catch(Exception ex) {
			System.out.println("Error with playing sound.");
			ex.printStackTrace();
		}
	}

	public static void playSound(String file) {
		try {
			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(file).getAbsoluteFile());
			AudioFormat format = audioInputStream.getFormat();
			long frames = audioInputStream.getFrameLength();
			durationInSeconds = (frames+0.0) / format.getFrameRate();  
			clip = AudioSystem.getClip();
			clip.open(audioInputStream);
			FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
			if (!file.equals(".ON/Bit5.wav")){
				gainControl.setValue(-30.0f); // soften sound effects
			} else {
				gainControl.setValue(-15.0f); // soften sound effect less
			}
			clip.start();
		} catch(Exception ex) {
			System.out.println("Error with playing sound.");
			ex.printStackTrace();
		}
	}

	static double getLength() {
		return durationInSeconds;
	}
	static void stopSound() {
		clip.stop(); //stop playing the looped clip
		clip.close(); // this step is needed to free eclipse resources
	}
}