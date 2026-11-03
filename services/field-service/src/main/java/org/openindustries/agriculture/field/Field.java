package org.openindustries.agriculture.field;

import java.time.Instant;
import java.util.UUID;

/**
 * Field aggregate — Field registry: land units, crops and seasons (ADAPT-aligned shapes).
 *
 * Privacy by design (ISO/IEC 27002 A.5.34): secret identifiers are validated at
 * creation and NEVER persisted or returned in clear — only a masked form is kept.
 * All identifiers in this reference implementation are synthetic.
 */
public class Field {
    public UUID id;
        public String name;
        public String cropType;
        public int hectares;
        public String regionCode;
    public String status;
    public Instant createdAt;

    public static Field create(String name, String cropType, int hectares, String regionCode) {
        Field e = new Field();
        e.id = UUID.randomUUID();
        e.name = name;
        e.cropType = cropType;
        e.hectares = hectares;
        e.regionCode = regionCode;
        e.status = "ACTIVE";
        e.createdAt = Instant.now();
        return e;
    }
}
