package mcgu2329;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Stack;

import spacesettlers.actions.AbstractAction;
import spacesettlers.actions.DoNothingAction;
import spacesettlers.actions.MoveAction;
import spacesettlers.graphics.LineGraphics;
import spacesettlers.graphics.SpacewarGraphics;
import spacesettlers.graphics.TargetGraphics;
import spacesettlers.objects.Ship;
import spacesettlers.simulator.Toroidal2DPhysics;
import spacesettlers.utilities.Movement;
import spacesettlers.utilities.Position;
import spacesettlers.utilities.Vector2D;

/**
 * 
 * Modification of MoveAction class. This class does not slow down when approaching
 * beacons or asteroids; it always moves full speed. 
 * @author Sean
 */

class FastMove extends AbstractAction {
	Stack<Node> nodeS;
	ArrayList<Node> nodeL;
	Toroidal2DPhysics space;
	Position currentTarget;
	Node currentNode;
	Ship ship;
	FastMove(Toroidal2DPhysics space, Ship ship, Stack<Node> s) {
		this.space = space;
		this.ship = ship;
		nodeS = s;
		if (s == null) {
			System.out.println("The stack was null!");
		}
		nodeL = new ArrayList<Node>();
		initialize();
	}
	public void initialize() {
		//while (!nodeS.isEmpty()) {
		//	nodeL.add(nodeS.pop());
		//}
		if (!nodeS.isEmpty()) {
			currentNode = nodeS.pop();
			currentTarget = currentNode.getCenter();
		}
	}
	//public AbstractAction getNextMovement() {
	//	return new MoveAction(space, nodeL.get(0).getCenter(), nodeL.get(nodeL.size()-1).getCenter());
		//return new DoNothingAction();
	//}
	public ArrayList<SpacewarGraphics> getGraphics() {
		ArrayList<SpacewarGraphics> graphicsToAdd = new ArrayList<SpacewarGraphics>();
		TargetGraphics t1 = new TargetGraphics(20, currentTarget);
		TargetGraphics t2 = new TargetGraphics(20, nodeS.peek().getCenter());
		graphicsToAdd.add(t1);
		graphicsToAdd.add(t2);
		return graphicsToAdd;
	}
	@Override
	public Movement getMovement(Toroidal2DPhysics space, Ship ship) {
		// TODO Auto-generated method stub
		this.ship = ship;
		Vector2D myvec = space.findShortestDistanceVector(ship.getPosition(), currentTarget);
//		if (myvec.getXValue() > myvec.getYValue())
//			myvec = myvec.multiply(50.0/myvec.getXValue());
//		else
//			myvec = myvec.multiply(50.0/myvec.getYValue());
//		
		Movement m = new Movement();
		m.setTranslationalAcceleration(myvec);
		return m;
		
		//return (new MoveAction(space, nodeL.get(0).getCenter(), nodeL.get(nodeL.size()-1).getCenter())).getMovement(space, ship);
	}
	@Override
	public boolean isMovementFinished(Toroidal2DPhysics space) {
		// TODO Auto-generated method stub
		if (nodeS.isEmpty()) {
			return true;
		} else if (space.findShortestDistance(ship.getPosition(), currentTarget) < 20) {
			initialize();
			return false;
		} else {
			return false;
		}
	}
}


//class FastMove extends MoveAction {
//	/**
//	 * stores the target position for this move
//	 */
//	Position targetPos;
//	/**
//	 * stores the start time of this move
//	 */
//	int startTime;
//	/**
//	 * stores the end time of this move
//	 */
//	int endTime;
//	
//	Ship ship;
//	Toroidal2DPhysics space;
//	private ArrayList<SpacewarGraphics> graphicsToAdd;
//	Position currentPos;
//	
//	/**
//	 * Constructor that builds a FastMove action given a startTime and targetPosition
//	 * 
//	 * @param startTime 
//	 * @param targetPosition
//	 */
//	
//	private static Vector2D getVec(Toroidal2DPhysics space, Position currentPosition, Position targetPosition) {
//		//System.out.println("Getting herererere");
//		Vector2D myvec = space.findShortestDistanceVector(currentPosition, targetPosition);
//		if (myvec.getXValue() > myvec.getYValue())
//			myvec = myvec.multiply(50.0/myvec.getXValue());
//		else
//			myvec = myvec.multiply(50.0/myvec.getYValue());
//		return myvec;
//	}
//	
//	FastMove(Toroidal2DPhysics space, Ship ship, Position currentPosition, Position targetPosition) {
//		super(space, currentPosition, targetPosition, getVec(space, currentPosition, targetPosition));
//		//System.out.println("Getting here?");
//		this.startTime = startTime;
//		this.endTime = startTime + 200;
//		this.targetPos = targetPosition;
//		this.currentPos = currentPosition;
//		this.space = space;
//		this.ship = ship;
//	}
//	
//	public ArrayList<SpacewarGraphics> getGraphics() {
//		graphicsToAdd = new ArrayList<SpacewarGraphics>();
//		double startX = currentPos.getX();
//		double startY = currentPos.getY();
//		double endX = targetPos.getX();
//		double endY = targetPos.getY();
//
//		double aX, aY, bX, bY, cX, cY;
//		if (startX > endX) {
//			aX = startX;
//			bX = endX;
//		} else {
//			bX = startX;
//			aX = endX;
//		}
//		if (startY > endY) {
//			aY = startY;
//			bY = endY;
//		} else {
//			bY = startY;
//			aY = endY;
//		}
//		Position oneSide, otherSide;
//		LineGraphics line;
//		
//		cX = bX;
//		cY = bY;
//		
//		while (cX <= aX) {
//			System.out.println("running");
//			oneSide = new Position(cX, aY);
//			otherSide = new Position(cX, bY);
//			line = new LineGraphics(oneSide, otherSide, 
//					space.findShortestDistanceVector(oneSide, otherSide));
//			line.setLineColor(Color.YELLOW);
//			graphicsToAdd.add(line);
//			cX += 15;
//		}
//		while (cY <= aY) {
//			oneSide = new Position(aX, cY);
//			otherSide = new Position(bX, cY);
//			line = new LineGraphics(oneSide, otherSide, space.findShortestDistanceVector(oneSide, otherSide));
//			line.setLineColor(Color.YELLOW);
//			graphicsToAdd.add(line);
//			cY += 15;
//		}
//		/*Position oneCorner = new Position(startX, endY);
//		Position otherCorner = new Position(endX, startY);
//
//		line = new LineGraphics(currentPos, oneCorner, 
//				space.findShortestDistanceVector(currentPos, oneCorner));
//		line.setLineColor(Color.YELLOW);
//		graphicsToAdd.add(line);
//		
//		line = new LineGraphics(currentPos, otherCorner, 
//				space.findShortestDistanceVector(currentPos, otherCorner));
//		line.setLineColor(Color.YELLOW);
//		graphicsToAdd.add(line);
//		
//		line = new LineGraphics(targetPos, oneCorner, 
//				space.findShortestDistanceVector(targetPos, oneCorner));
//		line.setLineColor(Color.YELLOW);
//		graphicsToAdd.add(line);
//		
//		line = new LineGraphics(targetPos, otherCorner, 
//				space.findShortestDistanceVector(targetPos, otherCorner));
//		line.setLineColor(Color.YELLOW);
//		graphicsToAdd.add(line);*/
//		
//		return graphicsToAdd;
//	}
//
//	@Override
//	public boolean isMovementFinished(Toroidal2DPhysics space) {
//		if (space.getCurrentTimestep() > endTime)
//			return true;
//		else
//			return false;
//	}
//	
//}