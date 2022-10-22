package com.rakutenmobile.messageapi.usermessage.adapter.in.restful;

import com.rakutenmobile.messageapi.usermessage.domain.UserMessage;
import com.rakutenmobile.messageapi.usermessage.port.in.MessageUseCase;
import com.rakutenmobile.messageapi.usermessage.port.out.PublishMessageUseCase;
import com.rakutenmobile.openapi.models.Message;
import com.rakutenmobile.openapi.models.MessagesGet200Response;
import com.rakutenmobile.openapi.models.Pagination;
import com.rakutenmobile.openapi.models.SubmitMessageRequest;
import com.rakutenmobile.openapi.spring.reactive.api.MessageApi;
import com.rakutenmobile.openapi.spring.reactive.api.MessagesApi;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
public class MessageController implements MessagesApi, MessageApi {

    private final PublishMessageUseCase publisher;
    private final MessageUseCase messageUseCase;

    private final ReactiveKafkaProducerTemplate<String, String> kafka;

    public MessageController(PublishMessageUseCase publisher, MessageUseCase messageUseCase, ReactiveKafkaProducerTemplate<String, String> kafka) {
        this.publisher = publisher;
        this.messageUseCase = messageUseCase;
        this.kafka = kafka;
    }

    @Override
    public Mono<ResponseEntity<MessagesGet200Response>> messagesGet(Integer page, Integer pageSize, String userId, String topic, ServerWebExchange exchange) {
        return messageUseCase.findAll(PageRequest.of(page - 1, pageSize), Optional.ofNullable(userId),
                        Optional.ofNullable(topic))
                .map(result -> {
                    MessagesGet200Response dto = new MessagesGet200Response();

                    List<Message> dtoMessages = result.getContent().stream().map(item -> {
                        Message messageDto = new Message();
                        messageDto.userId(item.getUserId());
                        messageDto.topic(item.getTopic());
                        messageDto.createdAt(item.getCreatedAt());
                        messageDto.content(item.getContent());
                        messageDto.setId(item.getId());
                        return messageDto;
                    }).collect(Collectors.toList());

                    Pagination paginationDto = new Pagination();
                    paginationDto.page(page);
                    paginationDto.pageSize(pageSize);
                    paginationDto.totalItems(result.getTotalElements());
                    paginationDto.totalPage(result.getTotalPages());

                    dto.setItems(dtoMessages);
                    dto.setPagination(paginationDto);
                    return new ResponseEntity<>(dto, HttpStatus.OK);
                });
    }

    @Override
    public Mono<ResponseEntity<Void>> messagesPost(Flux<SubmitMessageRequest> submitMessageRequest, ServerWebExchange exchange) {
        Hooks.onOperatorDebug();
        Flux<UserMessage> sources = exchange.getPrincipal()
                .map(v -> v.getName())
                .flatMapMany(usr -> submitMessageRequest.flatMap(req -> {
                    UserMessage userMessage = UserMessage.builder()
                            .content(req.getContent())
                            .topic(req.getTopic())
                            .createdAt(OffsetDateTime.now())
                            .userId(usr)
                            .build();
                    return Flux.just(userMessage);
                }));
        return publisher.publish(sources).thenReturn(new ResponseEntity<>(null, HttpStatus.OK));
    }

    @Override
    public Mono<ResponseEntity<Void>> messageIdDelete(UUID id, ServerWebExchange exchange) {
        return messageUseCase.deleteMessageById(id).thenReturn(new ResponseEntity<>(null, HttpStatus.OK));
    }

    @Override
    public Mono<ResponseEntity<Message>> messageIdGet(UUID id, ServerWebExchange exchange) {
        Mono<UserMessage> message = messageUseCase.getMessageById(id);
        return message.map(r -> {
            Message dto = new Message();
            dto.id(r.getId());
            dto.content(r.getContent());
            dto.createdAt(r.getCreatedAt());
            dto.topic(r.getTopic());
            dto.userId(r.getUserId());
            return new ResponseEntity<>(dto, HttpStatus.OK);
        });
    }
}
