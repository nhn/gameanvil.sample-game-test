package com.nhn.gameanvil.sample.test.scenario.state;

import static org.slf4j.LoggerFactory.getLogger;

import com.nhn.gameanvil.gamehammer.config.TesterConfigLoader;
import com.nhn.gameanvil.gamehammer.scenario.State;
import com.nhn.gameanvil.sample.protocol.Authentication;
import com.nhn.gameanvil.sample.protocol.Authentication.LoginType;
import com.nhn.gameanvil.sample.test.common.GameConstants;
import com.nhn.gameanvil.sample.test.scenario.TapTapActor;
import org.slf4j.Logger;

public class _4_LoginState extends State<TapTapActor> {
    private static final Logger logger = getLogger(_4_LoginState.class);

    @Override
    protected void onEnter(TapTapActor actor) {
        logger.debug("TapTapActor idx[{}] - onEnter : {}", actor.getIndex(), getStateName());

        // 로그인 데이터
        Authentication.LoginReq.Builder loginReq = Authentication.LoginReq.newBuilder();
        loginReq.setUuid(String.valueOf(actor.getConnection().getUuid()));
        loginReq.setLoginType(LoginType.LOGIN_GUEST);
        loginReq.setAppVersion("0.0.1");
        loginReq.setAppStore("NONE");
        loginReq.setDeviceModel("PC");
        loginReq.setDeviceCountry("KR");
        loginReq.setDeviceLanguage("ko");

        actor.getUser().login(loginRes -> {
            if (loginRes.isSuccess()) {
                actor.changeState(_5_CreateRoomState.class);
//                actor.changeState(_5_MatchUserState.class);
            } else {
                logger.info(
                    "[{}] Fail - uuid : {}, AccountId : {}\t{}, {}",
                    getStateName(),
                    actor.getConnection().getUuid(),
                    actor.getConnection().getAccountId(),
                    loginRes.getErrorCode(),
                    loginRes.getResultCode()
                );
                actor.finish(false);
            }
        }, GameConstants.GAME_USER_TYPE, TesterConfigLoader.getInstance().getTesterConfig().getServiceInfo(GameConstants.GAME_NAME).getNextChannelId(), loginReq);
    }

    @Override
    protected void onExit(TapTapActor actor) {
        logger.debug("TapTapActor idx[{}] - onExit : {}", actor.getIndex(), getStateName());
    }

}
