package org.firstinspires.ftc.teamcode.IronNestBEE;
import static com.pedropathing.ivy.Scheduler.schedule;
import static com.pedropathing.ivy.groups.Groups.parallel;
import static com.pedropathing.ivy.groups.Groups.race;
import static com.pedropathing.ivy.groups.Groups.sequential;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.utils.Timer;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
@Autonomous(name="wait4Leave", group = "Autonomous")
public class wait4Leave extends OpMode {
    private DriveTrain bee;
    private Launcher Ln;
    private Tollbooth TbL;
    private Tollbooth TbR;
    private Intake In;
    private Hood doomAndDespair;

    @Override
    public void init() {
        Scheduler.reset();
        // 2. Initialize them here using hardwareMap
        bee = new DriveTrain(hardwareMap);
        doomAndDespair = new Hood(hardwareMap);
        Ln = new Launcher(hardwareMap,telemetry);
        TbL = new Tollbooth(hardwareMap,true);
        TbR = new Tollbooth(hardwareMap,false);
        In = new Intake(hardwareMap);
        doomAndDespair.stowCommand();
    }

    public Command waitS(double seconds) {
        Timer timer = new Timer();
        return Command.build()
                .setStart(timer::reset)
                .setDone(() -> timer.seconds() >= seconds);
    }
    @Override
    public void start(){
        final Command autonomous = sequential(
                sequential(
                        //initial wait for partner to tip
                        waitS(19),
                race(
                        sequential(
                                waitS(1.5),
                                parallel(
                                        TbL.autoHold(5,true),
                                        TbR.autoHold(5,false)
                                )
                        ),
                        Ln.autoLaunch(),
                        doomAndDespair.retractCommand(),
                        sequential(
                                waitS(2),
                                In.autoTake(3.5),
                                waitS(1)
                        )
                ))
                        .then( bee.autoDrive(-0.4,0,0,0.6
                        ))
                        .then(bee.autoDrive(0,-0.4,0, 2.5
                        ))
                        .then(bee.Stop())
        );
        schedule(autonomous);
    }

    @Override
    public void loop(){
        Scheduler.execute();
    }
}
