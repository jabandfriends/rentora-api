package com.rentora.api.model.entity.elastic;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rentora.api.constant.enums.UserRole;
import lombok.*;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.util.UUID;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "apartment_users")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApartmentUserDocument {

    @Id
    private UUID apartmentUserId;
    private UUID userId;

    @Field(type = FieldType.Text, analyzer = "thai_edge_ngram")
    private String fullName;
    private String email;
    private String phoneNumber;

    private String role;
    private Boolean isActive;
    private Boolean accountStatus;
    private Boolean occupiedStatus;
    private String unitName;
    private UUID apartmentId;
    private Long createdAt;
}
