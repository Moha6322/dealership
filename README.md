# Car Dealership Management System

Object-oriented Java application managing the inventory, sales and analytics of a vehicle dealership.

## Structure

```
dealership/
├── pom.xml
├── vehicles.txt                      (input data, hash-delimited)
├── outputDealership.txt              (generated report, same text as the console)
└── src/main/java/dealership/
    ├── VehicleInterface.java
    ├── ElectricInterface.java
    ├── Vehicle.java                  (base class, FuelType enum, Comparable<Vehicle>)
    ├── ElectricVehicle.java          (extends Vehicle, implements ElectricInterface)
    └── Dealership.java               (TreeSet<Vehicle>, file I/O, queries, main)
```

## Running

Run `Dealership.main` from IntelliJ with the project root as working directory, or:

```
javac -d out src/main/java/dealership/*.java
java -cp out dealership.Dealership
```

An optional argument selects a different input file. If the input file cannot be read (or contains no valid rows), the built-in sample vehicles are used and a notice goes to stderr.

## Price rules

- Base: `basePrice - age * 150000`, minimum 200000
- Fuel modifier: HYBRID +15%, DIESEL +5%, PETROL unchanged
- ElectricVehicle (always HYBRID): battery >= 60 kWh +10%, heat pump +5%
- The final price is truncated to an `int`

## Design notes

- Vehicles with equal final prices are ordered by name, age, km and base price, so the `TreeSet` never drops a vehicle as a "duplicate".
- The discount simulation removes the vehicle from the `TreeSet` before changing its price and re-adds it afterwards, so the set stays correctly ordered.
