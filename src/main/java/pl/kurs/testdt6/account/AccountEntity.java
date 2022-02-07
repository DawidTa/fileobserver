package pl.kurs.testdt6.account;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pl.kurs.testdt6.job.JobEntity;
import pl.kurs.testdt6.role.RoleEntity;

import javax.persistence.*;
import java.util.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "account",
        uniqueConstraints = {@UniqueConstraint(name = "UniqueUsernameAndEmail",
                columnNames = {"username", "email"})})
public class AccountEntity implements UserDetails {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator")
    private String accountId;
    private String username;
    private String password;
    private String name;
    private String lastname;
    private String email;
    private boolean isActive;
    private String activationToken;

    @ManyToMany(
            cascade = {CascadeType.MERGE, CascadeType.PERSIST},
            fetch = FetchType.EAGER
    )
    @JoinTable(
            name = "users_jobs",
            joinColumns = @JoinColumn(name = "account_id"),
            inverseJoinColumns = @JoinColumn(name = "job_id")
    )
    private Set<JobEntity> jobs = new HashSet<>();

    @ManyToMany(
            cascade = {CascadeType.MERGE, CascadeType.PERSIST},
            fetch = FetchType.EAGER
    )
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(name = "account_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<RoleEntity> roles = new HashSet<>();

    public void addJob(JobEntity jobEntity) {
        jobEntity.getAccounts().add(this);
        this.jobs.add(jobEntity);
    }

    public void removeJob(JobEntity jobEntity) {
        this.jobs.remove(jobEntity);
        jobEntity.getAccounts().remove(this);
    }

    public void removeJobFromAllAccounts(JobEntity jobEntity) {
        for (AccountEntity account : jobEntity.getAccounts()) {
            account.getJobs().remove(jobEntity);
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String prefix = "ROLE_";
        Set<RoleEntity> roles = this.getRoles();
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        for (RoleEntity role : roles) {
            authorities.add(new SimpleGrantedAuthority(prefix + role.getName()));
        }
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }
}
