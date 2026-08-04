<script lang="ts">
  const categories = [
    {
      id: 'servo',
      title: 'アーム駆動系 — サーボモーター',
      subtotal: '¥37,180',
      rows: [
        { no: 'S1', name: 'バスサーボ 大 (J1 旋回 / J2 肩ピッチ)', maker: '近藤科学', pn: 'KRS-6013IHV', spec: '35 kg·cm, ICS3.5, 7.4–12V', qty: 2, price: '¥8,200', origin: '日本' },
        { no: 'S2', name: 'バスサーボ 中 (J3 肘 / J4 手首ピッチ)', maker: '近藤科学', pn: 'KRS-4014ICS', spec: '20 kg·cm, ICS3.5, 6.0–8.4V', qty: 2, price: '¥5,800', origin: '日本' },
        { no: 'S3', name: 'バスサーボ 小 (J5 手首ロール / J6 Yaw)', maker: '近藤科学', pn: 'KRS-2350IHV', spec: '8 kg·cm, ICS3.5, 6.0–7.4V', qty: 2, price: '¥4,100', origin: '日本' },
        { no: 'S4', name: 'グリッパーサーボ', maker: '双葉電子工業', pn: 'S3003', spec: '3.2 kg·cm, PWM, 4.8–6V', qty: 1, price: '¥980', origin: '日本' },
      ],
    },
    {
      id: 'crawler',
      title: 'クローラー駆動系',
      subtotal: '¥13,840',
      rows: [
        { no: 'C1', name: 'DC モーター（左右トラック）', maker: 'マブチモーター', pn: 'RS-550PC-7525', spec: '12V, 14,400 rpm, 550 サイズ', qty: 2, price: '¥1,200', origin: '日本' },
        { no: 'C2', name: 'エンコーダ付きギアドモーター', maker: '日本電産 (Nidec)', pn: 'DC12V 100rpm', spec: '12V, 100 rpm, エンコーダ付き', qty: 2, price: '¥2,800', origin: '日本' },
        { no: 'C3', name: 'トラックユニット（左右）', maker: 'タミヤ', pn: '70168', spec: '幅 42 mm、ゴムトラック', qty: 2, price: '¥1,200', origin: '日本' },
        { no: 'C4', name: 'スプロケット（駆動 / 従動 各 2）', maker: 'Meviy (ミスミ)', pn: 'AL カスタム', spec: 'Meviy CNC 切削, AL6061', qty: 4, price: '¥800', origin: '日本' },
        { no: 'C5', name: 'モータードライバー IC', maker: '東芝デバイス&ストレージ', pn: 'TB6612FNG', spec: 'H ブリッジ × 2ch, 1.2A × 2, 15V', qty: 2, price: '¥120', origin: '日本' },
      ],
    },
    {
      id: 'frame',
      title: 'アーム構造・フレーム',
      subtotal: '¥17,100',
      rows: [
        { no: 'F1', name: 'アルミ押出し材 15 × 15 mm', maker: 'ミスミ', pn: 'HFSB3-15-300', spec: '3 mm 溝, 300 mm, A6063', qty: 6, price: '¥380', origin: '日本' },
        { no: 'F2', name: 'アルミブラケット L 字', maker: 'ミスミ', pn: 'HBLSS3', spec: '15 mm 溝用, A6063', qty: 12, price: '¥95', origin: '日本' },
        { no: 'F3', name: 'アーム関節ブラケット（J1 台座）', maker: 'Meviy 加工', pn: 'カスタム', spec: 'AL6061, CNC 切削, t=6 mm', qty: 1, price: '¥2,800', origin: '日本' },
        { no: 'F4', name: 'アーム関節ブラケット（J2–J3 リンク）', maker: 'Meviy 加工', pn: 'カスタム', spec: 'AL6061, CNC 切削', qty: 2, price: '¥1,800', origin: '日本' },
        { no: 'F5', name: '手首フランジプレート', maker: 'Meviy 加工', pn: 'カスタム', spec: 'AL6061, t=4 mm', qty: 1, price: '¥1,200', origin: '日本' },
        { no: 'F6', name: 'グリッパー指（左右）', maker: 'Meviy 加工', pn: 'カスタム', spec: 'AL6061 + TPU インサート', qty: 2, price: '¥900', origin: '日本' },
        { no: 'F7', name: 'ベアリング 608ZZ / 693ZZ', maker: 'NTN / JTEKT', pn: '608ZZ, 693ZZ', spec: '内径 8 mm / 3 mm 深溝玉軸受', qty: 10, price: '¥180', origin: '日本' },
        { no: 'F8', name: 'シャフト（SUS304 Φ8 mm）', maker: 'ミスミ', pn: 'SRHS8-150', spec: 'SUS304, Φ8 mm × 150 mm', qty: 4, price: '¥420', origin: '日本' },
        { no: 'F9', name: 'M3 六角ボルト・ナットセット', maker: 'ダイドーハント / ミスミ', pn: 'M3 各種', spec: 'SUS304, M3 × 8 / 12 / 16 / 20 mm', qty: 1, price: '¥800', origin: '日本' },
      ],
    },
    {
      id: 'electronics',
      title: 'Otete HAT・電装系',
      subtotal: '¥18,500（目安）',
      rows: [
        { no: 'E1', name: 'Otete HAT PCB', maker: 'P-Ban.com', pn: 'カスタム設計', spec: '2 層 FR4, 65 × 56.5 mm, HASL', qty: 1, price: '¥4,500', origin: '日本' },
        { no: 'E2', name: 'TB6612FNG (ハンダ実装済み)', maker: '東芝', pn: 'TB6612FNG', spec: 'HAT 基板実装', qty: 2, price: '込', origin: '日本' },
        { no: 'E3', name: 'ICM-42688-P 6 軸 IMU', maker: 'TDK', pn: 'ICM-42688-P', spec: 'SPI1 実装', qty: 1, price: '込', origin: '日本' },
        { no: 'E4', name: 'VL53L4CX ToF センサ', maker: 'STMicro', pn: 'VL53L4CX', spec: 'I2C 0x29 実装', qty: 1, price: '込', origin: '—' },
        { no: 'E5', name: 'CCG3-24-05S DC-DC コンバーター', maker: 'TDK-Lambda', pn: 'CCG3-24-05S', spec: '7.4V → 5V, 3W 絶縁型', qty: 1, price: '込', origin: '日本' },
        { no: 'E6', name: '18650 バッテリーパック 4S2P', maker: 'パナソニック', pn: 'NCR18650B × 8', spec: '14.8V, 6,800 mAh, BMS 内蔵', qty: 1, price: '¥6,800', origin: '日本' },
        { no: 'E7', name: 'カメラモジュール（オプション）', maker: 'ラズパイ財団 / Sony', pn: 'Camera Module 3', spec: 'IMX708, 12MP', qty: 1, price: '¥5,000', origin: '別途' },
      ],
    },
  ];

  const githubBase = 'https://github.com/etzhayyim/otete';
</script>

<svelte:head>
  <title>BOM・部品一覧 | Giemon Otete — 日本製 6軸アームクローラーロボットキット</title>
  <meta name="description" content="Giemon Otete の公開 BOM。全国産部品リスト・型番・調達先・CAD/KiCad ソースファイルへのリンクを掲載。" />
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
    <span class="text-white">BOM・部品一覧</span>
  </div>
</div>

<main class="mx-auto max-w-6xl px-6 py-16 space-y-14">

  <div>
    <h1 class="mb-2 text-3xl font-bold text-white">BOM・部品一覧</h1>
    <p class="mb-6 text-slate-400">Giemon Otete v1.0 — オープンハードウェア公開 BOM</p>

    <!-- open source files -->
    <div class="rounded-xl border border-[#e85d04]/20 bg-[#1a1d27] p-6">
      <h2 class="mb-4 text-sm font-semibold text-white">ソースファイル (GitHub)</h2>
      <div class="flex flex-wrap gap-3">
        {#each [
          { label: 'STEP アセンブリ (CAD)', path: '/cad/Otete_JP_ASSY.step' },
          { label: 'KiCad プロジェクト (HAT)', path: '/pcb/Otete HAT' },
          { label: 'BOM CSV (全部品)', path: '/docs/BOM-v1.csv' },
          { label: '機械設計仕様書 PDF', path: '/docs/mechanical-spec-v1.pdf' },
          { label: 'HAT 回路図 PDF', path: '/docs/hat-schematic-v1.pdf' },
        ] as f}
          <a href="{githubBase}{f.path}" target="_blank" rel="noopener"
             class="flex items-center gap-2 rounded border border-[#2a2d3a] px-4 py-2 text-sm text-slate-300 hover:border-[#e85d04] hover:text-white transition-colors">
            <svg class="h-4 w-4" fill="currentColor" viewBox="0 0 24 24"><path d="M12 0C5.37 0 0 5.37 0 12c0 5.3 3.438 9.8 8.205 11.385.6.113.82-.258.82-.577 0-.285-.01-1.04-.015-2.04-3.338.724-4.042-1.61-4.042-1.61C4.422 18.07 3.633 17.7 3.633 17.7c-1.087-.744.084-.729.084-.729 1.205.084 1.838 1.236 1.838 1.236 1.07 1.835 2.809 1.305 3.495.998.108-.776.417-1.305.76-1.605-2.665-.3-5.466-1.332-5.466-5.93 0-1.31.465-2.38 1.235-3.22-.135-.303-.54-1.523.105-3.176 0 0 1.005-.322 3.3 1.23.96-.267 1.98-.399 3-.405 1.02.006 2.04.138 3 .405 2.28-1.552 3.285-1.23 3.285-1.23.645 1.653.24 2.873.12 3.176.765.84 1.23 1.91 1.23 3.22 0 4.61-2.805 5.625-5.475 5.92.42.36.81 1.096.81 2.22 0 1.606-.015 2.896-.015 3.286 0 .315.21.69.825.57C20.565 21.795 24 17.295 24 12c0-6.63-5.37-12-12-12"/></svg>
            {f.label}
          </a>
        {/each}
      </div>
      <p class="mt-3 text-xs text-slate-500">
        ライセンス: ハードウェア CERN-OHL-S v2 / ソフトウェア Apache 2.0 / ドキュメント CC BY-SA 4.0
      </p>
    </div>
  </div>

  <!-- BOM tables by category -->
  {#each categories as cat}
    <section id={cat.id}>
      <div class="mb-4 flex items-baseline justify-between">
        <h2 class="border-l-4 border-[#e85d04] pl-4 text-xl font-bold text-white">{cat.title}</h2>
        <span class="text-sm font-semibold text-[#f48c06]">小計 {cat.subtotal}</span>
      </div>
      <div class="overflow-x-auto rounded-xl border border-[#2a2d3a]">
        <table class="w-full text-xs">
          <thead>
            <tr class="border-b border-[#2a2d3a] bg-[#0f1117]">
              <th class="px-4 py-3 text-left font-semibold text-slate-400">No.</th>
              <th class="px-4 py-3 text-left font-semibold text-slate-400">部品名</th>
              <th class="px-4 py-3 text-left font-semibold text-slate-400">メーカー</th>
              <th class="px-4 py-3 text-left font-semibold text-slate-400 hidden md:table-cell">型番</th>
              <th class="px-4 py-3 text-left font-semibold text-slate-400 hidden lg:table-cell">仕様</th>
              <th class="px-4 py-3 text-right font-semibold text-slate-400">数量</th>
              <th class="px-4 py-3 text-right font-semibold text-slate-400">単価</th>
              <th class="px-4 py-3 text-center font-semibold text-slate-400 hidden sm:table-cell">国産</th>
            </tr>
          </thead>
          <tbody>
            {#each cat.rows as row, i}
              <tr class="border-b border-[#2a2d3a] last:border-0 {i % 2 === 0 ? 'bg-[#1a1d27]' : 'bg-[#0f1117]'} hover:bg-[#e85d04]/5 transition-colors">
                <td class="px-4 py-3 font-mono font-bold text-[#e85d04]">{row.no}</td>
                <td class="px-4 py-3 text-slate-200">{row.name}</td>
                <td class="px-4 py-3 text-slate-300">{row.maker}</td>
                <td class="px-4 py-3 font-mono text-slate-400 hidden md:table-cell">{row.pn}</td>
                <td class="px-4 py-3 text-slate-400 hidden lg:table-cell">{row.spec}</td>
                <td class="px-4 py-3 text-right tabular-nums text-slate-200">{row.qty}</td>
                <td class="px-4 py-3 text-right tabular-nums text-slate-200">{row.price}</td>
                <td class="px-4 py-3 text-center hidden sm:table-cell">
                  {#if row.origin === '日本'}
                    <span class="text-green-400">🇯🇵</span>
                  {:else if row.origin === '別途'}
                    <span class="text-slate-500">—</span>
                  {:else}
                    <span class="text-slate-500">{row.origin}</span>
                  {/if}
                </td>
              </tr>
            {/each}
          </tbody>
        </table>
      </div>
    </section>
  {/each}

  <!-- total -->
  <div class="rounded-xl border border-[#e85d04]/30 bg-gradient-to-r from-[#1a1010] to-[#1a1d27] p-6">
    <div class="flex flex-wrap items-center justify-between gap-4">
      <div>
        <div class="text-sm text-slate-400">BOM 原価合計（目安）</div>
        <div class="text-3xl font-bold text-white">¥28,000 <span class="text-lg font-normal text-slate-400">税抜</span></div>
        <div class="mt-1 text-xs text-slate-500">原価率 31% (販売価格 ¥89,800 基準)</div>
      </div>
      <div class="text-right">
        <div class="text-sm font-medium text-slate-300">国産率</div>
        <div class="text-2xl font-bold text-[#e85d04]">95%</div>
        <div class="text-xs text-slate-500">SBC (RPi 5) を除く全部品</div>
      </div>
    </div>
  </div>

  <div class="flex gap-4">
    <a href="/product" class="rounded border border-[#2a2d3a] px-5 py-2 text-sm text-slate-300 hover:border-[#e85d04] hover:text-white transition-colors">
      ← 製品詳細に戻る
    </a>
    <a href="/product/specs" class="rounded border border-[#2a2d3a] px-5 py-2 text-sm text-slate-300 hover:border-[#e85d04] hover:text-white transition-colors">
      詳細スペック表 →
    </a>
  </div>

</main>

<footer class="border-t border-[#2a2d3a] bg-[#0a0d14] py-10 text-center text-xs text-slate-500">
  <p>© 2026 etzhayyim Japan 株式会社 — <a href="https://creativecommons.org/licenses/by-sa/4.0/" class="hover:text-white transition-colors">CC BY-SA 4.0</a> / Hardware CERN-OHL-S v2</p>
</footer>
