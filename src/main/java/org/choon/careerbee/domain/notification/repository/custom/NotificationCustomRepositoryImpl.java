package org.choon.careerbee.domain.notification.repository.custom;

import static org.choon.careerbee.domain.notification.entity.QNotification.notification;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.choon.careerbee.domain.notification.dto.response.FetchNotiResp;
import org.choon.careerbee.domain.notification.dto.response.FetchNotiResp.NotificationInfo;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class NotificationCustomRepositoryImpl implements NotificationCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public FetchNotiResp fetchNotificationsByMemberId(
        Long accessMemberId, Long cursor, int size
    ) {
        List<NotificationInfo> notifications = queryFactory
            .select(Projections.constructor(
                NotificationInfo.class,
                notification.id,
                notification.type,
                notification.content,
                notification.createdAt,
                notification.isRead))
            .from(notification)
            .where(
                notification.member.id.eq(accessMemberId),
                cursorCondition(cursor)
            )
            .orderBy(notification.id.desc())
            .limit(size + 1)
            .fetch();

        boolean hasNext = notifications.size() > size;
        if (hasNext) {
            notifications.remove(size);
        }

        Long nextCursor = hasNext ? notifications.getLast().id() : null;
        return new FetchNotiResp(
            notifications,
            nextCursor,
            hasNext
        );
    }

    private BooleanExpression cursorCondition(Long cursor) {
        return cursor != null
            ? notification.id.lt(cursor)
            : null;
    }
}
