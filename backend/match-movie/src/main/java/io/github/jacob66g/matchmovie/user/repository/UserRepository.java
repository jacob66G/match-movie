package io.github.jacob66g.matchmovie.user.repository;

import io.github.jacob66g.matchmovie.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

}
