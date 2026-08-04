<script lang="ts">
  const highlights = [
    { icon: '📚', title: '全12回カリキュラム', body: '基礎からROS2・MoveIt!・強化学習まで体系的に学べる授業計画PDFを同梱。' },
    { icon: '🤝', title: '3台パックで割引', body: '1台 ¥98,780 → 3台セット ¥269,500（約9%割引）。チームで協働実験できる最小構成。' },
    { icon: '📄', title: '納品書・領収書対応', body: '文科省・大学・高専・高校の購買・備品管理に対応した書類を発行します。' },
    { icon: '📧', title: '優先サポート', body: '教育機関専用のメールサポート窓口。授業準備・トラブルシュートを素早くサポート。' },
    { icon: '🔓', title: '完全オープンソース', body: 'STEP/KiCad/ROS2/Python を全公開。改造・発展課題の設定が自由。' },
    { icon: '🇯🇵', title: '国産教材', body: '全国産部品で構成。メーカー・調達先が明確なため、セキュリティ審査が通りやすい。' },
  ];

  const curriculum = [
    { session: 1, title: '開封・組立 (1)', topic: 'ハードウェア概要、BOM 確認、シャーシ組立' },
    { session: 2, title: '開封・組立 (2)', topic: 'アームリンク組立、HAT 基板取り付け' },
    { session: 3, title: 'ファームウェア導入', topic: 'Ubuntu + ROS2 Humble セットアップ、サーボスキャン' },
    { session: 4, title: '基礎制御 (1)', topic: 'ICS サーボ制御 Python API、順運動学' },
    { session: 5, title: '基礎制御 (2)', topic: '逆運動学 (IK)、DLS ソルバー実装' },
    { session: 6, title: 'クローラー制御', topic: 'diff-drive モデル、PID 速度制御、オドメトリ' },
    { session: 7, title: 'ROS2 入門', topic: 'トピック/サービス/アクション、bringup launch' },
    { session: 8, title: 'MoveIt! 軌道計画', topic: 'RViz 可視化、OMPL プランナー、衝突回避' },
    { session: 9, title: 'コンピュータビジョン', topic: 'RPi Camera v3、OpenCV、物体検出 (YOLOv8)' },
    { session: 10, title: 'センサーフュージョン', topic: 'IMU + オドメトリ + ToF センサー統合' },
    { session: 11, title: '強化学習入門', topic: 'Gymnasium 環境構築、Stable-Baselines3 PPO' },
    { session: 12, title: '最終発表', topic: '自由課題制作・デモ発表・ポートフォリオ作成' },
  ];
</script>

<svelte:head>
  <title>教育機関向け | Giemon Otete</title>
  <meta name="description" content="Giemon Otete 教育 3-pack。大学・高専・高校向け 3 台パック + 全12回授業カリキュラム付き。" />
</svelte:head>

<!-- ─── Nav ─── -->
<nav class="sticky top-0 z-50 bg-[#0d1117]/90 backdrop-blur border-b border-[#21262d] px-5 py-3">
  <div class="max-w-5xl mx-auto flex items-center justify-between">
    <a href="/" class="text-white font-bold text-sm flex items-center gap-2"><span>🤖</span> Giemon Otete</a>
    <a href="/" class="text-slate-400 hover:text-white text-xs transition-colors">← ホームへ戻る</a>
  </div>
</nav>

<!-- ─── Hero ─── -->
<section class="py-20 px-5 bg-gradient-to-b from-[#0a0d14] to-[#0d1117]">
  <div class="max-w-3xl mx-auto text-center">
    <div class="inline-flex items-center gap-2 px-3 py-1 bg-[#161b22] border border-[#e85d04]/40 rounded-full text-[#e85d04] text-xs font-semibold mb-6">
      🎓 教育機関・研究機関向け
    </div>
    <h1 class="text-3xl sm:text-4xl font-extrabold text-white mb-4">Education 3-pack</h1>
    <div class="flex items-baseline justify-center gap-2 mb-2">
      <span class="text-4xl font-extrabold text-white">¥269,500</span>
      <span class="text-slate-400 text-sm">税込 / 3 台</span>
    </div>
    <p class="text-slate-500 text-xs mb-8">通常 3 台分 ¥296,340 → <span class="text-[#e85d04]">約 9% 割引</span></p>
    <a href="mailto:sales@etzhayyim.com?subject=Giemon Otete Education 3-pack 見積もり依頼"
      class="inline-block px-8 py-3 bg-[#e85d04] hover:bg-[#f48c06] text-white font-bold rounded-full transition-colors">
      見積もりを依頼する
    </a>
    <p class="mt-3 text-slate-500 text-xs">納品書・領収書・見積書を発行します</p>
  </div>
</section>

<!-- ─── Highlights ─── -->
<section class="py-16 px-5 bg-[#0d1117] border-t border-[#21262d]">
  <div class="max-w-4xl mx-auto">
    <h2 class="text-white font-bold text-xl text-center mb-10">教育機関向け特典</h2>
    <div class="grid sm:grid-cols-2 lg:grid-cols-3 gap-5">
      {#each highlights as h}
        <div class="p-5 bg-[#161b22] border border-[#21262d] rounded-2xl">
          <div class="text-2xl mb-3">{h.icon}</div>
          <h3 class="text-white font-semibold text-sm mb-2">{h.title}</h3>
          <p class="text-slate-400 text-xs leading-relaxed">{h.body}</p>
        </div>
      {/each}
    </div>
  </div>
</section>

<!-- ─── Curriculum ─── -->
<section class="py-16 px-5 bg-[#0a0d14] border-t border-[#21262d]">
  <div class="max-w-3xl mx-auto">
    <h2 class="text-white font-bold text-xl mb-2">授業カリキュラム（全12回）</h2>
    <p class="text-slate-500 text-sm mb-8">各90〜120分。1学期（前期/後期）での運用を想定した標準プランです。</p>
    <div class="space-y-2">
      {#each curriculum as c}
        <div class="flex gap-4 p-3 bg-[#161b22] border border-[#21262d] rounded-xl hover:border-[#30363d] transition-colors">
          <span class="w-8 h-8 flex items-center justify-center rounded-full bg-[#0d1117] border border-[#21262d] text-slate-400 text-xs font-mono shrink-0">
            {c.session}
          </span>
          <div>
            <div class="text-white text-sm font-medium">{c.title}</div>
            <div class="text-slate-500 text-xs mt-0.5">{c.topic}</div>
          </div>
        </div>
      {/each}
    </div>
  </div>
</section>

<!-- ─── Included ─── -->
<section class="py-16 px-5 bg-[#0d1117] border-t border-[#21262d]">
  <div class="max-w-3xl mx-auto">
    <h2 class="text-white font-bold text-xl mb-8">3-pack 同梱内容</h2>
    <div class="grid sm:grid-cols-2 gap-3">
      {#each [
        ['Giemon Otete 本体キット', '× 3 台'],
        ['授業カリキュラム PDF（全12回）', '× 1 部'],
        ['組立マニュアル URL カード', '× 3 枚'],
        ['Otete HAT 基板', '× 3 枚'],
        ['近藤科学 KRS サーボ全軸分', '× 3 セット'],
        ['マブチ RS-380PH モーター', '× 6 個'],
        ['NCR18650B 電池 4S2P パック', '× 3 パック'],
        ['M3 ネジ・ナットセット', '× 3 袋'],
        ['優先メールサポート（1年間）', '— 3 台分'],
        ['納品書・領収書・見積書', '— 発行可'],
      ] as [item, qty]}
        <div class="flex items-center gap-3 p-3 bg-[#161b22] border border-[#21262d] rounded-lg">
          <span class="text-[#e85d04] text-sm shrink-0">✓</span>
          <div class="flex-1 text-slate-200 text-sm">{item}</div>
          <div class="text-slate-500 text-xs shrink-0">{qty}</div>
        </div>
      {/each}
    </div>
  </div>
</section>

<!-- ─── CTA ─── -->
<section class="py-16 px-5 bg-[#0a0d14] border-t border-[#21262d] text-center">
  <h2 class="text-white font-bold text-xl mb-3">導入をご検討の教育機関様へ</h2>
  <p class="text-slate-400 text-sm mb-8 max-w-lg mx-auto">
    見積書・仕様書・デモ機の手配など、ご要望に合わせて対応します。
    お気軽にお問い合わせください。
  </p>
  <a href="mailto:sales@etzhayyim.com?subject=Giemon Otete Education 3-pack 見積もり依頼"
    class="inline-block px-8 py-3 bg-[#e85d04] hover:bg-[#f48c06] text-white font-bold rounded-full transition-colors">
    sales@etzhayyim.com にメールする
  </a>
</section>

<!-- ─── Footer ─── -->
<footer class="bg-[#080b10] border-t border-[#21262d] py-8 px-5 text-center text-[11px] text-slate-600">
  © 2026 amanomibashira — Giemon Otete は CERN-OHL-P v2 / Apache-2.0 ライセンスで公開
</footer>
