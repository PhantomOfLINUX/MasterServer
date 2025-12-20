package org.codequistify.master;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("실제 인프라 연동이 필요한 E2E 성격의 컨텍스트 로드 테스트로 개발 중 임시 비활성화")
class MasterApplicationTests {

  @Test
  void contextLoads() {}
}
