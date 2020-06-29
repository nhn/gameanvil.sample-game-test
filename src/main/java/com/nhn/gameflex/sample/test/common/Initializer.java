package com.nhn.gameflex.sample.test.common;

import com.nhn.gameflex.sample.protocol.Authentication;
import com.nhn.gameflex.sample.protocol.GameMulti;
import com.nhn.gameflex.sample.protocol.GameSingle;
import com.nhn.gameflex.sample.protocol.Result;
import com.nhn.gameflex.sample.protocol.User;
import com.nhn.gameflexcore.connector.tcp.GameflexConnector;

public class Initializer {
    public static GameflexConnector initConnector() {
        // 커넥터 생성.
        GameflexConnector connector = GameflexConnector.getInstance();

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
