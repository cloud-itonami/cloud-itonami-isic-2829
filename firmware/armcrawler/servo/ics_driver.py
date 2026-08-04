"""KONDO ICS3.5 bus servo driver for Raspberry Pi 5.

ICS3.5 protocol: half-duplex UART, 115200bps, 3.5 byte framing.
Reference: https://kondo-robot.com/faq/ics35
"""

from __future__ import annotations

import struct
import time
from dataclasses import dataclass
from typing import Optional

import serial


ICS_BAUD = 115200
ICS_CMD_POSITION = 0x80
ICS_CMD_READ_POS = 0xA0
ICS_CMD_READ_ID = 0xFF
ICS_CMD_WRITE_ID = 0xE0


@dataclass
class ServoStatus:
    servo_id: int
    position: int      # 3500=center, 800..7500 range
    temperature: Optional[int] = None
    current: Optional[int] = None


class ICSBusDriver:
    """Half-duplex UART driver for KONDO ICS3.5 bus servos.

    Requires a half-duplex UART adapter (e.g. ArmCrawlerHAT RS485/ICS port).
    GPIO-based TX-enable line controls bus direction.
    """

    def __init__(self, port: str = "/dev/ttyAMA0", tx_enable_gpio: int = 17):
        self._port = port
        self._tx_enable_gpio = tx_enable_gpio
        self._ser: Optional[serial.Serial] = None

    def open(self) -> None:
        try:
            import RPi.GPIO as GPIO
            GPIO.setmode(GPIO.BCM)
            GPIO.setup(self._tx_enable_gpio, GPIO.OUT, initial=GPIO.LOW)
            self._gpio = GPIO
        except ImportError:
            self._gpio = None  # allow unit tests without RPi

        self._ser = serial.Serial(
            self._port,
            baudrate=ICS_BAUD,
            bytesize=serial.EIGHTBITS,
            parity=serial.PARITY_EVEN,
            stopbits=serial.STOPBITS_ONE,
            timeout=0.05,
        )

    def close(self) -> None:
        if self._ser:
            self._ser.close()

    def _tx(self, data: bytes) -> None:
        if self._gpio:
            self._gpio.output(self._tx_enable_gpio, self._gpio.HIGH)
            time.sleep(0.0001)
        self._ser.write(data)
        self._ser.flush()
        if self._gpio:
            time.sleep(len(data) * 10 / ICS_BAUD + 0.0001)
            self._gpio.output(self._tx_enable_gpio, self._gpio.LOW)

    def set_position(self, servo_id: int, position: int) -> Optional[int]:
        """Command servo to position (800..7500, center=3500). Returns echo position."""
        position = max(800, min(7500, position))
        h = ((position >> 7) & 0x7F) | ICS_CMD_POSITION | (servo_id & 0x1F)
        l = position & 0x7F
        self._tx(bytes([h, l, l]))
        resp = self._ser.read(3)
        if len(resp) == 3:
            return ((resp[0] & 0x7F) << 7) | (resp[2] & 0x7F)
        return None

    def get_position(self, servo_id: int) -> Optional[int]:
        h = ICS_CMD_READ_POS | (servo_id & 0x1F)
        self._tx(bytes([h, 0, 0]))
        resp = self._ser.read(3)
        if len(resp) == 3:
            return ((resp[0] & 0x7F) << 7) | (resp[2] & 0x7F)
        return None

    def scan(self, id_range: range = range(1, 32)) -> list[int]:
        """Return list of responding servo IDs."""
        found = []
        for sid in id_range:
            pos = self.get_position(sid)
            if pos is not None:
                found.append(sid)
        return found

    @staticmethod
    def deg_to_ics(degrees: float) -> int:
        """Convert joint angle in degrees to ICS position value (center=0°)."""
        return int(3500 + degrees * (7500 - 800) / 270)

    @staticmethod
    def ics_to_deg(ics: int) -> float:
        return (ics - 3500) * 270 / (7500 - 800)
