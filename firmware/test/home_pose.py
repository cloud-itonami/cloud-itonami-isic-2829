#!/usr/bin/env python3
"""Move all arm servos to home position (all joints 0 degrees)."""

import sys
import time
sys.path.insert(0, str(__import__("pathlib").Path(__file__).resolve().parents[1]))

from armcrawler.servo.ics_driver import ICSBusDriver

HOME_ANGLES_DEG = [0.0, 45.0, 90.0, 0.0, 0.0, 0.0]  # J1..J6


def main() -> None:
    driver = ICSBusDriver()
    driver.open()

    print("Moving to home position...")
    for servo_id, angle in enumerate(HOME_ANGLES_DEG, start=1):
        ics_pos = ICSBusDriver.deg_to_ics(angle)
        result = driver.set_position(servo_id, ics_pos)
        status = f"OK (echo={result})" if result else "NO RESPONSE"
        print(f"  J{servo_id}: {angle}° → ICS {ics_pos} — {status}")
        time.sleep(0.05)

    time.sleep(1.0)
    driver.close()
    print("Done.")


if __name__ == "__main__":
    main()
