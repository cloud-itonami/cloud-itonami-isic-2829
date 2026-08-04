"""
ArmCrawlerJP — Crawler drive ROS2 node.

Subscriptions:
  /cmd_vel  (geometry_msgs/Twist) — linear.x (m/s), angular.z (rad/s)
                                    mapped to left/right motor duty [-1, 1]

Publications:
  /odom     (nav_msgs/Odometry)   — dead-reckoning at 20 Hz (encoder-less estimate)
  /crawler/status (std_msgs/String)

Parameters:
  wheel_base_m  (float) — track separation, default 0.18 m
  max_speed     (float) — duty cycle at full cmd_vel, default 1.0
  pwm_freq_hz   (int)   — default 20000
"""

import math
import os
import sys
import time

import rclpy
from rclpy.node import Node

from geometry_msgs.msg import Twist, TransformStamped
from nav_msgs.msg import Odometry
from std_msgs.msg import String
import tf2_ros

_fw_root = os.path.join(os.path.dirname(__file__), '..', '..', '..')
sys.path.insert(0, os.path.abspath(_fw_root))

from armcrawler.crawler.motor_driver import CrawlerDriver


class CrawlerNode(Node):
    def __init__(self):
        super().__init__('crawler')

        self.declare_parameter('wheel_base_m', 0.18)
        self.declare_parameter('max_speed', 1.0)
        self.declare_parameter('pwm_freq_hz', 20000)

        self._wheel_base = self.get_parameter('wheel_base_m').value
        self._max_speed = self.get_parameter('max_speed').value

        self._driver = CrawlerDriver(
            pwm_freq=self.get_parameter('pwm_freq_hz').value)
        self._driver.stop()

        # dead-reckoning state
        self._x = 0.0
        self._y = 0.0
        self._theta = 0.0
        self._last_ts = time.monotonic()
        self._vl = 0.0
        self._vr = 0.0

        self._odom_pub = self.create_publisher(Odometry, '/odom', 10)
        self._status_pub = self.create_publisher(String, '/crawler/status', 10)
        self._tf_broadcaster = tf2_ros.TransformBroadcaster(self)

        self.create_subscription(Twist, '/cmd_vel', self._on_cmd_vel, 10)
        self.create_timer(0.05, self._update_odometry)  # 20 Hz

        # watchdog: stop motors if no cmd_vel for 0.5 s
        self._last_cmd_ts = time.monotonic()
        self.create_timer(0.1, self._watchdog)

        self.get_logger().info('crawler ready')

    # ── callbacks ──────────────────────────────────────────────────────────

    def _on_cmd_vel(self, msg: Twist):
        self._last_cmd_ts = time.monotonic()
        v = msg.linear.x    # m/s (forward positive)
        w = msg.angular.z   # rad/s (CCW positive)

        # differential drive mixing: v_l = v - w*b/2, v_r = v + w*b/2
        vl = v - w * self._wheel_base / 2.0
        vr = v + w * self._wheel_base / 2.0

        # normalise to [-1, 1]
        scale = max(abs(vl), abs(vr), self._max_speed)
        self._vl = vl / scale
        self._vr = vr / scale
        self._driver.drive(self._vl, self._vr)

    def _update_odometry(self):
        now = time.monotonic()
        dt = now - self._last_ts
        self._last_ts = now

        # encoder-less dead reckoning (constant velocity assumption per step)
        v = (self._vl + self._vr) / 2.0
        w = (self._vr - self._vl) / self._wheel_base

        self._x += v * math.cos(self._theta) * dt
        self._y += v * math.sin(self._theta) * dt
        self._theta += w * dt

        stamp = self.get_clock().now().to_msg()

        # publish odom
        odom = Odometry()
        odom.header.stamp = stamp
        odom.header.frame_id = 'odom'
        odom.child_frame_id = 'base_link'
        odom.pose.pose.position.x = self._x
        odom.pose.pose.position.y = self._y
        odom.pose.pose.orientation.z = math.sin(self._theta / 2.0)
        odom.pose.pose.orientation.w = math.cos(self._theta / 2.0)
        odom.twist.twist.linear.x = v
        odom.twist.twist.angular.z = w
        self._odom_pub.publish(odom)

        # broadcast TF odom → base_link
        tf = TransformStamped()
        tf.header.stamp = stamp
        tf.header.frame_id = 'odom'
        tf.child_frame_id = 'base_link'
        tf.transform.translation.x = self._x
        tf.transform.translation.y = self._y
        tf.transform.rotation.z = math.sin(self._theta / 2.0)
        tf.transform.rotation.w = math.cos(self._theta / 2.0)
        self._tf_broadcaster.sendTransform(tf)

    def _watchdog(self):
        if time.monotonic() - self._last_cmd_ts > 0.5:
            self._vl = 0.0
            self._vr = 0.0
            self._driver.stop()


def main(args=None):
    rclpy.init(args=args)
    node = CrawlerNode()
    try:
        rclpy.spin(node)
    finally:
        node._driver.stop()
        node.destroy_node()
        rclpy.shutdown()


if __name__ == '__main__':
    main()
