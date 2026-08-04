#!/usr/bin/env python3
"""Scan ICS3.5 bus and print all responding servo IDs."""

import sys
sys.path.insert(0, str(__import__("pathlib").Path(__file__).resolve().parents[1]))

from armcrawler.servo.ics_driver import ICSBusDriver


def main() -> None:
    driver = ICSBusDriver()
    driver.open()
    print("Scanning ICS3.5 bus (ID 1..31)...")
    found = driver.scan()
    driver.close()

    if found:
        print(f"Found servo IDs: {found}")
        if sorted(found) == [1, 2, 3, 4, 5, 6]:
            print("All 6 arm servos detected.")
        else:
            missing = set(range(1, 7)) - set(found)
            if missing:
                print(f"WARNING: Missing expected IDs: {sorted(missing)}")
    else:
        print("No servos found. Check power and cable connections.")
        sys.exit(1)


if __name__ == "__main__":
    main()
