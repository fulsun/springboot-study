package tk.fulsun.demo;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author fulsun
 * @description: 主测试类
 * @date 6/8/2021 4:26 PM
 */
// @SpringBootTest
@Slf4j
class LogApplicationTest {
  Logger logger = LoggerFactory.getLogger(getClass());

  @Test
  public void contextLoads() {
    // 日志的级别；
    // 由低到高   trace<debug<info<warn<error
    // 可以调整输出的日志级别；日志就只会在这个级别以以后的高级别生效
    logger.trace("这是trace日志...");
    logger.debug("这是debug日志...");
    // SpringBoot 默认给我们使用的是 info 级别的，没有指定级别的就用SpringBoot 默认规定的级别；root 级别
    logger.info("这是info日志...");
    logger.warn("这是warn日志...");
    logger.error("这是error日志...");
    log.info("message by @slf4j");
  }
}
