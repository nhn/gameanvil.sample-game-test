package com.nhn.gameanvil.sample.test.common;

import com.nhn.gameanvil.sample.protocol.Authentication;
import com.nhn.gameanvil.sample.protocol.GameMulti;
import com.nhn.gameanvil.sample.protocol.GameSingle;
import com.nhn.gameanvil.sample.protocol.Result;
import com.nhn.gameanvil.sample.protocol.User;
import com.nhn.gameanvilcore.connector.tcp.GameAnvilConnector;

public class Initializer {
    public static GameAnvilConnector initConnector() {
        // 커넥터 생성.
        GameAnvilConnector connector = GameAnvilConnector.getInstance();

        // 컨텐츠 프로토콜 등록.
        connector.addProtoBufClass(0, Authentication.class);
        connector.addProtoBufClass(1, GameMulti.class);
        connector.addProtoBufClass(2, GameSingle.class);
        connector.addProtoBufClass(3, Result.class);
        connector.addProtoBufClass(4, User.class);

        // 컨텐츠 서비스 등록.
        connector.addService(1, GameConstants.GAME_NAME);

        return connector;
    }
}
