package com.infobro.apaar.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "institutions", indexes = {
    @Index(name = "idx_inst_code", columnList = "code")
})
public class Institution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 32)
    private String code;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 64)
    private String state;

    @Column(nullable = false, length = 64)
    private String city;

    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";

    public Institution() {}

    public Institution(String code, String name, String state, String city, String status) {
        this.code = code;
        this.name = name;
        this.state = state;
        this.city = city;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
