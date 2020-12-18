package com.nhn.gameanvil.sample.test.scenario.state;

import static org.slf4j.LoggerFactory.getLogger;

import com.nhn.gameanvil.gamehammer.scenario.State;
import com.nhn.gameanvil.sample.protocol.GameSingle.TapMsg;
import com.nhn.gameanvil.sample.test.scenario.TapTapActor;
import org.slf4j.Logger;

// 방생성 요청 상태
public class _6_PlayGameState extends State<TapTapActor> {
    private static final Logger logger = getLogger(_6_PlayGameState.class);

    @Override
    protected void onEnter(TapTapActor actor) {
        logger.debug("TapTapActor idx[{}] - onEnter : {}", actor.getIndex(), getStateName());

        // 5번 전송
        if (actor.getPlayGameCount() >= 4) {
            actor.resetPlayGameCount();
            actor.changeState(_7_LeaveRoomState.class);
        } else {
            // 게임 플레이 탭한 정보 전달
            TapMsg.Builder tapMsg = TapMsg.newBuilder();
            tapMsg.setSelectCardName("sushi_0" + actor.getPlayGameCount());
            tapMsg.setCombo(actor.getPlayGameCount());
            tapMsg.setTapScore(100 * actor.getPlayGameCount());
            actor.getUser().send(tapMsg.build());
            actor.increasePlayGameCount();
            actor.changeState(_6_PlayGameState.class);
        }
    }

    @Override
    protected void onExit(TapTapActor actor) {
        logger.debug("TapTapActor idx[{}] - onExit : {}", actor.getIndex(), getStateName());
    }
}

