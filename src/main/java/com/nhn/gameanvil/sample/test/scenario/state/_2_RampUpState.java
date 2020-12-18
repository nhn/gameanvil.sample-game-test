package com.nhn.gameanvil.sample.test.scenario.state;

import static org.slf4j.LoggerFactory.getLogger;

import com.nhn.gameanvil.gamehammer.config.TesterConfigLoader;
import com.nhn.gameanvil.gamehammer.event.EventContainer;
import com.nhn.gameanvil.gamehammer.scenario.State;
import com.nhn.gameanvil.gamehammer.tester.Statistics;
import com.nhn.gameanvil.sample.test.scenario.TapTapActor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.slf4j.Logger;

// 접속 속도조절 요청 상태
public class _2_RampUpState extends State<TapTapActor> {
    private static final Logger logger = getLogger(_2_RampUpState.class);
    private static EventContainer<Long> eventConnectComplete = new EventContainer<Long>();
    private final AtomicInteger connectCount = new AtomicInteger(0);
    private long enterTime;

    @Override
    protected void onEnter(TapTapActor actor) {
        logger.debug("TapTapActor idx[{}] - onEnter : {}", actor.getIndex(), getStateName());
        Consumer<Long> consumer = new Consumer<Long>() {
            @Override
            public void accept(Long connectionTime) {
                long delay = actor.getConnection().getUuid() * TesterConfigLoader.getInstance().getTesterConfig().getRampUpDelayMillis();
                actor.setTimer(() -> {
                    eventConnectComplete.remove(this);
                    actor.changeState(_3_AuthenticationState.class);
                }, delay);
            }
        };

        eventConnectComplete.add(consumer);
        if (TesterConfigLoader.getInstance().getTesterConfig().getActorCount() == connectCount.incrementAndGet()) {
            Statistics.getInstance().startRecord();
            connectCount.set(0);
            eventConnectComplete.eventNotify(System.currentTimeMillis() - enterTime);
        } else if (connectCount.get() == 0) {
            enterTime = System.currentTimeMillis();
        }
    }

    @Override
    protected void onExit(TapTapActor actor) {
        logger.debug("TapTapActor idx[{}] - onExit : {}", actor.getIndex(), getStateName());
    }
}
