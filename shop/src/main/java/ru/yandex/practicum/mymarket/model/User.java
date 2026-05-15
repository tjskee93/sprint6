package ru.yandex.practicum.mymarket.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table("users")
public class User {
    @Id
    private Long id;
    private String username;
    private String password;
    private boolean enabled;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

}
