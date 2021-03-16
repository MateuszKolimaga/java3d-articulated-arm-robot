import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class SoundEffect {
    
    Clip clip;
    
    public void ustawPlik(String soundFileName){
        try{
            File plik = new File(soundFileName);
            AudioInputStream dzwiek = AudioSystem.getAudioInputStream(plik);	
            clip = AudioSystem.getClip();
            clip.open(dzwiek);
        }
	catch(IOException | LineUnavailableException | UnsupportedAudioFileException e){
				
	}
    }
    public void odtworz(){
			
	    clip.setFramePosition(0);
	    clip.start();
	}
}

