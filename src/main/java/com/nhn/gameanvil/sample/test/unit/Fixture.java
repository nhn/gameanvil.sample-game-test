package com.nhn.gameanvil.sample.test.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.slf4j.LoggerFactory.getLogger;

import com.google.protobuf.GeneratedMessageV3;
import com.nhn.gameanvil.gamehammer.config.RemoteInfo;
import com.nhn.gameanvil.gamehammer.tester.Connection;
import com.nhn.gameanvil.gamehammer.tester.Tester;
import com.nhn.gameanvil.gamehammer.tester.User;
import com.nhn.gameanvil.gamehammer.tester.result.ResultAuthentication;
import com.nhn.gameanvil.gamehammer.tester.result.ResultConnect;
import com.nhn.gameanvil.gamehammer.tester.result.ResultConnect.ResultCodeConnect;
import com.nhn.gameanvil.gamehammer.tester.result.ResultCreateRoom;
import com.nhn.gameanvil.gamehammer.tester.result.ResultLeaveRoom;
import com.nhn.gameanvil.gamehammer.tester.result.ResultLogin;
import com.nhn.gameanvil.gamehammer.tester.result.ResultLogout;
import com.nhn.gameanvil.gamehammer.tester.result.ResultMatchRoom;
import com.nhn.gameanvil.gamehammer.tester.result.ResultMatchUserStart;
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

    // ??????????????? ????????? ???????????? ?????? ?????? ??????
    protected List<User> getCreateUsers(String serviceName, String userType, int count) {
        List<User> userList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            userList.add(getCreateUser(serviceName, userType));
        }
        return userList;
    }

    // ??????????????? ????????? ?????? ??????
    protected User getCreateUser(String serviceName, String userType) {
        // ????????? ??????
        Connection connection = createConnection(uuidCounter.incrementAndGet());

        // ??????
        ResultConnect resultConnect = connect(connection);
        assertEquals(ResultCodeConnect.CONNECT_SUCCESS, resultConnect.getResultCode());

        // ?????? ????????? ?????? ?????? ?????? ??????
        Authentication.AuthenticationReq.Builder authenticationReq = Authentication.AuthenticationReq.newBuilder().setAccessToken("TapTap_AccessToken!!!!");

        // ?????? ??????
        ResultAuthentication res = authentication(connection, String.valueOf(connection.getUuid()), String.valueOf(connection.getUuid()), connectionDeviceIdGenerator.generateUniqueId(), authenticationReq);
        assertTrue(res.isSuccess());

        // ?????? ??????
        User user = connection.createUser(serviceName, 1);

        // ????????? ?????????
        Authentication.LoginReq.Builder loginReq = Authentication.LoginReq.newBuilder();
        loginReq.setUuid(String.valueOf(connection.getUuid()));
        loginReq.setLoginType(LoginType.LOGIN_GUEST);
        loginReq.setAppVersion("0.0.1");
        loginReq.setAppStore("NONE");
        loginReq.setDeviceModel("PC");
        loginReq.setDeviceCountry("KR");
        loginReq.setDeviceLanguage("ko");

        // ????????? ??????
        ResultLogin loginRes = login(user, userType, user.getChannelId(), loginReq);
        assertTrue(loginRes.isSuccess());

        logger.info("User AccountId:{}, UserId:{}, SubId:{}, ChannelId:{}", user.getConnection().getAccountId(), user.getUserId(), user.getSubId(), user.getChannelId());
        assertNotEquals(user.getUserId(), 0);   // ?????? ???????????? ???????????? ??????
        return user;
    }

    // ????????? ??????
    protected static void initConnector(String ipAddress, int port) {
        if (null != tester) {
            tester.close();
        }

        tester = Tester.newBuilder()
            .addTargetServer(new RemoteInfo(ipAddress, port))
            .setDefaultPacketTimeoutSeconds(3)
            .addProtoBufClass(0, Authentication.getDescriptor())
            .addProtoBufClass(1, GameMulti.getDescriptor())
            .addProtoBufClass(2, GameSingle.getDescriptor())
            .addProtoBufClass(3, Result.getDescriptor())
            .addProtoBufClass(4, com.nhn.gameanvil.sample.protocol.User.getDescriptor())
            .Build();
    }

    // ????????? ??????
    protected static void resetConnect() {
        if (tester != null) {
            tester.close();
            tester = null;
        }
    }

    // ?????? ????????? ??????
    protected static void closeAllConnections() {
        tester.closeAllConnections();
    }

    // ????????? ??????
    protected Connection createConnection(int uuid) {
        return tester.createConnection(uuid);
    }

    // ????????? ??????
    protected ResultConnect connect(Connection connection) {
        Future<ResultConnect> future = connection.connect(connection.getConfig().getNextTargetServer());
        try {
            return future.get(GameConstants.WAIT_TIME_OUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Fixture::connect()", e);
        }
        return null;
    }

    // ?????? ??????
    protected ResultAuthentication authentication(Connection connection, String accountId, String password, String deviceId, GeneratedMessageV3.Builder<?>... sendPayloads) {
        Future<ResultAuthentication> completableFuture = connection.authentication(accountId, password, deviceId, sendPayloads);
        try {
            return completableFuture.get(GameConstants.WAIT_TIME_OUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Fixture::authentication()", e);
        }
        return null;
    }

    // ????????? ??????
    protected ResultLogin login(User user, String userType, String channelId, GeneratedMessageV3.Builder<?>... payloads) {
        Future<ResultLogin> completableFuture = user.login(userType, channelId, payloads);
        try {
            return completableFuture.get(GameConstants.WAIT_TIME_OUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Fixture::login()", e);
        }
        return null;
    }

    // ???????????? ??????
    protected ResultLogout logout(User user, GeneratedMessageV3.Builder<?>... payloads) {
        Future<ResultLogout> completableFuture = user.logout(payloads);

        try {
            return completableFuture.get(GameConstants.WAIT_TIME_OUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Fixture::logout()", e);
        }

        return null;
    }

    protected void userListLeaveRoom(List<User> userList, GeneratedMessageV3.Builder<?>... payload) {
        for (User user : userList) {
            ResultLeaveRoom leaveRoomRes = leaveRoom(user);
            assertTrue(leaveRoomRes.isSuccess());
        }
    }

    // ??????????????????
    protected void userListLogout(List<User> userList, GeneratedMessageV3.Builder<?>... payload) {
        for (User user : userList) {
            // ????????????
            ResultLogout logoutResult = logout(user);
            assertTrue(logoutResult.isSuccess());
        }
    }

    // ????????? ??????
    protected ResultCreateRoom createRoom(User user, String roomType, GeneratedMessageV3.Builder<?>... sendPayloads) {
        Future<ResultCreateRoom> completableFuture = user.createRoom(roomType, sendPayloads);

        try {
            return completableFuture.get(GameConstants.WAIT_TIME_OUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Fixture::createRoom()", e);
        }

        return null;
    }

    // ??? ????????? ??????
    protected ResultLeaveRoom leaveRoom(User user, GeneratedMessageV3.Builder<?>... sendPayloads) {
        Future<ResultLeaveRoom> completableFuture = user.leaveRoom(sendPayloads);

        try {
            return completableFuture.get(GameConstants.WAIT_TIME_OUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Fixture::leaveRoom()", e);
        }
        return null;
    }

    // ????????? ??????
    protected ResultMatchRoom matchRoom(User user, String roomType, String matchingGroup, String matchingUserCategory, boolean isCreate, boolean isMoveRoom, GeneratedMessageV3.Builder<?>... sendPayloads) {
        Future<ResultMatchRoom> completableFuture = user.matchRoom(roomType, matchingGroup, matchingUserCategory, isCreate, isMoveRoom, sendPayloads);

        try {
            return completableFuture.get(GameConstants.WAIT_TIME_OUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Fixture::matchRoom()", e);
        }

        return null;
    }

    // ?????? ?????? ??????
    protected ResultMatchUserStart matchUserStart(User user, String roomType, String matchingGroup, GeneratedMessageV3.Builder<?>... sendPayloads) {
        Future<ResultMatchUserStart> completableFuture = user.matchUserStart(roomType, matchingGroup, sendPayloads);

        try {
            return completableFuture.get(GameConstants.WAIT_TIME_OUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Fixture::matchUserStart()", e);
        }

        return null;
    }

}
