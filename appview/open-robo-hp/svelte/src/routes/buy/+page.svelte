<script lang="ts">
  const channels = [
    {
      name: 'Amazon JP',
      badge: '最速配送',
      badgeColor: '#e8a217',
      icon: '📦',
      price: '¥98,780',
      priceNote: '税込',
      desc: 'FBA 在庫から翌日〜翌々日配送。個人購入・ギフトに最適。',
      cta: 'Amazon で購入',
      ctaHref: 'https://amazon.co.jp',
      ctaStyle: 'bg-[#f90] hover:bg-[#e8a217] text-black',
      available: false,
      availableNote: '2026年内発売予定',
    },
    {
      name: 'Makuake',
      badge: '早割あり',
      badgeColor: '#e85d04',
      icon: '🚀',
      price: '¥89,800',
      priceNote: '早割価格',
      desc: 'クラウドファンディング早割。¥9,000 お得。数量限定。',
      cta: 'Makuake で支援する',
      ctaHref: 'https://www.makuake.com/project/otete',
      ctaStyle: 'bg-[#e85d04] hover:bg-[#f48c06] text-white',
      available: false,
      availableNote: 'キャンペーン準備中',
    },
    {
      name: 'Kickstarter',
      badge: 'グローバル',
      badgeColor: '#05ce78',
      icon: '🌏',
      price: '$699',
      priceNote: 'USD (early bird)',
      desc: '海外向け。英語ドキュメント付き。グローバルシッピング対応。',
      cta: 'Kickstarter で支援する',
      ctaHref: 'https://kickstarter.com/projects/etzhayyim/otete',
      ctaStyle: 'bg-[#05ce78] hover:bg-[#04b86c] text-black',
      available: false,
      availableNote: 'キャンペーン準備中',
    },
    {
      name: 'tsukuru.etzhayyim.com',
      badge: 'B2B 直販',
      badgeColor: '#58a6ff',
      icon: '🏢',
      price: 'お見積もり',
      priceNote: '法人・教育機関',
      desc: '企業・大学向け。納品書・領収書・見積書発行対応。Education 3-pack 割引あり。',
      cta: 'tsukuru.etzhayyim.com を開く',
      ctaHref: 'https://tsukuru.etzhayyim.com',
      ctaStyle: 'bg-[#58a6ff] hover:bg-[#79c0ff] text-black',
      available: true,
      availableNote: '受付中',
    },
  ];

  const faq = [
    {
      q: '送料はかかりますか？',
      a: 'Amazon JP は Prime 対象（Prime 会員は無料）。Makuake/Kickstarter は送料別途表示。tsukuru.etzhayyim.com は法人向け見積時に確認。',
    },
    {
      q: 'Raspberry Pi 5 は付属しますか？',
      a: 'Raspberry Pi 5（4GB）は本体キットに含まれます。microSD カードは付属しません（Class 10 以上 32GB 以上推奨）。',
    },
    {
      q: '海外への発送は可能ですか？',
      a: 'Kickstarter キャンペーン経由でグローバルシッピング対応予定。日本国内以外への直接発送は現在準備中です。',
    },
    {
      q: '保証期間は？',
      a: '初期不良は受取後 2 週間以内にメールでご連絡ください。Otete HAT 基板は 6 ヶ月保証、サーボは各メーカー保証（近藤科学 1 年）を適用します。',
    },
    {
      q: 'Raspberry Pi 5 が入手困難な場合は？',
      a: '初期ロットは RPi 5 確保済みです。在庫状況によっては出荷時期が変動する場合があります。',
    },
  ];

  let openFaq = $state<number | null>(null);
</script>

<svelte:head>
  <title>購入 | Giemon Otete</title>
  <meta name="description" content="Giemon Otete の購入チャンネル。Amazon JP、Makuake、Kickstarter、tsukuru.etzhayyim.com (B2B 直販)。" />
</svelte:head>

<!-- ─── Nav ─── -->
<nav class="sticky top-0 z-50 bg-[#0d1117]/90 backdrop-blur border-b border-[#21262d] px-5 py-3">
  <div class="max-w-5xl mx-auto flex items-center justify-between">
    <a href="/" class="text-white font-bold text-sm flex items-center gap-2"><span>🤖</span> Giemon Otete</a>
    <a href="/" class="text-slate-400 hover:text-white text-xs transition-colors">← ホームへ戻る</a>
  </div>
</nav>

<!-- ─── Hero ─── -->
<section class="py-16 px-5 bg-[#0d1117]">
  <div class="max-w-3xl mx-auto text-center">
    <div class="text-4xl mb-4">🛒</div>
    <h1 class="text-3xl sm:text-4xl font-extrabold text-white mb-3">購入チャンネル</h1>
    <p class="text-slate-400 text-sm">個人・法人・教育機関、それぞれに最適な購入方法をお選びください</p>
  </div>
</section>

<!-- ─── Channels ─── -->
<section class="py-12 px-5 bg-[#0d1117]">
  <div class="max-w-4xl mx-auto grid sm:grid-cols-2 gap-5">
    {#each channels as ch}
      <div class="flex flex-col p-6 bg-[#161b22] border border-[#21262d] rounded-2xl relative">
        {#if !ch.available}
          <div class="absolute inset-0 bg-[#0d1117]/60 rounded-2xl flex items-center justify-center z-10">
            <span class="px-3 py-1 bg-[#21262d] border border-[#30363d] rounded-full text-slate-400 text-xs">{ch.availableNote}</span>
          </div>
        {/if}
        <div class="flex items-start justify-between mb-4">
          <div>
            <div class="flex items-center gap-2">
              <span class="text-xl">{ch.icon}</span>
              <span class="text-white font-bold">{ch.name}</span>
            </div>
          </div>
          <span class="px-2 py-0.5 rounded-full text-xs font-semibold" style="background:{ch.badgeColor}20; color:{ch.badgeColor}; border: 1px solid {ch.badgeColor}40">
            {ch.badge}
          </span>
        </div>
        <div class="flex items-baseline gap-1 mb-2">
          <span class="text-2xl font-extrabold text-white">{ch.price}</span>
          <span class="text-slate-500 text-xs">{ch.priceNote}</span>
        </div>
        <p class="text-slate-400 text-xs leading-relaxed flex-1 mb-5">{ch.desc}</p>
        <a href={ch.ctaHref} target="_blank" rel="noopener"
          class={['block text-center py-2.5 rounded-full text-sm font-semibold transition-colors', ch.ctaStyle].join(' ')}>
          {ch.cta}
        </a>
      </div>
    {/each}
  </div>
</section>

<!-- ─── Education Banner ─── -->
<section class="py-10 px-5 bg-[#0a0d14] border-y border-[#21262d]">
  <div class="max-w-3xl mx-auto flex flex-col sm:flex-row items-center justify-between gap-4">
    <div>
      <p class="text-white font-semibold">🎓 教育機関・研究機関の方へ</p>
      <p class="text-slate-400 text-sm mt-1">3 台パックで約 9% 割引 + 全12回カリキュラム PDF 付き</p>
    </div>
    <a href="/education" class="shrink-0 px-5 py-2.5 border border-[#30363d] hover:border-slate-500 text-slate-300 hover:text-white text-sm rounded-full transition-colors">
      Education 3-pack を見る →
    </a>
  </div>
</section>

<!-- ─── FAQ ─── -->
<section class="py-16 px-5 bg-[#0d1117]">
  <div class="max-w-3xl mx-auto">
    <h2 class="text-white font-bold text-xl mb-8">よくある質問</h2>
    <div class="space-y-2">
      {#each faq as f, i}
        <div class="bg-[#161b22] border border-[#21262d] rounded-xl overflow-hidden">
          <button
            onclick={() => openFaq = openFaq === i ? null : i}
            class="w-full flex items-center justify-between px-5 py-4 text-left"
          >
            <span class="text-slate-200 text-sm font-medium">{f.q}</span>
            <span class="text-slate-400 text-xs ml-4 shrink-0">{openFaq === i ? '▲' : '▼'}</span>
          </button>
          {#if openFaq === i}
            <div class="px-5 pb-4 text-slate-400 text-sm border-t border-[#21262d] pt-3">
              {f.a}
            </div>
          {/if}
        </div>
      {/each}
    </div>
  </div>
</section>

<!-- ─── Footer ─── -->
<footer class="bg-[#080b10] border-t border-[#21262d] py-8 px-5 text-center text-[11px] text-slate-600">
  © 2026 amanomibashira — お問い合わせ: <a href="mailto:sales@etzhayyim.com" class="hover:text-slate-400">sales@etzhayyim.com</a>
</footer>
