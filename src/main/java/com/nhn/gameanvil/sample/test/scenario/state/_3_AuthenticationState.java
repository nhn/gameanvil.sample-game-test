package com.nhn.gameanvil.sample.test.scenario.state;

import static org.slf4j.LoggerFactory.getLogger;

import com.nhn.gameanvil.gamehammer.scenario.State;
import com.nhn.gameanvil.gamehammer.tool.UuidGenerator;
import com.nhn.gameanvil.sample.protocol.Authentication;
import com.nhn.gameanvil.sample.test.scenario.TapTapActor;
import org.slf4j.Logger;

// 인증 처리 상태
public class _3_AuthenticationState extends State<TapTapActor> {
    private static final Logger logger = getLogger(_3_AuthenticationState.class);
    protected static UuidGenerator connectionDeviceIdGenerator = new UuidGenerator("DeviceId");

    @Override
    protected void onEnter(TapTapActor actor) {
        logger.debug("TapTapActor idx[{}] - onEnter : {}", actor.getIndex(), getStateName());

        // 인증 메세지 생성
        Authentication.AuthenticationReq.Builder authenticationReq = Authentication.AuthenticationReq.newBuilder().setAccessToken("TapTap_AccessToken!!!!");

        // 인증 요청
        actor.getConnection().authentication(authenticationResult -> {
            if (authenticationResult.isSuccess()) {
                actor.changeState(_4_LoginState.class);
            } else {
                logger.warn("[{}] Fail - uuid : {}, AccountId : {} \t{}, {}",
                    getStateName(),
                    actor.getConnection().getUuid(),
                    actor.getConnection().getUuid(),
                    authenticationResult.getErrorCode(),
                    authenticationResult.getResultCode()
                );
                actor.finish(false);
            }
        }, String.valueOf(actor.getConnection().getUuid()), String.valueOf(actor.getConnection().getUuid()), connectionDeviceIdGenerator.generateUniqueId(), authenticationReq);
    }

    @Override
    protected void onExit(TapTapActor actor) {
        logger.debug("TapTapActor idx[{}] - onExit : {}", actor.getIndex(), getStateName());
    }
}
