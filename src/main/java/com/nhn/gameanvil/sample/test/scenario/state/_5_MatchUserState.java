package com.nhn.gameanvil.sample.test.scenario.state;

import static org.slf4j.LoggerFactory.getLogger;

import com.nhn.gameanvil.gamehammer.scenario.State;
import com.nhn.gameanvil.sample.protocol.GameSingle;
import com.nhn.gameanvil.sample.protocol.GameSingle.DifficultyType;
import com.nhn.gameanvil.sample.test.common.GameConstants;
import com.nhn.gameanvil.sample.test.scenario.TapTapActor;
import org.slf4j.Logger;

// 방생성 요청 상태
public class _5_MatchUserState extends State<TapTapActor> {
    private static final Logger logger = getLogger(_5_MatchUserState.class);

    @Override
    protected void onEnter(TapTapActor actor) {
        logger.debug("TapTapActor idx[{}] - onEnter : {}", actor.getIndex(), getStateName());

        actor.getUser().addListenerMatchUserTimeoutNoti(resultMatchUserTimeout -> {
            if (resultMatchUserTimeout.isSuccess()) {
                actor.getUser().removeAllListenerMatchUserTimeoutNoti();
                actor.getUser().removeAllListenerMatchUserDoneNoti();
                actor.changeState(_7_LeaveRoomState.class);
            } else {
                actor.changeState(_8_LogoutState.class);
                System.out.println("[ERROR - addListenerMatchUserTimeout]");
            }
        });

        actor.getUser().addListenerMatchUserDoneNoti(resultMatchUserDone -> {
            if (resultMatchUserDone.isSuccess()) {
                actor.getUser().removeAllListenerMatchUserTimeoutNoti();
                actor.getUser().removeAllListenerMatchUserDoneNoti();
                actor.changeState(_7_LeaveRoomState.class);
            } else {
                actor.changeState(_8_LogoutState.class);
                System.out.println("[ERROR - addListenerMatchUserDone]");
            }
        });

        actor.getUser().matchUserStart(resultMatchUserStart -> {
            if (!resultMatchUserStart.isSuccess()) {
                logger.info(
                    "[{}] Fail - uuid : {}, AccountId : {}\t{}, {}",
                    getStateName(),
                    actor.getConnection().getUuid(),
                    actor.getConnection().getAccountId(),
                    resultMatchUserStart.getErrorCode(),
                    resultMatchUserStart.getPacketResultCode()
                );
                actor.changeState(_8_LogoutState.class);
            }
        }, GameConstants.GAME_ROOM_TYPE_MULTI_USER_MATCH);
    }

    @Override
    protected void onExit(TapTapActor actor) {
        logger.debug("TapTapActor idx[{}] - onExit : {}", actor.getIndex(), getStateName());
    }
}

