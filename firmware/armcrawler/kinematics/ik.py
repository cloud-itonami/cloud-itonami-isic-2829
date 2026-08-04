"""6-DOF inverse kinematics for ArmCrawler JP.

Modified DH parameters (see cad-spec/mechanical-spec-v1.md §3.1):

Joint | a(mm) | d(mm) | alpha(deg)
J1    |   0   |  85   |    0
J2    |   0   | 120   |   90
J3    | 160   |   0   |    0
J4    |  40   |   0   |   90
J5    |   0   |  80   |  -90
J6    |   0   |  30   |   90

Solver: numerical Jacobian pseudo-inverse (damped least squares).
"""

from __future__ import annotations

import math
from dataclasses import dataclass, field

import numpy as np


# DH parameters: (a, d, alpha_deg)
_DH = [
    (0.0,   85.0,   0.0),
    (0.0,  120.0,  90.0),
    (160.0,  0.0,   0.0),
    (40.0,   0.0,  90.0),
    (0.0,   80.0, -90.0),
    (0.0,   30.0,  90.0),
]

# Joint limits in degrees
JOINT_LIMITS_DEG = [
    (-180.0, 180.0),   # J1
    (-10.0,  100.0),   # J2
    (-10.0,  120.0),   # J3
    (-90.0,   90.0),   # J4
    (-180.0, 180.0),   # J5
    (-180.0, 180.0),   # J6
]


def _dh_transform(a: float, d: float, alpha_deg: float, theta_deg: float) -> np.ndarray:
    alpha = math.radians(alpha_deg)
    theta = math.radians(theta_deg)
    ca, sa = math.cos(alpha), math.sin(alpha)
    ct, st = math.cos(theta), math.sin(theta)
    return np.array([
        [ct,      -st,       0,    a],
        [st*ca,   ct*ca,   -sa, -sa*d],
        [st*sa,   ct*sa,    ca,  ca*d],
        [0,       0,         0,    1],
    ])


def forward_kinematics(joint_angles_deg: list[float]) -> np.ndarray:
    """Compute 4×4 end-effector transform from joint angles (degrees)."""
    T = np.eye(4)
    for (a, d, alpha), theta in zip(_DH, joint_angles_deg):
        T = T @ _dh_transform(a, d, alpha, theta)
    return T


def _jacobian(angles_deg: list[float], delta: float = 0.01) -> np.ndarray:
    """Numerical 6×6 Jacobian (position 3 rows + orientation 3 rows)."""
    T0 = forward_kinematics(angles_deg)
    J = np.zeros((6, 6))
    for i in range(6):
        a_plus = list(angles_deg)
        a_plus[i] += delta
        T_plus = forward_kinematics(a_plus)
        dp = (T_plus[:3, 3] - T0[:3, 3]) / delta
        # rotation axis delta via skew of R_delta
        dR = (T_plus[:3, :3] - T0[:3, :3]) / delta @ T0[:3, :3].T
        dw = np.array([dR[2, 1], dR[0, 2], dR[1, 0]])
        J[:3, i] = dp
        J[3:, i] = dw
    return J


def inverse_kinematics(
    target_pos_mm: list[float],
    target_rpy_deg: list[float],
    initial_angles_deg: list[float] | None = None,
    max_iter: int = 200,
    tol: float = 0.5,
    damping: float = 0.1,
) -> tuple[list[float], bool]:
    """Damped least-squares IK solver.

    Returns (joint_angles_deg, converged).
    target_rpy_deg: [roll, pitch, yaw] in degrees.
    """
    from scipy.spatial.transform import Rotation

    angles = list(initial_angles_deg or [0.0] * 6)
    target_T = np.eye(4)
    target_T[:3, 3] = target_pos_mm
    target_T[:3, :3] = Rotation.from_euler("xyz", target_rpy_deg, degrees=True).as_matrix()

    for _ in range(max_iter):
        T = forward_kinematics(angles)
        dp = target_T[:3, 3] - T[:3, 3]
        dR = target_T[:3, :3] @ T[:3, :3].T
        r = Rotation.from_matrix(dR)
        dw = r.as_rotvec()
        err = np.concatenate([dp, dw])

        if np.linalg.norm(err[:3]) < tol:
            return angles, True

        J = _jacobian(angles)
        JT = J.T
        delta = JT @ np.linalg.solve(J @ JT + damping**2 * np.eye(6), err)
        angles = [
            float(np.clip(a + math.degrees(da), lo, hi))
            for a, da, (lo, hi) in zip(angles, delta, JOINT_LIMITS_DEG)
        ]

    return angles, False
