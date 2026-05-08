package com.rental.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 小区树形结构DTO
 */
@Data
public class CommunityTreeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String type;  // community 或 building

    private Long parentId;

    private String nodeKey;  // 唯一标识: community_1 或 building_1

    private List<CommunityTreeDTO> children;
}
