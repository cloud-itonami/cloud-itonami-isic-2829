<script lang="ts">
  const dhParams = [
    { joint: 'J1', a: 0, d: 85, alpha: 0, range: '±180°', servo: 'KRS-6013IHV', torque: '13.0 kg·cm' },
    { joint: 'J2', a: 0, d: 120, alpha: 90, range: '-10° 〜 +100°', servo: 'KRS-6013IHV', torque: '13.0 kg·cm' },
    { joint: 'J3', a: 160, d: 0, alpha: 0, range: '-10° 〜 +120°', servo: 'KRS-4014ICS', torque: '14.0 kg·cm' },
    { joint: 'J4', a: 40, d: 0, alpha: 90, range: '±90°', servo: 'KRS-4014ICS', torque: '14.0 kg·cm' },
    { joint: 'J5', a: 0, d: 80, alpha: -90, range: '±180°', servo: 'KRS-2350IHV', torque: '5.5 kg·cm' },
    { joint: 'J6', a: 0, d: 30, alpha: 90, range: '±180°', servo: 'KRS-2350IHV', torque: '5.5 kg·cm' },
  ];

  const crawlerSpecs = [
    { label: '全長 × 全幅 × 全高', value: '320 × 280 × 120 mm' },
    { label: 'トラック幅', value: '42 mm（タミヤ 70168 規格）' },
    { label: '最低地上高', value: '22 mm' },
    { label: '最大走行速度', value: '0.6 m/s（無負荷）' },
    { label: '最大登坂角', value: '20°' },
    { label: '最小信地旋回半径', value: '0（超信地旋回可能）' },
    { label: 'モーター', value: 'マブチ RS-380PH × 2' },
    { label: 'ドライバー IC', value: '東芝 TB6612FNG × 2' },
    { label: 'フレーム材質', value: 'AL6061-T6 アルマイト（黒）' },
  ];

  const hatChips = [
    { part: 'TB6612FNG × 2', role: 'クローラーモータードライバー', maker: '東芝', note: 'PWMA/B + AIN/BIN × 2ch' },
    { part: 'ICM-42688-P', role: '6軸 IMU (加速度 + ジャイロ)', maker: 'TDK', note: 'SPI1, 最大 8kHz ODR' },
    { part: 'VL53L4CX', role: 'ToF 距離センサ', maker: 'ST', note: 'I2C (0x29), 最大 6m' },
    { part: '74AHCT1G125', role: 'ICS3.5 半二重 UART バッファ', maker: 'TI', note: 'TX_EN GPIO17 制御' },
    { part: 'MAX3485E × 2', role: 'RS485 トランシーバー', maker: 'ADI', note: 'Futaba RS485 オルタナティブ用' },
    { part: 'CCG3-24-05S', role: 'DC-DC コンバーター 7.4V→5V', maker: 'TDK-Lambda', note: '3W 絶縁型 産業用' },
  ];

  const swStack = [
    { layer: 'ユーザーアプリ', color: '#e85d04', desc: 'Python / ROS2 Node (カスタムロジック)', icon: '👤' },
    { layer: 'ROS2 Humble', color: '#c44b03', desc: 'otete_ros2 パッケージ — bringup.launch.py', icon: '🤖' },
    { layer: 'MoveIt! 2', color: '#a33f02', desc: '軌道計画・衝突回避・IK プラグイン', icon: '🧠' },
    { layer: 'kinematics.py', color: '#7a2f01', desc: 'DLS 逆運動学ソルバー（numpy のみ）', icon: '📐' },
    { layer: 'ics_driver.py', color: '#521f01', desc: 'ICS3.5 半二重 UART / motor_driver.py', icon: '⚙️' },
    { layer: 'Otete HAT', color: '#2d1000', desc: 'RPi 5 GPIO + TB6612FNG + ICM-42688 + VL53L4CX', icon: '🔌' },
  ];

  const contents = [
    { item: 'クローラーベースユニット（組立済みフレーム）', qty: '1 式' },
    { item: 'ロボットアームユニット（バラ部品）', qty: '1 式' },
    { item: 'Otete HAT 基板', qty: '1 枚' },
    { item: 'KRS-6013IHV バスサーボ', qty: '2 個' },
    { item: 'KRS-4014ICS バスサーボ', qty: '2 個' },
    { item: 'KRS-2350IHV バスサーボ', qty: '2 個' },
    { item: 'Futaba S3003（グリッパー）', qty: '1 個' },
    { item: 'マブチ RS-380PH モーター', qty: '2 個' },
    { item: 'パナソニック NCR18650B × 8 セル（4S2P）', qty: '1 パック' },
    { item: 'M3 ネジ・ナット セット', qty: '各種' },
    { item: '組立マニュアル URL カード', qty: '1 枚' },
  ];
</script>

<svelte:head>
  <title>製品詳細 | Giemon Otete — 日本製 6軸アームクローラーロボットキット</title>
  <meta name="description" content="Giemon Otete の詳細仕様。6軸アーム DH パラメータ、クローラー性能、Otete HAT 回路、ソフトウェアスタック、梱包内容を掲載。" />
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
    <span class="text-white">製品詳細</span>
  </div>
</div>

<main class="mx-auto max-w-6xl px-6 py-16 space-y-24">

  <!-- page title -->
  <section>
    <div class="flex flex-wrap items-end gap-4 mb-6">
      <h1 class="text-4xl font-bold text-white">Giemon Otete</h1>
      <span class="rounded-full border border-[#e85d04]/40 bg-[#e85d04]/10 px-3 py-1 text-sm text-[#f48c06]">v1.0</span>
    </div>
    <p class="max-w-2xl text-lg text-slate-300 leading-relaxed">
      6軸アームとクローラーベースを組み合わせた日本製オープンハードウェアロボットキット。
      全国産部品・ROS2 Humble 対応・完全オープンソース。
    </p>
    <div class="mt-6 flex flex-wrap gap-3">
      <a href="/product/specs" class="rounded border border-[#2a2d3a] px-4 py-2 text-sm text-slate-300 hover:border-[#e85d04] hover:text-white transition-colors">
        詳細スペック表 →
      </a>
      <a href="/product/bom" class="rounded border border-[#2a2d3a] px-4 py-2 text-sm text-slate-300 hover:border-[#e85d04] hover:text-white transition-colors">
        BOM・部品一覧 →
      </a>
      <a href="https://github.com/etzhayyim/otete" target="_blank" rel="noopener"
         class="rounded border border-[#2a2d3a] px-4 py-2 text-sm text-slate-300 hover:border-[#e85d04] hover:text-white transition-colors">
        GitHub →
      </a>
    </div>
  </section>

  <!-- 3D view placeholder -->
  <section>
    <h2 class="mb-6 text-2xl font-bold text-white">3D ビュー</h2>
    <div class="relative overflow-hidden rounded-xl border border-[#2a2d3a] bg-[#1a1d27]" style="height:480px">
      <iframe
        src="/viewer.htm"
        title="Giemon Otete 3D ビューア"
        class="absolute inset-0 h-full w-full border-0"
        loading="lazy"
      ></iframe>
      <div class="absolute bottom-3 right-3 flex gap-2">
        <a href="/viewer.htm" target="_blank" rel="noopener"
           class="rounded border border-[#2a2d3a] bg-[#0f1117]/80 px-3 py-1.5 text-xs text-slate-300 backdrop-blur hover:border-[#e85d04] hover:text-white transition-colors">
          フルスクリーン
        </a>
        <a href="https://github.com/etzhayyim/otete/tree/main/cad"
           target="_blank" rel="noopener"
           class="rounded border border-[#2a2d3a] bg-[#0f1117]/80 px-3 py-1.5 text-xs text-slate-300 backdrop-blur hover:border-[#e85d04] hover:text-white transition-colors">
          STEP DL
        </a>
      </div>
    </div>
  </section>

  <!-- arm specs: DH params -->
  <section>
    <h2 class="mb-2 text-2xl font-bold text-white">アーム仕様</h2>
    <p class="mb-6 text-slate-400 text-sm">Modified Denavit-Hartenberg パラメータ</p>

    <div class="overflow-x-auto rounded-xl border border-[#2a2d3a]">
      <table class="w-full text-sm">
        <thead>
          <tr class="border-b border-[#2a2d3a] bg-[#1a1d27]">
            <th class="px-4 py-3 text-left font-semibold text-slate-300">Joint</th>
            <th class="px-4 py-3 text-right font-semibold text-slate-300">a (mm)</th>
            <th class="px-4 py-3 text-right font-semibold text-slate-300">d (mm)</th>
            <th class="px-4 py-3 text-right font-semibold text-slate-300">α (deg)</th>
            <th class="px-4 py-3 text-left font-semibold text-slate-300">θ 可動範囲</th>
            <th class="px-4 py-3 text-left font-semibold text-slate-300">サーボ</th>
            <th class="px-4 py-3 text-right font-semibold text-slate-300">最大トルク</th>
          </tr>
        </thead>
        <tbody>
          {#each dhParams as row, i}
            <tr class="border-b border-[#2a2d3a] {i % 2 === 0 ? 'bg-[#0f1117]' : 'bg-[#1a1d27]'} hover:bg-[#e85d04]/5 transition-colors">
              <td class="px-4 py-3 font-bold text-[#e85d04]">{row.joint}</td>
              <td class="px-4 py-3 text-right tabular-nums text-slate-200">{row.a}</td>
              <td class="px-4 py-3 text-right tabular-nums text-slate-200">{row.d}</td>
              <td class="px-4 py-3 text-right tabular-nums text-slate-200">{row.alpha}</td>
              <td class="px-4 py-3 text-slate-300">{row.range}</td>
              <td class="px-4 py-3 text-slate-300 font-mono text-xs">{row.servo}</td>
              <td class="px-4 py-3 text-right tabular-nums text-slate-200">{row.torque}</td>
            </tr>
          {/each}
        </tbody>
      </table>
    </div>

    <div class="mt-6 grid grid-cols-2 gap-4 sm:grid-cols-4">
      {#each [
        { label: '最大リーチ', value: '420 mm' },
        { label: '最小リーチ（折り畳み）', value: '85 mm' },
        { label: '可搬重量', value: '500 g' },
        { label: '繰り返し精度', value: '±2 mm' },
      ] as kv}
        <div class="rounded-lg border border-[#2a2d3a] bg-[#1a1d27] p-4">
          <div class="text-xs text-slate-400">{kv.label}</div>
          <div class="mt-1 text-xl font-bold text-white">{kv.value}</div>
        </div>
      {/each}
    </div>

    <!-- gripper -->
    <div class="mt-6 rounded-xl border border-[#2a2d3a] bg-[#1a1d27] p-6">
      <h3 class="mb-4 font-semibold text-white">グリッパー仕様</h3>
      <div class="grid grid-cols-2 gap-x-8 gap-y-2 text-sm sm:grid-cols-4">
        {#each [
          { k: '最大開口幅', v: '80 mm' },
          { k: '把持力', v: '約 8 N' },
          { k: '指長', v: '65 mm' },
          { k: '駆動', v: 'Futaba S3003（PWM）' },
          { k: '材質', v: 'AL6061 + TPU インサート' },
          { k: '閉口幅', v: '0 mm（完全閉）' },
        ] as p}
          <div>
            <span class="text-slate-400">{p.k}</span>
            <span class="ml-2 font-medium text-slate-100">{p.v}</span>
          </div>
        {/each}
      </div>
    </div>
  </section>

  <!-- crawler specs -->
  <section>
    <h2 class="mb-6 text-2xl font-bold text-white">クローラー仕様</h2>
    <div class="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
      {#each crawlerSpecs as spec}
        <div class="flex items-start gap-3 rounded-lg border border-[#2a2d3a] bg-[#1a1d27] px-4 py-3">
          <span class="mt-0.5 h-2 w-2 shrink-0 rounded-full bg-[#e85d04]"></span>
          <div class="min-w-0">
            <div class="text-xs text-slate-400">{spec.label}</div>
            <div class="text-sm font-medium text-slate-100">{spec.value}</div>
          </div>
        </div>
      {/each}
    </div>
  </section>

  <!-- HAT specs -->
  <section>
    <h2 class="mb-2 text-2xl font-bold text-white">Otete HAT 仕様</h2>
    <p class="mb-6 text-slate-400 text-sm">Raspberry Pi HAT 規格 (65 × 56.5 mm) — P-Ban.com 製造 / 2 層 FR4 1.6 mm HASL</p>

    <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
      {#each hatChips as chip}
        <div class="rounded-xl border border-[#2a2d3a] bg-[#1a1d27] p-5">
          <div class="mb-1 font-mono text-sm font-semibold text-[#e85d04]">{chip.part}</div>
          <div class="text-sm font-medium text-white">{chip.role}</div>
          <div class="mt-1 text-xs text-slate-400">{chip.maker} — {chip.note}</div>
        </div>
      {/each}
    </div>

    <div class="mt-6 rounded-xl border border-[#2a2d3a] bg-[#1a1d27] p-6">
      <h3 class="mb-4 text-sm font-semibold text-slate-300">接続インターフェース</h3>
      <div class="grid gap-2 text-sm sm:grid-cols-2">
        {#each [
          { cn: 'CN1', desc: 'ICS バス 4P (サーボチェーン)' },
          { cn: 'CN2/CN3', desc: 'RS485 × 2ch (Futaba 代替)' },
          { cn: 'CN4', desc: '左クローラーモーター 2P' },
          { cn: 'CN5', desc: '右クローラーモーター 2P' },
          { cn: 'J1', desc: 'RPi 40-pin HAT ヘッダー' },
          { cn: 'J2', desc: 'DC バレル (5.5/2.1 mm) — 7.4 V 入力' },
        ] as cn}
          <div class="flex gap-3">
            <span class="w-16 shrink-0 font-mono text-xs font-bold text-[#e85d04]">{cn.cn}</span>
            <span class="text-slate-300">{cn.desc}</span>
          </div>
        {/each}
      </div>
    </div>
  </section>

  <!-- software stack -->
  <section>
    <h2 class="mb-6 text-2xl font-bold text-white">ソフトウェアスタック</h2>
    <div class="space-y-2">
      {#each swStack as layer}
        <div class="flex items-center gap-4 rounded-lg border border-[#2a2d3a] px-5 py-4"
             style="background: color-mix(in srgb, {layer.color} 8%, #1a1d27);">
          <span class="text-2xl">{layer.icon}</span>
          <div class="min-w-0">
            <div class="font-semibold text-white">{layer.layer}</div>
            <div class="text-sm text-slate-400">{layer.desc}</div>
          </div>
        </div>
      {/each}
    </div>
    <div class="mt-4 text-xs text-slate-500">
      ↑ 上位層がユーザーに近い。各層は独立して使用可能。
    </div>
  </section>

  <!-- package contents -->
  <section>
    <h2 class="mb-6 text-2xl font-bold text-white">梱包内容</h2>
    <div class="rounded-xl border border-[#2a2d3a] bg-[#1a1d27] overflow-hidden">
      <table class="w-full text-sm">
        <thead>
          <tr class="border-b border-[#2a2d3a] bg-[#0f1117]">
            <th class="px-5 py-3 text-left font-semibold text-slate-300">部品・内容物</th>
            <th class="px-5 py-3 text-right font-semibold text-slate-300">数量</th>
          </tr>
        </thead>
        <tbody>
          {#each contents as item, i}
            <tr class="border-b border-[#2a2d3a] last:border-0 {i % 2 === 0 ? '' : 'bg-[#0f1117]/40'} hover:bg-[#e85d04]/5 transition-colors">
              <td class="px-5 py-3 text-slate-200">{item.item}</td>
              <td class="px-5 py-3 text-right tabular-nums text-slate-300">{item.qty}</td>
            </tr>
          {/each}
        </tbody>
      </table>
    </div>
    <p class="mt-3 text-xs text-slate-500">
      ※ Raspberry Pi 5 は別途ご用意ください。カメラモジュール (IMX477) は付属しません。
    </p>
  </section>

  <!-- CTA -->
  <section class="rounded-2xl border border-[#e85d04]/20 bg-gradient-to-br from-[#1a1010] to-[#1a1d27] p-10 text-center">
    <h2 class="mb-3 text-2xl font-bold text-white">今すぐ入手する</h2>
    <p class="mb-8 text-slate-300">Standard キット ¥98,780（税込）— Makuake 早割 ¥89,800</p>
    <div class="flex flex-wrap justify-center gap-4">
      <a href="/buy" class="rounded-lg bg-[#e85d04] px-8 py-3 font-semibold text-white hover:bg-[#f48c06] transition-colors">
        購入ページへ
      </a>
      <a href="/assembly" class="rounded-lg border border-[#2a2d3a] px-8 py-3 font-semibold text-slate-200 hover:border-[#e85d04] hover:text-white transition-colors">
        組立を見る
      </a>
    </div>
  </section>

</main>

<!-- footer -->
<footer class="border-t border-[#2a2d3a] bg-[#0a0d14] py-10 text-center text-xs text-slate-500">
  <p>© 2026 etzhayyim Japan 株式会社 — <a href="https://creativecommons.org/licenses/by-sa/4.0/" class="hover:text-white transition-colors">CC BY-SA 4.0</a> / Hardware CERN-OHL-S v2</p>
</footer>
