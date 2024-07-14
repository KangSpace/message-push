package org.kangspace.messagepush.core.dto.page;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * 通用列表页响应DTO
 *
 * @author kango2gler@gmail.com
 * @since 2021-04-23
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponseDto<T> {

    /**
     * 总记录数
     */
    private Long totalCount;

    /**
     * 总页数
     */
    private Long totalPages;

    /**
     * 响应记录列表
     */
    private List<T> list;

}
