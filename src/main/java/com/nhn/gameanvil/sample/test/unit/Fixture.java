package com.nhn.gameanvil.sample.test.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.slf4j.LoggerFactory.getLogger;

import com.google.protobuf.GeneratedMessageV3;
import com.nhn.gameanvil.gamehammer.tester.Connection;
import com.nhn.gameanvil.gamehammer.tester.RemoteInfo;
import com.nhn.gameanvil.gamehammer.tester.ResultAuthentication;
import com.nhn.gameanvil.gamehammer.tester.ResultConnect;
import com.nhn.gameanvil.gamehammer.tester.ResultConnect.ResultCodeConnect;
import com.nhn.gameanvil.gamehammer.tester.ResultCreateRoom;
import com.nhn.gameanvil.gamehammer.tester.ResultLeaveRoom;
import com.nhn.gameanvil.gamehammer.tester.ResultLogin;
import com.nhn.gameanvil.gamehammer.tester.ResultLogout;
import com.nhn.gameanvil.gamehammer.tester.ResultMatchRoom;
import com.nhn.gameanvil.gamehammer.tester.ResultMatchUserStart;
import com.nhn.gameanvil.gamehammer.tester.Tester;
import com.nhn.gameanvil.gamehammer.tester.User;
import com.nhn.gameanvil.gamehammer.tool.UuidGenerator;
import com.nhn.gameanvil.sample.protocol.Authentication;
import com.nhn.gameanvil.sample.protocol.Authentication.LoginType;
import com.nhn.gameanvil.sample.protocol.GameMulti;
import com.nhn.gameanvil.sample.protocol.GameSingle;
import com.nhn.gameanvil.sample.protocol.Result;
import com.nhn.gameanvil.sample.test.common.GameConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;

public class Fixture {
    private static final Logger logger = getLogger(Fixture.class);
    private static Tester tester = null;
    protected static UuidGenerator connectionDeviceIdGenerator = new UuidGenerator("DeviceId");
    protected static AtomicInteger uuidCounter = new AtomicInteger(0);

    // 로그인까지 완료된 전달받은 명수 만큼 생성
    protected List<User> getCreateUsers(String serviceName, String userType, int count) {
        List<User> userList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            userList.add(getCreateUser(serviceName, userType));
        }
        return userList;
    }

    // 로그인까지 완료된 유저 생성
    protected User getCreateUser(String serviceName, String userType) {
        // 커넥션 생성
        Connection connection = createConnection(uuidCounter.incrementAndGet());

        // 연결
        ResultConnect resultConnect = connect(connection);
        assertEquals(ResultCodeConnect.CONNECT_SUCCESS, resultConnect.getResultCode());

        // 인증 검증을 위한 토큰 정보 생성
        Authentication.AuthenticationReq.Builder authenticationReq = Authentication.AuthenticationReq.newBuilder().setAccessToken("TapTap_AccessToken!!!!");

        // 인증 진행
        ResultAuthentication res = authentication(connection, String.valueOf(connection.getUuid()), String.valueOf(connection.getUuid()), connectionDeviceIdGenerator.generateUniqueId(), authenticationReq);
        assertTrue(res.isSuccess());

        // 유저 생성
        User user = connection.createUser(serviceName, 1);

        // 로그인 데이터
        Authentication.LoginReq.Builder loginReq = Authentication.LoginReq.newBuilder();
        loginReq.setUuid(String.valueOf(connection.getUuid()));
        loginReq.setLoginType(LoginType.LOGIN_GUEST);
        loginReq.setAppVersion("0.0.1");
        loginReq.setAppStore("NONE");
        loginReq.setDeviceModel("PC");
        loginReq.setDeviceCountry("KR");
        loginReq.setDeviceLanguage("ko");

        // 로그인 진행
        ResultLogin loginRes = login(user, userType, user.getChannelId(), loginReq);
        assertTrue(loginRes.isSuccess());

        logger.info("User AccountId:{}, UserId:{}, SubId:{}, ChannelId:{}", user.getConnection().getAccountId(), user.getUserId(), user.getSubId(), user.getChannelId());
        assertNotEquals(user.getUserId(), 0);   // 유저 아이디가 유효한지 확인
        return user;
    }

    // 테스터 생성
    protected static void initConnector(String ipAddress, int port) {
        if (null != tester) {
            tester.close();
        }

        tester = Tester.newBuilder()
            .addRemoteInfo(new RemoteInfo(ipAddress, port))
            .setDefaultPacketTimeoutSeconds(3)
            .addProtoBufClass(0, Authentication.getDescriptor())
            .addProtoBufClass(1, GameMulti.getDescriptor())
            .addProtoBufClass(2, GameSingle.getDescriptor())
            .addProtoBufClass(3, Result.getDescriptor())
            .addProtoBufClass(4, com.nhn.gameanvil.sample.protocol.User.getDescriptor())
            .Build();
    }

    // 테스터 해제
    protected static void resetConnect() {
        if (tester != null) {
            tester.close();
            tester = null;
        }
    }

    // 모든 커넥션 해제
    protected static void closeAllConnections() {
        tester.closeAllConnections();
    }

    // 커넥션 생성
    protected Connection createConnection(int uuid) {
        return tester.createConnection(uuid);
    }

    // 커넥션 연결
    protected ResultConnect connect(Connection connection) {
        Future<ResultConnect> future = connection.connect(connection.getConfig().getNextRemoteInfo());
        try {
            return future.get(GameConstants.WAIT_TIME_OUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.error("InterruptedException", e);
        } catch (ExecutionException e) {
            logger.error("ExecutionException", e);
        } catch (TimeoutException e) {
            logger.error("TimeoutException", e);
        }
        return null;
    }

    // 인증 요청
    protected ResultAuthentication authentication(Connection connection, String accountId, String password, String deviceId, GeneratedMessageV3.Builder<?>... sendPayloads) {
        Future<ResultAuthentication> completableFuture = connection.authentication(accountId, password, deviceId, sendPayloads);
        try {
            return completableFuture.get(GameConstants.WAIT_TIME_OUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("[Future] - ", e);
        }
        return null;
    }

    // 로그인 요청
    protected ResultLogin login(User user, String userType, String channelId, GeneratedMessageV3.Builder<?>... payloads) {
        Future<ResultLogin> completableFuture = user.login(userType, channelId, payloads);
        try {
            return completableFuture.get(GameConstants.WAIT_TIME_OUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("[Future] - ", e);
        }
        return null;
    }

    // 로그아웃 요청
    protected ResultLogout logout(User user, GeneratedMessageV3.Builder<?>... payloads) {
        Future<ResultLogout> completableFuture = user.logout(payloads);

        try {
            return completableFuture.get(GameConstants.WAIT_TIME_OUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("[Future] - ", e);
        }

        return null;
    }

    protected void userListLeaveRoom(List<User> userList, GeneratedMessageV3.Builder<?>... payload) {
        for (User user : userList) {
            ResultLeaveRoom leaveRoomRes = leaveRoom(user);
            assertTrue(leaveRoomRes.isSuccess());
        }
    }

    // 로그아웃처리
    protected void userListLogout(List<User> userList, GeneratedMessageV3.Builder<?>... payload) {
        for (User user : userList) {
            // 로그아웃
            ResultLogout logoutResult = logout(user);
            assertTrue(logoutResult.isSuccess());
        }
    }

    // 룸생성 요청
    protected ResultCreateRoom createRoom(User user, String roomType, GeneratedMessageV3.Builder<?>... sendPayloads) {
        Future<ResultCreateRoom> completableFuture = user.createRoom(roomType, sendPayloads);

        try {
            return completableFuture.get(GameConstants.WAIT_TIME_OUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("[Future] - ", e);
        }

        return null;
    }

    // 룸 나가기 요청
    protected ResultLeaveRoom leaveRoom(User user, GeneratedMessageV3.Builder<?>... sendPayloads) {
        Future<ResultLeaveRoom> completableFuture = user.leaveRoom(sendPayloads);

        try {
            return completableFuture.get(GameConstants.WAIT_TIME_OUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("[Future] - ", e);
        }
        return null;
    }

    // 매치룸 요청
    protected ResultMatchRoom matchRoom(User user, String roomType, boolean isCreate, boolean isMoveRoom, GeneratedMessageV3.Builder<?>... sendPayloads) {
        Future<ResultMatchRoom> completableFuture = user.matchRoom(roomType, isCreate, isMoveRoom, sendPayloads);

        try {
            return completableFuture.get(GameConstants.WAIT_TIME_OUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("[Future] - ", e);
        }

        return null;
    }

    // 유저 매치 요청
    protected ResultMatchUserStart matchUserStart(User user, String roomType, GeneratedMessageV3.Builder<?>... sendPayloads) {
        Future<ResultMatchUserStart> completableFuture = user.matchUserStart(roomType, sendPayloads);

        try {
            return completableFuture.get(GameConstants.WAIT_TIME_OUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("[Future] - ", e);
        }

        return null;
    }
}
