package spelling;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.Timer;

import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

/**
 * 
 * This class creates the main window that opens when the user chooses
 * to view a video reward, whether it is the special video or not.
 * @authors yyap601 hchu167
 *
 */
@SuppressWarnings("serial")
public class MediaPlayer extends JWindow{
	//Main embedded vlc player
	private final EmbeddedMediaPlayerComponent mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
	//Integer store to allow toggling of play/pause button
	public int pp = 0;

	//Main constructor of MediaPlayer
	public MediaPlayer(int i) {
		final JWindow frame = new JWindow();
		
		frame.setAlwaysOnTop(true);
		final EmbeddedMediaPlayer video = mediaPlayerComponent.getMediaPlayer();
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(mediaPlayerComponent, BorderLayout.CENTER); //add video to centre of window

		frame.setContentPane(panel);
		FlowLayout options = new FlowLayout();
		final JPanel tabs = new JPanel(); //set video functionality on top of window
		tabs.setLayout(options);
		options.setAlignment(FlowLayout.TRAILING);

		final JLabel videoTimer = new JLabel("Time: "+ "00:00:00"); //set timer on bottom of window
		videoTimer.setSize(60,25);
		panel.add(videoTimer, BorderLayout.NORTH);

		//Timer is used to display time in video
		final Timer timer = new Timer(500, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int time = (int) (video.getTime()/1000);
				if(time == 0){
					videoTimer.setText("Time: "+ "00:00:00");
				} else {

					//Some division is done to display the time in appropriate format
					int second = time%60;
					int min = time/60;
					int minute = min%60;
					int hour = min/60;
					String h = Integer.toString(hour);
					String m = Integer.toString(minute);
					String s = Integer.toString(second);
					if(second < 10){
						s="0"+s;
					}
					if(minute < 10){
						m="0"+m;
					}
					if(hour < 10){
						h="0"+h;
					}
					videoTimer.setText("Time: "+ h+":"+m+":"+s);
				}
			}
		});
		timer.start();

		//Make a mute button that stops playing sound of video
		JButton btnMute = new JButton("MUTE");
		btnMute.setPreferredSize(new Dimension(204, 30));
		tabs.add(btnMute);
		btnMute.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				video.mute();
			}
		});

		//Make a play/pause button that starts/stops playing the video graphics
		final JButton btnPause = new JButton("PAUSE");
		btnPause.setPreferredSize(new Dimension(204, 30));
		tabs.add(btnPause);
		btnPause.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				video.pause();
				//Toggle text of button to play/pause
				if (pp % 2 == 0){
					btnPause.setText("PLAY");
				}
				else {
					btnPause.setText("PAUSE");
				}
				pp++;
			}
		});

		//Make a rewind button that skips back in the video
		JButton btnRewind = new JButton("REWIND");
		btnRewind.setPreferredSize(new Dimension(204, 30));
		tabs.add(btnRewind);
		btnRewind.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				video.skip(-2500);
			}
		});

		//Make a fast forward button that skips forward in the video
		JButton btnFastForward = new JButton("FAST FORWARD");
		btnFastForward.setPreferredSize(new Dimension(204, 30));
		tabs.add(btnFastForward);
		btnFastForward.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				video.skip(2500);
			}
		});

		//Make a close button to close the video
		JButton btnClose = new JButton("CLOSE");
		btnClose.setPreferredSize(new Dimension(204, 30));
		tabs.add(btnClose);
		btnClose.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(video.isMute()){
					video.mute();
				}
				video.stop();
				frame.setVisible(false);
				if (SpellingList.playingTrack2){
					AudioPlayer.playLoopSound(".ON/Track2.wav",-15.0f);
				}
				if (SpellingList.playingTrack1){
					AudioPlayer.playLoopSound(".ON/Track1.wav",-15.0f);
				}
			}

		});

		//Set dimensions of window
		frame.setLocation(100, 100);
		frame.setSize(1050, 600);
		frame.setVisible(true);
		frame.add(tabs, BorderLayout.SOUTH);

		if (i == 1){
			video.playMedia("big_buck_bunny_1_minute.avi");
		}
		else if (i == 2){
			//Play extra video option
			video.playMedia("output.avi");
		}
	}

}