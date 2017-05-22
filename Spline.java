///////////////////////////////////////////////////////
///////////// C1 QUADRATIC SPLINE APPLET //////////////
//////// CREATED BY: MIROSLAV SERGIEV, 13568 //////////
/////////////////// FMI - SOFIA ///////////////////////
////////////////////// 2016 ///////////////////////////
///////////////////////////////////////////////////////



package Curve;

import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import javax.swing.*;

//Main class start
public class Spline extends JApplet implements MouseListener, MouseMotionListener {

    private static final long serialVersionUID = 1L; //Serial UID
   
    Graphics g = this.getGraphics(); //Initialize drawing space
    drawPanel dPanel; //Invoke drawing space instance
    sliderPanel sPanel; //Invoke slider drawing space instance
    boolean pointDrag, knobDrag; //Mouse dragging flags
    int dragIndex; //Index of dragged point

	public Spline(){ //Default constructor
	}
    public void init(){ //Initialize interface objects
		setSize(800, 600); //Window size
      		
		setBackground(Color.WHITE); //Background color
		getContentPane().setLayout(null); //Absolute layout
		
		//Drawing panel parameters
		dPanel  = new drawPanel();
		dPanel.setBounds(0, 0, 800, 549);
		dPanel.setForeground(Color.WHITE);
		dPanel.setPreferredSize(new Dimension(450,450));
		getContentPane().add(dPanel);
		dPanel.setBackground(Color.WHITE);
		dPanel.setLayout(null);
		dPanel.setDoubleBuffered(true);
		
		//Slider panel parameters
		sPanel  = new sliderPanel();
		sPanel.setBounds(0, 550, 800, 50);
		sPanel.setForeground(Color.WHITE);
		sPanel.setPreferredSize(new Dimension(450,50));
		getContentPane().add(sPanel);
		sPanel.setBackground(Color.WHITE);
		sPanel.setDoubleBuffered(true);
		sPanel.setLayout(null);
		
		//Create mouse listeners
	    addMouseListener(this);
	    addMouseMotionListener(this);
	    
	    System.out.println("------START------"); //System message
	      
	   }	
	private void isOnPoint(){ //Calculate if cursor is around a control point
		int rad = 50;
		ArrayList<Point> tmp = dPanel.getPoints();
		for(int i = 0; i < tmp.size(); i++){
			if(getMousePosition().y < 550 && 
			   tmp.get(i).getX() > (dPanel.getMousePosition().getX() - rad) && 
			   tmp.get(i).getX() < (dPanel.getMousePosition().getX() + rad) &&
			   tmp.get(i).getY() > (dPanel.getMousePosition().getY() - rad) && 
			   tmp.get(i).getY() < (dPanel.getMousePosition().getY() + rad) &&
			   pointDrag == false)
			{
				dragIndex = i;
				pointDrag = true;
			} 
		}
	}
	private void isOnKnob(){ //Calculate if cursor is around a slider knob
		int rad = 30;
		ArrayList<Point> tmp = sPanel.getKnobs();
		for(int i = 1; i < tmp.size()-1; i++){
			if( getMousePosition().y > 550 && 
				tmp.get(i).getX() > (sPanel.getMousePosition().getX() - rad) && 
				tmp.get(i).getX() < (sPanel.getMousePosition().getX() + rad) &&
				tmp.get(i).getY() > (sPanel.getMousePosition().getY() - rad) && 
				tmp.get(i).getY() < (sPanel.getMousePosition().getY() + rad) &&
				knobDrag == false)
			{
				dragIndex = i;
				knobDrag = true;
			} 
		}			
	}
	//Mouse event handlers
	@Override
	public void mouseDragged (MouseEvent e) {
		isOnPoint();
		if(pointDrag && getMousePosition().y < 550 && getMousePosition().x < 800){
			dPanel.setPoint(dPanel.getMousePosition(), dragIndex);
			repaint();
		}
		isOnKnob();
		if(knobDrag && getMousePosition().y > 550 && getMousePosition().y < 600 && getMousePosition().x < 800){
			sPanel.setKnob(sPanel.getMousePosition(), dragIndex);
			repaint();
		}
		e.consume();		
	}
	@Override
	public void mouseMoved   (MouseEvent e) {
		showStatus("(" + getMousePosition().x + "," + getMousePosition().y + ")   Control points: " + dPanel.getPoints().size());
	    e.consume();
	}
	@Override
	public void mouseClicked (MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON3){
			dPanel.clearAll();
			sPanel.clearAll();
		} else {
		if(getMousePosition().y < 550){
			dPanel.appendPoint(getMousePosition());	
			sPanel.appendKnob();
		}		
		sPanel.calculateRatios();
		dPanel.setKnobs(sPanel.getKnobs());
		}
		dPanel.repaint();
		sPanel.repaint();
	    e.consume();
	}
	@Override
	public void mouseEntered (MouseEvent e) {
	}
	@Override
	public void mouseExited  (MouseEvent e) {
	}
	@Override
	public void mousePressed (MouseEvent e) {	
	}
	@Override
	public void mouseReleased(MouseEvent e) {
	      pointDrag = false;
	      knobDrag = false;
	      e.consume();
	}
}
//Main class end

//=====================================================================================

//Drawing panel class start
class drawPanel extends JPanel{
    private static final long serialVersionUID = 2L; //Serial UID

    Graphics2D g; //Initialize drawing space
    ArrayList<Point> deBoorPoints = new ArrayList<Point>(); //Initialize control point list
    ArrayList<Point> bezierPoints = new ArrayList<Point>(); //Initialize Bezier point list
    ArrayList<Point> knobs = new ArrayList<Point>(); //Initialize ratio list
    Point oldPoint = new Point(-1, -1); //Used to connect Bezier points
    		    
	public void paintComponent(Graphics g){ //Paint method
        this.g = (Graphics2D)g;	 //Get drawing space
        super.paintComponent(g); //Draw on main window
        this.g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); //Activate panel anti-aliasing
		drawBG(); //Draw background grid
		//Call curve algorithms when at least 3 control points are present
		if(deBoorPoints.size() > 2) {
			deBoor();
			drawBezier();
		}
		drawElements(); //Draw points and segments
	   }
	private void drawElements(){ //Draws points and segments on screen
		g.setColor(Color.BLACK);
		g.setStroke(new BasicStroke(2));
	    for(int i = 0; i < deBoorPoints.size()-1; i++){
	    	g.drawLine(deBoorPoints.get(i+1).x, deBoorPoints.get(i+1).y, deBoorPoints.get(i).x, deBoorPoints.get(i).y);
	    }	    
		for(int i = 0; i < deBoorPoints.size(); i++){
				g.setColor(Color.BLUE);
				g.fillOval(deBoorPoints.get(i).x-5, deBoorPoints.get(i).y-5, 10, 10);
				printPoint("d", i, deBoorPoints.get(i));
		}
		for(int i = 0; i < bezierPoints.size(); i++){
			g.setColor(Color.RED);
			g.fillOval(bezierPoints.get(i).x-4, bezierPoints.get(i).y-4, 8, 8);
		}
	}
	private void deBoor(){ //De Boor algorithm method
		double ratio;
		int bezX, bezY;
		bezierPoints.clear();
		bezierPoints.add(deBoorPoints.get(0));
		for(int i = 2; i < deBoorPoints.size()-1; i++){
			//Calculate ratio from the slider knobs' X coordinates
			ratio = (knobs.get(i-1).getX() - knobs.get(i-2).getX())/(knobs.get(i).getX() - 
					knobs.get(i-2).getX());
			//Calculate missing Bezier polygon points
			bezX = deBoorPoints.get(i-1).x + 
					(int)((deBoorPoints.get(i).x - deBoorPoints.get(i-1).x)*ratio);
			bezY = deBoorPoints.get(i-1).y + 
					(int)((deBoorPoints.get(i).y - deBoorPoints.get(i-1).y)*ratio);
			bezierPoints.add(new Point(bezX, bezY));
		}
		bezierPoints.add(deBoorPoints.get(deBoorPoints.size()-1));
	}
	private void drawBezier(){ //De Casteljau method helper
		ArrayList<Point> pTmp = new ArrayList<Point>();
		for(int i = 1; i < bezierPoints.size(); i++){
			pTmp.clear();
			
			//Add three points in a temporary array to be passed as Bezier control polygon
			pTmp.add(bezierPoints.get(i-1));
			pTmp.add(deBoorPoints.get(i));
			pTmp.add(bezierPoints.get(i));
			
			//Call De Casteljau method
			for(double r = 0; r < 1; r = r + 0.1){
				deCasteljau(pTmp, r);
			}
		}
		oldPoint.setLocation(-1, -1);
	}
	private void deCasteljau(ArrayList<Point> p, double ratio){ //De Casteljau recursive method
		if(p.size() == 1){
			g.setColor(Color.RED);
			if(oldPoint.x != -1) {
				g.drawLine(oldPoint.x, oldPoint.y, p.get(0).x, p.get(0).y);
			}
			oldPoint = new Point(p.get(0).x, p.get(0).y);
		}
		 	    
  	    ArrayList<Point> pTmp = new ArrayList<Point>();
  	    for(int i = 0; i < p.size()-1; i++){
  	    	Point tmp = new Point();
  	    	//Calculate new point, according to assigned ratio
  	    	tmp.setLocation((int)((p.get(i+1).x - p.get(i).x)*ratio) + p.get(i).x, 
  	    					(int)((p.get(i+1).y - p.get(i).y)*ratio) + p.get(i).y);
  	  	  pTmp.add(tmp);
  	    }
  	    
  	    //Cycle with new control polygon until a single point remains
  	    if(p.size() > 1)
  	    	deCasteljau(pTmp, ratio);
	}	
    public void drawBG(){ //Draw background grid method
    	g.setColor(Color.GRAY);
    	for(int i = 0; i < 50; i++){
    		g.drawLine(20*i, 0, 20*i, 5000);
    		g.drawLine(0, 20*i, 3000, 20*i);
    	}
	}  
    public void clearAll(){ //Clear all data method
		deBoorPoints.clear();
		bezierPoints.clear();
		knobs.clear();
		repaint();
	}
	public void setKnobs(ArrayList<Point> knobs){ //Set knob points
		this.knobs = knobs;
		repaint();
	}
	public void setPoints(ArrayList<Point> points){ //Control point array set method
		this.deBoorPoints = points;
		repaint();
	}
	public void setPoint(Point point, int i){ //Control point set method
		if(point != null && point.x >= 0 && point.y >= 0 && point.x < 1000 && point.y < 1000){  
			this.deBoorPoints.set(i, point);
			repaint();
		}
	}
	public ArrayList<Point> getPoints(){ //Point array get method
		return deBoorPoints;
	}
	public void appendPoint(Point point){ //Append a control point to array method
		this.deBoorPoints.add(point);
		repaint();
	}
	public void printPoint(String label, int index, Point p){ //Print point index method
		g.setColor(Color.BLACK);
		g.setFont(new Font("SansSerif", Font.BOLD, 18));
		g.drawString((label + (index - 1)), p.x, p.y+20);
	}
}
//Drawing panel class end

//=====================================================================================
	
//Slider panel class start
class sliderPanel extends JPanel{
    private static final long serialVersionUID = 3L; //Serial UID

    Graphics2D g; //Initialize drawing space
    ArrayList<Double> ratios = new ArrayList<Double>(); //Initialize ratio list
    ArrayList<Point> knobs = new ArrayList<Point>(); //Initialize knob list
	int length = 700; //Length of slider in pixels
	int radius = 15; //Knob radius
	int rangeMin = (800-length-radius)/2; //Lower range bound
	int rangeMax = rangeMin + length; //Upper range bound
    		    
	public void paintComponent(Graphics g){ //Paint method
        this.g = (Graphics2D)g;	//Get drawing space
        super.paintComponent(g); //Draw on main window
        this.g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); //Activate panel anti-aliasing
        drawSlider(); //Draw slider
        calculateRatios(); //Get knob ratios
	}
	private void drawSlider(){ //Slider drawing method
		g.setColor(Color.BLACK);
		g.setStroke(new BasicStroke(1));
		g.drawLine(0, 0, 800, 0);
		g.drawLine(0, 49, 800, 49);
					
		if(knobs.size() == 0){
			g.setColor(Color.BLACK);
			g.setFont(new Font("SansSerif", Font.PLAIN, 18));
			g.drawString("Place points in the field above.", 300, 30);
		} else if(knobs.size() == 1){
			setKnob(new Point((int)(rangeMin + (rangeMax - rangeMin)/2), (int)(18-(radius/2))), 0);
			drawKnob(knobs.get(0));
			printPoint(0);
		} else {
			g.setColor(Color.BLACK);
			g.setStroke(new BasicStroke(3));
			g.drawLine(rangeMin+radius/2, 18, rangeMax+radius/2, 18);				
			for(int i = 0; i < knobs.size(); i++){
				drawKnob(knobs.get(i));
				printPoint(i);
			}
		}
	}
    private void drawKnob(Point knob){ //Knob drawing method
		g.setColor(Color.BLUE);
		g.fillOval(knob.x, knob.y, radius, radius);
	}
    private void redistribute(){ //Knob redistribution method
    	if(knobs.size() > 1){
	    	for(int i = 0; i < knobs.size(); i++){
				knobs.set(i,new Point(( rangeMin + i*(rangeMax-rangeMin)/(knobs.size()-1) ), knobs.get(i).y));
			}
    	}
    }
    public void calculateRatios(){ //Knob ratios calculating method
    	if(knobs.size() > 1){
    		ratios.clear();
	    	for(int i = 0; i < knobs.size()-1; i++){
				appendRatio((double)((knobs.get(i+1).getX()-knobs.get(i).getX())/length));
				printRatio(i);
			}
    	}
    }
    public void setKnob(Point knob, int i){ //Knob set method
    	System.out.println("Called with: (" + knob.x + "," + knob.y + ")    Index: " + i);
		if(knob != null && knob.x > rangeMin && knob.x < rangeMax){
			this.knobs.set(i, new Point(knob.x, (int)(18-(radius/2))));
		}
	}
    public ArrayList<Double> getRatios(){ //Ratio array get method
    	return ratios;
    }
    public void appendKnob(){ //Append knob to array method
		this.knobs.add(new Point(0,(int)(18-(radius/2))));
		redistribute();
    	repaint();
    }
	public void clearAll(){ //Clear all data method
		//ratios.clear();
		knobs.clear();
		repaint();
	}
	public ArrayList<Point> getKnobs(){ //Knob array get method
		return knobs;
	}
	public void appendRatio(double l){ //Append ratio to array method
		this.ratios.add(l);
	}
	public void printPoint(int i){ //Print knob index method
		g.setColor(Color.BLACK);
		g.setFont(new Font("SansSerif", Font.BOLD, 18));
		g.drawString(("u" + i), knobs.get(i).x-2, knobs.get(i).y+30);
	}	
	public void printRatio(int i){ //Print ratio method
		g.setColor(Color.BLACK);
		g.setFont(new Font("SansSerif", Font.PLAIN, 10));
		g.drawString(new DecimalFormat("#.##").format(ratios.get(i)), ((knobs.get(i+1).x-knobs.get(i).x)/2) + knobs.get(i).x, 10);
	}	
}
//Slider panel class end
