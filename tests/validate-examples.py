#!/usr/bin/env python3
"""Contract tests for the core: validate synthetic examples against schemas.

Usage from the repository root: python3 tests/validate-examples.py
Requires: pyyaml, jsonschema.
Exit code 0 only when every example validates.
"""
import json
import sys
from pathlib import Path

import yaml
from jsonschema import Draft202012Validator, RefResolver

ROOT = Path(__file__).resolve().parent.parent

REQUESTS = {
    "organization": "CreateOrganization",
    "establishment": "CreateEstablishment",
    "parcel": "CreateParcel",
    "crop-season": "CreateCropSeason",
    "animal": "CreateAnimal",
    "herd": "CreateHerd",
    "batch": "CreateBatch",
}

EVENTS = {
    "organization-registered": "organization-registered",
    "establishment-registered": "establishment-registered",
    "parcel-registered": "parcel-registered",
    "crop-season-started": "crop-season-started",
    "animal-registered": "animal-registered",
    "herd-formed": "herd-formed",
    "batch-created": "batch-created",
}


def check(label, path, validator, instance):
    errors = sorted(validator.iter_errors(instance), key=lambda e: list(e.path))
    if errors:
        print(f"FAIL {path}:")
        for e in errors:
            where = ".".join(str(p) for p in e.path)
            print(f"  - {where}: {e.message}")
        return 1
    print(f"OK {path}")
    return 0


def main() -> int:
    failures = 0
    openapi = yaml.safe_load((ROOT / "services" / "core" / "openapi.yaml").read_text())
    resolver = RefResolver(base_uri="", referrer=openapi, store={"": openapi})

    for name, schema_name in REQUESTS.items():
        path = ROOT / "examples" / "core" / "requests" / f"{name}.json"
        instance = json.loads(path.read_text())
        validator = Draft202012Validator(
            {"$ref": f"#/components/schemas/{schema_name}"}, resolver=resolver
        )
        failures += check(f"request {name}", path, validator, instance)

    for name, schema_file in EVENTS.items():
        path = ROOT / "examples" / "core" / "events" / f"{name}.json"
        schema = json.loads(
            (ROOT / "contracts" / "cloudevents" / f"{schema_file}.schema.json").read_text()
        )
        instance = json.loads(path.read_text())
        failures += check(f"event {name}", path, Draft202012Validator(schema), instance)

    print(f"{'FAILED' if failures else 'PASSED'}: {failures} failure(s)")
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
