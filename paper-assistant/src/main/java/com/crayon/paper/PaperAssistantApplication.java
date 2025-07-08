package com.crayon.paper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class PaperAssistantApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaperAssistantApplication.class, args);
    }


    //@Bean
    //public CommandLineRunner commandLineRunner(DeepSeekService deepSeekService) {
    //	return args -> {
    //		String question = "请搜索最新数据，用一句告诉我Java在AI领域的未来。";
    //		String answer = deepSeekService.chat(question);
    //		log.info("Answer: {}", answer);
    //	};
    //}
}
