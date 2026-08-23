package io.github.jacob66g.matchmovie.common.sort;

import io.github.jacob66g.matchmovie.common.exception.ApplicationException;
import io.github.jacob66g.matchmovie.common.exception.errorcode.CommonErrorCode;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


public final class SortWhitelist<E extends Enum<E> & SortableField> {

    private final Map<String, E> byApiName;
    private final List<String> allowedApiNames;

    private SortWhitelist(Class<E> fieldType) {
        this.byApiName = Arrays.stream(fieldType.getEnumConstants())
                .collect(Collectors.toMap(
                        field -> field.apiName().toLowerCase(Locale.ROOT),
                        Function.identity(),
                        (first, duplicate) -> first,
                        LinkedHashMap::new));
        this.allowedApiNames = byApiName.values().stream()
                .map(SortableField::apiName)
                .toList();
    }

    public static <E extends Enum<E> & SortableField> SortWhitelist<E> of(Class<E> fieldType) {
        return new SortWhitelist<>(fieldType);
    }

    public Pageable sanitize(Pageable pageable) {
        if (pageable.isUnpaged() || pageable.getSort().isUnsorted()) {
            return pageable;
        }

        List<Sort.Order> orders = pageable.getSort().stream()
                .map(this::toEntityOrder)
                .toList();

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(orders));
    }

    private Sort.Order toEntityOrder(Sort.Order order) {
        E field = byApiName.get(order.getProperty().toLowerCase(Locale.ROOT));

        if (field == null) {
            throw new ApplicationException(CommonErrorCode.INVALID_SORT_PROPERTY)
                    .param("property", order.getProperty())
                    .param("allowed", allowedApiNames)
                    .with("sortProperty", order.getProperty());
        }

        return order.withProperty(field.entityPath());
    }
}
