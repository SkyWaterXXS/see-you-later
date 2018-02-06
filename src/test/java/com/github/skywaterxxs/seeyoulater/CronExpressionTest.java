package com.github.skywaterxxs.seeyoulater;

import java.text.ParseException;
import java.util.Date;

import org.junit.Test;

/**
 * @author xuxiaoshuo 2018/2/2
 */
public class CronExpressionTest {

    @Test
    public void test1() throws ParseException {

        CronExpression cronExpression = new CronExpression("0 0 * * * ?");

        System.out.println(cronExpression.getNextValidTimeAfter(new Date()));
    }
}
