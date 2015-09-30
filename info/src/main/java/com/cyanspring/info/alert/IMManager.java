package com.cyanspring.info.alert;

import com.cyanspring.common.alert.SendNotificationRequestEvent;
import com.cyanspring.common.event.ScheduleManager;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by xiaowenda on 2015/9/21.
 */
public class IMManager extends Compute {
    private static final Logger log = LoggerFactory
            .getLogger(AlertManager.class);

    @Autowired
    SessionFactory sessionFactory;

    @Autowired
    ScheduleManager scheduleManager;

    @Override
    public void SubscirbetoEvents() {
    }

    @Override
    public void SubscribetoEventsMD() {
        SubscirbetoEvent(SendNotificationRequestEvent.class);
    }

    @Override
    public void init() {

    }

    @Override
    public void processSendNotificationRequestEvent(SendNotificationRequestEvent event,
                                                    List<Compute> computes) {
    }
}
