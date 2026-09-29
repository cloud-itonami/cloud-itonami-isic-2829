# etzhayyim-project-open-robo

**Giemon** — 日本製オープンハードウェアロボットキットブランド。からくり儀右衛門 (田中久重) に由来。
初代製品: Giemon Otete (6軸アーム + クローラー)。将来: Hitogata, Quad 等に拡張予定。

## Scope

| 成果物 | パス | 状態 |
|---|---|---|
| BOM + 部品調達仕様書 | `bom/BOM-v1.md` | ✅ 作成済 |
| 機械設計仕様 + DH パラメータ | `cad-spec/mechanical-spec-v1.md` | ✅ 作成済 |
| HAT 回路図仕様 (KiCad) | `cad-spec/hat-schematic-spec-v1.md` | ✅ 作成済 |
| 組立説明書 | `docs/assembly-manual-v1.md` | ✅ 作成済 |
| Makuake / Kickstarter 商品文 | `marketing/crowdfunding-copy.md` | ✅ 作成済 |
| Amazon JP 商品ページ骨格 | `marketing/amazon-listing.md` | ✅ 作成済 |
| 公式サイト構成案 | `marketing/website-ia.md` | ✅ 作成済 |
| ICS3.5 サーボドライバー | `firmware/armcrawler/servo/ics_driver.py` | ✅ 作成済 |
| 逆運動学ソルバー | `firmware/armcrawler/kinematics/ik.py` | ✅ 作成済 |
| クローラードライバー | `firmware/armcrawler/crawler/motor_driver.py` | ✅ 作成済 |
| ROS2 ノード群 | `firmware/armcrawler/ros2/` | ✅ 作成済 |
| tsukuru CAD/PCB 設計フロースキーマ | `30-graph/graph-schema/sql_migrations/20260514150000_*` | ✅ 作成済 |
| 都市鉱山ロボティクス自動化設計 | `docs/urban-mining-automation-v1.md` | ✅ 作成済 |
| 都市鉱山 CAD セル仕様 | `cad-spec/urban-mining-cell-cad-v1.md`, `cad/urban_mining_cell_v1.scad` | ✅ 作成済 |
| 都市鉱山 USD world model | `worlds/urban_mining_cell_v1.usda` | ✅ 作成済 |
| 都市鉱山 business model | `docs/urban-mining-business-model-v1.md` | ✅ 作成済 |
| 都市鉱山 ROS2 classifier/sorter | `firmware/armcrawler/ros2/armcrawler_ros2/urban_mining_*_node.py` | ✅ 作成済 |
| 都市鉱山公開マニフェスト | `PUBLICATION.md` | ✅ 作成済 |

## Key Design Decisions

- SBC: Raspberry Pi 5（4GB）— UK設計・英国Sony工場製造。他の全部品は日本調達・日本製造
- アーム: 6軸 + グリッパー、KONDOまたはFutaba RS485バスサーボ
- クローラー: マブチモーター + タミヤTC-01互換トラック機構
- 構造体: ミスミアルミ押出し + Meviyカスタム切削部品
- 制御基板: ローム / 東芝ドライバIC使用、国内基板メーカー製造
- 電源: パナソニック18650セル
- 都市鉱山セル: e-waste 受入、RGB-D/XRF 検査、低信頼度レビュー、Li-ion 隔離、混合 PCB / 銅アルミ / 希土類磁石の自動選別
- 都市鉱山監査: `com.etzhayyim.apps.toshiKozan.registerEwasteStream` に接続する ROS2 audit event を公開

## SSoT

全テーブルデータ・製造パートナーリストは `bom/BOM-v1.md`。
都市鉱山セル設計は `docs/urban-mining-automation-v1.md`。
