package main;

import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

public class CanvasPanel extends JPanel {
	
	GridRenderer grid;

	public CanvasPanel() {
		grid = new GridRenderer(this);
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2 = (Graphics2D) g;
		grid.drawGrid(g2);
		if (grid.getLine().size() != 0) {
			grid.drawLines(g2);;
		}
	}
}
