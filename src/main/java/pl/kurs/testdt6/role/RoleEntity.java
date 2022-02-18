package pl.kurs.testdt6.role;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import pl.kurs.testdt6.account.AccountEntity;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "role")
public class RoleEntity implements GrantedAuthority {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator")
    private String roleId;
    private String name;

    @JsonIgnore
    @ManyToMany(mappedBy = "roles")
    private Set<AccountEntity> accounts = new HashSet<>();

    @Override
    public String getAuthority() {
        String prefix = "ROLE_";
        SimpleGrantedAuthority grantedAuthority = new SimpleGrantedAuthority(prefix + name);
        return grantedAuthority.toString();
    }
}
