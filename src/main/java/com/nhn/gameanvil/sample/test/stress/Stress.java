package com.nhn.gameanvil.sample.test.stress;

import com.nhn.gameanvil.sample.test.stress._handler.CallbackAuthenticationRes;
import com.nhn.gameanvil.sample.test.stress._handler.CallbackCreateRoomRes;
import com.nhn.gameanvil.sample.test.stress._handler.CallbackLeaveRoomRes;
import com.nhn.gameanvil.sample.test.stress._handler.CallbackLoginRes;
import com.nhn.gameanvil.sample.test.stress._handler.CallbackLogout;
import com.nhn.gameanvil.sample.test.stress._handler.CallbackShuffleDeckRes;
import com.nhn.gameanvil.sample.test.stress._handler.SampleTimeout;
import com.nhn.gameanvil.sample.protocol.Authentication;
import com.nhn.gameanvil.sample.protocol.User;
import com.nhn.gameanvil.sample.test.common.GameConstants;
import com.nhn.gameanvil.sample.test.common.Initializer;
import com.nhn.gameanvilcore.connector.common.Config;
import com.nhn.gameanvilcore.connector.tcp.ConnectorSession;
import com.nhn.gameanvilcore.connector.tcp.GameAnvilConnector;
import com.nhn.gameanvilcore.connector.tcp.agent.parent.IAsyncConnectorUser;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Stress {
    private static GameAnvilConnector connector;
    private Logger logger = LoggerFactory.getLogger(getClass());

    //-------------------------------------------------------------------------------------

    @BeforeClass
    public static void configuration() {

        // 테스트 하려는 서버의 IP 와 Port 를 지정합니다.
//        Config.addRemoteInfo("10.77.35.47", 11200);
        Config.addRemoteInfo("127.0.0.1", 11200);

        // 패킷 수신에 대한 타임아웃 시간을 지정합니다. (밀리초)
        Config.WAIT_RECV_TIMEOUT_MSEC = 10000; // [default 3000]

        // 커넥터의 run 매서드에 대한 강제종료 시간을 설정합니다. (초)
        Config.FORCE_EXIT_TIMEOUT_SEC = 60; // [default 300]

        // Ping 주기를 설정합니다. (밀리초)
        Config.PING_INTERVAL_MSEC = 3000; // [default 3000]

        Config.CONCURRENT_USER = 100;

        // 부하 테스트 시작시, Bot 유저들에 딜레이를 두고 런칭 시킬 수 있습니다.
        Config.RAMP_UP_DELAY_MSEC = 1; // [default 0]

        // 커넥터를 생성합니다.
        connector = Initializer.initConnector();

        // 콜백 목록을 등록합니다.
        connector.addPacketCallbackAuthentication(new CallbackAuthenticationRes());
        connector.addPacketCallbackLogin(new CallbackLoginRes());
        connector.addPacketCallback(User.ShuffleDeckRes.class, new CallbackShuffleDeckRes(), 10, TimeUnit.MILLISECONDS); // 해당 콜백을 딜레이 시켜서 호출하고자 할 경우 파라미터로 옵션값을 지정할 수 있습니다.
        connector.addPacketCallbackCreateRoom(new CallbackCreateRoomRes());
        connector.addPacketCallbackLeaveRoom(new CallbackLeaveRoomRes(), 10); // 해당 콜백을 딜레이 시켜서 호출하고자 할 경우 파라미터로 옵션값을 지정할 수 있습니다.
        connector.addPacketCallbackLogout(new CallbackLogout());

        connector.addPacketTimeoutCallback(new SampleTimeout());
    }

    //-------------------------------------------------------------------------------------

    @Test
    public void runMultiUser() throws TimeoutException {

        for (int i = 0; i < Config.CONCURRENT_USER; ++i) {

            // 커넥션을 생성하고 세션 정보가 담긴 객체를 리턴 받습니다.

            ConnectorSession session = connector.addSession(connector.getHostIncrementedValue("account"), connector.makeUniqueId());

            // 여기서는 커스텀 클래스를 지정하여, 등록한 콜백에서 쉽게 활용할 수 있도록 합니다.
            SampleUserClass sampleUser = session.addUser(GameConstants.GAME_NAME, SampleUserClass.class);
        }

        // 횟수로 반복
        //connector.repeatByEntire(/* ... */);
        connector.repeatByIndividual(new GameAnvilConnector.InitialProtocol() {
            @Override
            public void send(IAsyncConnectorUser iUser) {
                Authentication.AuthenticationReq.Builder authenticationReq = Authentication.AuthenticationReq.newBuilder().setAccessToken("TapTap_AccessToken!!!!");
                iUser.authentication(iUser.getAccountId(), authenticationReq);
            }
        }, 10);

//        // FORCE_EXIT_TIMEOUT_SEC 시간만큼 반복
//        connector.repeatByIndividual(new GameAnvilConnector.InitialProtocol() {
//            @Override
//            public void send(IAsyncConnectorUser iUser) {
//                Authentication.AuthenticationReq.Builder authenticationReq = Authentication.AuthenticationReq.newBuilder().setAccessToken("TapTap_AccessToken!!!!");
//                iUser.authentication(iUser.getAccountId(), authenticationReq);
//            }
//        });
    }

}
