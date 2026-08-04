# Open Robo Urban Mining Publication Manifest

This manifest maps the urban mining automation goal to public artifacts in this repository.

| Goal item | Public artifact | Evidence |
|---|---|---|
| 都市鉱山での廃材回収 | `docs/urban-mining-automation-v1.md` | Cell intake, inspection, handling, sort wall, audit station |
| リサイクル自動化 | `docs/urban-mining-automation-v1.md` | Material streams, bin routing, audit event contract |
| CAD | `cad/urban_mining_cell_v1.scad`, `cad-spec/urban-mining-cell-cad-v1.md` | Parametric OpenSCAD layout plus exchange/fabrication spec |
| USD / UDS world model | `worlds/urban_mining_cell_v1.usda` | USD stage with cell frames, arm base, inspection tunnel, bin fractions |
| Business model | `docs/urban-mining-business-model-v1.md` | Customer, revenue, operating model, unit economics |
| Robotics design | `docs/urban-mining-automation-v1.md` | ROS2 graph, safety gates, deployment path |
| ROS2 implementation | `firmware/armcrawler/ros2/armcrawler_ros2/urban_mining_core.py` | Classifier, sorter command builder, audit event builder |
| ROS2 launch/config | `firmware/armcrawler/ros2/launch/urban_mining.launch.py`, `firmware/armcrawler/ros2/config/urban_mining_params.yaml` | Launchable classifier/sorter nodes with bin target config |
| Public metadata | `PROJECT.jsonld` | Published project capability metadata |

Verification performed:

- Python syntax compile for the new ROS2 files and launch file.
- JSON validation for `PROJECT.jsonld` and the existing `toshiKozan` lexicon.
- Pure Python smoke test for classify -> sort -> audit flow.
