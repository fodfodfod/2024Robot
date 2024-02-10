// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import frc.robot.Constants.OperatorConstants;
import frc.robot.Constants.SwerveConstants;
import frc.robot.commands.Autos;
import frc.robot.subsystems.LED;
import frc.robot.shuffleboard.DriverStationTab;
import frc.robot.shuffleboard.MotorTab;
import frc.robot.shuffleboard.LEDTab;
import frc.robot.shuffleboard.ShuffleboardInfo;
import frc.robot.shuffleboard.ShuffleboardTabBase;
import frc.robot.shuffleboard.SwerveTab;
import frc.robot.util.TunableNumber;
import swervelib.SwerveDrive;

import java.util.ArrayList;

import com.pathplanner.lib.commands.PathPlannerAuto;

import frc.robot.commands.drivetrain.AbsoluteDriveDirectAngle;
import frc.robot.commands.drivetrain.LockWheelsState;
import frc.robot.commands.drivetrain.AbsoluteDriveAngularRotation;
import frc.robot.subsystems.Drivetrain;
import frc.robot.subsystems.Vision;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.GenericHID.RumbleType;
import edu.wpi.first.wpilibj.SerialPort;
import frc.robot.subsystems.Intake;
import frc.robot.subsystems.Shooter;
import com.pathplanner.lib.auto.NamedCommands;
import com.pathplanner.lib.commands.PathPlannerAuto;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.ScheduleCommand;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine;

/**
 * This class is where the bulk of the robot should be declared. Since
 * Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in
 * the {@link Robot}
 * periodic methods (other than the scheduler calls). Instead, the structure of
 * the robot (including
 * subsystems, commands, and trigger mappings) should be declared here.
 */
public class RobotContainer {
	// The robot's subsystems and commands are defined here...
	private final ShuffleboardInfo shuffleboard;
	LED led = new LED(SerialPort.Port.kMXP);

	// Replace with CommandPS4Controller or CommandJoystick if needed
	private final CommandXboxController driverController = new CommandXboxController(
			OperatorConstants.DRIVER_CONTROLLER_PORT);
	private double speedMultiplier = SwerveConstants.REGULAR_SPEED;
	private final Vision vision = new Vision();
	private final Drivetrain drivetrain = new Drivetrain(vision);

	private final AbsoluteDriveDirectAngle absoluteDrive = (new AbsoluteDriveDirectAngle(drivetrain,
			() -> (-MathUtil.applyDeadband(driverController.getLeftY() * speedMultiplier,
					OperatorConstants.JOYSTICK_DEADBAND)),
			() -> (-MathUtil.applyDeadband(driverController.getLeftX() * speedMultiplier,
					OperatorConstants.JOYSTICK_DEADBAND)),
			() -> (-MathUtil.applyDeadband(driverController.getRightX() * speedMultiplier,
					OperatorConstants.JOYSTICK_DEADBAND)),
			() -> (-MathUtil.applyDeadband(driverController.getRightY() * speedMultiplier,
					OperatorConstants.JOYSTICK_DEADBAND))));

	private final AbsoluteDriveAngularRotation rotationDrive = (new AbsoluteDriveAngularRotation(drivetrain,
			() -> (-MathUtil.applyDeadband(driverController.getLeftY() * speedMultiplier,
					OperatorConstants.JOYSTICK_DEADBAND)),
			() -> (-MathUtil.applyDeadband(driverController.getLeftX() * speedMultiplier,
					OperatorConstants.JOYSTICK_DEADBAND)),
			() -> (-MathUtil.applyDeadband(driverController.getRightX() * speedMultiplier,
					OperatorConstants.JOYSTICK_DEADBAND))));

	/**
	 * The container for the robot. Contains subsystems, OI devices, and commands.
	 */
	public RobotContainer() {
		// PathPlanner stuff
		// Subsystem initialization

		// Register Named Commands

		// Configure the trigger bindings
		// end of path planner stuff
		configureBindings();

		new TunableNumber();
		shuffleboard = ShuffleboardInfo.getInstance();
		ArrayList<ShuffleboardTabBase> tabs = new ArrayList<>();
		// YOUR CODE HERE | | |
		// \/ \/ \/
		tabs.add(new DriverStationTab());

		tabs.add(MotorTab.getInstance());

		tabs.add(new LEDTab(led));
		tabs.add(new SwerveTab(drivetrain));
		// STOP HERE
		shuffleboard.addTabs(tabs);
	}

	/**
	 * Use this method to define your trigger->command mappings. Triggers can be
	 * created via the
	 * {@link Trigger#Trigger(java.util.function.BooleanSupplier)} constructor with
	 * an arbitrary
	 * predicate, or via the named factories in {@link
	 * edu.wpi.first.wpilibj2.command.button.CommandGenericHID}'s subclasses for
	 * {@link
	 * CommandXboxController
	 * Xbox}/{@link edu.wpi.first.wpilibj2.command.button.CommandPS4Controller
	 * PS4} controllers or
	 * {@link edu.wpi.first.wpilibj2.command.button.CommandJoystick Flight
	 * joysticks}.
	 */
	private void configureBindings() {
		drivetrain.setDefaultCommand(absoluteDrive);

		driverController.povDown().toggleOnTrue(new LockWheelsState(drivetrain));
		driverController.leftBumper().onTrue(new ScheduleCommand(rotationDrive))
				.onFalse(Commands.runOnce(() -> rotationDrive.cancel()));
		driverController.rightBumper()
				.onTrue(Commands.runOnce(
						() -> speedMultiplier = SwerveConstants.SLOW_SPEED))
				.onFalse(Commands.runOnce(
						() -> speedMultiplier = SwerveConstants.REGULAR_SPEED));
		driverController.rightTrigger()
				.onTrue(Commands.runOnce(
						() -> speedMultiplier = SwerveConstants.FAST_SPEED))
				.onFalse(Commands.runOnce(
						() -> speedMultiplier = SwerveConstants.REGULAR_SPEED));

		driverController.povUp().onTrue(new InstantCommand(() -> drivetrain.setWheelsForward()));
		driverController.y().whileTrue(drivetrain.sysIdDynamic(SysIdRoutine.Direction.kReverse));
		driverController.x().whileTrue(drivetrain.sysIdDynamic(SysIdRoutine.Direction.kForward));
		driverController.b().whileTrue(drivetrain.sysIdQuasistatic(SysIdRoutine.Direction.kReverse));
		driverController.a().whileTrue(drivetrain.sysIdQuasistatic(SysIdRoutine.Direction.kForward));
	}

	/**
	 * Use this to pass the autonomous command to the main {@link Robot} class.
	 *
	 * @return the command to run in autonomous
	 */
	public Command getAutonomousCommand() {
		// An example command will be run in autonomous
		return new PathPlannerAuto("test auto forwardback");
	}

	public void setMotorBrake(boolean isBraked) {
		drivetrain.setMotorBrake(isBraked);
	}
}
