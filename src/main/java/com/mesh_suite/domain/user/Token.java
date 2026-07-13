package com.mesh_suite.domain.user;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "token")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Token{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "access_token")
    private String accessToken;

    @Column(name = "is_logged_out", nullable = false)
    private boolean loggedOut = false;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user;

    @CreationTimestamp
    @Column(name = "created_on")
    private LocalDateTime createdOn;

}
