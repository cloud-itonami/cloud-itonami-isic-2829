"""
Urban mining inspection classifier.

Input and output are JSON strings on std_msgs/String so the pilot can run
without a custom ROS interface package.
"""

import json

import rclpy
from rclpy.node import Node
from std_msgs.msg import String

from armcrawler_ros2.urban_mining_core import DEFAULT_RULES, classify_inspection


class UrbanMiningClassifierNode(Node):
    def __init__(self):
        super().__init__('urban_mining_classifier')
        self.declare_parameter('low_confidence_threshold', 0.68)
        self.declare_parameter('battery_labels', ['battery-visible', 'swollen-battery', 'li-ion'])
        self.declare_parameter('stream_rules', DEFAULT_RULES)

        self._low_conf = float(self.get_parameter('low_confidence_threshold').value)
        self._battery_labels = set(self.get_parameter('battery_labels').value)
        self._rules = self.get_parameter('stream_rules').value or DEFAULT_RULES

        self._pub = self.create_publisher(String, '/urban_mining/classification', 10)
        self.create_subscription(String, '/urban_mining/inspection', self._on_inspection, 10)
        self.get_logger().info('urban_mining_classifier ready')

    def _on_inspection(self, msg: String):
        try:
            inspection = json.loads(msg.data)
            result = classify_inspection(inspection, self._rules, self._battery_labels, self._low_conf)
        except Exception as exc:
            self.get_logger().error(f'inspection classification failed: {exc}')
            result = {
                'event_type': 'classification_error',
                'error': str(exc),
                'destination_bin': 'manual_review',
                'confidence': 0.0,
                'policy': 'manual_review',
            }

        out = String()
        out.data = json.dumps(result, sort_keys=True)
        self._pub.publish(out)


def main(args=None):
    rclpy.init(args=args)
    node = UrbanMiningClassifierNode()
    try:
        rclpy.spin(node)
    finally:
        node.destroy_node()
        rclpy.shutdown()


if __name__ == '__main__':
    main()
