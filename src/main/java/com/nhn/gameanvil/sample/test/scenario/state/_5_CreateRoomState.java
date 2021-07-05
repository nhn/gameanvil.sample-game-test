package com.nhn.gameanvil.sample.test.scenario.state;

import static org.slf4j.LoggerFactory.getLogger;

import com.nhn.gameanvil.gamehammer.scenario.State;
import com.nhn.gameanvil.sample.protocol.GameSingle;
import com.nhn.gameanvil.sample.protocol.GameSingle.DifficultyType;
import com.nhn.gameanvil.sample.test.common.GameConstants;
import com.nhn.gameanvil.sample.test.scenario.TapTapActor;
import org.slf4j.Logger;

// 방생성 요청 상태
public class _5_CreateRoomState extends State<TapTapActor> {
    private static final Logger logger = getLogger(_5_CreateRoomState.class);

    @Override
    protected void onEnter(TapTapActor actor) {
        logger.debug("TapTapActor idx[{}] - onEnter : {}", actor.getIndex(), getStateName());

        // 싱글게임 입장 메세지 생성
        GameSingle.StartGameReq.Builder startGameReq = GameSingle.StartGameReq.newBuilder();
        startGameReq.setDeck("sushi");
        startGameReq.setDifficulty(DifficultyType.DIFFICULTY_NORMAL);

        // 방생성
        actor.getUser().createRoom(createRoomResult -> {
            if (createRoomResult.isSuccess()) {
                actor.changeState(_6_PlayGameState.class);
            } else {
                logger.warn(
                    "[{}] Fail - uuid : {}, AccountId : {}\t{}, {}",
                    getStateName(),
                    actor.getConnection().getUuid(),
                    actor.getConnection().getAccountId(),
                    createRoomResult.getErrorCode(),
                    createRoomResult.getPacketResultCode()
                );
                actor.changeState(_8_LogoutState.class);
            }
        }, GameConstants.GAME_ROOM_TYPE_SINGLE, startGameReq);

    }

    @Override
    protected void onExit(TapTapActor actor) {
        logger.debug("TapTapActor idx[{}] - onExit : {}", actor.getIndex(), getStateName());
    }
}

