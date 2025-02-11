package frc.robot.util;

import java.util.HashMap;
import java.util.Map;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.DriverStation;

/**
 * Class for a tunable number. Gets value from dashboard in tuning mode, returns default if not or value not in dashboard.
 */
public class TunableNumber {
	private NetworkTable networkTable;
	private final NetworkTableEntry entry;
	private double defaultValue;
	private Map<Integer, Double> lastHasChangedValues = new HashMap<>();
	
	/**
	 * Create a new TunableNumber. Tunable Numbers should only be created in activateShuffleboard() methods.
	 *
	 * @param tab
	 *            The name of the shuffleboard tab to be placed on
	 * @param key
	 *            The name of the field
	 * @param defaultValue
	 *            The default value
	 */
	public TunableNumber(String tab, String key, double defaultValue) {
		NetworkTableInstance inst = NetworkTableInstance.getDefault();
		if (!DriverStation.isFMSAttached()) {
			networkTable = inst.getTable("Shuffleboard/" + tab);
			entry = networkTable.getEntry(key);
			this.defaultValue = defaultValue;
			entry.setDouble(defaultValue);
		}
		else {
			entry = null;
		}
	}
	
	/**
	 * Get the current value from the dashboard
	 *
	 * @return The current value
	 */
	public double get() {
		return !DriverStation.isFMSAttached() ? entry.getDouble(defaultValue) : defaultValue;
	}
	
	/**
	 * Checks whether the number has changed since our last check
	 *
	 * @param id
	 *            Unique identifier for the caller to avoid conflicts when shared between multiple objects. Recommended approach is to pass the result of "hashCode()"
	 * @return True if the number has changed since the last time this method was called, false otherwise.
	 */
	public boolean hasChanged(int id) {
		double currentValue = get();
		Double lastValue = lastHasChangedValues.get(id);
		if (lastValue == null || currentValue != lastValue) {
			lastHasChangedValues.put(id, currentValue);
			return true;
		}
		
		return false;
	}
}
