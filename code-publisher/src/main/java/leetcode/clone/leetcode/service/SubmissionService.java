package leetcode.clone.leetcode.service;

import leetcode.clone.leetcode.repository.SubmissionRepository;
import leetcode.clone.leetcode.response.Submission;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionRepository submissionRepository;

    public Submission getSubmissionById(Long id) {
        return submissionRepository.findById(id).orElse(null);
    }

    public List<Submission> getAllSubmissions() {
        Iterable<Submission> submissionIterator = submissionRepository.findAll();
        List<Submission> submissions = new ArrayList<>();
        submissionIterator.forEach(submissions::add);
        return submissions;
    }

    @Transactional
    public void save(Submission submission) {
        submissionRepository.save(submission);
    }
}
