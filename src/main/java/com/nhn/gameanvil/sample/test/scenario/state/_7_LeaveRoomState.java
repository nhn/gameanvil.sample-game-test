package com.nhn.gameanvil.sample.test.scenario.state;

import static org.slf4j.LoggerFactory.getLogger;

import com.nhn.gameanvil.gamehammer.scenario.State;
import com.nhn.gameanvil.sample.protocol.GameSingle;
import com.nhn.gameanvil.sample.protocol.GameSingle.EndType;
import com.nhn.gameanvil.sample.test.scenario.TapTapActor;
import org.slf4j.Logger;

// 방나가는 요청 상태
public class _7_LeaveRoomState extends State<TapTapActor> {
    private static final Logger logger = getLogger(_7_LeaveRoomState.class);

    @Override
    protected void onEnter(TapTapActor actor) {
        logger.debug("TapTapActor idx[{}] - onEnter : {}", actor.getIndex(), getStateName());

        // 게임 종료 패킷
        GameSingle.EndGameReq.Builder endGameReq = GameSingle.EndGameReq.newBuilder();
        endGameReq.setEndType(EndType.GAME_END_TIME_UP);
        // 게임 종료
        actor.getUser().leaveRoom((leaveRoomResult) -> {
            if (leaveRoomResult.isSuccess()) {
                actor.changeState(_8_LogoutState.class);
            } else {
                logger.warn(
                    "[{}] Fail - uuid : {}, AccountId : {}\t{}, {}",
                    getStateName(),
                    actor.getConnection().getUuid(),
                    actor.getConnection().getAccountId(),
                    leaveRoomResult.getErrorCode(),
                    leaveRoomResult.getPacketResultCode()
                );
                actor.changeState(_8_LogoutState.class);
            }
        }, endGameReq);


    }

    @Override
    protected void onExit(TapTapActor actor) {
        logger.debug("TapTapActor idx[{}] - onExit : {}", actor.getIndex(), getStateName());
    }

}
