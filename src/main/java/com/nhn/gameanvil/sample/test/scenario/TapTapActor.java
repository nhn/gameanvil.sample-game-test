package com.nhn.gameanvil.sample.test.scenario;

import com.nhn.gameanvil.gamehammer.scenario.ScenarioActor;
import com.nhn.gameanvil.gamehammer.tester.User;

// 시나리오 유저
public class TapTapActor extends ScenarioActor<TapTapActor> {
    private User user;
    private int playGameCount = 0;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getPlayGameCount() {
        return playGameCount;
    }

    public void resetPlayGameCount() {
        playGameCount = 0;
    }

    public void increasePlayGameCount() {
        playGameCount++;
    }
}
