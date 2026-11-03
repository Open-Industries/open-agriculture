package org.openindustries.agriculture.machine;

import java.util.UUID;

/** Machine aggregate — Fleet registry: tractors, harvesters, drones and pumps. */
public class Machine {
    public UUID id;
        public String name;
        public String machineType;
        public int powerKw;
    public String status;

    public static Machine create(String name, String machineType, int powerKw) {
        Machine e = new Machine();
        e.id = UUID.randomUUID();
        e.name = name;
        e.machineType = machineType;
        e.powerKw = powerKw;
        e.status = "AVAILABLE";
        return e;
    }
}
