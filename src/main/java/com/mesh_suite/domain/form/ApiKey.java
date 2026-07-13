package com.mesh_suite.domain.form;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "api_key")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiKey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String username;
    private String password;

    @Column(name = "client_name")
    private String clientName;

    @Column(nullable = false, columnDefinition = "boolean default true")
    private Boolean enabled = true;

    @CreationTimestamp
    @Column(name ="created_on")
    private LocalDateTime createdOn;

}
