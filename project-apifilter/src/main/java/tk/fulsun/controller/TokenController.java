package tk.fulsun.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tk.fulsun.annotation.AutoICheckToken;
import tk.fulsun.service.TokenUtilService;

/**
 * @author fulsun
 * @description: 测试token的 Controller 类
 * @date 6/10/2021 11:10 AM
 */
@Slf4j
@RestController
public class TokenController {
  @Autowired private TokenUtilService tokenService;

  /**
   * 获取 Token 接口
   *
   * @return Token 串
   */
  @GetMapping("/token")
  public String getToken() {
    // 获取用户信息（这里使用模拟数据）
    // 注：这里存储该内容只是举例，其作用为辅助验证，使其验证逻辑更安全，如这里存储用户信息，其目的为:
    // - 1)、使用"token"验证 Redis 中是否存在对应的 Key
    // - 2)、使用"用户信息"验证 Redis 的 Value 是否匹配。
    String userInfo = "fulsun";
    // 获取 Token 字符串，并返回
    return tokenService.generateToken(userInfo);
  }

  /**
   * 接口幂等性测试接口
   *
   * @param token 幂等 Token 串
   * @return 执行结果
   */
  @PostMapping("/test")
  public String test(
      @RequestHeader(value = "token") String token,
      @RequestParam(value = "userinfo", defaultValue = "fulsun", required = false)
          String userInfo) {
    // 获取用户信息（这里使用模拟数据）
    // String userInfo = "fulsun";
    // 根据 Token 和与用户相关的信息到 Redis 验证是否存在对应的信息
    boolean result = tokenService.validToken(token, userInfo);
    // 根据验证结果响应不同信息
    return result ? "正常调用" : "重复调用";
  }

  /**
   * 访问该接口，如果Head 中不带 token测试是否会进入该方法
   *
   * @return
   */
  @AutoICheckToken
  @GetMapping("/testToken")
  public String testIdempotence() {
    return "成功";
  }
}
