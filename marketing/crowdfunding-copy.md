# Giemon Otete — クラウドファンディング商品文

## Makuake 掲載文（日本語）

---

### プロジェクトタイトル

**日本製ロボットキット「Giemon Otete」— 6軸アーム×クローラー、Raspberry Pi 5で動く本格AIロボット**

---

### キャッチコピー

> ねじを締めれば、ロボットと話せる。
> 日本の部品だけで作った、本物の教育用ロボット組み立てキット。

---

### リード文（200字）

Giemon Oteteは、6軸ロボットアームとクローラー走行ベースを組み合わせた日本製組み立てキットです。
近藤科学・双葉電子のサーボ、ミスミのアルミフレーム、パナソニック電池——国内調達の部品でゼロから作り上げ、Raspberry Pi 5でAI・ROS2制御まで学べます。中国製競合品に頼らない、日本のメイカーが作るメイカーのためのロボットです。

---

### ストーリー

#### なぜ今、日本製なのか

ロボット教育市場に流通する多くの組み立てキットは、中国製ハードウェアで設計・製造されています。それは悪いことではありませんが、「日本の技術でゼロから設計した」本格的な教育向けロボットキットはほとんど存在しません。

私たちは問いました。**近藤科学のサーボ、ミスミのフレーム、パナソニックの電池でロボットキットを作ったら、どれだけ丈夫で、どれだけ学べるものになるか。**

Giemon Oteteは、その答えです。

#### 競合（HiWonder ArmPi Ultra）との比較

| 比較項目 | Giemon Otete | HiWonder ArmPi Ultra |
|---|---|---|
| アーム自由度 | 6軸 + グリッパー | 5軸 + グリッパー |
| サーボ品質 | 近藤科学 KRS（日本産業グレード） | カスタム中国製バスサーボ |
| フレーム素材 | ミスミ AL6061アルマイト（CNC切削） | アルミ板金（汎用） |
| 制御SBC | Raspberry Pi 5 (4GB) | Raspberry Pi 5 (4GB) |
| ROS2対応 | あり（Humble、Ubuntu22.04） | なし（専用SDKのみ） |
| 組立難易度 | 中級（全部品を自分で組む） | 初級（ほぼ組み立て済み） |
| サポート言語 | 日本語ネイティブ | 英語/中国語（日本語翻訳） |
| 価格（CF早割） | ¥89,800 | 約¥120,000（国内輸入） |

#### 6軸アーム — 本物の工業ロボットアーキテクチャ

Giemon Oteteのアームは「Modified D-H パラメーター」で設計した6軸構成。大学・企業の産業ロボット研究で使われる座標系と同じ設計思想なので、このキットで習得したプログラミング知識はそのままROS2・MoveIt!に転用できます。

```
最大リーチ: 420mm
可搬重量:   500g
繰り返し精度: ±2mm（位置キャリブレーション後）
```

#### クローラーベース — 不整地・坂道もこなすタフな走行

タミヤ製ゴムトラックとマブチモーターの組み合わせで、最大20°の傾斜を安定走行。前後の村田製作所 ToFセンサーで自動障害物回避も実装可能です。

#### Raspberry Pi 5 × ROS2 = 本格AIロボット制御

- **カメラ**: Sony IMX477 12MPカメラ + OpenCV 物体認識
- **ROS2 Humble**: ノード・トピック・アクション設計を実際に動く実機で学習
- **逆運動学**: NumPy + Roboticsライブラリで座標→関節角度の計算体験
- **強化学習**: Gymnasium + Stable-Baselines3でAI制御実験も可能

#### 日本製にこだわった理由

| 部品 | メーカー | こだわりポイント |
|---|---|---|
| バスサーボ | 近藤科学（東京） | 40年以上の日本ロボットホビー文化を牽引。ICS通信は確実で高精度 |
| アルミフレーム | ミスミ（東京） | Meviyオンラインで設計→翌日納品の国内CNC加工 |
| 電池 | パナソニック（大阪） | NCR18650Bは世界最高水準のエネルギー密度と信頼性 |
| DC-DC電源 | TDKラムダ（東京） | 産業用グレードの安定化電源。教育現場での安全性を最優先 |
| 制御基板 | P-Ban.com（東京） | 国内PCB設計・製造。修理・改造もしやすい |

---

### リターン設計

| コース | 内容 | 価格 | 限定数 |
|---|---|---|---|
| **超早割 A** | Giemon Otete 完全キット（バッテリー込み）+ 組立サポート動画 | **¥79,800** | 30名 |
| **早割 B** | Giemon Otete 完全キット（バッテリー込み）+ 組立サポート動画 | **¥89,800** | 70名 |
| **通常 C** | Giemon Otete 完全キット（バッテリー込み） | **¥109,800** | 無制限 |
| **教育セット D** | キット×3台 + 教室向けカリキュラムPDF + オンラインサポート1年 | **¥249,800** | 20セット |
| **法人 E** | キット×10台 + 設計データ（STEP/STL）ライセンス + 導入サポート | **¥890,000** | 5社 |

---

### スケジュール

| 時期 | マイルストーン |
|---|---|
| 2026年6月 | Makuakeプロジェクト公開 |
| 2026年7月 | 目標額達成→量産発注（ミスミMeviy / P-Ban.com） |
| 2026年9月 | 試作確認・組立説明書最終版 |
| 2026年10月 | 支援者向け出荷開始 |
| 2026年11月 | Amazon JP 一般販売開始 |
| 2026年12月 | Kickstarter（海外）公開 |

---

### リスクと対策

| リスク | 対策 |
|---|---|
| 近藤科学 KRS 在庫不足 | 双葉電子 RS405CB（同等性能）への切替オプション確保済み |
| バッテリー輸送規制 | 国内出荷はPSE認証済みBMS込みで問題なし。海外はバッテリー別途購入案内 |
| Raspberry Pi 入手難 | スイッチサイエンス・KSY 国内正規代理店から確保済み |
| 製造遅延 | Meviy 最短1日納品。バッファ6週間を設計済み |

---

## Kickstarter 掲載文（English）

### Title

**Giemon Otete — 6-DOF Arm + Crawler Robot Kit, 100% Japanese Parts, Raspberry Pi 5 Powered**

### Tagline

> Tighten the screws. Talk to your robot.
> A real AI-capable robot kit engineered entirely in Japan.

### Description (Short)

Giemon Otete is an open-hardware robot kit combining a 6-degree-of-freedom arm with a tracked crawler base. Every structural component, servo, battery, and PCB is sourced and manufactured in Japan — KONDO Science servos, Misumi CNC aluminum, Panasonic cells, and TDK-Lambda power. Runs ROS2 Humble on Raspberry Pi 5.

### Key Specs

- **Arm**: 6-DOF + gripper, 420mm reach, 500g payload
- **Servos**: KONDO KRS-6013IHV (35kg·cm bus), KRS-4014ICS, KRS-2350IHV
- **Crawler**: Tamiya rubber tracks, Mabuchi DC motors, 20° slope capability
- **SBC**: Raspberry Pi 5 4GB
- **Camera**: Sony IMX477 12MP (Raspberry Pi Camera v3)
- **Sensors**: Murata VL53L4CX ToF ×2, 9-axis IMU
- **Power**: Panasonic NCR18650B ×4 (14.8V 3400mAh)
- **Software**: Ubuntu 22.04, ROS2 Humble, Python 3.11

### Why Japanese Parts?

The global robot kit market is dominated by Chinese hardware. Japan has a 40-year heritage of precision servo manufacturing (KONDO Science, Futaba), world-class aluminum machining (Misumi), and industrial-grade power electronics (TDK-Lambda, Cosel). Giemon Otete brings that heritage into an open, hackable kit format.

### Reward Tiers (USD)

| Tier | Contents | Price | Qty |
|---|---|---|---|
| Super Early Bird | Full kit + assembly support video | **$499** | 30 |
| Early Bird | Full kit + assembly support video | **$599** | 70 |
| Standard | Full kit | **$699** | unlimited |
| Education Pack | 3 kits + curriculum PDF + 1yr support | **$1,599** | 20 |

> Note: International shipping ~$80. Batteries shipped separately to comply with air freight regulations.

---

## Amazon JP 商品ページ骨格

### 商品タイトル

Giemon Otete 6軸ロボットアーム+クローラー 組み立てキット Raspberry Pi 5対応 ROS2対応 日本製部品使用 近藤科学KRSサーボ ミスミアルミフレーム

### 商品説明文（箇条書き）

- **本格6軸アーム**: Modified D-Hパラメーター設計、最大リーチ420mm、可搬重量500g。産業ロボットと同じ座標系でプログラミング学習
- **高品質日本製サーボ**: 近藤科学 KRS-6013IHV（35kg·cm）、KRS-4014ICS（20kg·cm）を採用。バスサーボで6軸を1本のケーブルで制御
- **ミスミCNCアルミフレーム**: Meviyオンラインで製造したAL6061アルマイト仕上げ。剛性が高く組み立てやすい
- **クローラー走行ベース**: タミヤゴムトラック×マブチモーター。最大傾斜20°対応。村田製作所ToFセンサーで障害物検知
- **Raspberry Pi 5対応**: Ubuntu 22.04 + ROS2 Humble + Python 3.11。OpenCV・MoveIt!・強化学習まで幅広く対応
- **パナソニック18650電池**: NCR18650B×4本（14.8V/3400mAh）付属。TDKラムダ・コーセル製DC-DC電源で安定動作

### カテゴリ

おもちゃ＞ロボット＞教育・STEM ロボット

### キーワード

ロボット キット 組み立て 6軸 ロボットアーム クローラー Raspberry Pi ROS2 近藤科学 ミスミ 日本製 教育

---

## 公式サイト構成案 (armcrawler.etzhayyim.com)

### ページ構成

```
/ (トップ)
├── /product         — 製品仕様・写真ギャラリー
├── /assembly        — 組立説明書（Web版、全ステップ写真付き）
├── /firmware        — GitHubリンク + クイックスタートガイド
├── /education       — 教育機関向け情報・カリキュラム例
├── /buy             — Makuake / Kickstarter / Amazon JP リンク
├── /support         — FAQ + GitHub Issues + サポートフォーム
└── /blog            — 活用事例・開発日誌
```

### トップページ構成（LP）

1. **Hero**: ロボット走行+アーム動作のGIF/動画 + CTA「支援する（Makuake）」
2. **What is it**: 30秒で分かる仕様カード（6軸/クローラー/ROS2）
3. **Japan-Made**: 各部品メーカーのロゴ + マップ（近藤科学＝東京、ミスミ＝東京…）
4. **Specs**: スペック表（競合比較含む）
5. **Assembly Preview**: 組立ステップ6画面スライド
6. **Education**: 学校・研究機関採用事例
7. **FAQ**: 主要5問
8. **CTA**: 「今すぐ支援」ボタン（Makuake/Kickstarter）
