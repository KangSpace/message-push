package org.kangspace.messagepush.core.dto.page;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 通用列表页请求DTO
 *
 * @author kango2gler@gmail.com
 * @since 2021-04-23
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class PageRequestDto {

    /**
     * 页索引，需大于等于1
     */
    @NotNull(message = "pageIndex不能为空")
    @Min(value = 1, message = "pageIndex必须大于等于1")
    private Integer pageNum;

    /**
     * 页大小
     */
    @NotNull(message = "pageSize不能为空")
    @Min(value = 1, message = "pageSize必须大于等于1")
    private Integer pageSize;

    /**
     * 查询字段，可使用ApiUtil.getFields辅助构建
     */
    @NotEmpty(message = "fields 字段不能为NULL和空集合")
    private List<String> fields;

    /**
     * 排序字符串,可使用ApiUtil.getOrders、ApiUtil.getAscOrder、ApiUtil.getDescOrder辅助构建
     */
    private List<Order> orders;

}
