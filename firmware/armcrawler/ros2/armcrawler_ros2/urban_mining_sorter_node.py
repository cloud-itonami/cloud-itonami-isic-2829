"""
Urban mining sorter orchestration node.

The node accepts classifier JSON, validates the destination against configured
bin poses, then emits a safe arm pose command and an audit event.
"""

import json

import rclpy
from geometry_msgs.msg import Pose
from rclpy.node import Node
from std_msgs.msg import String

from armcrawler_ros2.urban_mining_core import (
    DEFAULT_BIN_TARGETS,
    build_audit_event,
    build_sort_command,
    quaternion_from_euler,
)


class UrbanMiningSorterNode(Node):
    def __init__(self):
        super().__init__('urban_mining_sorter')
        self.declare_parameter('bin_targets', DEFAULT_BIN_TARGETS)
        self.declare_parameter('arm_ready_status', 'ready')

        self._bin_targets = self.get_parameter('bin_targets').value or DEFAULT_BIN_TARGETS
        self._arm_ready_status = str(self.get_parameter('arm_ready_status').value)
        self._arm_status = self._arm_ready_status

        self._pose_pub = self.create_publisher(Pose, '/arm/cartesian_target', 10)
        self._command_pub = self.create_publisher(String, '/urban_mining/sort_command', 10)
        self._audit_pub = self.create_publisher(String, '/urban_mining/audit_event', 10)
        self.create_subscription(String, '/arm/status', self._on_arm_status, 10)
        self.create_subscription(String, '/urban_mining/classification', self._on_classification, 10)
        self.get_logger().info('urban_mining_sorter ready')

    def _on_arm_status(self, msg: String):
        self._arm_status = msg.data

    def _on_classification(self, msg: String):
        try:
            classification = json.loads(msg.data)
            command = build_sort_command(classification, self._bin_targets, self._arm_status, self._arm_ready_status)
        except Exception as exc:
            self.get_logger().error(f'sort command failed: {exc}')
            command = {
                'event_type': 'sort_rejected',
                'reason': str(exc),
                'destination_bin': 'manual_review',
                'policy': 'manual_review',
            }

        command_msg = String()
        command_msg.data = json.dumps(command, sort_keys=True)
        self._command_pub.publish(command_msg)

        if command.get('event_type') == 'sort_commanded':
            self._pose_pub.publish(pose_from_target(command['target_pose']))

        audit = build_audit_event(classification if 'classification' in locals() else {}, command)
        audit_msg = String()
        audit_msg.data = json.dumps(audit, sort_keys=True)
        self._audit_pub.publish(audit_msg)


def pose_from_target(target: list[float]) -> Pose:
    pose = Pose()
    pose.position.x = float(target[0])
    pose.position.y = float(target[1])
    pose.position.z = float(target[2])
    roll, pitch, yaw = [float(v) for v in target[3:6]]
    qx, qy, qz, qw = quaternion_from_euler(roll, pitch, yaw)
    pose.orientation.x = qx
    pose.orientation.y = qy
    pose.orientation.z = qz
    pose.orientation.w = qw
    return pose


def main(args=None):
    rclpy.init(args=args)
    node = UrbanMiningSorterNode()
    try:
        rclpy.spin(node)
    finally:
        node.destroy_node()
        rclpy.shutdown()


if __name__ == '__main__':
    main()
