package main;

import javax.swing.JFrame;

import helper.Config;

public class Main {
	
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setSize(Config.WIDTH,Config.HEIGHT);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setUndecorated(false);
		frame.add(new CanvasPanel());
		frame.setVisible(true);
	}
}
