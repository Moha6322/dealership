package dealership;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.Locale;

public class Vehicle implements VehicleInterface, Comparable<Vehicle> {

    public enum FuelType {
        PETROL, DIESEL, HYBRID
    }

    private static final int AGE_DEPRECIATION = 150000;
    private static final int MINIMUM_PRICE = 200000;

    private static final Comparator<Vehicle> NATURAL_ORDER =
            Comparator.comparingInt(Vehicle::getFinalPrice)
                    .thenComparing(Vehicle::getFullName)
                    .thenComparingInt(Vehicle::getAge)
                    .thenComparingDouble(Vehicle::getKm)
                    .thenComparingDouble(Vehicle::getBasePrice);

    private String brand;
    private String model;
    private double basePrice;
    private int age;
    private double km;
    private FuelType fuelType;

    public Vehicle(String brand, String model, double basePrice, int age, double km, FuelType fuelType) {
        setBrand(brand);
        setModel(model);
        setBasePrice(basePrice);
        setAge(age);
        setKm(km);
        setFuelType(fuelType);
    }

    public String getBrand() {
        return brand;
    }

    public final void setBrand(String brand) {
        if (brand == null || brand.isBlank()) {
            throw new IllegalArgumentException("Brand must not be empty");
        }
        this.brand = brand.trim();
    }

    public String getModel() {
        return model;
    }

    public final void setModel(String model) {
        this.model = model == null ? "" : model.trim();
    }

    public String getFullName() {
        return model.isEmpty() ? brand : brand + " " + model;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public final void setBasePrice(double basePrice) {
        if (basePrice < 0) {
            throw new IllegalArgumentException("Base price must not be negative");
        }
        this.basePrice = basePrice;
    }

    public int getAge() {
        return age;
    }

    public final void setAge(int age) {
        if (age < 0) {
            throw new IllegalArgumentException("Age must not be negative");
        }
        this.age = age;
    }

    public double getKm() {
        return km;
    }

    public final void setKm(double km) {
        if (km < 0) {
            throw new IllegalArgumentException("Kilometers must not be negative");
        }
        this.km = km;
    }

    public FuelType getFuelType() {
        return fuelType;
    }

    public final void setFuelType(FuelType fuelType) {
        if (fuelType == null) {
            throw new IllegalArgumentException("Fuel type must not be null");
        }
        this.fuelType = fuelType;
    }

    @Override
    public void applyDiscount(int percentage) {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("Discount must be between 0 and 100 percent");
        }
        basePrice = basePrice * (100 - percentage) / 100.0;
    }

    @Override
    public int getFinalPrice() {
        double depreciated = Math.max(basePrice - (double) age * AGE_DEPRECIATION, MINIMUM_PRICE);
        BigDecimal price = BigDecimal.valueOf(depreciated).multiply(fuelMultiplier());
        return price.setScale(0, RoundingMode.DOWN).intValue();
    }

    private BigDecimal fuelMultiplier() {
        return switch (fuelType) {
            case HYBRID -> new BigDecimal("1.15");
            case DIESEL -> new BigDecimal("1.05");
            case PETROL -> BigDecimal.ONE;
        };
    }

    @Override
    public double averageKmPerYear() {
        return age == 0 ? km : km / age;
    }

    @Override
    public int compareTo(Vehicle other) {
        return NATURAL_ORDER.compare(this, other);
    }

    @Override
    public String toString() {
        return String.format(Locale.US,
                "%s | %s | age: %d | km: %.1f | base price: %.2f | final price: %d | avg km/year: %.2f",
                getFullName(), fuelType, age, km, basePrice, getFinalPrice(), averageKmPerYear());
    }
}
