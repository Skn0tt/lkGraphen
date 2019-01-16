import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public class Gui extends JFrame {
  // Anfang Attribute  
  private JLabel jLabel1 = new JLabel();
  private JLabel jLabel2 = new JLabel();
  private JButton bGraphUebernehmen = new JButton();
  private JTextField jTextField1 = new JTextField();
  private JTextField jTextField2 = new JTextField();
  private JButton bGraphzeichnen = new JButton();

  private Graph result = null;
  // Ende Attribute
  
  public Gui(String title) {
    // Frame-Initialisierung
    super (title);
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    int frameWidth = 514; 
    int frameHeight = 154;
    setSize(frameWidth, frameHeight);
    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
    int x = (d.width - getSize().width) / 2;
    int y = (d.height - getSize().height) / 2;
    setLocation(x, y);
    Container cp = getContentPane();
    cp.setLayout(null);
    // Anfang Komponenten
    
    jLabel1.setBounds(16, 8, 49, 20);
    jLabel1.setText("Knoten:");
    jLabel1.setFont(new Font("Dialog", Font.PLAIN, 13));
    cp.add(jLabel1);
    jLabel2.setBounds(152, 8, 49, 20);
    jLabel2.setText("Kanten:");
    jLabel2.setFont(new Font("Dialog", Font.PLAIN, 13));
    cp.add(jLabel2);
    bGraphUebernehmen.setBounds(40, 72, 187, 25);
    bGraphUebernehmen.setText("Graph �bernehmen");
    bGraphUebernehmen.addActionListener(new ActionListener() {
    public void actionPerformed(ActionEvent evt) {
    bGraphUebernehmen_ActionPerformed(evt);
    }
    });
    bGraphUebernehmen.setFont(new Font("Dialog", Font.PLAIN, 13));
    cp.add(bGraphUebernehmen);
    jTextField1.setBounds(16, 32, 105, 24);
    jTextField1.setText("A,B,C,D,E,F");
    jTextField1.setFont(new Font("Dialog", Font.PLAIN, 13));
    cp.add(jTextField1);
    jTextField2.setBounds(152, 32, 313, 24);
    jTextField2.setText("AB9,AC1,BC4,BD1,CD8,BE3,CF2,DE8,DF4");
    //Nikolaus: AB1,AC1,BC1,CD1,CE1,AD1,AE1,ED1,EF1,DF1
    jTextField2.setFont(new Font("Dialog", Font.PLAIN, 13));
    cp.add(jTextField2);
    
    bGraphzeichnen.setBounds(248, 72, 155, 25);
    bGraphzeichnen.setText("Graph zeichnen");
    bGraphzeichnen.setMargin(new Insets(2, 2, 2, 2));
    bGraphzeichnen.addActionListener(new ActionListener() { 
    public void actionPerformed(ActionEvent evt) { 
    bGraphzeichnen_ActionPerformed(evt);
    }
    });
    bGraphzeichnen.setFont(new Font("Dialog", Font.PLAIN, 12));
    bGraphzeichnen.setEnabled(false);
    cp.add(bGraphzeichnen);
    // Ende Komponenten
    
    setResizable(false);
    setVisible(true);
  }
  
  // Anfang Methoden
  public void bGraphUebernehmen_ActionPerformed(ActionEvent evt) {
    String verticesInput = this.jTextField1.getText();
    String edgesInput = this.jTextField2.getText();

    this.result = GraphParser.parse(verticesInput, edgesInput);

    bGraphzeichnen.setEnabled(true);
  }
    
  public void bGraphzeichnen_ActionPerformed(ActionEvent evt) {
    if (this.result != null) {
      MalGUI gui = new MalGUI(this.result);
      gui.show();
    }
  } // end of bGraphzeichnen_ActionPerformed

    // Ende Methoden
    
  public static void main(String[] args) {
    new Gui("Gui");
  }
}
