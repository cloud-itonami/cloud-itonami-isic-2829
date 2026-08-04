# Urban Mining Robotics Business Model v1

## Offer

Open hardware and software for robotic e-waste recovery cells. The product is not a black-box recycler; it is a reproducible cell design with CAD, USD world model, ROS2 code, BOM, deployment runbook, and public audit contracts.

## Customers

| Segment | Need | Entry Product |
|---|---|---|
| Municipal recycling centers | Labor shortage, fire risk from batteries, public reporting | One inspection + sorting cell |
| Refurbishers | Separate reusable parts before shredding | Disassembly bench + audit API |
| Electronics retailers | Take-back compliance and traceability | Compact intake cell |
| Schools and labs | Circular economy robotics training | Simulator + tabletop cell |

## Revenue

1. **Hardware kit margin**: sell certified kits and spares while keeping design files public.
2. **Deployment service**: site survey, safety review, calibration, and operator training.
3. **Maintenance subscription**: preventive maintenance, replacement grippers, sensor calibration, and ROS2 release support.
4. **Audit SaaS**: hosted dashboards for WEEE, battery, and critical mineral recovery reporting.
5. **Data cooperative**: optional anonymized recovery-yield benchmarks shared back to participating facilities.

No ads are required. Public-good deployments can be donation-funded or grant-funded.

## Unit Economics Target

| Metric | Pilot Target |
|---|---:|
| Cell capex | JPY 4.8M to 9.8M |
| Throughput | 60 to 180 kg/day mixed small e-waste |
| Labor reduction | 40% to 70% on intake and primary sorting |
| Battery incident reduction | 80% by early isolation |
| Payback | 18 to 36 months depending on labor and recovered material prices |

## Operating Model

- The facility owns the waste stream and recovery contracts.
- The robot cell produces auditable material flow events.
- High-value fractions are sold to certified refiners.
- Hazardous fractions are routed to licensed processors.
- The software stack remains forkable so municipalities can avoid vendor lock-in.

## Moat

- Open CAD + ROS2 lowers adoption friction.
- Public `toshiKozan` data contracts make audit and recovery claims portable.
- USD world models allow simulation, digital twin validation, and third-party cell layout extensions.
- Safety policy is explicit and inspectable instead of hidden in proprietary PLC code.
