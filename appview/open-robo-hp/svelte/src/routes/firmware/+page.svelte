<script lang="ts">
  let activeTab = $state<'quickstart' | 'ros2' | 'api'>('quickstart');

  const quickstartSteps = [
    {
      title: 'OS イメージ書き込み',
      code: `# Raspberry Pi Imager で Ubuntu 22.04 Server (64-bit) を microSD に書き込み
# hostname: giemon, user: giemon, SSH: 有効化`,
    },
    {
      title: 'SSH 接続 & パッケージ更新',
      code: `ssh giemon@giemon.local
sudo apt update && sudo apt upgrade -y`,
    },
    {
      title: 'ROS2 Humble インストール',
      code: `sudo apt install -y software-properties-common
sudo add-apt-repository universe
sudo curl -sSL https://raw.githubusercontent.com/ros/rosdistro/master/ros.key \\
  -o /usr/share/keyrings/ros-archive-keyring.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/ros-archive-keyring.gpg] \\
  http://packages.ros.org/ros2/ubuntu jammy main" | sudo tee /etc/apt/sources.list.d/ros2.list
sudo apt update && sudo apt install -y ros-humble-desktop ros-humble-moveit`,
    },
    {
      title: 'Otete ファームウェアクローン',
      code: `cd ~
git clone https://github.com/etzhayyim/otete.git
cd otete
pip3 install -r firmware/armcrawler/requirements.txt`,
    },
    {
      title: 'サーボスキャン確認',
      code: `python firmware/armcrawler/servo/ics_driver.py --scan
# 期待出力:
# Found servo IDs: [1, 2, 3, 4, 5, 6, 7]
# J1=1 J2=2 J3=3 J4=4 J5=5 J6=6 Gripper=7`,
    },
    {
      title: 'クローラーテスト',
      code: `python firmware/armcrawler/crawler/motor_driver.py --test
# 前進 → 停止 → 後進 → 停止 → 左回転 → 右回転 の順に動作確認`,
    },
    {
      title: 'ROS2 起動',
      code: `source /opt/ros/humble/setup.bash
cd ~/otete
colcon build --packages-select otete_ros2
source install/setup.bash
ros2 launch otete_ros2 bringup.launch.py`,
    },
  ];

  const ros2Topics = [
    { topic: '/otete/joint_states', type: 'sensor_msgs/JointState', dir: 'pub', desc: '7軸の現在角度・速度・トルク' },
    { topic: '/otete/joint_command', type: 'trajectory_msgs/JointTrajectory', dir: 'sub', desc: '軌道コマンド送信' },
    { topic: '/otete/crawler/cmd_vel', type: 'geometry_msgs/Twist', dir: 'sub', desc: 'クローラー速度指令 (linear.x, angular.z)' },
    { topic: '/otete/crawler/odom', type: 'nav_msgs/Odometry', dir: 'pub', desc: 'クローラーオドメトリ' },
    { topic: '/otete/imu', type: 'sensor_msgs/Imu', dir: 'pub', desc: 'ICM-42688-P 6軸 IMU データ' },
    { topic: '/otete/tof_range', type: 'sensor_msgs/Range', dir: 'pub', desc: 'VL53L4CX ToF 距離センサ (m)' },
    { topic: '/otete/camera/image_raw', type: 'sensor_msgs/Image', dir: 'pub', desc: 'RPi Camera v3 画像ストリーム' },
    { topic: '/otete/diagnostics', type: 'diagnostic_msgs/DiagnosticArray', dir: 'pub', desc: 'バッテリー電圧・温度・エラー' },
  ];

  const pythonApi = [
    {
      cls: 'ICSDriver',
      module: 'firmware.armcrawler.servo.ics_driver',
      methods: [
        { sig: 'scan() → list[int]', desc: '接続中のサーボ ID 一覧を返す' },
        { sig: 'move(id: int, deg: float, time_ms: int)', desc: '指定サーボを deg 度へ time_ms ミリ秒で移動' },
        { sig: 'read_angle(id: int) → float', desc: '現在角度 (度) を返す' },
        { sig: 'torque_on(id: int)', desc: 'トルク有効化' },
        { sig: 'torque_off(id: int)', desc: 'トルク無効化（脱力）' },
      ],
    },
    {
      cls: 'ArmIK',
      module: 'firmware.armcrawler.kinematics.ik',
      methods: [
        { sig: 'solve(target: np.ndarray, orientation: np.ndarray) → np.ndarray', desc: '6x1 関節角度ベクトルを返す (DLS 法)' },
        { sig: 'forward(joints: np.ndarray) → np.ndarray', desc: '順運動学: エンドエフェクタ SE3 行列を返す' },
      ],
    },
    {
      cls: 'MotorDriver',
      module: 'firmware.armcrawler.crawler.motor_driver',
      methods: [
        { sig: 'drive(left: float, right: float)', desc: '左右モーター速度 -1.0 〜 1.0 を設定' },
        { sig: 'stop()', desc: '即時停止（inertia brake）' },
        { sig: 'set_speed(linear: float, angular: float)', desc: 'diff-drive モデルで速度指令 (m/s, rad/s)' },
      ],
    },
  ];
</script>

<svelte:head>
  <title>ファームウェア | Giemon Otete</title>
  <meta name="description" content="Giemon Otete のファームウェアドキュメント。クイックスタート、ROS2 トピック一覧、Python API リファレンス。" />
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
  <div class="max-w-3xl mx-auto text-center">
    <div class="text-4xl mb-4">💾</div>
    <h1 class="text-3xl sm:text-4xl font-extrabold text-white mb-3">ファームウェア</h1>
    <p class="text-slate-400 text-sm">ROS2 Humble + Python SDK + ICS3.5 サーボドライバー</p>
  </div>
</section>

<!-- ─── Tabs ─── -->
<div class="sticky top-[49px] z-40 bg-[#0d1117] border-b border-[#21262d] px-5">
  <div class="max-w-3xl mx-auto flex gap-1">
    {#each [['quickstart','クイックスタート'],['ros2','ROS2 トピック'],['api','Python API']] as [id, label]}
      <button
        onclick={() => activeTab = id as typeof activeTab}
        class={[
          'px-4 py-3 text-sm font-medium border-b-2 transition-colors',
          activeTab === id
            ? 'border-[#e85d04] text-white'
            : 'border-transparent text-slate-400 hover:text-white'
        ].join(' ')}
      >
        {label}
      </button>
    {/each}
  </div>
</div>

<div class="max-w-3xl mx-auto px-5 py-12">

  <!-- Quickstart -->
  {#if activeTab === 'quickstart'}
    <div class="space-y-8">
      <p class="text-slate-400 text-sm">
        Raspberry Pi 5 に Ubuntu 22.04 + ROS2 Humble をセットアップし、Otete ファームウェアを起動するまでの手順です。
      </p>
      {#each quickstartSteps as step, i}
        <div>
          <div class="flex items-center gap-2 mb-2">
            <span class="w-6 h-6 flex items-center justify-center rounded-full bg-[#e85d04] text-white text-xs font-bold">{i + 1}</span>
            <h3 class="text-white font-semibold text-sm">{step.title}</h3>
          </div>
          <pre class="bg-[#161b22] border border-[#21262d] rounded-xl p-4 text-[#7ee787] text-xs font-mono overflow-x-auto leading-relaxed">{step.code}</pre>
        </div>
      {/each}
      <div class="p-4 bg-[#0f2415] border border-[#238636]/40 rounded-xl text-slate-300 text-sm">
        ✅ <strong>完了</strong>: RViz でアームの可視化を確認したら <a href="/assembly" class="text-[#e85d04] hover:underline">組立マニュアル</a> に戻るか、GitHub の <code class="text-[#7ee787]">examples/</code> フォルダのサンプルを試してください。
      </div>
    </div>

  <!-- ROS2 Topics -->
  {:else if activeTab === 'ros2'}
    <div>
      <p class="text-slate-400 text-sm mb-6">
        <code class="text-[#7ee787] bg-[#161b22] px-1.5 py-0.5 rounded">ros2 launch otete_ros2 bringup.launch.py</code> 起動後に利用可能なトピック一覧。
      </p>
      <div class="overflow-x-auto">
        <table class="w-full text-sm border-collapse">
          <thead>
            <tr class="border-b border-[#21262d]">
              <th class="text-left py-2 pr-4 text-slate-400 font-medium text-xs">トピック</th>
              <th class="text-left py-2 pr-4 text-slate-400 font-medium text-xs">型</th>
              <th class="text-left py-2 pr-4 text-slate-400 font-medium text-xs">方向</th>
              <th class="text-left py-2 text-slate-400 font-medium text-xs">説明</th>
            </tr>
          </thead>
          <tbody>
            {#each ros2Topics as t}
              <tr class="border-b border-[#21262d]/50 hover:bg-[#161b22] transition-colors">
                <td class="py-2.5 pr-4 font-mono text-[#7ee787] text-xs">{t.topic}</td>
                <td class="py-2.5 pr-4 text-slate-400 text-xs">{t.type}</td>
                <td class="py-2.5 pr-4">
                  <span class={t.dir === 'pub' ? 'text-[#58a6ff] text-xs' : 'text-[#f78166] text-xs'}>
                    {t.dir === 'pub' ? '↑ pub' : '↓ sub'}
                  </span>
                </td>
                <td class="py-2.5 text-slate-400 text-xs">{t.desc}</td>
              </tr>
            {/each}
          </tbody>
        </table>
      </div>
      <div class="mt-8 p-4 bg-[#161b22] border border-[#21262d] rounded-xl">
        <p class="text-slate-400 text-xs mb-2">MoveIt! 2 との連携:</p>
        <pre class="text-[#7ee787] text-xs font-mono">ros2 launch otete_ros2 moveit.launch.py
# → /move_group アクションサーバーが起動
# → RViz の MotionPlanning プラグインで軌道計画可能</pre>
      </div>
    </div>

  <!-- Python API -->
  {:else}
    <div class="space-y-10">
      <p class="text-slate-400 text-sm">
        ROS2 を使わず Python から直接ハードウェアを制御する場合の API リファレンスです。
      </p>
      {#each pythonApi as cls}
        <div>
          <div class="mb-1">
            <span class="text-[#79c0ff] font-mono text-sm font-bold">{cls.cls}</span>
            <span class="text-slate-500 text-xs ml-2">from {cls.module}</span>
          </div>
          <div class="border border-[#21262d] rounded-xl overflow-hidden">
            {#each cls.methods as m, i}
              <div class={['p-3 text-sm', i > 0 ? 'border-t border-[#21262d]' : ''].join(' ')}>
                <code class="text-[#7ee787] font-mono text-xs">{m.sig}</code>
                <p class="text-slate-400 text-xs mt-1">{m.desc}</p>
              </div>
            {/each}
          </div>
        </div>
      {/each}
      <div class="p-4 bg-[#161b22] border border-[#21262d] rounded-xl">
        <p class="text-slate-400 text-xs mb-2">使用例:</p>
        <pre class="text-[#7ee787] text-xs font-mono">{`from firmware.armcrawler.servo.ics_driver import ICSDriver
from firmware.armcrawler.kinematics.ik import ArmIK
import numpy as np

arm = ICSDriver('/dev/ttyAMA0', baudrate=115200)
ik = ArmIK()

# ホームポジションへ移動
for sid in arm.scan():
    arm.move(sid, 0.0, 1000)

# IK でエンドエフェクタを指定座標へ
target = np.array([200, 0, 150])  # mm
joints = ik.solve(target, np.eye(3))
for i, angle in enumerate(joints):
    arm.move(i + 1, float(np.degrees(angle)), 800)`}</pre>
      </div>
    </div>
  {/if}

</div>

<!-- ─── Footer ─── -->
<footer class="bg-[#080b10] border-t border-[#21262d] py-8 px-5 text-center text-[11px] text-slate-600">
  © 2026 amanomibashira — Giemon Otete は Apache-2.0 ライセンスで公開
</footer>
