from launch import LaunchDescription
from launch_ros.actions import Node
from ament_index_python.packages import get_package_share_directory
import os


def generate_launch_description():
    pkg_share = get_package_share_directory('armcrawler_ros2')
    params = os.path.join(pkg_share, 'config', 'urban_mining_params.yaml')

    return LaunchDescription([
        Node(
            package='armcrawler_ros2',
            executable='urban_mining_classifier',
            name='urban_mining_classifier',
            output='screen',
            parameters=[params],
        ),
        Node(
            package='armcrawler_ros2',
            executable='urban_mining_sorter',
            name='urban_mining_sorter',
            output='screen',
            parameters=[params],
        ),
    ])
