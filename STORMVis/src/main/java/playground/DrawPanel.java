package playground;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import model.LineDataSet;

import org.jzy3d.maths.Point2D;

class DrawPanel extends JPanel {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int squareX = 0;
    private int squareY = 0;
    private int squareW = 10;
    private int squareH = 10;
    private boolean start = true;
    
    public DrawManager drawManager;
    public int scrollOffsetX;
    public int scrollOffsetY;
    public float zoomFactor;

    public DrawPanel() {
        setBorder(BorderFactory.createLineBorder(Color.black));
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
//                moveSquare(e.getX(),e.getY());
                System.out.println("x|y : " + e.getX() +" " + e.getY());
                drawManager.currentPoints.add(new Point2D((int) ((e.getX()+ scrollOffsetX)/zoomFactor),(int) ((e.getY()+ scrollOffsetY)/zoomFactor)));
                repaint();
            }
        });
        zoomFactor = 1.f;
        scrollOffsetX = 0;
        scrollOffsetY = 0;
        drawManager = new DrawManager();
        setLayout(new BorderLayout());
    }
    
    private void moveSquare(int x, int y) {
        int OFFSET = 1;
        if ((squareX!=x) || (squareY!=y)) {
            repaint(squareX,squareY,squareW+OFFSET,squareH+OFFSET);
            squareX=x;
            squareY=y;
            repaint(squareX,squareY,squareW+OFFSET,squareH+OFFSET);
        } 
    }
    
    public Dimension getPreferredSize() {
        return new Dimension(500,500);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
    	super.paintComponent(g);
//    	if(!start) {
//    		super.paintComponent(g);      
//    		g.setColor(Color.RED);
//    		g.fillRect(squareX,squareY,squareW,squareH);
//    		g.setColor(Color.BLACK);
//    		g.drawRect(squareX,squareY,squareW,squareH);
//    	}
    	
    	if(drawManager.currentPoints.size() != 0) {
    		List<Point2D> transformedPoints = new ArrayList<Point2D>();
    		int offset = -5;
    		for(Point2D drawPoint : drawManager.currentPoints) {
    			Point2D actualPoint = new Point2D((int) (drawPoint.x*zoomFactor) - scrollOffsetX, (int) (drawPoint.y*zoomFactor) - scrollOffsetY);
    			transformedPoints.add(actualPoint);
    			g.setColor(Color.RED);
    			g.fillRect(actualPoint.x+offset,actualPoint.y+offset,squareW,squareH);
    			g.setColor(Color.BLACK);
    			g.drawRect(actualPoint.x+offset,actualPoint.y+offset,squareW,squareH);
    		}
    		for(int i = 0; i < transformedPoints.size(); i++) {
    			g.setColor(Color.RED);
    			if(i < (transformedPoints.size()-1)) {
    				Point2D p1 = transformedPoints.get(i);
    				Point2D p2 = transformedPoints.get(i+1);
    				g.drawLine(p1.x,p1.y,p2.x,p2.y);
    			}
    		}
    	}
    	
//    	start = false;
    }  
    
    public LineDataSet addCurrentPointsToLineDataSet(LineDataSet s) {
    	s =  drawManager.addCurrentPointsToLineDataSet(s);
    	return s;
    }
    
}