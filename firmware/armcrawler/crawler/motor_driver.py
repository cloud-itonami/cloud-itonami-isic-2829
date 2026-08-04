"""Crawler motor driver via Toshiba TB6612FNG on ArmCrawlerHAT.

HAT GPIO mapping (BCM):
  Left motor:  AIN1=20, AIN2=21, PWMA=12
  Right motor: BIN1=24, BIN2=25, PWMB=13
  STBY: 16
"""

from __future__ import annotations

from dataclasses import dataclass


@dataclass
class _MotorPins:
    in1: int
    in2: int
    pwm: int


_LEFT  = _MotorPins(in1=20, in2=21, pwm=12)
_RIGHT = _MotorPins(in1=24, in2=25, pwm=13)
_STBY  = 16
_PWM_FREQ = 20_000  # 20kHz, above audible range


class CrawlerDriver:
    """Differential drive controller for two TB6612FNG channels."""

    def __init__(self) -> None:
        self._pwm_left = None
        self._pwm_right = None

    def open(self) -> None:
        import RPi.GPIO as GPIO
        GPIO.setmode(GPIO.BCM)
        for pin in (
            _LEFT.in1, _LEFT.in2, _LEFT.pwm,
            _RIGHT.in1, _RIGHT.in2, _RIGHT.pwm,
            _STBY,
        ):
            GPIO.setup(pin, GPIO.OUT, initial=GPIO.LOW)

        self._gpio = GPIO
        self._pwm_left = GPIO.PWM(_LEFT.pwm, _PWM_FREQ)
        self._pwm_right = GPIO.PWM(_RIGHT.pwm, _PWM_FREQ)
        self._pwm_left.start(0)
        self._pwm_right.start(0)
        GPIO.output(_STBY, GPIO.HIGH)

    def close(self) -> None:
        self.stop()
        if self._pwm_left:
            self._pwm_left.stop()
            self._pwm_right.stop()

    def _set_motor(self, pins: _MotorPins, pwm_obj, speed: float) -> None:
        """speed: -1.0 (full reverse) to +1.0 (full forward)."""
        speed = max(-1.0, min(1.0, speed))
        duty = abs(speed) * 100.0
        if speed > 0:
            self._gpio.output(pins.in1, self._gpio.HIGH)
            self._gpio.output(pins.in2, self._gpio.LOW)
        elif speed < 0:
            self._gpio.output(pins.in1, self._gpio.LOW)
            self._gpio.output(pins.in2, self._gpio.HIGH)
        else:
            self._gpio.output(pins.in1, self._gpio.LOW)
            self._gpio.output(pins.in2, self._gpio.LOW)
        pwm_obj.ChangeDutyCycle(duty)

    def drive(self, left: float, right: float) -> None:
        """Set both motors. left/right: -1.0..+1.0."""
        self._set_motor(_LEFT, self._pwm_left, left)
        self._set_motor(_RIGHT, self._pwm_right, right)

    def forward(self, speed: float = 0.6) -> None:
        self.drive(speed, speed)

    def backward(self, speed: float = 0.6) -> None:
        self.drive(-speed, -speed)

    def turn_left(self, speed: float = 0.5) -> None:
        self.drive(-speed, speed)

    def turn_right(self, speed: float = 0.5) -> None:
        self.drive(speed, -speed)

    def stop(self) -> None:
        self.drive(0.0, 0.0)
