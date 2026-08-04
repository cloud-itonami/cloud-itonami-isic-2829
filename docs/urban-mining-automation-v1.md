# Urban Mining Robotics Automation v1

**Scope**: 都市鉱山で発生する e-waste / 廃材を、受入、認識、分解、選別、回収、監査までロボティクスで自動化するための公開設計。

## Success Criteria

| Requirement | Artifact |
|---|---|
| CAD | `cad-spec/urban-mining-cell-cad-v1.md` |
| USD / UDS world model | `worlds/urban_mining_cell_v1.usda` |
| Business model | `docs/urban-mining-business-model-v1.md` |
| Robotics architecture | This document |
| ROS2 implementation | `firmware/armcrawler/ros2/armcrawler_ros2/urban_mining_*_node.py` |
| Public project metadata | `PROJECT.jsonld` |

## Cell Layout

The first deployable unit is a 6 m x 3 m semi-enclosed sorting cell:

1. **Inbound tote station** accepts mixed small e-waste: phones, PCs, server boards, Li-ion packs, mixed PCB, rare-earth magnets.
2. **Vision inspection tunnel** captures RGB-D, barcode, weight, and optional XRF measurements.
3. **ArmCrawler handling lane** moves bins and performs low-force pick-and-place.
4. **Disassembly bench** supports screw removal, lid opening, battery isolation, and PCB extraction.
5. **Material sort wall** routes items into locked recovery bins for Cu, Al, steel, Li-ion battery, mixed PCB, rare-earth magnet, and reject.
6. **Audit station** writes every stream event to `com.etzhayyim.apps.toshiKozan.*` records.

## ROS2 Graph

| Node | Responsibility | Key Topics |
|---|---|---|
| `urban_mining_classifier` | Turns sensor observations into material class, confidence, hazard flags, and destination bin | subscribes `/urban_mining/inspection`, publishes `/urban_mining/classification` |
| `urban_mining_sorter` | Converts classification into robot-safe sort commands and audit events | subscribes `/urban_mining/classification`, publishes `/arm/cartesian_target`, `/urban_mining/sort_command`, `/urban_mining/audit_event` |
| `arm_controller` | Existing 6-DOF arm execution | subscribes `/arm/cartesian_target`, `/arm/joint_trajectory` |
| `crawler` | Existing mobile base control | subscribes `/cmd_vel` |

The implementation uses JSON over `std_msgs/String` at the project boundary so it can run before custom ROS interfaces are stabilized. Internal production deployments can replace these with typed IDL messages without changing the business process.

## Safety Gates

- Battery-like objects are routed only to the isolated Li-ion bin unless an operator override is recorded.
- Low-confidence classification goes to `manual_review`.
- The sorter publishes only pre-approved Cartesian target poses from config, not arbitrary model-generated coordinates.
- A failed arm or crawler status blocks new sort commands.
- Every recovery action emits an audit event with stream type, target materials, confidence, destination, and policy decision.

## Data Contract

Inspection input:

```json
{
  "item_id": "urn:uuid:...",
  "mass_g": 142.5,
  "labels": ["smartphone", "battery-visible"],
  "barcode": "optional",
  "xrf": {"cu": 0.18, "au_ppm": 280},
  "rgbd_ref": "b2://bucket/frame-set"
}
```

Classification output:

```json
{
  "item_id": "urn:uuid:...",
  "stream_type": "smartphone",
  "target_materials": ["au", "ag", "cu", "li", "co"],
  "destination_bin": "li_ion_isolation",
  "confidence": 0.91,
  "hazards": ["battery"],
  "policy": "isolate_battery_first"
}
```

Audit event:

```json
{
  "event_type": "sort_commanded",
  "item_id": "urn:uuid:...",
  "stream_type": "smartphone",
  "destination_bin": "li_ion_isolation",
  "toshi_kozan_lexicon": "com.etzhayyim.apps.toshiKozan.registerEwasteStream"
}
```

## Deployment Path

1. Build the ROS2 package under Ubuntu 22.04 / ROS2 Humble.
2. Launch `ros2 launch armcrawler_ros2 urban_mining.launch.py`.
3. Feed inspection events from the camera/XRF pipeline into `/urban_mining/inspection`.
4. Use `/urban_mining/audit_event` to write to the toshiKozan records and downstream recovery ledgers.
5. Publish CAD, USD world model, BOM deltas, and firmware in the same GitHub release.
