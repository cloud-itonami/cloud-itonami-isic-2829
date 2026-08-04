<script lang="ts">
  const posts = [
    {
      slug: 'giemon-otete-announcement',
      date: '2026-05-14',
      title: 'Giemon Otete — 日本製オープンソースロボットキットを発表',
      category: 'アナウンス',
      excerpt: '全国産部品・ROS2 対応・CERN-OHL-P v2 ライセンスのロボットアームキット Giemon Otete を発表しました。からくり儀右衛門（田中久重）の精神をオープンハードウェアで継承します。',
      readTime: '3分',
    },
    {
      slug: 'ics35-servo-protocol-deep-dive',
      date: '2026-05-14',
      title: '近藤科学 ICS3.5 プロトコルの詳細解説と Python 実装',
      category: '技術',
      excerpt: 'ICS3.5 は半二重 UART をベースにしたシリアルバスプロトコルです。1 本のケーブルで最大 32 軸を制御できます。UART バッファ IC の選定から TX_EN 制御のタイミングまでを詳解します。',
      readTime: '8分',
    },
    {
      slug: 'otete-dls-inverse-kinematics',
      date: '2026-05-14',
      title: 'DLS 逆運動学ソルバー: numpy だけで 6 軸アームの IK を解く',
      category: '技術',
      excerpt: 'Damped Least Squares (DLS) 法による逆運動学ソルバーを numpy のみで実装しました。特異点付近でも安定して解が得られ、依存ライブラリなしで Raspberry Pi 5 上でリアルタイム動作します。',
      readTime: '12分',
    },
    {
      slug: 'makuake-crowdfunding-preparation',
      date: '2026-05-14',
      title: 'Makuake クラウドファンディング準備中 — 早割 9% OFF',
      category: 'ニュース',
      excerpt: 'Makuake でのクラウドファンディングキャンペーンを準備中です。早割価格 ¥89,800（通常 ¥98,780）での先行支援を予定しています。お知らせ登録はこちら。',
      readTime: '2分',
    },
  ];

  const categories = ['すべて', 'アナウンス', '技術', 'ニュース'];
  let selectedCategory = $state('すべて');

  const filtered = $derived(
    selectedCategory === 'すべて' ? posts : posts.filter(p => p.category === selectedCategory)
  );
</script>

<svelte:head>
  <title>技術ブログ | Giemon Otete</title>
  <meta name="description" content="Giemon Otete の技術ブログ。ロボット制御・ROS2・逆運動学・オープンハードウェア開発の記事を公開中。" />
</svelte:head>

<!-- ─── Nav ─── -->
<nav class="sticky top-0 z-50 bg-[#0d1117]/90 backdrop-blur border-b border-[#21262d] px-5 py-3">
  <div class="max-w-5xl mx-auto flex items-center justify-between">
    <a href="/" class="text-white font-bold text-sm flex items-center gap-2"><span>🤖</span> Giemon Otete</a>
    <a href="/" class="text-slate-400 hover:text-white text-xs transition-colors">← ホームへ戻る</a>
  </div>
</nav>

<!-- ─── Hero ─── -->
<section class="py-14 px-5 bg-[#0d1117]">
  <div class="max-w-3xl mx-auto">
    <h1 class="text-3xl font-extrabold text-white mb-2">技術ブログ</h1>
    <p class="text-slate-400 text-sm">ロボット制御・ROS2・逆運動学・オープンハードウェア開発</p>
  </div>
</section>

<!-- ─── Filter ─── -->
<div class="border-b border-[#21262d] px-5">
  <div class="max-w-3xl mx-auto flex gap-1 overflow-x-auto">
    {#each categories as cat}
      <button
        onclick={() => selectedCategory = cat}
        class={[
          'px-4 py-3 text-sm font-medium border-b-2 whitespace-nowrap transition-colors',
          selectedCategory === cat
            ? 'border-[#e85d04] text-white'
            : 'border-transparent text-slate-400 hover:text-white'
        ].join(' ')}
      >
        {cat}
      </button>
    {/each}
  </div>
</div>

<!-- ─── Posts ─── -->
<section class="py-12 px-5 bg-[#0d1117]">
  <div class="max-w-3xl mx-auto space-y-5">
    {#each filtered as post}
      <article class="p-5 bg-[#161b22] border border-[#21262d] rounded-2xl hover:border-[#30363d] transition-colors cursor-pointer group">
        <div class="flex items-center gap-2 mb-3">
          <span class="px-2 py-0.5 bg-[#21262d] text-slate-400 text-xs rounded-full">{post.category}</span>
          <span class="text-slate-600 text-xs">{post.date}</span>
          <span class="text-slate-600 text-xs">· {post.readTime}</span>
        </div>
        <h2 class="text-white font-bold text-base mb-2 group-hover:text-[#e85d04] transition-colors">{post.title}</h2>
        <p class="text-slate-400 text-sm leading-relaxed">{post.excerpt}</p>
        <div class="mt-4 text-[#e85d04] text-xs font-medium">続きを読む →</div>
      </article>
    {/each}

    {#if filtered.length === 0}
      <p class="text-slate-500 text-sm text-center py-12">該当する記事がありません</p>
    {/if}
  </div>
</section>

<!-- ─── Newsletter ─── -->
<section class="py-14 px-5 bg-[#0a0d14] border-t border-[#21262d]">
  <div class="max-w-3xl mx-auto text-center">
    <h2 class="text-white font-bold text-xl mb-3">新着記事をメールで受け取る</h2>
    <p class="text-slate-400 text-sm mb-6">ロボット制御・オープンハードウェアの最新情報をお届けします</p>
    <a href="mailto:newsletter@etzhayyim.com?subject=Giemon ブログ購読希望"
      class="inline-block px-6 py-2.5 border border-[#30363d] hover:border-slate-500 text-slate-300 hover:text-white text-sm rounded-full transition-colors">
      購読する (newsletter@etzhayyim.com)
    </a>
  </div>
</section>

<!-- ─── Footer ─── -->
<footer class="bg-[#080b10] border-t border-[#21262d] py-8 px-5 text-center text-[11px] text-slate-600">
  © 2026 amanomibashira — Giemon Otete
</footer>
