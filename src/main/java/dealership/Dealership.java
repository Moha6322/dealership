package dealership;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

public class Dealership {

    private static final String INPUT_FILE = "vehicles.txt";
    private static final String OUTPUT_FILE = "outputDealership.txt";
    private static final int YOUNG_AGE_LIMIT = 5;
    private static final int SIMULATED_DISCOUNT = 10;

    private final TreeSet<Vehicle> vehicles = new TreeSet<>();

    public TreeSet<Vehicle> getVehicles() {
        return vehicles;
    }

    public void loadData(String path) {
        if (!loadFromFile(path)) {
            System.err.println("Could not read '" + path + "', using built-in sample data instead.");
            loadDefaults();
        }
    }

    public boolean loadFromFile(String path) {
        try {
            List<String> lines = Files.readAllLines(Path.of(path), StandardCharsets.UTF_8);
            lines.forEach(this::addFromLine);
            return !vehicles.isEmpty();
        } catch (IOException e) {
            return false;
        }
    }

    private void addFromLine(String line) {
        String clean = line.replace("\uFEFF", "").trim();
        if (clean.isEmpty()) {
            return;
        }
        try {
            vehicles.add(parseVehicle(clean.split("#")));
        } catch (IllegalArgumentException e) {
            System.err.println("Skipping invalid line: " + clean);
        }
    }

    private static Vehicle parseVehicle(String[] f) {
        String type = f[0].trim().toUpperCase();
        int requiredFields = type.equals("ELECTRIC") ? 8 : 6;
        if (!type.equals("VEHICLE") && !type.equals("ELECTRIC")) {
            throw new IllegalArgumentException("Unknown row type: " + type);
        }
        if (f.length < requiredFields) {
            throw new IllegalArgumentException("Too few fields");
        }
        String[] name = splitName(f[1].trim());
        double basePrice = Double.parseDouble(f[2].trim());
        int age = Integer.parseInt(f[3].trim());
        double km = Double.parseDouble(f[4].trim());
        Vehicle.FuelType fuel = Vehicle.FuelType.valueOf(f[5].trim().toUpperCase());
        if (type.equals("VEHICLE")) {
            return new Vehicle(name[0], name[1], basePrice, age, km, fuel);
        }
        return new ElectricVehicle(name[0], name[1], basePrice, age, km,
                Integer.parseInt(f[6].trim()), parseYesNo(f[7].trim()));
    }

    private static String[] splitName(String fullName) {
        int space = fullName.indexOf(' ');
        if (space < 0) {
            return new String[]{fullName, ""};
        }
        return new String[]{fullName.substring(0, space), fullName.substring(space + 1).trim()};
    }

    private static boolean parseYesNo(String value) {
        if (value.equalsIgnoreCase("yes")) {
            return true;
        }
        if (value.equalsIgnoreCase("no")) {
            return false;
        }
        throw new IllegalArgumentException("Expected yes/no but got: " + value);
    }

    public void loadDefaults() {
        vehicles.add(new Vehicle("Toyota", "Corolla", 5500000, 4, 75000, Vehicle.FuelType.HYBRID));
        vehicles.add(new Vehicle("Volkswagen", "Golf", 4200000, 6, 120000, Vehicle.FuelType.DIESEL));
        vehicles.add(new Vehicle("Ford", "Focus", 3100000, 8, 145000, Vehicle.FuelType.PETROL));
        vehicles.add(new Vehicle("BMW", "320d", 7800000, 3, 60000, Vehicle.FuelType.DIESEL));
        vehicles.add(new Vehicle("Opel", "Astra", 2200000, 11, 190000, Vehicle.FuelType.PETROL));
        vehicles.add(new ElectricVehicle("Nissan", "Leaf", 4800000, 5, 65000, 40, false));
        vehicles.add(new ElectricVehicle("Tesla", "Model 3", 11500000, 2, 38000, 75, true));
        vehicles.add(new ElectricVehicle("Renault", "Zoe", 3900000, 6, 82000, 52, true));
        vehicles.add(new ElectricVehicle("Kia", "EV6", 13200000, 1, 15000, 77, true));
        vehicles.add(new ElectricVehicle("VW", "ID.3", 7100000, 4, 54000, 58, false));
    }

    public double averageKm() {
        return vehicles.stream().mapToDouble(Vehicle::getKm).average().orElse(0);
    }

    public Vehicle cheapest() {
        return vehicles.first();
    }

    public ElectricVehicle topElectric() {
        for (Vehicle v : vehicles.descendingSet()) {
            if (v instanceof ElectricVehicle) {
                return (ElectricVehicle) v;
            }
        }
        return null;
    }

    public long totalValue() {
        return vehicles.stream().mapToLong(Vehicle::getFinalPrice).sum();
    }

    public double averagePrice() {
        return vehicles.stream().mapToInt(Vehicle::getFinalPrice).average().orElse(0);
    }

    public List<Vehicle> hybridsAtOrBelowAverage() {
        double average = averagePrice();
        return vehicles.stream()
                .filter(v -> v.getFuelType() == Vehicle.FuelType.HYBRID)
                .filter(v -> v.getFinalPrice() <= average)
                .toList();
    }

    public long countYoung() {
        return vehicles.stream().filter(v -> v.getAge() < YOUNG_AGE_LIMIT).count();
    }

    public int discountAndGetPrice(Vehicle vehicle, int percentage) {
        vehicles.remove(vehicle);
        vehicle.applyDiscount(percentage);
        vehicles.add(vehicle);
        return vehicle.getFinalPrice();
    }

    public String buildReport() {
        StringBuilder out = new StringBuilder();
        out.append(String.format(Locale.US, "1. Fleet average mileage: %.2f km%n", averageKm()));
        out.append(String.format("2. Cheapest vehicle: %s%n", cheapest()));
        out.append(topElectricLine());
        out.append(String.format("4. Total fleet inventory value: %d%n", totalValue()));
        out.append(hybridLines());
        out.append(String.format("6. Vehicles younger than %d years: %d%n", YOUNG_AGE_LIMIT, countYoung()));
        out.append(discountLine());
        return out.toString();
    }

    private String topElectricLine() {
        ElectricVehicle top = topElectric();
        if (top == null) {
            return String.format("3. Top electric vehicle: none in stock%n");
        }
        return String.format(Locale.US, "3. Top electric vehicle: %s | battery capacity: %d kWh | average annual km: %.2f%n",
                top.getFullName(), top.getBatteryCapacity(), top.averageKmPerYear());
    }

    private String hybridLines() {
        StringBuilder out = new StringBuilder();
        out.append(String.format(Locale.US,
                "5. Hybrid vehicles priced at or below the fleet average (%.2f):%n", averagePrice()));
        for (Vehicle v : hybridsAtOrBelowAverage()) {
            out.append(String.format("   - %s%n", v));
        }
        return out.toString();
    }

    private String discountLine() {
        Vehicle selected = cheapest();
        int before = selected.getFinalPrice();
        int after = discountAndGetPrice(selected, SIMULATED_DISCOUNT);
        return String.format("7. Discount simulation (%d%% off %s): final price %d -> %d%n",
                SIMULATED_DISCOUNT, selected.getFullName(), before, after);
    }

    public static void writeReport(String report, String path) {
        try {
            Files.writeString(Path.of(path), report, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Could not write '" + path + "': " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Dealership dealership = new Dealership();
        dealership.loadData(args.length > 0 ? args[0] : INPUT_FILE);
        String report = dealership.buildReport();
        System.out.print(report);
        writeReport(report, OUTPUT_FILE);
    }
}
