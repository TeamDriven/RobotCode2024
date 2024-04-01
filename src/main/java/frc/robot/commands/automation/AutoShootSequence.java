package frc.robot.commands.automation;

import static frc.robot.Subsystems.*;

import java.util.function.DoubleSupplier;

import static frc.robot.Constants.ShooterConstants.*;
import static frc.robot.Constants.ActuationConstants.*;
import static frc.robot.Constants.IndexerConstants.*;
import static frc.robot.Constants.IntakeConstants.*;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.WaitCommand;

/**
 * Represents an automated shooting sequence for a robot.
 * This class extends the SequentialCommandGroup class.
 * It contains a series of commands that are executed sequentially to perform the shooting sequence in auto.
 *
 * @param angle A DoubleSupplier that provides the angle for the shooter.
 * @param velocity A DoubleSupplier that provides the velocity for the shooter.
 * @param restingAngle The resting angle for the shooter after the shooting sequence is completed.
 */
public class AutoShootSequence extends SequentialCommandGroup {
    
    public AutoShootSequence(DoubleSupplier angle, DoubleSupplier velocity, double restingAngle) {
        addCommands(
            angleController.setPositionCommandSupplier(angle),
            shooter.speedUpShooter(velocity, shooterSequenceAcceleration),
            angleController.waitUntilAtPositionSupplier(angle),
            shooter.checkIfAtSpeedSupplier(() -> velocity.getAsDouble() * 0.75),
            indexer.speedUpIndexer(indexerVelocity, indexerAcceleration),
            shooter.checkIfAtSpeedSupplier(velocity),
            indexer.checkIfAtSpeedSupplier(() -> indexerVelocity),
            actuation.waitUntilAtPosition(actuationTuckPosition),
            intake.startFeedingCommand(feedVelocity, feedAcceleration),
            new WaitCommand(0.5).raceWith(shooter.waitUntilRingLeft()),
            new StopShoot(restingAngle)
        );
    }

    public AutoShootSequence(DoubleSupplier angle, DoubleSupplier velocity, double restingAngle, double indexerVelocity) {
        addCommands(
            angleController.setPositionCommandSupplier(angle),
            shooter.speedUpShooterSlow(velocity, shooterSequenceAcceleration),
            angleController.waitUntilAtPositionSupplier(angle),
            shooter.checkIfAtSpeedSupplier(() -> velocity.getAsDouble() * 0.75),
            indexer.speedUpIndexer(indexerVelocity, indexerAcceleration),
            shooter.checkIfAtSpeedSupplier(velocity),
            indexer.checkIfAtSpeedSupplier(() -> indexerVelocity),
            actuation.waitUntilAtPosition(actuationTuckPosition),
            intake.startFeedingCommand(feedVelocity, feedAcceleration),
            new WaitCommand(1.0).raceWith(shooter.waitUntilRingLeft()),
            // shooter.waitUntilRingLeft(),
            new StopShoot(restingAngle)
        );
    }
}
