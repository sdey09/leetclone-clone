package leetcode.clone.leetcode.controller;


import leetcode.clone.leetcode.response.Submission;
import leetcode.clone.leetcode.response.User;
import leetcode.clone.leetcode.service.SubmissionService;
import leetcode.clone.leetcode.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = {"/api", "/api/"})
@RequiredArgsConstructor
public class MainController {

    private final UserService userService;
    private final SubmissionService submissionService;

    @GetMapping
    public String getWhetherServiceIsRunning() {
        return "Service is running";
    }

    @GetMapping({"/submission/{id}", "/submission/{id}/"})
    public Submission getSubmissionById(@PathVariable("id") Long id) {
        return submissionService.getSubmissionById(id);
    }

    @GetMapping({"/submission/all", "/submission/all/"})
    public List<Submission> getAllSubmissionList() {
        return submissionService.getAllSubmissions();
    }

    @PostMapping({"/submission/"})
    public void addSubmission(@RequestBody Submission submission) {
        submissionService.save(submission);
    }

    @GetMapping({"/users/{id}", "/users/{id}/"})
    public User getUserById(@PathVariable("id") Long id) {
        Optional<User> user = userService.getUserById(id);
        return user.orElse(null);
    }

    @GetMapping({"/users/all", "/users/all/"})
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }
}