package uk.ac.soton.comp1206;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;



public class Multimedia {

    private static final Logger logger = LogManager.getLogger(Multimedia.class);
    private static MediaPlayer audioPlayer;
    private static MediaPlayer musicPlayer;
    private static boolean audioEnabled = true;

    public static  void playMusic(String file) {
        if (!audioEnabled) return;

        String toPlay = Multimedia.class.getResource("/music/" + file).toExternalForm();
        logger.info("Playing music: " + toPlay);

        try {
            musicPlayer = new MediaPlayer(new Media(toPlay));
            musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            musicPlayer.setVolume(0.4);
            musicPlayer.play();
        } catch (Exception e) {
            audioEnabled = false;
            e.printStackTrace();
            logger.error("Unable to play music file, disabling music");
        }
    }
    public static  void playAudio(String file) {

        if (!audioEnabled) return;

        String toPlay = Multimedia.class.getResource("/sounds/" + file).toExternalForm();
        logger.info("Playing audio: " + toPlay);
        try {
            audioPlayer = new MediaPlayer(new Media(toPlay));
            audioPlayer.play();
        } catch (Exception e) {
            audioEnabled = false;
            e.printStackTrace();
            logger.error("Unable to play audio file, disabling audio");
        }

    }
    public static void stopMusic() {
        if (musicPlayer != null) {
            musicPlayer.stop();
        }
    }



}
