import { createWorkerExport } from '@etzhayyim/kotodama-host-sdk';

const _inner = createWorkerExport((sdk) => {
  sdk.app.query('com.etzhayyim.apps.openRoboHp.getProduct', async (_input, _ctx) => {
    return {
      name: 'Giemon Otete',
      version: '1.0.0',
      price_jpy: 98780,
      url: 'https://giemon.etzhayyim.com',
    };
  });
});

export default {
  async fetch(request: Request, env: Record<string, unknown>, ctx?: { waitUntil(p: Promise<unknown>): void }) {
    // 301 redirect: armcrawler.etzhayyim.com → giemon.etzhayyim.com
    const url = new URL(request.url);
    if (url.hostname === 'armcrawler.etzhayyim.com') {
      url.hostname = 'giemon.etzhayyim.com';
      return Response.redirect(url.toString(), 301);
    }
    return _inner.fetch(request, env, ctx);
  },
};
