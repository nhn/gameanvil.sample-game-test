package com.nhn.gameanvil.sample.test.scenario;

import static org.slf4j.LoggerFactory.getLogger;

import com.nhn.gameanvil.gamehammer.scenario.ScenarioMachine;
import com.nhn.gameanvil.gamehammer.scenario.ScenarioTest;
import com.nhn.gameanvil.gamehammer.tester.Tester;
import com.nhn.gameanvil.gamehammer.tester.TimeoutStatistics;
import com.nhn.gameanvil.sample.protocol.Authentication;
import com.nhn.gameanvil.sample.protocol.GameMulti;
import com.nhn.gameanvil.sample.protocol.GameSingle;
import com.nhn.gameanvil.sample.protocol.Result;
import com.nhn.gameanvil.sample.test.scenario.state._1_ConnectState;
import com.nhn.gameanvil.sample.test.scenario.state._2_RampUpState;
import com.nhn.gameanvil.sample.test.scenario.state._3_AuthenticationState;
import com.nhn.gameanvil.sample.test.scenario.state._4_LoginState;
import com.nhn.gameanvil.sample.test.scenario.state._5_CreateRoomState;
import com.nhn.gameanvil.sample.test.scenario.state._6_PlayGameState;
import com.nhn.gameanvil.sample.test.scenario.state._7_LeaveRoomState;
import com.nhn.gameanvil.sample.test.scenario.state._8_LogoutState;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;

public class TestScenario {
    private static final Logger logger = getLogger(TestScenario.class);
    static ScenarioTest<TapTapActor> scenarioTest;
    static Tester.Builder testerBuilder;

    @BeforeClass
    public static void beforeClass() {
        initConfig();
        initScenario();
    }

    @Test
    public void scenarioTest() {
        // 테스터 생성
        Tester tester = testerBuilder.Build();

        // 테스트 시작
        logger.info("Test Start!!!");
        scenarioTest.start(tester,
            TapTapActor.class,
            _1_ConnectState.class,
            false
        );

        // 테스트 결과 출력
        logger.info(scenarioTest.printStatistics("Finished"));
        logger.info(TimeoutStatistics.getInstance().printClientTimeout());
    }

    private static void initConfig() {
        // 테스터 기본 프로토콜 설정
        testerBuilder = Tester.newBuilderWithConfig();
        testerBuilder.addProtoBufClass(0, Authentication.getDescriptor())
            .addProtoBufClass(1, GameMulti.getDescriptor())
            .addProtoBufClass(2, GameSingle.getDescriptor())
            .addProtoBufClass(3, Result.getDescriptor())
            .addProtoBufClass(4, com.nhn.gameanvil.sample.protocol.User.getDescriptor());
    }

    private static void initScenario() {
        // scenario 생성
        ScenarioMachine<TapTapActor> scenario = getScenarioMachineType();
        scenarioTest = new ScenarioTest<>(scenario);
    }

    private static ScenarioMachine<TapTapActor> getScenarioMachineType() {
        // 시나이로 머신에 상태 등록
        ScenarioMachine<TapTapActor> scenario = new ScenarioMachine<>("TapTap");

        scenario.addState(new _1_ConnectState());
        scenario.addState(new _2_RampUpState());
        scenario.addState(new _3_AuthenticationState());
        scenario.addState(new _4_LoginState());
        scenario.addState(new _5_CreateRoomState());
        scenario.addState(new _6_PlayGameState());
        scenario.addState(new _7_LeaveRoomState());
        scenario.addState(new _8_LogoutState());

        return scenario;
    }


}
