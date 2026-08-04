# Urban Mining Cell CAD Specification v1

## Deliverables

| File Type | Purpose |
|---|---|
| STEP AP214 | Mechanical exchange with fabricators |
| DXF | Sheet metal, acrylic guard, signage, bin dividers |
| KiCad | Sensor/IO HAT and safety relay board |
| USD / USDA | Simulation and world model handoff |
| STL | Non-load-bearing printed fixtures and gripper fingers |

## Envelope

- Cell footprint: 6000 mm x 3000 mm.
- Guard height: 2200 mm.
- Robot lane: 900 mm minimum service corridor.
- Inbound tote: 600 mm x 400 mm Euro container compatible.
- Sort bin pitch: 420 mm center-to-center.
- Maximum item mass for arm pick: 1.2 kg.

## Coordinate Frames

| Frame | Origin | Notes |
|---|---|---|
| `world` | Front-left floor datum | Matches USD stage |
| `cell_base` | Guarded cell center | Static transform from `world` |
| `inspection_tunnel` | Camera optical rail midpoint | RGB-D and XRF references |
| `arm_base` | ArmCrawler base link | Existing robot stack |
| `sort_wall` | Bin wall lower-left | Bin target poses derive from this |

All CAD dimensions are millimeters. ROS2 transforms publish meters.

## Major Assemblies

1. **UMC-100 frame**: Misumi 40 mm aluminum extrusion, guarded with clear polycarbonate.
2. **UMC-200 conveyor/tote deck**: low-speed belt, reversible, 24 V DC.
3. **UMC-300 inspection tunnel**: RGB-D camera, lighting bar, scale, optional XRF interlock bay.
4. **UMC-400 robot handling**: ArmCrawler base, quick-change gripper, low-force compliance pad.
5. **UMC-500 sort wall**: seven locked bins with color-coded doors and fill sensors.
6. **UMC-600 safety system**: E-stop loop, door interlock, battery isolation fire box.

## Sort Bins

| Bin ID | Fraction | Default Target Pose |
|---|---|---|
| `manual_review` | Unknown / low confidence | `bin_manual_review` |
| `li_ion_isolation` | Li-ion battery or battery-visible device | `bin_li_ion_isolation` |
| `mixed_pcb` | PCB and PCB-bearing assemblies | `bin_mixed_pcb` |
| `copper_aluminum` | Harness, heat sink, motor, cable | `bin_copper_aluminum` |
| `ferrous` | Steel chassis and screws | `bin_ferrous` |
| `rare_earth_magnet` | Speaker, HDD, motor magnet | `bin_rare_earth_magnet` |
| `reject` | Non-processable waste | `bin_reject` |

## Fabrication Notes

- Put battery isolation behind a fire-rated liner and independent smoke sensor.
- Use rounded gripper fingers and current limits for consumer devices.
- Mount all high-voltage or XRF equipment outside normal robot reach unless the interlock is closed.
- Keep bin coordinates configurable in ROS2 YAML; CAD positions are the default, not the runtime authority.
