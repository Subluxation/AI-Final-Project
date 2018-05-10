package nguy0001;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import nguy0001.astar.AStarSearch;
import nguy0001.astar.FollowPathAction;
import nguy0001.astar.Graph;
import nguy0001.astar.Vertex;
import spacesettlers.actions.AbstractAction;
import spacesettlers.actions.DoNothingAction;
import nguy0001.MoveToObjectAction;
import spacesettlers.actions.PurchaseCosts;
import spacesettlers.actions.PurchaseTypes;
import spacesettlers.clients.TeamClient;
import spacesettlers.graphics.SpacewarGraphics;
import spacesettlers.graphics.StarGraphics;
import spacesettlers.objects.AbstractActionableObject;
import spacesettlers.objects.AbstractObject;
import spacesettlers.objects.Asteroid;
import spacesettlers.objects.Base;
import spacesettlers.objects.Beacon;
import spacesettlers.objects.Flag;
import spacesettlers.objects.Ship;
import spacesettlers.objects.powerups.SpaceSettlersPowerupEnum;
import spacesettlers.objects.resources.ResourcePile;
import spacesettlers.simulator.Toroidal2DPhysics;
import spacesettlers.utilities.Position;

/**
 * Just a beacon collector but it uses Astar (to show how to do it) and shows the Astar graphics
 * 
 * @author amy
 */
public class ExampleAStarClient extends TeamClient {
	HashMap <UUID, Ship> asteroidToShipMap;
	Planning planner;
	FollowPathAction followPathAction;
	HashMap <UUID, Graph> graphByShip;
	final String FLAG = "flag";
	final String ASTEROID = "asteroid";

	int REPLAN_STEPS = 100;
	boolean flagCollected = false;
	/**
	 * Assigns ships to asteroids and beacons, as described above
	 */
	public Map<UUID, AbstractAction> getMovementStart(Toroidal2DPhysics space,
			Set<AbstractActionableObject> actionableObjects) {
		HashMap<UUID, AbstractAction> actions = new HashMap<UUID, AbstractAction>();
		HashMap<UUID, String> shipRoles = planner.getShipRoles();

		// loop through each ship
		for (AbstractObject actionable :  actionableObjects) {
			if (actionable instanceof Ship) {
				Ship ship = (Ship) actionable;
				AbstractAction current = ship.getCurrentAction();

				if (planner.getNumFlagShips() < 2 && !shipRoles.containsKey(ship.getId()))
				{
					shipRoles.put(ship.getId(), FLAG);
					planner.add2RoleHash(ship.getId(), FLAG);
					System.out.println("ADDED FLAG SHIP");
				}
				else if (!shipRoles.containsKey(ship.getId()))
				{
					shipRoles.put(ship.getId(), ASTEROID);
					planner.add2RoleHash(ship.getId(), ASTEROID);
					System.out.println("ADDED ASTEROID SHIP");
				}

				String role = shipRoles.get(ship.getId());
				//				System.out.println("ROLE: " + role);
				if (role.equals(FLAG))
				{
					Flag obj = getEnemyFlag(space);
					if (obj != null) {
						flagCollected = false;
					}
					//current == null
					if (flagCollected == false || !ship.isCarryingFlag())
					{
						//						System.out.println("Collecting Enemy Flag...");
						obj = getEnemyFlag(space);
						if(obj.isBeingCarried() || obj == null) {
							actions.put(ship.getId(), Planning.WaitForFlag2(space, ship.getId()));
						}
						else {
							AbstractAction action = getAStarPathToGoal(space, ship, obj.getPosition());
							actions.put(ship.getId(), action);
//							actions.put(ship.getId(), Planning.Move2Flag(space,ship.getId(), obj));

						}
						//						actions.put(ship.getId(), Planning.Move2Flag(space,ship.getId(), obj));
					}
					if (ship.isCarryingFlag())
					{
						flagCollected = true;
						Base base = findNearestBase(space, ship);	
						actions.put(ship.getId(), Planning.Deposit(space,ship.getId(), base));
					}
				}
				else if (role.equals(ASTEROID))
				{
					AbstractAction action;
					Asteroid asteroid = pickHighestValueNearestFreeAsteroid(space, ship);
					actions.put(ship.getId(), Planning.Move(ship.getId(), asteroid,space));

					//					Beacon beacon = pickNearestBeacon(space, ship);
					//					AbstractAction action = getAStarPathToGoal(space, ship, beacon.getPosition());

					//					actions.put(ship.getId(), action);
				}
				//				
				//				//If flagShip
				//				if ((current == null || currentSteps >= REPLAN_STEPS) && flagCollected == false) {
				//					Flag objective = getEnemyFlag(space);
				//					AbstractAction action = getAStarPathToGoal(space, ship, objective.getPosition());
				//					actions.put(ship.getId(), action);
				//					currentSteps = 0;
				//				}
				//				
				//				//return to base ASAP
				//				if(ship.isCarryingFlag() == true) {
				//					flagCollected = true;
				//					Base home = findNearestBase(space, ship);
				//					AbstractAction action = getAStarPathToGoal(space,ship,home.getPosition());
				//					actions.put(ship.getId(), action);
				//				}				
				//				
				//				if (current == null || currentSteps >= REPLAN_STEPS) {
				//					Beacon beacon = pickNearestBeacon(space, ship);
				//					AbstractAction action = getAStarPathToGoal(space, ship, beacon.getPosition());
				//					actions.put(ship.getId(), action);
				//					currentSteps = 0;
				//				}
			} else {
				// it is a base.  Do nothing
				actions.put(actionable.getId(), new DoNothingAction());
			}
		} 

		return actions;
	}

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

	private Flag getEnemyFlag(Toroidal2DPhysics space) {
		Flag enemyFlag = null;
		for (Flag flag : space.getFlags()) {
			if (flag.getTeamName().equalsIgnoreCase(getTeamName())) {
				continue;
			} else {
				enemyFlag = flag;
			}
		}
		return enemyFlag;
	}

	/**
	 * Follow an aStar path to the goal
	 * @param space
	 * @param ship
	 * @param goalPosition
	 * @return
	 */
	private AbstractAction getAStarPathToGoal(Toroidal2DPhysics space, Ship ship, Position goalPosition) {
		AbstractAction newAction;

		Graph graph = AStarSearch.createGraphToGoalWithBeacons(space, ship, goalPosition, new Random());
		Vertex[] path = graph.findAStarPath(space);
		followPathAction = new FollowPathAction(path);
		//followPathAction.followNewPath(path);
		newAction = followPathAction.followPath(space, ship);
		graphByShip.put(ship.getId(), graph);
		return newAction;
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



	@Override
	public void getMovementEnd(Toroidal2DPhysics space, Set<AbstractActionableObject> actionableObjects) {
		ArrayList<Asteroid> finishedAsteroids = new ArrayList<Asteroid>();

		for (UUID asteroidId : asteroidToShipMap.keySet()) {
			Asteroid asteroid = (Asteroid) space.getObjectById(asteroidId);
			if (asteroid == null || !asteroid.isAlive() || asteroid.isMoveable()) {
				finishedAsteroids.add(asteroid);
				//System.out.println("Removing asteroid from map");
			}
		}

		for (Asteroid asteroid : finishedAsteroids) {
			asteroidToShipMap.remove(asteroid.getId());
		}



	}

	@Override
	public void initialize(Toroidal2DPhysics space) {
		asteroidToShipMap = new HashMap<UUID, Ship>();
		planner = new Planning(space);
		graphByShip = new HashMap<UUID, Graph>();
	}

	@Override
	public void shutDown(Toroidal2DPhysics space) {
		// TODO Auto-generated method stub

	}

	@Override
	public Set<SpacewarGraphics> getGraphics() {
		HashSet<SpacewarGraphics> graphics = new HashSet<SpacewarGraphics>();
		if (graphByShip != null) {
			for (Graph graph : graphByShip.values()) {
				// uncomment to see the full graph
				//graphics.addAll(graph.getAllGraphics());
				graphics.addAll(graph.getSolutionPathGraphics());
			}
		}
		HashSet<SpacewarGraphics> newGraphicsClone = (HashSet<SpacewarGraphics>) graphics.clone();
		graphics.clear();
		return newGraphicsClone;
	}

	@Override
	/**
	 * Never purchases
	 */
	public Map<UUID, PurchaseTypes> getTeamPurchases(Toroidal2DPhysics space,
			Set<AbstractActionableObject> actionableObjects, 
			ResourcePile resourcesAvailable, 
			PurchaseCosts purchaseCosts) {

		return null;
	}
	/**
	 * Returns the asteroid of highest value that isn't already being chased by this team
	 * 
	 * @return
	 */
	private Asteroid pickHighestValueNearestFreeAsteroid(Toroidal2DPhysics space, Ship ship) {
		Set<Asteroid> asteroids = space.getAsteroids();
		int bestMoney = Integer.MIN_VALUE;
		Asteroid bestAsteroid = null;
		double minDistance = Double.MAX_VALUE;

		for (Asteroid asteroid : asteroids) {
			if (!asteroidToShipMap.containsKey(asteroid.getId())) {
				if (asteroid.isMineable() && asteroid.getResources().getTotal() > bestMoney) {
					double dist = space.findShortestDistance(asteroid.getPosition(), ship.getPosition());
					if (dist < minDistance) {
						bestMoney = asteroid.getResources().getTotal();
						//System.out.println("Considering asteroid " + asteroid.getId() + " as a best one");
						bestAsteroid = asteroid;
						minDistance = dist;
					}
				}
			}
		}
		//System.out.println("Best asteroid has " + bestMoney);
		return bestAsteroid;
	}

	/**
	 * No shooting
	 * 
	 * @param space
	 * @param actionableObjects
	 * @return
	 */
	@Override
	public Map<UUID, SpaceSettlersPowerupEnum> getPowerups(Toroidal2DPhysics space,
			Set<AbstractActionableObject> actionableObjects) {
		return null;
	}

}
