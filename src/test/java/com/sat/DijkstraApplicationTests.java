package com.sat;

import com.sat.controller.EdgeController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DijkstraApplicationTests {

    @Autowired
    EdgeController edgeController;

    @Test
    void contextLoads() {
        int begin = edgeController.begin();
        System.out.println(begin);
    }

}
