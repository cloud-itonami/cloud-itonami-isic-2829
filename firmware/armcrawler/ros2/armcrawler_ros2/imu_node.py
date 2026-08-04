"""
ArmCrawlerJP — ICM-42688-P IMU ROS2 node.

Reads 6-axis IMU via SPI0 (spidev) and publishes at 100 Hz.

Publications:
  /imu/data_raw  (sensor_msgs/Imu) — accel (m/s²) + gyro (rad/s), no orientation
  /imu/status    (std_msgs/String)

Parameters:
  spi_bus      (int) — default 0
  spi_device   (int) — default 0  (CE0 = GPIO8)
  spi_max_hz   (int) — default 8000000
  accel_range  (int) — ±g range: 2, 4, 8, 16 — default 4
  gyro_range   (int) — ±dps: 250, 500, 1000, 2000 — default 500
"""

import math
import struct
import time

import rclpy
from rclpy.node import Node
from sensor_msgs.msg import Imu
from std_msgs.msg import String

try:
    import spidev
    _SPIDEV_AVAILABLE = True
except ImportError:
    _SPIDEV_AVAILABLE = False

# ICM-42688-P register map
_REG_PWR_MGMT0 = 0x4E
_REG_GYRO_CONFIG0 = 0x4F
_REG_ACCEL_CONFIG0 = 0x50
_REG_ACCEL_XOUT_H = 0x1F
_REG_GYRO_XOUT_H = 0x25
_REG_WHO_AM_I = 0x75
_WHO_AM_I_VAL = 0x47

_ACCEL_RANGE_LSB = {2: 16384.0, 4: 8192.0, 8: 4096.0, 16: 2048.0}
_GYRO_RANGE_LSB = {250: 131.0, 500: 65.5, 1000: 32.8, 2000: 16.4}
_GYRO_FS_SEL = {250: 0x03, 500: 0x02, 1000: 0x01, 2000: 0x00}
_ACCEL_FS_SEL = {2: 0x03, 4: 0x02, 8: 0x01, 16: 0x00}


class ImuNode(Node):
    def __init__(self):
        super().__init__('imu')

        self.declare_parameter('spi_bus', 0)
        self.declare_parameter('spi_device', 0)
        self.declare_parameter('spi_max_hz', 8_000_000)
        self.declare_parameter('accel_range', 4)
        self.declare_parameter('gyro_range', 500)

        bus = self.get_parameter('spi_bus').value
        dev = self.get_parameter('spi_device').value
        hz = self.get_parameter('spi_max_hz').value
        self._accel_range = self.get_parameter('accel_range').value
        self._gyro_range = self.get_parameter('gyro_range').value

        self._accel_lsb = _ACCEL_RANGE_LSB[self._accel_range]
        self._gyro_lsb = _GYRO_RANGE_LSB[self._gyro_range]

        self._spi = None
        if _SPIDEV_AVAILABLE:
            self._spi = spidev.SpiDev()
            self._spi.open(bus, dev)
            self._spi.max_speed_hz = hz
            self._spi.mode = 3
            self._init_sensor()
        else:
            self.get_logger().warn('spidev not available — publishing zeros')

        self._imu_pub = self.create_publisher(Imu, '/imu/data_raw', 10)
        self._status_pub = self.create_publisher(String, '/imu/status', 10)
        self.create_timer(0.01, self._read_and_publish)  # 100 Hz

        self.get_logger().info('imu node ready')

    # ── SPI helpers ────────────────────────────────────────────────────────

    def _reg_read(self, reg: int, length: int = 1) -> list[int]:
        return self._spi.xfer2([reg | 0x80] + [0x00] * length)[1:]

    def _reg_write(self, reg: int, val: int):
        self._spi.xfer2([reg & 0x7F, val])

    def _init_sensor(self):
        who = self._reg_read(_REG_WHO_AM_I)[0]
        if who != _WHO_AM_I_VAL:
            self.get_logger().error(f'ICM-42688-P not found (WHO_AM_I=0x{who:02x})')
            return
        # Accel + Gyro low-noise mode, temperature sensor on
        self._reg_write(_REG_PWR_MGMT0, 0x0F)
        time.sleep(0.001)
        # Gyro config: ODR 200 Hz
        fs_g = _GYRO_FS_SEL[self._gyro_range]
        self._reg_write(_REG_GYRO_CONFIG0, (fs_g << 4) | 0x06)
        # Accel config: ODR 200 Hz
        fs_a = _ACCEL_FS_SEL[self._accel_range]
        self._reg_write(_REG_ACCEL_CONFIG0, (fs_a << 4) | 0x06)
        time.sleep(0.01)
        self.get_logger().info('ICM-42688-P initialised')

    # ── timer callback ─────────────────────────────────────────────────────

    def _read_and_publish(self):
        msg = Imu()
        msg.header.stamp = self.get_clock().now().to_msg()
        msg.header.frame_id = 'imu_link'
        msg.orientation_covariance[0] = -1.0  # orientation not provided

        if self._spi is not None:
            try:
                raw_a = self._reg_read(_REG_ACCEL_XOUT_H, 6)
                ax, ay, az = [
                    struct.unpack('>h', bytes(raw_a[i:i+2]))[0] / self._accel_lsb * 9.80665
                    for i in (0, 2, 4)
                ]
                raw_g = self._reg_read(_REG_GYRO_XOUT_H, 6)
                gx, gy, gz = [
                    struct.unpack('>h', bytes(raw_g[i:i+2]))[0] / self._gyro_lsb * (math.pi / 180.0)
                    for i in (0, 2, 4)
                ]
                msg.linear_acceleration.x = ax
                msg.linear_acceleration.y = ay
                msg.linear_acceleration.z = az
                msg.angular_velocity.x = gx
                msg.angular_velocity.y = gy
                msg.angular_velocity.z = gz
            except Exception as e:
                self.get_logger().warn(f'IMU read error: {e}')

        self._imu_pub.publish(msg)


def main(args=None):
    rclpy.init(args=args)
    node = ImuNode()
    try:
        rclpy.spin(node)
    finally:
        if node._spi:
            node._spi.close()
        node.destroy_node()
        rclpy.shutdown()


if __name__ == '__main__':
    main()
