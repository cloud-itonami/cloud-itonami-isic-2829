"""
ArmCrawlerJP — 6-DOF arm controller ROS2 node.

Subscriptions:
  /arm/joint_trajectory  (trajectory_msgs/JointTrajectory)  — execute joint-space trajectory
  /arm/cartesian_target  (geometry_msgs/Pose)               — IK → execute

Publications:
  /arm/joint_states      (sensor_msgs/JointState)           — current angles at 50 Hz
  /arm/status            (std_msgs/String)                  — "ready" | "moving" | "error"

Parameters:
  servo_port  (str)   — default "/dev/ttyAMA0"
  tx_en_gpio  (int)   — default 17
  servo_ids   (list)  — default [1,2,3,4,5,6]
  ik_tol_mm   (float) — default 0.5
"""

import sys
import os

import rclpy
from rclpy.node import Node
from rclpy.executors import MultiThreadedExecutor
from rclpy.callback_groups import ReentrantCallbackGroup

from sensor_msgs.msg import JointState
from trajectory_msgs.msg import JointTrajectory
from geometry_msgs.msg import Pose
from std_msgs.msg import String

# Firmware modules live two levels up from this package
_fw_root = os.path.join(os.path.dirname(__file__), '..', '..', '..')
sys.path.insert(0, os.path.abspath(_fw_root))

from armcrawler.servo.ics_driver import ICSBusDriver
from armcrawler.kinematics.ik import forward_kinematics, inverse_kinematics


_JOINT_NAMES = ['joint1', 'joint2', 'joint3', 'joint4', 'joint5', 'joint6']
_HOME_ANGLES = [0.0, 45.0, 90.0, 0.0, 0.0, 0.0]


class ArmControllerNode(Node):
    def __init__(self):
        super().__init__('arm_controller')

        self.declare_parameter('servo_port', '/dev/ttyAMA0')
        self.declare_parameter('tx_en_gpio', 17)
        self.declare_parameter('servo_ids', [1, 2, 3, 4, 5, 6])
        self.declare_parameter('ik_tol_mm', 0.5)

        port = self.get_parameter('servo_port').value
        tx_en = self.get_parameter('tx_en_gpio').value
        self._servo_ids = self.get_parameter('servo_ids').value
        self._ik_tol = self.get_parameter('ik_tol_mm').value

        self._driver = ICSBusDriver(port=port, tx_en_gpio=tx_en)
        self._current_angles = list(_HOME_ANGLES)
        self._moving = False

        cb = ReentrantCallbackGroup()

        self._js_pub = self.create_publisher(JointState, '/arm/joint_states', 10)
        self._status_pub = self.create_publisher(String, '/arm/status', 10)

        self.create_subscription(
            JointTrajectory, '/arm/joint_trajectory',
            self._on_trajectory, 10, callback_group=cb)
        self.create_subscription(
            Pose, '/arm/cartesian_target',
            self._on_cartesian_target, 10, callback_group=cb)

        self.create_timer(0.02, self._publish_joint_states)  # 50 Hz
        self.get_logger().info('arm_controller ready')

    # ── callbacks ──────────────────────────────────────────────────────────

    def _on_trajectory(self, msg: JointTrajectory):
        if not msg.points:
            return
        self._moving = True
        self._publish_status('moving')
        try:
            for point in msg.points:
                angles_deg = [float(a * 180.0 / 3.14159265) for a in point.positions]
                self._execute_angles(angles_deg)
                if point.time_from_start.nanoseconds > 0:
                    dt = point.time_from_start.nanoseconds / 1e9
                    import time
                    time.sleep(max(0.0, dt - 0.01))
        except Exception as e:
            self.get_logger().error(f'trajectory error: {e}')
            self._publish_status('error')
        finally:
            self._moving = False
            self._publish_status('ready')

    def _on_cartesian_target(self, msg: Pose):
        target_pos = [
            msg.position.x * 1000.0,  # m → mm
            msg.position.y * 1000.0,
            msg.position.z * 1000.0,
        ]
        # Quaternion → RPY (simplified, small-angle approximation for now)
        import math
        q = msg.orientation
        roll = math.atan2(2*(q.w*q.x + q.y*q.z), 1 - 2*(q.x**2 + q.y**2))
        pitch = math.asin(2*(q.w*q.y - q.z*q.x))
        yaw = math.atan2(2*(q.w*q.z + q.x*q.y), 1 - 2*(q.y**2 + q.z**2))
        target_rpy = [math.degrees(roll), math.degrees(pitch), math.degrees(yaw)]

        angles, converged = inverse_kinematics(
            target_pos, target_rpy, self._current_angles, tol=self._ik_tol)
        if not converged:
            self.get_logger().warn('IK did not converge for target pose')
            return
        self._moving = True
        self._publish_status('moving')
        try:
            self._execute_angles(angles)
        finally:
            self._moving = False
            self._publish_status('ready')

    # ── helpers ────────────────────────────────────────────────────────────

    def _execute_angles(self, angles_deg: list[float]):
        for sid, angle in zip(self._servo_ids, angles_deg):
            self._driver.set_position(sid, angle)
        self._current_angles = list(angles_deg)

    def _publish_joint_states(self):
        msg = JointState()
        msg.header.stamp = self.get_clock().now().to_msg()
        msg.name = _JOINT_NAMES
        import math
        msg.position = [a * math.pi / 180.0 for a in self._current_angles]
        self._js_pub.publish(msg)

    def _publish_status(self, status: str):
        msg = String()
        msg.data = status
        self._status_pub.publish(msg)


def main(args=None):
    rclpy.init(args=args)
    node = ArmControllerNode()
    executor = MultiThreadedExecutor(num_threads=2)
    executor.add_node(node)
    try:
        executor.spin()
    finally:
        node.destroy_node()
        rclpy.shutdown()


if __name__ == '__main__':
    main()
