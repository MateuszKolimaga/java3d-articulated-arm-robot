import javax.media.j3d.Behavior;
import javax.media.j3d.BoundingBox;
import javax.media.j3d.WakeupOnCollisionEntry;
import java.util.Enumeration;
import javax.media.j3d.Shape3D;


public class CollisionDetection2 extends Behavior {
	private boolean kolizjaBool;
	private WakeupOnCollisionEntry wEnter;
	Shape3D element;
        
        CollisionDetection2(Shape3D obiekt, BoundingBox bryla) {
                kolizjaBool = false;
                element = obiekt;
                element.setCollisionBounds(bryla);
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