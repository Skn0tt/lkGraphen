
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class MalGUI implements ActionListener, ItemListener, MouseListener {

  public static final String COPYRIGHT = "Maxim Shevchishin, 2015";
  public static final String COPYRIGHT_EX = "IF LK1 VDB (2015/'16)";

  public class ShowHideButton extends JButton {

    private boolean shown, dragging, inside;

    {
      setOpaque(false);
      setVisible(false);
      setForeground(new Color(0, 0, 0, 128));
      getModel().addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent evt) {
          if (getModel().isRollover()) {
            setForeground(new Color(0, 0, 0, 255));
          } else {
            setForeground(new Color(0, 0, 0, 128));
          }
        }
      });
    }

    public boolean isShown() {
      return shown;
    }
    public void setShown(boolean s) {
      shown = s;
    }

    public boolean isDragging() {
      return dragging;
    }
    public void setDragging(boolean d) {
      dragging = d;
      setVisible(inside && ! dragging);
    }

    public boolean isInside() {
      return inside;
    }
    public void setInside(boolean i) {
      inside = i;
      setVisible(inside && ! dragging);
    }

    public void paintComponent(Graphics g) {
      g = g.create();
      if (g instanceof Graphics2D) {
        Graphics2D gr = (Graphics2D) g;
        gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
        gr.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
        RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
      }
      g.setColor(getForeground());
      int w = getWidth(), h = getHeight();
      int[] xs = new int[3], ys = new int[3];
      if (shown) {
        xs[0] = 3;     ys[0] = 3;
        xs[1] = w - 3; ys[1] = 3;
        xs[2] = w / 2; ys[2] = h - 3;
      } else {
        xs[0] = w / 2; ys[0] = 3;
        xs[1] = w - 3; ys[1] = h - 3;
        xs[2] = 3;     ys[2] = h - 3;
      }
      g.fillPolygon(xs, ys, 3);
      g.dispose();
    }
    public void paintBorder(Graphics g) {
      int w = getWidth(), h = getHeight();
      g.setColor(getForeground());
      g.drawRect(0, 0, w - 1, h - 1);
    }

  }

  private final boolean zeigeControls;
  private final JFrame fenster;
  private final JPanel oben;
  private final JTextField knoten;
  private final JTextField kanten;
  private final JCheckBox einfuegen;
  private final JButton los;
  private final ShowHideButton anzeigen;
  private final JPanel unten;
  private final JButton kreis;
  private final JButton zufall;
  private final JCheckBox dynamisch;
  private final JButton anschieben;
  private final GraphPanel ausgabe;

  public MalGUI(boolean zeigeControls) {
    this.zeigeControls = zeigeControls;
    fenster = new JFrame("MalGUI");
    oben = new JPanel();
    knoten = new JTextField();
    kanten = new JTextField();
    knoten.setText("A,B,C,D,E,F");
    kanten.setText("AB9,AC1,BC4,BD1,CD8,BE3,CF2,DE8,DF4");
    einfuegen = new JCheckBox("Knoten automatisch einf\u00fcgen");
    los = new JButton("Los");
    anzeigen = new ShowHideButton();
    unten = new JPanel();
    kreis = new JButton("Kreis");
    zufall = new JButton("Zufall");
    dynamisch = new JCheckBox("Dynamisch");
    anschieben = new JButton("Anschieben");
    ausgabe = new GraphPanel() {
      public void mausGedrueckt(Point2D p) {
        super.mausGedrueckt(p);
        anzeigen.setDragging(true);
      }
      public void mausLosgelassen() {
        super.mausLosgelassen();
        anzeigen.setDragging(false);
      }
    };
    createUI();
  }
  public MalGUI() {
    this(true);
  }
  public MalGUI(Graph g) {
    this(false);
    setGraph(g);
  }
  public MalGUI(String kanten) {
    this(macheGraph(null, kanten, true));
  }

  protected void createUI() {
    fenster.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    fenster.setLayout(new BorderLayout());
    oben.setLayout(new GridLayout(3, 2));
    oben.add(new JLabel("Knoten:"));
    oben.add(new JLabel("Kanten:"));
    oben.add(knoten);
    oben.add(kanten);
    oben.add(einfuegen);
    oben.add(los);
    fenster.add(oben, BorderLayout.NORTH);
    anzeigen.setShown(true);
    anzeigen.setPreferredSize(new Dimension(30, 30));
    ausgabe.setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    gbc.anchor = GridBagConstraints.SOUTHWEST;
    ausgabe.add(anzeigen, gbc);
    fenster.add(ausgabe, BorderLayout.CENTER);
    unten.setLayout(new GridBagLayout());
    gbc.fill = GridBagConstraints.BOTH;
    gbc.gridx = GridBagConstraints.RELATIVE;
    gbc.gridy = 0;
    unten.add(kreis, gbc);
    unten.add(zufall, gbc);
    unten.add(anschieben, gbc);
    gbc.gridy = 1;
    gbc.gridwidth = 2;
    JLabel copyright = new JLabel() {
    private boolean autsch = false;
    {
    setText("<html><i>" + COPYRIGHT + "</i></html>");
    setToolTipText(COPYRIGHT_EX);
    addMouseListener(new MouseAdapter() {
    public void mouseClicked(MouseEvent evt) {
    if (evt.getClickCount() == 3) {
    if (! autsch) {
    setText("<html><i>Autsch!</i></html>");
    autsch = true;
    return;
    }
    final int magick = (int) (Math.random() * 42 + 1);
    setText("<html><tt>" + magick + "</tt></html>");
    throw new RuntimeException(String.valueOf(magick)) {
    public int magick() {
    return magick;
    }
    };
    }
    }
    });
    }
    };
    unten.add(copyright, gbc);
    gbc.gridwidth = 1;
    unten.add(dynamisch, gbc);
    fenster.add(unten, BorderLayout.SOUTH);
    fenster.pack();
    fenster.setMinimumSize(fenster.getSize());
    oben.setVisible(zeigeControls);
    einfuegen.setSelected(true);
    fenster.addWindowListener(new WindowAdapter() {
    public void windowClosed(WindowEvent evt) {
    ausgabe.layout(GraphPanel.LayoutAktion.DYNAMISCH_AUS);
    }
    });
    knoten.addActionListener(this);
    kanten.addActionListener(this);
    los.addActionListener(this);
    anzeigen.addActionListener(this);
    anzeigen.addMouseListener(this);
    kreis.addActionListener(this);
    zufall.addActionListener(this);
    dynamisch.addItemListener(this);
    anschieben.addActionListener(this);
    ausgabe.addMouseListener(this);
  }

  public void actionPerformed(ActionEvent evt) {
    Object o = evt.getSource();
    if (o == knoten || o == kanten || o == los) {
      Graph g;
      try {
        g = macheGraph(knoten.getText(), kanten.getText(),
        einfuegen.isSelected());
      } catch (IllegalArgumentException exc) {
        JOptionPane.showMessageDialog(fenster, exc.getMessage(), "Fehler",
        JOptionPane.ERROR_MESSAGE);
        return;
      }
      ausgabe.setGraph(g);
    } else if (o == anzeigen) {
      if (unten.isVisible()) {
        unten.setVisible(false);
        anzeigen.setShown(false);
      } else {
        unten.setVisible(true);
        anzeigen.setShown(true);
      }
      fenster.repaint();
    } else if (o == kreis) {
      ausgabe.layout(GraphPanel.LayoutAktion.KREIS);
    } else if (o == zufall) {
      ausgabe.layout(GraphPanel.LayoutAktion.ZUFALL);
    } else if (o == anschieben) {
      ausgabe.layout(GraphPanel.LayoutAktion.ANSCHIEBEN);
    }
  }

  public void itemStateChanged(ItemEvent evt) {
    if (evt.getItem() == dynamisch) {
      if (dynamisch.isSelected()) {
        ausgabe.layout(GraphPanel.LayoutAktion.DYNAMISCH_AN);
      } else {
        ausgabe.layout(GraphPanel.LayoutAktion.DYNAMISCH_AUS);
      }
    }
  }

  public void mouseEntered(MouseEvent evt) {
    pruefeMaus(evt.getPoint());
  }
  public void mousePressed(MouseEvent evt) {}
  public void mouseReleased(MouseEvent evt) {}
  public void mouseClicked(MouseEvent evt) {}
  public void mouseExited(MouseEvent evt) {
    pruefeMaus(evt.getPoint());
  }

  private void pruefeMaus(Point p) {
    boolean v = (p.x >= 0 && p.y >= 0 && p.x < ausgabe.getWidth() &&
    p.y < ausgabe.getHeight());
    anzeigen.setInside(v);
  }

  public void setGraph(Graph graph) {
    ausgabe.setGraph(graph);
  }
  public void aktualisiere(boolean komplett) {
    ausgabe.aktualisiere(komplett);
  }

  public void show() {
    fenster.setVisible(true);
  }
  public void hide() {
    fenster.setVisible(false);
  }
  public void dispose() {
    fenster.dispose();
  }

  public static Graph macheGraph(String knoten, String kanten, boolean ae) {
    Graph g = new Graph();
    if (knoten != null) {
      Character lc = null;
      for (char c : knoten.toCharArray()) {
        if (c == ',') {
          continue;
        } else if (c == '#') {
          if (lc != null) {
            getNode(g, String.valueOf(lc), true).setMark(true);
          }
          continue;
        }
        getNode(g, String.valueOf(c), true);
        lc = c;
      }
    }
    if (kanten != null) {
      for (String s : kanten.split(",")) {
        double w;
        boolean m;
        if (s.endsWith("#")) {
          m = true;
          s = s.substring(0, s.length() - 1);
        } else {
          m = false;
        }
        if (s.length() < 2) {
          continue;
        } else if (s.length() == 2) {
          w = 1.0;
        } else {
          try {
            w = Double.parseDouble(s.substring(2));
          } catch (NumberFormatException exc) {
            throw new IllegalArgumentException("Das ist keine Zahl: " +
            s.substring(2));
          }
        }
        Edge e = new Edge(getNode(g, s.substring(0, 1), ae),
        getNode(g, s.substring(1, 2), ae), w);
        if (m) e.setMark(true);
        g.addEdge(e);
      }
    }
    return g;
  }
  public static Graph macheGraph(String knoten, String kanten) {
    return macheGraph(knoten, kanten, false);
  }

  public static Vertex getNode(Graph g, String name, boolean ae) {
    Vertex v = g.getVertex(name);
    if (v == null && ae) {
      v = new Vertex(name);
      g.addVertex(v);
    }
    return v;
  }

  private static void initLAF() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception exc) {
      exc.printStackTrace();
    }
  }

  public static void main(String[] args) {
    initLAF();
    MalGUI m = new MalGUI();
    if (args.length > 0) {
      m.setGraph(macheGraph(null, args[0], true));
      m.kanten.setText(args[0]);
    }
    m.show();
  }

}
