// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import static frc.robot.Constants.*;
import static frc.robot.Subsystems.*;

import com.ctre.phoenix6.mechanisms.swerve.SwerveModule.DriveRequestType;
import com.ctre.phoenix6.mechanisms.swerve.SwerveRequest;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.button.Trigger;

/**
 * This class is where the bulk of the robot should be declared. Since Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in the {@link Robot}
 * periodic methods (other than the scheduler calls). Instead, the structure of the robot (including
 * subsystems, commands, and trigger mappings) should be declared here.
 */
public class RobotContainer {
  
  private final CommandXboxController joystick = new CommandXboxController(0); // My joystick

  private final SwerveRequest.FieldCentric drive = new SwerveRequest.FieldCentric() // I want field-centric
      .withDeadband(MaxSpeed * 0.1).withRotationalDeadband(MaxAngularRate * 0.1) // Add a 10% deadband
      .withDriveRequestType(DriveRequestType.OpenLoopVoltage); // driving in open loop

  // Lock wheels
  private final SwerveRequest.SwerveDriveBrake brake = new SwerveRequest.SwerveDriveBrake();

  public boolean intakePosition = false;
  public boolean tuckPosition = true;

  /** The container for the robot. Contains subsystems, OI devices, and commands. */
  public RobotContainer() {
    // Configure the trigger bindings
    configureBindings();
  }

  /**
   * Use this method to define your trigger->command mappings. Triggers can be created via the
   * {@link Trigger#Trigger(java.util.function.BooleanSupplier)} constructor with an arbitrary
   * predicate, or via the named factories in {@link
   * edu.wpi.first.wpilibj2.command.button.CommandGenericHID}'s subclasses for {@link
   * CommandXboxController Xbox}/{@link edu.wpi.first.wpilibj2.command.button.CommandPS4Controller
   * PS4} controllers or {@link edu.wpi.first.wpilibj2.command.button.CommandJoystick Flight
   * joysticks}.
   */
  private void configureBindings() {
    drivetrain.setDefaultCommand( // Drivetrain will execute this command periodically
        drivetrain.applyRequest(() -> drive.withVelocityX(-joystick.getLeftY() * MaxSpeed) // Drive forward with
                                                                                           // negative Y (forward)
            .withVelocityY(-joystick.getLeftX() * MaxSpeed) // Drive left with negative X (left)
            .withRotationalRate(-joystick.getRightX() * MaxAngularRate) // Drive counterclockwise with negative X (left)
        ));

    // joystick.a().whileTrue(drivetrain.applyRequest(() -> brake)); // Lock wheels on A press

    // reset the field-centric heading on left bumper press
    // joystick.leftBumper().onTrue(drivetrain.runOnce(() -> drivetrain.seedFieldRelative()));

    joystick.x().whileTrue(new ParallelCommandGroup(
      actuation.setPositionCommand(95 * actuationTicksPerDegree),
      new InstantCommand(this::setIntakePosition)
    ));
    joystick.b().whileTrue(new ParallelCommandGroup(
      actuation.setPositionCommand(-60 * actuationTicksPerDegree),
      new InstantCommand(this::setTuckPosition)
    ));
    joystick.povDown().whileTrue(
      slapper.runSlapper(2)
    );
    joystick.povUp().whileTrue(
      slapper.runSlapper(-2)
    );

    joystick.rightBumper().and(this::isIntakePosition).whileTrue(intake.runIntakeCommand(15, 40));
    joystick.leftBumper().whileTrue(intake.runIntakeCommand(-15, 40));
    joystick.a().and(this::isTuckPosition).whileTrue(intake.feedCommand(60, 100));

    new Trigger(this::isPieceIn).and(this::isIntakePosition).onTrue(new SequentialCommandGroup(
      new InstantCommand(intake::stopIntakeMotor, intake),
      actuation.setPositionCommand(-55 * actuationTicksPerDegree),
      new InstantCommand(this::setTuckPosition)
    ));

    new Trigger(actuation::getLimitSwitch).onTrue(actuation.resetEncoderCommand());

    joystick.rightTrigger(0.1).whileTrue(shooter.runShooterCommand(80, 100));
    joystick.leftTrigger(0.1).whileTrue(shooter.runShooterCommand(-80, 100));
    // joystick.rightTrigger(0.1).whileTrue(shooter.runShooterPercent(0.4));
    // joystick.leftTrigger(0.1).whileTrue(shooter.runShooterPercent(0.4));
  }

  public boolean isPieceIn() {
    return intake.pdp.getCurrent(16) >= 50;
  }

  public void setIntakePosition() {
    intakePosition = true;
    tuckPosition = false;
  }

  public void setTuckPosition() {
    tuckPosition = true;
    intakePosition = false;
  }

  public boolean isIntakePosition() {
    return intakePosition;
  }

  public boolean isTuckPosition() {
    return tuckPosition;
  }

  /**
   * Use this to pass the autonomous command to the main {@link Robot} class.
   *
   * @return the command to run in autonomous
   */
  public Command getAutonomousCommand() {
    // An example command will be run in autonomous
    return new SequentialCommandGroup();
  }
}
