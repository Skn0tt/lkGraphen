
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class GraphPanel extends JPanel {

  public enum LayoutAktion { KREIS, ZUFALL, ANSCHIEBEN, DYNAMISCH,
                             DYNAMISCH_AN, DYNAMISCH_AUS };

  private static class GraphPunkt {

    public final Point2D position, kraft;
    public final Vertex vertex;
    public final Map<Vertex, GraphKante> nachbarn;
    public boolean aktiv;
    public Point2D griffPunkt;

    public GraphPunkt(Vertex vertex) {
      this.position = new Point2D.Double();
      this.kraft = new Point2D.Double();
      this.vertex = vertex;
      this.nachbarn = new LinkedHashMap<>();
      this.aktiv = false;
    }

    public Rectangle2D getBounds2D() {
      double d = KNOTEN_RADIUS + PADDING / 2.0;
      return new Rectangle2D.Double(position.getX() - d,
                                    position.getY() - d, 2 * d, 2 * d);
    }

    public Point2D berechneKraft(GraphPunkt anderer) {
      double dx = position.getX() - anderer.position.getX();
      double dy = position.getY() - anderer.position.getY();
      double d = Math.sqrt(dx * dx + dy * dy), f;
      if (d == 0.0) return new Point2D.Double();
      if (nachbarn.containsKey(anderer.vertex)) {
        // Abstand von ca. vierfachem Radius.
        f = (KNOTEN_RADIUS * 4 - d) * 5;
      } else {
        f = KNOTEN_RADIUS;
        // Nahe Knoten stoßen sich stark ab.
        if (d < KNOTEN_DURCH) {
          f += (KNOTEN_DURCH - d) * 4;
        }
      }
      f /= 20.0;
      return new Point2D.Double(dx / d * f, dy / d * f);
    }

    public boolean trifftPunkt(Point2D p) {
      return (p.distance(position) < (KNOTEN_RADIUS + STRICHSTAERKE / 2));
    }

    public static GraphPunkt aus(Graph g, Vertex vertex) {
      return new GraphPunkt(vertex);
    }
    public void fuelleNachbarn(Graph g, Map<Vertex, GraphPunkt> punkte) {
      nachbarn.clear();
      for (Vertex v : w(g.getNeighbours(vertex))) {
        Edge e = g.getEdge(vertex, v);
        nachbarn.put(v, new GraphKante(punkte.get(vertex), punkte.get(v),
                                       e.getWeight(), e.isMarked()));
      }
    }

  }

  private static class GraphKante {

    public final GraphPunkt a;
    public final GraphPunkt b;
    public final double gewicht;
    public final boolean markiert;

    public GraphKante(GraphPunkt a, GraphPunkt b, double gewicht,
                      boolean markiert) {
      this.a = a;
      this.b = b;
      this.gewicht = gewicht;
      this.markiert = markiert;
    }

    public double laenge() {
      return b.position.distance(a.position);
    }

    public Point2D punkt(double p) {
      double x1 = a.position.getX(), y1 = a.position.getY();
      double x2 = b.position.getX(), y2 = b.position.getY();
      return new Point2D.Double(x1 + p * (x2 - x1), y1 + p * (y2 - y1));
    }

    public double schneidet(GraphKante andere) {
      if (andere == this) return Double.NaN;
      // Nach http://paulbourke.net/geometry/pointlineplane/
      double x1 =        a.position.getX(), y1 =        a.position.getY();
      double x2 =        b.position.getX(), y2 =        b.position.getY();
      double x3 = andere.a.position.getX(), y3 = andere.a.position.getY();
      double x4 = andere.b.position.getX(), y4 = andere.b.position.getY();
      double n  = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
      double za = (x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3);
      double zb = (x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3);
      if (n == 0) return Double.NaN;
      za /= n;
      zb /= n;
      if (zb <= 0 || zb >= 1 || za <= 0 || za >= 1) return Double.NaN;
      return za;
    }

    public ArrayList<Double> schnitte(Iterable<GraphKante> kanten) {
      ArrayList<Double> ret = new ArrayList<>();
      for (GraphKante k : kanten) {
        double s = schneidet(k);
        if (Double.isNaN(s)) continue;
        ret.add(s);
      }
      return ret;
    }

    public static GraphKante aus(Map<Vertex, GraphPunkt> p, Edge e) {
      return new GraphKante(p.get(e.getVertices()[0]),
                            p.get(e.getVertices()[1]),
                            e.getWeight(), e.isMarked());
    }

  }

  private class GravAktualisierer implements ActionListener {

    public void actionPerformed(ActionEvent evt) {
      if (gravAn) {
        berechnePunkteDynamisch(punkte);
        aktualisiere(false);
      }
    }

  }

  public static final int STRICHSTAERKE = 100;
  public static final int ANSCHIEBEN = 200;
  public static final int KNOTEN_RADIUS = 2000;
  public static final int PADDING = 500;
  public static final Font SCHRIFTART =
    new Font(Font.MONOSPACED, Font.PLAIN, 1500);
  public static final Font KLEINE_SCHRIFTART =
    new Font(Font.MONOSPACED, Font.PLAIN, 1000);
  public static final int SUBSCHRITTE = 10;

  private static final int KNOTEN_DURCH = KNOTEN_RADIUS * 2 + PADDING;

  private Graph graph;
  private Map<Vertex, GraphPunkt> punkte;
  private boolean brauchtKreis;
  private boolean gravAn;
  private Timer tmr;
  private AffineTransform transformation;
  private Point mausPosition;
  private volatile int subSchritte;

  public GraphPanel() {
    setBackground(Color.WHITE);
    setMinimumSize(new Dimension(300, 200));
    setPreferredSize(getMinimumSize());
    tmr = new Timer(33, new GravAktualisierer());
    transformation = null;
    subSchritte = SUBSCHRITTE;
    addComponentListener(new ComponentListener() {
      public void componentShown(ComponentEvent evt) {
        if (gravAn) tmr.start();
      }
      public void componentHidden(ComponentEvent evt) {
        tmr.stop();
      }
      public void componentMoved(ComponentEvent evt) {}
      public void componentResized(ComponentEvent evt) {
        transformation = null;
        repaint();
      }
    });
    addMouseListener(new MouseListener() {
      public void mouseEntered(MouseEvent evt) {
        if (! isActive()) return;
        mausBewegt(evt.getX(), evt.getY());
      }
      public void mousePressed(MouseEvent evt) {
        if (! isActive()) return;
        if (evt.getButton() != MouseEvent.BUTTON1) return;
        Point2D p = mausBewegt(evt.getX(), evt.getY());
        mausGedrueckt(p);
      }
      public void mouseReleased(MouseEvent evt) {
        if (! isActive()) return;
        if (evt.getButton() != MouseEvent.BUTTON1) return;
        mausLosgelassen();
      }
      public void mouseClicked(MouseEvent evt) {}
      public void mouseExited(MouseEvent evt) {
        if (! isActive()) return;
        mausVerlassen();
      }
    });
    addMouseMotionListener(new MouseMotionListener() {
      public void mouseDragged(MouseEvent evt) {
        if (! isActive()) return;
        mausBewegt(evt.getX(), evt.getY());
      }
      public void mouseMoved(MouseEvent evt) {
        if (! isActive()) return;
        mausBewegt(evt.getX(), evt.getY());
      }
    });
  }

  public Graph getGraph() {
    return graph;
  }
  public void setGraph(Graph g) {
    graph = g;
    aktualisiere(true);
  }
  public boolean isActive() {
    return (getGraph() != null);
  }

  public void aktualisiere(final boolean komplett) {
    if (komplett) {
      Graph g = graph;
      Map<Vertex, GraphPunkt> p = new LinkedHashMap<>();
      for (Vertex v : w(g.getVertices())) {
        p.put(v, GraphPunkt.aus(g, v));
      }
      for (GraphPunkt punkt : p.values()) {
        punkt.fuelleNachbarn(g, p);
      }
      punkte = p;
      brauchtKreis = true;
    }
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        transformation = null;
        repaint();
      }
    });
  }

  public void layout(LayoutAktion akt) {
    switch (akt) {
      case KREIS: berechnePunkteKreis(punkte); break;
      case ZUFALL: berechnePunkteZufall(punkte); break;
      case ANSCHIEBEN: berechnePunkteAnschieben(punkte); break;
      case DYNAMISCH: berechnePunkteDynamisch(punkte); break;
      case DYNAMISCH_AN: gravAn = true; tmr.start(); mausVerlassen(); break;
      case DYNAMISCH_AUS: gravAn = false; tmr.stop(); break;
    }
    repaint();
  }

  public void setSubSchritte(int anzahl) {
    if (anzahl <= 0)
      throw new IllegalArgumentException("Schrittzahl muss > 0 sein!");
    subSchritte = anzahl;
  }

  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    g = g.create();
    if (! (g instanceof Graphics2D)) {
      g.dispose();
      return;
    }
    Graphics2D gr = (Graphics2D) g;
    gr.setStroke(new BasicStroke(STRICHSTAERKE));
    gr.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
    gr.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
    Map<Vertex, GraphPunkt> punkte = this.punkte;
    if (punkte == null || punkte.isEmpty()) return;
    if (brauchtKreis) {
      brauchtKreis = false;
      berechnePunkteKreis(punkte);
    }
    AffineTransform tr = new AffineTransform(getTransformation());
    tr.preConcatenate(gr.getTransform());
    gr.setTransform(tr);
    gr.setColor(Color.BLACK);
    Set<GraphPunkt> gesehen = new HashSet<>();
    ArrayList<GraphKante> kanten = new ArrayList<>();
    for (GraphPunkt p1 : punkte.values()) {
      gesehen.add(p1);
      for (GraphKante k : p1.nachbarn.values()) {
        if (gesehen.contains(k.b)) continue;
        kanten.add(k);
      }
    }
    for (GraphKante k : kanten) {
      ArrayList<Double> s = k.schnitte(kanten);
      int index = -1;
      double l = k.laenge();
      if (l > 0.0) {
        double f = KNOTEN_RADIUS / l, invf = 1.0 - f;
        s.add(f);
        s.add(invf);
        Collections.sort(s);
        int len = s.size() - 1;
        double maxDist = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < len; i++) {
          double cs = s.get(i), ns = s.get(i + 1);
          if (cs < f) continue;
          if (ns > invf) break;
          double d = ns - cs;
          if (d > maxDist) {
            index = i;
            maxDist = d;
          }
        }
      }
      Point pa = p(k.a.position), pb = p(k.b.position);
      String gewicht = Double.toString(k.gewicht);
      Point pos;
      if (gewicht.endsWith(".0"))
      g.setColor((k.markiert) ? Color.RED : Color.BLACK);
      g.drawLine(pa.x, pa.y, pb.x, pb.y);
      gewicht = gewicht.substring(0, gewicht.length() - 2);
      if (index != -1) {
        pos = p(k.punkt((s.get(index) + s.get(index + 1)) / 2));
      } else {
        pos = p(k.punkt(0.5));
      }
      zentrierterText(gr, pos, KLEINE_SCHRIFTART, Color.BLACK, gewicht, true);
    }
    for (GraphPunkt p : punkte.values()) {
      zeichneKnoten(gr, p);
    }
    gr.dispose();
  }

  protected Point2D gibMausPosition(int x, int y) {
    AffineTransform tr;
    try {
      tr = getTransformation().createInverse();
    } catch (NoninvertibleTransformException exc) {
      return null;
    }
    return tr.transform(new Point(x, y), null);
  }
  protected Point2D gibMausPosition() {
    if (mausPosition == null) return null;
    return gibMausPosition(mausPosition.x, mausPosition.y);
  }
  protected Point2D mausBewegt(int x, int y) {
    mausPosition = new Point(x, y);
    Point2D pos = gibMausPosition(x, y);
    boolean invalidieren = false;
    for (GraphPunkt p : punkte.values()) {
      Point2D grp = p.griffPunkt;
      if (grp != null) {
        p.position.setLocation(pos.getX() + grp.getX(),
        pos.getY() + grp.getY());
        invalidieren = true;
      } else {
        p.aktiv = p.trifftPunkt(pos);
      }
    }
    if (invalidieren) transformation = null;
    repaint();
    return pos;
  }
  protected void mausVerlassen() {
    if (! isActive()) return;
    for (GraphPunkt p : punkte.values()) p.aktiv = false;
    repaint();
  }
  protected void mausGedrueckt(Point2D pos) {
    for (GraphPunkt p : punkte.values()) {
      if (p.aktiv)
      p.griffPunkt = new Point2D.Double(p.position.getX() - pos.getX(),
      p.position.getY() - pos.getY());
    }
    repaint();
  }
  protected void mausLosgelassen() {
    for (GraphPunkt p : punkte.values()) {
      p.griffPunkt = null;
    }
    repaint();
  }

  private double kreisRadius(Map<Vertex, GraphPunkt> punkte) {
    int len = punkte.size();
    if (len == 1) {
      return 0.0;
    } else if (len == 2) {
      return KNOTEN_RADIUS * 2;
    } else {
      return (KNOTEN_RADIUS + PADDING) * 3 * (len + 1) / (2 * Math.PI);
    }
  }
  private void berechnePunkteKreis(Map<Vertex, GraphPunkt> punkte) {
    if (punkte == null) return;
    int len = punkte.size();
    double r = kreisRadius(punkte);
    int i = 0;
    for (GraphPunkt p : punkte.values()) {
      double winkel = (2 * Math.PI) * i / len;
      p.position.setLocation(r * Math.sin(winkel), -r * Math.cos(winkel));
      i++;
    }
    transformation = null;
  }
  private void berechnePunkteZufall(Map<Vertex, GraphPunkt> punkte) {
    if (punkte == null) return;
    Random rnd = new Random();
    double r = kreisRadius(punkte);
    for (GraphPunkt p : punkte.values()) {
      p.position.setLocation(r * (rnd.nextDouble() * 2 - 1),
                             r * (rnd.nextDouble() * 2 - 1));
    }
    transformation = null;
  }
  private void berechnePunkteAnschieben(Map<Vertex, GraphPunkt> punkte) {
    if (punkte == null) return;
    Random rnd = new Random();
    for (GraphPunkt p : punkte.values()) {
      double dx = (rnd.nextDouble() * 2 - 1) * ANSCHIEBEN;
      double dy = (rnd.nextDouble() * 2 - 1) * ANSCHIEBEN;
      p.position.setLocation(p.position.getX() + dx,
      p.position.getY() + dy);
    }
    transformation = null;
  }
  private void berechnePunkteDynamisch(Map<Vertex, GraphPunkt> punkte,
                                       double delta) {
    if (punkte == null) return;
    for (GraphPunkt p : punkte.values())
      p.kraft.setLocation(0.0, 0.0);
    for (GraphPunkt p1 : punkte.values()) {
      for (GraphPunkt p2 : punkte.values()) {
        Point2D kraft = p1.berechneKraft(p2);
        p1.kraft.setLocation(p1.kraft.getX() + kraft.getX(),
                             p1.kraft.getY() + kraft.getY());
      }
    }
    Point2D mp = gibMausPosition();
    for (GraphPunkt p : punkte.values()) {
      if (p.griffPunkt != null && mp != null) {
        // TODO: Entsprechende Gegenkraft auf andere Punkte anwenden.
        // TODO: Diese... Entitäten tatsächlich zu Kräften *machen*.
        p.position.setLocation(mp.getX() + p.griffPunkt.getX(),
                               mp.getY() + p.griffPunkt.getY());
      } else {
        p.position.setLocation(p.position.getX() + p.kraft.getX() * delta,
                               p.position.getY() + p.kraft.getY() * delta);
      }
    }
  }
  private void berechnePunkteDynamisch(Map<Vertex, GraphPunkt> punkte) {
    int schritte = subSchritte;
    double delta = 1.0 / schritte;
    for (int i = 0; i < schritte; i++) {
      berechnePunkteDynamisch(punkte, delta);
    }
    transformation = null;
  }
  
  private AffineTransform getTransformation() {
    if (transformation != null) return transformation;
    int b = getWidth(), h = getHeight();
    // Ein leeres Rectangle2D hinzutun half nicht... :(
    Rectangle2D r = null;
    for (GraphPunkt p : punkte.values()) {
      if (r == null) {
        r = p.getBounds2D();
      } else {
        r = r.createUnion(p.getBounds2D());
      }
    }
    if (r == null) r = new Rectangle2D.Double(-0.5, -0.5, 1.0, 1.0);
    transformation = berechneTransformation(b, h, r);
    return transformation;
  }
  private AffineTransform berechneTransformation(int breite, int hoehe,
                                                 Rectangle2D grenzen) {
    AffineTransform t = new AffineTransform();
    double skalX = breite / grenzen.getWidth();
    double skalY = hoehe / grenzen.getHeight();
    double skal = Math.min(skalX, skalY);
    t.translate(breite / 2.0, hoehe / 2.0);
    t.scale(skal, skal);
    double verschX = grenzen.getX() + grenzen.getWidth() / 2.0;
    double verschY = grenzen.getY() + grenzen.getHeight() / 2.0;
    t.translate(-verschX, -verschY);
    return t;
  }
  
  private void zentrierterText(Graphics g, Point p, Font f, Color c, String s,
                               boolean hintergrund) {
    Rectangle grenzen = g.getFontMetrics(f).getStringBounds(s,
                                                            g).getBounds();
    Rectangle effGrenzen = new Rectangle(p.x - grenzen.x - grenzen.width / 2,
      p.y - grenzen.y - grenzen.height / 2, grenzen.width, grenzen.height);
    Rectangle altGrenzen = new Rectangle(p.x - grenzen.width / 2,
      p.y - grenzen.height / 2, grenzen.width, grenzen.height);
    g.setFont(f);
    if (hintergrund) {
      g.setColor(Color.WHITE);
      g.fillRect(altGrenzen.x, altGrenzen.y,
      effGrenzen.width, effGrenzen.height);
    }
    g.setColor(c);
    g.drawString(s, effGrenzen.x, effGrenzen.y);
  }

  private void zeichneKnoten(Graphics2D g, GraphPunkt pkt) {
    String name = pkt.vertex.getID();
    boolean markiert = pkt.vertex.isMarked();
    Point p = p(pkt.position);
    g.setColor((markiert) ? Color.LIGHT_GRAY : Color.WHITE);
    g.fillOval(p.x - KNOTEN_RADIUS, p.y - KNOTEN_RADIUS,
               KNOTEN_RADIUS * 2, KNOTEN_RADIUS * 2);
    g.setColor(Color.BLACK);
    if (! pkt.aktiv) {
      g.drawOval(p.x - KNOTEN_RADIUS, p.y - KNOTEN_RADIUS,
                 KNOTEN_RADIUS * 2, KNOTEN_RADIUS * 2);
    } else {
      Stroke old = g.getStroke();
      g.setStroke(new BasicStroke(STRICHSTAERKE * 2));
      g.drawOval(p.x - KNOTEN_RADIUS + STRICHSTAERKE / 2,
                 p.y - KNOTEN_RADIUS + STRICHSTAERKE / 2,
                 KNOTEN_RADIUS * 2 - STRICHSTAERKE,
                 KNOTEN_RADIUS * 2 - STRICHSTAERKE);
      g.setStroke(old);
    }
    zentrierterText(g, p, SCHRIFTART, Color.BLACK, name, false);
  }

  private Point p(Point2D p) {
    return new Point((int) p.getX(), (int) p.getY());
  }

  public static <X> Iterable<X> w(final List<X> l) {
    return new Iterable<X>() {
      public Iterator<X> iterator() {
        return new Iterator<X>() {
          {
            l.toFirst();
          }
          public boolean hasNext() {
            return l.hasAccess();
          }
          public X next() {
            if (! l.hasAccess())
              throw new NoSuchElementException("List iteration stopped");
            X ret = l.getContent();
            l.next();
            return ret;
          }
          public void remove() {
            l.remove();
          }
        };
      }
    };
  }

}
