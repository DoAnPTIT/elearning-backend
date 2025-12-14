package com.doanptit.elearing_backend_service.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CourseCategory {
    ARTIFICIAL_INTELLIGENCE_OF_THINGS("Trí tuệ nhân tạo vạn vật (AIoT)"),
    INFORMATION_TECHNOLOGY("Công nghệ thông tin"),
    ARTIFICIAL_INTELLIGENCE("Trí tuệ nhân tạo"),
    GAME_DESIGN_AND_DEVELOPMENT("Thiết kế và phát triển Game"),
    MULTIMEDIA_COMMUNICATION("Truyền thông đa phương tiện"),
    ELECTRONICS_AND_TELECOMMUNICATION_ENGINEERING("Kỹ thuật Điện tử viễn thông"),
    INFORMATION_SECURITY("An toàn thông tin"),
    ELECTRICAL_AND_ELECTRONIC_ENGINEERING_TECHNOLOGY("Công nghệ kỹ thuật Điện, điện tử"),
    MULTIMEDIA_TECHNOLOGY("Công nghệ đa phương tiện"),
    MARKETING("Marketing"),
    BUSINESS_ADMINISTRATION("Quản trị kinh doanh"),
    ACCOUNTING("Kế toán"),
    E_COMMERCE("Thương mại điện tử"),
    FINANCIAL_TECHNOLOGY("Công nghệ tài chính (Fintech)"),
    CONTROL_AND_AUTOMATION_ENGINEERING("Kỹ thuật Điều khiển và Tự động hoá"),
    COMPUTER_SCIENCE("Khoa học máy tính"),
    JOURNALISM("Báo chí"),
    COMPUTER_NETWORKS_AND_DATA_COMMUNICATIONS("Mạng máy tính và truyền thông dữ liệu"),
    PUBLIC_RELATIONS("Quan hệ công chúng"),
    SEMICONDUCTOR_MICROELECTRONICS_TECHNOLOGY("Công nghệ vi mạch bán dẫn"),
    LOGISTICS_AND_SUPPLY_CHAIN_MANAGEMENT("Logistics và Quản lý chuỗi cung ứng");

    private final String vietnameseName;

    CourseCategory(String vietnameseName) {
        this.vietnameseName = vietnameseName;
    }

    @JsonValue
    public String getVietnameseName() {
        return vietnameseName;
    }

    @JsonCreator
    public static CourseCategory fromString(String value) {
        if (value == null) {
            return null;
        }
        
        // Try to match by Vietnamese name first
        for (CourseCategory category : CourseCategory.values()) {
            if (category.vietnameseName.equalsIgnoreCase(value)) {
                return category;
            }
        }
        
        // Fallback to enum name
        try {
            return CourseCategory.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid category: " + value);
        }
    }
}
