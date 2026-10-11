package dealership;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ElectricVehicle extends Vehicle implements ElectricInterface {

    private static final int LARGE_BATTERY_KWH = 60;
    private static final int RANGE_CATEGORY_TOLERANCE_KWH = 5;
    private static final int BATTERY_COST_PER_KWH = 25000;

    private int batteryCapacity;
    private boolean hasHeatPump;

    public ElectricVehicle(String brand, String model, double basePrice, int age, double km,
                           int batteryCapacity, boolean hasHeatPump) {
        super(brand, model, basePrice, age, km, FuelType.HYBRID);
        setBatteryCapacity(batteryCapacity);
        this.hasHeatPump = hasHeatPump;
    }

    public int getBatteryCapacity() {
        return batteryCapacity;
    }

    public final void setBatteryCapacity(int batteryCapacity) {
        if (batteryCapacity < 0) {
            throw new IllegalArgumentException("Battery capacity must not be negative");
        }
        this.batteryCapacity = batteryCapacity;
    }

    public boolean hasHeatPump() {
        return hasHeatPump;
    }

    public void setHasHeatPump(boolean hasHeatPump) {
        this.hasHeatPump = hasHeatPump;
    }

    @Override
    public int getFinalPrice() {
        BigDecimal price = BigDecimal.valueOf(super.getFinalPrice());
        if (batteryCapacity >= LARGE_BATTERY_KWH) {
            price = price.multiply(new BigDecimal("1.10"));
        }
        if (hasHeatPump) {
            price = price.multiply(new BigDecimal("1.05"));
        }
        return price.setScale(0, RoundingMode.DOWN).intValue();
    }

    @Override
    public boolean hasSameRangeCategory(Vehicle other) {
        if (!(other instanceof ElectricVehicle)) {
            return false;
        }
        int difference = Math.abs(batteryCapacity - ((ElectricVehicle) other).batteryCapacity);
        return difference <= RANGE_CATEGORY_TOLERANCE_KWH;
    }

    @Override
    public int batteryCostShare() {
        return batteryCapacity * BATTERY_COST_PER_KWH;
    }

    @Override
    public String toString() {
        return super.toString() + String.format(" | battery: %d kWh | heat pump: %s",
                batteryCapacity, hasHeatPump ? "yes" : "no");
    }
}
