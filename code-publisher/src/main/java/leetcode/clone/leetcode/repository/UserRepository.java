package leetcode.clone.leetcode.repository;

import leetcode.clone.leetcode.response.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {}
