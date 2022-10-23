package com.rakutenmobile.messageapi.usermessage.port.service;

import com.rakutenmobile.messageapi.usermessage.adapter.out.persistence.MessageEntity;
import com.rakutenmobile.messageapi.usermessage.adapter.out.persistence.MessageRepository;
import com.rakutenmobile.messageapi.usermessage.domain.exception.MessageNotFoundException;
import com.rakutenmobile.messageapi.usermessage.domain.exception.MessageNotOwnedException;
import com.rakutenmobile.messageapi.usermessage.port.in.MessageUseCase;
import com.rakutenmobile.messageapi.usermessage.domain.UserMessage;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

public class MessageService implements MessageUseCase {
    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Override
    public Mono<UserMessage> submitMessage(UserMessage message) {
        MessageEntity entity = MessageEntity.builder().content(message.getContent())
                .topic(message.getTopic()).createdAt(message.getCreatedAt()).userId(message.getUserId()).build();
        Mono<MessageEntity> result = messageRepository.save(entity);
        return result.map(r -> UserMessage.builder().id(r.getId())
                .content(r.getContent())
                .topic(r.getTopic())
                .createdAt(r.getCreatedAt())
                .userId(r.getUserId()).build());
    }

    @Override
    public Mono<UserMessage> getMessageById(UUID id) {
        return messageRepository.findById(id)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new MessageNotFoundException("Message not found"))))
                .map(r -> UserMessage.builder().id(r.getId())
                        .content(r.getContent())
                        .topic(r.getTopic())
                        .createdAt(r.getCreatedAt())
                        .userId(r.getUserId()).build());
    }

    @Override
    public Mono<Void> deleteMessageById(UUID id) {
        return messageRepository.findById(id)
                .switchIfEmpty(Mono.error(new MessageNotFoundException("message not found")))
                .then(ReactiveSecurityContextHolder.getContext())
                .map(context -> context.getAuthentication().getPrincipal())
                .cast(UserDetails.class)
                .flatMap(v -> messageRepository.findMessageEntityByIdAndUserId(id, v.getUsername()))
                .switchIfEmpty(
                        Mono.defer(
                                () -> Mono.error(
                                        new MessageNotOwnedException(
                                                "Forbid to delete other user's message"
                                        )
                                )
                        )
                ).flatMap(d -> messageRepository.deleteById(id))
                .then();
    }

    @Override
    public Mono<Page<UserMessage>> findAll(PageRequest pageRequest, Optional<String> userId, Optional<String> topic) {
        Flux<MessageEntity> data;
        Pageable paging = pageRequest.withSort(Sort.by("createdAt").descending());
        if (userId.isPresent() && topic.isPresent()) {
            data = messageRepository.findAllByUserIdAndTopicContainingIgnoreCase(
                    paging,
                    userId.get(),
                    topic.get()
            );
        }else if (userId.isPresent()) {
            data = messageRepository.findAllByUserId(paging, userId.get());
        }else if (topic.isPresent()) {
            data = messageRepository.findAllByTopicContainsIgnoreCase(paging, topic.get());
        }else {
            data = messageRepository.findAllBy(
                    paging
            );
        }
        return data.map(messageEntity -> UserMessage.builder()
                        .id(messageEntity.getId())
                        .userId(messageEntity.getUserId())
                        .topic(messageEntity.getTopic())
                        .content(messageEntity.getContent())
                        .createdAt(messageEntity.getCreatedAt())
                        .build())
                .collectList().zipWith(messageRepository.count())
                .map(item -> new PageImpl<>(item.getT1(), pageRequest, item.getT2()));
    }
}
