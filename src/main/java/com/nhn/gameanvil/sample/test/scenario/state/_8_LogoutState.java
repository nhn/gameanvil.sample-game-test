package com.nhn.gameanvil.sample.test.scenario.state;

import static org.slf4j.LoggerFactory.getLogger;

import com.nhn.gameanvil.gamehammer.scenario.State;
import com.nhn.gameanvil.sample.test.scenario.TapTapActor;
import org.slf4j.Logger;

// 로그아웃 요청 상태
public class _8_LogoutState extends State<TapTapActor> {
    private static final Logger logger = getLogger(_8_LogoutState.class);

    @Override
    protected void onEnter(TapTapActor actor) {
        logger.debug("TapTapActor idx[{}] - onEnter : {}", actor.getIndex(), getStateName());
        actor.getUser().logout(result -> {
            if (result.isSuccess()) {
                actor.finish(true);
            } else {
                logger.info(
                    "[{}] Fail - uuid : {}, AccountId : {}\t{}, {}",
                    getStateName(),
                    actor.getConnection().getUuid(),
                    actor.getConnection().getAccountId(),
                    result.getErrorCode(),
                    result.getResultCode()
                );
                actor.changeState(_8_LogoutState.class);
            }
        });
    }

    @Override
    protected void onExit(TapTapActor actor) {
        logger.debug("TapTapActor idx[{}] - onExit : {}", actor.getIndex(), getStateName());
    }

}

