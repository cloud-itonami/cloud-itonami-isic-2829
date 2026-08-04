# Giemon Otete Firmware

Raspberry Pi 5 + Ubuntu 22.04 + ROS2 Humble

## 構成

```
firmware/
├── armcrawler/           # メインPythonパッケージ
│   ├── servo/            # KONDO ICS3.5 バスサーボドライバ
│   ├── crawler/          # クローラーモータードライバ（TB6612）
│   ├── kinematics/       # 逆運動学（IK）エンジン
│   ├── sensors/          # ToF, IMU, Camera
│   └── ros2/             # ROS2ノード群
├── tools/                # ID設定・スキャン・キャリブレーションツール
├── test/                 # ユニットテスト + 実機テスト
├── install.sh            # セットアップスクリプト
└── pyproject.toml
```

## クイックスタート

```bash
git clone https://github.com/etzhayyim/otete-firmware
cd otete-firmware
./install.sh

# サーボ確認
python3 tools/servo_scan.py

# ホームポジション移動
python3 test/home_pose.py

# ROS2ノード起動
ros2 launch armcrawler bringup.launch.py
```
