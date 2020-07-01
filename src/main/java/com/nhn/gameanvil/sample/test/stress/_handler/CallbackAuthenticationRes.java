package com.nhn.gameanvil.sample.test.stress._handler;

import static org.junit.Assert.assertTrue;

import com.nhn.gameanvil.sample.test.common.GameConstants;
import com.nhn.gameanvil.sample.protocol.Authentication;
import com.nhn.gameanvil.sample.protocol.Authentication.LoginType;
import com.nhn.gameanvilcore.connector.callback.parent.IDispatchPacket;
import com.nhn.gameanvilcore.connector.protocol.Packet;
import com.nhn.gameanvilcore.connector.protocol.result.AuthenticationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.nhn.gameanvil.sample.test.stress.SampleUserClass;

public class CallbackAuthenticationRes implements IDispatchPacket<SampleUserClass> {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void dispatch(Packet packet, SampleUserClass user) {
        //응답확인
        AuthenticationResult result = user.parseAuthenticationResult(packet);
        assertTrue(result.isSuccess());

        Authentication.LoginReq.Builder loginReq = Authentication.LoginReq.newBuilder();
        loginReq.setUuid(user.getDeviceId());
        loginReq.setLoginType(LoginType.LOGIN_GUEST);
        loginReq.setAppVersion("0.0.1");
        loginReq.setAppStore("NONE");
        loginReq.setDeviceModel("PC");
        loginReq.setDeviceCountry("KR");
        loginReq.setDeviceLanguage("ko");
        user.login(GameConstants.GAME_USER_TYPE, "", loginReq);
    }

}
