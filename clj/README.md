# open-robo firmware — clj/cljc port (ADR-2606280030)

Status: **partial**. This directory holds the babashka-native twin of the **one
pure-logic module** in the open-robo firmware tree. The other 15 python modules
are hardware/ROS2-bound and cannot run on babashka (see "Blocked" below); they
stay as `.py` and remain the deployed code on the robot.

## What this app is

`60-apps/etzhayyim-project-open-robo/` is the **Giemon Otete** open-hardware robot
kit (6-axis arm + crawler). The python under `firmware/` is **embedded robot
firmware for Raspberry Pi 5 + Ubuntu 22.04 + ROS2 Humble** — it is not a server,
CLI, pipeline, or LangGraph app. It drives physical hardware over `rclpy`,
`RPi.GPIO`, `smbus2` (I2C), `picamera2`, and `pyserial` (RS-485 bus servos), with
`numpy`/`scipy` inverse kinematics.

## Ported (verified)

| python | cljc twin | notes |
|---|---|---|
| `firmware/armcrawler/ros2/armcrawler_ros2/urban_mining_core.py` | `src/etzhayyim/open_robo/urban_mining_core.cljc` | pure logic (`math` + dicts): the e-waste inspection → sort-decision rules engine bound to `com.etzhayyim.apps.toshiKozan.registerEwasteStream` |

The port is **faithful to the JSON wire contract**: the ROS2 classifier/sorter
nodes exchange JSON-decoded maps on `std_msgs/String`, so the cljc keeps STRING
keys throughout and reproduces the python output byte-for-byte (verified against
`urban_mining_core.py` across all decision branches, incl. the quaternion values
`0.7071067811865475 / 0.7071067811865476`).

```bash
# from this directory
bb run_tests.clj     # 9 tests / 56 assertions green
bb test              # same, via bb.edn task
```

Pure-stdlib (`clojure.set` + `java.lang.Math` only) — loads on a bare babashka,
no external deps, and does NOT touch the repo-root `bb.edn`.

## Blocked — kept as `.py` (no babashka equivalent / forbidden numerics)

These 15 modules are NOT portable and stay `.py` (they are the live deployed
firmware, launched via `firmware/armcrawler/ros2/launch/*.launch.py`):

| module(s) | hard dependency | why blocked |
|---|---|---|
| `ros2/.../arm_controller_node.py`, `crawler_node.py`, `imu_node.py`, `camera_node.py`, `urban_mining_classifier_node.py`, `urban_mining_sorter_node.py` | `rclpy`, `sensor_msgs`, `geometry_msgs`, `std_msgs` | ROS2 nodes — no babashka ROS2 runtime exists |
| `servo/ics_driver.py`, `tools/servo_scan.py`, `test/home_pose.py` | `pyserial` (KONDO ICS3.5 RS-485 bus) | direct serial hardware I/O on the robot |
| `crawler/motor_driver.py` | `RPi.GPIO` (TB6612, aarch64-only) | GPIO pin actuation |
| `kinematics/ik.py` | `numpy` / `scipy` (damped least-squares Jacobian IK) | heavy numerics — per ADR/task rule, do NOT reimplement numpy/scipy |
| `ros2/launch/*.launch.py`, `ros2/setup.py`, `ros2/.../__init__.py` | ROS2 ament / launch | deploy manifests, not first-party logic |

The single pure-logic module `urban_mining_core.py` has been **deleted** — its
verified `.cljc` twin (`src/etzhayyim/open_robo/urban_mining_core.cljc`) is now
the canonical home for the e-waste inspection → sort-decision logic
(ADR-2606280030, founder directive "twin の py を削除"). The dev-stage ROS2
classifier/sorter nodes still carry an in-repo `import` of the old
`armcrawler_ros2.urban_mining_core`; that python module is intentionally gone, so
the canonical sort-decision logic lives in cljc. `py_removed = 1`
(`urban_mining_core.py`); the 15 hardware/ROS2/numpy-bound modules above stay
`.py` (no babashka equivalent).

## Why this is `partial`, not a full port

There is no StateGraph and no LLM here, and only `urban_mining_core` is
substrate-agnostic logic. The rest is hardware actuation + ROS2 + numpy/scipy,
which the migration ADR explicitly leaves out of scope. The cljc twin makes the
sort-decision logic available off-robot as a kotoba-Datom-log-native fold over
the toshiKozan e-waste stream, without changing what runs on the Raspberry Pi.
