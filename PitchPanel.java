import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
/*
 * Monica Anuforo, Sharman Tan, Tara Iyer
 */
import javax.swing.JButton;
import javax.swing.JPanel;

public class PitchPanel extends JPanel {
	private static final long serialVersionUID = -5107785666165487335L;
	
	public PitchPanel(ActionListener algoChangeListener){
		super(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		createButtons(algoChangeListener, c);
	}
	
	// Creates Start and End Buttons
	private void createButtons(ActionListener algoChangeListener, GridBagConstraints c) {
		JButton startButton = new JButton("START");
		c.gridx = 0;
		c.gridy = 0;
		
		startButton.setBackground(new Color(0, 200, 120));
		startButton.setContentAreaFilled(false);
		startButton.setOpaque(true);
		startButton.setForeground(Color.white);
		startButton.setActionCommand("START");
		startButton.addActionListener(algoChangeListener);		
		add(startButton, c);
	}
	
	// Private Instance variables
	private JButton startButton, endButton;

}
