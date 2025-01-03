package com.ttn.punchingSystem.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailDTO {

    @Id
    private String id;
    private long projectId;
    private List<String> defaulterUserEmails;
    private String totalHours;
    private String reportingManagerName;
    private String fromEmail;
    private List<String> toEmails;

}
