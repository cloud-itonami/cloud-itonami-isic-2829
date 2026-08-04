<script lang="ts">
  const steps = [
    {
      num: 1,
      title: 'クローラーシャーシ組み立て',
      time: '約60分',
      icon: '🛤️',
      items: [
        'サイドプレートにホイールシャフト (SUS304 Φ8mm×80mm) を差し込み、608ZZ ベアリングをゴムハンマーで圧入',
        'マブチ RS-380PH モーターをモーターマウントブラケットに M3×12mm で固定',
        'ボトムプレートにサイドプレート L/R を M3×12mm × 8 本で固定（水平を確認）',
        'タミヤ 70168 トラックベルトをアイドラーホイールで 3mm 程度のたるみに調整',
      ],
      warning: 'トラックテンションが緩すぎると走行中に外れる原因となります。必ずアイドラーホイールで調整してください。',
    },
    {
      num: 2,
      title: 'アームベース・J1旋回台',
      time: '約30分',
      icon: '🔩',
      items: [
        'トップマウントプレートをサイドプレート上部に M3×8mm × 8 本で固定',
        'KRS-6013IHV サーボを J1 ハウジングに挿入し、アルミホーンを M3×4mm で固定',
        '6004ZZ ベアリングをハウジングに圧入',
        'J1 ハウジングをトップマウントプレートに M3×12mm × 6 本で固定',
      ],
    },
    {
      num: 3,
      title: 'アームリンク (J2–J6)',
      time: '約60分',
      icon: '🦾',
      items: [
        'J2 肩: KRS-6013IHV + 平行リンク L120mm。リンクを M3×10mm × 4 本で固定',
        'J3 肘: KRS-4014ICS をリンクフレームに圧入後 M3×8mm × 4 本',
        'J4–J5 手首: KRS-4014ICS + KRS-2350IHV。全ケーブルを ICS バス配線',
        'J6 グリッパー: Futaba S3003 をグリッパーハウジングに M3×6mm × 2 本で取り付け',
        'ICS ケーブルを J1 から J6 まで数珠つなぎにルーティング',
      ],
      warning: 'サーボに通電した状態でトルクが発生します。組立中はサーボへの電源供給を切ってください。',
    },
    {
      num: 4,
      title: 'Otete HAT 基板取り付け',
      time: '約20分',
      icon: '🔌',
      items: [
        'Raspberry Pi 5 をトップマウントプレートの M2.5 スペーサー × 4 本に固定',
        'Otete HAT を RPi 5 の 40-pin GPIO ヘッダーに差し込み',
        'ICS3.5 ケーブルをサーボバスコネクターへ接続（極性を確認）',
        'TB6612FNG モーターコネクター L/R を接続',
        'RPi Camera v3 フラットケーブルを HAT カメラポートへ差し込み',
      ],
    },
    {
      num: 5,
      title: 'バッテリー・電源系統',
      time: '約20分',
      icon: '🔋',
      items: [
        'NCR18650B × 8 本を 4S2P ホルダーに極性を確認して挿入',
        'BMS コネクターをバッテリーホルダーに接続',
        'TDK-Lambda DC-DC コンバーター → HAT へ 5V / 7.4V ケーブルを接続',
        'メインスイッチを OFF にしたまま、シャーシ内配線をタイラップで整線',
      ],
      warning: 'バッテリー逆接は BMS 保護が働いても破損原因となります。極性を必ずテスターで確認してから接続してください。',
    },
    {
      num: 6,
      title: '動作確認 & ファームウェア書き込み',
      time: '約30分',
      icon: '✅',
      items: [
        'メインスイッチを ON → RPi 5 LED が点灯することを確認',
        'SSH 接続 (`ssh giemon@giemon.local`) でログイン',
        '`cd ~/otete && python firmware/armcrawler/servo/ics_driver.py --scan` でサーボ 7 軸検出を確認',
        '`python firmware/armcrawler/crawler/motor_driver.py --test` でクローラー正転・逆転確認',
        'ROS2 起動: `ros2 launch otete_ros2 bringup.launch.py`',
        'RViz でアーム可視化: `ros2 launch otete_ros2 display.launch.py`',
      ],
    },
  ];

  const tools = [
    { name: 'ヘクサゴンレンチ M2.5 / M3', note: '必須' },
    { name: 'プラスドライバー #1 / #2', note: '必須' },
    { name: 'ニッパー', note: '必須' },
    { name: 'テスター（導通・電圧確認）', note: '必須' },
    { name: 'ゴムハンマー', note: 'ベアリング圧入に使用' },
    { name: '電動ドライバー', note: '任意（作業効率向上）' },
    { name: '静電気防止手袋', note: 'RPi 5 取り扱い時推奨' },
  ];
</script>

<svelte:head>
  <title>組立マニュアル | Giemon Otete</title>
  <meta name="description" content="Giemon Otete の 6 ステップ組立マニュアル。所要時間約4〜6時間。必要工具・パーツ確認から動作テストまで。" />
</svelte:head>

<!-- ─── Nav ─── -->
<nav class="sticky top-0 z-50 bg-[#0d1117]/90 backdrop-blur border-b border-[#21262d] px-5 py-3">
  <div class="max-w-5xl mx-auto flex items-center justify-between">
    <a href="/" class="text-white font-bold text-sm flex items-center gap-2">
      <span>🤖</span> Giemon Otete
    </a>
    <a href="/" class="text-slate-400 hover:text-white text-xs transition-colors">← ホームへ戻る</a>
  </div>
</nav>

<!-- ─── Hero ─── -->
<section class="py-16 px-5 bg-[#0d1117]">
  <div class="max-w-3xl mx-auto text-center">
    <div class="text-4xl mb-4">🔧</div>
    <h1 class="text-3xl sm:text-4xl font-extrabold text-white mb-3">組立マニュアル</h1>
    <p class="text-slate-400 text-sm mb-6">Giemon Otete v1.0 — 所要時間 約4〜6時間（初回）</p>
    <div class="flex flex-wrap gap-3 justify-center text-xs">
      <span class="px-3 py-1 bg-[#161b22] border border-[#21262d] rounded-full text-slate-400">難易度: 中級</span>
      <span class="px-3 py-1 bg-[#161b22] border border-[#21262d] rounded-full text-slate-400">6 ステップ</span>
      <span class="px-3 py-1 bg-[#161b22] border border-[#21262d] rounded-full text-slate-400">電子工作経験者向け</span>
    </div>
  </div>
</section>

<!-- ─── Safety ─── -->
<section class="py-8 px-5 bg-[#100c04] border-y border-[#e85d04]/30">
  <div class="max-w-3xl mx-auto">
    <h2 class="text-[#f48c06] font-bold mb-3 flex items-center gap-2">⚠️ 安全上のご注意</h2>
    <ul class="space-y-1.5 text-slate-300 text-sm list-disc list-inside">
      <li>バッテリーは逆接しないこと（BMS 保護あり、ただし破損原因となる場合あり）</li>
      <li>サーボに通電した状態でトルクが発生します。指を挟まないよう注意</li>
      <li>アルミ切削面にはバリがある場合があります。組立時に手袋着用を推奨</li>
      <li>Raspberry Pi 5 は静電気に敏感です。接触前にアース放電してください</li>
    </ul>
  </div>
</section>

<!-- ─── Tools ─── -->
<section class="py-12 px-5 bg-[#0d1117] border-b border-[#21262d]">
  <div class="max-w-3xl mx-auto">
    <h2 class="text-white font-bold text-xl mb-6">必要工具</h2>
    <div class="grid sm:grid-cols-2 gap-2">
      {#each tools as t}
        <div class="flex items-center gap-3 p-3 bg-[#161b22] rounded-lg border border-[#21262d]">
          <span class={t.note === '必須' ? 'text-[#e85d04]' : 'text-slate-500'}>
            {t.note === '必須' ? '✓' : '○'}
          </span>
          <div>
            <div class="text-slate-200 text-sm">{t.name}</div>
            <div class="text-slate-500 text-xs">{t.note}</div>
          </div>
        </div>
      {/each}
    </div>
  </div>
</section>

<!-- ─── Steps ─── -->
<section class="py-16 px-5 bg-[#0d1117]">
  <div class="max-w-3xl mx-auto space-y-10">
    {#each steps as step}
      <div class="flex gap-5">
        <div class="flex flex-col items-center">
          <div class="w-10 h-10 flex items-center justify-center rounded-full bg-[#e85d04] text-white font-bold text-sm shrink-0">
            {step.num}
          </div>
          {#if step.num < steps.length}
            <div class="w-px flex-1 mt-2 bg-[#21262d]"></div>
          {/if}
        </div>
        <div class="flex-1 pb-8">
          <div class="flex items-center gap-2 mb-1">
            <span class="text-xl">{step.icon}</span>
            <h3 class="text-white font-bold text-lg">ステップ{step.num}: {step.title}</h3>
          </div>
          <p class="text-slate-500 text-xs mb-4">⏱ {step.time}</p>
          <ol class="space-y-2">
            {#each step.items as item, i}
              <li class="flex gap-2 text-slate-300 text-sm">
                <span class="text-slate-600 shrink-0 font-mono text-xs mt-0.5">{i + 1}.</span>
                {item}
              </li>
            {/each}
          </ol>
          {#if step.warning}
            <div class="mt-4 p-3 bg-[#100c04] border border-[#e85d04]/40 rounded-lg text-[#f48c06] text-xs">
              ⚠️ {step.warning}
            </div>
          {/if}
        </div>
      </div>
    {/each}
  </div>
</section>

<!-- ─── CTA ─── -->
<section class="py-14 px-5 bg-[#0a0d14] border-t border-[#21262d] text-center">
  <h2 class="text-white font-bold text-xl mb-4">組み立て完了！次のステップ</h2>
  <div class="flex flex-wrap gap-4 justify-center">
    <a href="/firmware" class="px-5 py-2.5 bg-[#e85d04] hover:bg-[#f48c06] text-white text-sm font-semibold rounded-full transition-colors">
      ファームウェアを書き込む →
    </a>
    <a href="https://github.com/etzhayyim/otete" target="_blank" rel="noopener"
      class="px-5 py-2.5 border border-[#30363d] hover:border-slate-500 text-slate-300 hover:text-white text-sm rounded-full transition-colors">
      GitHub でソースを見る
    </a>
  </div>
</section>

<!-- ─── Footer ─── -->
<footer class="bg-[#080b10] border-t border-[#21262d] py-8 px-5 text-center text-[11px] text-slate-600">
  © 2026 amanomibashira — Giemon Otete は CERN-OHL-P v2 ライセンスで公開
</footer>
