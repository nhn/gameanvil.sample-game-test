package com.nhn.gameanvil.sample.test.stress._handler;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.nhn.gameanvil.sample.test.common.GameConstants;
import com.nhn.gameanvil.sample.protocol.GameSingle;
import com.nhn.gameanvil.sample.protocol.GameSingle.DifficultyType;
import com.nhn.gameanvil.sample.protocol.Result.ErrorCode;
import com.nhn.gameanvil.sample.protocol.User;
import com.nhn.gameanvilcore.connector.callback.parent.IDispatchPacket;
import com.nhn.gameanvilcore.connector.protocol.Packet;
import java.io.IOException;
import com.nhn.gameanvil.sample.test.stress.SampleUserClass;

public class CallbackShuffleDeckRes implements IDispatchPacket<SampleUserClass> {

    @Override
    public void dispatch(Packet packet, SampleUserClass user) {

        try {
            User.ShuffleDeckRes shuffleDeckRes = User.ShuffleDeckRes.parseFrom(packet.getStream());
            assertTrue(shuffleDeckRes.getResultCode() == ErrorCode.NONE);
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }

        GameSingle.StartGameReq.Builder startGameReq = GameSingle.StartGameReq.newBuilder();
        startGameReq.setDeck("sushi");
        startGameReq.setDifficulty(DifficultyType.DIFFICULTY_NORMAL);
        user.createRoom(GameConstants.GAME_ROOM_TYPE_SINGLE, startGameReq);
    }
}
