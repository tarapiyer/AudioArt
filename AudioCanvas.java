/*
 * AudioCanvas: Monica Anuforo, Sharman Tan, Tara Iyer
 */
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.geom.Ellipse2D;
import java.lang.Math;
import java.util.concurrent.SynchronousQueue;

import javax.swing.JPanel;

public class AudioCanvas extends JPanel implements Runnable{

	private static final long serialVersionUID = 1L;
	private static final double R = 3.0;
	private static final int N = 1000;
	private static final int SCREEN_WIDTH = 500;
	private static final int SCREEN_HEIGHT = 500;

	float r = (float) 0.0;
	float a = (float) 137.5;
	float c = (float) 8.0;
	float i = (float) 0.0;
	float j = (float) 0.0;
	float phi = (float) 0.0;
	double increment = 0.0;
	Color currColor = Color.BLACK;
	Thread runner;
	private SynchronousQueue<Double> queue;
	int rValue = 0;
	
	public AudioCanvas(SynchronousQueue<Double> queue){
		this.queue = queue;
	}
	     
	public void paintComponent(Graphics g) {
		 super.paintComponent(g);
		 Graphics2D graphics2D = (Graphics2D)g;
		 a+=(increment*(.01))/600;
		 
		 for (int dot = 0; dot < N; dot++) {
				phi = dot * a;
		        r = (float)(c*Math.sqrt(1.0*dot));
		        j = (float)((r*Math.cos(phi)) + SCREEN_WIDTH/2);
		        i = (float)(r*Math.sin(phi)) + SCREEN_HEIGHT/2;
		        if ((0 <= i) && (0 <= j) && (i < SCREEN_WIDTH) && (j < SCREEN_HEIGHT)) {
		            Ellipse2D.Double circle = new Ellipse2D.Double((i - R), (j - R), (2*R), (2*R));
		            Double pitch = increment;
		            rValue = setRValue(pitch);
		            graphics2D.setColor(new Color(rValue, 255 - rValue, 128));
		            graphics2D.fill(circle);
		        }
		 }
		 //System.out.print(a + " " + c);
	}
	
	private int setRValue(Double pitch) {
		Double decimalRValue = 0.32*pitch;
		long longValue = Math.round(decimalRValue);
		int rValue = (int)longValue;
		return rValue;
	}
		
	
	public void updateCanvas(double pitch, double volume) {
		a+=pitch;
		c+=volume;
	}
	
	public void start() {
		if (runner == null) {
		runner = new Thread(this);
		runner.start();
		}
		}


	@Override
	public void run() {
		while (true) {
			try{
				Double pitch = (Double)queue.take();
				System.out.println(pitch + "received");
				increment = pitch;
				repaint();
				try { Thread.sleep(200); }
				catch (InterruptedException e) { }
			 } catch (InterruptedException e) {
				 e.printStackTrace();
			 }
		}
	}
	
}
	