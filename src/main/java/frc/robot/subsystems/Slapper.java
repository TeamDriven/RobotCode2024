// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import static frc.robot.Constants.*;

import com.ctre.phoenix6.StatusCode;
import com.ctre.phoenix6.configs.TalonFXConfiguration;

import com.ctre.phoenix6.controls.MotionMagicVoltage;
import com.ctre.phoenix6.controls.NeutralOut;
import com.ctre.phoenix6.controls.VoltageOut;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.GravityTypeValue;
import com.ctre.phoenix6.signals.InvertedValue;
import com.ctre.phoenix6.signals.NeutralModeValue;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class Slapper extends SubsystemBase {
  private TalonFX slapperMotor1 = new TalonFX(20);
  private TalonFX slapperMotor2 = new TalonFX(21);

  MotionMagicVoltage motionMagicControl;
  VoltageOut voltageControl;
  NeutralOut stopMode;
  
  /**
   * Creates a new Intake.
   */
  public Slapper() {
    initSlapperMotors();
  }
  

  /**
   * Initialize the actuation motor
   */
  public void initSlapperMotors() {
    // actuationMotor.setNeutralMode(NeutralModeValue.Brake);

    TalonFXConfiguration configs = new TalonFXConfiguration();

    configs.MotorOutput.NeutralMode = NeutralModeValue.Brake;
    configs.MotorOutput.Inverted = InvertedValue.CounterClockwise_Positive;
    configs.MotorOutput.DutyCycleNeutralDeadband = 0.001;

    configs.MotionMagic.MotionMagicCruiseVelocity = 15;
    configs.MotionMagic.MotionMagicAcceleration = 20;
    configs.MotionMagic.MotionMagicJerk = 50;

    /* Voltage-based velocity requires a feed forward to account for the back-emf of the motor */
    configs.Slot0.GravityType = GravityTypeValue.Arm_Cosine;
    configs.Slot0.kP = 40; // An error of 1 rotation per second results in 2V output
    configs.Slot0.kI = 0.0; // An error of 1 rotation per second increases output by 0.5V every second
    configs.Slot0.kD = 0.0; // A change of 1 rotation per second squared results in 0.01 volts output
    configs.Slot0.kV = 0.12; // Falcon 500 is a 500kV motor, 500rpm per V = 8.333 rps per V, 1/8.33 = 0.12 volts / Rotation per second
    // Peak output of 8 volts
    configs.Voltage.PeakForwardVoltage = 12;
    configs.Voltage.PeakReverseVoltage = -12;

    StatusCode status = StatusCode.StatusCodeNotInitialized;
    for (int i = 0; i < 5; ++i) {
      status = slapperMotor1.getConfigurator().apply(configs);
      if (status.isOK()) break;
    }
    if(!status.isOK()) {
      System.out.println("Could not apply configs, error code: " + status.toString());
    }
    for (int i = 0; i < 5; ++i) {
      status = slapperMotor2.getConfigurator().apply(configs);
      if (status.isOK()) break;
    }
    if(!status.isOK()) {
      System.out.println("Could not apply configs, error code: " + status.toString());
    }

    motionMagicControl = new MotionMagicVoltage(0, 
                                                true, 
                                                1,
                                                0, 
                                                false, 
                                                false, 
                                                false
                                              );

    stopMode = new NeutralOut();

    voltageControl = new VoltageOut(0, 
                                  true, 
                                  false, 
                                  false, 
                                  false);
  }

  public Command runSlapper(double voltage) {
    return new Command() {
      @Override
      public void execute() {
        runSlapperVoltage(voltage);
      }

      @Override
      public void end(boolean interrupted) {
        stopSlapper();
      }
    };
  }

  public void runSlapperVoltage(double voltage) {
    slapperMotor1.setControl(voltageControl.withOutput(voltage));
    slapperMotor2.setControl(voltageControl.withOutput(voltage));
  }

  public Command runSlapperPercent(double speed) {
    return new Command() {
      @Override
      public void execute() {
        slapperMotor1.set(speed);
        slapperMotor2.set(speed);
      }

      @Override
      public void end(boolean interrupted) {
        slapperMotor1.set(0);
        slapperMotor2.set(0);
      }
    };
  }

  public void stopSlapper() {
    slapperMotor1.setControl(stopMode);
    slapperMotor2.setControl(stopMode);
  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
    // System.out.println(getLimitSwitch());
    // System.out.println(actuationMotor.getPosition());
    // System.out.println(actuationMotor.getMotorVoltage());
  }

  @Override
  public void simulationPeriodic() {
    // This method will be called once per scheduler run during simulation
  }
}
