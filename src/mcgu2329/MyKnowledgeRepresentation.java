package mcgu2329;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import spacesettlers.actions.AbstractAction;
import spacesettlers.objects.AbstractObject;
import spacesettlers.objects.Asteroid;
import spacesettlers.objects.Base;
import spacesettlers.objects.Beacon;
import spacesettlers.objects.Ship;
import spacesettlers.simulator.Toroidal2DPhysics;
import spacesettlers.utilities.Position;
import spacesettlers.utilities.Vector2D;

class MyKnowledgeRepresentation {
	/**
	 * Closest enemy ship in space
	 */
	Ship nearestEnemy;
	/**
	 * Closest base in space
	 */
	Base nearestBase;
	/**
	 * Closest beacon in space
	 */
	Beacon nearestBeacon;
	/**
	 * Current action being undertaken
	 */
	AbstractAction currentAction;
	/**
	 * Current position of ship in space
	 */
	Position currentPosition;
	/**
	 * Current level of energy of ship (adjusted by 500 to ensure we continue to chase weakened enemies)
	 */
	double effectiveEnergyLevel;
	
	double firingRange = 500;
	
	boolean shouldShoot;
	
	/**
	 * Constructor that generates state information relative to given ship (egotistical approach)
	 * 
	 * @param space state of the map
	 * @param ship the ship which other info is measured from
	 */
	MyKnowledgeRepresentation(Toroidal2DPhysics space, Ship ship) {
		nearestEnemy = pickNearestEnemyShip(space, ship);
		nearestBase = findNearestBase(space, ship);
		nearestBeacon = pickNearestBeacon(space, ship);
		currentAction = ship.getCurrentAction();
		currentPosition = ship.getPosition();
		effectiveEnergyLevel = ship.getEnergy() + 500;
		shouldShoot = shouldShoot(space, ship);
	}
	
	/**
	 * Find the nearest ship on another team and aim for it
	 * @param space
	 * @param ship
	 * @return
	 */
	private Ship pickNearestEnemyShip(Toroidal2DPhysics space, Ship ship) {
		double minDistance = Double.POSITIVE_INFINITY;
		Ship nearestShip = null;
		for (Ship otherShip : space.getShips()) {
			// don't aim for our own team (or ourself)
			if (otherShip.getTeamName().equals(ship.getTeamName())) {
				continue;
			}
			
			double distance = space.findShortestDistance(ship.getPosition(), otherShip.getPosition());
			if (distance < minDistance) {
				minDistance = distance;
				nearestShip = otherShip;
			}
		}
		
		return nearestShip;
	}
	
	/**
	 * Find the base for this team nearest to this ship
	 * 
	 * @param space
	 * @param ship
	 * @return
	 */
	private Base findNearestBase(Toroidal2DPhysics space, Ship ship) {
		double minDistance = Double.MAX_VALUE;
		Base nearestBase = null;

		for (Base base : space.getBases()) {
			if (base.getTeamName().equalsIgnoreCase(ship.getTeamName())) {
				double dist = space.findShortestDistance(ship.getPosition(), base.getPosition());
				if (dist < minDistance) {
					minDistance = dist;
					nearestBase = base;
				}
			}
		}
		return nearestBase;
	}

	/**
	 * Find the nearest beacon to this ship
	 * @param space
	 * @param ship
	 * @return
	 */
	private Beacon pickNearestBeacon(Toroidal2DPhysics space, Ship ship) {
		// get the current beacons
		Set<Beacon> beacons = space.getBeacons();

		Beacon closestBeacon = null;
		double bestDistance = Double.POSITIVE_INFINITY;

		for (Beacon beacon : beacons) {
			double dist = space.findShortestDistance(ship.getPosition(), beacon.getPosition());
			if (dist < bestDistance) {
				bestDistance = dist;
				closestBeacon = beacon;
			}
		}

		return closestBeacon;
	}
	
	private boolean shouldShoot(Toroidal2DPhysics space, Ship ship) {
		double myOrientation = ship.getPosition().getOrientation();
		double theirVel = nearestEnemy.getPosition().getTranslationalVelocity().getAngle();
		
		if (space.findShortestDistance(currentPosition, nearestEnemy.getPosition()) > firingRange)
			return false;
		else {
			double myAngle = ship.getPosition().getOrientation();
			Vector2D aimVec = space.findShortestDistanceVector(currentPosition, nearestEnemy.getPosition());
			aimVec = aimVec.add(nearestEnemy.getPosition().getTranslationalVelocity());
			double enemyAngle = aimVec.getAngle();
			
			double result = Double.POSITIVE_INFINITY;
			double firingWindow = Math.PI / 8;
			if (myAngle < 0)
				myAngle += (2*Math.PI);
			if (enemyAngle < 0)
				enemyAngle += (2*Math.PI);
			if (myAngle > (2*Math.PI - firingWindow)) {
				if (enemyAngle < firingWindow) {
					result = (2*Math.PI - myAngle + enemyAngle);
				}
			} else if (enemyAngle > (2*Math.PI - firingWindow)) {
				if (myAngle < firingWindow) {
					result = (2*Math.PI + myAngle - enemyAngle);
				}
			} else {
				result = Math.abs(myAngle - enemyAngle);
			}
								
			if (result < firingWindow)
				return true;
			else
				return false;
		}
	}
}
