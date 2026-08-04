<script lang="ts">
  const sections = [
    {
      title: 'ロボットアーム',
      rows: [
        { spec: '自由度', value: '6軸 + グリッパー (7軸相当)' },
        { spec: '最大リーチ', value: '420 mm' },
        { spec: '最小リーチ（折り畳み）', value: '85 mm' },
        { spec: '可搬重量', value: '500 g（手首基準）' },
        { spec: '繰り返し精度', value: '±2 mm' },
        { spec: 'J1 旋回角', value: '±180°' },
        { spec: 'J2 肩ピッチ', value: '-10° 〜 +100°' },
        { spec: 'J3 肘ピッチ', value: '-10° 〜 +120°' },
        { spec: 'J4 手首ピッチ', value: '±90°' },
        { spec: 'J5 手首ロール', value: '±180°' },
        { spec: 'J6 手首 Yaw', value: '±180°' },
        { spec: 'J1/J2 サーボ', value: 'KRS-6013IHV (13.0 kg·cm, 0.16 s/60°)' },
        { spec: 'J3/J4 サーボ', value: 'KRS-4014ICS (14.0 kg·cm, 0.14 s/60°)' },
        { spec: 'J5/J6 サーボ', value: 'KRS-2350IHV (5.5 kg·cm, 0.12 s/60°)' },
        { spec: 'グリッパー開口幅', value: '0 〜 80 mm' },
        { spec: 'グリッパー把持力', value: '約 8 N' },
        { spec: 'グリッパー駆動', value: 'Futaba S3003 (PWM)' },
        { spec: 'フレーム材質', value: 'AL6061-T6 アルマイト（黒）' },
      ],
    },
    {
      title: 'クローラーベース',
      rows: [
        { spec: '全長', value: '320 mm' },
        { spec: '全幅（トラック外幅）', value: '280 mm' },
        { spec: '車体幅（サイドプレート内幅）', value: '196 mm' },
        { spec: '全高（トラック面〜上面）', value: '120 mm' },
        { spec: 'トラック幅', value: '42 mm（タミヤ 70168 規格）' },
        { spec: '最低地上高', value: '22 mm' },
        { spec: '最大走行速度', value: '0.6 m/s（無負荷）' },
        { spec: '最大登坂角', value: '20°' },
        { spec: '最小信地旋回半径', value: '0（超信地旋回可能）' },
        { spec: 'スプロケット歯数', value: '12T（ピッチ円 φ38 mm）' },
        { spec: 'クローラーモーター', value: 'マブチ RS-380PH × 2（6〜12V）' },
        { spec: 'モータードライバー IC', value: '東芝 TB6612FNG × 2' },
        { spec: 'ボトムプレート', value: 'AL6061-T6, 310 × 196 × 3 mm, アルマイト黒' },
        { spec: 'サイドプレート', value: 'AL6061-T6, 320 × 80 × 3 mm, アルマイト黒（ハード）' },
      ],
    },
    {
      title: 'Otete HAT (制御基板)',
      rows: [
        { spec: '基板規格', value: 'Raspberry Pi HAT (65 × 56.5 mm)' },
        { spec: '層数 / 材質', value: '2 層 FR4 1.6 mm HASL' },
        { spec: '製造', value: 'P-Ban.com（東京）' },
        { spec: 'IMU', value: 'TDK ICM-42688-P 6 軸 (加速度 + ジャイロ) SPI1' },
        { spec: 'ToF センサ', value: 'ST VL53L4CX (最大 6m) I2C 0x29' },
        { spec: 'モータードライバー', value: '東芝 TB6612FNG × 2 (2ch × 2 = 4ch)' },
        { spec: 'ICS バッファ', value: 'TI 74AHCT1G125 半二重 UART TX_EN' },
        { spec: 'RS485 トランシーバー', value: 'ADI MAX3485E × 2 ch' },
        { spec: 'DC-DC コンバーター', value: 'TDK-Lambda CCG3-24-05S (7.4V → 5V, 3W 絶縁型)' },
        { spec: 'ICS バスコネクタ', value: 'CN1 4P（サーボチェーン）' },
        { spec: 'モーターコネクタ', value: 'CN4/CN5 各 2P' },
        { spec: 'RS485 コネクタ', value: 'CN2/CN3 各 4P' },
        { spec: '電源入力', value: 'DC バレル 5.5/2.1 mm (7.4V)' },
      ],
    },
    {
      title: 'コンピュータ / ソフトウェア',
      rows: [
        { spec: 'SBC', value: 'Raspberry Pi 5 (4GB RAM)' },
        { spec: 'OS', value: 'Ubuntu 22.04 LTS (64-bit)' },
        { spec: 'ROS バージョン', value: 'ROS2 Humble Hawksbill' },
        { spec: 'Python バージョン', value: '3.10 以上' },
        { spec: 'ICS 通信速度', value: '115,200 bps 半二重 UART' },
        { spec: 'ICS プロトコル', value: 'ICS3.5 (近藤科学)' },
        { spec: 'キャメラ（オプション）', value: 'IMX477 Sony 12 MP (別途購入)' },
        { spec: '依存ライブラリ', value: 'pyserial, numpy, rclpy, sensor_msgs, geometry_msgs' },
        { spec: 'ライセンス（ソフトウェア）', value: 'Apache License 2.0' },
        { spec: 'ライセンス（ハードウェア）', value: 'CERN-OHL-S v2 + CC BY-SA 4.0' },
      ],
    },
    {
      title: '電源',
      rows: [
        { spec: 'バッテリーセル', value: 'パナソニック NCR18650B' },
        { spec: 'バッテリー構成', value: '4S2P（7.4V〜16.8V、公称 14.8V）' },
        { spec: '容量', value: '6,800 mAh（3,400 mAh × 2P）' },
        { spec: 'エネルギー', value: '100.6 Wh' },
        { spec: '連続動作時間（目安）', value: '約 90 分（中程度負荷）' },
        { spec: '充電方式', value: 'リバランス充電（BMS 内蔵）' },
        { spec: 'BMS', value: 'DW01 系 セルバランス回路' },
      ],
    },
    {
      title: '梱包 / 出荷',
      rows: [
        { spec: '梱包外形（目安）', value: '450 × 380 × 200 mm' },
        { spec: '梱包重量（目安）', value: '約 3.5 kg' },
        { spec: '原産国', value: '日本（アセンブリ：東京）' },
        { spec: 'CE / PSE', value: '取得予定（出荷前）' },
        { spec: '保証期間', value: '1 年（製造不良）' },
      ],
    },
  ];
</script>

<svelte:head>
  <title>詳細スペック表 | Giemon Otete — 日本製 6軸アームクローラーロボットキット</title>
  <meta name="description" content="Giemon Otete の完全スペック表。アーム・クローラー・HAT・電源・ソフトウェアの全仕様を掲載。" />
</svelte:head>

<!-- nav -->
<header class="sticky top-0 z-50 border-b border-[#2a2d3a] bg-[#0f1117]/90 backdrop-blur">
  <div class="mx-auto flex max-w-6xl items-center justify-between px-6 py-4">
    <a href="/" class="text-lg font-bold tracking-tight text-white">
      <span class="text-[#e85d04]">Giemon</span> Otete
    </a>
    <nav class="hidden gap-6 text-sm font-medium text-slate-300 md:flex">
      <a href="/product" class="text-white">製品</a>
      <a href="/assembly" class="hover:text-white transition-colors">組立</a>
      <a href="/firmware" class="hover:text-white transition-colors">ファームウェア</a>
      <a href="/education" class="hover:text-white transition-colors">教育</a>
      <a href="/buy" class="rounded bg-[#e85d04] px-4 py-1.5 text-white hover:bg-[#f48c06] transition-colors">購入する</a>
    </nav>
  </div>
</header>

<!-- breadcrumb -->
<div class="border-b border-[#2a2d3a] bg-[#0f1117]">
  <div class="mx-auto max-w-6xl px-6 py-3 text-sm text-slate-400">
    <a href="/" class="hover:text-white transition-colors">ホーム</a>
    <span class="mx-2">/</span>
    <a href="/product" class="hover:text-white transition-colors">製品詳細</a>
    <span class="mx-2">/</span>
    <span class="text-white">詳細スペック表</span>
  </div>
</div>

<main class="mx-auto max-w-5xl px-6 py-16 space-y-12">

  <div>
    <h1 class="mb-2 text-3xl font-bold text-white">詳細スペック表</h1>
    <p class="text-slate-400">Giemon Otete v1.0 — 全仕様</p>
  </div>

  {#each sections as section}
    <section>
      <h2 class="mb-4 border-l-4 border-[#e85d04] pl-4 text-xl font-bold text-white">{section.title}</h2>
      <div class="overflow-x-auto rounded-xl border border-[#2a2d3a]">
        <table class="w-full text-sm">
          <tbody>
            {#each section.rows as row, i}
              <tr class="border-b border-[#2a2d3a] last:border-0 {i % 2 === 0 ? 'bg-[#1a1d27]' : 'bg-[#0f1117]'} hover:bg-[#e85d04]/5 transition-colors">
                <td class="w-48 px-5 py-3 font-medium text-slate-400 lg:w-64">{row.spec}</td>
                <td class="px-5 py-3 text-slate-100">{row.value}</td>
              </tr>
            {/each}
          </tbody>
        </table>
      </div>
    </section>
  {/each}

  <div class="flex gap-4 pt-4">
    <a href="/product" class="rounded border border-[#2a2d3a] px-5 py-2 text-sm text-slate-300 hover:border-[#e85d04] hover:text-white transition-colors">
      ← 製品詳細に戻る
    </a>
    <a href="/product/bom" class="rounded border border-[#2a2d3a] px-5 py-2 text-sm text-slate-300 hover:border-[#e85d04] hover:text-white transition-colors">
      BOM・部品一覧 →
    </a>
  </div>

</main>

<footer class="border-t border-[#2a2d3a] bg-[#0a0d14] py-10 text-center text-xs text-slate-500">
  <p>© 2026 etzhayyim Japan 株式会社 — <a href="https://creativecommons.org/licenses/by-sa/4.0/" class="hover:text-white transition-colors">CC BY-SA 4.0</a> / Hardware CERN-OHL-S v2</p>
</footer>
