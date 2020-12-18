package com.nhn.gameanvil.sample.test.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.slf4j.LoggerFactory.getLogger;

import com.nhn.gameanvil.gamehammer.tester.PacketResult;
import com.nhn.gameanvil.gamehammer.tester.ResultCreateRoom;
import com.nhn.gameanvil.gamehammer.tester.ResultLeaveRoom;
import com.nhn.gameanvil.gamehammer.tester.ResultLogout;
import com.nhn.gameanvil.gamehammer.tester.ResultMatchRoom;
import com.nhn.gameanvil.gamehammer.tester.ResultMatchUserStart;
import com.nhn.gameanvil.gamehammer.tester.User;
import com.nhn.gameanvil.sample.protocol.GameMulti;
import com.nhn.gameanvil.sample.protocol.GameMulti.TapBirdUserData;
import com.nhn.gameanvil.sample.protocol.GameSingle;
import com.nhn.gameanvil.sample.protocol.GameSingle.DifficultyType;
import com.nhn.gameanvil.sample.protocol.GameSingle.EndType;
import com.nhn.gameanvil.sample.protocol.GameSingle.TapMsg;
import com.nhn.gameanvil.sample.protocol.Result.ErrorCode;
import com.nhn.gameanvil.sample.protocol.User.CurrencyType;
import com.nhn.gameanvil.sample.test.common.GameConstants;
import com.nhn.gameanvilcore.protocol.Error;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;

public class CheckGame extends Fixture {
    private static final Logger logger = getLogger(CheckGame.class);

    //-------------------------------------------------------------------------------------

    @BeforeClass
    public static void beforeClass() {
        // 테스트 하려는 서버의 IP 와 Port 를 지정합니다.
        initConnector("127.0.0.1", 11200);
    }

    @AfterClass
    public static void afterClass() {
        resetConnect();
    }

    @After
    public void after() {
        // 테스트 한번 끝날때 마다 모든 커넥션 해제
        closeAllConnections();
    }

    //-------------------------------------------------------------------------------------

    @Test
    public void loginSuccess() {
        // 기본 로그인
        User user = getCreateUser(GameConstants.GAME_NAME, GameConstants.GAME_USER_TYPE);

        // 로그아웃
        ResultLogout logoutResult = logout(user);
        // 로그아웃 성공 확인
        assertTrue(logoutResult.isSuccess());
    }

    @Test
    public void deckShuffle() throws TimeoutException, ExecutionException, InterruptedException {
        User user = getCreateUser(GameConstants.GAME_NAME, GameConstants.GAME_USER_TYPE);

        // 덱교체 요청 메세지 생성
        com.nhn.gameanvil.sample.protocol.User.ShuffleDeckReq.Builder shuffleDeckReq = com.nhn.gameanvil.sample.protocol.User.ShuffleDeckReq.newBuilder();
        shuffleDeckReq.setCurrencyType(CurrencyType.CURRENCY_COIN);
        shuffleDeckReq.setUsage(1);

        // 덱교체 게임컨텐츠 프로토콜 요청/응답
        PacketResult packetResult = user.request(shuffleDeckReq.build()).get(GameConstants.WAIT_TIME_OUT, TimeUnit.MILLISECONDS);
        try {
            // 응답 패킷 확인
            com.nhn.gameanvil.sample.protocol.User.ShuffleDeckRes shuffleDeckRes = com.nhn.gameanvil.sample.protocol.User.ShuffleDeckRes.parseFrom(packetResult.getStream());
            logger.info("CheckGame::deckShuffle Deck[{}]", shuffleDeckRes.getDeck());
            assertTrue(shuffleDeckRes.getResultCode() == ErrorCode.NONE);
        } catch (IOException e) {
            logger.error("CheckGame::deckShuffle()", e);
        }

        // 로그아웃
        ResultLogout logoutResult = logout(user);
        assertTrue(logoutResult.isSuccess());
    }

    @Test
    public void singleGamePlay() {
        User user = getCreateUser(GameConstants.GAME_NAME, GameConstants.GAME_USER_TYPE);

        // 싱글게임 입장 메세지 생성
        GameSingle.StartGameReq.Builder startGameReq = GameSingle.StartGameReq.newBuilder();
        startGameReq.setDeck("sushi");
        startGameReq.setDifficulty(DifficultyType.DIFFICULTY_NORMAL);

        // 방생성
        ResultCreateRoom res = createRoom(user, GameConstants.GAME_ROOM_TYPE_SINGLE, startGameReq);
        assertTrue(res.isSuccess());
        assertNotEquals(0, res.getRoomId());

        // 게임 플레이 탭한 정보 전달
        TapMsg.Builder tapMsg = TapMsg.newBuilder();
        for (int i = 1; i < 5; i++) {
            tapMsg.setSelectCardName("sushi_0" + i);
            tapMsg.setCombo(i);
            tapMsg.setTapScore(100 * i);
            user.send(tapMsg.build());
        }

        // 게임 종료 패킷
        GameSingle.EndGameReq.Builder endGameReq = GameSingle.EndGameReq.newBuilder();
        endGameReq.setEndType(EndType.GAME_END_TIME_UP);
        // 게임 종료
        ResultLeaveRoom leaveRoomRes = leaveRoom(user, endGameReq);
        assertTrue(leaveRoomRes.isSuccess());

        // 로그아웃
        ResultLogout logoutResult = logout(user);
        assertTrue(logoutResult.isSuccess());
    }

    @Test
    public void unlimitedTapGamePlay() {
        // 유저 4명 생성
        List<User> userList = getCreateUsers(GameConstants.GAME_NAME, GameConstants.GAME_USER_TYPE, 4);
        int score = 0;
        for (User user : userList) {
            // 전체 전달용 탭 메세지 리스너 등록
            user.addListener(GameMulti.BroadcastTapBirdMsg.getDescriptor(), packetResult -> {
                try {
                    GameMulti.BroadcastTapBirdMsg message = GameMulti.BroadcastTapBirdMsg.parseFrom(packetResult.getStream());
                    assertTrue(message != null);
                    for (TapBirdUserData userData : message.getTapBirdDataList()) {
                        logger.info("BroadcastTapBirdMsg NickName {}", userData.getUserData().getNickName());
                        assertFalse(userData.getUserData().getId().isEmpty());
                    }
                } catch (IOException e) {
                    logger.error("BroadcastTapBirdMsg parse Error", e);
                }

                assertTrue(packetResult.isSuccess());
            });

            score++;
            // 매치룸 요청
            ResultMatchRoom resultMatchRoom = matchRoom(user, GameConstants.GAME_ROOM_TYPE_MULTI_ROOM_MATCH, true, false);
            assertTrue(resultMatchRoom.isSuccess());
            assertNotEquals(0, resultMatchRoom.getRoomId());

            // 점수 전송
            GameMulti.ScoreUpMsg.Builder scoreUpMsg = GameMulti.ScoreUpMsg.newBuilder();
            scoreUpMsg.setScore(score);
            user.send(scoreUpMsg.build());
        }

        // 방 나가기
        userListLeaveRoom(userList);
        // 로그아웃
        userListLogout(userList);
    }

    @Test
    public void snakeGamePlay() {
        // 유저 2명 생성
        List<User> userList = getCreateUsers(GameConstants.GAME_NAME, GameConstants.GAME_USER_TYPE, 2);

        AtomicInteger loopFlag = new AtomicInteger(0);
        CompletableFuture loopFuture = new CompletableFuture();
        int score = 0;
        for (com.nhn.gameanvil.gamehammer.tester.User user : userList) {
            // 유저 매치 완료 메세지 리스너 등록
            user.addListenerMatchUserDoneNoti(resultMatchUserDone -> {
                assertEquals(Error.ErrorCode.MATCH_USER_DONE_SUCCESS, resultMatchUserDone.getErrorCode());
                if (resultMatchUserDone.getErrorCode() == Error.ErrorCode.MATCH_USER_DONE_SUCCESS) {
                    logger.info("SnakeGamePlay userId:{}, roomId:{}", user.getUserId(), resultMatchUserDone.getRoomId());
                    if (userList.size() == loopFlag.incrementAndGet()) {
                        loopFuture.complete(null);
                    }
                }
            });

            // 스네이크 게임 food 메세지 리스너 등록
            user.addListener(GameMulti.SnakeFoodMsg.getDescriptor(), packetResult -> {
                try {
                    GameMulti.SnakeFoodMsg message = GameMulti.SnakeFoodMsg.parseFrom(packetResult.getStream());
                    logger.info("SnakeGamePlay SnakeFoodMsg:idx {}", message.getFoodData().getIdx());
                    assertTrue(message != null);
                    assertFalse(message.getFoodData().getIdx() < 0);
                } catch (IOException e) {
                    logger.error("SnakeFoodMsg parse Error", e);
                }

                assertTrue(packetResult.isSuccess());
            });

            // 스네이크 게임 유저 메세지 리스터 등록
            user.addListener(GameMulti.SnakeUserMsg.getDescriptor(), packetResult -> {
                try {
                    GameMulti.SnakeUserMsg message = GameMulti.SnakeUserMsg.parseFrom(packetResult.getStream());
                    logger.info("SnakeGamePlay UserData:id {}", message.getUserData().getBaseData().getId());
                    assertTrue(message != null);
                    assertFalse(message.getUserData().getBaseData().getId().isEmpty());
                } catch (IOException e) {
                    logger.error("SnakeUserMsg parse Error", e);
                }

                assertTrue(packetResult.isSuccess());
            });

            score++;

            // 유저 매치 요청
            ResultMatchUserStart resultMatchUserStart = matchUserStart(user, GameConstants.GAME_ROOM_TYPE_MULTI_USER_MATCH);
            assertTrue(resultMatchUserStart.isSuccess());
        }

        // 유저 매칭이 완료되어 패킷이 오는 지 확인
        try {
            loopFuture.get(5000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("CheckGame::SnakeGamePlay() : loopFuture.get()", e);
            fail();
        }

        // 스네이크 게임 food 생성 수신 할수 있도록 대기
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (User user : userList) {
            score++;
            // 스네이크 게임 정보
            GameMulti.RoomUserData.Builder roomUserData = GameMulti.RoomUserData.newBuilder();
            roomUserData.setScore(score);
            roomUserData.setId(String.valueOf(user.getUserId()));

            GameMulti.SnakeUserData.Builder snakeUserData = GameMulti.SnakeUserData.newBuilder();
            snakeUserData.setBaseData(roomUserData);

            for (int i = 1; i < 3; i++) {
                // 유저 스네이크 위치
                GameMulti.SnakePositionData.Builder snakePositionData = GameMulti.SnakePositionData.newBuilder();
                snakePositionData.setIdx(i);
                snakePositionData.setX(i + 10);
                snakePositionData.setY(i + 20);
                snakeUserData.addUserPositionListData(snakePositionData);
            }

            // 스네이크게임 유저 정보 전달
            GameMulti.SnakeUserMsg.Builder snakeUserMsg = GameMulti.SnakeUserMsg.newBuilder();
            snakeUserMsg.setUserData(snakeUserData);
            user.send(snakeUserMsg.build());
        }

        // 방 나가기
        userListLeaveRoom(userList);
        // 로그아웃
        userListLogout(userList);
    }
}
