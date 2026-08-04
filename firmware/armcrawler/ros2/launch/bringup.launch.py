"""
ArmCrawlerJP — full bringup launch file.

Usage:
  ros2 launch armcrawler_ros2 bringup.launch.py
  ros2 launch armcrawler_ros2 bringup.launch.py sim:=true   # no hardware
  ros2 launch armcrawler_ros2 bringup.launch.py arm_only:=true
"""

from launch import LaunchDescription
from launch.actions import DeclareLaunchArgument, GroupAction, OpaqueFunction
from launch.conditions import IfCondition, UnlessCondition
from launch.substitutions import LaunchConfiguration, PathJoinSubstitution
from launch_ros.actions import Node
from launch_ros.substitutions import FindPackageShare


def generate_launch_description():
    pkg = FindPackageShare('armcrawler_ros2')

    sim_arg = DeclareLaunchArgument('sim', default_value='false',
                                    description='Run in simulation (no hardware I/O)')
    arm_only_arg = DeclareLaunchArgument('arm_only', default_value='false',
                                         description='Launch arm controller only')

    params_file = PathJoinSubstitution([pkg, 'config', 'params.yaml'])

    arm_node = Node(
        package='armcrawler_ros2',
        executable='arm_controller',
        name='arm_controller',
        output='screen',
        parameters=[params_file],
    )

    crawler_node = Node(
        package='armcrawler_ros2',
        executable='crawler',
        name='crawler',
        output='screen',
        parameters=[params_file],
        condition=UnlessCondition(LaunchConfiguration('arm_only')),
    )

    camera_node = Node(
        package='armcrawler_ros2',
        executable='camera',
        name='camera',
        output='screen',
        parameters=[params_file],
        condition=UnlessCondition(LaunchConfiguration('arm_only')),
    )

    imu_node = Node(
        package='armcrawler_ros2',
        executable='imu',
        name='imu',
        output='screen',
        parameters=[params_file],
        condition=UnlessCondition(LaunchConfiguration('arm_only')),
    )

    # Static TF: base_link → camera_link (camera mounted at front-top of chassis)
    static_tf_camera = Node(
        package='tf2_ros',
        executable='static_transform_publisher',
        name='static_tf_camera',
        arguments=['0.10', '0.0', '0.12', '0', '0.2618', '0',
                   'base_link', 'camera_link'],  # 15° downward tilt
        condition=UnlessCondition(LaunchConfiguration('arm_only')),
    )

    # Static TF: base_link → imu_link (IMU is on HAT, centre of chassis)
    static_tf_imu = Node(
        package='tf2_ros',
        executable='static_transform_publisher',
        name='static_tf_imu',
        arguments=['0.0', '0.0', '0.03', '0', '0', '0',
                   'base_link', 'imu_link'],
        condition=UnlessCondition(LaunchConfiguration('arm_only')),
    )

    # Static TF: base_link → arm_base_link (arm turntable J1 mount point)
    static_tf_arm = Node(
        package='tf2_ros',
        executable='static_transform_publisher',
        name='static_tf_arm',
        arguments=['0.0', '0.0', '0.08', '0', '0', '0',
                   'base_link', 'arm_base_link'],
    )

    return LaunchDescription([
        sim_arg,
        arm_only_arg,
        arm_node,
        crawler_node,
        camera_node,
        imu_node,
        static_tf_camera,
        static_tf_imu,
        static_tf_arm,
    ])
