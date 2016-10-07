package spelling;

import java.io.File;

import javax.swing.SwingWorker;

/**
 * 
 * This class exists to create the video with special effects if it doesn't exist
 * @authors yyap601 hchu167
 *
 */
public class VideoCreator extends SwingWorker<Void,Void>{

	protected Void doInBackground() throws Exception {
		//Extra video option
		File f = new File("output.avi");
		if(!f.exists()) { 
			Tools.processStarter("ffmpeg -i big_buck_bunny_1_minute.avi -filter_complex '[0:v]setpts=0.5*PTS[v];[0:a]atempo=2.0[a]' -map '[v]' -map '[a]' output.avi");
		}
		return null;
	}
}