package io.confluent.developer.spring;

import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

@SpringBootApplication
public class SpringCcloudApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringCcloudApplication.class, args);
	}

}

@Component
@RequiredArgsConstructor
class Producer {

	private final KafkaTemplate<Integer, String> template;

	Faker faker;


	@EventListener(ApplicationStartedEvent.class)
	public void generate() {

		faker = Faker.instance();
		Flux<Long> interval = Flux.interval(Duration.ofMillis(1_00));

		Flux<String> quotes = Flux.fromStream(Stream.generate((() -> faker.hobbit().quote())));

		Flux.zip(interval, quotes).map( it -> template.send("hobbit",
				faker.random().nextInt(42), it.getT2())).blockLast();
	}

}
