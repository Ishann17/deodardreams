package com.deodardreams;

import com.deodardreams.testconfig.MySqlTestContainerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(MySqlTestContainerConfig.class)
class BookingServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
