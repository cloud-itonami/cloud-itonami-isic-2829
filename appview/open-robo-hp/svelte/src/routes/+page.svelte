<script lang="ts">
  let mobileOpen = $state(false);
  let selectedModel = $state<'arm' | 'hitogata' | 'caterpillar'>('arm');

  const modelTabs = [
    { id: 'arm',         label: 'Otete',  emoji: '🦾', desc: '6軸アーム + クローラー' },
    { id: 'hitogata',    label: 'Hitogata',    emoji: '🤖', desc: '2足歩行ヒューマノイド' },
    { id: 'caterpillar', label: 'Caterpillar', emoji: '🚜', desc: '重装甲 UGV + LiDAR' },
  ] as const;

  const features = [
    {
      icon: '🇯🇵',
      title: '全国産部品',
      body: '近藤科学サーボ・ミスミフレーム・パナソニック電池・P-Ban.com 基板。サプライチェーンを国内で完結。',
    },
    {
      icon: '🤖',
      title: 'ROS2 ネイティブ対応',
      body: 'ROS2 Humble + MoveIt! 対応。大学・研究機関レベルの制御スタックを手元で学べる Modified D-H 設計。',
    },
    {
      icon: '🔓',
      title: '完全オープンソース',
      body: 'ハードウェア STEP/KiCad、ファームウェア、ROS2 パッケージをすべて GitHub 公開。',
    },
  ];

  const specs = [
    { label: '自由度', value: '6軸 + グリッパー' },
    { label: '最大リーチ', value: '420 mm' },
    { label: '可搬重量', value: '500 g' },
    { label: '繰り返し精度', value: '±2 mm' },
    { label: '全長 × 全幅', value: '320 × 280 mm' },
    { label: '最大傾斜走行', value: '20°' },
    { label: 'トラック幅', value: '42 mm' },
    { label: '最低地上高', value: '22 mm' },
    { label: '制御 SBC', value: 'Raspberry Pi 5 (4 GB)' },
    { label: 'サーボ通信', value: 'ICS3.5 (近藤科学)' },
    { label: 'OS / ROS', value: 'Ubuntu 22.04 / ROS2 Humble' },
    { label: '電源', value: 'パナソニック NCR18650B × 4S2P' },
  ];

  const makers = [
    { name: '近藤科学', role: 'バスサーボ ICS3.5', location: '東京', note: '日本ロボットホビー40年の信頼' },
    { name: 'ミスミ / Meviy', role: 'AL6061 フレーム・CNC 切削', location: '東京', note: '翌日納品の国内精密加工' },
    { name: 'パナソニック', role: '18650 電池セル', location: '大阪', note: '世界最高水準エネルギー密度' },
    { name: 'P-Ban.com', role: 'PCB 設計・製造', location: '東京', note: '国内基板で修理・改造しやすく' },
    { name: 'TDK ラムダ', role: 'DC-DC 産業用電源', location: '東京', note: '教育現場の安全性を最優先' },
    { name: 'タミヤ', role: 'ゴムトラック (TC-01 互換)', location: '静岡', note: '実績ある走行モジュール' },
  ];

  const ecosystem = [
    { label: 'GitHub', desc: 'HW / FW / ROS2 パッケージ全公開', link: 'https://github.com/etzhayyim/otete' },
    { label: 'ROS2 Humble', desc: 'ノード・トピック・アクション完全対応', link: null },
    { label: 'MoveIt! 2', desc: '軌道計画・衝突回避', link: null },
    { label: 'OpenCV + IMX477', desc: 'Sony 12MP カメラ物体認識', link: null },
    { label: 'Stable-Baselines3', desc: 'Gymnasium 強化学習実験', link: null },
    { label: 'tsukuru.etzhayyim.com', desc: 'B2B 直販・法人見積', link: 'https://tsukuru.etzhayyim.com' },
  ];

  const plans = [
    {
      name: 'Standard',
      price: '¥98,780',
      priceNote: '税込',
      earlyPrice: '¥89,800',
      earlyNote: 'Makuake 早割',
      features: ['本体キット一式', '組立マニュアル PDF', 'ファームウェア + ROS2 パッケージ', 'コミュニティサポート'],
      cta: 'Amazon で購入',
      ctaHref: '/buy',
      highlight: false,
    },
    {
      name: 'Education 3-pack',
      price: '¥269,500',
      priceNote: '税込 / 3 台',
      earlyPrice: null,
      earlyNote: '教育機関向け',
      features: ['本体キット × 3 台', '授業カリキュラム PDF (全12回)', '優先メールサポート', '納品書・領収書対応'],
      cta: '見積を依頼',
      ctaHref: 'mailto:sales@etzhayyim.com',
      highlight: true,
    },
    {
      name: 'HAT 単体',
      price: '¥14,800',
      priceNote: '税込',
      earlyPrice: null,
      earlyNote: 'RPi 5 拡張基板のみ',
      features: ['Otete HAT 基板', 'KiCad ソース公開', 'Python ドライバライブラリ', '回路図 PDF'],
      cta: 'Amazon で購入',
      ctaHref: '/buy',
      highlight: false,
    },
  ];
</script>

<svelte:head>
  <title>Giemon Otete — 日本製 6軸アームクローラーロボットキット</title>
  <script type="application/ld+json">
    {JSON.stringify({
      "@context": "https://schema.org",
      "@type": "Product",
      "name": "Giemon Otete",
      "description": "6軸ロボットアームとクローラー走行ベースを組み合わせた日本製組み立てキット。ROS2対応・全国産部品。",
      "brand": { "@type": "Brand", "name": "Giemon" },
      "offers": {
        "@type": "Offer",
        "price": "98780",
        "priceCurrency": "JPY",
        "availability": "https://schema.org/PreOrder",
        "url": "https://giemon.etzhayyim.com/buy"
      }
    })}
  </script>
</svelte:head>

<!-- ─── Nav ─── -->
<header class="sticky top-0 z-50 bg-[#0d1117]/90 backdrop-blur-md border-b border-[#21262d]">
  <div class="max-w-6xl mx-auto px-5 h-14 flex items-center justify-between">
    <a href="/" class="flex items-center gap-2 font-bold text-white text-base">
      <span class="text-2xl">🤖</span>
      <span>Giemon <span class="text-[#e85d04]">Otete</span></span>
    </a>
    <nav class="hidden md:flex items-center gap-6 text-[13px] text-slate-400">
      <a href="#features" class="hover:text-white transition-colors">特長</a>
      <a href="#specs" class="hover:text-white transition-colors">仕様</a>
      <a href="#japan-made" class="hover:text-white transition-colors">日本製</a>
      <a href="#ecosystem" class="hover:text-white transition-colors">エコシステム</a>
      <a href="#pricing" class="hover:text-white transition-colors">価格</a>
      <a href="/buy" class="ml-1 px-4 py-1.5 bg-[#e85d04] hover:bg-[#f48c06] text-white font-semibold rounded-full transition-colors">
        購入する
      </a>
    </nav>
    <button
      class="md:hidden p-1 text-slate-400 hover:text-white"
      onclick={() => (mobileOpen = !mobileOpen)}
      aria-label="メニュー"
    >
      <svg class="w-6 h-6" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
        {#if mobileOpen}
          <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
        {:else}
          <path stroke-linecap="round" stroke-linejoin="round" d="M4 6h16M4 12h16M4 18h16" />
        {/if}
      </svg>
    </button>
  </div>
  {#if mobileOpen}
    <div class="md:hidden bg-[#161b22] border-t border-[#21262d] px-5 py-3 flex flex-col gap-2 text-sm">
      {#each [['特長','#features'],['仕様','#specs'],['日本製','#japan-made'],['エコシステム','#ecosystem'],['価格','#pricing']] as [l,h]}
        <a {href} class="text-slate-400 hover:text-white py-1.5" onclick={() => (mobileOpen = false)}>{l}</a>
      {/each}
      <a href="/buy" class="mt-1 text-[#e85d04] font-semibold py-1.5" onclick={() => (mobileOpen = false)}>購入する →</a>
    </div>
  {/if}
</header>

<!-- ─── ① Hero ─── -->
<section class="relative min-h-[90vh] flex flex-col items-center justify-center text-center px-5 overflow-hidden bg-[#0d1117]">
  <div class="absolute inset-0 bg-[radial-gradient(ellipse_80%_50%_at_50%_-10%,rgba(232,93,4,0.12),transparent)] pointer-events-none"></div>
  <div class="absolute inset-0 opacity-[0.035] pointer-events-none"
    style="background-image:linear-gradient(#e85d04 1px,transparent 1px),linear-gradient(90deg,#e85d04 1px,transparent 1px);background-size:48px 48px;"></div>

  <span class="inline-flex items-center gap-1.5 px-3 py-1 rounded-full border border-[#e85d04]/35 text-[#f48c06] text-[11px] font-semibold uppercase tracking-widest mb-6">
    🇯🇵 Made in Japan — クラウドファンディング準備中
  </span>

  <h1 class="text-[clamp(2.5rem,8vw,4.5rem)] font-extrabold text-white leading-[1.08] tracking-tight max-w-3xl">
    ねじを締めれば、<br />
    <span class="text-[#e85d04]">ロボットと話せる。</span>
  </h1>
  <p class="mt-5 text-[clamp(1rem,2.5vw,1.2rem)] text-slate-400 max-w-lg leading-relaxed">
    日本の部品だけで作った教育用ロボット組み立てキット。<br />
    6軸アーム + クローラー + Raspberry Pi 5 + ROS2。
  </p>

  <div class="mt-8 flex flex-wrap gap-3 justify-center">
    <a href="#pricing" class="px-7 py-3 bg-[#e85d04] hover:bg-[#f48c06] text-white font-bold rounded-full transition-colors shadow-lg shadow-orange-900/25 text-sm">
      購入する・支援する
    </a>
    <a href="#features" class="px-7 py-3 border border-[#30363d] hover:border-slate-500 text-slate-300 hover:text-white font-medium rounded-full transition-colors text-sm">
      詳しく見る →
    </a>
  </div>

  <div class="mt-14 grid grid-cols-2 sm:grid-cols-4 gap-x-8 gap-y-4 max-w-xl w-full">
    {#each [['6軸','ロボットアーム'],['420mm','最大リーチ'],['ROS2','Humble 対応'],['全国産','部品・製造']] as [v,l]}
      <div class="text-center">
        <div class="text-2xl font-bold text-white">{v}</div>
        <div class="text-[11px] text-slate-500 mt-0.5">{l}</div>
      </div>
    {/each}
  </div>
</section>

<!-- ─── ② Key Features ─── -->
<section class="py-20 px-5 bg-[#0d1117] border-t border-[#21262d]" id="features">
  <div class="max-w-5xl mx-auto">
    <h2 class="text-2xl sm:text-3xl font-bold text-white text-center mb-2">なぜ Giemon Otete か</h2>
    <p class="text-slate-500 text-center text-sm mb-12">3つの核心価値</p>
    <div class="grid sm:grid-cols-3 gap-5">
      {#each features as f}
        <div class="bg-[#161b22] border border-[#21262d] hover:border-[#e85d04]/50 rounded-2xl p-6 transition-colors">
          <div class="text-4xl mb-4">{f.icon}</div>
          <h3 class="text-white font-semibold text-base mb-2">{f.title}</h3>
          <p class="text-slate-400 text-sm leading-relaxed">{f.body}</p>
        </div>
      {/each}
    </div>
  </div>
</section>

<!-- ─── ③ Product Demo ─── -->
<section class="py-20 px-5 bg-[#0a0d14] border-t border-[#21262d]">
  <div class="max-w-5xl mx-auto">
    <div class="text-[#e85d04] text-[11px] font-bold uppercase tracking-widest text-center mb-3">製品ラインナップ</div>
    <h2 class="text-2xl sm:text-3xl font-bold text-white text-center mb-2">Giemon ロボットシリーズ</h2>
    <p class="text-slate-500 text-sm text-center mb-8">3D モデルを回してみよう</p>

    <!-- model tabs -->
    <div class="flex justify-center gap-2 mb-6 flex-wrap">
      {#each modelTabs as tab}
        <button
          onclick={() => (selectedModel = tab.id)}
          class={[
            'px-4 py-2 rounded-full text-sm font-medium transition-all border',
            selectedModel === tab.id
              ? 'bg-[#e85d04] border-[#e85d04] text-white shadow-lg shadow-orange-900/25'
              : 'bg-transparent border-[#30363d] text-slate-400 hover:border-slate-500 hover:text-white'
          ].join(' ')}
        >
          {tab.emoji} {tab.label}
          <span class="ml-1.5 text-[10px] opacity-70">{tab.desc}</span>
        </button>
      {/each}
    </div>

    <div class="grid md:grid-cols-2 gap-10 items-center">
      <!-- 3D viewer -->
      <div class="relative bg-[#161b22] border border-[#21262d] hover:border-[#e85d04]/50 rounded-2xl overflow-hidden transition-colors" style="aspect-ratio:16/10">
        <iframe
          src={`/viewer.htm?model=${selectedModel}`}
          title={`Giemon ${modelTabs.find(t => t.id === selectedModel)?.label} 3D Viewer`}
          class="absolute inset-0 w-full h-full border-0"
          loading="lazy"
          allow="accelerometer"
        ></iframe>
        <div class="absolute bottom-2 right-3 text-[10px] text-slate-600 pointer-events-none select-none">WebGPU / WebGL2</div>
      </div>

      <!-- model description -->
      {#if selectedModel === 'arm'}
        <div>
          <h3 class="text-xl font-bold text-white mb-3">Otete</h3>
          <p class="text-slate-400 text-sm leading-relaxed mb-4">
            HiWonder の競合品とは違い、Giemon Otete は「ほぼ組み立て済み」ではなく、
            <strong class="text-slate-200">全部品をゼロから組む</strong>中級〜上級向けキット。
            組み立てを通じて産業ロボットの設計思想を体で理解できます。
          </p>
          <ul class="space-y-2">
            {#each ['6軸アーム + グリッパー (可搬 500g)','クローラー不整地走行 (最大 20° 傾斜)','RViz によるリアルタイム姿勢可視化','NumPy 逆運動学で任意座標への移動','Sony IMX477 + OpenCV 物体認識','Gymnasium + SB3 強化学習制御'] as item}
              <li class="flex items-start gap-2 text-slate-400 text-sm">
                <span class="text-[#e85d04] mt-0.5 shrink-0">✓</span>{item}
              </li>
            {/each}
          </ul>
        </div>
      {:else if selectedModel === 'hitogata'}
        <div>
          <h3 class="text-xl font-bold text-white mb-3">Hitogata <span class="text-[11px] text-slate-500 font-normal ml-1">Coming Soon</span></h3>
          <p class="text-slate-400 text-sm leading-relaxed mb-4">
            全高 285mm の 2 足歩行ヒューマノイド。近藤科学 ICS3.5 バスサーボ × 17軸で
            スムーズな全身動作を実現。Giemon Otete と同じ RPi 5 + ROS2 スタック。
          </p>
          <ul class="space-y-2">
            {#each ['17軸フルボディ (脚 × 10 + 腕 × 6 + 頭 × 1)','全高 285 mm / 重量 約 800 g (目標)','歩行・起き上がり・ダンス動作','ROS2 + ros2_control 準拠','ZMP 歩行安定化 (予定)','OpenCV + 顔追跡 (予定)'] as item}
              <li class="flex items-start gap-2 text-slate-400 text-sm">
                <span class="text-[#e85d04] mt-0.5 shrink-0">✓</span>{item}
              </li>
            {/each}
          </ul>
        </div>
      {:else}
        <div>
          <h3 class="text-xl font-bold text-white mb-3">Caterpillar <span class="text-[11px] text-slate-500 font-normal ml-1">Coming Soon</span></h3>
          <p class="text-slate-400 text-sm leading-relaxed mb-4">
            380 × 300 mm の重装甲デュアルトラック UGV。LiDAR + ステレオカメラ + IMU/GPS を
            標準搭載し、SLAM 自律探索に最適。Otete のクローラー設計を大型化した
            研究・産業向けプラットフォーム。
          </p>
          <ul class="space-y-2">
            {#each ['デュアルトラック (±132mm 幅)','Ø 60 mm LiDAR ドーム + ステレオカメラ','IMU + GPS 搭載センサーポッド','SLAM / Nav2 対応','PCB + RPi5 + 18650 内蔵','アーム増設オプション (予定)'] as item}
              <li class="flex items-start gap-2 text-slate-400 text-sm">
                <span class="text-[#e85d04] mt-0.5 shrink-0">✓</span>{item}
              </li>
            {/each}
          </ul>
        </div>
      {/if}
    </div>
  </div>
</section>

<!-- ─── ④ Specs ─── -->
<section class="py-20 px-5 bg-[#0d1117] border-t border-[#21262d]" id="specs">
  <div class="max-w-4xl mx-auto">
    <h2 class="text-2xl sm:text-3xl font-bold text-white text-center mb-2">主要スペック</h2>
    <p class="text-slate-500 text-sm text-center mb-10">
      詳細は <a href="/product/specs" class="text-[#e85d04] hover:underline">/product/specs</a> を参照
    </p>
    <div class="grid sm:grid-cols-2 gap-2.5">
      {#each specs as s}
        <div class="flex items-center justify-between bg-[#161b22] border border-[#21262d] rounded-xl px-5 py-3">
          <span class="text-slate-400 text-sm">{s.label}</span>
          <span class="text-white font-semibold text-sm text-right">{s.value}</span>
        </div>
      {/each}
    </div>
  </div>
</section>

<!-- ─── ⑤ Japan-Made Story ─── -->
<section class="py-20 px-5 bg-[#0a0d14] border-t border-[#21262d]" id="japan-made">
  <div class="max-w-5xl mx-auto">
    <div class="text-[#e85d04] text-[11px] font-bold uppercase tracking-widest text-center mb-3">Japan-Made Story</div>
    <h2 class="text-2xl sm:text-3xl font-bold text-white text-center mb-3">すべての部品が、日本産。</h2>
    <p class="text-slate-400 text-sm text-center max-w-xl mx-auto mb-10">
      多くの競合キットは中国製ハードウェアで設計・製造されています。
      Giemon Otete は、国内調達のサプライチェーンにこだわりました。
    </p>
    <div class="grid sm:grid-cols-2 md:grid-cols-3 gap-4">
      {#each makers as m}
        <div class="bg-[#161b22] border border-[#21262d] rounded-xl px-5 py-4">
          <div class="text-white font-semibold text-sm">{m.name}</div>
          <div class="text-[#e85d04] text-xs mt-0.5">{m.role}</div>
          <div class="text-slate-500 text-xs mt-0.5">📍 {m.location}</div>
          <div class="text-slate-500 text-xs mt-1.5 leading-relaxed">{m.note}</div>
        </div>
      {/each}
    </div>
  </div>
</section>

<!-- ─── ⑥ Ecosystem ─── -->
<section class="py-20 px-5 bg-[#0d1117] border-t border-[#21262d]" id="ecosystem">
  <div class="max-w-5xl mx-auto">
    <h2 class="text-2xl sm:text-3xl font-bold text-white text-center mb-2">エコシステム連携</h2>
    <p class="text-slate-500 text-sm text-center mb-10">オープンな技術スタックで何でも作れる</p>
    <div class="grid sm:grid-cols-2 md:grid-cols-3 gap-4">
      {#each ecosystem as e}
        <div class="bg-[#161b22] border border-[#21262d] hover:border-[#e85d04]/40 rounded-xl px-5 py-4 transition-colors">
          <div class="text-white font-semibold text-sm">{e.label}</div>
          <div class="text-slate-400 text-xs mt-1">{e.desc}</div>
          {#if e.link}
            <a href={e.link} class="text-[#e85d04] text-xs mt-1.5 inline-block hover:underline" target="_blank" rel="noopener">
              開く →
            </a>
          {/if}
        </div>
      {/each}
    </div>
    <div class="mt-8 text-center">
      <a
        href="https://github.com/etzhayyim/otete"
        target="_blank" rel="noopener"
        class="inline-flex items-center gap-2 px-5 py-2.5 border border-[#30363d] hover:border-slate-500 text-slate-300 hover:text-white rounded-full text-sm transition-colors"
      >
        <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 24 24">
          <path d="M12 2C6.477 2 2 6.484 2 12.017c0 4.425 2.865 8.18 6.839 9.504.5.092.682-.217.682-.483 0-.237-.008-.868-.013-1.703-2.782.605-3.369-1.343-3.369-1.343-.454-1.158-1.11-1.466-1.11-1.466-.908-.62.069-.608.069-.608 1.003.07 1.531 1.032 1.531 1.032.892 1.53 2.341 1.088 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.113-4.555-4.951 0-1.093.39-1.988 1.029-2.688-.103-.253-.446-1.272.098-2.65 0 0 .84-.27 2.75 1.026A9.564 9.564 0 0112 6.844c.85.004 1.705.115 2.504.337 1.909-1.296 2.747-1.027 2.747-1.027.546 1.379.202 2.398.1 2.651.64.7 1.028 1.595 1.028 2.688 0 3.848-2.339 4.695-4.566 4.943.359.309.678.92.678 1.855 0 1.338-.012 2.419-.012 2.747 0 .268.18.58.688.482A10.019 10.019 0 0022 12.017C22 6.484 17.522 2 12 2z" />
        </svg>
        GitHub で見る
      </a>
    </div>
  </div>
</section>

<!-- ─── ⑦ Pricing ─── -->
<section class="py-20 px-5 bg-[#0a0d14] border-t border-[#21262d]" id="pricing">
  <div class="max-w-5xl mx-auto">
    <h2 class="text-2xl sm:text-3xl font-bold text-white text-center mb-2">価格・ラインアップ</h2>
    <p class="text-slate-500 text-sm text-center mb-12">クラウドファンディング早割価格あり</p>
    <div class="grid md:grid-cols-3 gap-5">
      {#each plans as p}
        <div class={[
          'relative flex flex-col rounded-2xl border p-6',
          p.highlight ? 'bg-[#161b22] border-[#e85d04] shadow-xl shadow-orange-900/20' : 'bg-[#161b22] border-[#21262d]'
        ].join(' ')}>
          {#if p.highlight}
            <div class="absolute -top-3 left-1/2 -translate-x-1/2 px-3 py-0.5 bg-[#e85d04] text-white text-[11px] font-bold rounded-full whitespace-nowrap">
              おすすめ
            </div>
          {/if}
          <div class="text-slate-400 text-sm">{p.name}</div>
          <div class="mt-2 flex items-baseline gap-1">
            <span class="text-[2rem] font-extrabold text-white leading-none">{p.price}</span>
            <span class="text-slate-500 text-xs">{p.priceNote}</span>
          </div>
          {#if p.earlyPrice}
            <div class="mt-1">
              <span class="text-[#e85d04] text-sm font-semibold">{p.earlyPrice}</span>
              <span class="text-slate-500 text-xs ml-1">{p.earlyNote}</span>
            </div>
          {:else}
            <div class="mt-1 text-slate-500 text-xs">{p.earlyNote}</div>
          {/if}
          <ul class="mt-5 space-y-2 flex-1">
            {#each p.features as feat}
              <li class="flex items-start gap-2 text-slate-400 text-sm">
                <span class="text-[#e85d04] shrink-0 mt-0.5">✓</span>{feat}
              </li>
            {/each}
          </ul>
          <a
            href={p.ctaHref}
            class={[
              'mt-6 block text-center py-2.5 rounded-full text-sm font-semibold transition-colors',
              p.highlight
                ? 'bg-[#e85d04] hover:bg-[#f48c06] text-white'
                : 'border border-[#30363d] hover:border-slate-500 text-slate-300 hover:text-white'
            ].join(' ')}
          >
            {p.cta}
          </a>
        </div>
      {/each}
    </div>
    <p class="mt-8 text-center text-slate-600 text-xs">
      Amazon JP FBA / Makuake / Kickstarter / tsukuru.etzhayyim.com (B2B 直販) で展開予定
    </p>
  </div>
</section>

<!-- ─── ⑧ Media (placeholder) ─── -->
<section class="py-10 px-5 bg-[#0d1117] border-t border-[#21262d]">
  <p class="text-center text-slate-600 text-xs">メディア掲載情報は出荷後に追加予定</p>
</section>

<!-- ─── ⑨ Footer ─── -->
<footer class="bg-[#080b10] border-t border-[#21262d] py-12 px-5">
  <div class="max-w-5xl mx-auto grid sm:grid-cols-3 gap-8 text-sm text-slate-400">
    <div>
      <div class="text-white font-bold mb-2 flex items-center gap-2"><span>🤖</span>Giemon Otete</div>
      <p class="text-xs text-slate-500 leading-relaxed">
        6軸アーム × クローラーロボットキット。<br />全国産部品・ROS2 対応・オープンソース。<br />
        開発元: etzhayyim Japan株式会社 / amanomibashira
      </p>
    </div>
    <div>
      <div class="text-white font-semibold text-sm mb-3">製品</div>
      <ul class="space-y-1.5 text-xs">
        {#each [['製品詳細','/product'],['スペック表','/product/specs'],['BOM 一覧','/product/bom'],['組立マニュアル','/assembly'],['ファームウェア','/firmware'],['教育向け','/education'],['購入','/buy']] as [l,h]}
          <li><a href={h} class="hover:text-white transition-colors">{l}</a></li>
        {/each}
      </ul>
    </div>
    <div>
      <div class="text-white font-semibold text-sm mb-3">リンク</div>
      <ul class="space-y-1.5 text-xs">
        <li><a href="https://github.com/etzhayyim/otete" target="_blank" rel="noopener" class="hover:text-white transition-colors">GitHub</a></li>
        <li><a href="/blog" class="hover:text-white transition-colors">技術ブログ</a></li>
        <li><a href="mailto:sales@etzhayyim.com" class="hover:text-white transition-colors">法人・教育機関問い合わせ</a></li>
        <li><a href="https://tsukuru.etzhayyim.com" target="_blank" rel="noopener" class="hover:text-white transition-colors">tsukuru.etzhayyim.com (B2B)</a></li>
      </ul>
    </div>
  </div>
  <div class="max-w-5xl mx-auto mt-8 pt-6 border-t border-[#161b22] flex flex-wrap gap-4 items-center justify-between text-[11px] text-slate-600">
    <span>© 2026 amanomibashira. All rights reserved.</span>
    <span>ライセンス: CERN-OHL-P v2 (ハードウェア) / Apache-2.0 (ソフトウェア)</span>
  </div>
</footer>
