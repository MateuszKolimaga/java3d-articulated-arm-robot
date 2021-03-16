import javax.media.j3d.Behavior;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.WakeupOnCollisionEntry;
import java.util.Enumeration;
import com.sun.j3d.utils.geometry.Sphere;


public class CollisionDetection extends Behavior {
	private boolean kolizjaBool;
	private WakeupOnCollisionEntry wEnter;
	Sphere element;
        
        CollisionDetection(Sphere obiekt, BoundingSphere sfera) {
                kolizjaBool = false;
                element = obiekt;
                element.setCollisionBounds(sfera);
        }
        
	public boolean czyKoliduje() {
		return kolizjaBool;
	}

	public void ustawKolizja(boolean inCollision) {
		this.kolizjaBool = inCollision;
	}
	
	public void initialize() {
		wEnter = new WakeupOnCollisionEntry(element);
		wakeupOn(wEnter);              
	}

	public void processStimulus(@SuppressWarnings("rawtypes") Enumeration criteria) {
		kolizjaBool = true;
		wakeupOn(wEnter);
	} 
}