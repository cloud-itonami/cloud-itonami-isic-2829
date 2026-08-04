"""
ArmCrawlerJP — Raspberry Pi Camera (picamera2) ROS2 node.

Publications:
  /camera/image_raw        (sensor_msgs/Image)       — BGRA8 or RGB8 at target_fps
  /camera/camera_info      (sensor_msgs/CameraInfo)  — intrinsic parameters

Parameters:
  width        (int)   — default 640
  height       (int)   — default 480
  target_fps   (int)   — default 30
  encoding     (str)   — "bgr8" | "rgb8" — default "bgr8"
  camera_frame (str)   — TF frame id, default "camera_link"
"""

import rclpy
from rclpy.node import Node
from sensor_msgs.msg import Image, CameraInfo

try:
    from picamera2 import Picamera2
    import numpy as np
    _PICAM_AVAILABLE = True
except ImportError:
    _PICAM_AVAILABLE = False


class CameraNode(Node):
    def __init__(self):
        super().__init__('camera')

        self.declare_parameter('width', 640)
        self.declare_parameter('height', 480)
        self.declare_parameter('target_fps', 30)
        self.declare_parameter('encoding', 'bgr8')
        self.declare_parameter('camera_frame', 'camera_link')

        self._w = self.get_parameter('width').value
        self._h = self.get_parameter('height').value
        self._fps = self.get_parameter('target_fps').value
        self._enc = self.get_parameter('encoding').value
        self._frame_id = self.get_parameter('camera_frame').value

        self._img_pub = self.create_publisher(Image, '/camera/image_raw', 5)
        self._info_pub = self.create_publisher(CameraInfo, '/camera/camera_info', 5)

        self._picam = None
        if _PICAM_AVAILABLE:
            self._picam = Picamera2()
            cfg = self._picam.create_video_configuration(
                main={'size': (self._w, self._h), 'format': 'BGR888'})
            self._picam.configure(cfg)
            self._picam.start()
            self.get_logger().info(f'camera started {self._w}x{self._h}@{self._fps}fps')
        else:
            self.get_logger().warn('picamera2 not available — publishing blank frames')

        self._camera_info = self._build_camera_info()
        self.create_timer(1.0 / self._fps, self._capture)

    # ── helpers ────────────────────────────────────────────────────────────

    def _build_camera_info(self) -> CameraInfo:
        info = CameraInfo()
        info.header.frame_id = self._frame_id
        info.width = self._w
        info.height = self._h
        # Pinhole model placeholder — calibrate with camera_calibration pkg
        fx = fy = float(self._w)  # rough approximation: f ≈ width pixels
        cx = self._w / 2.0
        cy = self._h / 2.0
        info.k = [fx, 0.0, cx, 0.0, fy, cy, 0.0, 0.0, 1.0]
        info.p = [fx, 0.0, cx, 0.0, 0.0, fy, cy, 0.0, 0.0, 0.0, 1.0, 0.0]
        info.distortion_model = 'plumb_bob'
        info.d = [0.0, 0.0, 0.0, 0.0, 0.0]
        return info

    def _capture(self):
        stamp = self.get_clock().now().to_msg()

        if self._picam is not None:
            try:
                import numpy as np
                frame = self._picam.capture_array('main')
                msg = Image()
                msg.header.stamp = stamp
                msg.header.frame_id = self._frame_id
                msg.height = self._h
                msg.width = self._w
                msg.encoding = self._enc
                msg.is_bigendian = 0
                msg.step = self._w * 3
                msg.data = frame.tobytes()
                self._img_pub.publish(msg)
            except Exception as e:
                self.get_logger().warn(f'capture error: {e}')
        else:
            # publish blank frame for testing without hardware
            import array as arr
            msg = Image()
            msg.header.stamp = stamp
            msg.header.frame_id = self._frame_id
            msg.height = self._h
            msg.width = self._w
            msg.encoding = self._enc
            msg.step = self._w * 3
            msg.data = arr.array('B', [0] * (self._w * self._h * 3)).tobytes()
            self._img_pub.publish(msg)

        self._camera_info.header.stamp = stamp
        self._info_pub.publish(self._camera_info)


def main(args=None):
    rclpy.init(args=args)
    node = CameraNode()
    try:
        rclpy.spin(node)
    finally:
        if node._picam:
            node._picam.stop()
        node.destroy_node()
        rclpy.shutdown()


if __name__ == '__main__':
    main()
