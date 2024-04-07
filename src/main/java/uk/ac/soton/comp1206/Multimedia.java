package uk.ac.soton.comp1206;
import javafx.scene.image.Image;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import uk.ac.soton.comp1206.scene.SettingsScene;


public class Multimedia {

    private static final Logger logger = LogManager.getLogger(Multimedia.class);
    public static MediaPlayer audioPlayer;
    public static MediaPlayer musicPlayer;
    private static boolean audioEnabled = true;

    /**
     * Method used to play background Music
     * @param file: music file name
     */
    public static  void playMusic(String file) {
        if (!audioEnabled) return;

        String toPlay = Multimedia.class.getResource("/music/" + file).toExternalForm();
        logger.info("Playing music: " + toPlay);

        try {
            musicPlayer = new MediaPlayer(new Media(toPlay));
            musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            musicPlayer.setVolume(SettingsScene.musicVolume / 100);
            musicPlayer.play();
        } catch (Exception e) {
            audioEnabled = false;
            e.printStackTrace();
            logger.error("Unable to play music file, disabling music");
        }
    }

    /**
     * Method used to play audio sounds for events
     * @param file: audio file name
     */
    public static  void playAudio(String file) {

        if (!audioEnabled) return;

        String toPlay = Multimedia.class.getResource("/sounds/" + file).toExternalForm();
        logger.info("Playing audio: " + toPlay);
        try {
            audioPlayer = new MediaPlayer(new Media(toPlay));
            audioPlayer.setVolume(SettingsScene.audioVolume / 100);
            audioPlayer.play();
        } catch (Exception e) {
            audioEnabled = false;
            e.printStackTrace();
            logger.error("Unable to play audio file, disabling audio");
        }

    }

    /**
     * Method used to stop the music playing
     */
    public static void stopMusic() {
        if (musicPlayer != null) {
            musicPlayer.stop();
        }
    }
    /**
     * Retrieve image
     * @param file image name
     * @return selected image
     */
    public static Image getImage(String file) {
        logger.info("Image " + file + " selected");
        return new Image(Multimedia.class.getResource("/images/" + file).toExternalForm());
    }



}
