package nguy0001;

import java.util.HashMap;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import nguy0001.ExampleAStarClient;
import nguy0001.astar.AStarSearch;
import nguy0001.astar.FollowPathAction;
import nguy0001.astar.Graph;
import nguy0001.astar.Vertex;
import spacesettlers.actions.AbstractAction;
import spacesettlers.actions.MoveAction;
import spacesettlers.actions.MoveToObjectAction;
import spacesettlers.objects.AbstractObject;
import spacesettlers.objects.Asteroid;
import spacesettlers.objects.Base;
import spacesettlers.objects.Flag;
import spacesettlers.objects.Ship;
import spacesettlers.simulator.Toroidal2DPhysics;
import spacesettlers.utilities.Position;
import spacesettlers.utilities.Vector2D;
/**
 * Planning class used for doling out tasks depending on satisfied pre-conditions
 * @author SpencerBarnes & Anthony Nguyen
 *
 */
public class Planning {


	public static HashMap<UUID, AbstractObject> shipGoal;
	public static HashMap<UUID, String> shipRole;
	public static HashMap<UUID, Boolean> shipFlag;
	public static HashMap<UUID, String> shipLocation;

//	public static UUID shipID;
	public static boolean baseLeft;
	public static String role;
	public static AbstractObject goalObj;
	//make sure to update the space every time this class is called on
//	public static Toroidal2DPhysics space;

	/**
	 * Empty Constructor for Planning
	 */
	public Planning(Toroidal2DPhysics newSpace) {
		shipGoal = new HashMap<UUID, AbstractObject>();
		shipRole = new HashMap<UUID, String>();
		shipFlag = new HashMap<UUID,Boolean>();
		shipLocation = new HashMap<UUID,String>();
//		space = newSpace;
	}

	/**
	 * Contructing Planner with first set of parameters to enter into hashmap
	 * @param shipID
	 * @param role
	 * @param goalObj
	 */
	public Planning(UUID shipID, String role, AbstractObject goalObj, Toroidal2DPhysics space) {
//		shipID = Planning.shipID;
		role = Planning.role;
		goalObj = Planning.goalObj;
//		space = this.space;

		shipGoal = new HashMap<UUID, AbstractObject>();
		shipGoal.put(shipID, goalObj);

		shipRole = new HashMap<UUID, String>();
		shipRole.put(shipID, role);
	}
	/**
	 * Is the ship assigned to a location?
	 * @param shipID
	 * @return
	 */
	public static boolean isInShipLoc(UUID shipID) {
		return shipLocation.containsKey(shipID);
	}

	
	/**
	 * Adding a ship with its associated goal to the hashmap
	 * @param shipID
	 * @param goalObj
	 */
	public void add2GoalHash(UUID shipID, AbstractObject goalObj) {
		shipGoal.put(shipID, goalObj);
	}
	/**
	 * Adding a ship and its associated role to the hashmap
	 * @param shipID
	 * @param role
	 */
	public void add2RoleHash(UUID shipID, String role) {
		shipRole.put(shipID, role);
	}

	//ACTIONS

	/**
	 * Places a flag carrier in between two possible flag spawns, depending on base location
	 * **FOR 2 FLAG SHIPS ONLY**
	 * @param shipID
	 * @return action
	 */
	public static AbstractAction WaitForFlag2(Toroidal2DPhysics space, UUID shipID) {
		//If a ship has a flag
		Ship ship = (Ship) space.getObjectById(shipID);
		if(shipFlag.containsValue(true)) {
			//if the base is on the left
			if(baseLeft == true) {
				Position right = new Position(1270,550);
				AbstractAction action = new MoveAction(space,ship.getPosition(),right,new Vector2D(0,0));
				return action;
				
			}
			//if base is on the right
			else {
				Position left = new Position(330,550);
				AbstractAction action = new MoveAction(space,ship.getPosition(),left,new Vector2D(0,0));
				return action;
			}
		}
		return null;
	}
	/**
	 * If the there are 3 flag ships, designate 2 different wait locations
	 * @param space
	 * @param shipID
	 * @return
	 */
	public static AbstractAction WaitForFlag3(Toroidal2DPhysics space, UUID shipID) {
		Ship ship = (Ship) space.getObjectById(shipID);
		//If a ship has a flag
		if(shipFlag.containsValue(true)) {
			if(baseLeft == true) {
				if(isInShipLoc(shipID)) {
					if(shipLocation.get(shipID).equalsIgnoreCase("top")) {
						Position top = new Position(1270,250);
						AbstractAction action = new MoveAction(space,ship.getPosition(),top,new Vector2D(0,0));
						return action;
					}else {
						Position bttm = new Position(1270,800);
						AbstractAction action = new MoveAction(space,ship.getPosition(),bttm,new Vector2D(0,0));
						return action;
					}
				}
				//not in shipLocation
				else {
					if(shipLocation.containsValue("bottom")) {
						shipLocation.put(shipID, "top");
						Position top = new Position(1270,250);
						AbstractAction action = new MoveAction(space,ship.getPosition(),top,new Vector2D(0,0));
						return action;
					}
					else if (shipLocation.containsValue("top")) {
						shipLocation.put(shipID, "bottom");
						Position bttm = new Position(1270,800);
						AbstractAction action = new MoveAction(space,ship.getPosition(),bttm,new Vector2D(0,0));
						return action;
					}
				}
			}
			//base is on the right
			else {
				if(isInShipLoc(shipID)) {
					if(shipLocation.get(shipID).equalsIgnoreCase("top")) {
						Position top = new Position(330,250);
						AbstractAction action = new MoveAction(space,ship.getPosition(),top,new Vector2D(0,0));
						return action;
					}else {
						Position bttm = new Position(330,800);
						AbstractAction action = new MoveAction(space,ship.getPosition(),bttm,new Vector2D(0,0));
						return action;
					}
				}
				//not in shipLocation
				else {
					if(shipLocation.containsValue("bottom")) {
						shipLocation.put(shipID, "top");
						Position top = new Position(330,250);
						AbstractAction action = new MoveAction(space,ship.getPosition(),top,new Vector2D(0,0));
						return action;
					}
					else if (shipLocation.containsValue("top")) {
						shipLocation.put(shipID, "bottom");
						Position bttm = new Position(330,800);
						AbstractAction action = new MoveAction(space,ship.getPosition(),bttm,new Vector2D(0,0));
						return action;
					}
				}
			}
		}
		
		
		
		return null;
	}
	
	/**
	 * Move to flag action
	 * @param shipID
	 * @param obj
	 * @return
	 */
	public static AbstractAction Move2Flag(Toroidal2DPhysics space,UUID shipID, AbstractObject obj) {
		//Pre-conditions
		if(obj instanceof Flag) {
			if(!isGoal(((Flag) obj))) {
				if(shipRole.get(shipID).equalsIgnoreCase("flag")) {
					if(obj.getPosition().getX() < 900) {
						baseLeft = false;
					}
					else {
						baseLeft = true;
					}
					
					Ship ship = (Ship) space.getObjectById(shipID);
					AbstractAction action = getAStarPathToGoal(space, ship, obj.getPosition());
					//AbstractAction action = new MoveToObjectAction(space,ship.getPosition(), obj);
					return action;
				}
			}
		}
		return null;
	}
	
	/**
	 * Will deposit the flag or resources at base.
	 * @param shipID
	 * @param obj
	 * @return
	 */
	public static AbstractAction Deposit(Toroidal2DPhysics space, UUID shipID, AbstractObject obj) {
		//Pre-Conditions
		Ship ship = (Ship) space.getObjectById(shipID);
		
			if(shipFlag.get(shipID).booleanValue()) {
				//Deposit Flag
				AbstractAction action = getAStarPathToGoal(space, ship, obj.getPosition());
				//AbstractAction action = new MoveToObjectAction(space,ship.getPosition(),obj);
				shipGoal.remove(shipID, obj);
				return action;
			}
			if(GetNumResources(shipID,space) >= 5000) {
				//Deposit resources
				AbstractAction action = getAStarPathToGoal(space, ship, obj.getPosition());
				//AbstractAction action = new MoveToObjectAction(space,ship.getPosition(),obj);
				shipGoal.remove(shipID, obj);
				return action;
			}
		
		return null;
		
	}

	/**
	 * Move to Base or Asteroid Action
	 * @param shipID
	 * @param obj
	 * @return
	 */
	public AbstractAction Move(UUID shipID, AbstractObject obj, Toroidal2DPhysics space) {
		//Pre-Conditions
		if(obj instanceof Base) {
			if(!isGoal(((Base) obj))) {
				if(shipRole.get(shipID).equalsIgnoreCase("asteroid")) {
					Ship ship = (Ship) space.getObjectById(shipID);
					AbstractAction action = getAStarPathToGoal(space, ship, obj.getPosition());
					//AbstractAction action = new MoveToObjectAction(space,ship.getPosition(), obj);
					return action;
				}
			}
		}
		if(obj instanceof Asteroid) {
			if(!isGoal(((Asteroid) obj))) {
				if(shipRole.get(shipID).equalsIgnoreCase("asteroid")) {
					if(GetNumResources(shipID, space) < 5000) {
						Ship ship = (Ship) space.getObjectById(shipID);
						AbstractAction action = getAStarPathToGoal(space, ship, obj.getPosition());
						//AbstractAction action = new MoveToObjectAction(space,ship.getPosition(), obj);
						return action;
					}

				}
			}
		}
		return null;
	}

	

	//RELATIONS

	/**
	 * Is the ship carrying a flag?
	 * @param shipID
	 * @return
	 */
	public static void HasFlag(UUID shipID, Toroidal2DPhysics space) {
		if(shipRole.get(shipID).equalsIgnoreCase("flag")) {
			Ship ship = (Ship) space.getObjectById(shipID);
			if(ship.isCarryingFlag()) {
				shipFlag.replace(shipID, true);
			}
			else {
				shipFlag.replace(shipID, false);
			}
		}
	}
	/**
	 * Gets the number of resources held by the ship
	 * @param shipID
	 * @return
	 */
	public static int GetNumResources(UUID shipID, Toroidal2DPhysics space) {
		Set<AbstractObject> objects = space.getAllObjects();
		for (AbstractObject aO: objects){
			if(aO.getId() == shipID) {
				Ship ship = (Ship) aO;
				return ship.getResources().getTotal();
			}
			else {
				break;
			}
		}
		return 0;
	}
	/**
	 * Is the ship in transit to the location?
	 * @param shipID
	 * @param location
	 * @return
	 */
	public static boolean InProgress(UUID shipID, Position location, Toroidal2DPhysics space) {
		//TODO: May not need location parameter
		Set<AbstractObject> objects = space.getAllObjects();
		for (AbstractObject aO: objects){
			if(aO.getId() == shipID) {
				Ship ship = (Ship) aO;
				//If the movement is not finished
				if(!ship.getCurrentAction().isMovementFinished(space)) {
					return true;
				}
			}
			else {
				break;
			}
		}
		return false;
	}
	/**
	 * Is the location the goalObj's location?
	 * @param location
	 * @return
	 */
	public static boolean isGoal(AbstractObject obj) {
		if(shipGoal.containsValue(obj)) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public HashMap<UUID, String> getShipRoles()
	{
		return shipRole;
	}
	
	public int getNumFlagShips()
	{
		int count = 0;
		for (String role: shipRole.values())
		{
			if (role.equals("flag"))
				count++;
		}
		return count;
	}
	
	public int getNumAsteroidShips()
	{
		int count = 0;
		for (String role: shipRole.values())
		{
			if (role.equals("asteroid"))
				count++;
		}
		return count;
	}

	/**
	 * Follow an aStar path to the goal
	 * @param space
	 * @param ship
	 * @param goalPosition
	 * @return
	 */
	private static AbstractAction getAStarPathToGoal(Toroidal2DPhysics space, Ship ship, Position goalPosition) {
		AbstractAction newAction;
		
		Graph graph = AStarSearch.createGraphToGoalWithBeacons(space, ship, goalPosition, new Random());
		Vertex[] path = graph.findAStarPath(space);
		FollowPathAction followPathAction = new FollowPathAction(path);
		//followPathAction.followNewPath(path);
		newAction = followPathAction.followPath(space, ship);
		HashMap<UUID, Graph> graphByShip = new HashMap<UUID, Graph>();
		graphByShip.put(ship.getId(), graph);
		return newAction;
	}

}
