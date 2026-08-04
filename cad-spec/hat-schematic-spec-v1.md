# Otete HAT — KiCad 回路図仕様書 v1

Revision: 2026-05-14
基板名: Otete HAT
形状: Raspberry Pi HAT (65 × 56.5 mm)
製造: P-Ban.com 2層FR4 1.6mm HASL
KiCad バージョン: 8.x

---

## 1. 機能ブロック概要

```
┌──────────────────────────────────────────────────────┐
│  Raspberry Pi 5 (4GB) [GPIO 40P]                     │
│  J1: 40-pin HAT header (2.54mm 2×20)                 │
└───────────┬──────────────────────────────────────────┘
            │
   ┌────────┴──────────────────────────────────────┐
   │              Otete HAT                    │
   │                                               │
   │  ┌─────────────────┐  ┌──────────────────┐   │
   │  │  ICS3.5 IF      │  │  RS485 × 2ch     │   │
   │  │  (Half-duplex   │  │  (Futaba RS485   │   │
   │  │   UART, 115200) │  │   Alternative)   │   │
   │  │  Q1 74AHCT1G125 │  │  U3/U4 MAX3485E  │   │
   │  └────────┬────────┘  └────────┬─────────┘   │
   │           │ CN1 (ICS bus 4P)   │ CN2/CN3      │
   │                                              │
   │  ┌─────────────────┐  ┌──────────────────┐   │
   │  │  Motor Driver   │  │  Motor Driver    │   │
   │  │  U1 TB6612FNG   │  │  U2 TB6612FNG    │   │
   │  │  (Left crawler) │  │  (Right crawler) │   │
   │  └────────┬────────┘  └────────┬─────────┘   │
   │           │ CN4 (L motor 2P)   │ CN5 (R motor)│
   │                                              │
   │  ┌─────────────────┐  ┌──────────────────┐   │
   │  │  IMU             │  │  ToF Sensor      │   │
   │  │  U5 ICM-42688-P  │  │  U6 VL53L4CX    │   │
   │  │  SPI1 (CE0)      │  │  I2C-1 (0x29)   │   │
   │  └─────────────────┘  └──────────────────┘   │
   │                                               │
   │  ┌────────────────────────────────────────┐   │
   │  │  Power Section                         │   │
   │  │  J2: DC barrel (5.5/2.1mm)             │   │
   │  │  U7: TDK-Lambda CCG3-24-05S (7.4→5V)  │   │
   │  │  U8: μPD78F0513 Nコン EMC filter       │   │
   │  │  5V rail → RPi via J1 pin2/4           │   │
   │  │  7.4V rail → CN1 (servo bus direct)   │   │
   │  │  12V rail → CN4/CN5 (motor direct)    │   │
   │  └────────────────────────────────────────┘   │
   └───────────────────────────────────────────────┘
```

---

## 2. GPIO ピンアサイン表 (RPi 40-pin)

| GPIO (BCM) | 物理ピン | 機能 | 方向 | 接続先 |
|---|---|---|---|---|
| 2 | 3 | I2C1_SDA | I/O | U6 VL53L4CX SDA |
| 3 | 5 | I2C1_SCL | O | U6 VL53L4CX SCL |
| 7 | 26 | SPI0_CE1_N | O | (予備 CE) |
| 8 | 24 | SPI0_CE0_N | O | U5 ICM-42688-P CS |
| 9 | 21 | SPI0_MISO | I | U5 MISO |
| 10 | 19 | SPI0_MOSI | O | U5 MOSI |
| 11 | 23 | SPI0_SCLK | O | U5 SCLK |
| 12 | 32 | PWM0 | O | U1 PWMA (左クローラー) |
| 13 | 33 | PWM1 | O | U2 PWMB (右クローラー) |
| 14 | 8 | UART0_TXD | O | Q1 ICS3.5 TX |
| 15 | 10 | UART0_RXD | I | Q1 ICS3.5 RX (half-dup) |
| 16 | 36 | GPIO16 | O | U1/U2 STBY (共通) |
| 17 | 11 | GPIO17 | O | Q1 TX_EN (half-duplex 切替) |
| 20 | 38 | GPIO20 | O | U1 AIN1 (左前進) |
| 21 | 40 | GPIO21 | O | U1 AIN2 (左後退) |
| 24 | 18 | GPIO24 | O | U2 BIN1 (右前進) |
| 25 | 22 | GPIO25 | O | U2 BIN2 (右後退) |
| 4 | 7 | GPIO4 | O | U6 XSHUT (ToF reset) |
| 27 | 13 | GPIO27 | I | U5 INT (IMU 割り込み) |
| 22 | 15 | GPIO22 | O | U3 DE/~RE (RS485-A Dir) |
| 23 | 16 | GPIO23 | O | U4 DE/~RE (RS485-B Dir) |
| — | 2,4 | +5V | — | 5V rail 入力 (U7出力) |
| — | 6,9,14,20,25,30,34,39 | GND | — | 共通 GND |

---

## 3. 回路ブロック詳細

### 3.1 ICS3.5 半二重 UART インターフェース

**目的**: KONDO KRS-3204 ICS バスサーボへの半二重 UART 通信
**プロトコル**: 115200 bps, 8N1, 半二重 (送受共用 1線)

**回路構成**:
```
GPIO17 (TX_EN) ──→ Q1.OE (74AHCT1G125 Buffer Enable)
GPIO14 (TXD) ────→ Q1.A (Buffer Input)
                   Q1.Y ──┬── R1 (100Ω) ── CN1.Data
GPIO15 (RXD) ←────────────┘
                           D1 (BAT54S Schottky, 5V clamp)
CN1 ピン配置 (JST PH 4P):
  1: +7.4V
  2: GND
  3: Data (half-duplex)
  4: (n.c.)
```

**部品**:
| Reference | 部品 | メーカー | 発注先 |
|---|---|---|---|
| Q1 | 74AHCT1G125GW | NXP | 秋月 |
| R1 | 100Ω 0402 5% | ローム MCR03 | Mouser |
| D1 | BAT54S SOT-23 | ローム | Mouser |
| C1 | 100nF 0402 X5R | TDK CGA2B3X5R | Mouser |
| CN1 | S4B-PH-SM4-TB | JST | 秋月 |

**制御シーケンス** (firmware/armcrawler/servo/ics_driver.py 参照):
1. `GPIO.output(TX_EN, HIGH)` → バッファ有効化、送信モード
2. UART.write(3 byte ICS frame)
3. `GPIO.output(TX_EN, LOW)` → 受信モード (最小ターンアラウンド: 1ms)
4. UART.read(3 bytes) with 10ms timeout

---

### 3.2 RS485 デュアルチャンネル (代替サーボバス)

**目的**: Futaba RS485 対応サーボの代替インターフェース (ICS 使用時は未使用)
**IC**: ROHM BA8481A-ME2 または Maxim MAX3485EESA+ (3.3V 動作, 半二重)

**回路構成**:
```
GPIO22 (DE/~RE_A) → U3.DE, ~U3.RE
GPIO14 (TXD_A)   → U3.DI
U3.RO            → GPIO15 (RXD_A)  [UART0]
U3.A ──── R5 120Ω ──── CN2.RS485+
U3.B              ──── CN2.RS485-

GPIO23 (DE/~RE_B) → U4.DE, ~U4.RE
GPIO0  (TXD_B)   → U4.DI  [UART2, alt function]
U4.RO            → GPIO1  (RXD_B)
U4.A ──── R6 120Ω ──── CN3.RS485+
U4.B              ──── CN3.RS485-
```

**CN2/CN3 ピン配置** (JST PH 4P):
```
1: +7.4V
2: RS485+
3: RS485-
4: GND
```

**部品**:
| Reference | 部品 | メーカー | 規格 |
|---|---|---|---|
| U3,U4 | MAX3485EESA+ | Maxim | SOIC-8, 3.3V, 10Mbps |
| R5,R6 | 120Ω 0402 1% | ローム MCR03 | 終端抵抗 |
| C4,C5 | 100nF 0402 | TDK CGA2 | バイパス |
| CN2,CN3 | S4B-PH-SM4-TB | JST | — |

---

### 3.3 モータードライバー (TB6612FNG × 2)

**目的**: 左右クローラー DC モーター独立 H ブリッジ制御
**IC**: 東芝 TB6612FNG (SSOP-24, VM最大15V, 1.2A連続/3.2Aピーク/ch)

**U1 (左クローラー) 回路**:
```
GPIO12 (PWM0)  → U1.PWMA
GPIO20 (AIN1)  → U1.AIN1
GPIO21 (AIN2)  → U1.AIN2
GPIO16 (STBY)  → U1.STBY (U2と共通)
U1.VM  ← +12V (or +7.4V)
U1.VCC ← +3.3V
U1.AO1 → CN4.Motor_A+
U1.AO2 → CN4.Motor_A-
```

**U2 (右クローラー) 回路**:
```
GPIO13 (PWM1)  → U2.PWMB
GPIO24 (BIN1)  → U2.BIN1
GPIO25 (BIN2)  → U2.BIN2
GPIO16 (STBY)  → U2.STBY
U2.VM  ← +12V (or +7.4V)
U2.VCC ← +3.3V
U2.BO1 → CN5.Motor_B+
U2.BO2 → CN5.Motor_B-
```

**デカップリング配置** (レイアウト重要):
- C6,C7 (100μF 16V 電解): U1.VM直近 (<5mm)
- C8,C9 (100nF 0402): U1.VCC
- C10,C11 (100μF): U2.VM直近
- C12,C13 (100nF): U2.VCC
- D2,D3 (RB521S-30 ショットキー × 2): フライバック保護

**CN4/CN5** (JST VH 2P 3.96mm ピッチ — モーター電流 2A 以上対応):
```
1: Motor+
2: Motor-
```

**部品**:
| Reference | 部品 | メーカー | 備考 |
|---|---|---|---|
| U1,U2 | TB6612FNG | 東芝 | SSOP-24 |
| C6-C11 | 100μF 16V | ニチコン UWT1C101MDD | SM電解 |
| C12-C15 | 100nF 0402 | TDK CGA2 | バイパス |
| D2,D3 | RB521S-30T2R | ローム | SOD-323, 30V 200mA |
| CN4,CN5 | VHR-2N | JST | 2P VH 3.96mm |

---

### 3.4 IMU (ICM-42688-P)

**目的**: 6軸慣性計測 (3軸ジャイロ + 3軸加速度), 走行制御・姿勢推定
**IC**: TDK InvenSense ICM-42688-P (LGA-14, SPI/I2C)
**インターフェース**: SPI0 (モード3: CPOL=1 CPHA=1), 最大24MHz

**回路**:
```
SPI0_CE0  (GPIO8)  → U5.CS
SPI0_MOSI (GPIO10) → U5.SDI
SPI0_MISO (GPIO9)  ← U5.SDO
SPI0_SCLK (GPIO11) → U5.SCLK
GPIO27             ← U5.INT1 (データレディ割り込み)
+3.3V              → U5.VDD, U5.VDDIO
```

**デカップリング** (ICM-42688 要求: VDD に100nF + 10μF, VDDIO に100nF):
- C16: 10μF 0402 X5R (U5.VDD直近)
- C17: 100nF 0402 X5R
- C18: 100nF 0402 X5R (VDDIO)

**レイアウト注意**:
- U5 の電源ピン下にベタ GND ビア (リターン電流最短化)
- SPI ラインは 25mil 幅, 等長配線 (±2mm 以内)
- IMU とモータードライバー U1/U2 は最低 10mm 離間

**部品**:
| Reference | 部品 | メーカー | 備考 |
|---|---|---|---|
| U5 | ICM-42688-P | TDK | LGA-14 2.5×3mm |
| C16 | 10μF 0402 X5R | TDK CGA2 | VDD デカップリング |
| C17,C18 | 100nF 0402 X5R | TDK CGA2 | バイパス |

---

### 3.5 ToF 距離センサー (VL53L4CX)

**目的**: 前方障害物検知 / グリッパー対象物距離計測
**IC**: ST Microelectronics VL53L4CX (SATEL-VL53L4 breakout, またはベアダイ 2.8V)
**インターフェース**: I2C-1 (SDA: GPIO2, SCL: GPIO3), アドレス 0x29
**電源**: 3.3V (VCSEL VCC 共用)

**回路**:
```
GPIO2 (I2C1_SDA) ─── R9 (4.7kΩ pullup → 3.3V) ─── U6.SDA
GPIO3 (I2C1_SCL) ─── R10 (4.7kΩ pullup → 3.3V) ─── U6.SCL
GPIO4 (XSHUT)   ───────────────────────────── U6.XSHUT
+3.3V ──────────────────────────────────────── U6.VDD, U6.AVDD
```

**注意**: I2C バスに他デバイスを追加する場合、XSHUT で排他アドレス割当てが必要。
VL53L4CX の AVDD (VCSEL 電源) は 2.8V 推奨。3.3V 動作も許容 (VL53L4CX DS §2.3)。

**部品**:
| Reference | 部品 | メーカー | 備考 |
|---|---|---|---|
| U6 | VL53L4CX | ST | SATEL または bare 12P LGA |
| R9,R10 | 4.7kΩ 0402 1% | ローム MCR03 | I2C プルアップ |
| C19 | 100nF 0402 | TDK | バイパス |
| C20 | 10μF 0402 X5R | TDK | AVDD バルク |

---

### 3.6 電源回路

#### 入力: 7.4V Li-ion (パナソニック NCR18650B × 2 直列)

| レール | 入力 | 出力 | デバイス | 備考 |
|---|---|---|---|---|
| 5V ロジック | 7.4V | 5V 3A | TDK-Lambda CCG3-24-05S | RPi 5 供給 |
| 7.4V サーボ | 直結 | 7.4V | — | CN1 servo bus 直接 |
| 3.3V オンボード | 5V | 3.3V 500mA | Texas TLVH431 + FET LDO | ICM/VL53 専用 |
| モーター | 7.4V | 7.4V | — | U1/U2 VM 直接 (フューズ経由) |

#### 5V DC-DC (TDK-Lambda CCG3-24-05S)
```
J2 (DC barrel 5.5/2.1mm) ─── F1 (3A ポリフューズ) ─── U7.VIN+
                                                       U7.VIN-
U7.VOUT+ → J1.Pin2/4 (RPi +5V)
U7.VOUT- → GND
C21: 220μF 16V (入力バルク)
C22: 100μF 10V (出力バルク)
```

#### 3.3V LDO (ICM/ToF 専用)
```
5V ── R11 (0Ω jumper / 抵抗調整) ── U8.Vin (MCP1700-3302E/TO)
U8.Vout → U5.VDD, U5.VDDIO, U6.VDD (各100nF バイパス付き)
```

MCP1700: Microchip, SOT-23, 250mA, Iq=1.6μA, dropout 178mV@100mA

#### フューズ
| Reference | 部品 | 定格 | 保護対象 |
|---|---|---|---|
| F1 | Littelfuse 0154003.DR (3A) | 3A | 全体入力 |
| F2 | Littelfuse 0154001.DR (1A) | 1A | サーボバス CN1 |

---

## 4. ネットリスト (主要 net 一覧)

| Net 名 | 電圧 | 主な接続先 |
|---|---|---|
| +7V4_BATT | 7.4V | J2, F1, U7.VIN, CN1.pin1, U1.VM, U2.VM |
| +5V0_RPi | 5V | U7.VOUT, J1.pin2/4, U8.VIN |
| +3V3_LGC | 3.3V | U8.VOUT, U5.VDD, U6.VDD |
| GND | 0V | 共通 (J1.GND, U1.GND, U2.GND, U5.GND, U6.GND, J2.GND) |
| ICS_DATA | — | Q1.Y, R1, CN1.pin3 |
| ICS_TX_RAW | — | GPIO14 → Q1.A |
| ICS_TX_EN | — | GPIO17 → Q1.OE |
| SPI0_MOSI | — | GPIO10 → U5.SDI |
| SPI0_MISO | — | U5.SDO → GPIO9 |
| SPI0_CLK | — | GPIO11 → U5.SCLK |
| SPI0_CS0 | — | GPIO8 → U5.CS |
| I2C1_SDA | — | GPIO2, R9, U6.SDA |
| I2C1_SCL | — | GPIO3, R10, U6.SCL |
| PWM_L | — | GPIO12 → U1.PWMA |
| PWM_R | — | GPIO13 → U2.PWMB |
| MTR_L_A | — | U1.AO1 → CN4.1 |
| MTR_L_B | — | U1.AO2 → CN4.2 |
| MTR_R_A | — | U2.BO1 → CN5.1 |
| MTR_R_B | — | U2.BO2 → CN5.2 |

---

## 5. コネクタ配置 (レイアウト)

```
          ┌──────────────────────────────────────┐
          │  65mm                                │
  ┌───────┤                                      ├───────┐
  │       │  [U5 IMU]        [U6 ToF]            │       │ 5
  │       │                                      │       │ 6
  │       │  [U1 MotorDrv]   [U2 MotorDrv]       │       │ .
  │       │                                      │       │ 5
  │  J1   │  [Q1 ICS]                            │       │ m
  │ 40P   │                 [U3/U4 RS485]        │  J2   │ m
  │       │  [U7 DC-DC]     [U8 LDO]             │ barrel│
  │       │                                      │       │
  └───────┤  CN4  CN5  CN1  CN2  CN3             ├───────┘
          └──────────────────────────────────────┘
          (Bottom edge: motor connectors + servo connectors)
```

**コネクタ座標** (基板左下原点):
| Reference | X (mm) | Y (mm) | 方向 |
|---|---|---|---|
| J1 (HAT 40P) | 3.5 | 11.0 | 左端, 縦配置 |
| J2 (DC barrel) | 60.0 | 28.0 | 右端 |
| CN1 (ICS bus) | 15.0 | 3.5 | 下端 |
| CN2 (RS485-A) | 28.0 | 3.5 | 下端 |
| CN3 (RS485-B) | 38.0 | 3.5 | 下端 |
| CN4 (左モーター) | 5.0 | 3.5 | 下端 |
| CN5 (右モーター) | 48.0 | 3.5 | 下端 |

---

## 6. P-Ban.com 発注仕様

| 項目 | 値 |
|---|---|
| 基板サイズ | 65 × 56.5 mm |
| 層数 | 2層 |
| 基板材質 | FR4 Tg130 |
| 銅箔厚 | 内外層 35μm (1oz) |
| 基板厚 | 1.6 mm |
| 表面処理 | HASL (鉛フリー SnCu) |
| 最小ライン/スペース | 0.1mm / 0.1mm |
| 最小ビア穴 | 0.3mm |
| ソルダーレジスト | 両面グリーン |
| シルク | 両面白 |
| 発注数 | 試作 5枚 → 量産 500枚 |
| Gerber 形式 | RS-274X + NC Drill (Excellon) |
| ファイル | KiCad → `File > Plot > Gerber` + `File > Fabrication Outputs > Drill Files` |

---

## 7. P-Ban.com BOM (部品実装発注用)

| Reference | 数量 | 部品番号 | メーカー | パッケージ | 備考 |
|---|---|---|---|---|---|
| U1,U2 | 2 | TB6612FNG | 東芝 | SSOP-24 | モータードライバー |
| U3,U4 | 2 | MAX3485EESA+ | Maxim | SOIC-8 | RS485 トランシーバー |
| U5 | 1 | ICM-42688-P | TDK | LGA-14 | IMU |
| U6 | 1 | VL53L4CX | ST | LGA-12 | ToF センサー |
| U7 | 1 | CCG3-24-05S | TDK-Lambda | SIP-8 | DC-DC 5V 3A |
| U8 | 1 | MCP1700-3302E/TO | Microchip | SOT-23 | 3.3V LDO |
| Q1 | 1 | 74AHCT1G125GW | NXP | SOT-353 | バッファ (ICS) |
| D1 | 1 | BAT54S | ローム | SOT-23 | クランプダイオード |
| D2,D3 | 2 | RB521S-30T2R | ローム | SOD-323 | フライバック |
| F1 | 1 | 0154003.DR | Littelfuse | 0603 SMD | 3A ポリフューズ |
| F2 | 1 | 0154001.DR | Littelfuse | 0603 SMD | 1A ポリフューズ |
| J1 | 1 | 2×20P 2.54mm メス | Amphenol | — | RPi HAT header |
| J2 | 1 | PJ-002AH-SMT | CUI | SMT | DC 5.5/2.1mm |
| CN1,CN2,CN3 | 3 | S4B-PH-SM4-TB | JST | PH 4P | サーボ/RS485 |
| CN4,CN5 | 2 | VHR-2N | JST | VH 2P | モーター |
| R1 | 1 | 100Ω 0402 5% | ローム MCR03 | 0402 | ICS ターミネーター |
| R5,R6 | 2 | 120Ω 0402 1% | ローム MCR03 | 0402 | RS485 終端 |
| R9,R10 | 2 | 4.7kΩ 0402 1% | ローム MCR03 | 0402 | I2C プルアップ |
| R11 | 1 | 0Ω 0402 | — | 0402 | 5V→LDO ジャンパー |
| C1-C5 | 5 | 100nF 0402 X5R | TDK CGA2 | 0402 | バイパス |
| C6-C11 | 6 | 100μF 16V | ニチコン UWT1C101 | SMD 電解 | VM デカップリング |
| C12-C18 | 7 | 100nF 0402 X5R | TDK CGA2 | 0402 | IC バイパス |
| C19,C20 | 2 | 10μF 0402 X5R | TDK CGA2 | 0402 | ToF/IMU バルク |
| C21 | 1 | 220μF 16V | ニチコン | SMD 電解 | DC-DC 入力 |
| C22 | 1 | 100μF 10V | ニチコン | SMD 電解 | DC-DC 出力 |

---

## 8. ERC / DRC チェックリスト (KiCad)

- [ ] 全 IC の VCC/VDD 未接続ピン → PWR_FLAG 追加
- [ ] SPI ライン長差 ≤ 2mm
- [ ] TB6612 VM ピン直下ビア (熱放散, 2 × φ0.4mm ビア)
- [ ] U7 DC-DC の入出力間最小 5mm 離間 (EMI)
- [ ] ICM-42688-P の GND パッド ビア密度 ≥ 4個 (熱放散)
- [ ] HAT フォームファクター規定穴位置 (RPi HAT Design Guide v1.0) 確認
  - M2.5 取り付け穴: (3.5, 3.5), (61.5, 3.5), (3.5, 52.5), (61.5, 52.5)

---

## 9. 試作フロー (Meviy + P-Ban.com 連携)

1. KiCad ERC/DRC クリア → Gerber + Drill エクスポート
2. P-Ban.com サイトに Gerber zip アップロード → 自動見積り
3. BOM CSV (P-Ban.com 形式) 同時アップロード → 実装見積り
4. tsukuru.etzhayyim.com `com.etzhayyim.apps.tsukuru.pcbProject.create` でプロジェクト登録
5. `com.etzhayyim.apps.tsukuru.pban.requestQuote` → 見積り取得 → 発注承認 (48h human_review)
6. 発注 → P-Ban.com 製造期間 5-7 営業日 → 納品
7. RPi 5 に装着 → `firmware/test/home_pose.py` 実行 → 全軸動作確認

---

## 10. 関連ファイル

| ファイル | 内容 |
|---|---|
| `firmware/armcrawler/servo/ics_driver.py` | ICS3.5 ドライバー (GPIO17 TX_EN) |
| `firmware/armcrawler/crawler/motor_driver.py` | TB6612FNG ドライバー |
| `firmware/armcrawler/kinematics/ik.py` | 逆運動学ソルバー |
| `cad-spec/mechanical-spec-v1.md` | 機械設計仕様 + DH パラメータ |
| `bom/BOM-v1.md` | 全体 BOM + 原価計算 |
| `30-graph/graph-schema/sql_migrations/20260514150000_tsukuru_cad_pcb_design_flow.up.sql` | tsukuru PCB 設計フロー |
