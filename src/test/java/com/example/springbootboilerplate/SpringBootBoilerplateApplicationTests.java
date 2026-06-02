package com.example.springbootboilerplate;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled("CI 환경에서는 DB 없이 Spring context 로딩 테스트를 스킵")
@SpringBootTest
class SpringBootBoilerplateApplicationTests {

}
