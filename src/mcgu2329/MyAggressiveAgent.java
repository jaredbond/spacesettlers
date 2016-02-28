package mcgu2329;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;

import spacesettlers.clients.TeamClient;
import spacesettlers.actions.AbstractAction;
import spacesettlers.actions.DoNothingAction;
import spacesettlers.actions.MoveAction;
import spacesettlers.actions.MoveToObjectAction;
import spacesettlers.actions.PurchaseCosts;
import spacesettlers.actions.PurchaseTypes;
import spacesettlers.graphics.LineGraphics;
import spacesettlers.graphics.SpacewarGraphics;
import spacesettlers.graphics.TargetGraphics;
import spacesettlers.objects.AbstractActionableObject;
import spacesettlers.objects.AbstractObject;
import spacesettlers.objects.Asteroid;
import spacesettlers.objects.Base;
import spacesettlers.objects.Beacon;
import spacesettlers.objects.Ship;
import spacesettlers.objects.powerups.SpaceSettlersPowerupEnum;
import spacesettlers.objects.resources.ResourcePile;
import spacesettlers.objects.weapons.AbstractWeapon;
import spacesettlers.simulator.Toroidal2DPhysics;
import spacesettlers.utilities.Position;
import spacesettlers.utilities.Vector2D;

/**
 * Modification of the aggressive heuristic asteroid collector to a team that only has one ship.  It 
 * tries to collect resources but it also tries to shoot other ships if they are nearby.
 * 
 * @author amy
 */
public class MyAggressiveAgent extends TeamClient {
	HashMap <UUID, Ship> asteroidToShipMap;
	HashMap <UUID, Boolean> aimingForBase;
	UUID asteroidCollectorID;
	double weaponsProbability = 0.25;
	double firingRange = 500;
	boolean shouldShoot = false;
	int myTimestep = 0;
	double previousVelocity = 0;
	boolean isAccelerating = false;
	MyKnowledgeRepresentation knowledge;
	private ArrayList<SpacewarGraphics> graphicsToAdd;
	Node[][] nodeMatrix;
	int mX;
	int mY;

	// Space height = 1080
	// Height div = 54 x 20
	// Space width = 1600
	// Width div = 80 x 20

	/**
	 * Assigns ships to asteroids and beacons, as described above
	 */
	public Map<UUID, AbstractAction> getMovementStart(Toroidal2DPhysics space,
			Set<AbstractActionableObject> actionableObjects) {
		HashMap<UUID, AbstractAction> actions = new HashMap<UUID, AbstractAction>();

		// loop through each ship
		for (AbstractObject actionable :  actionableObjects) {
			if (actionable instanceof Ship) {
				Ship ship = (Ship) actionable;

				// the first time we initialize, decide which ship is the asteroid collector
				if (asteroidCollectorID == null) {
					asteroidCollectorID = ship.getId();
				}

				//AbstractAction action = MyAggressiveAction(space, ship);
				AbstractAction action = MyOtherAction(space, ship);
				knowledge.currentAction = action;
				actions.put(ship.getId(), action);

			} else {
				// it is a base.  Heuristically decide when to use the shield (TODO)
				actions.put(actionable.getId(), new DoNothingAction());
			}
		} 
		return actions;
	}

	private AbstractAction MyOtherAction(Toroidal2DPhysics space, Ship ship) {
		knowledge = new MyKnowledgeRepresentation(space, ship);
		//AbstractAction current = knowledge.currentAction;
		Position currentPosition = knowledge.currentPosition;
		//	AbstractAction newAction = null;
		//Ship enemy = knowledge.nearestEnemy;
		AbstractAction current = knowledge.currentAction;
		Stack<Node> s = new Stack<Node>();
		if (current == null || current.isMovementFinished(space)) {
			if (s.isEmpty()) {
				graphicsToAdd.clear();
				Random random = new Random();
				Position newGoal = space.getRandomFreeLocationInRegion(random, Ship.SHIP_RADIUS, (int) currentPosition.getX(), 
						(int) currentPosition.getY(), 300);

				TargetGraphics t = new TargetGraphics(10, newGoal);
				Vector2D goalVec = space.findShortestDistanceVector(currentPosition, newGoal);
				goalVec = goalVec.getUnitVector();
				goalVec = goalVec.multiply(5);
				goalVec = goalVec.rotate(Math.PI/2);
				Position newP = new Position(newGoal.getX()+goalVec.getXValue(), newGoal.getY()+goalVec.getYValue());
				LineGraphics line = new LineGraphics(newGoal, newP, goalVec);
				line.setLineColor(Color.YELLOW);
				graphicsToAdd.add(t);
				graphicsToAdd.add(line);
				s = AStar(space, nodeMatrix, currentPosition, newGoal);
			}
			Position intGoal = s.pop().getCenter();
			TargetGraphics t1 = new TargetGraphics(5, intGoal);
			graphicsToAdd.add(t1);
			MoveAction move = new MoveAction(space, currentPosition, intGoal);
			move.setKpTranslational(9);
			move.setKvTranslational(6);
			move.setKpRotational(4);
			move.setKvTranslational(4);
			//FastMove myMove = new FastMove(space, ship, AStar(space, nodeMatrix, currentPosition, newGoal));
			//graphicsToAdd.addAll(myMove.getGraphics());
			return move;
		} else {
			return current;
		}
	}


	/**
	 * Aggressive heuristic that prioritizes attacking and killing enemy ships until significantly weakened
	 * It then flees to the closest beacon, or in a random direction if no beacon is nearby
	 * If no enemy is nearby, it also seeks a beacon
	 * 
	 * @param space current map of space
	 * @param ship whose action is to be determined
	 * @return action to be taken
	 */

	private AbstractAction MyAggressiveAction(Toroidal2DPhysics space, Ship ship) {
		knowledge = new MyKnowledgeRepresentation(space, ship);
		AbstractAction current = knowledge.currentAction;
		Position currentPosition = knowledge.currentPosition;
		AbstractAction newAction = null;
		Ship enemy = knowledge.nearestEnemy;
		//System.out.println("(" + ship.getPosition().getX() + "," + ship.getPosition().getY() + ")");
		/*if (currentPosition.getTotalTranslationalVelocity() > previousVelocity) {
			isAccelerating = true;
			System.out.println("Im accelerating. Vel = " + currentPosition.getTotalTranslationalVelocity());
		} else
			isAccelerating = false;
		previousVelocity = currentPosition.getTotalTranslationalVelocity();
		 */
		if (ship.getEnergy() < 1000) {/*
			Beacon beacon = knowledge.nearestBeacon;
			// if there is no beacon, then just skip a turn
			if (beacon == null) {
				newAction = new DoNothingAction();
			} else {
				newAction = new MoveToObjectAction(space, currentPosition, beacon);
			}
//			aimingForBase.put(ship.getId(), false);
			shouldShoot = false;
			return newAction;*/
			shouldShoot = false;
			Beacon beacon = knowledge.nearestBeacon;
			Base base = knowledge.nearestBase;
			if (beacon != null && base != null) {
				double beaconD = space.findShortestDistance(currentPosition, beacon.getPosition());
				double baseD = space.findShortestDistance(currentPosition, base.getPosition());
				if (base.getHealingEnergy() < 500)
					baseD = Double.POSITIVE_INFINITY;
				if (beaconD < baseD) // If the beacon is closer than the base
					return new MoveToObjectAction(space, currentPosition, beacon);
				else
					return new MoveToObjectAction(space, currentPosition, base);
			} else if (beacon != null) { // Flee to a beacon!
				return new MoveToObjectAction(space, currentPosition, beacon);
			} else if (base != null) {
				if (base.getHealingEnergy() > 500)
					return new MoveToObjectAction(space, currentPosition, base);
			} else { // No beacon or base, just flee!!!
				return new DoNothingAction();
			}
			return new DoNothingAction();
		}
		if (current == null || current.isMovementFinished(space)) {
			graphicsToAdd.clear();
			if (enemy != null) { // Is there an enemy nearby?
				if (enemy.getEnergy() < knowledge.effectiveEnergyLevel) { // Is the enemy weaker than me?
					// ATTACK!!
					shouldShoot = knowledge.shouldShoot;
					FastMove myMove = new FastMove(space, ship, AStar(space, nodeMatrix, currentPosition, enemy.getPosition()));
					graphicsToAdd.addAll(myMove.getGraphics());
					return myMove;
				} else { // The enemy is stronger than me
					// FLEE!!!
					shouldShoot = false;
					Beacon beacon = knowledge.nearestBeacon;
					Base base = knowledge.nearestBase;
					if (beacon != null && base != null) {
						double beaconD = space.findShortestDistance(currentPosition, beacon.getPosition());
						double baseD = space.findShortestDistance(currentPosition, base.getPosition());
						if (base.getHealingEnergy() < 500)
							baseD = Double.POSITIVE_INFINITY;
						if (beaconD < baseD) { // If the beacon is closer than the base
							FastMove myMove = new FastMove(space, ship, AStar(space, nodeMatrix, currentPosition, beacon.getPosition()));
							graphicsToAdd.addAll(myMove.getGraphics());
							return myMove;
						} else {
							FastMove myMove = new FastMove(space, ship, AStar(space, nodeMatrix, currentPosition, base.getPosition()));
							graphicsToAdd.addAll(myMove.getGraphics());
							return myMove;
						}
					} else if (beacon != null) { // Flee to a beacon!
						FastMove myMove = new FastMove(space, ship, AStar(space, nodeMatrix, currentPosition, beacon.getPosition()));
						graphicsToAdd.addAll(myMove.getGraphics());
						return myMove;
					} else if (base != null) {
						if (base.getHealingEnergy() > 500) {
							FastMove myMove = new FastMove(space, ship, AStar(space, nodeMatrix, currentPosition, base.getPosition()));						
							graphicsToAdd.addAll(myMove.getGraphics());
							return myMove;
						}
					} else { // No beacon or base, just flee!!!
						Random random = new Random();
						Position newGoal = space.getRandomFreeLocationInRegion(random, Ship.SHIP_RADIUS, (int) currentPosition.getX(), 
								(int) currentPosition.getY(), 200);
						FastMove myMove = new FastMove(space, ship, AStar(space, nodeMatrix, currentPosition, newGoal));
						graphicsToAdd.addAll(myMove.getGraphics());
						return myMove;
					}
					return new DoNothingAction();
				}
			} else { // No enemy nearby
				/*Beacon beacon = knowledge.nearestBeacon;
			if (beacon != null) { // Be productive
				shouldShoot = false;
				Vector2D myvec = space.findShortestDistanceVector(currentPosition, beacon.getPosition());
				if (myvec.getXValue() > myvec.getYValue())
					myvec = myvec.multiply(50.0/myvec.getXValue());
				else
					myvec = myvec.multiply(50.0/myvec.getYValue());
				newAction = new MoveAction(space, currentPosition, beacon.getPosition(), myvec);
				return newAction;
			} else { // No beacon
				shouldShoot = false;
				newAction = new DoNothingAction();
				return newAction;
			}*/
				shouldShoot = false;
				Beacon beacon = knowledge.nearestBeacon;
				Base base = knowledge.nearestBase;
				if (beacon != null && base != null) {
					double beaconD = space.findShortestDistance(currentPosition, beacon.getPosition());
					double baseD = space.findShortestDistance(currentPosition, base.getPosition());
					if (base.getHealingEnergy() < 500)
						baseD = Double.POSITIVE_INFINITY;
					if (beaconD < baseD) { // If the beacon is closer than the base
						FastMove myMove = new FastMove(space, ship, AStar(space, nodeMatrix, currentPosition, beacon.getPosition()));
						graphicsToAdd.addAll(myMove.getGraphics());
						return myMove;
					} else {
						FastMove myMove = new FastMove(space, ship, AStar(space, nodeMatrix, currentPosition, base.getPosition()));
						graphicsToAdd.addAll(myMove.getGraphics());
						return myMove;
					}
				} else if (beacon != null) { // Flee to a beacon!
					FastMove myMove = new FastMove(space, ship, AStar(space, nodeMatrix, currentPosition, beacon.getPosition()));
					graphicsToAdd.addAll(myMove.getGraphics());
					return myMove;
				} else if (base != null) {
					if (base.getHealingEnergy() > 500) {
						FastMove myMove = new FastMove(space, ship, AStar(space, nodeMatrix, currentPosition, base.getPosition()));
						graphicsToAdd.addAll(myMove.getGraphics());
						return myMove;
					}
				} else { // No beacon or base, just flee!!!
					return new DoNothingAction();
				}
				return new DoNothingAction();
			}
		} else {
			return current;
		}
	}

	@Override
	public void getMovementEnd(Toroidal2DPhysics space, Set<AbstractActionableObject> actionableObjects) {
		ArrayList<Asteroid> finishedAsteroids = new ArrayList<Asteroid>();

		for (UUID asteroidId : asteroidToShipMap.keySet()) {
			Asteroid asteroid = (Asteroid) space.getObjectById(asteroidId);
			if (asteroid != null && !asteroid.isAlive()) {
				finishedAsteroids.add(asteroid);
			}
		}

		for (Asteroid asteroid : finishedAsteroids) {
			asteroidToShipMap.remove(asteroid);
		}
	}

	/*private AbstractAction fastMove(Toroidal2DPhysics space, Ship ship, Position currentPosition, Position targetPosition) {
		Vector2D myvec = space.findShortestDistanceVector(currentPosition, targetPosition);
		if (myvec.getXValue() > myvec.getYValue())
			myvec = myvec.multiply(50.0/myvec.getXValue());
		else
			myvec = myvec.multiply(50.0/myvec.getYValue());
		return new MoveAction(space, currentPosition, targetPosition, myvec);
	}*/

	@Override
	public void initialize(Toroidal2DPhysics space) {
		mX = space.getWidth()/20;
		mY = space.getHeight()/20;
		asteroidToShipMap = new HashMap<UUID, Ship>();
		asteroidCollectorID = null;
		aimingForBase = new HashMap<UUID, Boolean>();
		graphicsToAdd = new ArrayList<SpacewarGraphics>();
		nodeMatrix = new Node[mX][mY];
		generateGraph(space, nodeMatrix);
	}

	public void generateGraph(Toroidal2DPhysics space, Node[][] nodeMatrix) {

		// Squares are 20x20

		// Create Nodes
		for (int i = 0; i < mX; i++) {
			for (int j = 0; j < mY; j++) {
				Position p = new Position((20*i)+10, (20*j)+10);
				nodeMatrix[i][j] = new Node(i, j, p);
			}
		}
		// Link Nodes
		for (int i = mX-1; i >= 0; i--) { //EDIT THISSSSSSSS
			for (int j = mY-1; j >= 0;j--) {
				if (i > 0) {
					nodeMatrix[i][j].addNode(nodeMatrix[i-1][j]);
					nodeMatrix[i-1][j].addNode(nodeMatrix[i][j]);
				}
				if (j > 0) {

					nodeMatrix[i][j].addNode(nodeMatrix[i][j-1]);
					nodeMatrix[i][j-1].addNode(nodeMatrix[i][j]);

				}
			}
		}
		System.out.println("Finished generating graph");
	}

	public Stack<Node> AStar(Toroidal2DPhysics space, Node[][] nodeMatrix, Position start, Position goal) {
		System.out.println("Starting ASTAR");
		PriorityQueue<Node> queue = new PriorityQueue<Node>(100, new MyComparator());
		Stack<Node> stack = null;
		double oX, oY;
		for (int i = 0; i < mX; i++) {
			for (int j = 0; j < mY; j++) {
				nodeMatrix[i][j].setHn(space.findShortestDistance(nodeMatrix[i][j].getCenter(), goal));
				nodeMatrix[i][j].setUnseen();
			}
		}
		for (AbstractObject o : space.getAllObjects()) {
			oX = o.getPosition().getX();
			oY = o.getPosition().getY();
			nodeMatrix[(int)(oX/20)][(int)(oY/20)].setSeen();
		}
		oX = start.getX();
		oY = start.getY();
		queue.add(nodeMatrix[(int)(oX/20)][(int)(oY/20)]);

		while (!queue.isEmpty()) {
			Node currentNode = queue.remove();
			currentNode.setSeen();
			if (currentNode.getHn() < 200) {
				System.out.println("Found the goal!");
				Node printNode = currentNode;
				stack = new Stack<Node>();
				while (printNode != null) {
					stack.add(printNode);
					printNode = printNode.getParent();
				}
				System.out.println("ASTAR Finshed");
				return stack;
			}
			for (Node n : currentNode.getAdjNodes()) {
				if (!n.getSeen()) {
					n.setParent(currentNode);
					n.setGn(currentNode.getGn()+1);
					queue.add(n);
				}
			}
		}
		System.out.println("ASTAR Finshed");
		return null;
	}

	class MyComparator implements Comparator<Node> {
		@Override
		public int compare(Node x, Node y) {
			if (x.getFn() < y.getFn())
				return -1;
			else if (x.getFn() > y.getFn())
				return 1;
			else
				return 0;
		}
	}

	@Override
	public void shutDown(Toroidal2DPhysics space) {
		// TODO Auto-generated method stub
	}

	@Override
	public Set<SpacewarGraphics> getGraphics() {
		HashSet<SpacewarGraphics> graphics = new HashSet<SpacewarGraphics>();
		graphics.addAll(graphicsToAdd);
		//graphicsToAdd.clear();
		return graphics;
	}

	@Override
	/**
	 * If there is enough resourcesAvailable, buy a base.  Place it by finding a ship that is sufficiently
	 * far away from the existing bases
	 */
	public Map<UUID, PurchaseTypes> getTeamPurchases(Toroidal2DPhysics space,
			Set<AbstractActionableObject> actionableObjects, 
			ResourcePile resourcesAvailable, 
			PurchaseCosts purchaseCosts) {

		HashMap<UUID, PurchaseTypes> purchases = new HashMap<UUID, PurchaseTypes>();
		double BASE_BUYING_DISTANCE = 200;
		boolean bought_base = false;

		if (purchaseCosts.canAfford(PurchaseTypes.BASE, resourcesAvailable)) {
			for (AbstractActionableObject actionableObject : actionableObjects) {
				if (actionableObject instanceof Ship) {
					Ship ship = (Ship) actionableObject;
					Set<Base> bases = space.getBases();

					// how far away is this ship to a base of my team?
					double maxDistance = Double.MIN_VALUE;
					for (Base base : bases) {
						if (base.getTeamName().equalsIgnoreCase(getTeamName())) {
							double distance = space.findShortestDistance(ship.getPosition(), base.getPosition());
							if (distance > maxDistance) {
								maxDistance = distance;
							}
						}
					}

					if (maxDistance > BASE_BUYING_DISTANCE) {
						purchases.put(ship.getId(), PurchaseTypes.BASE);
						bought_base = true;
						break;
					}
				}
			}		
		} 

		// see if you can buy EMPs
		if (purchaseCosts.canAfford(PurchaseTypes.POWERUP_EMP_LAUNCHER, resourcesAvailable)) {
			for (AbstractActionableObject actionableObject : actionableObjects) {
				if (actionableObject instanceof Ship) {
					Ship ship = (Ship) actionableObject;

					if (!ship.getId().equals(asteroidCollectorID) && ship.isValidPowerup(PurchaseTypes.POWERUP_EMP_LAUNCHER.getPowerupMap())) {
						purchases.put(ship.getId(), PurchaseTypes.POWERUP_EMP_LAUNCHER);
					}
				}
			}		
		} 


		// can I buy a ship?
		if (purchaseCosts.canAfford(PurchaseTypes.SHIP, resourcesAvailable) && bought_base == false) {
			for (AbstractActionableObject actionableObject : actionableObjects) {
				if (actionableObject instanceof Base) {
					Base base = (Base) actionableObject;

					purchases.put(base.getId(), PurchaseTypes.SHIP);
					break;
				}
			}
		}
		return purchases;
	}

	/**
	 * The aggressive asteroid collector shoots if there is an enemy nearby! 
	 * 
	 * @param space
	 * @param actionableObjects
	 * @return
	 */
	@Override
	public Map<UUID, SpaceSettlersPowerupEnum> getPowerups(Toroidal2DPhysics space,
			Set<AbstractActionableObject> actionableObjects) {
		Random rand = new Random();
		HashMap<UUID, SpaceSettlersPowerupEnum> powerupMap = new HashMap<UUID, SpaceSettlersPowerupEnum>();

		for (AbstractObject actionable :  actionableObjects) {
			if (actionable instanceof Ship) {
				Ship ship = (Ship) actionable;
				if (shouldShoot && rand.nextDouble() < weaponsProbability) {
					AbstractWeapon newBullet = ship.getNewWeapon(SpaceSettlersPowerupEnum.FIRE_MISSILE);
					if (newBullet != null) {
						powerupMap.put(ship.getId(), SpaceSettlersPowerupEnum.FIRE_MISSILE);
						//System.out.println("Firing!");
					}
				}
			}
		}
		return powerupMap;
	}

}
