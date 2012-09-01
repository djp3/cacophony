package edu.uci.ics.luci.util;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class PopUpWindow extends JPanel {
	private static final long serialVersionUID = 3803974813494976943L;
	JFrame frame = new JFrame();

	public static void main(String[] args) {
		new PopUpWindow("Click to stop test");
	}

	public PopUpWindow(String message) {
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JOptionPane.showMessageDialog(frame, message);
		frame.dispose();
	}

}