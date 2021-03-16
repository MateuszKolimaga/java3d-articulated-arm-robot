import javax.media.j3d.*;
import com.sun.j3d.utils.applet.MainFrame;
import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import com.sun.j3d.utils.universe.*;
import com.sun.j3d.utils.image.TextureLoader;
import com.sun.j3d.utils.geometry.Cylinder;
import com.sun.j3d.utils.geometry.Box;
import com.sun.j3d.utils.geometry.Sphere;
import java.awt.event.*;
import java.awt.*;
import javax.vecmath.*;
import javax.vecmath.Point3d;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import java.applet.Applet;

public class RobotProject extends Applet implements ActionListener, KeyListener{

    private TransformGroup pozycja;
    private TransformGroup pozycjaRobot;
    private TransformGroup pozycjaSpojenie1;
    private TransformGroup pozycjaRamie1;
    private TransformGroup pozycjaSpojenie2;
    private TransformGroup pozycjaRamie2;
    private TransformGroup kulka;
    private Transform3D wektorRobot;
    private Transform3D wektorRamie1;
    private Transform3D wektorRamie2;
    private Transform3D rotacja;
    private Transform3D przesuniecieKamery;
    private Button przyciskNagraj;
    private Button przyciskOdtworz;
    private Button przyciskInstrukcja;
    private Button przyciskReset;
    private Button przyciskZamknij;
    private BranchGroup wezelScena;
    private Appearance stylPlaszczyzna;
    private Appearance stylRamie;
    private Appearance stylOzdoby;
    private Appearance stylKulka;
    private String sekwencja;      
    private Frame opis;
    private CollisionDetection detektorKula;
    private CollisionDetection2 detektorPodloga;
    private SoundEffect dzwiek_trawa;
    private SoundEffect dzwiek_kulka;
    private Canvas3D canvas3D;
    private SimpleUniverse simpleU;
    private boolean rzutGora;
    private boolean odtwarzanie;
    private double kat, katObserwatora, katMax, katAktualny;    
    private char odwrotnyKrok;
    
       RobotProject(){
        pozycja = new TransformGroup();
        pozycjaRobot = new TransformGroup();
        pozycjaSpojenie1 = new TransformGroup();
        pozycjaRamie1 = new TransformGroup();
        pozycjaSpojenie2 = new TransformGroup();
        pozycjaRamie2 = new TransformGroup();
        kulka = new TransformGroup();
        wezelScena = new BranchGroup();
        przesuniecieKamery = new Transform3D();
        rotacja = new Transform3D();
        wektorRobot = new Transform3D();
        wektorRamie1 = new Transform3D();
        wektorRamie2 = new Transform3D();
        przyciskNagraj = new Button("Record move");
        przyciskOdtworz = new Button("Replay move");
        przyciskInstrukcja = new Button("Instruction");
        przyciskReset = new Button("Reset position");
        przyciskZamknij = new Button("Close");
        stylPlaszczyzna = new Appearance();
        stylRamie = new Appearance();
        stylOzdoby = new Appearance();
        stylKulka = new Appearance();
        wezelScena = new BranchGroup();
        dzwiek_trawa = new SoundEffect();
        dzwiek_kulka = new SoundEffect();
        odtwarzanie = false;        //Ta zmienna przechowuje informacje o tym, czy ruch jest odtwarzany z nagrania
        kat = 0.04f;                //O ten kat obracaja się poszczegolne czesci robota, po nacisnieciu przycisku
        katObserwatora = 1.6;
        katAktualny = 0.0;          //Ta zmienna przechowuje informacje o aktualnym kacie glownego ramienia robota
        katMax = -0.7;
        sekwencja = "";             //Ta zmienna przechowuje informacje na temat sekwencji wykonanych ruchow w postaci lancucha znakow
        dzwiek_trawa.ustawPlik("files//sound_grass.wav");
        dzwiek_kulka.ustawPlik("files//sound.wav");
             
        setLayout(new BorderLayout());
        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
        canvas3D = new Canvas3D(config);
        canvas3D.setVisible(true);
        add("Center", canvas3D);
        canvas3D.addKeyListener(this);
        
        JPanel panel = new JPanel();
        add("North", panel);        //Tworzenie paska menu
        panel.add(przyciskNagraj);
        panel.add(przyciskOdtworz);
        panel.add(przyciskInstrukcja);
        panel.add(przyciskReset);
        przyciskNagraj.addActionListener(this);
        przyciskOdtworz.addActionListener(this);
        przyciskInstrukcja.addActionListener(this);
        przyciskReset.addActionListener(this);
        przyciskZamknij.addActionListener(this);               
        
        BranchGroup scena = utworzScene();
        scena.compile();
        simpleU = new SimpleUniverse(canvas3D);        
        simpleU.addBranchGraph(scena);
        przesuniecieKamery.set(new Vector3f(0.0f, 0.8f, 5.0f));
        simpleU.getViewingPlatform().getViewPlatformTransform().setTransform(przesuniecieKamery);  
        
        OrbitBehavior orbit = new OrbitBehavior(canvas3D, OrbitBehavior.REVERSE_ROTATE);
        orbit.setSchedulingBounds(new BoundingSphere());
        simpleU.getViewingPlatform().getViewPlatformTransform().setTransform(przesuniecieKamery);
        simpleU.getViewingPlatform().setViewPlatformBehavior(orbit);
        
        dzwiek_trawa.odtworz();
    }
        
      public void ruch_robota(char x){      //Funkcja realizująca ruch robota
            if(!detektorPodloga.czyKoliduje() && !detektorKula.czyKoliduje()){      //Ruchy w tej petli wykonaja sie pod warunkiem braku jakiejkolwiek kolizji

                if(!odtwarzanie) sekwencja += x;        //Jesli ruch robota nie jest odtwarzany, to jest zapisywany 
                
                if (x == 'a' && katAktualny > katMax) {     //Po nacisnieciu tego przycisku, jedna z czesci robota sie obraca
                 katAktualny -= kat;
                 rotacja.rotX(-kat); 
                 wektorRamie1.mul(rotacja);
                 pozycjaSpojenie1.setTransform(wektorRamie1);
                }
                if (x == 'd') {
                 katAktualny += kat;
                 rotacja.rotX(kat); 
                 wektorRamie1.mul(rotacja);
                 pozycjaSpojenie1.setTransform(wektorRamie1);
                }
                if (x == 's') {
                 rotacja.rotX(-kat); 
                 wektorRamie2.mul(rotacja);
                 pozycjaSpojenie2.setTransform(wektorRamie2);
                }
                if (x == 'w') {
                 rotacja.rotX(kat); 
                 wektorRamie2.mul(rotacja);
                 pozycjaSpojenie2.setTransform(wektorRamie2);
                }
                if (x == 'q') {
                 rotacja.rotY(kat); 
                 wektorRobot.mul(rotacja);
                 pozycjaRobot.setTransform(wektorRobot);
                }
                if (x == 'e') {
                 rotacja.rotY(-kat); 
                 wektorRobot.mul(rotacja);
                 pozycjaRobot.setTransform(wektorRobot);
                }            
            }
             if(detektorPodloga.czyKoliduje() || detektorKula.czyKoliduje()) {     
                 if(detektorPodloga.czyKoliduje()) dzwiek_trawa.odtworz();
                 if(detektorKula.czyKoliduje()) dzwiek_kulka.odtworz();     //Jesli nastepuje jakakolwiek kolizja to jest odtwarzany dzwiek kolizji
                 
                 switch(sekwencja.charAt(sekwencja.length() -1)){       //Ta funkcja powoduje, ze robot po kolizji "odbija" sie od powierzchni, z która kolidowal
                     case 'a':{                                         //Robot po kolizji, porusza sie w kierunku przeciwnym do tego, ktory był tuz przed nia
                       katAktualny += kat;
                       rotacja.rotX(kat);
                       if(!odtwarzanie) sekwencja += 'd';
                       break;
                     }
                     case 'd':{
                       katAktualny -= kat;
                       rotacja.rotX(-kat);
                       if(!odtwarzanie) sekwencja += 'a';
                       break;
                     }
                     case 's':{
                       katAktualny -= kat;
                       rotacja.rotX(-kat);
                       if(!odtwarzanie) sekwencja += "wa";
                       break;
                     }
                     case 'w':{
                       katAktualny -= kat;
                       rotacja.rotX(-kat);
                       if(!odtwarzanie) sekwencja += "sa";
                        break;
                     }
                     case 'q':{
                       rotacja.rotY(-kat);
                       if(!odtwarzanie) sekwencja += 'e'; 
                       wektorRobot.mul(rotacja);
                       pozycjaRobot.setTransform(wektorRobot);
                       rotacja.rotX(0);
                       break;
                     }
                     case 'e':{
                       rotacja.rotY(kat);
                       if(!odtwarzanie) sekwencja += 'q'; 
                       wektorRobot.mul(rotacja);
                       pozycjaRobot.setTransform(wektorRobot);
                       rotacja.rotX(0);
                       break;
                     }
                 }
                 
                 detektorKula.ustawKolizja(false);
                 detektorPodloga.ustawKolizja(false);
                 
                 wektorRamie1.mul(rotacja);
                 pozycjaSpojenie1.setTransform(wektorRamie1); 

            } 
        }

       public void ruch_kamery(char x){             //Ta funkcja dodaje możliwosc poruszania kamera, za pomoca odpowienich klawiszy
            if (x == 'i' && rzutGora == false) {
             przesuniecieKamery.setTranslation(
                     new Vector3f(5.0f* (float)cos(katObserwatora), 0.8f, 5.0f*(float)sin(katObserwatora)));
             simpleU.getViewingPlatform().getViewPlatformTransform().setTransform(przesuniecieKamery);
             katObserwatora += 0.05;
            }
            if (x == 'o' && rzutGora == false) {
             przesuniecieKamery.setTranslation(
                     new Vector3f(5.0f* (float)cos(katObserwatora), 0.8f, 5.0f *(float)sin(katObserwatora)));
             simpleU.getViewingPlatform().getViewPlatformTransform().setTransform(przesuniecieKamery);
             katObserwatora -= 0.05;
            }
            if (x == 'u'&& rzutGora == false) {
             rotacja.rotY(2*kat);
             przesuniecieKamery.mul(rotacja);
             simpleU.getViewingPlatform().getViewPlatformTransform().setTransform(przesuniecieKamery);
            }
            if (x == 'p' && rzutGora == false) {
             rotacja.rotY(-2*kat);
             przesuniecieKamery.mul(rotacja);
             simpleU.getViewingPlatform().getViewPlatformTransform().setTransform(przesuniecieKamery);
            }

        }

       public void rzut_kamery(char x){         //Ta funkcja dodaje możliwosc podgladu pracy robota, z roznych perspektyw
          rotacja.rotY(Math.PI/2);

             if (x == '1') {
             rzutGora = false;
             przesuniecieKamery.set(new Vector3f(5.0f* (float)cos(1.6f), 0.8f, 5.0f*(float)sin(1.6f))); 
             simpleU.getViewingPlatform().getViewPlatformTransform().setTransform(przesuniecieKamery);
             katObserwatora = 1.65f;
            }
            if (x == '2') {
             rzutGora = false;
             rotacja.rotY(Math.PI/2);
             przesuniecieKamery.set(new Vector3f(5.0f* (float)cos(0.05f), 0.8f, 5.0f*(float)sin(0.05f)));
             przesuniecieKamery.mul(rotacja);
             simpleU.getViewingPlatform().getViewPlatformTransform().setTransform(przesuniecieKamery);
             katObserwatora = 0.1f;
            }
            if (x == '3') {
             rzutGora = false;
             rotacja.rotY(-Math.PI/2);
             przesuniecieKamery.set(new Vector3f(5.0f* (float)cos(3.15f), 0.8f, 5.0f*(float)sin(3.15f)));
             przesuniecieKamery.mul(rotacja); 
             simpleU.getViewingPlatform().getViewPlatformTransform().setTransform(przesuniecieKamery);
             katObserwatora = 3.2f;
            }
            if (x == '4') {
             rzutGora = false;
             rotacja.rotY(-Math.PI);
             przesuniecieKamery.set(new Vector3f(5.0f* (float)cos(4.75f), 0.8f, 5.0f*(float)sin(4.75f)));
             przesuniecieKamery.mul(rotacja); 
             simpleU.getViewingPlatform().getViewPlatformTransform().setTransform(przesuniecieKamery);
             katObserwatora = 4.8f;
            }
            if (x == '5') {
             rzutGora = true;
             rotacja.rotX(-Math.PI/2);
             przesuniecieKamery.set(new Vector3f(0.0f, 6.0f, 0.0f));
             przesuniecieKamery.mul(rotacja);
             simpleU.getViewingPlatform().getViewPlatformTransform().setTransform(przesuniecieKamery);      
            }  
        }

       public void keyPressed(KeyEvent e) {
            ruch_robota(e.getKeyChar());
            ruch_kamery(e.getKeyChar());
        }

       public void keyReleased(KeyEvent e) {}

       private class Instrukcja extends Applet{ void Instrukcja(){}}    //Dodatkowa klasa, dzieki ktorej mozliwe jest wyswietlenie instrukcji w osobnym oknie

       public void keyTyped(KeyEvent e){
             rzut_kamery(e.getKeyChar());
       }

       public void actionPerformed(ActionEvent e) {
          if(e.getSource() == przyciskNagraj) {
              sekwencja = "";
              odtwarzanie = false;

          }
           if(e.getSource() == przyciskOdtworz) {
                  odtwarzanie = true;
                  detektorKula.ustawKolizja(false);
                  detektorPodloga.ustawKolizja(false);
                  for(int i=sekwencja.length()-1; i>=0; i--){       //Dzieki operacjom w tej petli, mozliwe jest powrocenie do stanu robota
                      switch(sekwencja.charAt(i)){                  //do momentu, w którym wcisnieto przycisk "Nagraj"
                          case 'a': odwrotnyKrok = 'd'; break;
                          case 'd': odwrotnyKrok = 'a'; break;
                          case 'e': odwrotnyKrok = 'q'; break;
                          case 'q': odwrotnyKrok = 'e'; break;
                          case 's': odwrotnyKrok = 'w'; break;
                          case 'w': odwrotnyKrok = 's'; break;
                      }
                      ruch_robota(odwrotnyKrok);          
                  }
                 for(int i=0; i<sekwencja.length(); i++){           //W tej petli nastepuje własciwe odtwarzanie ruchow robota z zadana predkoscią
                      this.ruch_robota(sekwencja.charAt(i));            
                      try {
                           Thread.sleep(25);
                         } catch (InterruptedException ex) {System.out.println("Błąd przerwania.");}
                  } 
                 odtwarzanie = false;
                 sekwencja = "";
            }
           if(e.getSource() == przyciskInstrukcja) {              //Wypelnienie okna instrukcji tekstem
              JTextArea tekst = new JTextArea(" q - rotate robot (left) \n" 
                     + " e - rotate robot (right) \n"
              + " d - rotate first arm\n"
              + " a - rotate first arm (other direction)\n"
              + " w - rotate second arm\n"
              + " s - rotate second arm (other direction)ę\n\n"
              + " 1 - look from above\n"
              + " 2 - look from the right\n"
              + " 3 - look from the left\n"
              + " 4 - look from behind\n"
              + " 5 - look from above\n\n"
              + " i - rotate camera around the robot\n"
              + " j - rotate camera around the robot (other direction) \n"
              + " u - rotate camera around its axis\n"
              + " p - rotate camera around its axis (other direction)\n\n"
              + " mouse - roate camera around the robot\n\n");
              Instrukcja instr = new Instrukcja();
              opis = new MainFrame(instr ,400,350);
              JPanel panel2 = new JPanel();
              opis.add("North", panel2);
              panel2.add(przyciskZamknij);
              opis.add("West", tekst);
           }
            if(e.getSource() == przyciskZamknij) {opis.dispose();}
            if(e.getSource() == przyciskReset) {                // Powrot ramion robota do pozycji poczatkowej
                 wektorRamie1.set(new Vector3f(-0.15f, 0.7f, 0.0f));
                 wektorRamie2.set(new Vector3f(0.0f, 0.65f, 0.0f));
                 pozycjaSpojenie1.setTransform(wektorRamie1);
                 pozycjaSpojenie2.setTransform(wektorRamie2);
                 rotacja = new Transform3D();
                 detektorPodloga.ustawKolizja(false);
                 detektorKula.ustawKolizja(false);
                 katAktualny = 0;
            }

        }

       public BranchGroup utworzScene() {
           
            Material złoty = new Material();
            złoty.setEmissiveColor(0.4f, 0.27f, 0.01f);
            złoty.setDiffuseColor(0.32f, 0.85f, 0.97f);
            złoty.setSpecularColor(0.26f, 0.3f, 0.86f);
            złoty.setShininess(100f);

            Material ozdobny = new Material(); 
            ozdobny.setEmissiveColor(0.08f, 0.08f, 0.08f);
            ozdobny.setDiffuseColor(0.12f, 0.12f, 0.12f);
            ozdobny.setSpecularColor(0.15f, 0.15f, 0.15f);
            ozdobny.setShininess(38f);

            Material kulkowy = new Material();
            kulkowy.setEmissiveColor(0.80f, 0.1f, 0.26f);
            kulkowy.setDiffuseColor(0.32f, 0.21f, 0.08f);
            kulkowy.setSpecularColor(0.45f, 0.32f, 0.21f);
            kulkowy.setShininess(38f);

            stylRamie.setMaterial(złoty);
            stylKulka.setMaterial(kulkowy);
            stylOzdoby.setMaterial(ozdobny);

            
            //Swiatla
            BoundingSphere bounds = new BoundingSphere(new Point3d(),10000);
            Color3f light1Color = new Color3f(0.2f, 0.2f, 0.2f);
            Vector3f light1Direction = new Vector3f(4.0f, -7.0f, -12.0f);
            DirectionalLight light1 = new DirectionalLight(light1Color, light1Direction);
            light1.setInfluencingBounds(bounds);
            wezelScena.addChild(light1); 

            Color3f ambientColor = new Color3f(0.7f, 0.7f, 0.7f);
            AmbientLight ambientLightNode = new AmbientLight(ambientColor);
            ambientLightNode.setInfluencingBounds(bounds);
            wezelScena.addChild(ambientLightNode);

            
            //Dodanie elementów do sceny
            wezelScena.addChild(pozycja); 
            wezelScena.addChild(kulka);
            pozycja.addChild(pozycjaRobot);
            pozycjaRobot.addChild(pozycjaSpojenie1);
            pozycjaSpojenie1.addChild(pozycjaRamie1);
            pozycjaRamie1.addChild(pozycjaSpojenie2);
            pozycjaSpojenie2.addChild(pozycjaRamie2);

            
            //Wyglad, rozmiary i transformacje elementow
            Transform3D przesuniecie_kulki = new Transform3D();
            przesuniecie_kulki.set(new Vector3f(0.8f, 0.08f, 0.0f));
            kulka.setTransform(przesuniecie_kulki);
            Sphere ksztalt_kulki = new Sphere(0.08f, stylKulka);     
            kulka.addChild(ksztalt_kulki);

            TransformGroup podstawa = new TransformGroup();
            Cylinder ksztalt_podstawy = new Cylinder(0.3f, 0.03f, stylRamie);

            podstawa.addChild(ksztalt_podstawy);
            wezelScena.addChild(podstawa);

            TransformGroup trzon = new TransformGroup();
            Transform3D wektor_trzon = new Transform3D();
            wektor_trzon.set(new Vector3f(0.0f, 0.27f, 0.0f));
            trzon.setTransform(wektor_trzon);
            Cylinder ksztalt_trzon = new Cylinder(0.1f, 0.50f, stylRamie );

            trzon.addChild(ksztalt_trzon);
            pozycja.addChild(trzon);
                    
            TransformGroup ozdoba1 = new TransformGroup();
            Transform3D wektor_ozdoba1 = new Transform3D();
            wektor_ozdoba1.set(new Vector3f(0.08f, 0.22f, 0.08f));
            Box ksztalt_ozdoba1 = new Box(0.01f, 0.22f, 0.01f, stylOzdoby); 
            ozdoba1.setTransform(wektor_ozdoba1);

            ozdoba1.addChild(ksztalt_ozdoba1);
            pozycja.addChild(ozdoba1);

            TransformGroup ozdoba2 = new TransformGroup();
            Transform3D wektor_ozdoba2 = new Transform3D();
            wektor_ozdoba2.set(new Vector3f(-0.08f, 0.22f, -0.08f));
            Box ksztalt_ozdoba2 = new Box(0.01f, 0.22f, 0.01f, stylOzdoby);
            ozdoba2.setTransform(wektor_ozdoba2);

            ozdoba2.addChild(ksztalt_ozdoba2);
            pozycja.addChild(ozdoba2);

            TransformGroup ozdoba3 = new TransformGroup();
            Transform3D wektor_ozdoba3 = new Transform3D();
            wektor_ozdoba3.set(new Vector3f(0.08f, 0.22f, -0.08f));
            Box ksztalt_ozdoba3 = new Box(0.01f, 0.22f, 0.01f, stylOzdoby);
            ozdoba3.setTransform(wektor_ozdoba3);

            ozdoba3.addChild(ksztalt_ozdoba3);
            pozycja.addChild(ozdoba3);

            TransformGroup ozdoba4 = new TransformGroup();
            Transform3D wektor_ozdoba4 = new Transform3D();
            wektor_ozdoba4.set(new Vector3f(-0.08f, 0.22f, 0.08f));
            Box ksztalt_ozdoba4 = new Box(0.01f, 0.22f, 0.01f, stylOzdoby);
            ozdoba4.setTransform(wektor_ozdoba4);

            ozdoba4.addChild(ksztalt_ozdoba4);
            pozycja.addChild(ozdoba4);

            TransformGroup ozdoba5 = new TransformGroup();
            Transform3D wektor_ozdoba5 = new Transform3D();
            Transform3D obrot_ozdoba5 = new Transform3D();
            obrot_ozdoba5.rotZ(Math.PI/2);
            wektor_ozdoba5.set(new Vector3f(0.0f, 0.62f,-0.13f));
            wektor_ozdoba5.mul(obrot_ozdoba5);
            obrot_ozdoba5.rotY(-Math.PI/3);
            wektor_ozdoba5.mul(obrot_ozdoba5);
            Box ksztalt_ozdoba5 = new Box(0.02f, 0.14f, 0.05f, stylRamie);
            ozdoba5.setTransform(wektor_ozdoba5);

            ozdoba5.addChild(ksztalt_ozdoba5);
            pozycjaRobot.addChild(ozdoba5);

            TransformGroup czesc1 = new TransformGroup();
            Transform3D wektor_czesc1= new Transform3D();
            wektor_czesc1.set(new Vector3f(0.0f, 0.5f, 0.0f));
            czesc1.setTransform(wektor_czesc1);
            Cylinder ksztalt_czesc1 = new Cylinder(0.14f, 0.14f, stylRamie );

            czesc1.addChild(ksztalt_czesc1);
            pozycjaRobot.addChild(czesc1);

            TransformGroup czesc2 = new TransformGroup();
            Transform3D wektor_czesc2= new Transform3D();
            Transform3D obrot_czesc2= new Transform3D();
            obrot_czesc2.rotZ(Math.PI/2);
            wektor_czesc2.set(new Vector3f(0.08f, 0.7f, 0.0f));
            wektor_czesc2.mul(obrot_czesc2);
            czesc2.setTransform(wektor_czesc2);
            Cylinder ksztalt_czesc2 = new Cylinder(0.14f, 0.12f, stylOzdoby );

            czesc2.addChild(ksztalt_czesc2);
            pozycjaRobot.addChild(czesc2);

            TransformGroup czesc3 = new TransformGroup();
            Transform3D wektor_czesc3= new Transform3D();
            Transform3D obrot_czesc3= new Transform3D();
            obrot_czesc3.rotZ(Math.PI/2);
            wektor_czesc3.set(new Vector3f(-0.08f, 0.7f, 0.0f));
            wektor_czesc3.mul(obrot_czesc3);
            czesc3.setTransform(wektor_czesc3);
            Cylinder ksztalt_czesc3 = new Cylinder(0.14f, 0.12f, stylOzdoby);

            czesc3.addChild(ksztalt_czesc3);
            pozycjaRobot.addChild(czesc3);

            TransformGroup czesc4 = new TransformGroup();
            Transform3D wektor_czesc4= new Transform3D();
            Transform3D obrot_czesc4= new Transform3D();
            obrot_czesc4.rotZ(Math.PI/2);
            wektor_czesc4.set(new Vector3f(0.00f, 0.7f, 0.0f));
            wektor_czesc4.mul(obrot_czesc4);
            czesc4.setTransform(wektor_czesc4);
            Cylinder ksztalt_czesc4 = new Cylinder(0.14f, 0.04f, stylRamie);

            czesc4.addChild(ksztalt_czesc4);
            pozycjaRobot.addChild(czesc4);

            TransformGroup spojenie1 = new TransformGroup();
            Box ksztalt_spojenie1 = new Box(0.01f, 0.05f, 0.05f,stylOzdoby);

            spojenie1.addChild(ksztalt_spojenie1);
            pozycjaSpojenie1.addChild(spojenie1);

            TransformGroup ramie1 = new TransformGroup();
            Transform3D wektor_ramie1= new Transform3D();
            wektor_ramie1.set(new Vector3f(-0.06f, 0.3f, 0.0f));
            ramie1.setTransform(wektor_ramie1);
            Box walec3 = new Box(0.05f, 0.4f, 0.07f, stylRamie);

            ramie1.addChild(walec3);
            pozycjaRamie1.addChild(ramie1);

            TransformGroup spojenie2 = new TransformGroup();
            Box ksztalt_spojenie2 = new Box(0.01f, 0.05f, 0.05f, stylOzdoby);

            spojenie2.addChild(ksztalt_spojenie2);
            pozycjaSpojenie2.addChild(spojenie2);

            TransformGroup ramie2 = new TransformGroup();
            Transform3D wektor_ramie2= new Transform3D();
            wektor_ramie2.set(new Vector3f(0.06f, 0.20f, 0.0f));
            ramie2.setTransform(wektor_ramie2);
            Box walec4 = new Box(0.05f, 0.30f, 0.07f, stylRamie);

            ramie2.addChild(walec4);
            pozycjaRamie2.addChild(ramie2);

            pozycjaRobot.setCapability( TransformGroup.ALLOW_TRANSFORM_WRITE);
            pozycjaSpojenie1.setCapability( TransformGroup.ALLOW_TRANSFORM_WRITE);
            pozycjaSpojenie2.setCapability( TransformGroup.ALLOW_TRANSFORM_WRITE);
            pozycjaRamie2.setCapability( TransformGroup.ALLOW_TRANSFORM_WRITE);
            pozycjaRamie2.setCapability( TransformGroup.ALLOW_TRANSFORM_READ);

            wektorRamie1.set(new Vector3f(-0.15f, 0.7f, 0.0f));
            wektorRamie2.set(new Vector3f(0.0f, 0.65f, 0.0f));
            pozycjaSpojenie1.setTransform(wektorRamie1);
            pozycjaSpojenie2.setTransform(wektorRamie2);
            
            //Wyglad plaszczyzny,, na której umieszczony jest robot
            TextureLoader loader = new TextureLoader("files//grass.jpg", null);
            ImageComponent2D image = loader.getImage();
            Texture2D trawka = new Texture2D(Texture.BASE_LEVEL, Texture.RGBA, image.getWidth(), image.getHeight());

            trawka.setImage(0, image);
            trawka.setBoundaryModeS(Texture.WRAP);
            trawka.setBoundaryModeT(Texture.WRAP);

            stylPlaszczyzna.setTexture(trawka);
            Point3f[] coords = new Point3f[4];
            for(int i =0; i<4; i++)  coords[i] = new Point3f();
            Point2f[] tex_coords = new Point2f[4];
            for(int i =0; i<4; i++)  tex_coords[i] = new Point2f();

            coords[0].y = 0.0f;
            coords[1].y = 0.0f;
            coords[2].y = 0.0f;
            coords[3].y = 0.0f;
            coords[0].x = 7.0f;
            coords[1].x = 7.0f;
            coords[2].x = -7.0f;
            coords[3].x = -7.0f;
            coords[0].z = 7.0f;
            coords[1].z = -7.0f;
            coords[2].z = -7.0f;
            coords[3].z = 7.0f;

            tex_coords[0].x = 0.0f;
            tex_coords[0].y = 0.0f;
            tex_coords[1].x = 10.0f;
            tex_coords[1].y = 0.0f;
            tex_coords[2].x = 0.0f;
            tex_coords[2].y = 10.0f;
            tex_coords[3].x = 10.0f;   
            tex_coords[3].y = 10.0f;

            QuadArray quadTrawnik = new QuadArray(4, GeometryArray.COORDINATES|GeometryArray.TEXTURE_COORDINATE_2);
            quadTrawnik.setCoordinates(0,coords);
            quadTrawnik.setTextureCoordinates(0, tex_coords);
            Shape3D trawnik = new Shape3D(quadTrawnik);
            trawnik.setAppearance(stylPlaszczyzna);
            wezelScena.addChild(trawnik);
            
            
            //Dodanie do mniejszego ramienia dektorów kolizji
            detektorKula = new CollisionDetection(ksztalt_kulki, new BoundingSphere(new Point3d(0.0f, 0.0f, 0.03f), 0.03f));
            BoundingSphere boundsKolizjoner = new BoundingSphere(new Point3d(), 0.03f);
            detektorKula.setSchedulingBounds(boundsKolizjoner); 
            pozycjaRamie2.addChild(detektorKula);

            detektorPodloga = new CollisionDetection2(trawnik, new BoundingBox(new Point3d(-8.0f, -8.00f, -8.00f), new Point3d(8.0f, -0.02f, 8.00f)));
            BoundingBox boundsKolizjoner2 = new BoundingBox(new Point3d(0.00f, 0.00f, 0.0f), new Point3d(0.011f, 0.01f, 0.01f));
            detektorPodloga.setSchedulingBounds(boundsKolizjoner2); 
            pozycjaRamie2.addChild(detektorPodloga);

            return wezelScena;
        }

       public static void main(String args[]) {
            RobotProject bb = new RobotProject();
            Frame mf = new MainFrame(bb,1200,1000);
        }
}
