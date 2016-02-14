/**AudioArt
 * 
 * Created By: Monica Anuforo, Sharman Tan, and Tara Iyer
 * 
 * Audio Art is a program that detects sounds, and based on their pitches custom-generates
 * an original phyllotactic spiral. The pitch detected determines the phyllotactic spiral's 
 * seed angle and RBG color, thus generating a unique and dynamic work of art that 
 * changes in response to changing pitch. 
 */

import java.awt.Color;
import java.awt.Dimension;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.SynchronousQueue;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.SilenceDetector;
import be.tarsos.dsp.io.jvm.JVMAudioInputStream;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.sound.sampled.AudioSystem;
import javax.swing.UIManager;
import be.tarsos.dsp.example.Shared;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import be.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm;

public class AudioArt extends JFrame implements PitchDetectionHandler, AudioProcessor {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3501426880288136245L;
	private static double dx;
	private double dy;
	final static SynchronousQueue<Double> queue = new SynchronousQueue<Double>();
	static AudioCanvas audioPanel;

	//private final JTextArea textArea;
	public static double varyingPitch;
	private AudioDispatcher dispatcher;
	private Mixer currentMixer;
	SilenceDetector silenceDetector;
	double threshold;
	
	private PitchEstimationAlgorithm algo;	
	private ActionListener algoChangeListener = new ActionListener(){
		@Override
		public void actionPerformed(final ActionEvent e) {
			String name = e.getActionCommand();
			if(name.equals("START")){
				try {
					setNewMixer(currentMixer);
				} catch (LineUnavailableException e1) {
					e1.printStackTrace();
				} catch (UnsupportedAudioFileException e1) {
					e1.printStackTrace();
				}
			}
	}};

	public AudioArt() {
		this.setLayout(new GridBagLayout());
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setTitle("Pitch Detector");
		
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1;

		// Add PitchPanel to the JFrame (Technically their code)
		JPanel pitchPanel=  new PitchPanel(algoChangeListener);
		add(pitchPanel, c);
		c.gridx = 0;
		c.gridy = 0;
		
		audioPanel = new AudioCanvas(queue);
		audioPanel.setBackground(Color.black);
		audioPanel.setPreferredSize(new Dimension(500,500));
		add(audioPanel, c);
		Mixer newValue = AudioSystem.getMixer(Shared.getMixerInfo(false, true).get(0));
		currentMixer = newValue;		
		algo = PitchEstimationAlgorithm.DYNAMIC_WAVELET;
	}


	
	private void setNewMixer(Mixer mixer) throws LineUnavailableException,
			UnsupportedAudioFileException {
		
		if(dispatcher!= null){
			dispatcher.stop();
		}
		currentMixer = mixer;
		
		float sampleRate = 44100;
		int bufferSize = 1024;
		int overlap = 0;
		
		final AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, true);
		final DataLine.Info dataLineInfo = new DataLine.Info(
				TargetDataLine.class, format);
		TargetDataLine line;
		line = (TargetDataLine) mixer.getLine(dataLineInfo);
		final int numberOfSamples = bufferSize;
		line.open(format, numberOfSamples);
		line.start();
		final AudioInputStream stream = new AudioInputStream(line);

		JVMAudioInputStream audioStream = new JVMAudioInputStream(stream);
		
		// create a new dispatcher
		dispatcher = new AudioDispatcher(audioStream, bufferSize,
				overlap);

		// add a processor
		dispatcher.addAudioProcessor(new PitchProcessor(algo, sampleRate, bufferSize, this));
		
		new Thread(dispatcher,"Audio dispatching").start();
		
		silenceDetector = new SilenceDetector(threshold,false);
		dispatcher.addAudioProcessor(silenceDetector);
		dispatcher.addAudioProcessor(this);

		// run the dispatcher (on a new thread).
		new Thread(dispatcher,"Audio dispatching").start();
	}
	
	public static void main(String... strings) throws InterruptedException,
			InvocationTargetException {
		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (Exception e) {
					//ignore failure to set default look en feel;
				}

				JFrame frame = new AudioArt();
				frame.pack();
				frame.setVisible(true);
			}
		});
		
		new Thread(audioPanel).start();
		new Thread(new AudioArt().new JoinerThread(queue)).start(); 
		
	}
	
	@Override
	public boolean process(AudioEvent audioEvent) {
		handleSound();
		return true;
	}
	
	private void handleSound(){

			//textArea.append("Sound detected at:" + System.currentTimeMillis() + ", " + (int)(silenceDetector.currentSPL()) + "dB SPL\n");
			//textArea.setCaretPosition(textArea.getDocument().getLength());
	 double sound = silenceDetector.currentSPL();
	 //System.out.println(sound);		
	}
	@Override
	public void processingFinished() {		
		
	}


	@Override
	public void handlePitch(PitchDetectionResult pitchDetectionResult,AudioEvent audioEvent) {
		if(pitchDetectionResult.getPitch() != -1){
			double timeStamp = audioEvent.getTimeStamp();
			float pitch = pitchDetectionResult.getPitch();
			varyingPitch = pitch;
			//System.out.println(pitch); //ADDED BY MONICA
			float probability = pitchDetectionResult.getProbability();
			double rms = audioEvent.getRMS() * 100;
			String message = String.format("Pitch detected at %.2fs: %.2fHz ( %.2f probability, RMS: %.5f )\n", timeStamp,pitch,probability,rms);
			//textArea.append(message);
			//textArea.setCaretPosition(textArea.getDocument().getLength());
		}
	}
	
	public double getDX(){
		dx =varyingPitch;//*(.01))/500;
		return dx;
	}
	
	private class JoinerThread implements Runnable{
		
		private SynchronousQueue<Double> queue;
		private Double pitch;
		private Double volume;
		
		public JoinerThread(SynchronousQueue<Double> queue){
			this.queue = queue;
		}

		@Override
		public void run() {
			while(true){
			pitch = getDX();
			volume = dy;
			try{
				
				queue.put(pitch);
				System.out.println(pitch);

			} catch(InterruptedException e){
				e.printStackTrace();
			}
			}
			
		}
		
	}


}