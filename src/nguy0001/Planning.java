package nguy0001;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import spacesettlers.actions.AbstractAction;
import spacesettlers.actions.MoveToObjectAction;
import spacesettlers.objects.AbstractObject;
import spacesettlers.objects.Asteroid;
import spacesettlers.objects.Base;
import spacesettlers.objects.Flag;
import spacesettlers.objects.Ship;
import spacesettlers.simulator.Toroidal2DPhysics;
import spacesettlers.utilities.Position;
/**
 * Planning class used for doling out tasks depending on satisfied pre-conditions
 * @author SpencerBarnes & Anthony Nguyen
 *
 */
public class Planning {


	public static HashMap<UUID, AbstractObject> shipGoal;
	public static HashMap<UUID, String> shipRole;

//	public static UUID shipID;
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
	 * Move to flag action
	 * @param shipID
	 * @param obj
	 * @return
	 */
	public AbstractAction Move2Flag(UUID shipID, AbstractObject obj, Toroidal2DPhysics space) {
		//Pre-conditions
		if(obj instanceof Flag) {
			if(!isGoal(((Flag) obj).getPosition())) {
				if(shipRole.get(shipID).equalsIgnoreCase("flag")) {
//					Ship ship = (Ship) space.getObjectById(shipID);
//					System.out.println(shipID);
//					System.out.println("Ship: " + ship.getEnergy());
//					System.out.println("Flag: " + obj.getPosition());
//					AbstractAction action = new MoveToObjectAction(space,ship.getPosition(), obj);
					
					AbstractObject abst = space.getObjectById(shipID);
					Ship ship = (Ship) abst;

					System.out.println("Obj: " + space.getObjectById(obj.getId()));
					AbstractAction action = new MoveToObjectAction(space,ship.getPosition(), obj);
					return action;
				}
			}
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
			if(!isGoal(((Base) obj).getPosition())) {
				if(shipRole.get(shipID).equalsIgnoreCase("asteroid")) {
					Ship ship = (Ship) space.getObjectById(shipID);
					AbstractAction action = new MoveToObjectAction(space,ship.getPosition(), obj);
					return action;
				}
			}
		}
		if(obj instanceof Asteroid) {
			if(!isGoal(((Asteroid) obj).getPosition())) {
				if(shipRole.get(shipID).equalsIgnoreCase("asteroid")) {
					if(GetNumResources(shipID, space) < 5000) {
						Ship ship = (Ship) space.getObjectById(shipID);
						AbstractAction action = new MoveToObjectAction(space,ship.getPosition(), obj);
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
	public static boolean HasFlag(UUID shipID, Toroidal2DPhysics space) {
		Set<AbstractObject> objects = space.getAllObjects();
		for (AbstractObject aO: objects){
			if(aO.getId() == shipID) {
				Ship ship = (Ship) aO;
				if(ship.isCarryingFlag()) {
					return true;
				}
				else {
					break;
				}
			}
		}
		return false;
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
	public static boolean isGoal(Position location) {
		//TODO: Finish
		return false;
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


}
