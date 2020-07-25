package main;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.JPanel;

import helper.Config;

class AllowedDirections{
	public static final String[] UP = {"up","left","right"};
	public static final String[] DOWN = {"down","left","right"};
	public static final String[] LEFT = {"left","up","down"};
	public static final String[] RIGHT = {"right","up","down"};
}

class Line{
	public String initialDirection;
	public String currentDirection;
	public String currentColor;
	public Vector<Integer[]> coordinates = new Vector<Integer[]>();
}

public class GridRenderer implements MouseMotionListener{
	
	private JPanel canvas;
	private Vector<Line> lines = new Vector<Line>();
	public static boolean running;
	
	public Vector<Line> getLine(){
		return lines;
	}
	
	public GridRenderer(JPanel canvas) {
		this.canvas = canvas;
		canvas.setSize(Config.HEIGHT, Config.WIDTH);
		canvas.addMouseMotionListener(this);
		canvas.setBackground(Color.decode("#09203a"));
		running = true;
		Thread updateThread = new Thread(this::update);
		updateThread.start();
		 
	}
	
	private String pickDirection(String[] directions) {
		return directions[(int) Math.floor(Math.random() * directions.length)];
	}
	
	private String pickDirection() {
		String[] directions = {"down", "left", "right", "up"};
		return directions[(int) Math.floor(Math.random() * directions.length)];
	}
	
	private String pickLineColor() {
        return Config.COLORS[(int) Math.floor(Math.random() * Config.COLORS.length)];
    }
	
	private int getSquareLength() {
		return Config.squareSize;
	}
	
	private Integer[] getClosestIntersectionCoordinates(MouseEvent event) {
        int squareLength = getSquareLength();
        int x = Math.round(event.getX() / squareLength) * squareLength;
        int y = Math.round(event.getY() / squareLength) * squareLength;
        Integer[] coor = {x,y};
        return coor;
    }
	
	private void limitLineLength(Line line) {
		while(calculateSumLength(line) > Config.maxLineLength) {
			line.coordinates.remove(0);
		}
	}
	
	private double calculateSumLength(Line line) {
		return reduce(line.coordinates);
	}
	
	private double reduce(Vector<Integer[]> coordinates) {
		float accumulator = 0;
		for (Integer[] coor : coordinates) {
			accumulator += reduceFunction(coor, coordinates.indexOf(coor),coordinates );
		}
		return accumulator;
	}
	
	private double reduceFunction(Integer[] currentValue, int index, Vector<Integer[]>array) {
		if(index == 0)
			return 0;
		return Math.sqrt(Math.pow(currentValue[0] - array.get(index-1)[0], 2) + Math.pow(currentValue[1] - array.get(index-1)[1], 2));
	}

	private void getNewCoordinates(Line line,int step) {
		Integer[] recentCoor = line.coordinates.lastElement();
		int squareLength = getSquareLength();
		int nextNode=0, nextLinePos=0, lineDist = 0;
		switch(line.currentDirection) {
			case "up":{
				nextNode = (int) ((Math.ceil(recentCoor[1] / squareLength) - 1) * squareLength);
                nextLinePos = recentCoor[1] - step;
                lineDist = nextNode - nextLinePos;
                if (lineDist > 0) {
                    handleLineIntersectingNode(line,"up", "down",nextNode,lineDist);
                    return;
                }
				Integer[] nextCoor = {recentCoor[0],nextLinePos};
				line.coordinates.add(nextCoor);
				return;				
			}
			case "down":{				
				nextNode = (int) ((Math.floor(recentCoor[1] / squareLength) + 1) * squareLength);
				nextLinePos = recentCoor[1] + step;
				lineDist = nextLinePos - nextNode;
				if (lineDist > 0) {
					handleLineIntersectingNode(line,"down", "up",nextNode,lineDist);
					return;
				}
				Integer[] nextCoor = {recentCoor[0],nextLinePos};
				line.coordinates.add(nextCoor);
				return;
			}
			case "left":{
				nextNode = (int) ((Math.ceil(recentCoor[0] / squareLength) - 1) * squareLength);
				nextLinePos = recentCoor[0] - step;
				lineDist = nextNode - nextLinePos;
				if (lineDist > 0) {
					handleLineIntersectingNode(line,"left", "right",nextNode,lineDist);
					return;
				}
				Integer[] nextCoor = {nextLinePos, recentCoor[1]};
				line.coordinates.add(nextCoor);
				return;
			}
			case "right":{
				nextNode = (int) ((Math.floor(recentCoor[0] / squareLength) + 1) * squareLength);
                nextLinePos = recentCoor[0] + step;
                lineDist = nextLinePos - nextNode;
                if (lineDist > 0) {
                    handleLineIntersectingNode(line,"right", "left",nextNode,lineDist);
                    return;
                }
				Integer[] nextCoor = {nextLinePos, recentCoor[1]};
				line.coordinates.add(nextCoor);
				return;
			}
		}
	}
	
	private void handleLineIntersectingNode (Line line,String currentDirection, String oppositeDirection, Integer nextNode, int lineDist) {
		Integer[] lastCoor = line.coordinates.lastElement();
		if(currentDirection.equals("up") || currentDirection.equals("down")) {
			Integer[] coor = {lastCoor[0],nextNode};
			line.coordinates.add(coor);
		}else {
			Integer[] coor = {nextNode,lastCoor[1]};
			line.coordinates.add(coor);
		}
		switch (line.initialDirection) {
			case "up":
				line.currentDirection = pickDirection(Arrays.stream(AllowedDirections.UP).filter(s -> !s.equals(oppositeDirection)).toArray(String []::new));
				break;
			case "down":
				line.currentDirection = pickDirection(Arrays.stream(AllowedDirections.DOWN).filter(s -> !s.equals(oppositeDirection)).toArray(String []::new));
				break;
			case "left":
				line.currentDirection = pickDirection(Arrays.stream(AllowedDirections.LEFT).filter(s -> !s.equals(oppositeDirection)).toArray(String []::new));
				break;
			case "right":
				line.currentDirection = pickDirection(Arrays.stream(AllowedDirections.RIGHT).filter(s -> !s.equals(oppositeDirection)).toArray(String []::new));
				break;
		}
		getNewCoordinates(line, lineDist);
	}
	
	
	public void update() {
		double updatePeriod = 1e9/Config.MAX_FPS;
		while(running) {
			long startTime = System.nanoTime();
			
			canvas.repaint();
			
			long endTime = System.nanoTime();
			
			double computeTime = endTime - startTime;
			double sleepTime = updatePeriod- computeTime;
			
			double milisTime = sleepTime / 1e6;	
			
			try {
				Thread.sleep(Math.round( milisTime));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void drawGrid(Graphics2D g2) {
		Integer squareLength = getSquareLength();
		int numColumns = Math.round(canvas.getWidth() / squareLength);
		int numRows = Math.round(canvas.getHeight() / squareLength);
		Graphics2D context = g2;
		context.setColor(new Color(Config.gridColorR, Config.gridColorG, Config.gridColorB, Config.gridColorA));
		context.setStroke(new BasicStroke(2));
		for (int col = 0; col <= numColumns; col += 1) {
			context.drawLine(col * squareLength, 0, col * squareLength, canvas.getWidth());
		}
		for (int row = 0; row <= numRows; row += 1) {
			context.drawLine(0, row * squareLength,canvas.getWidth(), row * squareLength);
		}
	}
	
	public void drawLines(Graphics2D g2) {
		for (int idx = 0; idx < lines.size(); idx++) {
			Line line = lines.get(idx);
			g2.setStroke(new BasicStroke(Config.lineWidth));
			g2.setColor(Color.decode(line.currentColor));
			getNewCoordinates(line, Config.SPEED);
			limitLineLength(line);
			g2.drawLine(line.coordinates.get(0)[0], line.coordinates.get(0)[1], line.coordinates.get(1)[0], line.coordinates.get(1)[1]);
			for (int i = 1; i < line.coordinates.size() - 1; i++) {
				g2.drawLine(line.coordinates.get(i)[0], line.coordinates.get(i)[1], line.coordinates.get(i+1)[0], line.coordinates.get(i+1)[1]);
			}
			Integer[] lastCoor = line.coordinates.lastElement();
			if(lastCoor[0] >= canvas.getWidth() || lastCoor[0] < 0 || lastCoor[1] >= canvas.getHeight() || lastCoor[1] < 0)
				lines.remove(line);
		}		
	}
	
	

	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		Integer[] coor = getClosestIntersectionCoordinates(e);
		String direction = pickDirection();
		Line line = new Line();
		line.initialDirection = direction;
		line.currentDirection = direction;
		line.coordinates.add(coor);
		line.currentColor = pickLineColor();
		lines.add(line);
	}
}
