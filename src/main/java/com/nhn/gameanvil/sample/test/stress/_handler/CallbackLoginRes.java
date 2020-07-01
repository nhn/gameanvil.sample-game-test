package com.nhn.gameanvil.sample.test.stress._handler;

import static org.junit.Assert.assertEquals;

import com.nhn.gameanvil.sample.test.common.GameConstants;
import com.nhn.gameanvil.sample.protocol.GameSingle;
import com.nhn.gameanvil.sample.protocol.GameSingle.DifficultyType;
import com.nhn.gameanvilcore.connector.callback.parent.IDispatchPacket;
import com.nhn.gameanvilcore.connector.protocol.Packet;
import com.nhn.gameanvilcore.connector.protocol.result.LoginResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.nhn.gameanvil.sample.test.stress.SampleUserClass;

public class CallbackLoginRes implements IDispatchPacket<SampleUserClass> {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void dispatch(Packet packet, SampleUserClass user) {

        LoginResult result = user.parseLoginResult(packet);
        assertEquals(true, result.isSuccess());

        user.setSendCount(0);

        GameSingle.StartGameReq.Builder startGameReq = GameSingle.StartGameReq.newBuilder();
        startGameReq.setDeck("sushi");
        startGameReq.setDifficulty(DifficultyType.DIFFICULTY_NORMAL);
        user.createRoom(GameConstants.GAME_ROOM_TYPE_SINGLE, startGameReq);
    }

}
