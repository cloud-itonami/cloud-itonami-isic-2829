# Giemon Otete 公式サイト IA (情報アーキテクチャ) v1

URL: giemon.etzhayyim.com
フレームワーク: SvelteKit (既存 open-robo-hp performer)
ホスティング: Cloudflare Pages (CDN)
作成日: 2026-05-14

---

## 1. サイトマップ

```
giemon.etzhayyim.com/
├── /                    ランディングページ (LP)
├── /product             製品詳細
│   ├── /product/specs   仕様・スペック表
│   └── /product/bom     BOM・部品一覧 (公開版)
├── /assembly            組立マニュアル
│   ├── /assembly/step/1 … /assembly/step/6
│   └── /assembly/faq    よくある質問
├── /firmware            ファームウェア・ドキュメント
│   ├── /firmware/quickstart
│   ├── /firmware/ros2
│   └── /firmware/api
├── /education           教育・研究向け
├── /buy                 購入ページ
└── /blog                技術ブログ
```

---

## 2. ページ別コンテンツ仕様

### `/` — ランディングページ

**目的**: 初回訪問者の興味喚起 → 購入/クラファン誘導

**セクション**:

| # | セクション名 | コンテンツ | CTA |
|---|---|---|---|
| 1 | Hero | キャッチコピー + ロボット動画ループ (15s) | 「購入する」「組立を見る」 |
| 2 | Key Features | 3カード: 全国産部品 / ROS2対応 / オープンソース | — |
| 3 | Product Demo | GIF: グリッパー把持 + クローラー走行 + RViz | 「仕様を見る」 |
| 4 | Specs At a Glance | アーム6軸・リーチ420mm・クローラー等 主要スペック | — |
| 5 | Japan-Made Story | 製造パートナー (Meviy / P-Ban.com) ロゴ + 短文 | — |
| 6 | Ecosystem | GitHub / ROS2 / Python / tsukuru 連携図 | 「GitHub を見る」 |
| 7 | Pricing | Standard ¥98,780 / Education 3-pack / HAT単体 | 「Amazon で購入」「Makuake で支援」 |
| 8 | Press / Media | (出荷後追加: メディア掲載ロゴ) | — |
| 9 | Footer | リンク集 + 会社情報 + ライセンス | — |

**技術要件**:
- Hero 動画: `<video autoplay muted loop playsinline>` WebM + MP4 フォールバック
- Core Web Vitals: LCP < 2.5s (Cloudflare CDN 静的配信で達成)
- OGP: `og:image` 1200×630 完成品写真

---

### `/product` — 製品詳細

**目的**: 購入検討者へ技術的信頼性の訴求

**セクション**:
1. **3Dビュー** (Three.js GLTF viewer, RPi 5 HAT装着状態)
   ← STEP → GLTF 変換: `npm run convert-step` (Open CASCADE WASM)
2. **アーム仕様**: DH パラメータ表 + 動作範囲図
3. **クローラー仕様**: 走行速度・登坂角度・最小旋回半径
4. **HAT 仕様**: ICM-42688-P / VL53L4CX / TB6612FNG × 2 ブロック図
5. **ソフトウェアスタック**: 層構成図 (ics_driver → kinematics → ROS2 → ユーザーアプリ)
6. **梱包内容**: 部品画像一覧

`/product/specs` — 詳細スペック表 (BOM-v1.md の公開 subset)
`/product/bom` — GitHub raw STEP/回路図へのリンク集

---

### `/assembly` — 組立マニュアル

**目的**: 購入後の組立サポート、購入前の難易度確認

**UX 要件**:
- ステップ 1〜6 の進捗バー (localStorage で記憶)
- 各ステップ: 写真 + 動画クリップ + テキスト手順
- 印刷用 PDF ダウンロード (`/assembly/manual-v1.pdf`)
- 右側パネル: 必要部品チェックリスト (クリックで ✅)

**ステップ対応** (assembly-manual-v1.md §1〜6):
| URL | タイトル |
|---|---|
| `/assembly/step/1` | クローラーシャシー組立 (約60分) |
| `/assembly/step/2` | J1 ターンテーブル取付 (約30分) |
| `/assembly/step/3` | アームリンク J2〜J6 組立 (約45分) |
| `/assembly/step/4` | グリッパー取付 (約15分) |
| `/assembly/step/5` | 電装・配線 (約30分) |
| `/assembly/step/6` | ソフトウェアセットアップ (約30分) |

---

### `/firmware` — ファームウェアドキュメント

**目的**: 開発者・上級ユーザー向けの技術リファレンス

**サブページ**:

`/firmware/quickstart`:
```bash
# 3コマンドでスタート
git clone https://github.com/etzhayyim/otete
cd otete/firmware
python test/home_pose.py   # 全軸ホームポジション確認
```

`/firmware/ros2`:
- インストール手順 (ROS2 Humble + otete_ros2 パッケージ)
- `ros2 launch otete_ros2 bringup.launch.py` 実行例
- トピック一覧表 (/cmd_vel, /arm/joint_states, /odom, /imu/data_raw, /camera/image_raw)
- RViz 設定ファイル (ダウンロード)

`/firmware/api`:
- `ICSBusDriver` API リファレンス
- `inverse_kinematics()` 関数仕様
- `CrawlerDriver` API リファレンス
- Python サンプルコード (埋め込み表示, Shiki.js シンタックスハイライト)

---

### `/education` — 教育・研究向け

**対象**: 高校ロボット部顧問・大学研究室

**コンテンツ**:
- 教育版パッケージ案内 (3セット + 指導資料 ¥269,500)
- 授業カリキュラム例 (全12回 PDF ダウンロード)
- 対応大会・学習目標 (NHK大学ロボコン準拠項目)
- 導入校インタビュー (出荷後追加)
- 見積もり依頼フォーム (Cloudflare Forms → sales@etzhayyim.com)

---

### `/buy` — 購入ページ

**目的**: 購入チャンネルへの誘導ハブ

**チャンネル**:
| チャンネル | リンク | 特典 |
|---|---|---|
| Amazon JP (FBA) | amazon.co.jp/dp/{ASIN} | プライム翌日配送 |
| Makuake | makuake.com/project/otete | 早期割引 ¥79,800 |
| Kickstarter | kickstarter.com/projects/etzhayyim/otete | $499 early bird |
| tsukuru.etzhayyim.com | 直販 (B2B 法人向け) | 見積もり対応 |

**在庫状況**: Cloudflare KV から動的取得 (Amazon API / Makuake API)

---

### `/blog` — 技術ブログ

**目的**: SEO + 開発者コミュニティ醸成

**想定記事**:
| タイトル案 | 公開タイミング |
|---|---|
| ICS3.5 バスサーボを RPi 5 で動かす: 半二重 UART 徹底解説 | クラファン開始時 |
| DLS-IK 実装詳解: numpy だけで 6 軸逆運動学 | クラファン開始時 |
| Meviy で翌日納品: アルミ切削部品を 24h で手に入れる方法 | クラファン開始時 |
| Otete HAT 回路設計: KiCad + P-Ban.com で基板を国内製造 | 試作基板完成時 |
| ROS2 Humble + Navigation2 でクローラー自律走行 | 出荷前 |

**システム**: Markdown + SvelteKit `+page.md` ルーティング (mdsvex)

---

## 3. 共通技術要件

### フロントエンド

| 項目 | 技術選択 |
|---|---|
| フレームワーク | SvelteKit (既存 open-robo-hp performer) |
| スタイリング | Tailwind CSS v4 |
| コードハイライト | Shiki.js (ROS2 / Python) |
| 3Dビュー | Three.js (WebGL, GLTF) |
| アニメーション | svelte/transition + CSS |
| フォント | Noto Sans JP (日本語) + Inter (英語) |

### パフォーマンス目標

| 指標 | 目標値 |
|---|---|
| LCP | < 2.5s (Cloudflare CDN) |
| CLS | < 0.1 |
| FID | < 100ms |
| Lighthouse Score | ≥ 90 (all categories) |

### i18n

- デフォルト: 日本語 (`/ja/` prefix 省略)
- 英語: `/en/` prefix (Kickstarter 向け)
- 切替: ヘッダー右上 JA / EN トグル
- 翻訳ファイル: `src/lib/i18n/{ja,en}.json`

### SEO

- `<title>` パターン: `{ページ名} | Giemon Otete — 日本製 6軸アームクローラーロボット`
- canonical URL 設定
- Sitemap XML: `/sitemap.xml` (SvelteKit エンドポイント自動生成)
- robots.txt: 全クロール許可
- Structured Data: `Product` schema.org JSON-LD (価格・在庫)

### Analytics

- Cloudflare Web Analytics (プライバシーファースト、cookie なし)
- コンバージョン追跡: `/buy` ページのチャンネル別クリック数

---

## 4. デプロイ構成

```
open-robo-hp (SvelteKit)
├── src/
│   ├── routes/               # SvelteKit ルート (上記ページ対応)
│   ├── lib/
│   │   ├── i18n/             # 翻訳ファイル
│   │   └── components/       # 共有コンポーネント
│   └── static/
│       ├── models/           # GLTF 3Dモデル
│       ├── images/           # 製品写真
│       └── downloads/        # PDF マニュアル
├── kotodama.jsonld           # Cloudflare Pages performer
└── wrangler.jsonc
```

**ドメイン**: `giemon.etzhayyim.com` → Cloudflare Pages
**CD**: GitHub Actions → `pnpm build` → `etzhayyim deploy` (mainブランチマージ時)
