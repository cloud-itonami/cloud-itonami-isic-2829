from setuptools import find_packages, setup
import os
from glob import glob

package_name = 'armcrawler_ros2'

setup(
    name=package_name,
    version='0.1.0',
    packages=find_packages(exclude=['test']),
    data_files=[
        ('share/ament_index/resource_index/packages', ['resource/' + package_name]),
        ('share/' + package_name, ['package.xml']),
        (os.path.join('share', package_name, 'launch'), glob('launch/*.launch.py')),
        (os.path.join('share', package_name, 'config'), glob('config/*.yaml')),
    ],
    install_requires=['setuptools'],
    zip_safe=True,
    maintainer='etzhayyim Japan',
    maintainer_email='firmware@etzhayyim.com',
    description='ArmCrawler JP ROS2 nodes',
    license='Apache-2.0',
    tests_require=['pytest'],
    entry_points={
        'console_scripts': [
            'arm_controller = armcrawler_ros2.arm_controller_node:main',
            'crawler        = armcrawler_ros2.crawler_node:main',
            'camera         = armcrawler_ros2.camera_node:main',
            'imu            = armcrawler_ros2.imu_node:main',
            'urban_mining_classifier = armcrawler_ros2.urban_mining_classifier_node:main',
            'urban_mining_sorter     = armcrawler_ros2.urban_mining_sorter_node:main',
        ],
    },
)
