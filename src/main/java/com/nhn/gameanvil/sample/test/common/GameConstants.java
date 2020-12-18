package com.nhn.gameanvil.sample.test.common;

/**
 * 게임에서 사용하는 상수값들 정의
 */
public class GameConstants {
    // 게임서비스 스테이스 이름
    public static final String GAME_NAME = "TapTap";

    // 게임스페이스에서 사용할 유저 타입
    public static final String GAME_USER_TYPE = "TapTapUser";

    // 게임스페이스의 싱글룸
    public static final String GAME_ROOM_TYPE_SINGLE = "SingleTapRoom";

    // 게임 스페이스의 멀티룸 - 무제한 탭 게임
    public static final String GAME_ROOM_TYPE_MULTI_ROOM_MATCH = "UnlimitedTapRoom";

    // 게임 스페이스의 멀티룸 - 스네이크 게임
    public static final String GAME_ROOM_TYPE_MULTI_USER_MATCH = "SnakeRoom";

    public static final int WAIT_TIME_OUT = 10000;
}
