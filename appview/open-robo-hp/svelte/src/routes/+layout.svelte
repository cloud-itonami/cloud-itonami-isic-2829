<script lang="ts">
  import '../app.css';
  import type { Snippet } from 'svelte';
  let { children }: { children: Snippet } = $props();
</script>

<svelte:head>
  <meta name="description" content="Giemon Otete — 6軸アーム付きクローラーロボットキット 日本製。全国産部品・ROS2対応・オープンソース。" />
  <meta property="og:title" content="Giemon Otete" />
  <meta property="og:description" content="6軸アーム付きクローラーロボットキット 日本製。全国産部品・ROS2対応・オープンソース。" />
  <meta property="og:type" content="website" />
  <meta property="og:url" content="https://giemon.etzhayyim.com" />
  <link rel="preconnect" href="https://fonts.googleapis.com" />
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin="anonymous" />
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Noto+Sans+JP:wght@400;500;700&display=swap" rel="stylesheet" />
</svelte:head>

{@render children()}
